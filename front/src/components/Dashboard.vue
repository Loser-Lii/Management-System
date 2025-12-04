<template>
  <div class="dashboard">
    <!-- 欢迎区域 -->
    <div class="welcome-banner">
      <div class="welcome-content">
        <h1 class="welcome-title">欢迎回来，{{ userName }}！</h1>
        <p class="welcome-subtitle">{{ currentDate }}，{{ isAdmin ? '管理全局数据' : '专注个人业绩' }}</p>
      </div>
      <div class="welcome-icon">
        <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
          <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
      </div>
    </div>

    <!-- 管理员视图 -->
    <div v-if="isAdmin" class="admin-view">
      <!-- 数据统计卡片 -->
      <div class="stats-grid">
        <div class="stat-card card-blue">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">总销售额</div>
            <div class="card-value">¥ {{ formatNumber(totalSales) }}</div>
            <div class="card-trend up">
              <span>全公司累计</span>
            </div>
          </div>
        </div>

        <div class="stat-card card-green">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
              <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">活跃销售员</div>
            <div class="card-value">{{ activeSalesmen }}</div>
            <div class="card-trend up">
              <span>近三个月有成交订单的销售员</span>
            </div>
          </div>
        </div>

        <div class="stat-card card-purple">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">销售记录</div>
            <div class="card-value">{{ totalRecords }}</div>
            <div class="card-trend up">
              <span>总记录数</span>
            </div>
          </div>
        </div>

        <div class="stat-card card-orange">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2"/>
              <circle cx="12" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">客户总数</div>
            <div class="card-value">{{ totalCustomers }}</div>
            <div class="card-trend up">
              <span>全部客户</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-grid">
        <!-- 销售员排行 -->
        <div class="panel ranking-panel">
          <div class="panel-header">
            <h3 class="panel-title">销售员排行榜</h3>
            <span class="badge">TOP 10</span>
          </div>
          <div class="panel-body">
            <div class="ranking-list">
              <div class="ranking-item" v-for="(item, index) in topSalesmen" :key="index">
                <div class="rank-number" :class="{ gold: index === 0, silver: index === 1, bronze: index === 2 }">
                  {{ index + 1 }}
                </div>
                <div class="rank-info">
                  <div class="rank-name">{{ item.name }}</div>
                  <div class="rank-level">{{ getLevelText(item.level) }}</div>
                </div>
                <div class="rank-amount">¥{{ formatNumber(item.sales) }}</div>
              </div>
            </div>
          </div>
        </div>

        <!-- 近期销售 -->
        <div class="panel recent-sales">
          <div class="panel-header">
            <h3 class="panel-title">近期销售记录</h3>
            <a href="#" class="panel-link">查看全部 →</a>
          </div>
          <div class="panel-body">
            <div class="sales-list">
              <div class="sales-item" v-for="(sale, index) in recentSales" :key="index">
                <div class="sales-info">
                  <div class="sales-customer">{{ sale.customer }}</div>
                  <div class="sales-salesman">{{ sale.salesman }}</div>
                </div>
                <div class="sales-amount">¥ {{ formatNumber(sale.amount) }}</div>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <!-- 销售员视图 -->
    <div v-else class="salesman-view">
      <!-- 个人业绩卡片 -->
      <div class="stats-grid">
        <div class="stat-card card-blue">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">本月销售额</div>
            <div class="card-value">¥ {{ formatNumber(mySales) }}</div>
            <div class="card-trend" :class="{ up: salesGrowth >= 0, down: salesGrowth < 0 }">
              <svg viewBox="0 0 24 24" fill="none">
                <path :d="salesGrowth >= 0 ? 'M7 17L17 7M17 7H7M17 7V17' : 'M17 7L7 17M7 17H17M7 17V7'" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              </svg>
              <span>{{ salesGrowth >= 0 ? '+' : '' }}{{ salesGrowth.toFixed(1) }}% 较上月</span>
            </div>
          </div>
        </div>

        <div class="stat-card card-green">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">预计提成</div>
            <div class="card-value">¥ {{ formatNumber(myCommission) }}</div>
            <div class="card-trend up">
              <span>按 {{ (commissionRate * 100).toFixed(1) }}% 计算</span>
            </div>
          </div>
        </div>

        <div class="stat-card card-purple">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M21 12V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V5C3 4.46957 3.21071 3.96086 3.58579 3.58579C3.96086 3.21071 4.46957 3 5 3H16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">本月销售笔数</div>
            <div class="card-value">{{ mySalesCount }}</div>
            <div class="card-trend up">
              <span>共 {{ mySalesCount }} 笔交易</span>
            </div>
          </div>
        </div>

        <div class="stat-card card-orange">
          <div class="card-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
              <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="card-content">
            <div class="card-label">我的客户</div>
            <div class="card-value">{{ myCustomers }}</div>
            <div class="card-trend up">
              <span>本月新增 {{ newCustomers }} 人</span>
            </div>
          </div>
        </div>
      </div>

      <!-- 内容区域 -->
      <div class="content-grid">
        <!-- 我的客户 -->
        <div class="panel customer-panel">
          <div class="panel-header">
            <h3 class="panel-title">我的客户</h3>
            <span class="badge">{{ myCustomers }}</span>
          </div>
          <div class="panel-body">
            <div class="customer-list">
              <div class="customer-item" v-for="(customer, index) in myCustomerList" :key="index">
                <div class="customer-info">
                  <div class="customer-name">{{ customer.name }}</div>
                  <div class="customer-contact">{{ customer.phone }}</div>
                </div>
                <div class="customer-status" :class="customer.needFollow ? 'warning' : 'normal'">
                  {{ customer.needFollow ? '需跟进' : '正常' }}
                </div>
              </div>
              <div v-if="myCustomerList.length === 0" class="empty-state">
                <svg viewBox="0 0 24 24" fill="none">
                  <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                  <path d="M12 8V12M12 16H12.01" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <p>暂无客户数据</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 待办事项 -->
        <div class="panel todo-panel">
          <div class="panel-header">
            <h3 class="panel-title">今日待办</h3>
            <span class="badge">{{ todos.length }}</span>
          </div>
          <div class="panel-body">
            <div class="todo-list">
              <div class="todo-item" v-for="(todo, index) in todos" :key="index">
                <div class="todo-icon" :class="todo.type">
                  <svg viewBox="0 0 24 24" fill="none">
                    <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
                    <path d="M12 6V12L16 14" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                  </svg>
                </div>
                <div class="todo-content">
                  <div class="todo-title">{{ todo.title }}</div>
                  <div class="todo-time">{{ todo.time }}</div>
                </div>
              </div>
              <div v-if="todos.length === 0" class="empty-state">
                <svg viewBox="0 0 24 24" fill="none">
                  <path d="M9 11L12 14L22 4" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
                </svg>
                <p>暂无待办事项</p>
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- 快捷操作 -->
      <div class="quick-actions">
        <div class="action-card">
          <div class="action-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M12 5V19M5 12H19" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="action-label">新增销售</div>
        </div>
        <div class="action-card">
          <div class="action-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M17 21V19C17 17.9391 16.5786 16.9217 15.8284 16.1716C15.0783 15.4214 14.0609 15 13 15H5C3.93913 15 2.92172 15.4214 2.17157 16.1716C1.42143 16.9217 1 17.9391 1 19V21" stroke="currentColor" stroke-width="2"/>
              <circle cx="9" cy="7" r="4" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="action-label">新增客户</div>
        </div>
        <div class="action-card">
          <div class="action-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <path d="M8 10L12 14L16 10M12 14V3" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
          </div>
          <div class="action-label">联络记录</div>
        </div>
        <div class="action-card">
          <div class="action-icon">
            <svg viewBox="0 0 24 24" fill="none">
              <rect x="3" y="3" width="18" height="18" rx="2" stroke="currentColor" stroke-width="2"/>
              <path d="M9 3V21M3 9H21M3 15H21" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="action-label">查看业绩</div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted } from 'vue'
import { salesmanApi, salesRecordApi, customerApi, performanceApi } from '../services/api'

const props = defineProps({
  salesmanId: {
    type: Number,
    default: null
  }
})

// 从localStorage获取用户角色信息
const userRole = ref(localStorage.getItem('userRole') || 'salesman')
const userName = ref(localStorage.getItem('userName') || '用户')

const isAdmin = computed(() => userRole.value === 'admin')

const currentDate = computed(() => {
  const date = new Date()
  return `${date.getFullYear()}年${date.getMonth() + 1}月${date.getDate()}日`
})

// 格式化数字
function formatNumber(num: number) {
  return num.toLocaleString('zh-CN', { minimumFractionDigits: 2, maximumFractionDigits: 2 })
}

function getLevelText(level: string) {
  const levelMap: Record<string, string> = {
    'junior': '初级',
    'intermediate': '中级',
    'senior': '高级',
    'expert': '资深'
  }
  return levelMap[level] || level
}

// ========== 管理员数据 ==========
const totalSales = ref(0)
const activeSalesmen = ref(0)
const totalRecords = ref(0)
const totalCustomers = ref(0)
const topSalesmen = ref<any[]>([])
const recentSales = ref<any[]>([])

// ========== 销售员数据 ==========
const mySales = ref(0)
const myCommission = ref(0)
const mySalesCount = ref(0)
const myCustomers = ref(0)
const newCustomers = ref(0)
const salesGrowth = ref(0)
const commissionRate = ref(0.04)
const myCustomerList = ref<any[]>([])

// ========== 通用数据 ==========
const todos = ref<any[]>([])

// 加载管理员统计数据
const loadAdminStatistics = async () => {
  try {
    // 1. 获取活跃销售员数量（近三个月内有销售记录）
    const activeSalesmenRes = await performanceApi.getActiveSalesmenCount()
    if (activeSalesmenRes.code === 200) {
      activeSalesmen.value = activeSalesmenRes.data || 0
    }

    // 2. 获取所有销售员用于排名
    const salesmenRes = await salesmanApi.getAll()
    if (salesmenRes.code === 200 && salesmenRes.data) {
      // 计算每个销售员的销售额用于排名
      const salesmenWithSales = await Promise.all(
        salesmenRes.data.map(async (salesman: any) => {
          try {
            const salesRes = await salesRecordApi.getBySalesman(salesman.id)
            const sales = salesRes.code === 200 && salesRes.data 
              ? salesRes.data.reduce((sum: number, record: any) => sum + Number(record.totalAmount || 0), 0)
              : 0
            return {
              name: salesman.name,
              level: salesman.level,
              sales: sales
            }
          } catch {
            return {
              name: salesman.name,
              level: salesman.level,
              sales: 0
            }
          }
        })
      )
      
      // 排序并取前10名
      topSalesmen.value = salesmenWithSales
        .sort((a, b) => b.sales - a.sales)
        .slice(0, 10)
      
      // 计算总销售额
      totalSales.value = salesmenWithSales.reduce((sum, s) => sum + s.sales, 0)
    }

    // 2. 获取销售记录数
    const salesRes = await salesRecordApi.getAll()
    if (salesRes.code === 200 && salesRes.data) {
      totalRecords.value = salesRes.data.length
      
      // 获取近期销售（最近5条）
      recentSales.value = salesRes.data
        .slice(0, 5)
        .map((record: any) => ({
          customer: record.customer?.name || '未知客户',
          salesman: record.salesman?.name || '未知销售员',
          amount: Number(record.totalAmount || 0)
        }))
    }

    // 3. 获取客户总数
    const customersRes = await customerApi.getAll()
    if (customersRes.code === 200 && customersRes.data) {
      totalCustomers.value = customersRes.data.length
    }

    // 4. 模拟待办事项（管理员）
    todos.value = [
      { title: '审核新增客户信息', time: '2小时前', type: 'info' },
      { title: '查看本周销售报表', time: '今天 9:00', type: 'normal' },
      { title: '处理客户投诉记录', time: '昨天 16:30', type: 'urgent' }
    ]
    
  } catch (error) {
    console.error('加载管理员统计数据失败:', error)
  }
}

// 加载销售员统计数据
const loadSalesmanStatistics = async () => {
  if (!props.salesmanId) {
    console.warn('未提供salesmanId')
    return
  }

  try {
    // 1. 获取销售员信息
    const salesmanRes = await salesmanApi.getById(props.salesmanId)
    if (salesmanRes.code === 200 && salesmanRes.data) {
      userName.value = salesmanRes.data.name
      commissionRate.value = Number(salesmanRes.data.commissionRate || 0.04)
    }

    // 2. 获取我的销售记录
    const salesRes = await salesRecordApi.getBySalesman(props.salesmanId)
    if (salesRes.code === 200 && salesRes.data) {
      // 本月销售数据
      const now = new Date()
      const currentMonth = now.getMonth()
      const currentYear = now.getFullYear()
      
      const thisMonthSales = salesRes.data.filter((record: any) => {
        const saleDate = new Date(record.saleDate)
        return saleDate.getMonth() === currentMonth && saleDate.getFullYear() === currentYear
      })
      
      mySales.value = thisMonthSales.reduce((sum: number, record: any) => 
        sum + Number(record.totalAmount || 0), 0)
      mySalesCount.value = thisMonthSales.length
      myCommission.value = mySales.value * commissionRate.value
      
      // 上月销售额（用于计算增长率）
      const lastMonth = currentMonth === 0 ? 11 : currentMonth - 1
      const lastMonthYear = currentMonth === 0 ? currentYear - 1 : currentYear
      
      const lastMonthSales = salesRes.data.filter((record: any) => {
        const saleDate = new Date(record.saleDate)
        return saleDate.getMonth() === lastMonth && saleDate.getFullYear() === lastMonthYear
      })
      
      const lastMonthTotal = lastMonthSales.reduce((sum: number, record: any) => 
        sum + Number(record.totalAmount || 0), 0)
      
      if (lastMonthTotal > 0) {
        salesGrowth.value = ((mySales.value - lastMonthTotal) / lastMonthTotal) * 100
      } else {
        salesGrowth.value = mySales.value > 0 ? 100 : 0
      }
    }

    // 3. 获取我的客户
    const customersRes = await customerApi.getAll()
    if (customersRes.code === 200 && customersRes.data) {
      const myCustomersList = customersRes.data.filter((c: any) => c.salesmanId === props.salesmanId)
      myCustomers.value = myCustomersList.length
      
      // 本月新增客户
      const now = new Date()
      const currentMonth = now.getMonth()
      const currentYear = now.getFullYear()
      
      newCustomers.value = myCustomersList.filter((c: any) => {
        const createDate = new Date(c.createTime)
        return createDate.getMonth() === currentMonth && createDate.getFullYear() === currentYear
      }).length
      
      // 显示前5个客户（可扩展为需要跟进的客户）
      myCustomerList.value = myCustomersList
        .slice(0, 5)
        .map((c: any) => ({
          name: c.name,
          phone: c.phone || '暂无电话',
          needFollow: false // 这里可以加入跟进逻辑判断
        }))
    }

    // 4. 待办事项（销售员）
    todos.value = [
      { title: '联系客户张三进行产品推荐', time: '今天 10:00', type: 'urgent' },
      { title: '跟进李四的订单进度', time: '今天 14:00', type: 'normal' },
      { title: '完成本周销售总结报告', time: '本周五', type: 'info' }
    ]
    
  } catch (error) {
    console.error('加载销售员统计数据失败:', error)
  }
}

// 挂载时加载数据
onMounted(() => {
  // 从App.vue获取用户信息
  const storedRole = localStorage.getItem('ms_role')
  const storedName = localStorage.getItem('ms_name')
  
  if (storedRole) userRole.value = storedRole
  if (storedName) userName.value = storedName
  
  if (isAdmin.value) {
    loadAdminStatistics()
  } else {
    loadSalesmanStatistics()
  }
})
</script>

<style scoped>
.dashboard {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
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
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
  animation: slideInDown 0.6s ease-out;
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
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
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

