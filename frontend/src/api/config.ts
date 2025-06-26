import { request } from '@/utils/request'
import type { ApiResult, PageResult } from '@/types/common'
import type { ConfigItem, ConfigQuery, ConfigForm } from '@/types/config'

/**
 * 分页查询配置列表
 */
export function getConfigPage(params: ConfigQuery): Promise<ApiResult<PageResult<ConfigItem>>> {
  return request.get('/config/page', params)
}

/**
 * 获取单个配置
 */
export function getConfig(params: {
  appName: string
  environment: string
  groupName: string
  configKey: string
}): Promise<ApiResult<ConfigItem>> {
  return request.get('/config', params)
}

/**
 * 获取应用的所有配置
 */
export function getConfigs(appName: string, environment: string): Promise<ApiResult<ConfigItem[]>> {
  return request.get('/config/list', { appName, environment })
}

/**
 * 创建配置
 */
export function createConfig(data: ConfigForm): Promise<ApiResult<boolean>> {
  return request.post('/config', data)
}

/**
 * 更新配置
 */
export function updateConfig(id: number, data: ConfigForm): Promise<ApiResult<boolean>> {
  return request.put(`/config/${id}`, data)
}

/**
 * 删除配置
 */
export function deleteConfig(id: number): Promise<ApiResult<boolean>> {
  return request.delete(`/config/${id}`)
}

/**
 * 发布配置
 */
export function publishConfig(id: number): Promise<ApiResult<boolean>> {
  return request.post(`/config/${id}/publish`)
}

/**
 * 批量发布配置
 */
export function publishConfigs(appName: string, environment: string): Promise<ApiResult<boolean>> {
  return request.post('/config/batch-publish', { appName, environment })
}

/**
 * 搜索配置
 */
export function searchConfigs(params: {
  keyword: string
  appName?: string
  environment?: string
}): Promise<ApiResult<ConfigItem[]>> {
  return request.get('/config/search', params)
}

/**
 * 获取配置历史
 */
export function getConfigHistory(id: number): Promise<ApiResult<any[]>> {
  return request.get(`/config/${id}/history`)
}

/**
 * 验证配置格式
 */
export function validateConfig(configValue: string, dataType: string): Promise<ApiResult<boolean>> {
  return request.post('/config/validate', { configValue, dataType })
} 