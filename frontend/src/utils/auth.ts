import Cookies from 'js-cookie'

const TOKEN_KEY = 'config-center-token'
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
 * 检查是否已登录
 */
export function isLoggedIn(): boolean {
  return !!getToken()
} 