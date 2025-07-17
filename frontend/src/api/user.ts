import { request } from '@/utils/request'
import type { ApiResult, PageResult } from '@/types/common'
import type { UserInfo } from '@/types/user'
import axios from 'axios'
import { getToken } from '@/utils/auth'

// 用户查询参数
export interface UserQueryParams {
  page?: number
  size?: number
  keyword?: string
  role?: string
  status?: string
  department?: string
  startTime?: string
  endTime?: string
}

// 用户创建请求
export interface UserCreateRequest {
  username: string
  password: string
  realName: string
  email: string
  phone?: string
  department?: string
  role: string
  status?: string
  avatar?: string
  remark?: string
}

// 用户更新请求
export interface UserUpdateRequest {
  id: number
  realName: string
  email: string
  phone?: string
  department?: string
  role: string
  status: string
  avatar?: string
  remark?: string
}

// 密码修改请求
export interface PasswordChangeRequest {
  oldPassword: string
  newPassword: string
  confirmPassword: string
}

// 用户统计信息
export interface UserStatistics {
  totalUsers: number
  activeUsers: number
  inactiveUsers: number
  lockedUsers: number
  adminUsers: number
  developerUsers: number
  viewerUsers: number
}

/**
 * 分页查询用户列表
 */
export function getUserList(params: UserQueryParams): Promise<ApiResult<PageResult<UserInfo>>> {
  const token = getToken()
  const headers = token ? { Authorization: `Bearer ${token}` } : {}
  
  return axios.get('/users/list', {
    params,
    headers,
    baseURL: (import.meta as any).env?.VITE_API_BASE_URL || '/api'
  }).then(response => response.data)
}

/**
 * 根据ID查询用户详情
 */
export function getUserById(id: number): Promise<ApiResult<UserInfo>> {
  return request.get(`/users/${id}`)
}

/**
 * 创建用户
 */
export function createUser(data: UserCreateRequest): Promise<ApiResult<UserInfo>> {
  return request.post('/users/create', data)
}

/**
 * 更新用户信息
 */
export function updateUser(data: UserUpdateRequest): Promise<ApiResult<UserInfo>> {
  return request.put('/users/update', data)
}

/**
 * 删除用户
 */
export function deleteUser(id: number): Promise<ApiResult<string>> {
  return request.delete(`/users/${id}`)
}

/**
 * 批量删除用户
 */
export function deleteUsers(ids: number[]): Promise<ApiResult<string>> {
  return request.delete('/users/batch', { data: ids })
}

/**
 * 启用/禁用用户
 */
export function toggleUserStatus(id: number, status: string): Promise<ApiResult<string>> {
  return request.put(`/users/${id}/status?status=${status}`)
}

/**
 * 重置用户密码
 */
export function resetUserPassword(id: number, newPassword: string): Promise<ApiResult<string>> {
  return request.put(`/users/${id}/reset-password?newPassword=${encodeURIComponent(newPassword)}`)
}

/**
 * 修改密码
 */
export function changePassword(data: PasswordChangeRequest): Promise<ApiResult<string>> {
  return request.put('/users/change-password', data)
}

/**
 * 检查用户名是否存在
 */
export function checkUsername(username: string): Promise<ApiResult<boolean>> {
  return request.get('/users/check-username', { params: { username } })
}

/**
 * 检查邮箱是否存在
 */
export function checkEmail(email: string): Promise<ApiResult<boolean>> {
  return request.get('/users/check-email', { params: { email } })
}

/**
 * 获取用户统计信息
 */
export function getUserStatistics(): Promise<ApiResult<UserStatistics>> {
  return request.get('/users/statistics')
}

/**
 * 获取当前用户信息
 */
export function getCurrentUserProfile(): Promise<ApiResult<UserInfo>> {
  return request.get('/users/profile')
}

/**
 * 更新当前用户信息
 */
export function updateCurrentUserProfile(data: UserUpdateRequest): Promise<ApiResult<UserInfo>> {
  return request.put('/users/profile', data)
} 