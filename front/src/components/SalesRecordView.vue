<template>
  <div class="page">
    <div class="tabs-row">
      <button
        v-for="tab in tabs"
        :key="tab.status"
        :class="['tab-btn', { active: selectedTab === tab.status }]"
        @click="changeTab(tab.status)"
      >
        {{ tab.label }} {{ tabCounts[tab.status] ?? 0 }}
      </button>
    </div>

    <div class="toolbar">
      <!-- 销售员模式：新增 -->
      <button v-if="!isAdmin" @click="openNew">新增记录</button>

      <!-- 管理员模式：筛选销售员 -->
      <select v-if="isAdmin" v-model.number="adminFilters.salesmanId" class="filter-select">
        <option :value="0">所有销售员</option>
        <option v-for="sm in salesmen" :key="sm.id" :value="sm.id">{{ sm.name }}</option>
      </select>

      <div class="date-group">
        <label>起始日期</label>
        <input type="date" v-model="dateFrom" class="date-input" :max="dateTo" />
      </div>

      <div class="date-group">
        <label>截止日期</label>
        <input type="date" v-model="dateTo" class="date-input" :min="dateFrom" />
      </div>

      <input type="text" v-model.trim="keyword" placeholder="订单号 / 客户 / 产品" class="search-input" />

      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <section class="table-container">
      <!-- 管理员表格 -->
      <table v-if="isAdmin" class="table">
        <thead>
          <tr>
            <th>订单编号</th>
            <th>销售员</th>
            <th>客户</th>
            <th>产品</th>
            <th>数量</th>
            <th>总金额</th>
            <th>提成金额</th>
            <th>销售日期</th>
            <th>状态</th>
            <th v-if="selectedTab === 'rejected'">拒绝原因</th>
            <th v-if="selectedTab === 'pending'">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in pagedRecords" :key="record.orderNo">
            <td>{{ record.orderNo }}</td>
            <td>{{ record.salesman?.name || getSalesmanName(record.salesmanId) }}</td>
            <td>{{ record.customer?.name || record.customerName || '-' }}</td>
            <td>{{ record.product?.name || record.productName || '-' }}</td>
            <td>{{ record.quantity }}</td>
            <td>{{ formatCurrency(record.totalAmount) }}</td>
            <td>{{ formatCurrency(record.commissionAmount) }}</td>
            <td>{{ formatDate(record.saleDate) }}</td>
            <td>
              <span :class="['status-badge', record.status]">{{ statusLabels[record.status] || '未知' }}</span>
            </td>
            <td v-if="selectedTab === 'rejected'" class="reject-reason-cell">
              {{ record.rejectReason || '-' }}
            </td>
            <td v-if="selectedTab === 'pending'" class="actions">
              <button class="btn success" @click="approve(record)">通过</button>
              <button class="btn danger" @click="reject(record)">拒绝</button>
              <button class="btn" @click="returnToDraft(record)">撤回</button>
            </td>
          </tr>
          <tr v-if="!filteredRecords.length">
            <td colspan="10" class="empty">暂无符合筛选条件的数据</td>
          </tr>
        </tbody>
      </table>

      <!-- 销售员表格 -->
      <table v-else class="table">
        <thead>
          <tr>
            <th>订单编号</th>
            <th>客户</th>
            <th>产品</th>
            <th>数量</th>
            <th>单价</th>
            <th>总金额</th>
            <th>状态</th>
            <th v-if="selectedTab === 'rejected'">拒绝原因</th>
            <th v-if="selectedTab === 'draft' || selectedTab === 'rejected'">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="record in pagedRecords" :key="record.orderNo">
            <td>{{ record.orderNo }}</td>
            <td>{{ getCustomerName(record.customerId) }}</td>
            <td>{{ getProductName(record.productId) }}</td>
            <td>{{ record.quantity }}</td>
            <td>{{ formatCurrency(record.unitPrice) }}</td>
            <td>{{ formatCurrency(record.totalAmount) }}</td>
            <td>
              <span :class="['status-badge', record.status]">{{ statusLabels[record.status] || '-' }}</span>
            </td>
            <td v-if="selectedTab === 'rejected'" class="reject-reason-cell">
              {{ record.rejectReason || '-' }}
            </td>
            <td v-if="selectedTab === 'draft'" class="actions-cell">
              <button class="btn" @click="edit(record)">编辑</button>
              <button class="btn primary" @click="submit(record)">{{ record.fromWithdrawn ? '重新提交' : '提交' }}</button>
              <button class="btn danger" @click="remove(record)">删除</button>
            </td>
            <td v-else-if="selectedTab === 'rejected'" class="actions-cell">
              <button class="btn success" @click="edit(record)">修改</button>
              <button class="btn success" @click="submit(record)">重新提交</button>
            </td>
          </tr>
          <tr v-if="!rawRecords.length">
            <td :colspan="selectedTab === 'draft' || selectedTab === 'rejected' ? 8 : 7" class="empty">暂无数据</td>
          </tr>
        </tbody>
      </table>
    </section>

    <Pagination
      :total="filteredRecords.length"
      :pageSize="pageSize"
      :currentPage="currentPage"
      @update:currentPage="currentPage = $event"
    />

    <!-- 销售员：新增/编辑表单 -->
    <div v-if="!isAdmin && showForm" class="modal">
      <div class="modal-card">
        <h3>{{ editing?.orderNo ? `编辑订单 ${editing.orderNo}` : '新增销售记录' }}</h3>

        <div class="form-row">
          <label>客户</label>
          <select v-model.number="form.customerId">
            <option :value="0" disabled>请选择客户</option>
            <option v-for="cust in customers" :key="cust.id" :value="cust.id">{{ cust.name }}</option>
          </select>
        </div>
        <div class="form-row">
          <label>产品</label>
          <select v-model.number="form.productId" @change="onProductChange">
            <option :value="0" disabled>请选择产品</option>
            <option v-for="prod in products" :key="prod.id" :value="prod.id">{{ prod.name }} ({{ prod.productNo }})</option>
          </select>
        </div>
        <div class="form-row">
          <label>数量</label>
          <input type="number" min="1" v-model.number="form.quantity" />
        </div>
        <div class="form-row">
          <label>单价</label>
          <input type="number" min="0" step="0.01" v-model.number="form.unitPrice" />
        </div>
        <div class="form-row">
          <label>总金额</label>
          <div class="total-amount">{{ formatCurrency(formTotalAmount) }}</div>
        </div>
        <div class="form-row">
          <label>销售日期</label>
          <input type="date" v-model="form.saleDate" :max="today" />
        </div>
        <div class="form-row textarea-row">
          <label>备注</label>
          <textarea v-model="form.remark" rows="3" placeholder="可选"></textarea>
        </div>

        <div class="modal-actions">
          <button class="btn-cancel" @click="closeForm">取消</button>
          <button class="btn-save" @click="handleSave()">保存</button>
        </div>
      </div>
    </div>

    <!-- 管理员：编辑订单弹窗（仅保留 AdminAuditView 原样式/字段） -->
    <div v-if="isAdmin && showEdit" class="modal-overlay" @click.self="closeEdit">
      <div class="edit-modal">
        <header class="modal-header">
          <h3>编辑订单 {{ adminEditing?.orderNo }}</h3>
          <button class="close-btn" @click="closeEdit">×</button>
        </header>
        <section class="modal-body">
          <label>
            数量
            <input type="number" min="1" v-model.number="adminForm.quantity" />
          </label>
          <label>
            单价
            <input type="number" min="0" step="0.01" v-model.number="adminForm.unitPrice" />
          </label>
          <label>
            销售日期
            <input type="date" v-model="adminForm.saleDate" />
          </label>
          <label>
            备注
            <textarea v-model="adminForm.remark" rows="3" placeholder="可选"></textarea>
          </label>
        </section>
        <footer class="modal-footer">
          <button class="btn" @click="closeEdit">取消</button>
          <button class="btn primary" @click="updateEditing">保存</button>
        </footer>
      </div>
    </div>

    <!-- 复用确认框（管理员：可带拒绝原因；销售员：仅确认/取消） -->
    <div v-if="showConfirmDialog" class="modal">
      <div class="modal-card confirm-dialog">
        <h3>{{ confirmTitle }}</h3>
        <p class="confirm-message">{{ confirmMessage }}</p>
        <input
          v-if="isAdmin && showRejectDialog"
          v-model="rejectReason"
          type="text"
          placeholder="请输入拒绝原因"
          class="reject-input"
        />
        <div class="modal-actions">
          <button @click="handleConfirm" class="primary">确认</button>
          <button @click="showConfirmDialog = false" class="muted">取消</button>
        </div>
      </div>
    </div>

    <!-- 管理员 toast（保留现有样式） -->
    <div v-if="isAdmin && toast.visible" :class="['toast', toast.type]">
      {{ toast.message }}
    </div>

    <AuditLogModal
      :show="showAllLogsModal"
      :entity-name="'SalesRecord'"
      :entity-id="0"
      @close="showAllLogsModal = false"
    />
  </div>
</template>

<script setup lang="ts">
import '../assets/table-styles.css'
import { ref, reactive, computed, watch, onMounted } from 'vue'
import Pagination from './Pagination.vue'
import AuditLogModal from './AuditLogModal.vue'
import { salesRecordApi, salesmanApi, customerApi, productApi } from '../services/api'

interface SalesRecord {
  id?: number
  orderNo: string
  status: 'draft' | 'pending' | 'approved' | 'rejected'
  salesmanId: number
  customerId: number
  productId: number
  quantity: number
  unitPrice?: number
  totalAmount?: number
  commissionAmount?: number
  saleDate: string
  remark?: string | null
  rejectReason?: string | null
  fromWithdrawn?: boolean
  salesman?: { id: number; name: string }
  customer?: { id: number; name: string }
  product?: { id: number; name: string; productNo?: string }
  customerName?: string
  productName?: string
}

interface Salesman { id: number; name: string; commissionRate?: number }
interface Customer { id: number; name: string }
interface Product { id: number; name: string; productNo: string; price?: number }

type AdminTab = 'pending' | 'approved' | 'rejected'
type SalesmanTab = 'draft' | 'pending' | 'approved' | 'rejected'
type TabStatus = AdminTab | SalesmanTab

const userRole = ref(localStorage.getItem('ms_role') || 'salesman')
const isAdmin = computed(() => userRole.value === 'admin')

const username = localStorage.getItem('ms_username') || 'system'
const salesmanIdStr = localStorage.getItem('ms_salesmanId')
const salesmanId = salesmanIdStr ? Number(salesmanIdStr) : 0

const statusLabels: Record<SalesRecord['status'], string> = {
  draft: '草稿箱',
  pending: '待审核',
  approved: '已确认',
  rejected: '已拒绝',
}

const tabs = computed(() => {
  if (isAdmin.value) {
    return [
      { status: 'pending' as const, label: '待审核' },
      { status: 'approved' as const, label: '已确认' },
      { status: 'rejected' as const, label: '已拒绝' },
    ]
  }
  return [
    { status: 'draft' as const, label: '草稿箱' },
    { status: 'pending' as const, label: '待审核' },
    { status: 'approved' as const, label: '已确认' },
    { status: 'rejected' as const, label: '已拒绝' },
  ]
})

const selectedTab = ref<TabStatus>('pending')
watch(isAdmin, (val) => {
  selectedTab.value = val ? 'pending' : 'draft'
}, { immediate: true })

const rawRecords = ref<SalesRecord[]>([])
const currentPage = ref(1)
const pageSize = ref(10)
const showAllLogsModal = ref(false)

// 统一筛选字段（管理员/销售员共用）
const dateFrom = ref('')
const dateTo = ref('')
const keyword = ref('')

const adminFilters = reactive({ salesmanId: 0 })

const WITHDRAWN_FLAG_KEY = 'ms_withdrawn_orders'

function readWithdrawnFlags(): Record<string, boolean> {
  try {
    const raw = localStorage.getItem(WITHDRAWN_FLAG_KEY)
    if (!raw) return {}
    const parsed = JSON.parse(raw)
    return parsed && typeof parsed === 'object' ? parsed : {}
  } catch (e) {
    console.warn('failed to read withdrawn flags', e)
    return {}
  }
}

function writeWithdrawnFlags(flags: Record<string, boolean>) {
  localStorage.setItem(WITHDRAWN_FLAG_KEY, JSON.stringify(flags))
}

function tagWithdrawn(records: SalesRecord[]) {
  const flags = readWithdrawnFlags()
  records.forEach(r => { r.fromWithdrawn = !!flags[r.orderNo] })
}

const filteredRecords = computed(() => {
  const from = dateFrom.value
  const to = dateTo.value
  const kw = keyword.value?.trim().toLowerCase()

  return rawRecords.value.filter((record) => {
    if (isAdmin.value && adminFilters.salesmanId && record.salesmanId !== adminFilters.salesmanId) return false

    const saleDate = new Date(record.saleDate)
    if (from && saleDate < new Date(from)) return false
    if (to && saleDate > new Date(`${to}T23:59:59`)) return false

    if (kw) {
      const haystack = [
        record.orderNo,
        record.customer?.name,
        record.customerName,
        record.product?.name,
        record.productName,
      ].filter(Boolean).join('').toLowerCase()
      if (!haystack.includes(kw)) return false
    }
    return true
  })
})

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRecords.value.slice(start, start + pageSize.value)
})

const tabCounts = reactive<Record<string, number>>({
  draft: 0,
  pending: 0,
  approved: 0,
  rejected: 0,
})

// 管理员：toast
const toast = reactive({ visible: false, message: '', type: 'success' as 'success' | 'error' })
function showToast(message: string, type: 'success' | 'error' = 'success') {
  toast.message = message
  toast.type = type
  toast.visible = true
  setTimeout(() => { toast.visible = false }, 3000)
}

// 资源（管理员：销售员下拉；销售员：客户/产品）
const salesmen = ref<Salesman[]>([])
const customers = ref<Customer[]>([])
const products = ref<Product[]>([])

// 销售员：表单
const showForm = ref(false)
const editing = ref<SalesRecord | null>(null)
const form = reactive({
  customerId: 0,
  productId: 0,
  quantity: 0,
  unitPrice: 0,
  saleDate: new Date().toISOString().split('T')[0],
  remark: '' as string | null,
})
const today = new Date().toISOString().split('T')[0]
const formTotalAmount = computed(() => (form.unitPrice || 0) * (form.quantity || 0))

// 管理员：编辑弹窗
const showEdit = ref(false)
const adminEditing = ref<SalesRecord | null>(null)
const adminForm = reactive({
  quantity: 1,
  unitPrice: 0,
  saleDate: new Date().toISOString().split('T')[0],
  remark: '' as string | null,
})

// 确认框（共用）
const showConfirmDialog = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)

// 管理员拒绝原因
const showRejectDialog = ref(false)
const rejectReason = ref('')

function showConfirm(title: string, message: string, action: () => void) {
  confirmTitle.value = title
  confirmMessage.value = message
  confirmAction.value = action
  showConfirmDialog.value = true
}

function handleConfirm() {
  showConfirmDialog.value = false
  if (confirmAction.value) confirmAction.value()
}

function openAllLogs() {
  showAllLogsModal.value = true
}

async function loadSalesmen() {
  const { data } = await salesmanApi.getAll(true)
  salesmen.value = data || []
}

async function loadSalesmanResources() {
  if (!salesmanId) return
  const [custRes, prodRes] = await Promise.all([
    customerApi.getAll('salesman', salesmanId),
    productApi.getAll(),
  ])
  customers.value = custRes.data || []
  products.value = prodRes.data || []
}

async function loadData() {
  try {
    if (isAdmin.value) {
      const res = await salesRecordApi.getByStatus(selectedTab.value as AdminTab)
      rawRecords.value = res.data || []
    } else {
      if (!salesmanId) {
        rawRecords.value = []
        return
      }
      const res = await salesRecordApi.getBySalesmanAndStatus(salesmanId, selectedTab.value as SalesmanTab)
      rawRecords.value = res.data || []
      tagWithdrawn(rawRecords.value)
    }
    await refreshCounts()
  } catch (error: any) {
    if (isAdmin.value) {
      showToast(error?.response?.data?.message || '加载数据失败', 'error')
    } else {
      console.error(error)
      rawRecords.value = []
    }
  }
}

async function refreshCounts() {
  try {
    if (isAdmin.value) {
      const [pendingRes, approvedRes, rejectedRes] = await Promise.all([
        salesRecordApi.getByStatus('pending'),
        salesRecordApi.getByStatus('approved'),
        salesRecordApi.getByStatus('rejected'),
      ])
      tabCounts.pending = pendingRes.data?.length || 0
      tabCounts.approved = approvedRes.data?.length || 0
      tabCounts.rejected = rejectedRes.data?.length || 0
    } else {
      if (!salesmanId) return
      const statuses: SalesmanTab[] = ['draft', 'pending', 'approved', 'rejected']
      const responses = await Promise.all(statuses.map(s => salesRecordApi.getBySalesmanAndStatus(salesmanId, s)))
      statuses.forEach((status, index) => {
        tabCounts[status] = responses[index]?.data?.length || 0
      })
    }
  } catch (e) {
    console.error(e)
  }
}

function changeTab(status: TabStatus) {
  selectedTab.value = status
  currentPage.value = 1
  loadData()
}

// ---------- 管理员操作 ----------
async function approve(record: SalesRecord) {
  if (!record.orderNo) return
  showRejectDialog.value = false
  showConfirm('确认审核', `确认审核通过订单 ${record.orderNo} 吗？`, async () => {
    try {
      await salesRecordApi.approveByOrderNo(record.orderNo, username)
      await loadData()
      showToast('订单已通过审核', 'success')
    } catch (error: any) {
      showToast(error?.response?.data?.message || '审核失败', 'error')
    }
  })
}

function reject(record: SalesRecord) {
  if (!record.orderNo) return
  rejectReason.value = ''
  showRejectDialog.value = true
  showConfirm('确认拒绝', `确定拒绝订单 ${record.orderNo} 吗？`, async () => {
    if (!rejectReason.value.trim()) {
      showToast('请输入拒绝原因', 'error')
      return
    }
    try {
      await salesRecordApi.rejectByOrderNo(record.orderNo, username, rejectReason.value.trim())
      rejectReason.value = ''
      showRejectDialog.value = false
      await loadData()
      showToast('订单已拒绝', 'success')
    } catch (error: any) {
      showToast(error?.response?.data?.message || '拒绝失败', 'error')
    }
  })
}

async function returnToDraft(record: SalesRecord) {
  if (!record.orderNo) return
  showRejectDialog.value = false
  showConfirm('确认撤回', `确认将订单 ${record.orderNo} 撤回至草稿吗？`, async () => {
    try {
      await salesRecordApi.withdrawByOrderNo(record.orderNo, username)
      const flags = readWithdrawnFlags()
      flags[record.orderNo] = true
      writeWithdrawnFlags(flags)
      await loadData()
      showToast('订单已撤回至草稿', 'success')
    } catch (error: any) {
      showToast(error?.response?.data?.message || '撤回失败', 'error')
    }
  })
}

async function updateEditing() {
  if (!adminEditing.value?.orderNo) return
  try {
    await salesRecordApi.updateByOrderNo(adminEditing.value.orderNo, {
      quantity: adminForm.quantity,
      unitPrice: adminForm.unitPrice,
      saleDate: adminForm.saleDate,
      remark: adminForm.remark,
    })
    showEdit.value = false
    await loadData()
    showToast('订单已保存', 'success')
  } catch (error: any) {
    showToast(error?.response?.data?.message || '保存失败', 'error')
  }
}

function closeEdit() {
  showEdit.value = false
}

// ---------- 销售员操作 ----------
function openNew() {
  editing.value = null
  form.customerId = 0
  form.productId = 0
  form.quantity = 0
  form.unitPrice = 0
  form.saleDate = new Date().toISOString().split('T')[0]
  form.remark = ''
  showForm.value = true
}

function edit(record: SalesRecord) {
  editing.value = record
  form.customerId = record.customerId || (record.customer?.id || 0)
  form.productId = record.productId || (record.product?.id || 0)
  form.quantity = record.quantity
  form.unitPrice = Number(record.unitPrice || 0)
  form.saleDate = record.saleDate
  form.remark = record.remark ?? ''
  showForm.value = true
}

function closeForm() {
  showForm.value = false
}

function onProductChange() {
  const prod = products.value.find(p => p.id === form.productId)
  if (prod?.price !== undefined) form.unitPrice = prod.price
}

function buildPayload(currentStatus: SalesRecord['status']) {
  return {
    salesman: { id: salesmanId },
    customer: { id: form.customerId },
    product: { id: form.productId },
    quantity: form.quantity,
    unitPrice: form.unitPrice,
    totalAmount: form.quantity * form.unitPrice,
    saleDate: form.saleDate,
    remark: form.remark,
    status: currentStatus,
    createdBy: username,
  }
}

async function handleSave() {
  if (!salesmanId) return alert('系统错误：无法识别当前销售员身份')
  if (!form.customerId) return alert('请选择客户')
  if (!form.productId) return alert('请选择产品')
  if (!form.quantity || form.quantity <= 0) return alert('请输入有效的数量')
  if (form.unitPrice === undefined || form.unitPrice < 0) return alert('请输入有效的单价')
  if (!form.saleDate) return alert('请选择销售日期')

  try {
    let targetStatus: SalesRecord['status'] = 'draft'
    if (editing.value) targetStatus = editing.value.status

    const payload = buildPayload(targetStatus)
    if (editing.value?.orderNo) {
      await salesRecordApi.updateByOrderNo(editing.value.orderNo, payload)
    } else {
      await salesRecordApi.create(payload)
    }
    showForm.value = false
    await loadData()
    if (!editing.value && selectedTab.value !== 'draft') {
      changeTab('draft')
    }
  } catch (error: any) {
    console.error(error)
    alert(error?.response?.data?.message || '保存失败，请检查数据')
  }
}

async function submit(record: SalesRecord) {
  if (!record.orderNo) return
  try {
    await salesRecordApi.submitByOrderNo(record.orderNo, username)
    const flags = readWithdrawnFlags()
    if (flags[record.orderNo]) {
      delete flags[record.orderNo]
      writeWithdrawnFlags(flags)
    }
    selectedTab.value = 'pending'
    await loadData()
  } catch (error: any) {
    alert(error?.response?.data?.message || '提交失败')
  }
}

async function remove(record: SalesRecord) {
  if (!record.orderNo) return
  showConfirm('确认删除', `确认删除订单 ${record.orderNo} 吗？`, async () => {
    try {
      await salesRecordApi.deleteByOrderNo(record.orderNo)
      const flags = readWithdrawnFlags()
      if (flags[record.orderNo]) {
        delete flags[record.orderNo]
        writeWithdrawnFlags(flags)
      }
      await loadData()
    } catch (error: any) {
      alert(error?.response?.data?.message || '删除失败')
    }
  })
}

function getSalesmanName(id: number) {
  return salesmen.value.find(s => s.id === id)?.name || '-'
}

function getCustomerName(id: number) {
  const customer = customers.value.find(c => c.id === id)
  return customer ? customer.name : '-'
}

function getProductName(id: number) {
  const product = products.value.find(p => p.id === id)
  return product ? product.name : '-'
}

function formatCurrency(value?: number | null) {
  return `¥${Number(value || 0).toLocaleString('zh-CN', { minimumFractionDigits: 2 })}`
}

function formatDate(date: string) {
  if (!date) return '-'
  return new Date(date).toLocaleDateString('zh-CN')
}

watch([dateFrom, dateTo, keyword, () => adminFilters.salesmanId], () => {
  currentPage.value = 1
})

onMounted(async () => {
  if (isAdmin.value) {
    await loadSalesmen()
  } else {
    await loadSalesmanResources()
  }
  await loadData()
})
</script>

<style scoped>
@import '../assets/table-styles.css';

.tabs-row {
  display: flex;
  gap: 8px;
  margin-bottom: 16px;
}
.tab-btn {
  padding: 8px 16px;
  border: 1px solid #e2e8f0;
  background: #ffffff;
  border-radius: 8px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
  color: #334155;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.04);
}
.tab-btn.active {
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #0b1221;
  border-color: transparent;
  box-shadow: 0 10px 22px rgba(14, 165, 233, 0.28);
}
.tab-btn:hover {
  border-color: #0ea5e9;
}

.actions {
  display: flex;
  gap: 8px;
}

/* 销售员表单样式（来自 SalesmanRecordView） */
.modal { position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.modal-card { background: white; border-radius: 12px; width: 500px; max-width: 90%; max-height: 85vh; padding: 24px; box-shadow: 0 10px 25px rgba(0,0,0,0.2); }
.modal-card h3 { margin-top: 0; margin-bottom: 20px; font-size: 20px; color: #1e293b; }

.form-row { display: flex; align-items: center; gap: 16px; margin-bottom: 18px; }
.form-row label { width: 100px; text-align: justify; text-align-last: justify; font-size: 14px; color: #475569; font-weight: 500; flex-shrink: 0; }
.form-row input, .form-row select, .form-row textarea, .form-row .total-amount { flex: 1; width: 0; }
.form-row input, .form-row select, .form-row textarea { padding: 10px 14px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; }
.form-row textarea { min-height: 70px; resize: vertical; }
.form-row.textarea-row { align-items: flex-start; }
.form-row.textarea-row label { padding-top: 10px; }
.total-amount { font-size: 16px; font-weight: 600; color: #333; padding: 8px 12px; }

.modal-actions { display: flex; justify-content: center; gap: 12px; padding-top: 20px; margin-top: 20px; border-top: 1px solid #e5e7eb; }
.modal-actions button { min-width: 100px; padding: 10px 24px; border: none; border-radius: 6px; cursor: pointer; font-weight: 600; font-size: 14px; transition: all 0.2s; }
.btn-save { background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%); color: #0b1221; box-shadow: 0 8px 20px rgba(14, 165, 233, 0.26); border-radius: 10px; }
.btn-save:hover { box-shadow: 0 10px 24px rgba(14, 165, 233, 0.32); transform: translateY(-1px); }
.btn-cancel { background: #ffffff; color: #475569; border: 1px solid #e2e8f0; border-radius: 10px; box-shadow: 0 1px 3px rgba(0,0,0,0.04); }
.btn-cancel:hover { background: #f8fafc; box-shadow: 0 4px 12px rgba(14, 165, 233, 0.12); }

.actions-cell { display: flex; flex-wrap: wrap; gap: 6px; }
.date-group { display: flex; flex-direction: row; align-items: center; gap: 8px; }
.date-group label { font-size: 14px; color: #333; white-space: nowrap; }
.date-input { padding: 6px 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; min-width: 140px; }
.search-input { padding: 6px 8px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; min-width: 200px; }
.reject-reason-cell { max-width: 200px; word-wrap: break-word; font-size: 13px; color: #d32f2f; background: #ffebee; padding: 8px; border-radius: 4px; border-left: 3px solid #d32f2f; font-weight: 500; }

/* 管理员编辑弹窗（来自 AdminAuditView） */
.modal-overlay { position: fixed; inset: 0; background: rgba(0,0,0,0.35); display: flex; align-items: center; justify-content: center; z-index: 1000; }
.edit-modal {
  width: min(480px, 90vw);
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
}
.modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid #eee; padding-bottom: 12px; }
.modal-header h3 { margin: 0; font-size: 16px; }
.close-btn { border: none; background: transparent; font-size: 24px; cursor: pointer; color: #999; line-height: 1; padding: 0; }
.modal-footer { display: flex; justify-content: flex-end; gap: 8px; }
.modal-body textarea { width: 100%; padding: 10px; border: 1px solid #ddd; border-radius: 4px; font-size: 14px; box-sizing: border-box; }
.modal-body label { display: block; margin-bottom: 8px; font-size: 14px; }

/* 确认框样式（兼容 AdminAuditView） */
.confirm-dialog { max-width: 520px; padding: 22px; }
.confirm-dialog h3 { font-size: 20px; margin: 0 0 6px 0; }
.confirm-message { margin: 18px 0 22px 0; font-size: 15px; color: #444; text-align: center; }
.reject-input { width: 100%; padding: 10px 12px; border: 1px solid #ddd; border-radius: 6px; font-size: 14px; margin: 16px 0 22px 0; box-sizing: border-box; }
.modal-actions .primary { background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%); color: #0b1221; box-shadow: 0 8px 18px rgba(14, 165, 233, 0.24); }
.modal-actions .muted { background: #ffffff; color: #475569; border: 1px solid #e2e8f0; }

/* toast（管理员） */
.toast {
  position: fixed; left: 50%; top: 80px; transform: translateX(-50%);
  padding: 12px 24px; border-radius: 8px; font-weight: 600; z-index: 9999;
  box-shadow: 0 4px 12px rgba(0,0,0,0.15); color: #fff;
}
.toast.success { background: #10b981; }
.toast.error { background: #ef4444; }

/* 日志按钮 */
.btn-log {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  border: none;
  color: white;
  padding: 8px 16px;
  border-radius: 6px;
  cursor: pointer;
  font-weight: 600;
  transition: all 0.2s;
  box-shadow: 0 2px 4px rgba(139, 92, 246, 0.2);
}
.btn-log:hover {
  background: linear-gradient(135deg, #7c3aed 0%, #6d28d9 100%);
  box-shadow: 0 4px 8px rgba(139, 92, 246, 0.3);
  transform: translateY(-1px);
}
</style>
