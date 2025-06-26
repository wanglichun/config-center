import Cookies from 'js-cookie'
import type { UserInfo } from '@/types/user'

const TOKEN_KEY = 'config-center-token'
const USER_INFO_KEY = 'config-center-user-info'
const TOKEN_EXPIRES = 7 // 7天

/**
 * 获取Token
 */
export function getToken(): string | undefined {
  return Cookies.get(TOKEN_KEY)
}

/**
 * 设置Token
 */
export function setToken(token: string): void {
  Cookies.set(TOKEN_KEY, token, { expires: TOKEN_EXPIRES })
}

/**
 * 移除Token
 */
export function removeToken(): void {
  Cookies.remove(TOKEN_KEY)
}

/**
 * 获取用户信息
 */
export function getUserInfo(): UserInfo | null {
  const userInfoStr = localStorage.getItem(USER_INFO_KEY)
  if (userInfoStr) {
    try {
      return JSON.parse(userInfoStr)
    } catch (error) {
      console.error('解析用户信息失败:', error)
      removeUserInfo()
      return null
    }
  }
  return null
}

/**
 * 设置用户信息
 */
export function setUserInfo(userInfo: UserInfo): void {
  localStorage.setItem(USER_INFO_KEY, JSON.stringify(userInfo))
}

/**
 * 移除用户信息
 */
export function removeUserInfo(): void {
  localStorage.removeItem(USER_INFO_KEY)
}

/**
 * 检查是否已登录
 */
export function isLoggedIn(): boolean {
  return !!getToken()
} 