import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/store/user'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'Login',
      component: () => import('@/views/Login.vue'),
      meta: { title: '登录 - WMS仓库管理系统', public: true }
    },
    {
      path: '/',
      component: () => import('@/views/Layout.vue'),
      meta: { requiresAuth: true },
      children: [
        {
          path: '',
          redirect: '/dashboard'
        },
        {
          path: 'dashboard',
          name: 'Dashboard',
          component: () => import('@/views/Dashboard.vue'),
          meta: { title: '工作台', icon: 'Odometer' }
        },
        // 管理员功能
        {
          path: 'user',
          name: 'User',
          component: () => import('@/views/user/UserList.vue'),
          meta: { title: '用户管理', icon: 'User', roles: ['admin'] }
        },
        {
          path: 'manager',
          name: 'Manager',
          component: () => import('@/views/user/ManagerList.vue'),
          meta: { title: '仓管管理', icon: 'Avatar', roles: ['admin'] }
        },
        // 共有功能
        {
          path: 'supplier',
          name: 'Supplier',
          component: () => import('@/views/supplier/SupplierList.vue'),
          meta: { title: '供应商管理', icon: 'Shop', roles: ['admin', 'manager'] }
        },
        {
          path: 'warehouse',
          name: 'Warehouse',
          component: () => import('@/views/warehouse/WarehouseList.vue'),
          meta: { title: '仓库信息管理', icon: 'House', roles: ['admin', 'manager'] }
        },
        {
          path: 'material-type',
          name: 'MaterialType',
          component: () => import('@/views/material/MaterialTypeList.vue'),
          meta: { title: '物资类型管理', icon: 'Grid', roles: ['admin'] }
        },
        {
          path: 'material',
          name: 'Material',
          component: () => import('@/views/material/MaterialList.vue'),
          meta: { title: '物资信息管理', icon: 'Box', roles: ['admin', 'manager', 'user'] }
        },
        {
          path: 'apply',
          name: 'Apply',
          component: () => import('@/views/apply/ApplyList.vue'),
          meta: { title: '物资申请管理', icon: 'DocumentAdd', roles: ['admin', 'user'] }
        },
        {
          path: 'return',
          name: 'Return',
          component: () => import('@/views/apply/ReturnList.vue'),
          meta: { title: '物资归还管理', icon: 'RefreshLeft', roles: ['admin', 'user'] }
        },
        {
          path: 'inbound',
          name: 'Inbound',
          component: () => import('@/views/inbound/InboundList.vue'),
          meta: { title: '入库信息管理', icon: 'Download', roles: ['admin', 'manager'] }
        },
        {
          path: 'outbound',
          name: 'Outbound',
          component: () => import('@/views/outbound/OutboundList.vue'),
          meta: { title: '出库信息管理', icon: 'Upload', roles: ['admin', 'manager'] }
        },
        // 个人信息
        {
          path: 'profile',
          name: 'Profile',
          component: () => import('@/views/Profile.vue'),
          meta: { title: '我的信息', icon: 'Setting' }
        }
      ]
    },
    { path: '/:pathMatch(.*)*', redirect: '/' }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  document.title = to.meta.title ? `${to.meta.title} - WMS` : 'WMS仓库管理系统'

  const token = localStorage.getItem('wms_token')
  const userInfo = JSON.parse(localStorage.getItem('wms_user') || 'null')

  if (to.meta.public) {
    next()
    return
  }

  if (!token) {
    next('/login')
    return
  }

  // 角色权限检查
  if (to.meta.roles && userInfo) {
    if (!to.meta.roles.includes(userInfo.role)) {
      next('/dashboard')
      return
    }
  }

  next()
})

export default router
