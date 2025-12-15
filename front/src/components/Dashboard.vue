<template>
  <div class="dashboard">
    <div class="fx-bg" aria-hidden="true">
      <div class="fx-orb fx-orb--a"></div>
      <div class="fx-orb fx-orb--b"></div>
      <div class="fx-orb fx-orb--c"></div>
      <div class="fx-grid"></div>
    </div>

    <div class="dashboard-inner">
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来，{{ userName }}！</h1>
        <p class="welcome-subtitle">{{ currentDate }}，{{ isAdmin ? '管理全局数据' : '专注个人业绩' }}</p>
      </div>
      <div class="welcome-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round" />
        </svg>
      </div>
    </div>

    <div v-if="errorMessage" class="error-banner">{{ errorMessage }}</div>

    <div v-if="loading" class="skeleton">
      <div class="skeleton-banner"></div>
      <div class="stats-grid">
        <div class="stat-card skeleton-card" v-for="i in 4" :key="i">
          <div class="skeleton-icon"></div>
          <div class="skeleton-lines">
            <div class="skeleton-line w-40"></div>
            <div class="skeleton-line w-70"></div>
            <div class="skeleton-line w-55"></div>
          </div>
        </div>
      </div>
      <div class="content-grid">
        <div class="panel"><div class="skeleton-chart"></div></div>
        <div class="panel"><div class="skeleton-chart"></div></div>
      </div>
    </div>

    <div v-else>
      <!-- 管理员驾驶舱 -->
      <div v-if="isAdmin" class="admin-view">
        <div class="stats-grid">
          <div class="stat-card card-blue">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" />
                <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">总销售额（已确认）</div>
              <div class="card-value">¥ {{ formatMoney(displayAdmin.totalSales) }}</div>
              <div class="card-trend up"><span>来源：绩效统计</span></div>
            </div>
          </div>

          <div class="stat-card card-green">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">总提成（已确认）</div>
              <div class="card-value">¥ {{ formatMoney(displayAdmin.totalCommission) }}</div>
              <div class="card-trend up"><span>来源：绩效统计</span></div>
            </div>
          </div>

          <div class="stat-card card-purple">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" />
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">活跃销售员</div>
              <div class="card-value">{{ displayAdmin.activeSalesmen }}</div>
              <div class="card-trend up"><span>近三个月有成交订单</span></div>
            </div>
          </div>

          <div class="stat-card card-orange">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 22C17.5228 22 22 17.5228 22 12C22 6.47715 17.5228 2 12 2C6.47715 2 2 6.47715 2 12C2 17.5228 6.47715 22 12 22Z" stroke="currentColor" stroke-width="2" />
                <path d="M12 8V12" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                <path d="M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">投诉数量</div>
              <div class="card-value">{{ displayAdmin.complaintCount }}</div>
              <div class="card-trend up"><span>来源：绩效统计/投诉统计</span></div>
            </div>
          </div>
        </div>

        <div class="content-grid">
          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">近 6 个月成交额趋势</h3>
              <span class="badge">APPROVED</span>
            </div>
            <div class="panel-body chart-box">
              <Line v-if="trendLineData" :data="trendLineData" :options="lineOptions" />
              <div v-else class="empty-state"><p>暂无数据</p></div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">销售状态分布</h3>
              <span class="badge">ALL</span>
            </div>
            <div class="panel-body chart-box">
              <Doughnut v-if="salesStatusDoughnutData" :data="salesStatusDoughnutData" :options="doughnutOptions" />
              <div v-else class="empty-state"><p>暂无数据</p></div>
            </div>
          </div>
        </div>

        <div class="content-grid">
          <div class="panel ranking-panel">
            <div class="panel-header">
              <h3 class="panel-title">销售员排行榜</h3>
              <span class="badge">TOP</span>
            </div>
            <div class="panel-body">
              <div class="chart-box chart-box--short">
                <Bar v-if="topSalesmenBarData" :data="topSalesmenBarData" :options="barOptions" />
              </div>
              <div class="ranking-list">
                <div class="ranking-item" v-for="(item, index) in topSalesmenList" :key="item.salesmanId">
                  <div class="rank-number" :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">{{ index + 1 }}</div>
                  <div class="rank-info">
                    <div class="rank-name">{{ item.salesmanName }}</div>
                    <div class="rank-level">{{ formatMoney(item.totalSales) }}</div>
                  </div>
                  <div class="rank-amount">¥{{ formatMoney(item.totalCommission) }}</div>
                </div>
              </div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">投诉处理概览</h3>
              <span class="badge">{{ complaintStats.resolveRate }}%</span>
            </div>
            <div class="panel-body">
              <div class="chart-box chart-box--short">
                <Doughnut v-if="complaintDoughnutData" :data="complaintDoughnutData" :options="doughnutOptions" />
              </div>
              <div class="sales-list">
                <div class="sales-item">
                  <div class="sales-info"><div class="sales-customer">待处理</div></div>
                  <div class="sales-amount">{{ complaintStats.pendingCount }}</div>
                </div>
                <div class="sales-item">
                  <div class="sales-info"><div class="sales-customer">处理中</div></div>
                  <div class="sales-amount">{{ complaintStats.processingCount }}</div>
                </div>
                <div class="sales-item">
                  <div class="sales-info"><div class="sales-customer">已解决</div></div>
                  <div class="sales-amount">{{ complaintStats.resolvedCount }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="panel recent-sales">
          <div class="panel-header">
            <h3 class="panel-title">近期销售记录</h3>
            <span class="badge">LATEST 8</span>
          </div>
          <div class="panel-body">
            <div class="sales-list">
              <div class="sales-item" v-for="sale in recentSales" :key="sale.id">
                <div class="sales-info">
                  <div class="sales-customer">{{ sale.customer }}</div>
                  <div class="sales-salesman">{{ sale.salesman }} · {{ sale.date }}</div>
                </div>
                <div class="sales-amount">¥ {{ formatMoney(sale.amount) }}</div>
              </div>
              <div v-if="recentSales.length === 0" class="empty-state"><p>暂无销售数据</p></div>
            </div>
          </div>
        </div>
      </div>

      <!-- 销售员驾驶舱 -->
      <div v-else class="salesman-view">
        <div class="stats-grid">
          <div class="stat-card card-blue">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
                <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">近 30 天销售额（已确认）</div>
              <div class="card-value">¥ {{ formatMoney(displayMe.last30DaysSales) }}</div>
              <div class="card-trend up"><span>销售单：{{ displayMe.last30DaysCount }} 笔</span></div>
            </div>
          </div>

          <div class="stat-card card-green">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">累计提成（已确认）</div>
              <div class="card-value">¥ {{ formatMoney(displayMe.totalCommission) }}</div>
              <div class="card-trend up"><span>累计销售额：¥ {{ formatMoney(displayMe.totalSales) }}</span></div>
            </div>
          </div>

          <div class="stat-card card-purple">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">平均满意度</div>
              <div class="card-value">{{ displayMe.averageSatisfaction.toFixed(1) }}</div>
              <div class="card-trend up"><span>服务评分均值</span></div>
            </div>
          </div>

          <div class="stat-card card-orange">
            <div class="card-icon">
              <svg viewBox="0 0 24 24" fill="none">
                <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2" />
                <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2" />
              </svg>
            </div>
            <div class="card-content">
              <div class="card-label">我的客户</div>
              <div class="card-value">{{ displayMe.customerCount }}</div>
              <div class="card-trend up"><span>本月新增：{{ displayMe.newCustomersThisMonth }}</span></div>
            </div>
          </div>
        </div>

        <div class="content-grid">
          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">近 6 个月成交额趋势</h3>
              <span class="badge">APPROVED</span>
            </div>
            <div class="panel-body chart-box">
              <Line v-if="trendLineData" :data="trendLineData" :options="lineOptions" />
              <div v-else class="empty-state"><p>暂无数据</p></div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">投诉处理概览</h3>
              <span class="badge">{{ complaintStats.resolveRate }}%</span>
            </div>
            <div class="panel-body">
              <div class="chart-box chart-box--short">
                <Doughnut v-if="complaintDoughnutData" :data="complaintDoughnutData" :options="doughnutOptions" />
              </div>
              <div class="sales-list">
                <div class="sales-item">
                  <div class="sales-info"><div class="sales-customer">待处理</div></div>
                  <div class="sales-amount">{{ complaintStats.pendingCount }}</div>
                </div>
                <div class="sales-item">
                  <div class="sales-info"><div class="sales-customer">处理中</div></div>
                  <div class="sales-amount">{{ complaintStats.processingCount }}</div>
                </div>
                <div class="sales-item">
                  <div class="sales-info"><div class="sales-customer">已解决</div></div>
                  <div class="sales-amount">{{ complaintStats.resolvedCount }}</div>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div class="content-grid">
          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">我的客户（前 8）</h3>
              <span class="badge">TOP</span>
            </div>
            <div class="panel-body">
              <div class="ranking-list">
                <div class="ranking-item" v-for="(c, idx) in myTopCustomers" :key="c.id">
                  <div class="rank-number" :class="{ gold: idx === 0, silver: idx === 1, bronze: idx === 2 }">{{ idx + 1 }}</div>
                  <div class="rank-info">
                    <div class="rank-name">{{ c.name }}</div>
                    <div class="rank-level">成交额：¥ {{ formatMoney(c.sales) }}</div>
                  </div>
                  <div class="rank-amount">{{ c.count }} 单</div>
                </div>
              </div>
              <div v-if="myTopCustomers.length === 0" class="empty-state"><p>暂无客户成交数据</p></div>
            </div>
          </div>

          <div class="panel">
            <div class="panel-header">
              <h3 class="panel-title">我的待办（基于状态）</h3>
              <span class="badge">{{ myTodos.length }}</span>
            </div>
            <div class="panel-body">
              <div class="todo-list">
                <div class="todo-item" v-for="(todo, index) in myTodos" :key="index">
                  <div class="todo-icon" :class="todo.type">
                    <svg viewBox="0 0 24 24" fill="none">
                      <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2" />
                      <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round" />
                    </svg>
                  </div>
                  <div class="todo-content">
                    <div class="todo-title">{{ todo.title }}</div>
                    <div class="todo-time">{{ todo.time }}</div>
                  </div>
                </div>
                <div v-if="myTodos.length === 0" class="empty-state"><p>暂无待办</p></div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
  </div>
</template>

<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { Bar, Doughnut, Line } from 'vue-chartjs'
import {
  ArcElement,
  BarElement,
  CategoryScale,
  Chart as ChartJS,
  Filler,
  Legend,
  LinearScale,
  LineElement,
  PointElement,
  Tooltip
} from 'chart.js'
import { complaintRecordApi, customerApi, performanceApi, salesRecordApi } from '../services/api'

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, BarElement, ArcElement, Tooltip, Legend, Filler)

type SalesRecord = {
  id: number
  saleDate?: string
  totalAmount?: number
  status?: 'draft' | 'pending' | 'approved' | 'rejected' | 'withdrawn'
  customer?: { id: number; name: string }
  salesman?: { id: number; name: string }
  customerId?: number
  salesmanId?: number
}

type PerformanceStats = {
  salesmanId: number
  salesmanName: string
  totalSales: number
  totalCommission: number
  averageSatisfaction: number
  complaintCount: number
  salesCount: number
}

const loading = ref(true)
const errorMessage = ref('')

const userRole = ref(localStorage.getItem('ms_role') || 'salesman')
const userName = ref(localStorage.getItem('ms_name') || '用户')
const salesmanId = computed<number | null>(() => {
  const raw = localStorage.getItem('ms_salesmanId')
  if (!raw) return null
  const n = Number(raw)
  return Number.isFinite(n) ? n : null
})

const isAdmin = computed(() => userRole.value === 'admin')

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

function formatMoney(num: number) {
  const safe = Number.isFinite(num) ? num : 0
  return safe.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function monthKey(date: Date) {
  const y = date.getFullYear()
  const m = String(date.getMonth() + 1).padStart(2, '0')
  return `${y}-${m}`
}

function lastNMonths(n: number) {
  const now = new Date()
  const months: string[] = []
  for (let i = n - 1; i >= 0; i--) {
    const d = new Date(now.getFullYear(), now.getMonth() - i, 1)
    months.push(monthKey(d))
  }
  return months
}

const adminKpi = ref({
  totalSales: 0,
  totalCommission: 0,
  activeSalesmen: 0,
  complaintCount: 0
})

const displayAdmin = ref({
  totalSales: 0,
  totalCommission: 0,
  activeSalesmen: 0,
  complaintCount: 0
})

const myKpi = ref({
  totalSales: 0,
  totalCommission: 0,
  averageSatisfaction: 0,
  customerCount: 0,
  newCustomersThisMonth: 0,
  last30DaysSales: 0,
  last30DaysCount: 0
})

const displayMe = ref({
  totalSales: 0,
  totalCommission: 0,
  averageSatisfaction: 0,
  customerCount: 0,
  newCustomersThisMonth: 0,
  last30DaysSales: 0,
  last30DaysCount: 0
})

function animateTo(target: Record<string, number>, setter: (v: Record<string, number>) => void, durationMs = 900) {
  const start = performance.now()
  const from = { ...target }
  const keys = Object.keys(target)
  const begin: Record<string, number> = {}
  for (const k of keys) begin[k] = (from[k] ?? 0)

  const end: Record<string, number> = {}
  for (const k of keys) end[k] = Number(target[k] ?? 0)

  function easeOutCubic(t: number) {
    return 1 - Math.pow(1 - t, 3)
  }

  function step(now: number) {
    const p = Math.min(1, Math.max(0, (now - start) / durationMs))
    const e = easeOutCubic(p)
    const next: Record<string, number> = {}
    for (const k of keys) {
      const a = Number(begin[k] ?? 0)
      const b = Number(end[k] ?? 0)
      next[k] = a + (b - a) * e
    }
    setter(next)
    if (p < 1) requestAnimationFrame(step)
  }

  requestAnimationFrame(step)
}

const complaintStats = ref({
  pendingCount: 0,
  processingCount: 0,
  resolvedCount: 0,
  resolveRate: '0.0'
})

const salesRecords = ref<SalesRecord[]>([])
const performanceList = ref<PerformanceStats[]>([])
const recentSales = ref<{ id: number; customer: string; salesman: string; amount: number; date: string }[]>([])

const topSalesmenList = computed(() => {
  return [...performanceList.value]
    .sort((a, b) => (b.totalSales || 0) - (a.totalSales || 0))
    .slice(0, 10)
})

const topSalesmenBarData = computed<any>(() => {
  if (!topSalesmenList.value.length) return null
  const top = topSalesmenList.value.slice(0, 8)
  return {
    labels: top.map(i => i.salesmanName),
    datasets: [
      {
        label: '成交额',
        data: top.map(i => Number(i.totalSales || 0)),
        backgroundColor: 'rgba(102, 126, 234, 0.8)',
        borderRadius: 6
      }
    ]
  }
})

const trendLineData = computed<any>(() => {
  const months = lastNMonths(6)
  if (!salesRecords.value.length) return null

  const byMonth: Record<string, number> = {}
  for (const k of months) byMonth[k] = 0

  for (const r of salesRecords.value) {
    if (r.status !== 'approved') continue
    if (!r.saleDate) continue
    const d = new Date(r.saleDate)
    if (Number.isNaN(d.getTime())) continue
    const k = monthKey(d)
    if (Object.prototype.hasOwnProperty.call(byMonth, k)) {
      byMonth[k] = (byMonth[k] ?? 0) + Number(r.totalAmount || 0)
    }
  }

  return {
    labels: months,
    datasets: [
      {
        label: '成交额（¥）',
        data: months.map(m => byMonth[m] || 0),
        borderColor: 'rgba(102, 126, 234, 1)',
        backgroundColor: 'rgba(102, 126, 234, 0.12)',
        fill: true,
        tension: 0.35,
        pointRadius: 3
      }
    ]
  }
})

const salesStatusDoughnutData = computed<any>(() => {
  if (!salesRecords.value.length) return null
  const counts: Record<string, number> = { draft: 0, pending: 0, approved: 0, rejected: 0, withdrawn: 0 }
  for (const r of salesRecords.value) {
    const s = r.status || 'draft'
    if (counts[s] === undefined) counts[s] = 0
    counts[s] += 1
  }
  const labels = ['草稿', '待审核', '已确认', '已拒绝', '已撤回']
  const data: number[] = [
    Number(counts.draft ?? 0),
    Number(counts.pending ?? 0),
    Number(counts.approved ?? 0),
    Number(counts.rejected ?? 0),
    Number(counts.withdrawn ?? 0)
  ]
  return {
    labels,
    datasets: [
      {
        data,
        backgroundColor: [
          'rgba(156, 163, 175, 0.9)',
          'rgba(245, 158, 11, 0.9)',
          'rgba(16, 185, 129, 0.9)',
          'rgba(239, 68, 68, 0.9)',
          'rgba(59, 130, 246, 0.9)'
        ],
        borderWidth: 0
      }
    ]
  }
})

const complaintDoughnutData = computed<any>(() => {
  const total = complaintStats.value.pendingCount + complaintStats.value.processingCount + complaintStats.value.resolvedCount
  if (!total) return null
  return {
    labels: ['待处理', '处理中', '已解决'],
    datasets: [
      {
        data: [complaintStats.value.pendingCount, complaintStats.value.processingCount, complaintStats.value.resolvedCount],
        backgroundColor: ['rgba(239, 68, 68, 0.85)', 'rgba(245, 158, 11, 0.85)', 'rgba(16, 185, 129, 0.85)'],
        borderWidth: 0
      }
    ]
  }
})

const myTopCustomers = ref<{ id: number; name: string; sales: number; count: number }[]>([])
const myTodos = ref<{ title: string; time: string; type: 'urgent' | 'normal' | 'info' }[]>([])

const lineOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: any) => `¥ ${formatMoney(Number(ctx.raw || 0))}`
      }
    }
  },
  scales: {
    y: {
      ticks: {
        callback: (v: any) => `¥${Number(v).toLocaleString('zh-CN')}`
      }
    }
  }
}

const barOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { display: false },
    tooltip: {
      callbacks: {
        label: (ctx: any) => `¥ ${formatMoney(Number(ctx.raw || 0))}`
      }
    }
  },
  scales: {
    y: {
      ticks: {
        callback: (v: any) => `¥${Number(v).toLocaleString('zh-CN')}`
      }
    }
  }
}

const doughnutOptions = {
  responsive: true,
  maintainAspectRatio: false,
  plugins: {
    legend: { position: 'bottom' as const }
  }
}

async function loadAdmin() {
  const [perfRes, activeRes, salesRes, complaintRes] = await Promise.all([
    performanceApi.getAllPerformance(),
    performanceApi.getActiveSalesmenCount(),
    salesRecordApi.getAll('admin'),
    complaintRecordApi.getStatistics()
  ])

  if (perfRes.code === 200 && Array.isArray(perfRes.data)) {
    performanceList.value = perfRes.data.map((x: any) => ({
      salesmanId: Number(x.salesmanId),
      salesmanName: String(x.salesmanName || ''),
      totalSales: Number(x.totalSales || 0),
      totalCommission: Number(x.totalCommission || 0),
      averageSatisfaction: Number(x.averageSatisfaction || 0),
      complaintCount: Number(x.complaintCount || 0),
      salesCount: Number(x.salesCount || 0)
    }))

    adminKpi.value.totalSales = performanceList.value.reduce((s, i) => s + (i.totalSales || 0), 0)
    adminKpi.value.totalCommission = performanceList.value.reduce((s, i) => s + (i.totalCommission || 0), 0)
    adminKpi.value.complaintCount = performanceList.value.reduce((s, i) => s + (i.complaintCount || 0), 0)
  }

  if (activeRes.code === 200) adminKpi.value.activeSalesmen = Number(activeRes.data || 0)

  if (salesRes.code === 200 && Array.isArray(salesRes.data)) {
    salesRecords.value = salesRes.data as any
    recentSales.value = [...salesRecords.value]
      .filter(r => !!r.saleDate)
      .sort((a, b) => (new Date(b.saleDate || 0).getTime() - new Date(a.saleDate || 0).getTime()))
      .slice(0, 8)
      .map(r => ({
        id: r.id,
        customer: r.customer?.name || '未知客户',
        salesman: r.salesman?.name || '未知销售员',
        amount: Number(r.totalAmount || 0),
        date: String(r.saleDate || '')
      }))
  }

  if (complaintRes.code === 200 && complaintRes.data) {
    complaintStats.value = {
      pendingCount: Number((complaintRes.data as any).pendingCount || 0),
      processingCount: Number((complaintRes.data as any).processingCount || 0),
      resolvedCount: Number((complaintRes.data as any).resolvedCount || 0),
      resolveRate: String((complaintRes.data as any).resolveRate || '0.0')
    }
  }

  // KPI 数字滚动
  animateTo(
    {
      totalSales: adminKpi.value.totalSales,
      totalCommission: adminKpi.value.totalCommission,
      activeSalesmen: adminKpi.value.activeSalesmen,
      complaintCount: adminKpi.value.complaintCount
    },
    v => {
      displayAdmin.value = {
        totalSales: Number(v.totalSales || 0),
        totalCommission: Number(v.totalCommission || 0),
        activeSalesmen: Math.round(Number(v.activeSalesmen || 0)),
        complaintCount: Math.round(Number(v.complaintCount || 0))
      }
    }
  )
}

async function loadSalesman() {
  if (!salesmanId.value) throw new Error('无法识别当前销售员身份（ms_salesmanId 为空）')

  const [perfRes, salesRes, customersRes, complaintRes] = await Promise.all([
    performanceApi.getSalesmanPerformance(salesmanId.value),
    salesRecordApi.getBySalesman(salesmanId.value),
    customerApi.getAll('salesman', salesmanId.value),
    complaintRecordApi.getStatistics(salesmanId.value)
  ])

  if (perfRes.code === 200 && perfRes.data) {
    myKpi.value.totalSales = Number((perfRes.data as any).totalSales || 0)
    myKpi.value.totalCommission = Number((perfRes.data as any).totalCommission || 0)
    myKpi.value.averageSatisfaction = Number((perfRes.data as any).averageSatisfaction || 0)
  }

  if (salesRes.code === 200 && Array.isArray(salesRes.data)) {
    salesRecords.value = salesRes.data as any

    const now = new Date()
    const start = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000)

    const last30Approved = salesRecords.value.filter(r => {
      if (r.status !== 'approved') return false
      if (!r.saleDate) return false
      const d = new Date(r.saleDate)
      return d >= start && d <= now
    })

    myKpi.value.last30DaysSales = last30Approved.reduce((s, r) => s + Number(r.totalAmount || 0), 0)
    myKpi.value.last30DaysCount = last30Approved.length

    const draftCount = salesRecords.value.filter(r => r.status === 'draft').length
    const pendingCount = salesRecords.value.filter(r => r.status === 'pending').length
    const rejectedCount = salesRecords.value.filter(r => r.status === 'rejected').length
    myTodos.value = [
      ...(draftCount ? [{ title: `有 ${draftCount} 条草稿待提交`, time: '销售记录', type: 'normal' as const }] : []),
      ...(pendingCount ? [{ title: `有 ${pendingCount} 条待审核，耐心等待`, time: '销售记录', type: 'info' as const }] : []),
      ...(rejectedCount ? [{ title: `有 ${rejectedCount} 条被拒绝需修改`, time: '销售记录', type: 'urgent' as const }] : [])
    ]
  }

  if (customersRes.code === 200 && Array.isArray(customersRes.data)) {
    const list = customersRes.data as any[]
    myKpi.value.customerCount = list.length

    const now = new Date()
    const currentMonth = now.getMonth()
    const currentYear = now.getFullYear()
    myKpi.value.newCustomersThisMonth = list.filter(c => {
      const d = new Date(c.createTime)
      return d.getFullYear() === currentYear && d.getMonth() === currentMonth
    }).length

    const byCustomer: Record<number, { id: number; name: string; sales: number; count: number }> = {}
    for (const r of salesRecords.value) {
      if (r.status !== 'approved') continue
      const cid = r.customer?.id || r.customerId
      if (!cid) continue
      if (!byCustomer[cid]) {
        const found = list.find(x => x.id === cid)
        byCustomer[cid] = { id: cid, name: found?.name || r.customer?.name || '未知客户', sales: 0, count: 0 }
      }
      byCustomer[cid].sales += Number(r.totalAmount || 0)
      byCustomer[cid].count += 1
    }
    myTopCustomers.value = Object.values(byCustomer)
      .sort((a, b) => b.sales - a.sales)
      .slice(0, 8)
  }

  if (complaintRes.code === 200 && complaintRes.data) {
    complaintStats.value = {
      pendingCount: Number((complaintRes.data as any).pendingCount || 0),
      processingCount: Number((complaintRes.data as any).processingCount || 0),
      resolvedCount: Number((complaintRes.data as any).resolvedCount || 0),
      resolveRate: String((complaintRes.data as any).resolveRate || '0.0')
    }
  }

  // KPI 数字滚动
  animateTo(
    {
      totalSales: myKpi.value.totalSales,
      totalCommission: myKpi.value.totalCommission,
      averageSatisfaction: myKpi.value.averageSatisfaction,
      customerCount: myKpi.value.customerCount,
      newCustomersThisMonth: myKpi.value.newCustomersThisMonth,
      last30DaysSales: myKpi.value.last30DaysSales,
      last30DaysCount: myKpi.value.last30DaysCount
    },
    v => {
      displayMe.value = {
        totalSales: Number(v.totalSales || 0),
        totalCommission: Number(v.totalCommission || 0),
        averageSatisfaction: Number(v.averageSatisfaction || 0),
        customerCount: Math.round(Number(v.customerCount || 0)),
        newCustomersThisMonth: Math.round(Number(v.newCustomersThisMonth || 0)),
        last30DaysSales: Number(v.last30DaysSales || 0),
        last30DaysCount: Math.round(Number(v.last30DaysCount || 0))
      }
    }
  )
}

onMounted(async () => {
  try {
    const storedRole = localStorage.getItem('ms_role')
    const storedName = localStorage.getItem('ms_name')
    if (storedRole) userRole.value = storedRole
    if (storedName) userName.value = storedName

    loading.value = true
    errorMessage.value = ''

    if (isAdmin.value) {
      await loadAdmin()
    } else {
      await loadSalesman()
    }
  } catch (e: any) {
    console.error(e)
    errorMessage.value = e?.message || '加载失败，请稍后重试'
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.dashboard {
  position: relative;
  width: 100%;
  min-height: calc(100vh - 64px);
  overflow: hidden;
}

.dashboard-inner {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
  position: relative;
  z-index: 1;
}

.fx-bg {
  position: absolute;
  inset: 0;
  pointer-events: none;
  z-index: 0;
  overflow: hidden;
  background:
    radial-gradient(1200px 700px at 20% 10%, rgba(102, 126, 234, 0.35), transparent 55%),
    radial-gradient(900px 600px at 85% 25%, rgba(124, 58, 237, 0.28), transparent 55%),
    radial-gradient(900px 600px at 60% 90%, rgba(16, 185, 129, 0.18), transparent 55%),
    linear-gradient(180deg, rgba(36, 36, 36, 0.15), rgba(36, 36, 36, 0.55));
}

.fx-orb {
  position: absolute;
  width: 520px;
  height: 520px;
  border-radius: 999px;
  filter: blur(34px);
  opacity: 0.65;
  animation: orbFloat 12s ease-in-out infinite;
}

.fx-orb--a {
  left: -160px;
  top: -180px;
  background: radial-gradient(circle at 30% 30%, rgba(102, 126, 234, 0.75), rgba(118, 75, 162, 0.0) 60%);
}

.fx-orb--b {
  right: -180px;
  top: 120px;
  width: 620px;
  height: 620px;
  background: radial-gradient(circle at 30% 30%, rgba(124, 58, 237, 0.65), rgba(102, 126, 234, 0.0) 62%);
  animation-delay: -3s;
}

.fx-orb--c {
  left: 25%;
  bottom: -260px;
  width: 680px;
  height: 680px;
  background: radial-gradient(circle at 30% 30%, rgba(16, 185, 129, 0.42), rgba(16, 185, 129, 0.0) 65%);
  animation-delay: -6s;
}

.fx-grid {
  position: absolute;
  inset: 0;
  opacity: 0.12;
  background-image:
    linear-gradient(to right, rgba(255, 255, 255, 0.08) 1px, transparent 1px),
    linear-gradient(to bottom, rgba(255, 255, 255, 0.08) 1px, transparent 1px);
  background-size: 64px 64px;
  mask-image: radial-gradient(closest-side, rgba(0, 0, 0, 0.9), transparent);
}

@keyframes orbFloat {
  0%, 100% {
    transform: translate3d(0, 0, 0) scale(1);
  }
  50% {
    transform: translate3d(0, 22px, 0) scale(1.06);
  }
}

.loading {
  margin: 16px 0 24px;
  color: rgba(255, 255, 255, 0.85);
}

.error-banner {
  background: rgba(239, 68, 68, 0.15);
  color: #ef4444;
  border: 1px solid rgba(239, 68, 68, 0.35);
  border-radius: 12px;
  padding: 12px 16px;
  margin-bottom: 16px;
}

/* 欢迎横幅 */
.welcome-banner {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 24px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.35);
  animation: slideInDown 0.6s ease-out;
  position: relative;
  overflow: hidden;
}

.welcome-banner::after {
  content: '';
  position: absolute;
  inset: -80px;
  background: conic-gradient(from 180deg, rgba(255,255,255,0.0), rgba(255,255,255,0.18), rgba(255,255,255,0.0));
  animation: shimmer 6s linear infinite;
  opacity: 0.9;
}

@keyframes shimmer {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes slideInDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.welcome-title {
  font-size: 28px;
  font-weight: 700;
  margin: 0 0 8px 0;
}

.welcome-subtitle {
  font-size: 16px;
  opacity: 0.9;
  margin: 0;
}

.welcome-icon svg {
  width: 64px;
  height: 64px;
  opacity: 0.3;
}

/* 统计卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(260px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  background: white;
  border-radius: 16px;
  padding: 24px;
  display: flex;
  gap: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  animation: fadeInUp 0.6s ease-out;
  animation-fill-mode: both;
  position: relative;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.6);
  backdrop-filter: blur(10px);
}

.stat-card::before {
  content: '';
  position: absolute;
  inset: 0;
  background: radial-gradient(600px 200px at 10% 0%, rgba(102, 126, 234, 0.10), transparent 60%);
  opacity: 0;
  transition: opacity 0.25s ease;
}

.stat-card:hover::before {
  opacity: 1;
}

.stat-card:nth-child(1) { animation-delay: 0.1s; }
.stat-card:nth-child(2) { animation-delay: 0.2s; }
.stat-card:nth-child(3) { animation-delay: 0.3s; }
.stat-card:nth-child(4) { animation-delay: 0.4s; }

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 30px rgba(0, 0, 0, 0.14);
}

.card-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.card-icon svg {
  width: 28px;
  height: 28px;
}

.card-blue .card-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.card-green .card-icon {
  background: linear-gradient(135deg, #10b981 0%, #059669 100%);
  color: white;
}

.card-purple .card-icon {
  background: linear-gradient(135deg, #8b5cf6 0%, #7c3aed 100%);
  color: white;
}

.card-orange .card-icon {
  background: linear-gradient(135deg, #f59e0b 0%, #d97706 100%);
  color: white;
}

.card-content {
  flex: 1;
}

.card-label {
  font-size: 14px;
  color: #6b7280;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: 700;
  color: #1f2937;
  margin-bottom: 8px;
}

.card-trend {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 13px;
  color: #10b981;
}

.card-trend svg {
  width: 16px;
  height: 16px;
}

.card-trend.up {
  color: #10b981;
}

.card-trend.down {
  color: #ef4444;
}

/* 内容网格 */
.content-grid {
  display: grid;
  grid-template-columns: 2fr 1fr;
  gap: 20px;
  margin-bottom: 24px;
}

@media (max-width: 1024px) {
  .content-grid {
    grid-template-columns: 1fr;
  }
}

.panel {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.65);
  backdrop-filter: blur(10px);
  position: relative;
  overflow: hidden;
}

.panel::before {
  content: '';
  position: absolute;
  inset: 0;
  background:
    radial-gradient(500px 200px at 20% 0%, rgba(124, 58, 237, 0.10), transparent 60%),
    radial-gradient(500px 200px at 85% 20%, rgba(16, 185, 129, 0.08), transparent 60%);
  opacity: 0.0;
  transition: opacity 0.25s ease;
}

.panel:hover::before {
  opacity: 1;
}

.panel-body {
  position: relative;
}

.chart-box {
  height: 300px;
}

.chart-box--short {
  height: 220px;
  margin-bottom: 16px;
}

/* Skeleton */
.skeleton {
  margin: 16px 0 0;
}

.skeleton-banner {
  height: 110px;
  border-radius: 16px;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.12);
  margin-bottom: 20px;
  position: relative;
  overflow: hidden;
}

.skeleton-card {
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.skeleton-icon {
  width: 56px;
  height: 56px;
  border-radius: 12px;
  background: rgba(255, 255, 255, 0.12);
}

.skeleton-lines {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding-top: 4px;
}

.skeleton-line {
  height: 12px;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.12);
}

.w-40 { width: 40%; }
.w-55 { width: 55%; }
.w-70 { width: 70%; }

.skeleton-chart {
  height: 300px;
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.10);
  border: 1px solid rgba(255, 255, 255, 0.12);
}

.skeleton-banner::after,
.skeleton-card::after,
.skeleton-chart::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(90deg, transparent, rgba(255,255,255,0.18), transparent);
  transform: translateX(-100%);
  animation: skeletonShimmer 1.2s ease-in-out infinite;
}

@keyframes skeletonShimmer {
  0% { transform: translateX(-100%); }
  100% { transform: translateX(100%); }
}

.panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.panel-title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
  margin: 0;
}

.panel-link {
  font-size: 14px;
  color: #667eea;
  text-decoration: none;
  transition: color 0.2s;
}

.panel-link:hover {
  color: #5a67d8;
}

.badge {
  background: #ef4444;
  color: white;
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

/* 销售列表 */
.sales-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.sales-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  transition: background 0.2s;
}

.sales-item:hover {
  background: #f3f4f6;
}

.sales-customer {
  font-size: 15px;
  font-weight: 500;
  color: #1f2937;
}

.sales-product {
  font-size: 13px;
  color: #6b7280;
  margin-top: 2px;
}

.sales-amount {
  font-size: 16px;
  font-weight: 600;
  color: #667eea;
}

/* 待办列表 */
.todo-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.todo-item {
  display: flex;
  gap: 12px;
  align-items: flex-start;
}

.todo-icon {
  width: 40px;
  height: 40px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.todo-icon.urgent {
  background: #fee2e2;
  color: #ef4444;
}

.todo-icon.normal {
  background: #dbeafe;
  color: #3b82f6;
}

.todo-icon svg {
  width: 20px;
  height: 20px;
}

.todo-title {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

.todo-time {
  font-size: 12px;
  color: #9ca3af;
  margin-top: 2px;
}

/* 快捷操作 */
.quick-actions {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 16px;
}

.action-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  cursor: pointer;
  transition: all 0.3s ease;
}

.action-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.action-icon {
  width: 48px;
  height: 48px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
}

.action-icon svg {
  width: 24px;
  height: 24px;
}

.action-label {
  font-size: 14px;
  font-weight: 500;
  color: #1f2937;
}

/* 排行榜样式 */
.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  transition: all 0.2s;
}

.ranking-item:hover {
  background: #f3f4f6;
  transform: translateX(4px);
}

.rank-number {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
  font-size: 14px;
  color: #6b7280;
}

.rank-number.gold {
  background: linear-gradient(135deg, #fbbf24 0%, #f59e0b 100%);
  color: white;
}

.rank-number.silver {
  background: linear-gradient(135deg, #d1d5db 0%, #9ca3af 100%);
  color: white;
}

.rank-number.bronze {
  background: linear-gradient(135deg, #fdba74 0%, #fb923c 100%);
  color: white;
}

.rank-info {
  flex: 1;
}

.rank-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.rank-level {
  font-size: 12px;
  color: #6b7280;
}

.rank-amount {
  font-weight: 700;
  color: #10b981;
  font-size: 16px;
}

/* 客户列表样式 */
.customer-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.customer-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
  transition: all 0.2s;
}

.customer-item:hover {
  background: #f3f4f6;
}

.customer-info {
  flex: 1;
}

.customer-name {
  font-weight: 600;
  color: #1f2937;
  margin-bottom: 4px;
}

.customer-contact {
  font-size: 13px;
  color: #6b7280;
}

.customer-status {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.customer-status.normal {
  background: #d1fae5;
  color: #10b981;
}

.customer-status.warning {
  background: #fef3c7;
  color: #f59e0b;
}

/* 空状态样式 */
.empty-state {
  text-align: center;
  padding: 40px 20px;
  color: #9ca3af;
}

.empty-state svg {
  width: 48px;
  height: 48px;
  margin: 0 auto 12px;
  opacity: 0.5;
}

.empty-state p {
  margin: 0;
  font-size: 14px;
}

/* 销售员专用样式 */
.salesman-view {
  animation: fadeIn 0.6s ease-out;
}

.admin-view {
  animation: fadeIn 0.6s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-trend.down {
  color: #ef4444;
}

.sales-salesman {
  font-size: 12px;
  color: #6b7280;
}
</style>

