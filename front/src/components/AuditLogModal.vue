<template>
  <div v-if="show" class="modal" @click.self="$emit('close')">
    <div class="modal-card logs-modal">
      <div class="modal-header">
        <h3>操作日志</h3>
        <button class="close-btn" @click="$emit('close')">×</button>
      </div>

      <div class="logs-filter">
        <input 
          v-model="searchText" 
          type="text" 
          placeholder="搜索操作人、操作类型..." 
          class="search-input"
        />
      </div>

      <div class="logs-container">
        <div v-if="loading" class="loading">加载中...</div>
        <div v-else-if="!filteredLogs.length" class="empty">暂无日志记录</div>
        <div v-else class="logs-list">
          <div 
            v-for="log in filteredLogs" 
            :key="log.id" 
            class="log-item"
            :class="'log-' + log.operation.toLowerCase()"
          >
            <div class="log-header">
              <span class="log-operation" :class="'op-' + log.operation.toLowerCase()">
                {{ getOperationText(log.operation) }}
              </span>
              <span class="log-time">{{ formatDateTime(log.operationTime) }}</span>
            </div>
            <div class="log-body">
              <div class="log-row">
                <span class="log-label">操作人：</span>
                <span class="log-value">{{ log.operator }} ({{ getOperatorTypeText(log.operatorType) }})</span>
              </div>
              <div v-if="log.description" class="log-row">
                <span class="log-label">描述：</span>
                <span class="log-value">{{ log.description }}</span>
              </div>
              <div v-if="diffFields(log).length" class="diff-list">
                <div v-for="item in diffFields(log)" :key="item.key" class="diff-row">
                  <div class="diff-label">{{ item.label }}</div>
                  <div class="diff-values">
                    <span class="pill old-pill">{{ formatPrimitive(item.old) }}</span>
                    <span class="diff-arrow">→</span>
                    <span class="pill new-pill">{{ formatPrimitive(item.new) }}</span>
                  </div>
                </div>
              </div>
              <div v-else-if="log.oldValue && log.newValue" class="log-changes">
                <div class="change-item">
                  <span class="change-label">变更前：</span>
                  <span class="change-value old-value">{{ formatPretty(log.oldValue, log.entityName) }}</span>
                </div>
                <div class="change-arrow">→</div>
                <div class="change-item">
                  <span class="change-label">变更后：</span>
                  <span class="change-value new-value">{{ formatPretty(log.newValue, log.entityName) }}</span>
                </div>
              </div>
              <div v-else-if="log.oldValue && !log.newValue" class="log-changes">
                <div class="change-item">
                  <span class="change-label">删除内容：</span>
                  <span class="change-value old-value">{{ formatPretty(log.oldValue, log.entityName) }}</span>
                </div>
              </div>
              <div v-else-if="!log.oldValue && log.newValue" class="log-changes">
                <div class="change-item">
                  <span class="change-label">新增内容：</span>
                  <span class="change-value new-value">{{ formatPretty(log.newValue, log.entityName) }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </div>

      <div class="modal-footer">
        <button @click="$emit('close')" class="btn-close">关闭</button>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { auditLogApi } from '../services/api'

interface AuditLog {
  id: number
  entityId: number
  entityName: string
  operation: string
  operationTime: string
  operator: string
  operatorType: string
  description?: string
  oldValue?: string
  newValue?: string
  ipAddress?: string
}

const props = defineProps<{
  show: boolean
  entityName: string
  entityId: number
}>()

defineEmits(['close'])

const logs = ref<AuditLog[]>([])
const loading = ref(false)
const searchText = ref('')

const filteredLogs = computed(() => {
  if (!searchText.value.trim()) return logs.value
  
  const keyword = searchText.value.toLowerCase()
  return logs.value.filter(log => 
    log.operator?.toLowerCase().includes(keyword) ||
    log.operation?.toLowerCase().includes(keyword) ||
    log.description?.toLowerCase().includes(keyword)
  )
})

watch(() => props.show, async (newVal) => {
  if (newVal) {
    await loadLogs()
  }
})

async function loadLogs() {
  loading.value = true
  try {
    let res
    // 如果 entityId 为 0，则查询该类型的所有日志
    if (props.entityId === 0) {
      res = await auditLogApi.getEntityTypeLogs(props.entityName)
    } else {
      res = await auditLogApi.getEntityLogs(props.entityName, props.entityId)
    }
    
    if (res.code === 200) {
      logs.value = res.data || []
    }
  } catch (err: any) {
    console.error('加载日志失败:', err)
  } finally {
    loading.value = false
  }
}

function getOperationText(operation: string): string {
  const map: Record<string, string> = {
    CREATE: '创建',
    UPDATE: '更新',
    DELETE: '删除',
    SUBMIT: '提交',
    APPROVE: '审核通过',
    REJECT: '审核拒绝',
    WITHDRAW: '撤回',
    STOCK_SIMULATED: '模拟入库',
    PROCESS: '处理',
    RATE: '评分',
    PROMOTE: '升职',
    RESIGN: '离职',
    DEMOTE: '降职'
  }
  return map[operation] || operation
}

function getOperatorTypeText(type: string): string {
  const map: Record<string, string> = {
    admin: '管理员',
    salesman: '销售员',
    customer: '客户',
    system: '系统'
  }
  return map[type] || type
}

function formatDateTime(datetime?: string): string {
  if (!datetime) return '-'
  return new Date(datetime).toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

function formatValue(value: string): string {
  if (!value) return '-'
  return formatPretty(value, '')
}

type Primitive = string | number | boolean | null | undefined

function formatPrimitive(v: any): string {
  if (v === null || v === undefined) return '-'
  if (Array.isArray(v) || typeof v === 'object') return JSON.stringify(v)
  return String(v)
}

function parseMaybeJson(value?: string): any {
  if (!value) return undefined
  try {
    return JSON.parse(value)
  } catch {
    return value
  }
}

function isObjectLike(v: any) {
  return v && typeof v === 'object' && !Array.isArray(v)
}

const fieldLabelMap: Record<string, Record<string, string>> = {
  Product: {
    name: '产品名称',
    category: '分类',
    price: '价格',
    status: '状态',
    stock: '库存',
    description: '描述'
  },
  Salesman: {
    name: '姓名',
    phone: '电话',
    email: '邮箱',
    status: '状态',
    remark: '备注',
    level: '级别'
  },
  Customer: {
    name: '客户名称',
    contact: '联系人',
    phone: '电话',
    level: '等级',
    status: '状态'
  },
  SalesRecord: {
    status: '状态',
    totalAmount: '成交额',
    commission: '提成',
    saleDate: '成交时间'
  },
  ServiceRecord: {
    status: '状态',
    score: '满意度',
    remark: '备注'
  }
}

const valueLabelMap: Record<string, Record<string, Record<string, string>>> = {
  Product: {
    status: {
      on_sale: '在售',
      discontinued: '停售'
    }
  },
  Salesman: {
    status: {
      active: '在职',
      resigned: '离职'
    },
    level: {
      intern: '实习',
      junior: '初级',
      intermediate: '中级',
      middle: '中级',
      senior: '高级',
      expert: '资深',
      lead: '负责人'
    }
  },
  SalesRecord: {
    status: {
      draft: '草稿',
      pending: '待审核',
      approved: '已确认',
      rejected: '已拒绝',
      withdrawn: '已撤回'
    }
  },
  ServiceRecord: {
    status: {
      pending: '待处理',
      processing: '处理中',
      completed: '已完成'
    }
  }
}

function translateValue(entity: string, key: string, value: any): any {
  if (typeof value !== 'string') return value
  const lower = value.toLowerCase()
  const map = valueLabelMap[entity]?.[key]
  if (map) {
    if (map[value] !== undefined) return map[value]
    if (map[lower] !== undefined) return map[lower]
  }
  return value
}

function translatePrimitive(entity: string, value: any): { label: string; value: any } {
  if (typeof value !== 'string') return { label: '值', value }
  const byLevel = translateValue(entity, 'level', value)
  if (byLevel !== value) return { label: labelFor(entity, 'level'), value: byLevel }
  const byStatus = translateValue(entity, 'status', value)
  if (byStatus !== value) return { label: labelFor(entity, 'status'), value: byStatus }
  return { label: '值', value }
}

function labelFor(entity: string, key: string) {
  const map = fieldLabelMap[entity]
  return (map && map[key]) || key
}

function diffFields(log: AuditLog): { key: string; label: string; old: Primitive; new: Primitive }[] {
  const oldParsed = parseMaybeJson(log.oldValue)
  const newParsed = parseMaybeJson(log.newValue)

  // 如果是纯值（字符串/数字），尝试按级别/状态翻译
  if (!isObjectLike(oldParsed) || !isObjectLike(newParsed)) {
    if (log.oldValue !== undefined && log.newValue !== undefined) {
      const tOld = translatePrimitive(log.entityName, log.oldValue)
      const tNew = translatePrimitive(log.entityName, log.newValue)
      return [{ key: 'value', label: tOld.label === '值' ? tNew.label : tOld.label, old: tOld.value, new: tNew.value }]
    }
    return []
  }

  const keys = Array.from(new Set([...Object.keys(oldParsed || {}), ...Object.keys(newParsed || {})]))
  const diffs: { key: string; label: string; old: Primitive; new: Primitive }[] = []

  keys.forEach(k => {
    const oldVal = translateValue(log.entityName, k, (oldParsed as any)?.[k])
    const newVal = translateValue(log.entityName, k, (newParsed as any)?.[k])
    if (JSON.stringify(oldVal) !== JSON.stringify(newVal)) {
      diffs.push({ key: k, label: labelFor(log.entityName, k), old: oldVal, new: newVal })
    }
  })

  return diffs
}

function formatPretty(value?: string, entity?: string): string {
  if (!value) return '-'
  const parsed = parseMaybeJson(value)
  if (parsed === null || parsed === undefined) return '-'
  if (Array.isArray(parsed)) {
    return parsed.map(item => typeof item === 'object' ? JSON.stringify(item) : String(item)).join('；')
  }
  if (isObjectLike(parsed)) {
    const entries = Object.entries(parsed as Record<string, any>)
    if (!entries.length) return '-'
    return entries.map(([k, v]) => `${labelFor(entity || '', k)}：${formatSimpleValue(entity || '', k, v)}`).join('；')
  }
  return String(parsed)
}

function formatSimpleValue(entity: string, key: string, v: any): string {
  const translated = translateValue(entity, key, v)
  if (translated === null || translated === undefined) return '-'
  if (typeof translated === 'object') return JSON.stringify(translated)
  return String(translated)
}
</script>

<style scoped>
.logs-modal {
  max-width: 800px;
  max-height: 90vh;
  display: flex;
  flex-direction: column;
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding-bottom: 16px;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 16px;
}

.modal-header h3 {
  margin: 0;
  font-size: 20px;
  color: #111827;
}

.close-btn {
  background: none;
  border: none;
  font-size: 28px;
  color: #9ca3af;
  cursor: pointer;
  padding: 0;
  width: 32px;
  height: 32px;
  line-height: 1;
  transition: color 0.2s;
}

.close-btn:hover {
  color: #374151;
}

.logs-filter {
  margin-bottom: 16px;
}

.search-input {
  width: 100%;
  padding: 10px 14px;
  border: 1px solid #d1d5db;
  border-radius: 6px;
  font-size: 14px;
}

.logs-container {
  flex: 1;
  overflow-y: auto;
  min-height: 300px;
  max-height: 60vh;
}

.loading, .empty {
  text-align: center;
  padding: 40px;
  color: #9ca3af;
  font-size: 14px;
}

.logs-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.log-item {
  background: #f9fafb;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px 16px;
  transition: all 0.2s;
}

.log-item:hover {
  background: #f3f4f6;
  border-color: #d1d5db;
}

.log-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.log-operation {
  display: inline-block;
  padding: 4px 12px;
  border-radius: 6px;
  font-size: 13px;
  font-weight: 700;
  color: #0f172a;
  background: #e2e8f0;
  letter-spacing: 0.3px;
}

.op-create {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #f8fffb;
}

.op-update {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #f8fbff;
}

.op-delete {
  background: linear-gradient(135deg, #ef4444 0%, #dc2626 100%);
  color: #fff8f8;
}

.op-submit {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: #f8f5ff;
}

.op-approve {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: #f8fffb;
}

.op-reject {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: #fffaf2;
}

.op-withdraw {
  background: linear-gradient(135deg, #6b7280 0%, #4b5563 100%);
  color: #f8fafc;
}

.op-stock_simulated {
  background: linear-gradient(135deg, #06b6d4 0%, #0891b2 100%);
  color: #f8fdff;
}

.op-process {
  background: linear-gradient(135deg, #3b82f6 0%, #2563eb 100%);
  color: #f8fbff;
}

.op-rate {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: #fffaf2;
}

.op-promote {
  background: linear-gradient(135deg, #22c55e 0%, #16a34a 100%);
  color: #f0fff4;
}

.op-resign {
  background: linear-gradient(135deg, #f87171 0%, #dc2626 100%);
  color: #fff5f5;
}

.op-demote {
  background: linear-gradient(135deg, #94a3b8 0%, #6b7280 100%);
  color: #f8fafc;
}

.log-time {
  font-size: 13px;
  color: #6b7280;
}

.log-body {
  font-size: 14px;
}

.log-row {
  margin-bottom: 6px;
}

.log-label {
  color: #6b7280;
  font-weight: 500;
}

.log-value {
  color: #374151;
}

.log-changes {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-top: 8px;
  padding: 8px;
  background: white;
  border-radius: 4px;
}

.change-item {
  flex: 1;
}

.change-label {
  display: block;
  font-size: 12px;
  color: #6b7280;
  margin-bottom: 4px;
}

.change-value {
  display: block;
  padding: 6px;
  border-radius: 4px;
  font-size: 13px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
}

.old-value {
  background: #fef2f2;
  color: #991b1b;
  border: 1px solid #fecaca;
}

.new-value {
  background: #f0fdf4;
  color: #166534;
  border: 1px solid #bbf7d0;
}

.diff-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: 8px;
  padding: 8px;
  background: white;
  border-radius: 6px;
  border: 1px solid #e5e7eb;
}

.diff-row {
  display: flex;
  align-items: center;
  gap: 12px;
}

.diff-label {
  min-width: 88px;
  color: #6b7280;
  font-size: 13px;
  font-weight: 600;
}

.diff-values {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.pill {
  padding: 6px 10px;
  border-radius: 6px;
  font-size: 13px;
  font-family: monospace;
  white-space: pre-wrap;
  word-break: break-all;
  border: 1px solid transparent;
}

.old-pill {
  background: #fef2f2;
  color: #991b1b;
  border-color: #fecaca;
}

.new-pill {
  background: #f0fdf4;
  color: #166534;
  border-color: #bbf7d0;
}

.diff-arrow {
  color: #9ca3af;
  font-weight: 700;
}

.change-arrow {
  color: #9ca3af;
  font-size: 18px;
  font-weight: bold;
}

.modal-footer {
  padding-top: 16px;
  border-top: 1px solid #e5e7eb;
  margin-top: 16px;
  text-align: center;
}

.btn-close {
  padding: 10px 24px;
  background: #6b7280;
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.btn-close:hover {
  background: #4b5563;
}
</style>
