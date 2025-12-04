<template>
  <div class="page">
    <div class="toolbar">
      <input v-model="keyword" placeholder="按姓名、工号或手机号搜索" @input="load" />
      <button @click="showSuggestion" class="primary">职位变化建议</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th>工号</th>
          <th>姓名</th>
          <th>手机号</th>
          <th>邮箱</th>
          <th>入职日期</th>
          <th>提成率(%)</th>
          <th>等级</th>
          <th>操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="s in list" :key="s.id">
          <td>{{ s.employeeNo }}</td>
          <td>{{ s.name }}</td>
          <td>{{ s.phone }}</td>
          <td>{{ s.email }}</td>
          <td>{{ s.hireDate }}</td>
          <td>{{ (s.commissionRate * 100).toFixed(2) }}%</td>
          <td>
            <span class="badge">{{ formatLevelLabel(s.level) }}</span>
          </td>
          <td>
            <button @click="promote(s)" class="primary" title="升职">升职</button>
            <button @click="demote(s)" class="warning" title="降职">降职</button>
            <button @click="remove(s)" class="danger">删除</button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="8" class="empty">无数据</td>
        </tr>
      </tbody>
    </table>

    <Pagination 
      :total="totalRecords" 
      :pageSize="pageSize" 
      :currentPage="currentPage"
      @update:currentPage="currentPage = $event"
    />

    <!-- 职位变化建议弹窗 -->
    <div v-if="showSuggestionModal" class="modal">
      <div class="modal-card suggestion-card">
        <h3>职位变化建议（近三个月业绩分析）</h3>
        <div class="suggestion-content">
          <div class="suggestion-section">
            <h4 class="success-title">✓ 业绩优秀</h4>
            <div v-if="suggestion && suggestion.topPerformers && suggestion.topPerformers.length > 0">
              <p>近三个月成交额度最多的销售员为：</p>
              <ul>
                <li v-for="performer in suggestion.topPerformers" :key="performer.id">
                  <strong>{{ performer.name }}</strong> 
                  ({{ formatLevelLabel(performer.level) }})
                  - 成交额：¥{{ formatAmount(performer.totalAmount) }}
                  <span v-if="performer.level === 'expert'" class="badge success">建议发奖金</span>
                  <span v-else class="badge primary">建议升职</span>
                </li>
              </ul>
            </div>
            <p v-else class="empty-text">暂无数据</p>
          </div>

          <div class="suggestion-section">
            <h4 class="warning-title">⚠ 业绩待提升</h4>
            <div v-if="suggestion && suggestion.bottomPerformers && suggestion.bottomPerformers.length > 0">
              <p>近三个月成交额度最少的销售员为：</p>
              <ul>
                <li v-for="performer in suggestion.bottomPerformers" :key="performer.id">
                  <strong>{{ performer.name }}</strong> 
                  ({{ formatLevelLabel(performer.level) }})
                  - 成交额：¥{{ formatAmount(performer.totalAmount) }}
                  <span v-if="performer.level === 'junior'" class="badge info">建议约谈</span>
                  <span v-else class="badge warning">建议降职</span>
                </li>
              </ul>
            </div>
            <p v-else class="empty-text">暂无数据</p>
          </div>
        </div>
        <div class="modal-actions">
          <button @click="showSuggestionModal = false">关闭</button>
        </div>
      </div>
    </div>

    <!-- 确认对话框 -->
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

    <!-- 操作提示 Toast -->
    <div v-if="toast.visible" :class="['toast', toast.type]">
      <svg class="toast-icon" viewBox="0 0 24 24" width="20" height="20" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path v-if="toast.type === 'success'" d="M20 6L9 17L4 12" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        <path v-else d="M6 6L18 18M6 18L18 6" stroke="#fff" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
      </svg>
      <div class="toast-message">{{ toast.message }}</div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { salesmanApi } from '../services/api';
import Pagination from './Pagination.vue';

interface Salesman {
  id?: number
  name: string
  employeeNo: string
  phone: string
  email: string
  hireDate: string
  commissionRate: number
  level?: string
}

interface SalesmanPerformance {
  id: number
  name: string
  level: string
  totalAmount: number
}

interface PerformanceSuggestion {
  topPerformers: SalesmanPerformance[]
  bottomPerformers: SalesmanPerformance[]
}

const list = ref<Salesman[]>([]);
const allData = ref<Salesman[]>([]); // 存储所有数据
const keyword = ref('');
const showSuggestionModal = ref(false);
const suggestion = ref<PerformanceSuggestion | null>(null);

// 确认对话框状态（保留但不再用于常规操作）
const showConfirmDialog = ref(false);
const confirmTitle = ref('');
const confirmMessage = ref('');
const confirmAction = ref<(() => void) | null>(null);

// 操作提示 toast
const toast = reactive({ visible: false, message: '', type: 'success' as 'success' | 'error' });

function showToast(message: string, type: 'success' | 'error' = 'success', timeout = 3000) {
  toast.message = message;
  toast.type = type;
  toast.visible = true;
  setTimeout(() => { toast.visible = false }, timeout);
}

// 分页相关
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 监听页码变化，更新显示的数据
watch(currentPage, () => {
  updateDisplayList();
});

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() {
  try {
    const res = keyword.value.trim() 
      ? await salesmanApi.search(keyword.value) 
      : await salesmanApi.getAll();
    if (res.code === 200) {
      allData.value = res.data || [];
      currentPage.value = 1; // 重新加载数据时回到第一页
      updateDisplayList();
    }
  } catch (err) {
    console.error('加载销售员列表失败:', err);
  }
}

function formatLevelLabel(level?: string) {
  switch (level) {
    case 'junior': return '初级';
    case 'intermediate': return '中级';
    case 'senior': return '高级';
    case 'expert': return '资深';
    default: return '未知';
  }
}

function formatAmount(amount: number) {
  return amount ? amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00';
}

// 显示确认对话框
function showConfirm(title: string, message: string, action: () => void) {
  confirmTitle.value = title;
  confirmMessage.value = message;
  confirmAction.value = action;
  showConfirmDialog.value = true;
}

// 处理确认操作（保留）
function handleConfirm() {
  showConfirmDialog.value = false;
  if (confirmAction.value) {
    confirmAction.value();
  }
}

// 直接执行升职/降职/删除并展示 toast（不再要求二次确认）
async function promote(s: Salesman) {
  if (!s.id) return;
  if (s.level === 'expert') {
    showToast('已是最高级别，无法继续升职', 'error');
    return;
  }
  showConfirm(
    '确认升职',
    `确定要将 ${s.name}（${formatLevelLabel(s.level)}）升职吗？`,
    async () => {
      try {
        await salesmanApi.promote(s.id!);
        await load();
        showToast(`${s.name} 已成功升职`, 'success');
      } catch (err: any) {
        showToast('升职失败：' + (err?.response?.data?.message || err?.message), 'error');
      }
    }
  );
}

async function demote(s: Salesman) {
  if (!s.id) return;
  if (s.level === 'junior') {
    showToast('已是最低级别，无法继续降职', 'error');
    return;
  }
  showConfirm(
    '确认降职',
    `确定要将 ${s.name}（${formatLevelLabel(s.level)}）降职吗？`,
    async () => {
      try {
        await salesmanApi.demote(s.id!);
        await load();
        showToast(`${s.name} 已成功降职`, 'success');
      } catch (err: any) {
        showToast('降职失败：' + (err?.response?.data?.message || err?.message), 'error');
      }
    }
  );
}

async function showSuggestion() {
  try {
    const res = await salesmanApi.getPerformanceSuggestion();
    if (res.code === 200) {
      suggestion.value = res.data;
      showSuggestionModal.value = true;
    }
  } catch (err: any) {
    showToast('获取建议失败：' + (err?.response?.data?.message || err?.message), 'error');
  }
}

async function remove(item: Salesman) {
  if (!item.id) return;
  showConfirm(
    '确认删除',
    `确定要删除销售员 ${item.name} 吗？此操作不可恢复！`,
    async () => {
      try {
        await salesmanApi.delete(item.id!);
        await load();
        showToast('删除成功', 'success');
      } catch (err: any) {
        showToast('删除失败：' + (err?.response?.data?.message || err?.message), 'error');
      }
    }
  );
}

onMounted(load);
</script>

<style scoped>
@import '../assets/table-styles.css';

.warning {
  background-color: #ff9800;
  color: white;
}

.warning:hover {
  background-color: #f57c00;
}

.suggestion-card {
  max-width: 700px;
  max-height: 80vh;
  overflow-y: auto;
}

.suggestion-content {
  margin: 20px 0;
}

.suggestion-section {
  margin-bottom: 30px;
  padding: 15px;
  background: #f9f9f9;
  border-radius: 8px;
}

.suggestion-section h4 {
  margin-top: 0;
  margin-bottom: 10px;
  font-size: 18px;
}

.success-title {
  color: #4caf50;
}

.warning-title {
  color: #ff9800;
}

.suggestion-section ul {
  list-style: none;
  padding: 0;
  margin: 10px 0;
}

.suggestion-section li {
  padding: 10px;
  margin: 8px 0;
  background: white;
  border-radius: 5px;
  border-left: 4px solid #2196F3;
}

.empty-text {
  color: #999;
  font-style: italic;
  text-align: center;
  padding: 20px;
}

.badge.success {
  background-color: #4caf50;
}

.badge.warning {
  background-color: #ff9800;
}

.badge.info {
  background-color: #2196F3;
}

/* 通用模态层 */
.modal {
  position: fixed;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0,0,0,0.35);
  z-index: 1000;
  padding: 20px;
}

/* 模态卡片基调 */
.modal-card {
  background: #fff;
  border-radius: 14px;
  box-shadow: 0 10px 30px rgba(22, 27, 40, 0.12);
  padding: 22px 26px;
  max-width: 720px;
  width: 100%;
  color: #222;
}

.suggestion-card { max-width: 720px; }

.confirm-dialog { max-width: 520px; padding: 22px; }

.confirm-dialog h3 { font-size: 20px; margin: 0 0 6px 0; }

.confirm-message {
  margin: 18px 0 22px 0;
  font-size: 15px;
  line-height: 1.6;
  color: #444;
  text-align: center;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: center;
  margin-top: 8px;
}

/* 按钮样式（局部覆盖） */
.modal-actions button {
  min-width: 100px;
  padding: 8px 14px;
  border-radius: 8px;
  border: none;
  cursor: pointer;
  font-weight: 600;
}

.modal-actions .primary {
  background: linear-gradient(180deg,#5b8cff,#3b6cff);
  color: #fff;
}

.modal-actions .muted {
  background: #f3f6fb;
  color: #446;
}

.modal-actions .danger {
  background: linear-gradient(180deg,#ff6b6b,#ff4b4b);
  color: #fff;
}

/* 小屏幕优化 */
@media (max-width: 520px) {
  .modal-card { padding: 16px; border-radius: 12px; }
  .modal-actions button { min-width: 88px; padding: 8px 12px; }
}

/* toast 提示 */
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
  box-shadow: 0 8px 24px rgba(17,24,39,0.15);
  z-index: 2000;
  animation: slideDown 0.3s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateX(-50%) translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(-50%) translateY(0);
  }
}

.toast .toast-icon { opacity: 0.95; flex-shrink: 0; }
.toast.success { background: linear-gradient(135deg,#34c759,#17b35e); }
.toast.error { background: linear-gradient(135deg,#ff6b6b,#ff4b4b); }
.toast-message { font-weight: 600; font-size: 15px; }

@media (max-width: 520px) {
  .toast { left: 12px; right: 12px; transform: none; top: 12px; }
  @keyframes slideDown {
    from {
      opacity: 0;
      transform: translateY(-20px);
    }
    to {
      opacity: 1;
      transform: translateY(0);
    }
  }
}

</style>

