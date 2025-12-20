<template>
  <div class="page">
    <div class="toolbar">
      <select v-model="levelFilter" @change="load" class="filter-select">
        <option value="all">全部等级</option>
        <option value="junior">初级</option>
        <option value="intermediate">中级</option>
        <option value="senior">高级</option>
        <option value="expert">资深</option>
      </select>
      <select v-model="statusFilter" @change="load" class="filter-select">
        <option value="all">全部状态</option>
        <option value="active">仅在职</option>
        <option value="resigned">仅离职</option>
      </select>
      <input v-model="keyword" placeholder="按姓名、工号或手机号搜索" @input="load" class="search-input"/>
      <button @click="showSuggestion" class="primary">职位变化建议</button>
      <button @click="openAllLogs" class="btn-log">📋 查看日志</button>
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
          <th>状态</th>
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
            <span class="badge" :class="getLevelBadgeClass(s.level)">
              {{ formatLevelLabel(s.level) }}
            </span>
          </td>
          <td>
            <span :class="['status-badge', s.status === 'active' ? 'status-active' : 'status-resigned']">
              {{ s.status === 'active' ? '在职' : '离职' }}
            </span>
            <span v-if="s.status === 'resigned' && s.resignationDate" class="resignation-date">
              ({{ s.resignationDate }})
            </span>
          </td>
          <td>
            <template v-if="s.status === 'active'">
              <button 
                @click="promote(s)" 
                class="primary action-btn-small" 
                title="升职"
                :disabled="isExpert(s.level)"
                :class="{ 'btn-disabled': isExpert(s.level) }"
              >
                升职
              </button>
              <button 
                @click="demote(s)" 
                class="warning action-btn-small" 
                title="降职"
                :disabled="isJunior(s.level)"
                :class="{ 'btn-disabled': isJunior(s.level) }"
              >
                降职
              </button>
              <button @click="resign(s)" class="danger action-btn-small">办理离职</button>
            </template>
            <template v-else>
              <span class="resigned-text">已离职</span>
            </template>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="9" class="empty">无数据</td>
        </tr>
      </tbody>
    </table>

    <Pagination 
      :total="totalRecords" 
      :pageSize="pageSize" 
      :currentPage="currentPage"
      @update:currentPage="currentPage = $event"
    />

    <div v-if="showSuggestionModal" class="modal">
      <div class="modal-card suggestion-card">
        <div class="card-header">
          <h3>职位变化建议</h3>
          <span class="subtitle">近三个月业绩深度分析</span>
        </div>
        
        <div class="suggestion-content">
          <div class="suggestion-group">
            <h4 class="group-title success">
              <span class="icon-box success-bg">🏆</span> 
              <span>业绩优秀组</span>
              <span class="tag-line">建议重点激励</span>
            </h4>
            
            <div v-if="suggestion && suggestion.topPerformers?.length">
              <div class="card-list">
                <div v-for="performer in suggestion.topPerformers" :key="performer.id" class="performer-card success-border">
                  <div class="card-left">
                    <img v-if="performer.avatar" :src="getAvatarUrl(performer.avatar)" class="avatar-img success" :alt="performer.name" @error="(e) => (e.target as HTMLImageElement).style.display = 'none'" />
                    <div v-else class="avatar-circle success">{{ performer.name.charAt(0) }}</div>
                    <div class="info-text">
                      <span class="name">{{ performer.name }}</span>
                      <span class="level-badge" :class="getLevelBadgeClass(performer.level)">
                        {{ formatLevelLabel(performer.level) }}
                      </span>
                    </div>
                  </div>
                  
                  <div class="card-center">
                    <span class="label">季度成交总额</span>
                    <span class="amount">¥ {{ formatAmount(performer.totalAmount) }}</span>
                  </div>

                  <div class="card-right">
                    <div v-if="isExpert(performer.level)" class="modal-btn is-bonus">
                      <span class="btn-icon">💰</span> 颁发奖金
                    </div>
                    <div v-else class="modal-btn is-promote">
                      <span class="btn-icon">⬆</span> 立即升职
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <p v-else class="empty-text">暂无数据</p>
          </div>

          <div class="suggestion-group">
            <h4 class="group-title warning">
              <span class="icon-box warning-bg">⚠️</span> 
              <span>业绩待提升组</span>
              <span class="tag-line">建议及时干预</span>
            </h4>
            
            <div v-if="suggestion && suggestion.bottomPerformers?.length">
              <div class="card-list">
                <div v-for="performer in suggestion.bottomPerformers" :key="performer.id" class="performer-card warning-border">
                  <div class="card-left">
                    <img v-if="performer.avatar" :src="getAvatarUrl(performer.avatar)" class="avatar-img warning" :alt="performer.name" @error="(e) => (e.target as HTMLImageElement).style.display = 'none'" />
                    <div v-else class="avatar-circle warning">{{ performer.name.charAt(0) }}</div>
                    <div class="info-text">
                      <span class="name">{{ performer.name }}</span>
                      <span class="level-badge" :class="getLevelBadgeClass(performer.level)">
                        {{ formatLevelLabel(performer.level) }}
                      </span>
                    </div>
                  </div>
                  
                  <div class="card-center">
                    <span class="label">季度成交总额</span>
                    <span class="amount">¥ {{ formatAmount(performer.totalAmount) }}</span>
                  </div>

                  <div class="card-right">
                    <div v-if="isJunior(performer.level)" class="modal-btn is-talk">
                      <span class="btn-icon">💬</span> 绩效面谈
                    </div>
                    <div v-else class="modal-btn is-demote">
                      <span class="btn-icon">⬇</span> 职位降级
                    </div>
                  </div>
                </div>
              </div>
            </div>
            <p v-else class="empty-text">暂无数据</p>
          </div>
        </div>
        
        <div class="modal-footer">
          <button @click="showSuggestionModal = false" class="close-btn">关 闭</button>
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

    <div v-if="toast.visible" :class="['toast', toast.type]">
      <div class="toast-message">{{ toast.message }}</div>
    </div>

    <AuditLogModal 
      :show="showAllLogsModal"
      :entity-name="'Salesman'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { salesmanApi } from '../services/api';
import Pagination from './Pagination.vue';
import AuditLogModal from './AuditLogModal.vue';

// ... 接口定义 ...
interface Salesman { id?: number; name: string; employeeNo: string; phone: string; email: string; hireDate: string; commissionRate: number; level?: string; status?: string; resignationDate?: string; }
interface SalesmanPerformance { id: number; name: string; level: string; avatar?: string; totalAmount: number; }
interface PerformanceSuggestion { topPerformers: SalesmanPerformance[]; bottomPerformers: SalesmanPerformance[]; }

const list = ref<Salesman[]>([]);
const allData = ref<Salesman[]>([]);
const keyword = ref('');
const statusFilter = ref('all');
const levelFilter = ref('all');
const showSuggestionModal = ref(false);
const suggestion = ref<PerformanceSuggestion | null>(null);
const showAllLogsModal = ref(false);
const showConfirmDialog = ref(false);
const confirmTitle = ref('');
const confirmMessage = ref('');
const confirmAction = ref<(() => void) | null>(null);
const toast = reactive({ visible: false, message: '', type: 'success' as 'success' | 'error' });
const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

function isExpert(level?: string) { return (level || '').toLowerCase() === 'expert'; }
function isJunior(level?: string) { return (level || '').toLowerCase() === 'junior'; }
function getAvatarUrl(avatar?: string) {
  if (!avatar) return '';
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  return `http://localhost:8080${avatar}`;
}

function getLevelBadgeClass(level?: string) {
  if (!level) return 'default';
  const l = level.toLowerCase();
  switch (l) {
    case 'expert': return 'success';       
    case 'senior': return 'primary';       
    case 'intermediate': return 'info';    
    case 'junior': return 'warning';       
    default: return 'default';
  }
}

function showToast(message: string, type: 'success' | 'error' = 'success', timeout = 3000) { toast.message = message; toast.type = type; toast.visible = true; setTimeout(() => { toast.visible = false }, timeout); }
watch(currentPage, updateDisplayList);
function updateDisplayList() { const start = (currentPage.value - 1) * pageSize.value; const end = start + pageSize.value; list.value = allData.value.slice(start, end); }
async function load() { 
  try { 
    const includeResigned = statusFilter.value === 'all' || statusFilter.value === 'resigned';
    const res = keyword.value.trim() 
      ? await salesmanApi.search(keyword.value) 
      : await salesmanApi.getAll(includeResigned);
    if (res.code === 200) { 
      let data = res.data || [];
      // 根据状态筛选器过滤
      if (statusFilter.value === 'active') {
        data = data.filter((s: any) => !s.status || s.status === 'active');
      } else if (statusFilter.value === 'resigned') {
        data = data.filter((s: any) => s.status === 'resigned');
      }
      // 根据等级筛选器过滤
      if (levelFilter.value !== 'all') {
        data = data.filter((s: any) => s.level?.toLowerCase() === levelFilter.value);
      }
      allData.value = data;
      currentPage.value = 1; 
      updateDisplayList(); 
    } 
  } catch (err) { 
    console.error(err); 
  } 
}

function openAllLogs() {
  showAllLogsModal.value = true;
}

function formatLevelLabel(level?: string) { if (!level) return '未知'; const normalizedLevel = level.toLowerCase(); switch (normalizedLevel) { case 'junior': return '初级'; case 'intermediate': return '中级'; case 'senior': return '高级'; case 'expert': return '资深'; default: return '未知'; } }
function formatAmount(amount: number) { return amount ? amount.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 }) : '0.00'; }
function showConfirm(title: string, message: string, action: () => void) { confirmTitle.value = title; confirmMessage.value = message; confirmAction.value = action; showConfirmDialog.value = true; }
function handleConfirm() { showConfirmDialog.value = false; if (confirmAction.value) { confirmAction.value(); } }

async function promote(s: Salesman) {
  if (!s.id || isExpert(s.level)) return;
  showConfirm('确认升职', `确定要将 ${s.name} 升职吗？`, async () => {
    try { 
      await salesmanApi.promote(s.id!); 
      await load(); 
      showToast('升职成功'); 
    } catch (err: any) { 
      showToast(err.response?.data?.message || '升职失败', 'error'); 
    }
  });
}
async function demote(s: Salesman) {
  if (!s.id || isJunior(s.level)) return;
  showConfirm('确认降职', `确定要将 ${s.name} 降职吗？`, async () => {
    try { 
      await salesmanApi.demote(s.id!); 
      await load(); 
      showToast('降职成功'); 
    } catch (err: any) { 
      showToast(err.response?.data?.message || '降职失败', 'error'); 
    }
  });
}
async function showSuggestion() { 
  try { 
    const res = await salesmanApi.getPerformanceSuggestion(); 
    if (res.code === 200) { 
      suggestion.value = res.data; 
      showSuggestionModal.value = true; 
    } 
  } catch (err: any) { 
    showToast(err.response?.data?.message || '获取建议失败', 'error'); 
  } 
}
async function resign(item: Salesman) {
  if (!item.id) return;
  showConfirm('确认办理离职', `确定办理 ${item.name} 离职？`, async () => {
    try { 
      const res = await salesmanApi.resign(item.id!); 
      if(res.code === 200) { 
        await load(); 
        showToast('离职办理成功'); 
      } else {
        showToast(res.message || '离职办理失败', 'error');
      }
    } catch(err: any) { 
      showToast(err.response?.data?.message || '离职办理失败', 'error'); 
    }
  });
}

onMounted(load);
</script>

<style scoped>
@import '../assets/table-styles.css';

/* 搜索框 */
.search-input {
  min-width: 280px;
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
}

/* 筛选器 */
.filter-select {
  padding: 8px 12px;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  background: white;
  cursor: pointer;
}

/* --- 1. 轻奢软标签 (Soft Badges) --- */
.badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  min-width: 50px;
}
.badge.success { background-color: #dcfce7; color: #166534; }
.badge.primary { background-color: #dbeafe; color: #1e40af; }
.badge.info    { background-color: #e0f2fe; color: #075985; }
.badge.warning { background-color: #fff7ed; color: #9a3412; }
.badge.default { background-color: #f1f5f9; color: #64748b; }

/* --- 2. 状态小圆点 (Status Dots) - 重点加回了这里 --- */
.status-badge {
  display: inline-flex;
  align-items: center;
  gap: 8px; /* 文字和点的间距 */
  font-weight: 500;
  font-size: 13px;
}

/* 使用 ::before 伪元素画圆点 */
.status-badge::before {
  content: '';
  display: block;
  width: 8px;   /* 圆点宽度 */
  height: 8px;  /* 圆点高度 */
  border-radius: 50%; /* 变成圆形 */
}

/* 在职状态：绿色 */
.status-active { 
  color: #15803d; 
}
.status-active::before {
  background-color: #22c55e;
  box-shadow: 0 0 0 2px rgba(34, 197, 94, 0.2); /* 加一点光晕 */
}

/* 离职状态：灰色 */
.status-resigned {
  color: #64748b;
}
.status-resigned::before {
  background-color: #cbd5e1;
}

.resignation-date {
  display: block;
  font-size: 12px;
  color: #94a3b8;
  margin-top: 4px;
  padding-left: 16px; /* 对齐文字 */
}

.action-btn-small {
  margin-right: 6px;
  font-size: 12px;
  padding: 4px 10px;
}

/* --- 3. 大气卡片模态框 --- */
.modal {
  position: fixed;
  inset: 0;
  background: rgba(15, 23, 42, 0.4);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  padding: 20px;
}

.modal-card.suggestion-card {
  background: #fff;
  width: 100%;
  max-width: 800px;
  max-height: 90vh;
  border-radius: 24px;
  box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.card-header {
  padding: 24px 32px;
  border-bottom: 1px solid #f1f5f9;
  background: #fff;
}
.card-header h3 {
  margin: 0;
  font-size: 24px;
  font-weight: 800;
  color: #0f172a;
  letter-spacing: -0.5px;
}
.card-header .subtitle {
  display: block;
  margin-top: 4px;
  color: #64748b;
  font-size: 14px;
}

.suggestion-content {
  padding: 32px;
  overflow-y: auto;
  background: #fcfcfc;
}
.suggestion-group {
  margin-bottom: 40px;
}
.group-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 20px;
  font-size: 18px;
  font-weight: 700;
}
.group-title.success { color: #059669; }
.group-title.warning { color: #d97706; }

.icon-box {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
}
.success-bg { background: #d1fae5; }
.warning-bg { background: #fef3c7; }

.tag-line {
  font-size: 12px;
  font-weight: normal;
  background: #fff;
  padding: 2px 8px;
  border-radius: 10px;
  border: 1px solid currentColor;
  opacity: 0.8;
}

.card-list {
  display: grid;
  gap: 16px;
}

.performer-card {
  background: #fff;
  border: 1px solid #e2e8f0;
  border-radius: 16px;
  padding: 20px 24px;
  display: grid;
  grid-template-columns: 1.2fr 1.5fr 1fr;
  align-items: center;
  transition: all 0.3s ease;
  overflow: hidden;
}
.performer-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 10px 15px -3px rgba(0, 0, 0, 0.05);
}

.success-border { border-left: 4px solid #10b981; }
.warning-border { border-left: 4px solid #f59e0b; }

.card-left { display: flex; align-items: center; gap: 12px; }
.avatar-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
  font-size: 20px;
  color: #fff;
  flex-shrink: 0;
}
.avatar-circle.success { background: linear-gradient(135deg, #34d399, #059669); }
.avatar-circle.warning { background: linear-gradient(135deg, #fbbf24, #d97706); }

.avatar-img {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  object-fit: cover;
  flex-shrink: 0;
}
.avatar-img.success { border: 3px solid #10b981; }
.avatar-img.warning { border: 3px solid #f59e0b; }

.info-text { display: flex; flex-direction: column; gap: 4px; }
.info-text .name { font-size: 16px; font-weight: 700; color: #1e293b; }
.level-badge {
  font-size: 11px;
  padding: 2px 6px;
  border-radius: 4px;
  background: #f1f5f9;
  color: #64748b;
  width: fit-content;
}

.card-center {
  display: flex;
  flex-direction: column;
  border-left: 1px solid #f1f5f9;
  border-right: 1px solid #f1f5f9;
  padding: 0 24px;
}
.card-center .label { font-size: 12px; color: #94a3b8; text-transform: uppercase; margin-bottom: 4px; }
.card-center .amount {
  font-family: 'Arial', sans-serif;
  font-size: 24px;
  font-weight: 700;
  color: #0f172a;
}

.card-right { display: flex; justify-content: flex-end; }
.modal-btn {
  display: inline-flex;
  align-items: center;
  gap: 8px;
  padding: 10px 20px;
  border-radius: 12px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  cursor: default;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);
}
.is-bonus   { background: linear-gradient(135deg, #10b981, #059669); }
.is-promote { background: linear-gradient(135deg, #3b82f6, #2563eb); }
.is-talk    { background: linear-gradient(135deg, #8b5cf6, #7c3aed); }
.is-demote  { background: linear-gradient(135deg, #f59e0b, #d97706); }

.modal-footer { padding: 20px 32px; border-top: 1px solid #f1f5f9; text-align: right; background: #fff; }
.close-btn { padding: 10px 32px; background: #f1f5f9; color: #475569; font-weight: 600; border-radius: 8px; border: none; cursor: pointer; }
.close-btn:hover { background: #e2e8f0; }
.empty-text { text-align: center; color: #cbd5e1; padding: 40px; font-style: italic; }

.confirm-dialog { max-width: 520px; padding: 22px; }
.confirm-dialog h3 { font-size: 20px; margin: 0 0 6px 0; }
.confirm-message { margin: 18px 0 22px 0; font-size: 15px; color: #444; text-align: center; }
.modal-actions { display: flex; gap: 12px; justify-content: center; margin-top: 8px; }
.modal-actions button { min-width: 100px; padding: 8px 14px; border-radius: 8px; border: none; cursor: pointer; font-weight: 600; }
.modal-actions .primary { background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%); color: #0b1221; box-shadow: 0 8px 18px rgba(14, 165, 233, 0.24); }
.modal-actions .muted { background: #ffffff; color: #475569; border: 1px solid #e2e8f0; }

.toast { position: fixed; left: 50%; top: 80px; transform: translateX(-50%); display: flex; align-items: center; gap: 12px; padding: 12px 20px; border-radius: 10px; color: #fff; z-index: 2000; animation: slideDown 0.3s ease-out; }
@keyframes slideDown { from { opacity: 0; transform: translateX(-50%) translateY(-20px); } to { opacity: 1; transform: translateX(-50%) translateY(0); } }
.toast.success { background: linear-gradient(135deg,#34c759,#17b35e); }
.toast.error { background: linear-gradient(135deg,#ff6b6b,#ff4b4b); }
.toast-message { font-weight: 600; font-size: 15px; }

.resigned-text { color: #999; font-style: italic; }
button.btn-disabled { background: #e0e0e0 !important; color: #9e9e9e !important; cursor: not-allowed; }
</style>