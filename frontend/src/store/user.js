import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { authApi } from '@/api'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('wms_token') || '')
  const userInfo = ref(JSON.parse(localStorage.getItem('wms_user') || 'null'))

  const isLoggedIn = computed(() => !!token.value)
  const role = computed(() => userInfo.value?.role || '')
  const isAdmin = computed(() => role.value === 'admin')
  const isManager = computed(() => role.value === 'manager')
  const isUser = computed(() => role.value === 'user')

  function setToken(t) {
    token.value = t
    localStorage.setItem('wms_token', t)
  }

  function setUserInfo(info) {
    userInfo.value = info
    localStorage.setItem('wms_user', JSON.stringify(info))
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('wms_token')
    localStorage.removeItem('wms_user')
  }

  async function fetchUserInfo() {
    try {
      const res = await authApi.getInfo()
      setUserInfo(res.data)
      return res.data
    } catch {
      logout()
    }
  }

  return { token, userInfo, isLoggedIn, role, isAdmin, isManager, isUser,
           setToken, setUserInfo, logout, fetchUserInfo }
})
