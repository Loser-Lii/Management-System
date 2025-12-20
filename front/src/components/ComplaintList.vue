<template>
  <div class="page">
    <div class="dashboard">
      <div class="card pending">
        <div class="card-title">待处理</div>
        <div class="card-value">{{ stats.pendingCount || 0 }}</div>
      </div>
      <div class="card processing">
        <div class="card-title">处理中</div>
        <div class="card-value">{{ stats.processingCount || 0 }}</div>
      </div>
      <div class="card resolved">
        <div class="card-title">已解决</div>
        <div class="card-value">{{ stats.resolvedCount || 0 }}</div>
      </div>
      <div class="card rate">
        <div class="card-title">解决率</div>
        <div class="card-value">{{ stats.resolveRate || '0.0' }}%</div>
      </div>
    </div>

    <div class="toolbar">
      <select v-model="filter.severity" @change="load" class="filter-select">
        <option value="">所有严重等级</option>
        <option value="Critical">严重</option>
        <option value="High">较高</option>
        <option value="Normal">普通</option>
        <option value="Low">轻微</option>
      </select>

      <select v-model="filter.type" @change="load" class="filter-select">
        <option value="">所有类型</option>
        <option value="quality">质量</option>
        <option value="attitude">态度</option>
        <option value="logistics">物流</option>
        <option value="price">价格</option>
      </select>

      <select v-model="filter.status" @change="load" class="filter-select">
        <option value="">所有状态</option>
        <option value="pending">待处理</option>
        <option value="processing">处理中</option>
        <option value="resolved">已解决</option>
        <option value="closed">已撤销</option>
      </select>

      <select v-if="isAdmin" v-model="filter.salesmanId" @change="load" class="filter-select">
        <option value="">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>

      <input type="text" v-model.trim="filter.keyword" placeholder="投诉单号 / 客户 / 订单号" @input="load" class="search-input" />
      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <table class="table">
      <thead>
        <tr>
          <th style="width: 160px;">投诉编号</th>
          <th style="width: 80px;">严重等级</th>
          <th style="width: 80px;">类型</th>
          <th style="width: 200px;">客户 / 销售员</th>
          <th>投诉内容</th>
          <th style="width: 100px;">状态</th>
          <th style="width: 240px;">操作</th>
        </tr>
      </thead>
      <tbody>
        <tr v-for="r in list" :key="r.id" :class="{ 'row-critical': r.severity === 'Critical' }">
          <td>{{ r.complaintNo }}</td>
          <td>
            <span class="tag" :class="severityClass(r.severity)">
              <span v-if="r.severity === 'Critical'" class="icon">🔥</span>
              {{ severityText(r.severity) }}
            </span>
          </td>
          <td>
            <span class="tag type">{{ typeText(r.complaintType) }}</span>
          </td>
          <td>
            {{ r.customer?.name || getCustomerName(r.customerId) }} / 
            {{ r.salesman?.name || getSalesmanName(r.salesmanId) }}
          </td>
          <td class="content-cell">{{ r.content }}</td>
          <td>
            <span class="badge" :class="statusClass(r.status)">{{ statusText(r.status) }}</span>
          </td>
          <td>
            <!-- 第一列：编辑（仅管理员） -->
            <button v-if="isAdmin" @click="openEdit(r)" class="success">编辑</button>
            
            <!-- 第二列：查看详情（所有人都可以查看） -->
            <button @click="viewResult(r)" class="info">查看详情</button>
            
            <!-- 第三列：客户反馈（仅处理中状态且管理员可见） -->
            <button 
              v-if="isAdmin && r.status === 'processing'" 
              @click="openHandle(r)" 
              class="primary"
            >
              客户反馈
            </button>
            
            <!-- 销售员：待处理状态显示"去处理"按钮 -->
            <button 
              v-if="!isAdmin && r.status === 'pending'" 
              @click="openHandle(r)" 
              class="primary"
            >
              去处理
            </button>
          </td>
        </tr>
        <tr v-if="!list.length">
          <td colspan="7" class="empty">无数据</td>
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
        <h3>{{ getModalTitle() }}</h3>
        <div class="info-section">
          <div class="info-row"><label>投诉编号：</label><span>{{ editing?.complaintNo }}</span></div>
          <div class="info-row"><label>客户：</label><span>{{ editing?.customer?.name || getCustomerName(editing?.customerId) }}</span></div>
          <div class="info-row"><label>销售员：</label><span>{{ editing?.salesman?.name || getSalesmanName(editing?.salesmanId) }}</span></div>
          <div class="info-row"><label>关联订单：</label><span>{{ editing?.relatedOrderNo || '-' }}</span></div>
          <div class="info-row"><label>投诉类型：</label><span>{{ typeText(editing?.complaintType) }}</span></div>
          <div class="info-row"><label>严重等级：</label><span class="tag" :class="severityClass(editing?.severity)">{{ severityText(editing?.severity) }}</span></div>
          <div class="info-row"><label>投诉内容：</label><span>{{ editing?.content }}</span></div>
          <div class="info-row" v-if="editing?.evidenceImage">
            <label>图片凭证：</label>
            <img :src="editing.evidenceImage" alt="evidence" class="evidence-img" />
          </div>
        </div>
        
        <div v-if="editing?.status === 'pending'" class="form-row">
          <label>处理方案 *</label>
          <textarea v-model="form.solution" rows="4" placeholder="请详细描述如何解决此问题..." required></textarea>
        </div>
        
        <div v-if="editing?.status === 'processing'">
          <div class="info-row"><label>处理方案：</label><span>{{ editing?.solution }}</span></div>
          <div class="form-row">
            <label>客户反馈 *</label>
            <textarea v-model="form.customerFeedback" rows="3" placeholder="客户对处理方案的反馈..." required></textarea>
          </div>
        </div>
        
        <div class="modal-actions">
          <button @click="save" class="primary">{{ getSaveButtonText() }}</button>
          <button @click="closeForm" class="muted">取消</button>
        </div>
      </div>
    </div>

    <div v-if="showResult" class="modal">
      <div class="modal-card result-card">
        <h3>处理结果</h3>
        <div class="result-section">
          <div class="result-header">
            <div class="result-status" :class="'status-' + resultData?.status">
              {{ statusText(resultData?.status) }}
            </div>
            <div class="result-time">
              <div>投诉时间：{{ formatDate(resultData?.complaintTime) }}</div>
              <div v-if="resultData?.resolveTime">结束时间：{{ formatDate(resultData?.resolveTime) }}</div>
            </div>
          </div>
          
          <div class="result-block">
            <h4>投诉信息</h4>
            <div class="info-row"><label>投诉编号：</label><span>{{ resultData?.complaintNo }}</span></div>
            <div class="info-row"><label>客户：</label><span>{{ resultData?.customer?.name || getCustomerName(resultData?.customerId) }}</span></div>
            <div class="info-row"><label>销售员：</label><span>{{ resultData?.salesman?.name || getSalesmanName(resultData?.salesmanId) }}</span></div>
            <div class="info-row"><label>严重等级：</label><span class="tag" :class="severityClass(resultData?.severity)">{{ severityText(resultData?.severity) }}</span></div>
            <div class="info-row"><label>投诉类型：</label><span>{{ typeText(resultData?.complaintType) }}</span></div>
            <div class="info-row"><label>关联订单：</label><span>{{ resultData?.relatedOrderNo || '-' }}</span></div>
            <div class="info-row full"><label>投诉内容：</label><span>{{ resultData?.content }}</span></div>
            <div class="info-row" v-if="resultData?.evidenceImage">
              <label>图片凭证：</label>
              <img :src="resultData.evidenceImage" alt="evidence" class="evidence-img" />
            </div>
          </div>
          <div class="result-block" v-if="resultData?.solution">
            <h4>处理方案</h4>
            <div class="solution-content">{{ resultData?.solution }}</div>
          </div>
          <div class="result-block" v-if="resultData?.customerFeedback">
            <h4>客户反馈</h4>
            <div class="feedback-content">{{ resultData?.customerFeedback }}</div>
          </div>
          
          <div class="result-block" v-if="resultData?.status === 'pending'">
            <div class="pending-tip">⏳ 该投诉尚未处理</div>
          </div>
          
          <div class="result-block" v-if="resultData?.status === 'processing' && !resultData?.customerFeedback">
            <div class="processing-tip">⏱️ 已提出处理方案，等待客户反馈</div>
          </div>
          
          <div class="result-block" v-if="resultData?.status === 'closed'">
            <div class="closed-tip">🚫 客户已撤销此投诉</div>
          </div>
        </div>
        
        <div class="modal-actions">
          <button @click="closeResult" class="primary">关闭</button>
        </div>
      </div>
    </div>

    <!-- 管理员编辑模态框 -->
    <div v-if="showEditForm" class="modal">
      <div class="modal-card">
        <h3>编辑投诉记录（管理员）</h3>
        
        <div class="form-row">
          <label>投诉编号</label>
          <input type="text" v-model="editForm.complaintNo" disabled />
        </div>
        
        <div class="form-row">
          <label>严重等级 *</label>
          <select v-model="editForm.severity" required>
            <option value="Critical">严重</option>
            <option value="High">较高</option>
            <option value="Normal">普通</option>
            <option value="Low">轻微</option>
          </select>
        </div>
        
        <div class="form-row">
          <label>投诉类型 *</label>
          <select v-model="editForm.complaintType" required>
            <option value="quality">质量</option>
            <option value="attitude">态度</option>
            <option value="logistics">物流</option>
            <option value="price">价格</option>
          </select>
        </div>
        
        <div class="form-row">
          <label>投诉内容</label>
          <textarea v-model="editForm.content" rows="6" disabled class="readonly-textarea"></textarea>
        </div>
        
        <div class="modal-actions">
          <button @click="saveEdit" class="primary">保存修改</button>
          <button @click="closeEditForm" class="muted">取消</button>
        </div>
      </div>
    </div>

    <AuditLogModal 
      :show="showAllLogsModal"
      :entity-name="'ComplaintRecord'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, computed, watch } from 'vue';
import { salesmanApi, customerApi, complaintRecordApi } from '../services/api';
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
const customers = ref<any[]>([]);
const list = ref<any[]>([]);
const allData = ref<any[]>([]);
const stats = reactive<any>({ criticalCount: 0, pendingCount: 0, processingCount: 0, resolveRate: '0.0' });
const filter = reactive({ keyword: '', severity: '', type: '', status: '', salesmanId: '' });

const currentPage = ref(1);
const pageSize = ref(15);
const totalRecords = computed(() => allData.value.length);

// 模态框状态
const showForm = ref(false);
const showResult = ref(false);
const showEditForm = ref(false);
const showAllLogsModal = ref(false);
const editing = ref<any>(null);
const resultData = ref<any>(null);
const form = reactive({
  solution: '',
  status: 'processing',
  customerFeedback: ''
});
const editForm = reactive({
  id: 0,
  complaintNo: '',
  severity: '',
  complaintType: '',
  content: '',
  status: ''
});

watch(currentPage, () => {
  updateDisplayList();
});

async function loadSalesmen() {
  const res = await salesmanApi.getAll();
  if (res.code === 200) salesmen.value = res.data || [];
}

async function loadCustomers() {
  const role = userRole.value;
  const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value;
  const res = await customerApi.getAll(role, salesmanId);
  if (res.code === 200) customers.value = res.data || [];
}

function getCustomerName(id: number) {
  return customers.value.find(c => c.id === id)?.name || '-';
}

function getSalesmanName(id: number) {
  return salesmen.value.find(s => s.id === id)?.name || '-';
}

function updateDisplayList() {
  const start = (currentPage.value - 1) * pageSize.value;
  const end = start + pageSize.value;
  list.value = allData.value.slice(start, end);
}

async function load() {
  try {
    const role = userRole.value;
    const salesmanId = isAdmin.value 
      ? (filter.salesmanId ? Number(filter.salesmanId) : undefined)
      : currentSalesmanId.value;
    
    const res = await complaintRecordApi.getAll(role, salesmanId);
    if (res.code === 200) {
      let data = res.data || [];
      
      // 关键字搜索（投诉单号/客户/订单号）
      if (filter.keyword) {
        const kw = filter.keyword.toLowerCase();
        data = data.filter((r: any) =>
          (r.complaintNo || '').toLowerCase().includes(kw) ||
          (r.customer?.name || '').toLowerCase().includes(kw) ||
          (r.relatedOrderNo || '').toLowerCase().includes(kw)
        );
      }
      
      // 严重等级过滤
      if (filter.severity) data = data.filter((r: any) => r.severity === filter.severity);
      
      // 类型过滤
      if (filter.type) data = data.filter((r: any) => r.complaintType === filter.type);
      
      // 状态过滤
      if (filter.status) data = data.filter((r: any) => r.status === filter.status);
      
      allData.value = data;
      currentPage.value = 1;
      updateDisplayList();
    }
    
    // 加载仪表盘统计（销售员只看自己的统计）
    const statSalesmanId = isAdmin.value ? undefined : currentSalesmanId.value;
    const statRes = await complaintRecordApi.getStatistics(statSalesmanId);
    if (statRes.code === 200) Object.assign(stats, statRes.data || {});
  } catch (err) {
    console.error('加载投诉记录失败:', err);
  }
}

function openAllLogs() {
  showAllLogsModal.value = true;
}

// --- 辅助函数 ---
function severityText(sev: string) {
  const map: any = { Critical: '严重', High: '较高', Normal: '普通', Low: '轻微' };
  return map[sev] || sev || '-';
}

function severityClass(sev: string) {
  return {
    'sev-critical': sev === 'Critical',
    'sev-high': sev === 'High',
    'sev-normal': sev === 'Normal',
    'sev-low': sev === 'Low'
  };
}

function statusText(st: string) {
  const map: any = { pending: '待处理', processing: '处理中', resolved: '已解决', closed: '已撤销' };
  return map[st] || st || '-';
}

function statusClass(st: string) {
  return {
    'success': st === 'resolved',
    'warning': st === 'processing',
    'danger': st === 'pending',
    'muted': st === 'closed'
  };
}

function typeText(t: string) {
  const map: any = { quality: '质量', attitude: '态度', logistics: '物流', price: '价格'};
  return map[t] || t || '-';
}

function getActionButtonText(status: string) {
  if (status === 'pending') return '去处理';
  if (status === 'processing') return '客户反馈';
  if (status === 'resolved') return '查看详情';
  if (status === 'closed') return '查看详情';
  return '处理';
}

function getModalTitle() {
  if (editing.value?.status === 'pending') return '提出处理方案';
  if (editing.value?.status === 'processing') return '填写客户反馈';
  return '处理投诉';
}

function getSaveButtonText() {
  if (editing.value?.status === 'pending') return '提交方案';
  if (editing.value?.status === 'processing') return '确认已解决';
  return '保存';
}

function formatDate(dateStr: string) {
  if (!dateStr) return '-';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', { 
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit', 
    hour: '2-digit', 
    minute: '2-digit' 
  });
} 

// --- 模态框操作 ---

function openHandle(item: any) {
  // 对于已解决和已撤销的投诉，显示完整结果而不是表单
  if (item.status === 'resolved' || item.status === 'closed') {
    viewResult(item);
    return;
  }
  
  // 待处理：销售员填写解决方案
  // 处理中：管理员模拟客户反馈
  editing.value = item;
  form.solution = item.solution || '';
  form.customerFeedback = item.customerFeedback || '';
  showForm.value = true;
}

function viewResult(item: any) {
  resultData.value = item;
  showResult.value = true;
}

async function save() {
  try {
    const payload: any = {};
    
    // 待处理 -> 处理中：提交处理方案
    if (editing.value.status === 'pending') {
      if (!form.solution) { 
        alert('请填写处理方案'); 
        return; 
      }
      payload.solution = form.solution;
      payload.status = 'processing';
    }
    // 处理中 -> 已解决：提交客户反馈
    else if (editing.value.status === 'processing') {
      if (!form.customerFeedback) { 
        alert('请填写客户反馈'); 
        return; 
      }
      payload.customerFeedback = form.customerFeedback;
      payload.status = 'resolved';
      payload.resolveTime = new Date().toISOString(); // 记录解决时间
    }
    
    await complaintRecordApi.update(editing.value.id, payload);
    showForm.value = false;
    form.solution = '';
    form.customerFeedback = '';
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeForm() { 
  showForm.value = false;
  form.solution = '';
  form.customerFeedback = '';
}

function closeResult() {
  showResult.value = false;
  resultData.value = null;
}

function openEdit(item: any) {
  editForm.id = item.id;
  editForm.complaintNo = item.complaintNo;
  editForm.severity = item.severity;
  editForm.complaintType = item.complaintType;
  editForm.content = item.content;
  editForm.status = item.status;
  showEditForm.value = true;
}

async function saveEdit() {
  try {
    if (!editForm.severity || !editForm.complaintType || !editForm.content) {
      alert('请填写所有必填字段');
      return;
    }
    
    const payload = {
      severity: editForm.severity,
      complaintType: editForm.complaintType,
      content: editForm.content
    };
    
    await complaintRecordApi.update(editForm.id, payload);
    alert('修改成功');
    showEditForm.value = false;
    await load();
  } catch (err: any) {
    alert('保存失败：' + (err?.response?.data?.message || err?.message));
  }
}

function closeEditForm() {
  showEditForm.value = false;
  editForm.id = 0;
  editForm.complaintNo = '';
  editForm.severity = '';
  editForm.complaintType = '';
  editForm.content = '';
  editForm.status = '';
}

// --- 生命周期 ---
onMounted(async () => {
  await Promise.all([loadSalesmen(), loadCustomers()]);
  await load();
});
</script>

<style scoped>
@import '../assets/table-styles.css';

/* 仪表盘 */
.dashboard {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 12px;
  margin-bottom: 16px;
}

.card {
  padding: 16px;
  border-radius: 8px;
  color: #fff;
  box-shadow: 0 2px 4px rgba(0,0,0,0.1);
}

.card .card-title {
  font-size: 14px;
  opacity: 0.95;
  margin-bottom: 8px;
}

.card .card-value {
  font-size: 28px;
  font-weight: 700;
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 4px;
}

.card .icon {
  font-size: 24px;
}

.card .card-sub {
  font-size: 12px;
  opacity: 0.9;
  font-weight: 600;
}

.card.pending {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
}

.card.processing {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
}

.card.resolved {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
}

.card.rate {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
}

/* 筛选栏 */
.search-input {
  flex: 1;
  min-width: 280px;
}

/* 标签 */
.tag {
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  display: inline-flex;
  align-items: center;
  gap: 4px;
}

.tag.type {
  background: #f3f4f6;
  color: #374151;
}

.sev-critical {
  background: #fee2e2;
  color: #b91c1c;
  font-weight: 700;
}

.sev-high {
  background: #ffedd5;
  color: #c2410c;
  font-weight: 600;
}

.sev-normal {
  background: #dbeafe;
  color: #1d4ed8;
}

.sev-low {
  background: #ecfdf5;
  color: #047857;
}

/* 状态徽章 */
.badge {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 600;
  display: inline-block;
  text-align: center;
}

.badge.success {
  background: #d1fae5;
  color: #065f46;
}

.badge.warning {
  background: #fef3c7;
  color: #92400e;
}

.badge.danger {
  background: #fee2e2;
  color: #991b1b;
}

.badge.muted {
  background: #e5e7eb;
  color: #4b5563;
}

.row-critical {
  background: #fff1f2 !important;
}

.row-critical:hover {
  background: #ffe4e6 !important;
}

/* 内容单元格 */
.content-cell {
  max-width: 380px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

/* 信息区域 */
.info-section {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 20px;
}

.info-row {
  display: flex;
  margin-bottom: 10px;
  font-size: 14px;
  line-height: 1.6;
}

.info-row:last-child {
  margin-bottom: 0;
}

.info-row label {
  font-weight: 600;
  color: #495057;
  min-width: 100px;
  flex-shrink: 0;
}

.info-row span {
  color: #212529;
  flex: 1;
}

/* 图片凭证 */
.evidence-img {
  max-width: 100%;
  max-height: 300px;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
  margin-top: 8px;
  display: block;
}

/* 处理结果模态框 */
.result-card {
  max-width: 700px;
  max-height: 85vh;
  overflow-y: auto;
}

.result-section {
  max-height: 70vh;
  overflow-y: auto;
}

.result-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  border-radius: 8px;
  margin-bottom: 20px;
  color: #fff;
}

.result-status {
  font-size: 20px;
  font-weight: 700;
  padding: 8px 16px;
  border-radius: 20px;
  background: rgba(255,255,255,0.25);
}

.result-status.status-resolved {
  background: rgba(16,185,129,0.9);
}

.result-status.status-closed {
  background: rgba(107,114,128,0.9);
}

.result-time {
  text-align: right;
  font-size: 13px;
  opacity: 0.95;
}

.result-time div {
  margin-bottom: 4px;
}

.result-block {
  background: #f8f9fa;
  padding: 16px;
  border-radius: 8px;
  margin-bottom: 16px;
}

.result-block h4 {
  margin: 0 0 12px 0;
  font-size: 15px;
  color: #374151;
  font-weight: 600;
  border-bottom: 2px solid #e5e7eb;
  padding-bottom: 8px;
}

.result-block .info-row.full {
  display: block;
}

.result-block .info-row.full label {
  display: block;
  margin-bottom: 6px;
}

.result-block .info-row.full span {
  display: block;
  line-height: 1.6;
}

.solution-content,
.feedback-content {
  background: #fff;
  padding: 12px;
  border-radius: 6px;
  border-left: 3px solid #3b82f6;
  line-height: 1.6;
  color: #374151;
}

.feedback-content {
  border-left-color: #10b981;
}

.pending-tip,
.processing-tip,
.closed-tip {
  text-align: center;
  padding: 16px;
  font-size: 15px;
  font-weight: 500;
  color: #6b7280;
  background: #fff;
  border-radius: 6px;
}

.processing-tip {
  color: #f59e0b;
}

.closed-tip {
  color: #9ca3af;
}
</style>