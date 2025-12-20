<template>
  <div class="profile-container">
    <div class="profile-header">
      <h2>个人信息</h2>
      <p class="subtitle">管理您的个人资料和联系方式</p>
    </div>

    <div class="profile-content">
      <!-- 左侧：头像和基本信息 -->
      <div class="profile-sidebar">
        <div class="avatar-section">
          <div class="avatar-wrapper">
            <img v-if="form.avatar" :src="getAvatarUrl(form.avatar)" alt="头像" class="avatar-img" />
            <div v-else class="avatar-placeholder">
              <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
                <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              </svg>
            </div>
          </div>
          
          <label for="avatar-upload" class="upload-btn">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M21 15V19C21 19.5304 20.7893 20.0391 20.4142 20.4142C20.0391 20.7893 19.5304 21 19 21H5C4.46957 21 3.96086 20.7893 3.58579 20.4142C3.21071 20.0391 3 19.5304 3 19V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M17 8L12 3L7 8" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 3V15" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            {{ form.avatar ? '修改头像' : '上传头像' }}
          </label>
          <input 
            id="avatar-upload" 
            type="file" 
            accept="image/*" 
            @change="handleAvatarUpload" 
            style="display: none"
          />
          
          <p class="avatar-tip">支持JPG、PNG格式，大小不超过5MB</p>
        </div>

        <div class="basic-info">
          <div class="info-item">
            <span class="info-label">员工编号</span>
            <span class="info-value">{{ form.employeeNo || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">入职日期</span>
            <span class="info-value">{{ form.hireDate || '-' }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">当前等级</span>
            <span class="info-value">
              <span class="level-badge" :class="getLevelClass(form.level)">
                {{ getLevelText(form.level) }}
              </span>
            </span>
          </div>
          <div class="info-item">
            <span class="info-label">基础提成</span>
            <span class="info-value commission">{{ formatCommission(form.commissionRate) }}</span>
          </div>
          <div class="info-item">
            <span class="info-label">账号状态</span>
            <span class="info-value">
              <span class="status-badge" :class="form.status === 'active' ? 'active' : 'inactive'">
                {{ form.status === 'active' ? '在职' : '离职' }}
              </span>
            </span>
          </div>
        </div>
      </div>

      <!-- 右侧：详细信息表单 -->
      <div class="profile-main">
        <form @submit.prevent="handleSubmit" class="profile-form">
          <div class="form-section">
            <h3 class="section-title">基本信息</h3>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">姓名</label>
                <input 
                  v-model="form.name" 
                  type="text" 
                  class="form-input"
                  disabled
                />
              </div>
              <div class="form-group">
                <label class="form-label">员工编号</label>
                <input 
                  v-model="form.employeeNo" 
                  type="text" 
                  class="form-input"
                  disabled
                />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">联系方式</h3>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">
                  <span class="required">*</span>
                  联系电话
                </label>
                <input 
                  v-model="form.phone" 
                  type="tel" 
                  class="form-input"
                  placeholder="请输入手机号码"
                  :class="{ error: errors.phone }"
                />
                <span v-if="errors.phone" class="error-text">{{ errors.phone }}</span>
              </div>
              <div class="form-group">
                <label class="form-label">邮箱</label>
                <input 
                  v-model="form.email" 
                  type="email" 
                  class="form-input"
                  placeholder="请输入邮箱地址"
                  :class="{ error: errors.email }"
                />
                <span v-if="errors.email" class="error-text">{{ errors.email }}</span>
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">QQ号</label>
                <input 
                  v-model="form.qq" 
                  type="text" 
                  class="form-input"
                  placeholder="请输入QQ号"
                />
              </div>
              <div class="form-group">
                <label class="form-label">微信号</label>
                <input 
                  v-model="form.wechat" 
                  type="text" 
                  class="form-input"
                  placeholder="请输入微信号"
                />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">职业信息</h3>
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">入职日期</label>
                <input 
                  v-model="form.hireDate" 
                  type="date" 
                  class="form-input"
                  disabled
                />
              </div>
              <div class="form-group">
                <label class="form-label">职级</label>
                <input 
                  v-model="levelText" 
                  type="text" 
                  class="form-input"
                  disabled
                />
              </div>
            </div>
            
            <div class="form-row">
              <div class="form-group">
                <label class="form-label">基础提成比例</label>
                <input 
                  v-model="commissionText" 
                  type="text" 
                  class="form-input"
                  disabled
                />
              </div>
              <div class="form-group">
                <label class="form-label">账号状态</label>
                <input 
                  :value="form.status === 'active' ? '在职' : '离职'" 
                  type="text" 
                  class="form-input"
                  disabled
                />
              </div>
            </div>
          </div>

          <div class="form-section">
            <h3 class="section-title">备注信息</h3>
            <div class="form-group full-width">
              <label class="form-label">备注</label>
              <textarea 
                v-model="form.remark" 
                class="form-textarea"
                placeholder="暂无备注信息"
                rows="4"
                disabled
              ></textarea>
            </div>
          </div>

          <div class="form-actions">
            <button type="button" @click="handleReset" class="btn-secondary" :disabled="loading">
              重置
            </button>
            <button type="submit" class="btn-primary" :disabled="loading">
              <span v-if="loading">保存中...</span>
              <span v-else>保存更改</span>
            </button>
          </div>

          <div v-if="successMessage" class="success-message">
            {{ successMessage }}
          </div>
          <div v-if="errorMessage" class="error-message">
            {{ errorMessage }}
          </div>
        </form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { salesmanApi } from '../services/api'

const props = defineProps({
  salesmanId: {
    type: Number,
    required: true
  }
})

const form = ref({
  id: null,
  name: '',
  employeeNo: '',
  phone: '',
  qq: '',
  wechat: '',
  email: '',
  avatar: '',
  hireDate: '',
  level: '',
  commissionRate: '',
  status: '',
  remark: ''
})

const originalForm = ref({})
const errors = ref({})
const loading = ref(false)
const successMessage = ref('')
const errorMessage = ref('')

const levelText = computed(() => getLevelText(form.value.level))
const commissionText = computed(() => formatCommission(form.value.commissionRate))

function getLevelText(level) {
  if (!level) return '-'
  const normalizedLevel = level.toLowerCase()
  const levelMap = {
    'junior': '初级销售员',
    'intermediate': '中级销售员',
    'senior': '高级销售员',
    'expert': '资深销售员'
  }
  return levelMap[normalizedLevel] || '-'
}

function getLevelClass(level) {
  if (!level) return ''
  const normalizedLevel = level.toLowerCase()
  return {
    'junior': 'level-junior',
    'intermediate': 'level-intermediate',
    'senior': 'level-senior',
    'expert': 'level-expert'
  }[normalizedLevel] || ''
}

function formatCommission(rate) {
  if (!rate) return '-'
  return (parseFloat(rate) * 100).toFixed(2) + '%'
}

function getAvatarUrl(avatar) {
  if (!avatar) return ''
  if (avatar.startsWith('http')) return avatar
  return `http://localhost:8080${avatar}`
}

async function loadProfile() {
  try {
    const res = await salesmanApi.getById(props.salesmanId)
    if (res.code === 200 && res.data) {
      form.value = { ...res.data }
      originalForm.value = { ...res.data }
    }
  } catch (error) {
    console.error('加载个人信息失败:', error)
    errorMessage.value = '加载个人信息失败'
  }
}

async function handleAvatarUpload(event) {
  const file = event.target.files[0]
  if (!file) return

  // 验证文件类型
  if (!file.type.startsWith('image/')) {
    errorMessage.value = '只能上传图片文件'
    return
  }

  // 验证文件大小
  if (file.size > 5 * 1024 * 1024) {
    errorMessage.value = '图片大小不能超过5MB'
    return
  }

  try {
    loading.value = true
    errorMessage.value = ''
    
    const formData = new FormData()
    formData.append('file', file)

    const response = await fetch('http://localhost:8080/api/upload/avatar', {
      method: 'POST',
      body: formData
    })

    const result = await response.json()
    
    if (result.code === 200) {
      form.value.avatar = result.data
      successMessage.value = '头像上传成功'
      setTimeout(() => successMessage.value = '', 3000)
    } else {
      errorMessage.value = result.message || '头像上传失败'
    }
  } catch (error) {
    console.error('上传失败:', error)
    errorMessage.value = '头像上传失败'
  } finally {
    loading.value = false
  }
}

function validate() {
  errors.value = {}
  
  if (!form.value.phone || !form.value.phone.trim()) {
    errors.value.phone = '请输入联系电话'
  } else if (!/^1[3-9]\d{9}$/.test(form.value.phone)) {
    errors.value.phone = '请输入有效的手机号码'
  }
  
  if (form.value.email && !/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(form.value.email)) {
    errors.value.email = '请输入有效的邮箱地址'
  }
  
  return Object.keys(errors.value).length === 0
}

async function handleSubmit() {
  if (!validate()) return

  try {
    loading.value = true
    errorMessage.value = ''
    successMessage.value = ''

    const updateData = {
      phone: form.value.phone,
      qq: form.value.qq,
      wechat: form.value.wechat,
      email: form.value.email,
      avatar: form.value.avatar
    }

    const res = await salesmanApi.updateContact(props.salesmanId, updateData)
    
    if (res.code === 200) {
      successMessage.value = '保存成功'
      originalForm.value = { ...form.value }
      setTimeout(() => successMessage.value = '', 3000)
    } else {
      errorMessage.value = res.message || '保存失败'
    }
  } catch (error) {
    console.error('保存失败:', error)
    errorMessage.value = '保存失败，请稍后重试'
  } finally {
    loading.value = false
  }
}

function handleReset() {
  form.value = { ...originalForm.value }
  errors.value = {}
  errorMessage.value = ''
  successMessage.value = ''
}

onMounted(() => {
  loadProfile()
})
</script>

<style scoped>
.profile-container {
  padding: 24px;
  max-width: 1400px;
  margin: 0 auto;
}

.profile-header {
  margin-bottom: 32px;
}

.profile-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: 600;
  color: #1e293b;
}

.subtitle {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.profile-content {
  display: grid;
  grid-template-columns: 320px 1fr;
  gap: 24px;
}

/* 左侧栏 */
.profile-sidebar {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.avatar-section {
  background: white;
  border-radius: 12px;
  padding: 24px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.avatar-wrapper {
  width: 160px;
  height: 160px;
  margin: 0 auto 16px;
}

.avatar-img {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  object-fit: cover;
  border: 4px solid #f1f5f9;
}

.avatar-placeholder {
  width: 100%;
  height: 100%;
  border-radius: 50%;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  display: flex;
  align-items: center;
  justify-content: center;
}

.avatar-placeholder svg {
  width: 80px;
  height: 80px;
  color: white;
}

.upload-btn {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 10px 24px;
  background: #0ea5e90f;
  border: 2px solid rgba(14, 165, 233, 0.85);
  border-radius: 10px;
  color: #0ea5e9;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
  margin-bottom: 12px;
}

.upload-btn:hover {
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #0b1221;
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.3);
}

.upload-btn svg {
  width: 16px;
  height: 16px;
}

.avatar-tip {
  margin: 0;
  font-size: 12px;
  color: #94a3b8;
}

.basic-info {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.info-item {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 0;
  border-bottom: 1px solid #f1f5f9;
}

.info-item:last-child {
  border-bottom: none;
}

.info-label {
  font-size: 13px;
  color: #64748b;
  font-weight: 500;
}

.info-value {
  font-size: 14px;
  color: #1e293b;
  font-weight: 500;
}

.commission {
  color: #10b981;
  font-weight: 600;
}

.level-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.level-junior {
  background: #e2e8f0;
  color: #64748b;
}

.level-intermediate {
  background: #dbeafe;
  color: #3b82f6;
}

.level-senior {
  background: #f3e8ff;
  color: #a855f7;
}

.level-expert {
  background: #fef3c7;
  color: #f59e0b;
}

.status-badge {
  padding: 4px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 600;
}

.status-badge.active {
  background: #d1fae5;
  color: #10b981;
}

.status-badge.inactive {
  background: #fee2e2;
  color: #ef4444;
}

/* 右侧表单 */
.profile-main {
  background: white;
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.profile-form {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.form-section {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.section-title {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  padding-bottom: 12px;
  border-bottom: 2px solid #f1f5f9;
}

.form-row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group.full-width {
  grid-column: 1 / -1;
}

.form-label {
  font-size: 14px;
  font-weight: 500;
  color: #475569;
}

.required {
  color: #ef4444;
  margin-right: 4px;
}

.form-input,
.form-textarea {
  padding: 10px 14px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.2s;
}

.form-input:focus,
.form-textarea:focus {
  outline: none;
  border-color: rgba(14, 165, 233, 0.9);
  box-shadow: 0 10px 24px rgba(14, 165, 233, 0.18), 0 0 0 1px rgba(14, 165, 233, 0.75);
}

.form-input:disabled,
.form-textarea:disabled {
  background: #f8fafc;
  color: #94a3b8;
  cursor: not-allowed;
}

.form-input.error {
  border-color: #ef4444;
}

.error-text {
  font-size: 12px;
  color: #ef4444;
}

.form-textarea {
  resize: vertical;
  min-height: 100px;
  font-family: inherit;
}

.form-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  padding-top: 20px;
  border-top: 2px solid #f1f5f9;
}

.btn-secondary,
.btn-primary {
  padding: 12px 32px;
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

.btn-secondary:hover:not(:disabled) {
  background: #e2e8f0;
}

.btn-primary {
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #0b1221;
}

.btn-primary:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.35);
}

.btn-secondary:disabled,
.btn-primary:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.success-message,
.error-message {
  padding: 12px 16px;
  border-radius: 8px;
  font-size: 14px;
  text-align: center;
}

.success-message {
  background: #d1fae5;
  color: #10b981;
  border: 1px solid #10b981;
}

.error-message {
  background: #fee2e2;
  color: #ef4444;
  border: 1px solid #ef4444;
}

@media (max-width: 1024px) {
  .profile-content {
    grid-template-columns: 1fr;
  }
  
  .form-row {
    grid-template-columns: 1fr;
  }
}
</style>
