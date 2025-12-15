<template>
  <div class="login-container">
    <div class="login-background">
      <div class="shape shape-1"></div>
      <div class="shape shape-2"></div>
      <div class="shape shape-3"></div>
    </div>
    
    <div class="login-card">
      <div class="card-header">
        <div class="logo-wrapper">
          <div class="logo">
            <svg viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M12 2L2 7L12 12L22 7L12 2Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 17L12 22L22 17" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M2 12L12 17L22 12" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
          </div>
        </div>
        <h1 class="title">罗德岛医疗管理系统</h1>
        <p class="subtitle">欢迎回来，请登录您的账户</p>
      </div>

      <form @submit.prevent="onSubmit" class="login-form">
        <div class="form-group">
          <label class="form-label">
            <svg class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M20 21V19C20 17.9391 19.5786 16.9217 18.8284 16.1716C18.0783 15.4214 17.0609 15 16 15H8C6.93913 15 5.92172 15.4214 5.17157 16.1716C4.42143 16.9217 4 17.9391 4 19V21" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M12 11C14.2091 11 16 9.20914 16 7C16 4.79086 14.2091 3 12 3C9.79086 3 8 4.79086 8 7C8 9.20914 9.79086 11 12 11Z" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            用户名
          </label>
          <input 
            v-model="form.username" 
            type="text" 
            class="form-input"
            placeholder="请输入用户名或编号"
            :class="{ error: errors.username }"
          />
          <transition name="fade">
            <p v-if="errors.username" class="error-text">{{ errors.username }}</p>
          </transition>
        </div>

        <div class="form-group">
          <label class="form-label">
            <svg class="icon" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg">
              <rect x="3" y="11" width="18" height="11" rx="2" ry="2" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
              <path d="M7 11V7C7 5.67392 7.52678 4.40215 8.46447 3.46447C9.40215 2.52678 10.6739 2 12 2C13.3261 2 14.5979 2.52678 15.5355 3.46447C16.4732 4.40215 17 5.67392 17 7V11" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/>
            </svg>
            密码
          </label>
          <input 
            v-model="form.password" 
            type="password" 
            class="form-input"
            placeholder="请输入密码"
            :class="{ error: errors.password }"
          />
          <transition name="fade">
            <p v-if="errors.password" class="error-text">{{ errors.password }}</p>
          </transition>
        </div>

        <div class="form-options">
          <label class="checkbox-wrapper">
            <input type="checkbox" v-model="form.remember" />
            <span class="checkbox-label">记住我</span>
          </label>
          <a href="#" class="forgot-link">忘记密码？</a>
        </div>

        <button type="submit" class="submit-btn" :disabled="loading">
          <span v-if="!loading">登 录</span>
          <span v-else class="loading-text">
            <svg class="spinner" viewBox="0 0 24 24">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4" fill="none" opacity="0.25"/>
              <path d="M12 2a10 10 0 0 1 10 10" stroke="currentColor" stroke-width="4" fill="none" stroke-linecap="round"/>
            </svg>
            登录中...
          </span>
        </button>

        <transition name="fade">
          <div v-if="serverError" class="alert alert-error">
            <svg class="alert-icon" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="2"/>
              <line x1="12" y1="8" x2="12" y2="12" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
              <line x1="12" y1="16" x2="12.01" y2="16" stroke="currentColor" stroke-width="2" stroke-linecap="round"/>
            </svg>
            {{ serverError }}
          </div>
        </transition>
      </form>

      <div class="card-footer">
        <p class="footer-text">还没有账户？ <a @click="$emit('to-register')" class="register-link">立即注册</a></p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { reactive, ref, defineEmits } from 'vue';
import { authApi } from '../services/api';

const emit = defineEmits<{
  (e: 'login-success', payload: any): void,
  (e: 'to-register'): void
}>();

const form = reactive({ username: '', password: '', remember: false });
const errors = reactive({ username: '', password: '' });
const serverError = ref('');
const loading = ref(false);

function validate() {
  errors.username = '';
  errors.password = '';
  let ok = true;
  if (!form.username.trim()) {
    errors.username = '请输入用户名';
    ok = false;
  }
  if (!form.password) {
    errors.password = '请输入密码';
    ok = false;
  }
  return ok;
}

async function onSubmit() {
  serverError.value = '';
  // 已登录则阻止重复登录
  const existingToken = localStorage.getItem('ms_token');
  if (existingToken) {
    serverError.value = '当前已登录，如需切换账号请先退出';
    return;
  }
  if (!validate()) return;
  loading.value = true;
  try {
    const res = await authApi.login(form.username.trim(), form.password);
    if (res.code === 200 && res.data) {
      const token = res.data.token;
      if (form.remember && token) localStorage.setItem('ms_token', token);
      
      // 直接登录成功，将needCompleteProfile信息传递给App.vue
      emit('login-success', res.data);
    } else {
      serverError.value = res.message || '登录失败';
    }
  } catch (err: any) {
    serverError.value = err?.response?.data?.message || err?.message || '网络错误';
  } finally {
    loading.value = false;
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  width: 100vw;
  display: flex;
  align-items: center;
  justify-content: center;
  position: fixed;
  top: 0;
  left: 0;
  overflow: hidden;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  z-index: 999;
}

.login-background {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.shape {
  position: absolute;
  border-radius: 50%;
  filter: blur(80px);
  opacity: 0.5;
  animation: float 20s infinite ease-in-out;
}

.shape-1 {
  width: 500px;
  height: 500px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  top: -10%;
  left: -10%;
  animation-delay: 0s;
}

.shape-2 {
  width: 400px;
  height: 400px;
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  bottom: -10%;
  right: -5%;
  animation-delay: 3s;
}

.shape-3 {
  width: 350px;
  height: 350px;
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
  top: 40%;
  right: 20%;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(30px, -50px) scale(1.1);
  }
  66% {
    transform: translate(-20px, 20px) scale(0.9);
  }
}

.login-card {
  position: relative;
  z-index: 10;
  width: 460px;
  max-width: 90%;
  max-height: 90vh;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(20px);
  border-radius: 24px;
  padding: 40px 40px 36px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3), 0 0 0 1px rgba(255, 255, 255, 0.1);
  animation: slideUp 0.6s ease-out;
  overflow: hidden;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.card-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 16px;
}

.logo {
  width: 72px;
  height: 72px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  animation: logoFloat 3s ease-in-out infinite;
}

.logo svg {
  width: 36px;
  height: 36px;
}

@keyframes logoFloat {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-8px);
  }
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #1a202c;
  margin: 0 0 6px 0;
  letter-spacing: -0.5px;
}

.subtitle {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #2d3748;
  margin-bottom: 10px;
  gap: 8px;
}

.form-label .icon {
  width: 18px;
  height: 18px;
  color: #667eea;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  font-size: 15px;
  border: 2px solid #e2e8f0;
  border-radius: 12px;
  background: white;
  color: #2d3748;
  transition: all 0.3s ease;
  box-sizing: border-box;
  outline: none;
}

.form-input:focus {
  border-color: #667eea;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.form-input::placeholder {
  color: #a0aec0;
}

.form-input.error {
  border-color: #fc8181;
}

.error-text {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #e53e3e;
  display: flex;
  align-items: center;
  gap: 4px;
}

.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.checkbox-wrapper {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
}

.checkbox-wrapper input[type="checkbox"] {
  width: 18px;
  height: 18px;
  cursor: pointer;
  accent-color: #667eea;
}

.checkbox-label {
  font-size: 14px;
  color: #4a5568;
  user-select: none;
}

.forgot-link {
  font-size: 14px;
  color: #667eea;
  text-decoration: none;
  font-weight: 500;
  transition: color 0.2s;
}

.forgot-link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 600;
  color: white;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
  position: relative;
  overflow: hidden;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(102, 126, 234, 0.5);
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.7;
  cursor: not-allowed;
}

.loading-text {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
}

.spinner {
  width: 20px;
  height: 20px;
  animation: spin 1s linear infinite;
}

@keyframes spin {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.alert {
  margin-top: 16px;
  padding: 12px 16px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  animation: slideIn 0.3s ease-out;
}

@keyframes slideIn {
  from {
    opacity: 0;
    transform: translateY(-10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.alert-error {
  background: #fff5f5;
  color: #c53030;
  border: 1px solid #feb2b2;
}

.alert-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.card-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid #e2e8f0;
}

.footer-text {
  font-size: 14px;
  color: #718096;
  margin: 0;
}

.register-link {
  color: #667eea;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
  cursor: pointer;
}

.register-link:hover {
  color: #5a67d8;
  text-decoration: underline;
}

/* 弹窗样式 */
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
  z-index: 1000;
}

.modal-box {
  background: white;
  border-radius: 12px;
  padding: 30px;
  max-width: 400px;
  width: 90%;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
}

.modal-box h3 {
  margin: 0 0 15px 0;
  color: #1e293b;
  font-size: 20px;
}

.modal-box p {
  color: #64748b;
  margin: 0 0 10px 0;
  line-height: 1.6;
}

.tip-text {
  font-size: 13px;
  color: #94a3b8;
  margin-bottom: 20px !important;
}

.modal-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}

.btn-secondary, .btn-primary {
  flex: 1;
  padding: 10px 20px;
  border: none;
  border-radius: 8px;
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
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

@media (max-width: 480px) {
  .login-card {
    padding: 28px 20px 24px;
    width: 95%;
  }

  .title {
    font-size: 20px;
  }

  .logo {
    width: 60px;
    height: 60px;
  }

  .logo svg {
    width: 30px;
    height: 30px;
  }
  
  .card-header {
    margin-bottom: 24px;
  }
}
</style>
