<template>
  <div class="page">
    <div class="toolbar">
      <button v-if="!isAdmin" @click="openNew">新增催款记录</button>
      <select v-if="isAdmin" v-model.number="filter.salesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      
      <select v-model="filter.status" @change="load" class="filter-select">
        <option value="">所有状态</option>
        <option value="completed">已完成</option>
        <option value="pending">待回款</option>
      </select>
      
      <div class="date-group">
        <label>起始日期</label>
        <input type="date" v-model="filter.from" @change="load" class="date-input" :max="filter.to" />
      </div>
      <div class="date-group">
        <label>截止日期</label>
        <input type="date" v-model="filter.to" @change="load" class="date-input" :min="filter.from" />
      </div>
      
      <input type="text" v-model.trim="filter.keyword" placeholder="催款单号 / 订单号" @input="load" class="search-input" />
      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th style="width: 150px;">催款单号</th>
          <th style="width: 150px;">订单编号</th>
          <th style="width: 90px;">催款人</th>
          <th style="width: 110px;">催款日期</th>
          <th style="width: 120px;">本次回款</th>
          <th style="width: 120px;">剩余待回款</th>
          <th style="width: 90px;">状态</th>
          <th style="width: 200px;">备注</th>
          <th style="width: 150px;">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id">
          <td>{{ r.collectionNo }}</td>
          <td>{{ r.orderNo }}</td>
          <td>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</td>
          <td>{{ r.collectionDate }}</td>
          <td>
            <span v-if="r.currentAmount > 0" class="amount-positive">
              ¥{{ Number(r.currentAmount).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}
            </span>
            <span v-else class="amount-zero">仅催收</span>
          </td>
          <td>
            <span v-if="r.remainingAmount > 0" class="amount-warning">
              ¥{{ Number(r.remainingAmount).toLocaleString('zh-CN', { minimumFractionDigits: 2 }) }}
            </span>
            <span v-else class="order-completed">已结清</span>
          </td>
          <td>
            <span class="badge" :class="{
              'success': r.collectionStatus === 'completed',
              'warning': r.collectionStatus === 'pending'
            }">
              {{ r.collectionStatus === 'completed' ? '已完成' : '待回款' }}
            </span>
          </td>
          <td class="remark-cell">{{ r.remark || '-' }}</td>
          <td>
            <button @click="edit(r)">编辑</button>
            <button @click="remove(r)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="9" class="empty">暂无数据</td>
        </tr>
      </tbody>
    </table>

    <Pagination 
      :total="totalRecords" 
      :pageSize="pageSize" 
      :currentPage="currentPage"
      @update:currentPage="currentPage = $event"
    />

    <div v-if="showForm" class="modal">
      <div class="modal-card">
        <h3>{{ editing?.id ? '编辑催款记录' : '新增催款记录' }}</h3>
        
          <div v-if="editing?.collectionNo" class="form-row">
            <label>催款单号</label>
            <input type="text" v-model="editing.collectionNo" readonly class="readonly-input" />
          </div>
          
          <div v-if="isAdmin" class="form-row">
            <label>负责人 *</label>
            <select v-model.number="form.salesmanId" required>
              <option value="0" disabled selected>请选择负责人</option>
              <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
            </select>
          </div>
          
          <div class="form-row">
            <label>订单号 *</label>
            <select v-model="form.orderNo" required :disabled="!!editing">
              <option value="" disabled selected>请选择订单</option>
              <option v-for="order in approvedOrders" :key="order.orderNo" :value="order.orderNo">
                {{ order.orderNo }} - {{ order.customer?.name }} - ¥{{ order.totalAmount }}
              </option>
            </select>
          </div>
          
          <div class="form-row">
            <label>催款日期 *</label>
            <input type="date" v-model="form.collectionDate" required />
          </div>
          
          <div class="form-row">
            <label>本次回款金额 *</label>
            <input type="number" step="0.01" v-model.number="form.currentAmount" placeholder="0表示仅催收" required />
          </div>
          
          <div class="form-row">
            <label>剩余待回款金额 *</label>
            <input type="number" step="0.01" v-model.number="form.remainingAmount" required />
          </div>
          
          <div class="form-row textarea-row">
            <label>备注</label>
            <textarea v-model="form.remark" rows="3" placeholder="催收情况 / 付款方式 / 其他说明"></textarea>
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

    <!-- 催款记录类型日志弹窗 -->
    <AuditLogModal 
      :show="showAllLogsModal"
      :entity-name="'CollectionRecord'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { salesmanApi, collectionRecordApi, salesRecordApi } from '../services/api';
import Pagination from './Pagination.vue';
import AuditLogModal from './AuditLogModal.vue';

const props = defineProps<{
  salesmanId?: number
}>();

const userRole = ref(localStorage.getItem('ms_role') || 'salesman');
const salesmanIdStr = localStorage.getItem('ms_salesmanId')
const currentSalesmanId = ref(salesmanIdStr ? Number(salesmanIdStr) : props.salesmanId);
const isAdmin = computed(() => userRole.value === 'admin');

const salesmen = ref<any[]>([]);
const approvedOrders = ref<any[]>([]);
const list = ref<any[]>([]);
const allData = ref<any[]>([]);
const filter = reactive({ salesmanId: '', status: '', from: '', to: '', keyword: '' });

const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

watch(currentPage, () => {
  updateDisplayList();
});

const showForm = ref(false);
const showAllLogsModal = ref(false);
const editing = ref<any | null>(null);

const showConfirmDialog = ref(false);
const confirmTitle = ref('');
const confirmMessage = ref('');
const confirmAction = ref<(() => void) | null>(null);

const form = reactive({
  salesmanId: 0,
  orderNo: '',
  collectionDate: '',
  currentAmount: 0,
  remainingAmount: 0,
  collectionStatus: 'pending',
  remark: ''
});

// 加载销售员列表
async function loadSalesmen() {
  const res = await salesmanApi.getAll();
  if (res.code === 200) salesmen.value = res.data || [];
}

// 加载可供选择的订单 (仅显示 approved 状态)
async function loadApprovedOrders() {
  const role = userRole.value;
  // 如果是管理员，加载所有订单；如果是销售员，只加载自己的
  const querySalesmanId = isAdmin.value ? undefined : currentSalesmanId.value;
  
  try {
    const res = await salesRecordApi.getAll(role, querySalesmanId);
    if (res.code === 200) {
      approvedOrders.value = (res.data || []).filter((o: any) => o.status === 'approved');
    }
  } catch (e) {
    console.error("Failed to load orders", e);
  }
}

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

// 主加载函数
async function load() {
  try {
    const role = userRole.value;
    const querySalesmanId: number | undefined = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined)
      : currentSalesmanId.value;
    
    const res = await collectionRecordApi.getAll(role, querySalesmanId);
    if (res.code === 200) {
      let data = res.data || [];
      
      // 前端筛选
      if (filter.status) {
        data = data.filter((r: any) => r.collectionStatus === filter.status);
      }
      
      if (filter.from) {
        data = data.filter((r: any) => r.collectionDate >= filter.from);
      }
      if (filter.to) {
        data = data.filter((r: any) => r.collectionDate <= filter.to);
      }
      
      if (filter.keyword) {
        const kw = filter.keyword.toLowerCase();
        data = data.filter((r: any) => 
          (r.collectionNo && r.collectionNo.toLowerCase().includes(kw)) || 
          (r.orderNo && r.orderNo.toLowerCase().includes(kw))
        );
      }
      
      // 按日期倒序排列
      data.sort((a: any, b: any) => new Date(b.collectionDate).getTime() - new Date(a.collectionDate).getTime());

      allData.value = data;
      currentPage.value = 1;
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载催款记录失败:', err);
  }
}

function getSalesmanName(id: number) {
  return salesmen.value.find(x => x.id === id)?.name || id?.toString() || '-';
}

// 打开新增
function openNew() {
  editing.value = null;
  // 默认销售员：如果是销售员角色，就是自己；如果是管理员，默认选列表第一个或空
  const defaultSalesmanId = (!isAdmin.value && props.salesmanId) 
    ? props.salesmanId 
    :  0;
  
  form.salesmanId = defaultSalesmanId;
  form.orderNo = '';
  form.collectionDate = new Date().toISOString().split('T')[0] || '';
  form.currentAmount = 0;
  form.remainingAmount = 0;
  form.collectionStatus = 'pending';
  form.remark = '';
  showForm.value = true;
}

// 打开编辑 (关键修复：正确获取嵌套的 salesmanId)
function edit(item: any) {
  editing.value = item;
  // 优先从对象中获取 ID，其次尝试扁平字段
  form.salesmanId = item.salesman?.id || item.salesmanId || 0;
  form.orderNo = item.orderNo;
  form.collectionDate = item.collectionDate;
  form.currentAmount = item.currentAmount || 0;
  form.remainingAmount = item.remainingAmount || 0;
  form.collectionStatus = item.collectionStatus || 'pending';
  form.remark = item.remark || '';
  showForm.value = true;
}

// 保存逻辑 (关键修复：构造正确的 payload 结构)
async function save() {
  const finalSalesmanId = (!isAdmin.value && props.salesmanId) ? props.salesmanId : form.salesmanId;
  
  if (!finalSalesmanId) { alert('请选择负责人'); return; }
  if (!form.orderNo) { alert('请选择订单'); return; }
  if (!form.collectionDate) { alert('请选择日期'); return; }
  if (form.remainingAmount < 0) { alert('剩余金额不能为负数'); return; }

  try {
    // 使用扁平结构，让虚拟字段的setter正确工作
    // 注意：collectionStatus 是数据库自动生成的，不需要传递
    const payload = {
      orderNo: form.orderNo,
      collectionDate: form.collectionDate,
      currentAmount: form.currentAmount,
      remainingAmount: form.remainingAmount,
      remark: form.remark,
      salesmanId: finalSalesmanId
    };
    
    console.log('[CollectionList save] payload:', payload);
    
    if (editing.value?.id) {
      await collectionRecordApi.update(editing.value.id, payload);
    } else {
      await collectionRecordApi.create(payload, userRole.value);
    }
    showForm.value = false;
    await load();
  } catch (err: any) {
    console.error('[CollectionList save] error:', err);
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { 
  showForm.value = false; 
}

async function remove(item: any) {
  showConfirm('确认删除', '确认删除催款记录 ' + item.collectionNo + ' ?', async () => {
    try {
      if (item.id) {
        await collectionRecordApi.delete(item.id);
        await load();
      }
    } catch (err: any) {
      alert('删除失败：' + (err?.response?.data?.message || err?.message));
    }
  });
}

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

// 打开所有催款记录日志
function openAllLogs() {
  showAllLogsModal.value = true;
}

onMounted(async () => {
  await loadSalesmen();
  await loadApprovedOrders();
  await load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';

.amount-positive { color: #059669; font-weight: 600; }
.amount-zero { color: #9ca3af; font-style: italic; }
.amount-warning { color: #f59e0b; font-weight: 600; }
.order-completed { color: #059669; font-weight: 600; }
.remark-cell { max-width: 200px; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.form-row small { display: block; margin-top: 4px; color: #6b7280; font-size: 12px; }
.readonly-input { background: #f3f4f6; color: #6b7280; cursor: not-allowed; }
.date-input { padding: 8px; border: 1px solid #d1d5db; border-radius: 4px; font-size: 14px; }
.confirm-dialog { max-width: 520px; padding: 22px; }
.confirm-dialog h3 { font-size: 20px; margin: 0 0 6px 0; }
.confirm-message { margin: 18px 0 22px 0; font-size: 15px; color: #444; text-align: center; white-space: pre-line; }

/* 统一表单样式 */
.modal-card { max-width: 650px; max-height: 85vh; overflow-y: auto; padding: 24px; }
.form-row { display: flex; margin-bottom: 12px; align-items: center; }
.form-row label { width: 100px; text-align: justify; text-align-last: justify; flex-shrink: 0; margin-right: 12px; }
.form-row input, .form-row select, .form-row textarea { flex: 1; padding: 8px 12px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; }
.form-row textarea { min-height: 60px; resize: vertical; }
.form-row.textarea-row { align-items: flex-start; }
.form-row.textarea-row label { padding-top: 10px; }
.modal-actions { display: flex; gap: 12px; justify-content: center; margin-top: 24px; padding-top: 16px; border-top: 1px solid #e5e7eb; }
.modal-actions button { min-width: 100px; padding: 10px 24px; border-radius: 6px; border: none; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; }
.btn-save { background: #6366f1; color: #fff; }
.btn-save:hover { background: #4f46e5; box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4); }
.btn-cancel { background: #9ca3af; color: #fff; }
.btn-cancel:hover { background: #6b7280; }
.modal-actions .primary { background: linear-gradient(180deg, #5b8cff, #3b6cff); color: #fff; }
.modal-actions .muted { background: #f3f6fb; color: #446; }
</style>