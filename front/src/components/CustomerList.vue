<template>
  <div class="page">
    <div class="toolbar">      
      <button @click="openNew">新增客户</button>
      <select v-model="filterType" @change="load" class="filter-select">
        <option value="">所有类型</option>
        <option value="individual">个人</option>
        <option value="enterprise">企业</option>
      </select>
      <select v-model="filterLevel" @change="load" class="filter-select">
        <option value="">所有等级</option>
        <option value="A">A级</option>
        <option value="B">B级</option>
        <option value="C">C级</option>
      </select>
      <!-- 管理员可以按销售员筛选 -->
      <select v-if="isAdmin" v-model="filterSalesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmanList" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      <input v-model="keyword" placeholder="按客户名称或联系人搜索" @input="load" />
      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>客户编号</th>
          <th>客户名称</th>
          <th>联系人</th>
          <th>负责销售员</th>
          <th>联系电话</th>
          <th>邮箱</th>
          <th>地址</th>
          <th>客户类型</th>
          <th>客户等级</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="c in list" :key="c.id" :class="{ 'no-salesman-row': !c.salesmanId }">
          <td>{{ c.customerNo }}</td>
          <td>
            {{ c.name }}
            <span v-if="!c.salesmanId" class="urgent-badge">待分配</span>
          </td>
          <td>{{ c.contactPerson }}</td>
          <td>
            <span v-if="c.salesmanName" class="salesman-name">{{ c.salesmanName }}</span>
            <span v-else class="no-salesman-text">⚠️ 未分配</span>
          </td>
          <td>{{ c.phone }}</td>
          <td>{{ c.email }}</td>
          <td>{{ c.address }}</td>
          <td>
            <span class="badge" :class="c.customerType === 'enterprise' ? 'info' : 'gray'">
              {{ c.customerType === 'individual' ? '个人' : '企业' }}
            </span>
          </td>
          <td>
            <span class="badge" :class="{
              'success': c.level === 'A',
              'warning': c.level === 'B',
              'gray': c.level === 'C'
            }">
              {{ c.level }}级
            </span>
          </td>
          <td>
            <button @click="edit(c)">编辑</button>
            <button v-if="isAdmin" @click="remove(c)" class="danger">删除</button>
            <button v-else @click="unbind(c)" class="danger action-btn-small">解绑客户</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="10" class="empty">无数据</td>
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
        <h3>{{ editing?.id ? '编辑客户' : '新增客户' }}</h3>

        
          <div class="form-row"><label>客户名称</label><input v-model="form.name" placeholder="请输入客户名称" /></div>
          <div class="form-row"><label>联系人</label><input v-model="form.contactPerson" placeholder="请输入联系人姓名" /></div>
          <div class="form-row"><label>联系电话</label><input v-model="form.phone" placeholder="请输入电话号码" /></div>
          <div class="form-row"><label>邮箱</label><input v-model="form.email" placeholder="请输入邮箱" /></div>
          <div class="form-row"><label>地址</label><input v-model="form.address" placeholder="请输入地址" /></div>

          <div class="form-row">
            <label>客户类型</label>
            <div class="segmented-control">
              <div 
                class="segment-item" 
                :class="{ active: form.customerType === 'individual' }"
                @click="form.customerType = 'individual'"
              >
                <span class="icon">👤</span> <span>个人</span>
              </div>
              <div 
                class="segment-item" 
                :class="{ active: form.customerType === 'enterprise' }"
                @click="form.customerType = 'enterprise'"
              >
                <span class="icon">🏢</span> <span>企业</span>
              </div>
            </div>
          </div>

          <div class="form-row">
            <label>客户等级</label>
            <div class="level-selector">
              <div 
                v-for="lvl in ['A', 'B', 'C']" 
                :key="lvl"
                class="level-option"
                :class="[
                  { active: form.level === lvl }, 
                  'level-' + lvl.toLowerCase()
                ]"
                @click="form.level = lvl"
              >
                {{ lvl }}级
              </div>
            </div>
          </div>

          <div class="form-row" v-if="isAdmin">
            <label>负责销售员</label>
            <div class="select-wrapper">
              <select v-model="form.salesmanId" class="modern-select">
                <option :value="null" :disabled="form.salesmanId !== null">-- 请分配销售员 --</option>
                <option v-for="s in salesmanList" :key="s.id" :value="s.id">{{ s.name }}</option>
              </select>
              <span class="select-arrow">▼</span>
            </div>
          </div>

          <div class="form-row textarea-row">
            <label>备注</label>
            <textarea v-model="form.remark" rows="3" placeholder="填写备注信息..."></textarea>
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

    <!-- 问题列表模态框 -->
    <div v-if="showIssuesDialog" class="modal">
      <div class="modal-card issues-dialog">
        <h3 class="issues-title">⚠️ 无法删除客户</h3>
        <p class="issues-description">该客户还有以下未完成的事项，请先处理完成：</p>
        <ul class="issues-list">
          <li v-for="(issue, index) in issuesList" :key="index" class="issue-item">
            {{ issue }}
          </li>
        </ul>
        <div class="modal-actions">
          <button @click="showIssuesDialog = false" class="primary">了解</button>
        </div>
      </div>
    </div>

    <!-- Toast 提示 -->
    <div v-if="toast.visible" :class="['toast', toast.type]">
      <div class="toast-message">{{ toast.message }}</div>
    </div>

    <AuditLogModal 
      :show="showAllLogsModal"
      :entity-name="'Customer'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { customerApi, salesmanApi } from '../services/api';
import Pagination from './Pagination.vue';
import AuditLogModal from './AuditLogModal.vue';
const props = defineProps<{
  salesmanId?: number
}>();

const userRole = ref(localStorage.getItem('ms_role') || 'salesman');
const salesmanIdStr = localStorage.getItem('ms_salesmanId')
const currentSalesmanId = ref(salesmanIdStr ? Number(salesmanIdStr) : props.salesmanId);
const isAdmin = computed(() => userRole.value === 'admin');

interface Customer {
  id?: number
  customerNo?: string
  name: string
  contactPerson: string
  phone: string
  email: string
  address: string
  customerType: string
  level: string
  salesmanId?: number
  salesmanName?: string
  remark?: string
}

const list = ref<Customer[]>([]);
const allData = ref<Customer[]>([]); // 存储所有数据
const keyword = ref('');
const filterType = ref('');
const filterLevel = ref('');
const filterSalesmanId = ref<number | ''>(''); // 管理员用于筛选销售员
const salesmanList = ref<any[]>([]); // 管理员用的销售员列表
const showForm = ref(false);
const editing = ref<Customer | null>(null);
const showAllLogsModal = ref(false);
const showConfirmDialog = ref(false);
const confirmTitle = ref('');
const confirmMessage = ref('');
const confirmAction = ref<(() => void) | null>(null);

// 问题列表对话框
const showIssuesDialog = ref(false);
const issuesList = ref<string[]>([]);

// Toast 提示
const toast = reactive({ visible: false, message: '', type: 'success' as 'success' | 'error' });
function showToast(message: string, type: 'success' | 'error' = 'success', timeout = 3000) {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
  setTimeout(() => { toast.visible = false; }, timeout);
}

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});

const form = reactive({
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  address: '',
  customerType: 'individual',
  level: 'B',
  salesmanId: null as number | null,
  remark: ''
});

function updateDisplayList() {
  // 排序: 1)无负责人不量程序需要不量程序特别处理 2)按等级排序 A>B>C 3)同等级按客户名称排序
  const levelOrder = { 'A': 1, 'B': 2, 'C': 3 };
  const sorted = [...allData.value].sort((a, b) => {
    // 无负责人丘先（排在最前）
    if (!a.salesmanId && b.salesmanId) return -1;
    if (a.salesmanId && !b.salesmanId) return 1;
    
    // 按等级排序
    const levelA = levelOrder[a.level as keyof typeof levelOrder] || 999;
    const levelB = levelOrder[b.level as keyof typeof levelOrder] || 999;
    if (levelA !== levelB) return levelA - levelB;
    
    // 同等级按名称排序
    return (a.name || '').localeCompare(b.name || '', 'zh-Hans-CN');
  });
  
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = sorted.slice(start, end);
}

async function load() {
  try {
    // 销售员只能看到自己的客户
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filterSalesmanId.value || undefined) // 管理员可以选择性过滤
      : currentSalesmanId.value; // 销售员只看自己的
    
    const res = await customerApi.getAllWithSalesman(role, salesmanId, filterLevel.value || undefined);
    
    if (res.code === 200) {
      let data = res.data || [];
      if (filterType.value) {
        data = data.filter((c: Customer) => c.customerType === filterType.value);
      }
      if (keyword.value.trim()) {
        const kw = keyword.value.toLowerCase();
        data = data.filter((c: Customer) => 
          c.name.toLowerCase().includes(kw) || 
          c.contactPerson?.toLowerCase().includes(kw)
        );
      }
      allData.value = data;
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载客户列表失败:', err);
  }
}

// 加载销售员列表（管理员用）
async function loadSalesmen() {
  if (!isAdmin.value) return;
  try {
    const res = await salesmanApi.getAll();
    if (res.code === 200) {
      salesmanList.value = res.data || [];
    }
  } catch (err) {
    console.error('加载销售员列表失败:', err);
  }
}

function openNew() {
  editing.value = null
  form.name = ''
  form.contactPerson = ''
  form.phone = ''
  form.email = ''
  form.address = ''
  form.customerType = '' // 改为空，不默认选择
  form.level = '' // 改为空，不默认选择
  form.salesmanId = null
  form.remark = ''
  showForm.value = true
}

function edit(item: Customer) {
  editing.value = item;
  form.name = item.name;
  form.contactPerson = item.contactPerson;
  form.phone = item.phone;
  form.email = item.email;
  form.address = item.address;
  form.customerType = item.customerType;
  form.level = item.level;
  form.salesmanId = item.salesmanId || null;
  form.remark = item.remark || '';
  showForm.value = true;
}

function openAllLogs() {
  showAllLogsModal.value = true;
}

async function save() {
  if (!form.name.trim()) { alert('请输入客户名称'); return; }
  if (!form.contactPerson.trim()) { alert('请输入联系人'); return; }
  if (!form.customerType) { alert('请选择客户类型'); return; }
  if (!form.level) { alert('请选择客户等级'); return; }
  if (isAdmin.value && form.salesmanId === undefined) { alert('请选择负责销售员'); return; }

  try {
    const data = { ...form } as any;
    // 销售员添加客户时自动关联自己
    if (!isAdmin.value && currentSalesmanId.value) {
      data.salesmanId = currentSalesmanId.value;
    }
    
    if (editing.value?.id) {
      await customerApi.update(editing.value.id, data)
    } else {
      await customerApi.create(data)
    }
    showForm.value = false
    await load()
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message))
  }
}

function closeForm() { showForm.value = false }

function showConfirm(title: string, message: string, action: () => void) {
  confirmTitle.value = title;
  confirmMessage.value = message;
  confirmAction.value = action;
  showConfirmDialog.value = true;
}

async function handleConfirm() {
  if (confirmAction.value) {
    try {
      await confirmAction.value();
    } finally {
      showConfirmDialog.value = false;
    }
  } else {
    showConfirmDialog.value = false;
  }
}

async function remove(item: Customer) {
  if (!item.id) return;
  showConfirm('确认删除客户', `确定删除客户 ${item.name} 吗？`, async () => {
    try {
      const res = await customerApi.delete(item.id!);
      if (res.code === 200) {
        await load();
        showToast('删除成功');
      } else {
        showToast(res.message || '删除失败', 'error');
      }
    } catch (err: any) {
      const errorMsg = err?.response?.data?.message || '删除失败';
      // 检查是否是未完成事项的错误
      if (errorMsg && errorMsg.includes('未完成的事项')) {
        // 解析错误信息中的问题列表
        const lines = errorMsg.split('\n');
        if (lines.length > 1) {
          // 第一行是提示文本，之后是问题列表（用"、"分隔）
          const issuesLine = lines[1];
          const issues = issuesLine.split('、').filter((s: string) => s.trim());
          issuesList.value = issues;
          showIssuesDialog.value = true;
        } else {
          showToast(errorMsg, 'error');
        }
      } else {
        showToast(errorMsg, 'error');
      }
    }
  });
}

async function unbind(item: Customer) {
  if (!item.id) return;
  showConfirm('确认解绑客户', `确定解绑客户 ${item.name} 吗？`, async () => {
    try {
      const res = await customerApi.unbind(item.id!);
      if (res.code === 200) {
        await load();
        showToast('解绑成功');
      } else {
        showToast(res.message || '解绑失败', 'error');
      }
    } catch (err: any) {
      const errorMsg = err?.response?.data?.message || '解绑失败';
      // 检查是否是未完成事项的错误
      if (errorMsg && errorMsg.includes('未完成的事项')) {
        // 解析错误信息中的问题列表
        const lines = errorMsg.split('\n');
        if (lines.length > 1) {
          // 第一行是提示文本，之后是问题列表（用"、"分隔）
          const issuesLine = lines[1];
          const issues = issuesLine.split('、').filter((s: string) => s.trim());
          issuesList.value = issues;
          showIssuesDialog.value = true;
        } else {
          showToast(errorMsg, 'error');
        }
      } else {
        showToast(errorMsg, 'error');
      }
    }
  });
}

onMounted(() => {
  loadSalesmen();
  load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';

/* 无负责人客户的高亮样式 */
.no-salesman-row {
  background-color: #fff3e0 !important;
  border-left: 4px solid #ff9800;
}

.no-salesman-row:hover {
  background-color: #ffe0b2 !important;
}

.urgent-badge {
  display: inline-block;
  margin-left: 8px;
  padding: 2px 8px;
  background: linear-gradient(135deg, #ff6b6b, #ff4757);
  color: white;
  font-size: 12px;
  font-weight: bold;
  border-radius: 10px;
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.7;
  }
}

.no-salesman-text {
  color: #ff6b6b;
  font-weight: bold;
}

.salesman-name {
  color: inherit;
  font-weight: inherit;
}

/* --- 弹窗容器修复 --- */
.modal-card {
  background: white;
  border-radius: 12px;
  width: 500px;
  max-width: 90%;
  max-height: 85vh;
  overflow-y: auto;
  padding: 24px;
  box-shadow: 0 10px 25px rgba(0,0,0,0.2);
}

.modal-card h3 {
  margin-top: 0;
  margin-bottom: 20px;
  font-size: 20px;
  color: #1e293b;
}

/* 按钮统一样式 */
.modal-actions {
  display: flex;
  justify-content: center;
  gap: 12px;
  padding-top: 20px;
  margin-top: 20px;
  border-top: 1px solid #e5e7eb;
}

.modal-actions button {
  min-width: 100px;
  padding: 10px 24px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  font-size: 14px;
  transition: all 0.2s;
}

.btn-save {
  background: #6366f1;
  color: #fff;
}

.btn-save:hover {
  background: #4f46e5;
  box-shadow: 0 4px 12px rgba(99, 102, 241, 0.4);
}

.btn-cancel {
  background: #9ca3af;
  color: #fff;
}

.btn-cancel:hover {
  background: #6b7280;
}

/* --- 1. 分段控制器样式 (修复版) --- */
.segmented-control {
  display: flex;
  background: #f1f5f9;
  padding: 4px;
  border-radius: 8px;
  border: 1px solid #e2e8f0;
  width: 100%; /* 撑满宽度 */
}

.segment-item {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px 0;
  font-size: 14px;
  color: #64748b;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s;
  white-space: nowrap; /* 【关键】强制文字不换行 */
  gap: 6px;
}

.segment-item.active {
  background: white;
  color: #667eea;
  font-weight: 600;
  box-shadow: 0 2px 4px rgba(0,0,0,0.05);
}

/* --- 2. 等级选择器样式 (修复版) --- */
.level-selector {
  display: flex;
  gap: 10px;
  width: 100%;
}

.level-option {
  flex: 1;
  text-align: center;
  padding: 8px 0; /* 减少左右内边距，靠flex居中 */
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  background: white;
  color: #64748b;
  white-space: nowrap; /* 【关键】强制文字不换行 */
  display: flex;       /* 使用flex保证文字居中 */
  align-items: center;
  justify-content: center;
}

/* 选中状态 */
.level-option.active {
  color: white;
  border-color: transparent;
  box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1);
}
.level-option.level-a.active { background: #10b981; }
.level-option.level-b.active { background: #f59e0b; }
.level-option.level-c.active { background: #94a3b8; }

/* --- 3. 下拉框美化 --- */
.select-wrapper {
  position: relative;
  width: 100%;
}
.modern-select {
  width: 100%;
  appearance: none;
  background: white;
  border: 1px solid #e2e8f0;
  padding: 10px 14px;
  border-radius: 8px;
  color: #334155;
  outline: none;
}
.select-arrow {
  position: absolute;
  right: 14px;
  top: 50%;
  transform: translateY(-50%);
  color: #64748b;
  pointer-events: none;
  font-size: 12px;
}

/* --- 表单行布局修复 --- */
.form-row {
  display: flex;             /* 开启 Flex 布局 */
  align-items: center;       /* 【关键】子元素垂直居中对齐 */
  gap: 16px;                 /* 标签和输入框之间的间距 */
  margin-bottom: 18px;       /* 行间距 */
}

/* 标签样式：固定宽度 + 两端对齐 */
.form-row label {
  width: 80px;               /* 1. 给定一个固定宽度，保证所有输入框左侧对齐 */
  text-align: justify;       /* 2. 文字两端对齐 */
  text-align-last: justify;  /* 3. 强制最后一行也两端对齐（兼容3个字和4个字的标签） */
  font-size: 14px;
  color: #475569;
  font-weight: 500;
  flex-shrink: 0;            /* 防止标签被压缩 */
}

/* 输入框区域自适应填满 */
.form-row input,
.form-row textarea,
.form-row .segmented-control,
.form-row .level-selector,
.form-row .select-wrapper {
  flex: 1;                   /* 占据剩余所有空间 */
  width: 0;                  /* 防止 flex 子项撑开容器 */
}

/* --- 特殊处理：备注栏 --- */
/* 当包含 textarea 时，标签不要垂直居中，要顶端对齐 */
.form-row:has(textarea) {
  align-items: flex-start;   /* 顶端对齐 */
}
/* 给备注标签加一点点顶部内边距，让它对齐第一行文字 */
.form-row:has(textarea) label {
  padding-top: 10px;
}

/* 确认对话框样式 */
.confirm-dialog { max-width: 520px; padding: 22px; }
.confirm-dialog h3 { font-size: 20px; margin: 0 0 6px 0; }
.confirm-message { margin: 18px 0 22px 0; font-size: 15px; color: #444; text-align: center; white-space: pre-line; }
.modal-actions { display: flex; gap: 12px; justify-content: center; margin-top: 8px; }
.modal-actions button { min-width: 100px; padding: 8px 14px; border-radius: 8px; border: none; cursor: pointer; font-weight: 600; }
.modal-actions .primary { background: linear-gradient(180deg, #5b8cff, #3b6cff); color: #fff; }
.modal-actions .muted { background: #f3f6fb; color: #446; }

/* 操作按钮样式 */
.action-btn-small {
  margin-right: 6px;
  font-size: 12px;
  padding: 4px 10px;
}

/* Toast 提示样式 */
.toast {
  position: fixed;
  left: 50%;
  top: 80px;
  transform: translateX(-50%);
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 12px 20px;
  border-radius: 10px;
  color: #fff;
  z-index: 2000;
  animation: slideDown 0.3s ease-out;
}
@keyframes slideDown {
  from { opacity: 0; transform: translateX(-50%) translateY(-20px); }
  to { opacity: 1; transform: translateX(-50%) translateY(0); }
}
.toast.success {
  background: linear-gradient(135deg, #34c759, #17b35e);
}
.toast.error {
  background: linear-gradient(135deg, #ff6b6b, #ff4b4b);
}
.toast-message {
  font-weight: 600;
  font-size: 15px;
}

/* 问题列表对话框样式 */
.issues-dialog {
  width: 420px;
}

.issues-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 12px 0;
  color: #d97706;
}

.issues-description {
  font-size: 14px;
  color: #666;
  margin: 0 0 16px 0;
}

.issues-list {
  list-style: none;
  padding: 0;
  margin: 0 0 20px 0;
  border-left: 3px solid #fbbf24;
  background: #fef3c7;
  padding: 12px 16px;
  border-radius: 4px;
}

.issue-item {
  font-size: 14px;
  color: #92400e;
  margin: 6px 0;
  padding-left: 20px;
  position: relative;
}

.issue-item::before {
  content: '•';
  position: absolute;
  left: 0;
  color: #d97706;
  font-weight: bold;
}

</style>
