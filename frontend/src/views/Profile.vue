<template>
  <div class="profile-page">
    <div class="profile-grid">
      <!-- 左侧个人信息卡 -->
      <div class="wms-card profile-card">
        <div class="profile-avatar-wrap">
          <div class="profile-avatar" :style="{ background: avatarColor }">
            {{ userInitial }}
          </div>
          <div>
            <h3 class="profile-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</h3>
            <el-tag :type="roleTagType" effect="dark">{{ roleLabel }}</el-tag>
          </div>
        </div>

        <el-divider />

        <div class="info-list">
          <div class="info-item">
            <el-icon color="#4f46e5"><User /></el-icon>
            <span class="info-label">用户名</span>
            <span class="info-value">{{ userStore.userInfo?.username }}</span>
          </div>
          <div class="info-item">
            <el-icon color="#10b981"><Iphone /></el-icon>
            <span class="info-label">手机号</span>
            <span class="info-value">{{ form.phone || '未填写' }}</span>
          </div>
          <div class="info-item">
            <el-icon color="#f59e0b"><Message /></el-icon>
            <span class="info-label">邮箱</span>
            <span class="info-value">{{ form.email || '未填写' }}</span>
          </div>
        </div>
      </div>

      <!-- 右侧编辑区 -->
      <div class="wms-card">
        <div class="page-header">
          <h2 class="page-title">编辑个人信息</h2>
        </div>

        <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" style="max-width:480px">
          <el-form-item label="真实姓名" prop="realName">
            <el-input v-model="form.realName" placeholder="请输入真实姓名" id="profile-realName" />
          </el-form-item>
          <el-form-item label="手机号" prop="phone">
            <el-input v-model="form.phone" placeholder="请输入手机号" id="profile-phone" />
          </el-form-item>
          <el-form-item label="邮箱" prop="email">
            <el-input v-model="form.email" placeholder="请输入邮箱" id="profile-email" />
          </el-form-item>

          <el-divider>修改密码（选填）</el-divider>

          <el-form-item label="新密码" prop="password">
            <el-input
              v-model="form.password"
              type="password"
              placeholder="不修改请留空"
              show-password
              id="profile-password"
            />
          </el-form-item>
          <el-form-item label="确认密码" prop="confirmPassword">
            <el-input
              v-model="form.confirmPassword"
              type="password"
              placeholder="再次输入新密码"
              show-password
              id="profile-confirmPassword"
            />
          </el-form-item>

          <el-form-item>
            <el-button type="primary" :loading="loading" @click="handleSubmit" id="btn-save-profile">
              保存修改
            </el-button>
            <el-button @click="resetForm">重置</el-button>
          </el-form-item>
        </el-form>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'
import { userApi, authApi } from '@/api'

const userStore = useUserStore()
const formRef = ref(null)
const loading = ref(false)

const form = ref({
  realName: '',
  phone: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  email: [{ type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }],
  confirmPassword: [
    {
      validator: (rule, value, callback) => {
        if (form.value.password && value !== form.value.password) {
          callback(new Error('两次密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const userInitial = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || '?'
  return name.charAt(0).toUpperCase()
})

const avatarColor = computed(() => {
  const colors = { admin: '#4f46e5', manager: '#f59e0b', user: '#10b981' }
  return colors[userStore.role] || '#6366f1'
})

const roleLabel = computed(() => {
  const labels = { admin: '系统管理员', manager: '仓库管理员', user: '普通用户' }
  return labels[userStore.role] || '用户'
})

const roleTagType = computed(() => {
  const types = { admin: 'danger', manager: 'warning', user: 'success' }
  return types[userStore.role] || ''
})

function initForm() {
  const info = userStore.userInfo
  if (info) {
    form.value.realName = info.realName || ''
    form.value.phone = info.phone || ''
    form.value.email = info.email || ''
  }
}

function resetForm() {
  initForm()
  form.value.password = ''
  form.value.confirmPassword = ''
}

async function handleSubmit() {
  try {
    await formRef.value.validate()
    loading.value = true
    const data = {
      realName: form.value.realName,
      phone: form.value.phone,
      email: form.value.email
    }
    if (form.value.password) {
      data.password = form.value.password
    }
    await userApi.updateInfo(data)
    // 刷新用户信息
    await userStore.fetchUserInfo()
    initForm()
    form.value.password = ''
    form.value.confirmPassword = ''
    ElMessage.success('个人信息更新成功')
  } catch (e) {
    // error handled in interceptor
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  try {
    await userStore.fetchUserInfo()
    initForm()
  } catch {
    initForm()
  }
})
</script>

<style scoped>
.profile-page { max-width: 1000px; }

.profile-grid {
  display: grid;
  grid-template-columns: 280px 1fr;
  gap: 20px;
}

.profile-card {
  height: fit-content;
}

.profile-avatar-wrap {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  text-align: center;
  padding-bottom: 4px;
}

.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
  font-weight: 700;
  color: #fff;
  box-shadow: 0 8px 20px rgba(0,0,0,0.15);
}

.profile-name {
  font-size: 18px;
  font-weight: 700;
  color: #1e293b;
}

.info-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.info-label {
  font-size: 12px;
  color: #94a3b8;
  width: 48px;
  flex-shrink: 0;
}

.info-value {
  font-size: 13px;
  color: #374151;
  font-weight: 500;
}

@media (max-width: 768px) {
  .profile-grid {
    grid-template-columns: 1fr;
  }
}
</style>
