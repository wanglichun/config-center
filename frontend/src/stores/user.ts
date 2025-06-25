import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken } from '@/utils/auth'
import type { LoginForm, UserInfo } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(null)
  const loading = ref(false)

  // 登录
  const userLogin = async (loginForm: LoginForm) => {
    loading.value = true
    try {
      const response = await login(loginForm)
      if (response.success) {
        token.value = response.data.token
        setToken(response.data.token)
        await getUserInfo()
        return Promise.resolve(response)
      } else {
        return Promise.reject(new Error(response.message))
      }
    } catch (error) {
      return Promise.reject(error)
    } finally {
      loading.value = false
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const response = await getUserInfo()
      if (response.success) {
        userInfo.value = response.data
        return Promise.resolve(response.data)
      } else {
        return Promise.reject(new Error(response.message))
      }
    } catch (error) {
      return Promise.reject(error)
    }
  }

  // 登出
  const logout = () => {
    token.value = ''
    userInfo.value = null
    removeToken()
  }

  // 初始化用户信息
  const initUserInfo = async () => {
    if (token.value && !userInfo.value) {
      try {
        await getUserInfo()
      } catch (error) {
        console.error('初始化用户信息失败:', error)
        logout()
      }
    }
  }

  return {
    token,
    userInfo,
    loading,
    userLogin,
    getUserInfo,
    logout,
    initUserInfo
  }
}) 