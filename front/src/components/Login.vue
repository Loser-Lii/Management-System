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
@import url('https://fonts.googleapis.com/css2?family=Manrope:wght@500;600;700&display=swap');

:global(body) {
  font-family: 'Manrope', 'Segoe UI', sans-serif;
  background: #0b1221;
}

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
  background: radial-gradient(circle at 15% 20%, rgba(14, 165, 233, 0.16), transparent 35%),
              radial-gradient(circle at 80% 10%, rgba(245, 158, 11, 0.18), transparent 32%),
              linear-gradient(145deg, #0b1221 0%, #0f172a 50%, #0b1221 100%);
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
  filter: blur(90px);
  opacity: 0.7;
  animation: float 18s infinite ease-in-out;
}

.shape-1 {
  width: 520px;
  height: 520px;
  background: radial-gradient(circle, rgba(14, 165, 233, 0.55), rgba(15, 118, 178, 0.2));
  top: -12%;
  left: -14%;
  animation-delay: 0s;
}

.shape-2 {
  width: 440px;
  height: 440px;
  background: radial-gradient(circle, rgba(245, 158, 11, 0.45), rgba(251, 191, 36, 0.2));
  bottom: -14%;
  right: -10%;
  animation-delay: 2s;
}

.shape-3 {
  width: 380px;
  height: 380px;
  background: radial-gradient(circle, rgba(56, 189, 248, 0.45), rgba(59, 130, 246, 0.2));
  top: 35%;
  right: 18%;
  animation-delay: 4s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  33% {
    transform: translate(26px, -34px) scale(1.06);
  }
  66% {
    transform: translate(-22px, 18px) scale(0.94);
  }
}

.login-card {
  position: relative;
  z-index: 10;
  width: 480px;
  max-width: 90%;
  max-height: 90vh;
  background: rgba(12, 17, 28, 0.85);
  backdrop-filter: blur(20px);
  border-radius: 28px;
  padding: 42px 44px 36px;
  box-shadow: 0 24px 60px rgba(0, 0, 0, 0.5), 0 0 0 1px rgba(255, 255, 255, 0.05);
  animation: slideUp 0.6s ease-out;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.06);
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
  margin-bottom: 30px;
}

.logo-wrapper {
  display: flex;
  justify-content: center;
  margin-bottom: 18px;
}

.logo {
  width: 74px;
  height: 74px;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 40%, #f59e0b 100%);
  border-radius: 22px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #0b1221;
  box-shadow: 0 10px 28px rgba(14, 165, 233, 0.35), 0 0 0 1px rgba(255, 255, 255, 0.15);
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
  color: #e2e8f0;
  margin: 0 0 6px 0;
  letter-spacing: -0.4px;
}

.subtitle {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.login-form {
  margin-bottom: 20px;
}


.form-group {
  margin-bottom: 18px;
}

.form-label {
  display: flex;
  align-items: center;
  font-size: 14px;
  font-weight: 600;
  color: #cbd5e1;
  margin-bottom: 10px;
  gap: 8px;
}

.form-label .icon {
  width: 18px;
  height: 18px;
  color: #0ea5e9;
}

.form-input {
  width: 100%;
  padding: 14px 16px;
  font-size: 15px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.04);
  color: #e2e8f0;
  transition: all 0.25s ease;
  box-sizing: border-box;
  outline: none;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.04);
}

.form-input:focus {
  border-color: rgba(14, 165, 233, 0.9);
  box-shadow: 0 10px 28px rgba(14, 165, 233, 0.25), 0 0 0 1px rgba(14, 165, 233, 0.7);
  background: rgba(255, 255, 255, 0.06);
}

.form-input::placeholder {
  color: #94a3b8;
}

.form-input.error {
  border-color: #f87171;
  box-shadow: 0 0 0 1px rgba(248, 113, 113, 0.7);
}

.error-text {
  margin: 8px 0 0 0;
  font-size: 13px;
  color: #f87171;
  display: flex;
  align-items: center;
  gap: 4px;
}


.form-options {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 18px;
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
  accent-color: #0ea5e9;
}

.checkbox-label {
  font-size: 14px;
  color: #cbd5e1;
  user-select: none;
}

.forgot-link {
  font-size: 14px;
  color: #38bdf8;
  text-decoration: none;
  font-weight: 600;
  transition: color 0.2s;
}


.forgot-link:hover {
  color: #f59e0b;
  text-decoration: underline;
}

.submit-btn {
  width: 100%;
  padding: 14px;
  font-size: 16px;
  font-weight: 700;
  letter-spacing: 0.2px;
  color: #0b1221;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 40%, #f59e0b 100%);
  border: none;
  border-radius: 14px;
  cursor: pointer;
  transition: all 0.3s ease;
  box-shadow: 0 10px 28px rgba(14, 165, 233, 0.35);
  position: relative;
  overflow: hidden;
}

.submit-btn::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(120deg, rgba(255, 255, 255, 0.18), transparent 55%);
  opacity: 0;
  transition: opacity 0.2s ease;
}

.submit-btn:hover:not(:disabled) {
  transform: translateY(-2px) scale(1.005);
  box-shadow: 0 14px 32px rgba(14, 165, 233, 0.4);
}

.submit-btn:hover:not(:disabled)::after {
  opacity: 1;
}

.submit-btn:active:not(:disabled) {
  transform: translateY(0);
}

.submit-btn:disabled {
  opacity: 0.75;
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
  color: #0b1221;
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
  border-radius: 12px;
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
  background: rgba(248, 113, 113, 0.1);
  color: #fecdd3;
  border: 1px solid rgba(248, 113, 113, 0.35);
}

.alert-icon {
  width: 20px;
  height: 20px;
  flex-shrink: 0;
}

.card-footer {
  text-align: center;
  padding-top: 20px;
  border-top: 1px solid rgba(255, 255, 255, 0.05);
}

.footer-text {
  font-size: 14px;
  color: #94a3b8;
  margin: 0;
}

.register-link {
  color: #f59e0b;
  text-decoration: none;
  font-weight: 700;
  transition: color 0.2s;
  cursor: pointer;
}

.register-link:hover {
  color: #38bdf8;
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
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #0b1221;
}

.btn-primary:hover {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(14, 165, 233, 0.35);
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
