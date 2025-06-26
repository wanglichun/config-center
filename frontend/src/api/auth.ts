import { request } from '@/utils/request'
import type { LoginForm, LoginResponse, UserInfo } from '@/types/user'
import type { ApiResult } from '@/types/common'

/**
 * 用户登录
 */
export function login(data: LoginForm): Promise<ApiResult<LoginResponse>> {
  return request.post('/auth/login', data)
}

/**
 * 用户登出
 */
export function logout(): Promise<ApiResult<string>> {
  return request.post('/auth/logout')
}

/**
 * 获取用户信息
 */
export function getUserInfo(): Promise<ApiResult<UserInfo>> {
  return request.get('/auth/userinfo')
}

/**
 * 刷新Token
 */
export function refreshToken(): Promise<ApiResult<{ token: string }>> {
  return request.post('/auth/refresh')
} 