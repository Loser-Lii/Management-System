<template>
  <div class="register-container">
    <div class="register-card">
      <h2 class="register-title">用户注册</h2>
      
      <div class="role-selector">
        <button 
          :class="['role-btn', {active: form.role === 'salesman'}]" 
          @click="form.role = 'salesman'"
        >
          销售员注册
        </button>
        <button 
          :class="['role-btn', {active: form.role === 'admin'}]" 
          @click="form.role = 'admin'"
        >
          管理员注册
        </button>
      </div>

      <form @submit.prevent="handleRegister" class="register-form">
        <div class="form-group">
          <label>用户名</label>
          <input v-model="form.username" type="text" placeholder="请输入用户名" required />
        </div>

        <div class="form-group">
          <label>密码</label>
          <input v-model="form.password" type="password" placeholder="请输入密码" required />
        </div>

        <div v-if="form.role === 'salesman'" class="form-group">
          <label>真实姓名</label>
          <input v-model="form.name" type="text" placeholder="请输入您的真实姓名" required />
        </div>

        <div v-if="form.role === 'admin'" class="form-group">
          <label>管理员注册密钥</label>
          <input v-model="form.adminKey" type="password" placeholder="请输入管理员注册密钥" required />
          <p class="hint">密钥: RHODESISLAND2024</p>
        </div>

        <button type="submit" class="register-btn">注册</button>
      </form>

      <div class="register-footer">
        已有账号？<a @click="$emit('to-login')" class="link">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { reactive } from 'vue'
import axios from 'axios'

const form = reactive({
  username: '',
  password: '',
  role: 'salesman',  // 默认销售员
  name: '',
  adminKey: ''
})

async function handleRegister() {
  try {
    const response = await axios.post('http://localhost:8080/api/auth/register', form)
    
    if (response.data.code === 200) {
      alert(response.data.message || '注册成功！请登录')
      // 触发切换到登录页面
      emit('to-login')
    } else {
      alert(response.data.message || '注册失败')
    }
  } catch (error) {
    console.error('注册失败:', error)
    alert(error.response?.data?.message || '注册失败，请稍后重试')
  }
}

const emit = defineEmits(['to-login'])
</script>

<style scoped>
.register-container {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: radial-gradient(circle at 15% 20%, rgba(14, 165, 233, 0.16), transparent 35%),
              radial-gradient(circle at 80% 10%, rgba(245, 158, 11, 0.18), transparent 32%),
              linear-gradient(145deg, #0b1221 0%, #0f172a 50%, #0b1221 100%);
  padding: 20px;
  z-index: 999;
}

.register-card {
  background: rgba(12, 17, 28, 0.86);
  border-radius: 16px;
  padding: 40px;
  width: 100%;
  max-width: 450px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.4), 0 0 0 1px rgba(255, 255, 255, 0.06);
}

.register-title {
  text-align: center;
  font-size: 28px;
  font-weight: 700;
  color: #e2e8f0;
  margin-bottom: 30px;
}

.role-selector {
  display: flex;
  gap: 12px;
  margin-bottom: 30px;
}

.role-btn {
  flex: 1;
  padding: 12px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  background: rgba(255, 255, 255, 0.04);
  border-radius: 10px;
  font-size: 15px;
  font-weight: 600;
  color: #cbd5e1;
  cursor: pointer;
  transition: all 0.2s;
}

.role-btn:hover {
  border-color: rgba(14, 165, 233, 0.8);
  color: #0ea5e9;
  box-shadow: 0 8px 20px rgba(14, 165, 233, 0.24);
}

.role-btn.active {
  border-color: rgba(14, 165, 233, 0.9);
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #0b1221;
  box-shadow: 0 12px 28px rgba(14, 165, 233, 0.28);
}

.register-form {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.form-group {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.form-group label {
  font-size: 14px;
  font-weight: 600;
  color: #cbd5e1;
}

.form-group input {
  padding: 12px 16px;
  border: 1px solid rgba(148, 163, 184, 0.35);
  border-radius: 10px;
  font-size: 15px;
  transition: all 0.2s;
  background: rgba(255, 255, 255, 0.04);
  color: #e2e8f0;
}

.form-group input:focus {
  outline: none;
  border-color: rgba(14, 165, 233, 0.9);
  box-shadow: 0 10px 28px rgba(14, 165, 233, 0.25), 0 0 0 1px rgba(14, 165, 233, 0.7);
  background: rgba(255, 255, 255, 0.06);
}

.hint {
  font-size: 12px;
  color: #94a3b8;
  margin: 0;
}

.register-btn {
  padding: 14px;
  background: linear-gradient(135deg, #0ea5e9 0%, #22d3ee 45%, #f59e0b 100%);
  color: #0b1221;
  border: none;
  border-radius: 12px;
  font-size: 16px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.3s;
  margin-top: 10px;
  box-shadow: 0 10px 28px rgba(14, 165, 233, 0.32);
}

.register-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 30px rgba(14, 165, 233, 0.4);
}

.register-footer {
  text-align: center;
  margin-top: 24px;
  font-size: 14px;
  color: #94a3b8;
}

.link {
  color: #f59e0b;
  font-weight: 700;
  cursor: pointer;
  text-decoration: none;
}

.link:hover {
  text-decoration: underline;
  color: #22d3ee;
}
</style>
