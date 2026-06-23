<template>
  <div class="login-container">
    <!-- 背景装饰 -->
    <div class="bg-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>

    <div class="login-wrapper">
      <!-- 左侧品牌区 -->
      <div class="login-brand">
        <div class="brand-logo">
          <el-icon size="48" color="#fff"><Box /></el-icon>
        </div>
        <h1 class="brand-title">WMS 仓库管理系统</h1>
        <p class="brand-subtitle">高效 · 智能 · 全流程仓储管理平台</p>
        <div class="brand-features">
          <div class="feature-item" v-for="feat in features" :key="feat.text">
            <el-icon :color="feat.color"><component :is="feat.icon" /></el-icon>
            <span>{{ feat.text }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧登录区 -->
      <div class="login-form-card">
        <div class="form-header">
          <h2>欢迎回来</h2>
          <p>请登录您的账号</p>
        </div>

        <el-form
          ref="formRef"
          :model="form"
          :rules="rules"
          class="login-form"
          @keyup.enter="handleLogin"
        >
          <el-form-item prop="username">
            <el-input
              v-model="form.username"
              placeholder="请输入用户名"
              size="large"
              prefix-icon="User"
              id="login-username"
            />
          </el-form-item>

          <el-form-item prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="请输入密码"
              size="large"
              prefix-icon="Lock"
              show-password
              id="login-password"
            />
          </el-form-item>

          <div class="demo-accounts">
            <span class="demo-label">演示账号：</span>
            <el-tag
              v-for="acc in demoAccounts"
              :key="acc.username"
              :type="acc.type"
              class="demo-tag"
              @click="fillDemo(acc)"
            >{{ acc.label }}</el-tag>
          </div>

          <el-button
            id="login-btn"
            type="primary"
            size="large"
            class="login-btn"
            :loading="loading"
            @click="handleLogin"
          >
            <el-icon v-if="!loading"><Right /></el-icon>
            {{ loading ? '登录中...' : '立即登录' }}
          </el-button>
        </el-form>

        <p class="login-footer">© 2024 WMS 仓库管理系统</p>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { authApi } from '@/api'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const formRef = ref(null)
const loading = ref(false)

const form = ref({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const features = [
  { icon: 'Box', text: '物资全生命周期管理', color: '#a5f3fc' },
  { icon: 'TrendCharts', text: '实时库存数据看板', color: '#86efac' },
  { icon: 'UserFilled', text: '多角色权限管控', color: '#fca5a5' },
  { icon: 'Iphone', text: 'PC + 移动端响应式', color: '#c4b5fd' }
]

const demoAccounts = [
  { username: 'admin', password: '123456', label: '管理员', type: 'danger' },
  { username: 'manager', password: '123456', label: '仓管员', type: 'warning' },
  { username: 'user1', password: '123456', label: '普通用户', type: 'success' }
]

function fillDemo(acc) {
  form.value.username = acc.username
  form.value.password = acc.password
}

async function handleLogin() {
  try {
    await formRef.value.validate()
    loading.value = true
    const res = await authApi.login(form.value)
    userStore.setToken(res.data.token)
    userStore.setUserInfo({
      userId: res.data.userId,
      username: res.data.username,
      realName: res.data.realName,
      role: res.data.role
    })
    ElMessage.success(`欢迎回来，${res.data.realName || res.data.username}！`)
    router.push('/dashboard')
  } catch (e) {
    // 错误已在拦截器处理
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #1e1b4b 0%, #312e81 40%, #4c1d95 70%, #1e3a5f 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  overflow: hidden;
}

.bg-decoration {
  position: absolute;
  inset: 0;
  pointer-events: none;
}

.circle {
  position: absolute;
  border-radius: 50%;
  filter: blur(60px);
  opacity: 0.25;
}

.circle-1 {
  width: 400px;
  height: 400px;
  background: #6366f1;
  top: -100px;
  left: -100px;
  animation: float1 8s ease-in-out infinite;
}

.circle-2 {
  width: 350px;
  height: 350px;
  background: #8b5cf6;
  bottom: -80px;
  right: 10%;
  animation: float2 10s ease-in-out infinite;
}

.circle-3 {
  width: 250px;
  height: 250px;
  background: #0ea5e9;
  top: 40%;
  right: 30%;
  animation: float1 12s ease-in-out infinite reverse;
}

@keyframes float1 {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(-30px) scale(1.05); }
}

@keyframes float2 {
  0%, 100% { transform: translateY(0) scale(1); }
  50% { transform: translateY(20px) scale(0.95); }
}

.login-wrapper {
  display: flex;
  width: 920px;
  max-width: 95vw;
  min-height: 580px;
  border-radius: 24px;
  overflow: hidden;
  box-shadow: 0 25px 60px rgba(0,0,0,0.4);
  position: relative;
  z-index: 1;
}

/* 左侧品牌 */
.login-brand {
  flex: 1;
  background: linear-gradient(160deg, rgba(255,255,255,0.08) 0%, rgba(255,255,255,0.02) 100%);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(255,255,255,0.1);
  border-right: none;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
  color: #fff;
}

.brand-logo {
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #6366f1, #8b5cf6);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 24px;
  box-shadow: 0 8px 24px rgba(99,102,241,0.4);
}

.brand-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 8px;
  background: linear-gradient(90deg, #fff, #c7d2fe);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.brand-subtitle {
  font-size: 14px;
  color: rgba(255,255,255,0.6);
  margin-bottom: 40px;
}

.brand-features {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.feature-item {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  color: rgba(255,255,255,0.8);
}

/* 右侧表单 */
.login-form-card {
  width: 380px;
  background: #fff;
  padding: 48px 40px;
  display: flex;
  flex-direction: column;
  justify-content: center;
}

.form-header {
  margin-bottom: 32px;
}

.form-header h2 {
  font-size: 26px;
  font-weight: 700;
  color: #1e293b;
  margin-bottom: 6px;
}

.form-header p {
  color: #64748b;
  font-size: 14px;
}

.login-form {
  flex: 1;
}

.login-form :deep(.el-form-item) {
  margin-bottom: 18px;
}

.login-form :deep(.el-input__wrapper) {
  border-radius: 10px;
  box-shadow: 0 0 0 1px #e2e8f0;
  padding: 4px 12px;
  transition: all 0.2s;
}

.login-form :deep(.el-input__wrapper:hover) {
  box-shadow: 0 0 0 1px #6366f1;
}

.login-form :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(99,102,241,0.3), 0 0 0 1px #6366f1;
}

.demo-accounts {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.demo-label {
  font-size: 12px;
  color: #94a3b8;
}

.demo-tag {
  cursor: pointer;
  border-radius: 6px;
  transition: all 0.2s;
}

.demo-tag:hover {
  transform: translateY(-1px);
  box-shadow: 0 2px 8px rgba(0,0,0,0.15);
}

.login-btn {
  width: 100%;
  border-radius: 10px;
  height: 44px;
  font-size: 15px;
  font-weight: 600;
  background: linear-gradient(135deg, #4f46e5, #7c3aed) !important;
  border: none !important;
  transition: all 0.3s;
}

.login-btn:hover {
  transform: translateY(-1px);
  box-shadow: 0 8px 20px rgba(99,102,241,0.4) !important;
}

.login-footer {
  text-align: center;
  font-size: 12px;
  color: #cbd5e1;
  margin-top: 24px;
}

/* 移动端 */
@media (max-width: 768px) {
  .login-wrapper {
    flex-direction: column;
    width: 100%;
    min-height: 100vh;
    border-radius: 0;
  }

  .login-brand {
    padding: 32px 24px;
    min-height: auto;
    border-right: 1px solid rgba(255,255,255,0.1);
    border-bottom: 1px solid rgba(255,255,255,0.1);
  }

  .login-form-card {
    width: 100%;
    padding: 32px 24px;
  }

  .brand-features { display: none; }
}
</style>
