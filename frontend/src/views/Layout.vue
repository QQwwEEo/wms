<template>
  <div class="layout-container">
    <!-- 移动端遮罩 -->
    <div
      v-if="sidebarOpen && isMobile"
      class="sidebar-mask"
      @click="sidebarOpen = false"
    ></div>

    <!-- 侧边栏 -->
    <aside :class="['sidebar', { collapsed: !sidebarOpen, mobile: isMobile }]">
      <div class="sidebar-header">
        <div class="logo-icon">
          <el-icon size="22" color="#fff"><Box /></el-icon>
        </div>
        <span v-if="sidebarOpen" class="logo-text">WMS 系统</span>
      </div>

      <nav class="sidebar-nav">
        <!-- 仪表板 -->
        <router-link to="/dashboard" class="nav-item" active-class="active">
          <el-icon><Odometer /></el-icon>
          <span v-if="sidebarOpen">工作台</span>
        </router-link>

        <!-- 系统管理（仅管理员）-->
        <template v-if="isAdmin">
          <div v-if="sidebarOpen" class="nav-section-title">系统管理</div>
          <router-link to="/user" class="nav-item" active-class="active">
            <el-icon><User /></el-icon>
            <span v-if="sidebarOpen">用户管理</span>
          </router-link>
          <router-link to="/manager" class="nav-item" active-class="active">
            <el-icon><Avatar /></el-icon>
            <span v-if="sidebarOpen">仓管管理</span>
          </router-link>
        </template>

        <!-- 仓库管理 -->
        <template v-if="isAdmin || isManager">
          <div v-if="sidebarOpen" class="nav-section-title">仓库管理</div>
          <router-link to="/supplier" class="nav-item" active-class="active">
            <el-icon><Shop /></el-icon>
            <span v-if="sidebarOpen">供应商管理</span>
          </router-link>
          <router-link to="/warehouse" class="nav-item" active-class="active">
            <el-icon><House /></el-icon>
            <span v-if="sidebarOpen">仓库信息</span>
          </router-link>
          <router-link v-if="isAdmin" to="/material-type" class="nav-item" active-class="active">
            <el-icon><Grid /></el-icon>
            <span v-if="sidebarOpen">物资类型</span>
          </router-link>
        </template>

        <!-- 物资管理 -->
        <div v-if="sidebarOpen" class="nav-section-title">物资管理</div>
        <router-link to="/material" class="nav-item" active-class="active">
          <el-icon><Box /></el-icon>
          <span v-if="sidebarOpen">物资信息</span>
        </router-link>

        <template v-if="isAdmin || isUser">
          <router-link to="/apply" class="nav-item" active-class="active">
            <el-icon><DocumentAdd /></el-icon>
            <span v-if="sidebarOpen">物资申请</span>
          </router-link>
          <router-link to="/return" class="nav-item" active-class="active">
            <el-icon><RefreshLeft /></el-icon>
            <span v-if="sidebarOpen">物资归还</span>
          </router-link>
        </template>

        <template v-if="isAdmin || isManager">
          <router-link to="/inbound" class="nav-item" active-class="active">
            <el-icon><Download /></el-icon>
            <span v-if="sidebarOpen">入库管理</span>
          </router-link>
          <router-link to="/outbound" class="nav-item" active-class="active">
            <el-icon><Upload /></el-icon>
            <span v-if="sidebarOpen">出库管理</span>
          </router-link>
        </template>

        <!-- 个人 -->
        <div v-if="sidebarOpen" class="nav-section-title">个人</div>
        <router-link to="/profile" class="nav-item" active-class="active">
          <el-icon><Setting /></el-icon>
          <span v-if="sidebarOpen">我的信息</span>
        </router-link>
      </nav>

      <!-- 侧边栏底部 -->
      <div class="sidebar-footer">
        <div class="user-mini" @click="router.push('/profile')">
          <el-avatar :size="32" :style="{ background: avatarColor }">
            {{ userInitial }}
          </el-avatar>
          <div v-if="sidebarOpen" class="user-mini-info">
            <span class="user-mini-name">{{ userStore.userInfo?.realName || userStore.userInfo?.username }}</span>
            <el-tag :type="roleTagType" size="small">{{ roleLabel }}</el-tag>
          </div>
        </div>
      </div>
    </aside>

    <!-- 主内容区 -->
    <div class="main-container">
      <!-- 顶部导航栏 -->
      <header class="top-header">
        <div class="header-left">
          <button class="toggle-btn" @click="toggleSidebar" id="sidebar-toggle">
            <el-icon size="18"><Menu /></el-icon>
          </button>
          <!-- 面包屑 -->
          <el-breadcrumb separator="/" class="breadcrumb">
            <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
            <el-breadcrumb-item>{{ currentTitle }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="header-right">
          <el-tooltip content="工作台" placement="bottom">
            <el-button circle text @click="router.push('/dashboard')">
              <el-icon><Odometer /></el-icon>
            </el-button>
          </el-tooltip>

          <!-- 主题切换器 -->
          <el-dropdown trigger="click" @command="handleThemeChange">
            <el-tooltip content="更换主题" placement="bottom">
              <el-button circle text>
                <el-icon><Brush /></el-icon>
              </el-button>
            </el-tooltip>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="default">
                  <span class="theme-menu-item"><span class="theme-dot default"></span>默认靛蓝</span>
                </el-dropdown-item>
                <el-dropdown-item command="emerald">
                  <span class="theme-menu-item"><span class="theme-dot emerald"></span>翡翠绿色</span>
                </el-dropdown-item>
                <el-dropdown-item command="amber">
                  <span class="theme-menu-item"><span class="theme-dot amber"></span>活力橙色</span>
                </el-dropdown-item>
                <el-dropdown-item command="ocean">
                  <span class="theme-menu-item"><span class="theme-dot ocean"></span>深邃蓝色</span>
                </el-dropdown-item>
                <el-dropdown-item command="dark">
                  <span class="theme-menu-item"><span class="theme-dot dark"></span>暗黑夜色</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>

          <el-dropdown trigger="click" @command="handleCommand" id="user-dropdown">
            <div class="user-info-btn">
              <el-avatar :size="32" :style="{ background: avatarColor }">{{ userInitial }}</el-avatar>
              <span class="username-text">{{ userStore.userInfo?.realName || '用户' }}</span>
              <el-icon size="12" color="#94a3b8"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile" icon="Setting">个人信息</el-dropdown-item>
                <el-dropdown-item divided command="logout" icon="SwitchButton">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </header>

      <!-- 页面内容 -->
      <main class="main-content">
        <router-view v-slot="{ Component }">
          <transition name="page" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </main>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useUserStore } from '@/store/user'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()

const sidebarOpen = ref(true)
const isMobile = ref(false)

const isAdmin = computed(() => userStore.role === 'admin')
const isManager = computed(() => userStore.role === 'manager')
const isUser = computed(() => userStore.role === 'user')

const currentTitle = computed(() => route.meta.title || '工作台')

const userInitial = computed(() => {
  const name = userStore.userInfo?.realName || userStore.userInfo?.username || '?'
  return name.charAt(0).toUpperCase()
})

const avatarColor = computed(() => {
  const colors = { admin: '#4f46e5', manager: '#f59e0b', user: '#10b981' }
  return colors[userStore.role] || '#6366f1'
})

const roleLabel = computed(() => {
  const labels = { admin: '管理员', manager: '仓管员', user: '用户' }
  return labels[userStore.role] || '用户'
})

const roleTagType = computed(() => {
  const types = { admin: 'danger', manager: 'warning', user: 'success' }
  return types[userStore.role] || ''
})

function toggleSidebar() {
  sidebarOpen.value = !sidebarOpen.value
}

function checkMobile() {
  isMobile.value = window.innerWidth <= 768
  if (isMobile.value) sidebarOpen.value = false
  else sidebarOpen.value = true
}

async function handleCommand(cmd) {
  if (cmd === 'profile') {
    router.push('/profile')
  } else if (cmd === 'logout') {
    await ElMessageBox.confirm('确定要退出登录吗？', '提示', { type: 'warning' })
    userStore.logout()
    ElMessage.success('已退出登录')
    router.push('/login')
  }
}

const currentTheme = ref(localStorage.getItem('wms-theme') || 'default')

function handleThemeChange(theme) {
  currentTheme.value = theme
  localStorage.setItem('wms-theme', theme)
  document.documentElement.setAttribute('data-theme', theme)
}

onMounted(() => {
  checkMobile()
  window.addEventListener('resize', checkMobile)
})

onUnmounted(() => {
  window.removeEventListener('resize', checkMobile)
})

// 移动端路由跳转后关闭侧边栏
watch(route, () => {
  if (isMobile.value) sidebarOpen.value = false
})
</script>

<style scoped>
.layout-container {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar-mask {
  position: fixed;
  inset: 0;
  background: rgba(0,0,0,0.4);
  z-index: 100;
}

/* 侧边栏 */
.sidebar {
  width: var(--sidebar-width, 240px);
  background: var(--sidebar-bg);
  display: flex;
  flex-direction: column;
  transition: width 0.3s cubic-bezier(0.4,0,0.2,1);
  overflow: hidden;
  flex-shrink: 0;
  z-index: 101;
}

.sidebar.collapsed {
  width: 68px;
}

.sidebar.mobile {
  position: fixed;
  top: 0;
  left: 0;
  height: 100%;
  transform: translateX(-100%);
  transition: transform 0.3s;
}

.sidebar.mobile:not(.collapsed) {
  transform: translateX(0);
  width: 240px;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 20px 16px;
  border-bottom: 1px solid rgba(255,255,255,0.08);
  overflow: hidden;
  white-space: nowrap;
}

.logo-icon {
  width: 40px;
  height: 40px;
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(99,102,241,0.2);
}

.logo-text {
  font-size: 16px;
  font-weight: 700;
  color: #fff;
  white-space: nowrap;
}

.sidebar-nav {
  flex: 1;
  padding: 12px 10px;
  overflow-y: auto;
  overflow-x: hidden;
}

.nav-section-title {
  font-size: 10px;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  color: rgba(255,255,255,0.35);
  padding: 12px 8px 6px;
  white-space: nowrap;
}

.nav-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 10px;
  color: rgba(255,255,255,0.65);
  text-decoration: none;
  font-size: 14px;
  font-weight: 500;
  transition: all 0.2s;
  margin-bottom: 2px;
  white-space: nowrap;
}

.nav-item:hover {
  background: rgba(255,255,255,0.08);
  color: #fff;
}

.nav-item.active {
  background: linear-gradient(135deg, var(--primary-light), var(--primary));
  color: #fff;
  box-shadow: 0 4px 12px rgba(99,102,241,0.2);
}

.sidebar-footer {
  padding: 12px;
  border-top: 1px solid rgba(255,255,255,0.08);
}

.user-mini {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 8px;
  border-radius: 10px;
  cursor: pointer;
  transition: background 0.2s;
  overflow: hidden;
}

.user-mini:hover {
  background: rgba(255,255,255,0.08);
}

.user-mini-info {
  display: flex;
  flex-direction: column;
  gap: 2px;
  overflow: hidden;
}

.user-mini-name {
  font-size: 13px;
  font-weight: 600;
  color: #fff;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 主容器 */
.main-container {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
  min-width: 0;
}

.top-header {
  height: 64px;
  background: var(--bg-card);
  color: var(--text-primary);
  border-bottom: 1px solid var(--border-color, #e2e8f0);
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  flex-shrink: 0;
  box-shadow: 0 1px 4px rgba(0,0,0,0.06);
  z-index: 10;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 16px;
}

.toggle-btn {
  width: 36px;
  height: 36px;
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  background: transparent;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #64748b;
  transition: all 0.2s;
}

.toggle-btn:hover {
  background: var(--bg-base);
  color: var(--primary);
  border-color: var(--primary-light);
}

.breadcrumb {
  font-size: 13px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  cursor: pointer;
  padding: 6px 12px;
  border-radius: 8px;
  transition: background 0.2s;
}

.user-info-btn:hover {
  background: var(--bg-base);
}

.username-text {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
}

.main-content {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: var(--bg-base, #f1f5f9);
}

@media (max-width: 768px) {
  .main-content {
    padding: 16px;
  }
  .username-text { display: none; }
  .breadcrumb { display: none; }
}

/* 主题选择菜单样式 */
.theme-menu-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
}
.theme-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  border: 1px solid rgba(0,0,0,0.1);
  display: inline-block;
}
.theme-dot.default { background: #4f46e5; }
.theme-dot.emerald { background: #10b981; }
.theme-dot.amber { background: #f97316; }
.theme-dot.ocean { background: #0284c7; }
.theme-dot.dark { background: #1e293b; }
</style>
