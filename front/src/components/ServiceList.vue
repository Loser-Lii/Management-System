<template>
  <div class="page">
    <div class="toolbar">
      <button @click="openNew">新增服务记录</button>
      
      <select v-if="isAdmin" v-model.number="filters.salesmanId" class="filter-select">
        <option :value="0">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      
      <select v-model.number="filters.customerId" class="filter-select">
        <option :value="0">所有客户</option>
        <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      
      <select v-model="filters.serviceType" class="filter-select">
        <option value="">服务类型</option>
        <option value="repair">维修</option>
        <option value="maintenance">保养</option>
        <option value="training">培训</option>
        <option value="consult">咨询</option>
        <option value="installation">安装</option>
      </select>
      
      <select v-model="filters.status" class="filter-select">
        <option value="">服务状态</option>
        <option value="pending">待处理</option>
        <option value="processing">处理中</option>
        <option value="completed">已完成</option>
        <option value="cancelled">已取消</option>
      </select>
      
      <div class="date-group">
        <label>起始日期</label>
        <input type="date" v-model="filters.dateFrom" class="date-input" :max="filters.dateTo" />
      </div>
      
      <div class="date-group">
        <label>截止日期</label>
        <input type="date" v-model="filters.dateTo" class="date-input" :min="filters.dateFrom" />
      </div>
      
      <input type="text" v-model.trim="filters.keyword" placeholder="服务单号 / 客户" class="search-input" />
      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <section class="table-container">
      <table class="table enhanced-table">
        <thead>
          <tr>
            <th style="width: 150px;">服务单号</th>
            <th style="width: 90px;">销售员</th>
            <th style="width: 120px;">客户</th>
            <th style="width: 90px;">服务类型</th>
            <th style="width: 200px;">服务内容</th>
            <th style="width: 120px;">满意度</th>
            <th style="width: 70px;">状态</th>
            <th style="width: 140px;">创建时间</th>
            <th style="width: 140px;">完成时间</th>
            <th style="width: 280px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="item in filteredRecords" :key="item.id">
            <td>{{ item.serviceNo || '-' }}</td>
            <td>{{ item.salesman?.name || '-' }}</td>
            <td>{{ item.customer?.name || '-' }}</td>
            <td>
              <span :class="['badge', 'badge-' + item.serviceType]">
                {{ getTypeText(item.serviceType) }}
              </span>
            </td>
            <td>
              <div class="content-cell" :title="item.content">
                {{ item.content }}
              </div>
            </td>
            <td>
              <div class="star-rating">
                <span 
                  v-for="n in 5" 
                  :key="n" 
                  :class="n <= (item.satisfaction || 0) ? 'star-filled' : 'star-empty'"
                >
                  ★
                </span>
              </div>
            </td>
            <td>
              <span :class="['badge', 'badge-status-' + item.status]">
                {{ getStatusText(item.status) }}
              </span>
            </td>
            <td>{{ formatDateTime(item.startTime) }}</td>
            <td>{{ formatDateTime(item.endTime) }}</td>
            <td class="actions">
              <!-- 编辑（所有状态可编辑） -->
              <button 
                @click="edit(item)" 
                class="btn-sm"
              >
                编辑
              </button>
              <!-- 删除（所有状态可删除） -->
              <button 
                @click="remove(item)" 
                class="btn-sm danger"
              >
                删除
              </button>
              <!-- 管理员：客户打分（只有处理中可点） -->
              <button 
                v-if="isAdmin"
                :disabled="item.status !== 'processing'"
                @click="openRateModal(item)" 
                class="btn-sm btn-primary"
                :title="item.status !== 'processing' ? '只有处理中的服务才能评分' : '客户打分'"
              >
                客户打分
              </button>
              <!-- 销售员：去处理（只有待处理可点） -->
              <button 
                v-if="!isAdmin"
                :disabled="item.status !== 'pending'"
                @click="openProcessModal(item)" 
                class="btn-sm btn-success"
                :title="item.status !== 'pending' ? '只有待处理的服务才能处理' : '去处理'"
              >
                去处理
              </button>
            </td>
          </tr>
          <tr v-if="!filteredRecords.length">
            <td colspan="10" class="empty">无数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <!-- 新建/编辑服务表单 -->
    <div v-if="showForm" class="modal">
      <div class="modal-card">
        <h3>{{ editing ? '编辑服务记录' : '新增服务记录' }}</h3>
        
          <div v-if="isAdmin" class="form-row">
            <label>销售员 *</label>
            <select v-model.number="form.salesmanId" required>
              <option :value="0" :disabled="form.salesmanId !== 0" selected>请选择销售员</option>
              <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
            </select>
          </div>

          <div class="form-row">
            <label>客户 *</label>
            <select v-model.number="form.customerId" required>
              <option :value="0" :disabled="form.customerId !== 0" selected>请选择客户</option>
              <option v-for="c in filteredCustomers" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>

          <div class="form-row">
            <label>关联产品</label>
            <select v-model.number="form.productId">
              <option :value="0" :disabled="form.productId !== 0" selected>请选择关联产品 </option>
              <option v-for="p in products" :key="p.id" :value="p.id">{{ p.name }}</option>
            </select>
          </div>

          <div class="form-row">
            <label>服务类型 *</label>
            <select v-model="form.serviceType" required>
              <option value="" :disabled="form.serviceType !== ''" selected>请选择服务类型</option>
              <!-- 管理员只能选择保养和安装 -->
              <option v-if="isAdmin" value="maintenance">保养</option>
              <option v-if="isAdmin" value="installation">安装</option>
              <!-- 销售员只能选择培训 -->
              <option v-if="!isAdmin" value="training">培训</option>
            </select>
          </div>

          <div class="form-row">
            <label>开始时间 *</label>
            <input v-model="form.startTime" type="datetime-local" required />
          </div>

          <div class="form-row textarea-row">
            <label>服务内容 *</label>
            <textarea v-model="form.content" rows="3" required placeholder="请详细描述服务内容"></textarea>
          </div>
        <div class="modal-actions">
          <button class="btn-cancel" @click="closeForm">取消</button>
          <button class="btn-save" @click="save">保存</button>
        </div>
      </div>
    </div>

    <!-- 销售员处理服务弹窗 -->
    <div v-if="showProcessModal" class="modal">
      <div class="modal-card modal-small">
        <h3>处理服务</h3>
        
        <div class="info-row">
          <label>服务单号：</label>
          <span>{{ currentProcessing?.serviceNo }}</span>
        </div>
        
        <div class="info-row">
          <label>服务内容：</label>
          <span>{{ currentProcessing?.content }}</span>
        </div>
        
        <div class="form-row textarea-row">
          <label>处理结果 *</label>
          <textarea v-model="processForm.solution" rows="4" required placeholder="请填写处理结果"></textarea>
        </div>

        <div class="modal-actions">
          <button @click="processService" class="btn-save">确认处理</button>
          <button @click="showProcessModal = false" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 管理员客户打分弹窗 -->
    <div v-if="showRateModal" class="modal">
      <div class="modal-card modal-small">
        <h3>客户评分</h3>
        
        <div class="info-row">
          <label>服务单号：</label>
          <span>{{ currentRating?.serviceNo }}</span>
        </div>
        
        <div class="info-row">
          <label>处理结果：</label>
          <span>{{ currentRating?.solution }}</span>
        </div>

        <div class="form-row">
          <label>满意度评分 *</label>
          <div class="star-rating-input">
            <span 
              v-for="n in 5" 
              :key="n"
              @click="rateForm.satisfaction = n"
              :class="['star-clickable', n <= rateForm.satisfaction ? 'star-filled' : 'star-empty']"
            >
              ★
            </span>
          </div>
        </div>

        <div class="modal-actions">
          <button @click="rateService" class="btn-save">确认评分</button>
          <button @click="showRateModal = false" class="btn-cancel">取消</button>
        </div>
      </div>
    </div>

    <!-- 操作日志弹窗 -->
    <AuditLogModal 
      :show="showLogsModal"
      :entity-name="'ServiceRecord'"
      :entity-id="currentLogEntityId"
      @close="showLogsModal = false"
    />

    <!-- 服务记录类型日志弹窗 -->
    <AuditLogModal 
      :show="showAllLogsModal"
      :entity-name="'ServiceRecord'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />

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
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, watch, onMounted } from 'vue'
import { serviceRecordApi, salesmanApi, customerApi, productApi, salesRecordApi } from '../services/api'
import AuditLogModal from './AuditLogModal.vue'

interface ServiceRecord {
  id?: number
  serviceNo?: string
  salesmanId: number
  customerId: number
  productId?: number
  serviceType: string
  startTime: string
  endTime?: string
  content: string
  solution?: string
  isPaid: boolean
  satisfaction?: number
  status: string
  salesman?: { name: string }
  customer?: { name: string }
  product?: { name: string }
}

interface Salesman {
  id: number
  name: string
}

interface Customer {
  id: number
  name: string
  salesmanId?: number
}

interface Product {
  id: number
  name: string
}

const userRole = ref(localStorage.getItem('ms_role') || 'salesman')
const salesmanIdStr = localStorage.getItem('ms_salesmanId')
const currentSalesmanId = ref(salesmanIdStr ? Number(salesmanIdStr) : 0)
const isAdmin = computed(() => userRole.value === 'admin')

const salesmen = ref<Salesman[]>([])
const customers = ref<Customer[]>([])
const products = ref<Product[]>([])
const allProducts = ref<Product[]>([])
const records = ref<ServiceRecord[]>([])

const filters = reactive({
  salesmanId: 0,
  customerId: 0,
  serviceType: '',
  status: '',
  dateFrom: '',
  dateTo: '',
  keyword: ''
})

const showForm = ref(false)
const showProcessModal = ref(false)
const showRateModal = ref(false)
const showLogsModal = ref(false)
const showAllLogsModal = ref(false)
const editing = ref<ServiceRecord | null>(null)
const currentProcessing = ref<ServiceRecord | null>(null)
const currentRating = ref<ServiceRecord | null>(null)
const currentLogEntityId = ref(0)

const showConfirmDialog = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

const form = reactive({
  salesmanId: 0,
  customerId: 0,
  productId: 0,
  serviceType: '',
  startTime: '',
  content: ''
})

const processForm = reactive({
  solution: ''
})

const rateForm = reactive({
  satisfaction: 0
})

const filteredRecords = computed(() => {
  let result = records.value

  if (!isAdmin.value && currentSalesmanId.value) {
    result = result.filter(r => r.salesmanId === currentSalesmanId.value)
  } else if (filters.salesmanId) {
    result = result.filter(r => r.salesmanId === filters.salesmanId)
  }

  if (filters.customerId) {
    result = result.filter(r => r.customerId === filters.customerId)
  }

  if (filters.serviceType) {
    result = result.filter(r => r.serviceType === filters.serviceType)
  }

  if (filters.status) {
    result = result.filter(r => r.status === filters.status)
  }

  if (filters.dateFrom) {
    result = result.filter(r => r.startTime >= filters.dateFrom)
  }

  if (filters.dateTo) {
    result = result.filter(r => r.startTime <= filters.dateTo + 'T23:59:59')
  }

  if (filters.keyword) {
    const kw = filters.keyword.toLowerCase()
    result = result.filter(r => 
      r.serviceNo?.toLowerCase().includes(kw) ||
      r.customer?.name?.toLowerCase().includes(kw)
    )
  }

  return result
})

// 根据选择的销售员过滤客户列表
const filteredCustomers = computed(() => {
  if (!isAdmin.value || !form.salesmanId || form.salesmanId === 0) {
    return customers.value
  }
  return customers.value.filter(c => c.salesmanId === form.salesmanId)
})

// 监听客户选择，自动设置销售员
watch(() => form.customerId, (newCustomerId) => {
  if (isAdmin.value && newCustomerId && newCustomerId !== 0) {
    const selectedCustomer = customers.value.find(c => c.id === newCustomerId)
    if (selectedCustomer && selectedCustomer.salesmanId) {
      form.salesmanId = selectedCustomer.salesmanId
    }
  }
  loadProductsByRelation()
})

watch(() => form.salesmanId, () => {
  // 当销售员变化时重新按关系过滤产品
  loadProductsByRelation()
})

onMounted(async () => {
  await loadData()
  if (isAdmin.value) {
    await loadSalesmen()
  }
  await loadCustomers()
  await loadProducts()
})

async function loadData() {
  try {
    const role = userRole.value
    const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value
    console.log('=== loadData START ===')
    console.log('userRole.value:', userRole.value)
    console.log('currentSalesmanId.value:', currentSalesmanId.value)
    console.log('isAdmin.value:', isAdmin.value)
    console.log('role to send:', role)
    console.log('salesmanId to send:', salesmanId)
    
    const res = await serviceRecordApi.getAll(role, salesmanId)
    console.log('API response:', res)
    console.log('res.code:', res.code)
    console.log('res.data:', res.data)
    console.log('res.data length:', res.data ? res.data.length : 'N/A')
    
    if (res.code === 200) {
      records.value = res.data || []
      console.log('records.value updated to:', records.value)
      console.log('filteredRecords will have:', filteredRecords.value.length, 'items')
    } else {
      console.warn('API returned code:', res.code)
    }
    console.log('=== loadData END ===')
  } catch (err: any) {
    console.error('loadData error:', err)
    alert(err?.response?.data?.message || '加载失败')
  }
}

async function loadSalesmen() {
  try {
    const res = await salesmanApi.getAll()
    salesmen.value = res.data || []
  } catch (err: any) {
    console.error('加载销售员失败', err)
  }
}

async function loadCustomers() {
  try {
    const role = userRole.value
    const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value
    const res = await customerApi.getAll(role, salesmanId)
    customers.value = res.data || []
  } catch (err: any) {
    console.error('加载客户失败:', err)
  }
}

async function loadProducts() {
  try {
    const res = await productApi.getAll()
    allProducts.value = res.data || []
    products.value = allProducts.value
  } catch (err: any) {
    console.error('加载产品失败', err)
  }
}

async function loadProductsByRelation() {
  if (!form.salesmanId || !form.customerId) {
    products.value = []
    return
  }
  try {
    const res = await salesRecordApi.getProductsBySalesmanAndCustomer(form.salesmanId, form.customerId)
    const list = res.data || []
    products.value = list.length ? list : []
  } catch (err: any) {
    console.error('按销售员/客户加载产品失败', err)
    products.value = []
  }
}

function getTypeText(type: string): string {
  const map: Record<string, string> = {
    repair: '维修',
    maintenance: '保养',
    training: '培训',
    consult: '咨询',
    installation: '安装'
  }
  return map[type] || type
}

function getStatusText(status: string): string {
  const map: Record<string, string> = {
    pending: '待处理',
    processing: '处理中',
    completed: '已完成',
    cancelled: '已取消'
  }
  return map[status] || status
}

function formatDateTime(datetime?: string): string {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

function openNew() {
  editing.value = null
  form.salesmanId = isAdmin.value ? 0 : currentSalesmanId.value
  form.customerId = 0
  form.productId = 0
  form.serviceType = ''
  form.startTime = new Date().toISOString().slice(0, 16)
  form.content = ''
  showForm.value = true
  products.value = []
}

function edit(item: ServiceRecord) {
  editing.value = item
  form.salesmanId = item.salesmanId || 0
  form.customerId = item.customerId || 0
  form.productId = item.productId || 0
  form.serviceType = item.serviceType
  form.startTime = item.startTime ? item.startTime.slice(0, 16) : ''
  form.content = item.content
  showForm.value = true
  loadProductsByRelation()
}

async function save() {
  const salesmanId = isAdmin.value ? form.salesmanId : currentSalesmanId.value
  if (!salesmanId) return alert('请选择销售员')
  if (!form.customerId) return alert('请选择客户')
  if (!form.content.trim()) return alert('请填写服务内容')

  try {
    const payload: any = {
      salesmanId,
      customerId: form.customerId,
      productId: form.productId || null,
      serviceType: form.serviceType,
      startTime: form.startTime + ':00',
      content: form.content
    }
    
    if (editing.value?.id) {
      await serviceRecordApi.update(editing.value.id, payload)
    } else {
      await serviceRecordApi.create(payload)
    }
    showForm.value = false
    await loadData()
  } catch (err: any) {
    alert(err?.response?.data?.message || '保存失败')
  }
}

// 打开处理服务弹窗
function openProcessModal(item: ServiceRecord) {
  currentProcessing.value = item
  processForm.solution = ''
  showProcessModal.value = true
}

// 销售员处理服务
async function processService() {
  if (!processForm.solution.trim()) return alert('请填写处理结果')

  try {
    if (currentProcessing.value?.id) {
      await serviceRecordApi.processService(currentProcessing.value.id, processForm.solution)
      showProcessModal.value = false
      await loadData()
    }
  } catch (err: any) {
    alert(err?.response?.data?.message || '处理失败')
  }
}

// 打开客户评分弹窗
function openRateModal(item: ServiceRecord) {
  currentRating.value = item
  rateForm.satisfaction = 0
  showRateModal.value = true
}

// 打开所有服务记录日志
function openAllLogs() {
  showAllLogsModal.value = true
}

// 管理员客户打分
async function rateService() {
  if (!rateForm.satisfaction) return alert('请选择满意度评分')

  try {
    if (currentRating.value?.id) {
      await serviceRecordApi.rateService(currentRating.value.id, rateForm.satisfaction)
      showRateModal.value = false
      await loadData()
    }
  } catch (err: any) {
    alert(err?.response?.data?.message || '评分失败')
  }
}

function closeForm() {
  showForm.value = false
}

async function remove(item: ServiceRecord) {
  showConfirm('确认删除', `确认删除服务记录 ${item.serviceNo} 吗？`, async () => {
    try {
      if (item.id) {
        await serviceRecordApi.delete(item.id)
        await loadData()
      }
    } catch (err: any) {
      alert(err?.response?.data?.message || '删除失败')
    }
  })
}

function showConfirm(title: string, message: string, action: () => void) {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmAction.value = action
  showConfirmDialog.value = true
}

function handleConfirm() {
  showConfirmDialog.value = false
  if (confirmAction.value) {
    confirmAction.value()
  }
}
</script>

<style scoped>
@import '../assets/table-styles.css';

.content-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.badge-repair {
  background: linear-gradient(135deg, #ff6b35 0%, #ff8c61 100%);
  color: white;
}

.badge-repair {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
}

.badge-installation {
  background: linear-gradient(135deg, #7c3aed 0%, #8b5cf6 100%);
  color: white;
}

.badge-training {
  background: linear-gradient(135deg, #059669 0%, #10b981 100%);
  color: white;
}

.badge-consult {
  background: linear-gradient(135deg, #9333ea 0%, #a855f7 100%);
  color: white;
}

.badge-status-pending {
  background: #f3f4f6;
  color: #6b7280;
}

.badge-status-processing {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  color: white;
}

.badge-status-completed {
  background: linear-gradient(135deg, #059669 0%, #10b981 100%);
  color: white;
}

.badge-status-cancelled {
  background: #fef2f2;
  color: #dc2626;
  border: 1px solid #fca5a5;
}

.star-rating {
  display: flex;
  gap: 2px;
  font-size: 16px;
  justify-content: center;
}

.star-filled {
  color: #fbbf24;
}

.star-empty {
  color: #d1d5db;
}

.star-rating-input {
  display: flex;
  gap: 6px;
  font-size: 28px;
  margin-top: 8px;
}

.star-clickable {
  cursor: pointer;
  transition: all 0.2s;
  user-select: none;
}

.star-clickable:hover {
  transform: scale(1.2);
}

.info-row {
  display: flex;
  margin-bottom: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 6px;
  align-items: flex-start;
}

.info-row label {
  min-width: 90px;
  font-weight: 600;
  color: #374151;
}

.info-row span {
  flex: 1;
  color: #6b7280;
}

.modal-small {
  max-width: 500px;
}

.btn-success {
  background: linear-gradient(135deg, #059669 0%, #10b981 100%);
  border: none;
}

.btn-success:hover {
  background: linear-gradient(135deg, #047857 0%, #059669 100%);
}

.btn-primary {
  background: linear-gradient(135deg, #2563eb 0%, #3b82f6 100%);
  border: none;
  color: white;
}

.btn-primary:hover {
  background: linear-gradient(135deg, #1d4ed8 0%, #2563eb 100%);
}

.btn-log {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border: none;
  color: white;
}

.btn-log:hover {
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
}

/* 按钮禁用态样式 */
.btn-sm:disabled,
.btn-sm[disabled] {
  opacity: 0.4;
  cursor: not-allowed;
  filter: grayscale(50%);
}

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
