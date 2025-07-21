import { defineStore } from 'pinia'
import { ref } from 'vue'
import { login, getUserInfo as fetchUserInfo } from '@/api/auth'
import { getToken, setToken, removeToken, getUserInfo as getStoredUserInfo, setUserInfo, removeUserInfo } from '@/utils/auth'
import { setUserEmail, getUserEmail, removeUserEmail } from '@/utils/cookie'
import type { LoginForm, UserInfo } from '@/types/user'

export const useUserStore = defineStore('user', () => {
  const token = ref<string>(getToken() || '')
  const userInfo = ref<UserInfo | null>(getStoredUserInfo())
  const loading = ref(false)

  // 登录
  const userLogin = async (loginForm: LoginForm) => {
    loading.value = true
    try {
      console.log('发送登录请求:', loginForm)
      const response = await login(loginForm)
      console.log('登录响应:', response)
      
      if (response.success) {
        token.value = response.data.token
        userInfo.value = response.data.userInfo
        setToken(response.data.token)
        setUserInfo(response.data.userInfo)
        
        // 将用户邮箱保存到cookie中
        if (response.data.userInfo.email) {
          setUserEmail(response.data.userInfo.email)
          console.log('用户邮箱已保存到cookie:', response.data.userInfo.email)
        }
        
        console.log('登录成功，token已保存:', response.data.token)
        return Promise.resolve(response)
      } else {
        console.error('登录失败:', response.message)
        return Promise.reject(new Error(response.message))
      }
    } catch (error: any) {
      console.error('登录请求异常:', error)
      // 如果是网络错误或其他异常，重新抛出
      if (error.response) {
        return Promise.reject(error)
      } else {
        return Promise.reject(new Error('网络连接失败，请检查网络'))
      }
    } finally {
      loading.value = false
    }
  }

  // 获取用户信息
  const getUserInfo = async () => {
    try {
      const response = await fetchUserInfo()
      if (response.success) {
        userInfo.value = response.data
        // 更新cookie中的邮箱信息
        if (response.data.email) {
          setUserEmail(response.data.email)
        }
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
    removeUserInfo()
    // 清除cookie中的邮箱信息
    removeUserEmail()
    console.log('用户邮箱已从cookie中清除')
  }

  // 初始化用户信息（从本地存储恢复）
  const initUserInfo = () => {
    // 由于登录时已经获取了用户信息，这里只需要在应用启动时检查token
    if (!token.value) {
      logout()
    }
  }

  // 获取cookie中的用户邮箱
  const getStoredUserEmail = (): string | undefined => {
    return getUserEmail()
  }

  return {
    token,
    userInfo,
    loading,
    userLogin,
    getUserInfo,
    logout,
    initUserInfo,
    getUserEmail: getStoredUserEmail
  }
}) 