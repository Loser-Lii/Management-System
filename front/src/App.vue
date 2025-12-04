<script setup>
import { ref, computed } from 'vue'
import Login from './components/Login.vue'
import Register from './components/Register.vue'
import Dashboard from './components/Dashboard.vue'
import Profile from './components/Profile.vue'
import SalesmanList from './components/SalesmanList.vue'
import CustomerList from './components/CustomerList.vue'
import ProductList from './components/ProductList.vue'
import SalesRecordList from './components/SalesRecordList.vue'
import ContactList from './components/ContactList.vue'
import ServiceList from './components/ServiceList.vue'
import CollectionList from './components/CollectionList.vue'
import ComplaintList from './components/ComplaintList.vue'

const view = ref('login')
const token = ref(localStorage.getItem('ms_token'))
const currentUser = ref({ id: null, name: '', role: '' })
const showProfileTip = ref(false)

const isLoggedIn = computed(() => !!token.value)
const isAdmin = computed(() => currentUser.value.role === 'admin')

// 菜单配置
const menuConfig = [
  {
    id: 'home',
    label: '首页',
    icon: 'home',
    view: 'dashboard',
    roles: ['admin', 'salesman']
  },
  {
    id: 'salesmen',
    label: '销售员管理',
    icon: 'users',
    view: 'salesmen',
    roles: ['admin']
  },
  {
    id: 'customers',
    label: computed(() => isAdmin.value ? '客户总览' : '我的客户'),
    icon: 'contacts',
    view: 'customers',
    roles: ['admin', 'salesman']
  },
  {
    id: 'products',
    label: '产品管理',
    icon: 'box',
    view: 'products',
    roles: ['admin']
  },
  {
    id: 'sales',
    label: '销售记录',
    icon: 'trending-up',
    view: 'sales',
    roles: ['admin', 'salesman']
  },
  {
    id: 'contacts',
    label: '联络记录',
    icon: 'phone',
    view: 'contacts',
    roles: ['admin', 'salesman']
  },
  {
    id: 'services',
    label: '服务记录',
    icon: 'tool',
    view: 'services',
    roles: ['admin', 'salesman']
  },
  {
    id: 'collections',
    label: '催款记录',
    icon: 'dollar-sign',
    view: 'collections',
    roles: ['admin', 'salesman']
  },
  {
    id: 'complaints',
    label: '投诉记录',
    icon: 'alert-circle',
    view: 'complaints',
    roles: ['admin', 'salesman']
  },
  {
    id: 'performance',
    label: '绩效统计',
    icon: 'bar-chart',
    view: 'performance',
    roles: ['admin']
  },
  {
    id: 'profile',
    label: '个人信息',
    icon: 'user',
    view: 'profile',
    roles: ['salesman']
  }
]

const viewComponent = computed(() => {
  if (view.value === 'login') return Login
  if (view.value === 'register') return Register
  if (view.value === 'dashboard') return Dashboard
  if (view.value === 'profile') return Profile
  if (view.value === 'salesmen') return SalesmanList
  if (view.value === 'customers') return CustomerList
  if (view.value === 'products') return ProductList
  if (view.value === 'sales') return SalesRecordList
  if (view.value === 'contacts') return ContactList
  if (view.value === 'services') return ServiceList
  if (view.value === 'collections') return CollectionList
  if (view.value === 'complaints') return ComplaintList
  return Dashboard
})

const pageTitle = computed(() => {
  const titleMap = {
    dashboard: '首页',
    profile: '个人信息',
    salesmen: '销售员管理',
    customers: '我的客户',
    products: '产品管理',
    sales: '销售记录',
    contacts: '联络记录',
    services: '服务记录',
    collections: '催款记录',
    complaints: '投诉记录',
    performance: '绩效统计',
    profile: '个人信息'
  }
  return titleMap[view.value] || '系统'
})

function onLoginSuccess(payload){
  token.value = payload.token || null
  currentUser.value = {
    id: payload.salesmanId || null,
    name: payload.name || 'User',
    role: payload.role || 'salesman'
  }
  if (token.value) localStorage.setItem('ms_token', token.value)
  
  // 保存角色和用户名到localStorage供Dashboard使用
  localStorage.setItem('ms_role', payload.role || 'salesman')
  localStorage.setItem('ms_name', payload.name || 'User')
  
  // 先进入系统
  view.value = 'dashboard'
  
  // 如果需要完善联系方式，显示提示
  if (payload.needCompleteProfile && payload.role === 'salesman') {
    showProfileTip.value = true
  }
}

function toRegister() {
  view.value = 'register'
}

function toLogin() {
  view.value = 'login'
}

function logout(){
  token.value = null
  currentUser.value = { id: null, name: '', role: '' }
  localStorage.removeItem('ms_token')
  view.value = 'login'
}

function hasPermission(roles) {
  if (!roles || roles.length === 0) return true
  return roles.includes(currentUser.value.role)
}

function navigateTo(targetView) {
  view.value = targetView
}

function skipProfileTip() {
  showProfileTip.value = false
}

function goToProfile() {
  showProfileTip.value = false
  view.value = 'profile'
}
</script>

<template>
  <div>
    <div class="layout">
      <aside class="sidebar" v-if="isLoggedIn">
        <div class="sidebar-header">
          <div class="logo-icon">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2"/>
            </svg>
          </div>
          <div class="sidebar-title">罗德岛系统</div>
        </div>

        <nav class="menu">
          <div 
            v-for="item in menuConfig" 
            :key="item.id"
            v-show="hasPermission(item.roles)"
            class="menu-item" 
            :class="{active: view === item.view}"
            @click="navigateTo(item.view)"
          >
            <span class="menu-label">{{ item.label }}</span>
          </div>
        </nav>
      </aside>

      <div class="main-area">
        <header class="topbar" v-if="isLoggedIn">
          <div class="title">{{ pageTitle }}</div>
          <div class="top-actions">
            <span class="user-info">
              <span class="user-role">{{ isAdmin ? '管理员' : '销售员' }}</span>
              <span class="user-name">{{ currentUser.name }}</span>
            </span>
            <button @click="logout" class="logout-btn">退出登录</button>
          </div>
        </header>

        <main class="main" :class="{fullscreen: !isLoggedIn}">
          <component 
            :is="viewComponent" 
            :salesmanId="currentUser.id"
            @login-success="onLoginSuccess" 
            @to-register="toRegister"
            @to-login="toLogin"
          />
        </main>
      </div>
    </div>

    <!-- 完善联系方式提示弹窗 -->
    <div v-if="showProfileTip" class="modal-overlay" @click="showProfileTip = false">
      <div class="modal-box" @click.stop>
        <h3>提示</h3>
        <p>您还未完善联系方式，是否现在前往完善?</p>
        <p class="tip-text">完善联系方式后，方便公司和客户联系您</p>
        <div class="modal-actions">
          <button @click="skipProfileTip" class="btn-secondary">稍后完善</button>
          <button @click="goToProfile" class="btn-primary">立即完善</button>
        </div>
      </div>
    </div>
  </div>
</template>

<style scoped>
.layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  width: 240px;
  background: linear-gradient(180deg, #1e293b 0%, #0f172a 100%);
  color: #fff;
  display: flex;
  flex-direction: column;
  overflow-y: auto;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
}

.sidebar::-webkit-scrollbar {
  width: 6px;
}

.sidebar::-webkit-scrollbar-track {
  background: rgba(255, 255, 255, 0.05);
}

.sidebar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.sidebar-header {
  padding: 24px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon svg {
  width: 24px;
  height: 24px;
}

.sidebar-title {
  font-size: 18px;
  font-weight: 700;
  letter-spacing: 0.5px;
}

.menu {
  padding: 16px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-item {
  padding: 10px 16px;
  margin: 2px 0;
  border-radius: 8px;
  cursor: pointer;
  color: #cbd5e1;
  font-size: 14px;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  gap: 10px;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
}

.menu-item.active {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  font-weight: 500;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
}

.main-area {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.topbar {
  height: 64px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 24px;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.05);
}

.title {
  font-size: 18px;
  font-weight: 600;
  color: #1f2937;
}

.top-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 12px;
  background: #f3f4f6;
  border-radius: 8px;
}

.user-role {
  font-size: 12px;
  color: #6b7280;
  padding: 2px 8px;
  background: #e5e7eb;
  border-radius: 4px;
}

.user-name {
  font-size: 14px;
  color: #1f2937;
  font-weight: 500;
}

.logout-btn {
  padding: 8px 16px;
  border-radius: 8px;
  border: 1px solid #d1d5db;
  background: #fff;
  color: #374151;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.2s;
}

.logout-btn:hover {
  background: #f9fafb;
  border-color: #9ca3af;
}

.main {
  flex: 1;
  overflow-y: auto;
  background: #f9fafb;
}

.main.fullscreen {
  padding: 0;
  background: transparent;
}

.main::-webkit-scrollbar {
  width: 8px;
}

.main::-webkit-scrollbar-track {
  background: #f1f5f9;
}

.main::-webkit-scrollbar-thumb {
  background: #cbd5e1;
  border-radius: 4px;
}

.main::-webkit-scrollbar-thumb:hover {
  background: #94a3b8;
}

/* 模态框样式 */
.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10000;
}

.modal-box {
  background: white;
  border-radius: 12px;
  padding: 32px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-box h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  color: #1e293b;
  font-weight: 600;
}

.modal-box p {
  margin: 0 0 12px 0;
  color: #475569;
  font-size: 15px;
  line-height: 1.6;
}

.tip-text {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 24px !important;
}

.modal-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

.btn-secondary, .btn-primary {
  padding: 10px 24px;
  border-radius: 8px;
  border: none;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.btn-secondary {
  background: #f1f5f9;
  color: #64748b;
}

.btn-secondary:hover {
  background: #e2e8f0;
}

.btn-primary {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}
</style>

