import Cookies from 'js-cookie'

const USER_EMAIL_KEY = 'USER_EMAIL'
const USER_EMAIL_EXPIRES = 7 // 7天

/**
 * 设置用户邮箱到cookie
 * @param email 用户邮箱
 */
export function setUserEmail(email: string): void {
  Cookies.set(USER_EMAIL_KEY, email, { 
    expires: USER_EMAIL_EXPIRES,
    secure: process.env.NODE_ENV === 'production', // 生产环境使用HTTPS
    sameSite: 'strict' // 防止CSRF攻击
  })
}

/**
 * 从cookie获取用户邮箱
 * @returns 用户邮箱或undefined
 */
export function getUserEmail(): string | undefined {
  return Cookies.get(USER_EMAIL_KEY)
}

/**
 * 从cookie移除用户邮箱
 */
export function removeUserEmail(): void {
  Cookies.remove(USER_EMAIL_KEY)
}

/**
 * 检查是否存在用户邮箱cookie
 * @returns 是否存在
 */
export function hasUserEmail(): boolean {
  return !!getUserEmail()
}

/**
 * 获取所有cookie信息（用于调试）
 * @returns cookie对象
 */
export function getAllCookies(): Record<string, string> {
  return Cookies.get()
} 