<template>
  <div class="page">
    <div class="toolbar">
      <button @click="openNew">新增产品</button>
      
      <select v-model="filterCategory" @change="load" class="filter-select">
        <option value="">所有类别</option>
        <option value="药品">药品</option>
        <option value="医疗设备">医疗设备</option>
        <option value="防护用品">防护用品</option>
      </select>
      
      <select v-model="filterStatus" @change="load" class="filter-select">
        <option value="">所有状态</option>
        <option value="on_sale">在售</option>
        <option value="discontinued">停售</option>
      </select>
      
      <input v-model="keyword" placeholder="按产品名称或编号搜索" @input="load" class="search-input" />

      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <section class="table-container">
      <table class="table">
        <thead>
          <tr>
            <th>产品编号</th>
            <th>产品名称</th>
            <th>类别</th>
            <th>规格</th>
            <th>单位</th>
            <th>单价</th>
            <th>状态</th>
            <th>操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="p in list" :key="p.id">
            <td>{{ p.productNo }}</td>
            <td>{{ p.name }}</td>
            <td>{{ p.category }}</td>
            <td>{{ p.specification }}</td>
            <td>{{ p.unit }}</td>
            <td>¥{{ p.price.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) }}</td>
            <td>
              <span :class="['status-badge', getStatusClass(p.status)]">
                {{ getStatusText(p.status) }}
              </span>
            </td>
            <td class="actions-cell">
              <button 
                @click="setOnSale(p)"
                :disabled="isOnSale(p.status)"
                :class="['btn', { 'disabled': isOnSale(p.status) }]"
              >上架</button>
              <button 
                @click="setDiscontinued(p)"
                :disabled="isDiscontinued(p.status)"
                :class="['btn', { 'disabled': isDiscontinued(p.status) }]"
              >下架</button>
              <button class="btn success" @click="edit(p)">编辑</button>
              <button class="btn danger" @click="remove(p)">删除</button>
            </td>
          </tr>
          <tr v-if="!list.length">
            <td colspan="8" class="empty">无数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <Pagination 
      :total="totalRecords" 
      :pageSize="pageSize" 
      :currentPage="currentPage"
      @update:currentPage="currentPage = $event"
    />

    <div v-if="showForm" class="modal">
      <div class="modal-card">
        <h3>{{ editing?.id ? '编辑产品' : '新增产品' }}</h3>
        <div class="scroll-content">
          <div class="form-row">
            <label>产品编号</label>
            <input v-model="form.productNo" :disabled="true" placeholder="系统自动生成" style="background-color: #f5f5f5; cursor: not-allowed;" />
          </div>
          <div class="form-row"><label>产品名称</label><input v-model="form.name" placeholder="请输入产品名称" /></div>
          <div class="form-row">
            <label>类别</label>
            <select v-model="form.category">
              <option value="" :disabled="form.category !== ''" selected>请选择产品类别</option>
              <option value="药品">药品</option>
              <option value="医疗设备">医疗设备</option>
              <option value="防护用品">防护用品</option>
            </select>
          </div>
          <div class="form-row"><label>规格</label><input v-model="form.specification" placeholder="请输入产品规格，如: 500mg*20片/盒" /></div>
          <div class="form-row"><label>单位</label><input v-model="form.unit" placeholder="请输入产品单位，如: 件/盒/台" /></div>
          <div class="form-row"><label>单价</label><input type="number" step="0.01" v-model.number="form.price" /></div>
          <div class="form-row"><label>描述</label><textarea v-model="form.description" rows="3"></textarea></div>
        </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeForm">取消</button>
          <button class="btn-save" @click="save">保存</button>
        </div>
      </div>
    </div>

    <div v-if="showConfirmDialog" class="modal">
      <div class="modal-card confirm-dialog">
        <h3>{{ confirmTitle }}</h3>
        <p class="confirm-message">{{ confirmMessage }}</p>
        <div class="modal-actions">
          <button @click="handleConfirm" class="primary">确认</button>
          <button @click="showConfirmDialog = false" class="muted">取消</button>
        </div>
      </div>
    </div>

    <AuditLogModal 
      :show="showAllLogsModal"
      :entity-name="'Product'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { productApi } from '../services/api';
import Pagination from './Pagination.vue';
import AuditLogModal from './AuditLogModal.vue';

interface Product {
  id?: number
  productNo: string
  name: string
  category: string
  specification: string
  unit: string
  price: number
  status: string
  description?: string
}

const userRole = ref(localStorage.getItem('ms_role') || 'admin');
const isAdmin = computed(() => userRole.value === 'admin');

const list = ref<Product[]>([]);
const allData = ref<Product[]>([]); // 存储所有数据
const keyword = ref('');
const filterCategory = ref('');
const filterStatus = ref('');
const showForm = ref(false);
const showAllLogsModal = ref(false);
const showConfirmDialog = ref(false);
const confirmTitle = ref('');
const confirmMessage = ref('');
const confirmAction = ref<(() => void) | null>(null);

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});
const editing = ref<Product | null>(null);
const form = reactive({
  productNo: '',
  name: '',
  category: '',
  specification: '',
  unit: '',
  price: 0,
  description: ''
});

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

function isOnSale(status: string | undefined) {
  if (!status) return false;
  const normalized = String(status).trim().toLowerCase();
  return normalized === 'on_sale' || normalized === 'onsale';
}

function isDiscontinued(status: string | undefined) {
  if (!status) return false;
  const normalized = String(status).trim().toLowerCase();
  return normalized === 'discontinued';
}

function getStatusText(status: string | undefined) {
  console.log('原始status值:', status, '类型:', typeof status);
  return isOnSale(status) ? '在售' : '停售';
}

function getStatusClass(status: string | undefined) {
  return isOnSale(status) ? 'status-active' : 'status-inactive';
}

async function load() {
  try {
    const res = keyword.value.trim()
      ? await productApi.search(keyword.value)
      : await productApi.getAll();
    if (res.code === 200) {
      let data = res.data || [];
      console.log('加载的商品数据:', data.map((p: Product) => ({ name: p.name, status: p.status })));
      if (filterCategory.value) {
        data = data.filter((p: Product) => p.category === filterCategory.value);
      }
      if (filterStatus.value) {
        data = data.filter((p: Product) => p.status === filterStatus.value);
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载产品列表失败:', err);
  }
}

function openNew() {
  editing.value = null
  form.productNo = ''
  form.name = ''
  form.category = ''
  form.specification = ''
  form.unit = ''
  form.price = 0
  form.description = ''
  showForm.value = true
  // 获取下一个产品编号
  productApi.getNextNo().then(res => {
    if (res.code === 200) {
      form.productNo = res.data;
    }
  }).catch(err => {
    console.error('获取下一个产品编号失败:', err);
  });
}

function openAllLogs() {
  showAllLogsModal.value = true;
}

function edit(item: Product) {
  editing.value = item;
  form.productNo = item.productNo;
  form.name = item.name;
  form.category = item.category;
  form.specification = item.specification;
  form.unit = item.unit;
  form.price = item.price;
  form.description = item.description || '';
  showForm.value = true;
}

async function save() {
  if (!form.name.trim()) { alert('请输入产品名称'); return; }
  if (!form.category) { alert('请选择产品类别'); return; }
  if (!form.specification.trim()) { alert('请输入产品规格'); return; }

  try {
    if (editing.value?.id) {
      await productApi.update(editing.value.id, { ...form });
    } else {
      // 新增时不传productNo，让后端自动生成
      const { productNo, ...dataWithoutProductNo } = form;
      await productApi.create(dataWithoutProductNo);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { showForm.value = false; }

function showConfirm(title: string, message: string, action: () => void) {
  confirmTitle.value = title;
  confirmMessage.value = message;
  confirmAction.value = action;
  showConfirmDialog.value = true;
}

function handleConfirm() {
  showConfirmDialog.value = false;
  if (confirmAction.value) {
    confirmAction.value();
  }
}

async function setOnSale(item: Product) {
  if (isOnSale(item.status)) return;
  showConfirm('确认上架', `确认上架商品 ${item.name} 吗？`, async () => {
    try {
      if (item.id) {
        await productApi.update(item.id, { ...item, status: 'on_sale' });
        // 本地即时更新，避免重新加载导致行消失
        item.status = 'on_sale';
        updateDisplayList();
      }
    } catch (err: any) {
      alert('上架失败：' + (err?.response?.data?.message || err?.message));
    }
  });
}

async function setDiscontinued(item: Product) {
  if (isDiscontinued(item.status)) return;
  showConfirm('确认下架', `确认下架商品 ${item.name} 吗？`, async () => {
    try {
      if (item.id) {
        await productApi.update(item.id, { ...item, status: 'discontinued' });
        // 本地即时更新，避免重新加载导致行消失
        item.status = 'discontinued';
        updateDisplayList();
      }
    } catch (err: any) {
      alert('下架失败：' + (err?.response?.data?.message || err?.message));
    }
  });
}

async function remove(item: Product) {
  showConfirm('确认删除产品', `确认删除产品 ${item.name} ?`, async () => {
    try {
      if (item.id) {
        await productApi.delete(item.id);
        await load();
      }
    } catch (err: any) {
      alert('删除失败：' + (err?.response?.data?.message || err?.message));
    }
  });
}

onMounted(load);
</script>

<style scoped>
@import '../assets/table-styles.css';

.btn.disabled {
  opacity: 0.5;
  cursor: not-allowed !important;
  pointer-events: none;
}

/* 弹窗容器 */
.modal { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; border-radius: 12px; width: 500px; max-width: 90%; max-height: 85vh; padding: 24px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.modal-card h3 { margin-top: 0; margin-bottom: 20px; font-size: 20px; color: #1e293b; }

/* 表单行布局 */
.form-row { display: flex; align-items: center; gap: 16px; margin-bottom: 18px; }
.form-row label { width: 100px; text-align: justify; text-align-last: justify; font-size: 14px; color: #475569; font-weight: 500; flex-shrink: 0; }
.form-row input, .form-row select, .form-row textarea { flex: 1; width: 0; padding: 10px 14px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.form-row textarea { min-height: 70px; resize: vertical; }
.form-row.textarea-row { align-items: flex-start; }
.form-row.textarea-row label { padding-top: 10px; }

/* 按钮统一样式 */
.modal-actions { display: flex; justify-content: center; gap: 12px; padding-top: 20px; margin-top: 20px; border-top: 1px solid #e5e7eb; }
.modal-actions button { min-width: 100px; padding: 10px 24px; border: none; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; }
.btn-save { background: #6366f1; color: #fff; }
.btn-save:hover { background: #4f46e5; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4); }
.btn-cancel { background: #9ca3af; color: #fff; }
.btn-cancel:hover { background: #6b7280; }

.confirm-dialog { max-width: 520px; padding: 22px; }
.confirm-dialog h3 { font-size: 20px; margin: 0 0 6px 0; }
.confirm-message { margin: 18px 0 22px 0; font-size: 15px; color: #444; text-align: center; }
.modal-actions .primary { background: linear-gradient(180deg, #5b8cff, #3b6cff); color: #fff; }
.modal-actions .muted { background: #f3f6fb; color: #446; }
</style>
