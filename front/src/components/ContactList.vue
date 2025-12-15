<template>
  <div class="page">
    <div class="toolbar">
      <button v-if="!isAdmin" @click="openNew">新增联络记录</button>
      
      <select v-if="isAdmin" v-model.number="filters.salesmanId" class="filter-select">
        <option :value="0">所有销售员</option>
        <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
      </select>
      
      <select v-model.number="filters.customerId" class="filter-select">
        <option :value="0">所有客户</option>
        <option v-for="c in customers" :key="c.id" :value="c.id">{{ c.name }}</option>
      </select>
      
      <select v-model="filters.outcome" class="filter-select">
        <option value="">跟进结果</option>
        <option value="signed">已签约</option>
        <option value="interested">感兴趣</option>
        <option value="thinking">考虑中</option>
        <option value="rejected">已拒绝</option>
        <option value="no_answer">未接通</option>
      </select>
      
      <div class="date-group">
        <label>起始日期</label>
        <input type="date" v-model="filters.dateFrom" class="date-input" :max="filters.dateTo" />
      </div>
      
      <div class="date-group">
        <label>截止日期</label>
        <input type="date" v-model="filters.dateTo" class="date-input" :min="filters.dateFrom" />
      </div>
      
      <input type="text" v-model.trim="filters.keyword" placeholder="联络单号 / 客户名称" class="search-input" />
      <button v-if="isAdmin" @click="openAllLogs" class="btn-log">📋 查看日志</button>
    </div>

    <section class="table-container">
      <table class="table enhanced-table">
        <thead>
          <tr>
            <th style="width: 150px;">联络单号</th>
            <th v-if="isAdmin" style="width: 100px;">销售员</th>
            <th style="width: 120px;">客户</th>
            <th style="width: 140px;">联络时间</th>
            <th style="width: 80px;">方式</th>
            <th style="width: 200px;">沟通内容</th>
            <th style="width: 100px;">跟进结果</th>
            <th style="width: 150px;">操作</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="r in pagedRecords" :key="r.id">
            <td>{{ r.contactNo || '-' }}</td>
            <td v-if="isAdmin">
              <div class="info-cell">
                <strong>{{ r.salesman?.name || getSalesmanName(r.salesmanId) }}</strong>
              </div>
            </td>
            <td>
              <div class="info-cell">
                <strong>{{ r.customer?.name || getCustomerName(r.customerId) }}</strong>
              </div>
            </td>
            <td>{{ formatDateTime(r.contactDate) }}</td>
            <td>
              <span :class="['method-tag', getMethodClass(r.contactWay)]">
                {{ getMethodIcon(r.contactWay) }} {{ getMethodText(r.contactWay) }}
              </span>
            </td>
            <td>
              <div class="content-cell" :title="r.content">
                {{ r.content }}
              </div>
            </td>
            <td>
              <span :class="['result-badge', getResultClass(r.outcome)]">
                {{ getOutcomeText(r.outcome) }}
              </span>
            </td>
            <td class="actions-cell">
              <button class="btn success" @click="edit(r)">编辑</button>
              <button class="btn danger" @click="remove(r)">删除</button>
            </td>
          </tr>
          <tr v-if="!filteredRecords.length">
            <td :colspan="isAdmin ? 9 : 8" class="empty">暂无数据</td>
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

    <div v-if="showForm" class="modal">
      <div class="modal-card">
        <h3>{{ editing ? '编辑联络记录' : '新增联络记录' }}</h3>
        
          <div v-if="editing?.contactNo" class="form-row">
            <label>联络单号</label>
            <input :value="editing.contactNo" readonly class="readonly-input contact-no-input" />
          </div>
          
          <div v-if="isAdmin" class="form-row">
            <label>销售员</label>
            <select v-model.number="form.salesmanId">
              <option :value="0" :disabled="form.salesmanId !== 0" selected>请选择销售员</option>
              <option v-for="s in salesmen" :key="s.id" :value="s.id">{{ s.name }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>客户</label>
            <select v-model.number="form.customerId">
              <option :value="0" :disabled="form.customerId !== 0" selected>请选择客户</option>
              <option v-for="c in filteredCustomers" :key="c.id" :value="c.id">{{ c.name }}</option>
            </select>
          </div>
          <div class="form-row">
            <label>联络时间</label>
            <input type="datetime-local" v-model="form.contactDate" />
          </div>
          <div class="form-row">
            <label>联络方式</label>
            <select v-model="form.contactWay">
              <option value="" :disabled="form.contactWay !== ''" selected>请选择联络方式</option>
              <option value="phone">电话</option>
              <option value="wechat">微信</option>
              <option value="email">邮件</option>
              <option value="visit">上门拜访</option>
              <option value="dinner">商务饭局</option>
              <option value="other">其他</option>
            </select>
          </div>
          <div class="form-row">
            <label>沟通内容</label>
            <textarea v-model="form.content" rows="3" placeholder="记录联络内容要点"></textarea>
          </div>
          <div class="form-row">
            <label>跟进结果</label>
            <select v-model="form.outcome">
              <option value="" :disabled="form.outcome !== ''">请选择跟进结果</option>
              <option value="signed">已签约</option>
              <option value="interested">感兴趣</option>
              <option value="thinking">考虑中</option>
              <option value="rejected">已拒绝</option>
              <option value="no_answer">未接通</option>
            </select>
          </div>
          <div class="form-row">
            <label>位置信息</label>
            <input v-model="form.location" placeholder="如：客户公司地址、会议室等" />
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
      :entity-name="'ContactRecord'"
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
import { salesmanApi, customerApi, contactRecordApi } from '../services/api'

const userRole = ref(localStorage.getItem('ms_role') || 'salesman')
const salesmanIdStr = localStorage.getItem('ms_salesmanId')
const currentSalesmanId = ref(salesmanIdStr ? Number(salesmanIdStr) : 0)
const isAdmin = computed(() => userRole.value === 'admin')

interface Salesman {
  id: number
  name: string
}

interface Customer {
  id: number
  name: string
  salesmanId?: number
}

interface ContactRecord {
  id?: number
  contactNo?: string
  salesmanId: number
  customerId: number
  contactDate: string
  contactWay: string
  content: string
  outcome: string
  location?: string
  filePath?: string
  salesman?: { name: string }
  customer?: { name: string }
}

const salesmen = ref<Salesman[]>([])
const customers = ref<Customer[]>([])
const records = ref<ContactRecord[]>([])
const currentPage = ref(1)
const pageSize = ref(15)

const filters = reactive({
  salesmanId: 0,
  customerId: 0,
  dateFrom: '',
  dateTo: '',
  keyword: '',
  outcome: ''
})

const showForm = ref(false)
const editing = ref<ContactRecord | null>(null)
const showAllLogsModal = ref(false)

const showConfirmDialog = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const confirmAction = ref<(() => void) | null>(null)
const form = reactive({
  salesmanId: 0,
  customerId: 0,
  contactDate: '',
  contactWay: '',
  content: '',
  outcome: '',
  location: ''
})

const filteredRecords = computed(() => {
  let result = records.value
  
  if (filters.salesmanId && isAdmin.value) {
    result = result.filter(r => r.salesmanId === filters.salesmanId)
  }
  
  if (filters.customerId) {
    result = result.filter(r => r.customerId === filters.customerId)
  }
  
  if (filters.outcome) {
    result = result.filter(r => r.outcome === filters.outcome)
  }
  
  if (filters.dateFrom) {
    const fromDate = new Date(filters.dateFrom)
    result = result.filter(r => new Date(r.contactDate) >= fromDate)
  }
  
  if (filters.dateTo) {
    const toDate = new Date(`${filters.dateTo}T23:59:59`)
    result = result.filter(r => new Date(r.contactDate) <= toDate)
  }
  
  if (filters.keyword) {
    const keyword = filters.keyword.toLowerCase()
    result = result.filter(r => {
      const haystack = [
        r.contactNo,
        r.customer?.name,
        r.content
      ].filter(Boolean).join('').toLowerCase()
      return haystack.includes(keyword)
    })
  }
  
  // 按联络单号或创建时间降序排序（最新的在前）
  result.sort((a, b) => {
    if (a.contactNo && b.contactNo) {
      return b.contactNo.localeCompare(a.contactNo)
    }
    return new Date(b.contactDate).getTime() - new Date(a.contactDate).getTime()
  })
  
  return result
})

const pagedRecords = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredRecords.value.slice(start, start + pageSize.value)
})

watch(filters, () => {
  currentPage.value = 1
}, { deep: true })

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
})

async function loadData() {
  try {
    const role = userRole.value
    const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value
    const res = await contactRecordApi.getAll(role, salesmanId)
    if (res.code === 200) {
      records.value = res.data || []
    }
  } catch (err: any) {
    alert(err?.response?.data?.message || '加载数据失败')
  }
}

async function loadSalesmen() {
  try {
    const res = await salesmanApi.getAll()
    if (res.code === 200) salesmen.value = res.data || []
  } catch (err: any) {
    console.error('加载销售员失败:', err)
  }
}

async function loadCustomers() {
  try {
    const role = userRole.value
    const salesmanId = isAdmin.value ? undefined : currentSalesmanId.value
    const res = await customerApi.getAll(role, salesmanId)
    if (res.code === 200) customers.value = res.data || []
  } catch (err: any) {
    console.error('加载客户失败:', err)
  }
}

function getSalesmanName(id: number) {
  return salesmen.value.find(s => s.id === id)?.name || '-'
}

function getCustomerName(id: number) {
  return customers.value.find(c => c.id === id)?.name || '-'
}

function formatDateTime(val: string) {
  if (!val) return '-'
  try {
    if (val.includes('T')) {
      return val.replace('T', ' ').slice(0, 16)
    }
    return val
  } catch {
    return val
  }
}

function formatNextContact(val?: string) {
  if (!val) return '-'
  try {
    if (val.includes('T')) {
      const date = new Date(val)
      const today = new Date()
      today.setHours(0, 0, 0, 0)
      const tomorrow = new Date(today)
      tomorrow.setDate(tomorrow.getDate() + 1)
      
      const dateStr = date.toLocaleDateString('zh-CN')
      
      if (date >= today && date < tomorrow) {
        return '今天 ' + val.slice(11, 16)
      }
      return dateStr + ' ' + val.slice(11, 16)
    }
    return val
  } catch {
    return val
  }
}

function getMethodIcon(method: string) {
  const icons: Record<string, string> = {
    'phone': '📞',
    'wechat': '💬',
    'email': '✉️',
    'visit': '🚪',
    'dinner': '🍽️',
    'other': '📝'
  }
  return icons[method] || '📝'
}

function getMethodText(method: string) {
  const texts: Record<string, string> = {
    'phone': '电话',
    'wechat': '微信',
    'email': '邮件',
    'visit': '拜访',
    'dinner': '饭局',
    'other': '其他'
  }
  return texts[method] || method
}

function getMethodClass(method: string) {
  const classes: Record<string, string> = {
    'phone': 'method-phone',
    'wechat': 'method-wechat',
    'email': 'method-email',
    'visit': 'method-visit',
    'dinner': 'method-dinner',
    'other': 'method-other'
  }
  return classes[method] || 'method-other'
}

function getOutcomeText(outcome?: string) {
  const texts: Record<string, string> = {
    'signed': '已签约',
    'interested': '感兴趣',
    'thinking': '考虑中',
    'rejected': '已拒绝',
    'no_answer': '未接通'
  }
  return texts[outcome || ''] || '-'
}

function getResultClass(result?: string) {
  const classes: Record<string, string> = {
    'signed': 'result-signed',
    'interested': 'result-interested',
    'thinking': 'result-thinking',
    'rejected': 'result-rejected',
    'no_answer': 'result-noanswer'
  }
  return classes[result || ''] || 'result-default'
}

function getLevelClass(level?: string) {
  const classes: Record<string, string> = {
    'S': 'level-s',
    'A': 'level-a',
    'B': 'level-b',
    'C': 'level-c'
  }
  return classes[level || ''] || 'level-default'
}

function isUrgentFollowup(date?: string) {
  if (!date) return false
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  const followupDate = new Date(date)
  followupDate.setHours(0, 0, 0, 0)
  return followupDate <= today
}

function openNew() {
  editing.value = null
  Object.assign(form, {
    salesmanId: isAdmin.value ? 0 : currentSalesmanId.value,
    customerId: 0,
    contactDate: new Date().toISOString().slice(0, 16),
    contactWay: '',
    content: '',
    outcome: '',
    location: ''
  })
  showForm.value = true
}

function edit(item: ContactRecord) {
  editing.value = item
  form.salesmanId = item.salesmanId || 0
  form.customerId = item.customerId || 0
  form.contactDate = item.contactDate.slice(0, 16)
  form.contactWay = item.contactWay
  form.content = item.content
  form.outcome = item.outcome
  form.location = item.location || ''
  showForm.value = true
}

async function save() {
  const salesmanId = isAdmin.value ? form.salesmanId : currentSalesmanId.value
  if (!salesmanId) return alert('请选择销售员')
  if (!form.customerId) return alert('请选择客户')
  if (!form.content.trim()) return alert('请填写沟通内容')
  if (!form.outcome) return alert('请选择跟进结果')

  try {
    const payload = {
      ...form,
      salesmanId,
      contactDate: form.contactDate + ':00'
    }
    
    if (editing.value?.id) {
      await contactRecordApi.update(editing.value.id, payload)
    } else {
      await contactRecordApi.create(payload, userRole.value)
    }
    showForm.value = false
    await loadData()
  } catch (err: any) {
    alert(err?.response?.data?.message || '保存失败')
  }
}

function closeForm() {
  showForm.value = false
  editing.value = null
  Object.assign(form, {
    salesmanId: 0,
    customerId: 0,
    contactDate: '',
    contactWay: '',
    content: '',
    outcome: '',
    location: ''
  })
}

async function remove(item: ContactRecord) {
  const label = item.id || ''
  showConfirm('确认删除', `确认删除联络记录 ${label} 吗？`, async () => {
    try {
      if (item.id) {
        await contactRecordApi.delete(item.id)
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

function openAllLogs() {
  showAllLogsModal.value = true
}

onMounted(async () => {
  await Promise.all([loadSalesmen(), loadCustomers(), loadData()])
})
</script>

<style scoped>
@import '../assets/table-styles.css';

/* 联络单号单元格 */
.contact-no-cell {
  display: flex;
  align-items: center;
  gap: 8px;
}

.contact-no {
  color: #333;
}

.copy-btn {
  background: transparent;
  border: none;
  cursor: pointer;
  font-size: 16px;
  padding: 2px 4px;
  opacity: 0.6;
  transition: all 0.2s;
}

.copy-btn:hover {
  opacity: 1;
  transform: scale(1.1);
}

.copy-btn:active {
  transform: scale(0.95);
}

/* 表单中的联络单号输入框 */
.contact-no-input {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 14px;
  color: #1890ff;
  font-weight: 600;
  background: #f0f9ff;
  border-color: #91d5ff;
  letter-spacing: 0.5px;
}

.readonly-input {
  background-color: #f5f5f5;
  cursor: not-allowed;
}

/* 信息单元格 */
.info-cell {
  display: flex;
  flex-direction: column;
  gap: 2px;
}

.info-cell strong {
  color: #333;
  font-weight: 600;
}

/* 内容单元格 - 省略号显示 */
.content-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  cursor: help;
}

/* 联络方式标签 */
.method-tag {
  display: inline-flex;
  align-items: center;
  gap: 4px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-weight: 500;
}

.method-phone { background: #e6f7ff; color: #1890ff; }
.method-wechat { background: #f0f9ff; color: #07c160; }
.method-email { background: #fff7e6; color: #fa8c16; }
.method-visit { background: #f6ffed; color: #52c41a; }
.method-other { background: #f5f5f5; color: #666; }

/* 跟进结果徽章 */
.result-badge {
  display: inline-block;
  padding: 4px 10px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
  text-align: center;
}

.result-interested {
  background: #f6ffed;
  color: #52c41a;
  border: 1px solid #b7eb8f;
}

.result-thinking {
  background: #e6f7ff;
  color: #1890ff;
  border: 1px solid #91d5ff;
}

.result-noanswer {
  background: #fff7e6;
  color: #fa8c16;
  border: 1px solid #ffd591;
}

.result-rejected {
  background: #f5f5f5;
  color: #999;
  border: 1px solid #d9d9d9;
}

.result-default {
  background: #fafafa;
  color: #bbb;
}

/* 意向等级徽章 */
.level-badge {
  display: inline-block;
  width: 32px;
  height: 32px;
  line-height: 32px;
  text-align: center;
  border-radius: 50%;
  font-size: 14px;
  font-weight: 700;
}

.level-s {
  background: linear-gradient(135deg, #ff4d4f 0%, #f5222d 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(255, 77, 79, 0.4);
}

.level-a {
  background: linear-gradient(135deg, #ff7a45 0%, #fa8c16 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(250, 140, 22, 0.4);
}

.level-b {
  background: linear-gradient(135deg, #40a9ff 0%, #1890ff 100%);
  color: #fff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.4);
}

.level-c {
  background: #d9d9d9;
  color: #666;
}

.level-default {
  background: #fafafa;
  color: #bbb;
}

/* 下次跟进日期 */
.next-followup {
  font-size: 13px;
  color: #666;
}

.next-followup.urgent {
  color: #ff4d4f;
  font-weight: 600;
}

/* 增强的表格样式 */
.enhanced-table tbody tr:hover {
  background: #f0f5ff;
  transition: background 0.2s;
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
