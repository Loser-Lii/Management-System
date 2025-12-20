<script setup lang="ts">
import { ref, computed, watch } from 'vue'
import { authApi, adminApi } from './services/api'
import Login from './components/Login.vue'
import Register from './components/Register.vue'
import Dashboard from './components/Dashboard.vue'
import Profile from './components/Profile.vue'
import SalesmanList from './components/SalesmanList.vue'
import CustomerList from './components/CustomerList.vue'
import ProductList from './components/ProductList.vue'
import SalesRecordView from './components/SalesRecordView.vue'
import ContactList from './components/ContactList.vue'
import ServiceList from './components/ServiceList.vue'
import CollectionList from './components/CollectionList.vue'
import ComplaintList from './components/ComplaintList.vue'

// 初始化视图：如果已登录，优先使用保存的视图，否则使用 dashboard
const savedView = localStorage.getItem('ms_currentView')
const view = ref(localStorage.getItem('ms_token') ? (savedView || 'dashboard') : 'login')
const token = ref(localStorage.getItem('ms_token'))
const currentUser = ref({ 
  id: (localStorage.getItem('ms_salesmanId') ? Number(localStorage.getItem('ms_salesmanId')) : null), 
  name: localStorage.getItem('ms_name') || '', 
  role: localStorage.getItem('ms_role') || '',
  avatar: localStorage.getItem('ms_avatar') || ''
})
const showProfileTip = ref(false)
const showRestoreModal = ref(false)
const avatarClickCount = ref(0)
let avatarClickResetTimer: ReturnType<typeof setTimeout> | null = null
const restoreTextarea = ref<HTMLTextAreaElement | null>(null)
const secretKey = ref('')
const keyVerified = ref(false)
const keyError = ref('')
const SECRET_KEY = 'DEMO-SECRET-1234'

const backups = ref<{ name: string; size: number; lastModified: number }[]>([])
const backupLoading = ref(false)
const selectedBackup = ref('')
const restoreError = ref('')

// 全量重建脚本（PowerShell），基于所选备份文件，直接改密码即可执行
const restoreScript = computed(() => {
  const db = 'salesman_performance_management_system'
  const backupFile = selectedBackup.value || 'management_full_backup.sql'
  return [
    '# 在服务器 PowerShell 执行，先停写/下线',
    `$DB="${db}"`,
    `$DUMP="db_backups/${backupFile}"   # 如需绝对路径请自行补全`,
    '$env:MYSQL_PWD="<你的MySQL密码>"',
    'mysqldump --protocol=TCP --host=127.0.0.1 --port=3306 --user=root --password=$env:MYSQL_PWD --single-transaction --routines --triggers $DB > "management_before_restore_$((Get-Date).ToString(\'yyyyMMdd_HHmmss\')).sql"',
    'mysql --protocol=TCP --host=127.0.0.1 --port=3306 --user=root --password=$env:MYSQL_PWD -e "DROP DATABASE IF EXISTS `$DB; CREATE DATABASE `$DB DEFAULT CHARACTER SET utf8mb4;"',
    'mysql --protocol=TCP --host=127.0.0.1 --port=3306 --user=root --password=$env:MYSQL_PWD --binary-mode=1 $DB -e "source $DUMP"',
    'Remove-Item env:MYSQL_PWD'
  ].join('\n')
})

const isLoggedIn = computed(() => !!token.value)
const isAdmin = computed(() => {
  const result = currentUser.value.role === 'admin'
  console.log('isAdmin computed - role:', currentUser.value.role, 'result:', result)
  return result
})

// 菜单分组配置（按角色过滤，仅显示有权限的分组）
const menuSections = computed(() => {
  const sections = [
    {
      id: 'home-section',
      title: '首页',
      items: [
        { id: 'home', label: '首页', view: 'dashboard', roles: ['admin', 'salesman'] }
      ]
    },
    {
      id: 'resource-section',
      title: '资源管理',
      items: [
        { id: 'salesmen', label: '销售员管理', view: 'salesmen', roles: ['admin'] },
        { id: 'customers', label: isAdmin.value ? '客户总览' : '我的客户', view: 'customers', roles: ['admin', 'salesman'] },
        { id: 'products', label: '产品管理', view: 'products', roles: ['admin'] }
      ]
    },
    {
      id: 'business-section',
      title: '业务管理',
      items: [
        { id: 'sales', label: '销售记录', view: 'sales', roles: ['admin', 'salesman'] },
        { id: 'contacts', label: '联络记录', view: 'contacts', roles: ['admin', 'salesman'] },
        { id: 'services', label: '服务记录', view: 'services', roles: ['admin', 'salesman'] },
        { id: 'collections', label: '催款记录', view: 'collections', roles: ['admin', 'salesman'] },
        { id: 'complaints', label: '投诉记录', view: 'complaints', roles: ['admin', 'salesman'] }
      ]
    },
    {
      id: 'analysis-section',
      title: '统计分析',
      items: [
        { id: 'performance', label: '绩效统计', view: 'performance', roles: ['admin'] }
      ]
    },
    {
      id: 'profile-section',
      title: '个人',
      items: [
        { id: 'profile', label: '个人信息', view: 'profile', roles: ['salesman'] }
      ]
    }
  ]

  // 仅保留当前角色可见的分组
  return sections
    .map(section => {
      const visibleItems = section.items.filter(item => !item.roles || item.roles.includes(currentUser.value.role))
      return { ...section, items: visibleItems }
    })
    .filter(section => section.items.length > 0)
})

const viewComponent = computed(() => {
  if (view.value === 'login') return Login
  if (view.value === 'register') return Register
  if (view.value === 'dashboard') return Dashboard
  if (view.value === 'profile') return Profile
  if (view.value === 'salesmen') return SalesmanList
  if (view.value === 'customers') return CustomerList
  if (view.value === 'products') return ProductList
  if (view.value === 'sales') {
    console.log('销售记录视图 - 当前角色:', currentUser.value.role, '是否管理员:', isAdmin.value)
    return SalesRecordView
  }
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

function getAvatarUrl(avatar) {
  if (!avatar) return '';
  if (avatar.startsWith('http://') || avatar.startsWith('https://')) {
    return avatar;
  }
  return `http://localhost:8080${avatar}`;
}

function handleAdminAvatarClick() {
  if (!isAdmin.value) return
  avatarClickCount.value += 1
  if (avatarClickResetTimer) clearTimeout(avatarClickResetTimer)
  avatarClickResetTimer = setTimeout(() => {
    avatarClickCount.value = 0
  }, 2000)

  if (avatarClickCount.value >= 5) {
    showRestoreModal.value = true
    avatarClickCount.value = 0
    if (avatarClickResetTimer) {
      clearTimeout(avatarClickResetTimer)
      avatarClickResetTimer = null
    }
  }
}

async function copyRestoreScript() {
  try {
    if (!keyVerified.value) {
      alert('请先输入正确密钥再复制脚本')
      return
    }
    await navigator.clipboard.writeText(restoreScript.value)
    alert('全量重建脚本已复制')
  } catch (e) {
    console.error('复制失败', e)
    // 使用 textarea 选中后回退复制
    if (restoreTextarea.value) {
      restoreTextarea.value.focus()
      restoreTextarea.value.select()
      const ok = document.execCommand('copy')
      if (ok) {
        alert('已通过回退方式复制，如未成功请手动复制')
        return
      }
    }
    alert('复制失败，请手动选择脚本复制')
  }
}

function verifySecretKey() {
  if (!secretKey.value.trim()) {
    keyError.value = '请输入密钥'
    keyVerified.value = false
    return
  }
  if (secretKey.value.trim() === SECRET_KEY) {
    keyVerified.value = true
    keyError.value = ''
    return
  }
  keyVerified.value = false
  keyError.value = '密钥错误'
}

async function loadBackups() {
  if (!isAdmin.value) return
  backupLoading.value = true
  restoreError.value = ''
  try {
    const res = await adminApi.listBackups()
    if (res.code === 200 && Array.isArray(res.data)) {
      backups.value = res.data
      if (!selectedBackup.value && backups.value.length) {
        selectedBackup.value = backups.value[0].name
      }
    } else {
      restoreError.value = res.message || '获取备份列表失败'
    }
  } catch (e: any) {
    restoreError.value = e?.response?.data?.message || e?.message || '获取备份列表失败'
  } finally {
    backupLoading.value = false
  }
}

function formatTime(ts: number) {
  if (!ts) return ''
  const d = new Date(ts)
  return `${d.getFullYear()}-${String(d.getMonth() + 1).padStart(2, '0')}-${String(d.getDate()).padStart(2, '0')} ${String(d.getHours()).padStart(2, '0')}:${String(d.getMinutes()).padStart(2, '0')}`
}

watch(showRestoreModal, (v) => {
  if (v) {
    keyVerified.value = false
    secretKey.value = ''
    keyError.value = ''
    loadBackups()
  }
})

function onLoginSuccess(payload){
  console.log('登录成功 - 完整payload:', payload)
  token.value = payload.token || null
  const userRole = payload.role || 'salesman'
  const userId = payload.salesmanId || null
  const userName = payload.name || 'User'
  const userAvatar = payload.avatar || ''
  
  currentUser.value = {
    id: userId,
    name: userName,
    role: userRole,
    avatar: userAvatar
  }
  
  if (token.value) localStorage.setItem('ms_token', token.value)
  localStorage.setItem('ms_role', userRole)
  localStorage.setItem('ms_name', userName)
  localStorage.setItem('ms_salesmanId', String(userId || ''))
  localStorage.setItem('ms_username', payload.username || userName)
  if (userAvatar) localStorage.setItem('ms_avatar', userAvatar)
  
  console.log('登录成功 - 角色:', userRole, '是否管理员:', userRole === 'admin', 'isAdmin.value:', isAdmin.value)
  
  // 先进入系统
  view.value = 'dashboard'
  localStorage.setItem('ms_currentView', 'dashboard')
  
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

async function logout(){
  try {
    await authApi.logout()
  } catch (err) {
    console.error('Logout failed:', err)
  } finally {
    token.value = null
    currentUser.value = { id: null, name: '', role: '' }
    localStorage.removeItem('ms_token')
    localStorage.removeItem('ms_role')
    localStorage.removeItem('ms_name')
    localStorage.removeItem('ms_salesmanId')
    localStorage.removeItem('ms_username')
    localStorage.removeItem('ms_avatar')
    view.value = 'login'
  }
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

// 监听视图变化，自动保存到 localStorage
watch(view, (newView) => {
  if (newView !== 'login' && newView !== 'register') {
    localStorage.setItem('ms_currentView', newView)
  }
})
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
          <div class="sidebar-title">罗德岛业绩管理系统</div>
        </div>

        <nav class="menu">
          <div v-for="section in menuSections" :key="section.id" class="menu-section">
            <div class="menu-section-title">{{ section.title }}</div>
            <div
              v-for="item in section.items"
              :key="item.id"
              class="menu-item"
              :class="{active: view === item.view}"
              @click="navigateTo(item.view)"
            >
              <span class="menu-label">{{ item.label }}</span>
            </div>
          </div>
        </nav>
      </aside>

      <div class="main-area">
        <header class="topbar" v-if="isLoggedIn">
          <div class="title">{{ pageTitle }}</div>
          <div class="top-actions">
            <span class="user-info">
              <img
                v-if="!isAdmin && currentUser.avatar"
                :src="getAvatarUrl(currentUser.avatar)"
                class="user-avatar"
                :alt="currentUser.name"
              />
              <div
                v-else-if="isAdmin"
                class="user-avatar admin"
                role="button"
                tabindex="0"
                @click="handleAdminAvatarClick"
                @keydown.enter.prevent="handleAdminAvatarClick"
              >A</div>
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

    <!-- 隐藏入口：管理员头像 5 连击触发数据库恢复提示 -->
    <div v-if="showRestoreModal" class="modal-overlay" @click="showRestoreModal = false">
      <div class="modal-box" @click.stop>
        <h3>数据库恢复操作提示</h3>
        <p>此入口用于紧急恢复数据库，请确保具备权限并已备份当前数据。</p>
        <ol class="restore-steps">
          <li>确认已有备份文件（默认 management_full_backup.sql）。</li>
          <li>执行脚本前先停止应用写入或下线。</li>
          <li>在服务器 PowerShell 运行下方脚本进行全量重建。</li>
          <li>恢复后校验关键表行数与核心业务功能。</li>
        </ol>
        <div class="backup-picker">
          <div class="picker-header">
            <span>可用备份文件</span>
            <span v-if="backupLoading" class="muted">加载中...</span>
            <span v-else-if="!backups.length" class="muted">未找到 .sql 备份</span>
          </div>
          <div v-if="restoreError" class="error-text">{{ restoreError }}</div>
          <div class="backup-list" v-if="backups.length">
            <label v-for="b in backups" :key="b.name" class="backup-item">
              <input type="radio" :value="b.name" v-model="selectedBackup" />
              <div class="backup-meta">
                <div class="name">{{ b.name }}</div>
                <div class="sub">{{ formatTime(b.lastModified) }} · {{ (b.size/1024/1024).toFixed(2) }} MB</div>
              </div>
            </label>
          </div>
        </div>

        <div class="secret-input">
          <label for="restore-secret">安全密钥（默认 DEMO-SECRET-1234）</label>
          <input
            id="restore-secret"
            type="password"
            v-model="secretKey"
            placeholder="请输入密钥以生成脚本"
          />
          <div v-if="keyError" class="error-text">{{ keyError }}</div>
          <button class="btn-secondary" @click="verifySecretKey">验证密钥并生成脚本</button>
        </div>

        <div class="restore-script">
          <div class="restore-script__header">
            <span>全量重建脚本（选定备份后复制）</span>
            <button class="btn-secondary" @click="copyRestoreScript">复制脚本</button>
          </div>
          <div v-if="keyVerified">
            <textarea
              ref="restoreTextarea"
              class="script-textarea"
              readonly
              :value="restoreScript"
            ></textarea>
          </div>
          <div v-else class="muted">密钥校验通过后展示脚本</div>
        </div>
        <div class="modal-actions">
          <button @click="showRestoreModal = false" class="btn-secondary">关闭</button>
        </div>
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
  overflow: hidden;
  box-shadow: 2px 0 8px rgba(0, 0, 0, 0.1);
  position: relative;
  border-right: 1px solid rgba(14, 165, 233, 0.24);
}

.sidebar::before {
  content: '';
  position: absolute;
  inset: -40px;
  pointer-events: none;
  opacity: 0.22;
  background:
    radial-gradient(600px 320px at 20% 10%, rgba(14, 165, 233, 0.45), transparent 65%),
    radial-gradient(520px 320px at 80% 20%, rgba(34, 211, 238, 0.32), transparent 65%),
    radial-gradient(520px 420px at 40% 90%, rgba(245, 158, 11, 0.24), transparent 65%);
  animation: sidebarNeonDrift 8s ease-in-out infinite;
}

@keyframes sidebarNeonDrift {
  0%, 100% {
    transform: translate3d(0, 0, 0);
    filter: saturate(1);
  }
  50% {
    transform: translate3d(10px, -10px, 0);
    filter: saturate(1.15);
  }
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
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
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
  font-size: 15px;
  font-weight: 700;
  letter-spacing: 0.5px;
  text-shadow: 0 0 18px rgba(14, 165, 233, 0.35);
}

.menu {
  padding: 16px 12px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.menu-section {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.menu-section-title {
  font-size: 12px;
  color: rgba(226, 232, 240, 0.65);
  letter-spacing: 0.6px;
  padding: 4px 8px 2px;
  text-transform: none;
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
  position: relative;
  border: 1px solid transparent;
}

.menu-item:hover {
  background: rgba(255, 255, 255, 0.08);
  color: #fff;
  border-color: rgba(14, 165, 233, 0.32);
  box-shadow: 0 0 0 1px rgba(14, 165, 233, 0.16), 0 10px 24px rgba(0, 0, 0, 0.18);
}

.menu-item.active {
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #fff;
  font-weight: 500;
  border-color: rgba(255, 255, 255, 0.22);
  box-shadow:
    0 0 0 1px rgba(14, 165, 233, 0.5),
    0 10px 26px rgba(34, 211, 238, 0.32),
    0 0 22px rgba(245, 158, 11, 0.24);
}

.menu-item.active::after {
  content: '';
  position: absolute;
  inset: 0;
  border-radius: 8px;
  pointer-events: none;
  background: linear-gradient(90deg, rgba(255,255,255,0), rgba(255,255,255,0.22), rgba(255,255,255,0));
  transform: translateX(-120%);
  animation: menuScan 2.6s linear infinite;
  opacity: 0.9;
}

@keyframes menuScan {
  0% { transform: translateX(-120%); }
  55% { transform: translateX(120%); }
  100% { transform: translateX(120%); }
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

.user-avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  object-fit: cover;
  border: 2px solid #fff;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.user-avatar.admin {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  background: linear-gradient(135deg, #3b82f6, #1e40af);
  color: #fff;
  font-weight: 700;
  cursor: pointer;
  user-select: none;
  transition: transform 0.15s ease, box-shadow 0.15s ease;
}

.user-avatar.admin:focus,
.user-avatar.admin:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 20px rgba(30, 64, 175, 0.25);
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

.restore-steps {
  margin: 12px 0 20px;
  padding-left: 18px;
  color: #1f2937;
  line-height: 1.6;
}

.backup-picker {
  background: #f8fafc;
  border: 1px solid #e2e8f0;
  border-radius: 10px;
  padding: 12px;
  margin-bottom: 12px;
}

.picker-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-weight: 600;
  margin-bottom: 8px;
}

.backup-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
  max-height: 200px;
  overflow-y: auto;
}

.backup-item {
  display: flex;
  gap: 10px;
  padding: 8px 10px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #fff;
  cursor: pointer;
}

.backup-item input {
  margin-top: 6px;
}

.backup-meta .name {
  font-weight: 600;
  color: #111827;
}

.backup-meta .sub {
  color: #6b7280;
  font-size: 12px;
}

.secret-input {
  display: flex;
  flex-direction: column;
  gap: 6px;
  margin: 12px 0 16px;
}

.secret-input label {
  font-size: 13px;
  color: #e2e8f0;
}

.secret-input input {
  padding: 10px 12px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: #0b1220;
  color: #e2e8f0;
}

.muted { color: #94a3b8; font-size: 12px; }
.error-text { color: #dc2626; font-size: 12px; margin-bottom: 6px; }

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

.restore-script {
  background: #0f172a;
  color: #e2e8f0;
  border-radius: 10px;
  padding: 12px 12px 4px;
  margin: 8px 0 16px;
  border: 1px solid #1e293b;
  box-shadow: inset 0 0 0 1px rgba(255,255,255,0.02);
}

.restore-script__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
  font-weight: 600;
}

.script-textarea {
  width: 100%;
  min-height: 220px;
  margin: 0;
  padding: 12px;
  background: #0b1220;
  color: #cbd5e1;
  border-radius: 8px;
  font-family: "JetBrains Mono", Consolas, monospace;
  font-size: 13px;
  line-height: 1.6;
  border: 1px solid rgba(255, 255, 255, 0.08);
  resize: vertical;
  white-space: pre-wrap;
  word-break: break-all;
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
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: white;
}

.btn-primary:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.4);
}
</style>

