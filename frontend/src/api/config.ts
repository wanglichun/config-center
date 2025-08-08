import { request } from '@/utils/request'
import type { ApiResult, PageResult } from '@/types/common'
import type { ConfigItem, ConfigQuery, ConfigForm } from '@/types/config'
import type { MachineInstance } from '@/types/machine'

/**
 * 分页查询配置列表
 */
export function getConfigPage(params: ConfigQuery): Promise<ApiResult<PageResult<ConfigItem>>> {
  return request.get('/config/page', params)
}

/**
 * 通过ID获取配置详情
 */
export function getConfigById(id: number): Promise<ApiResult<ConfigItem>> {
  return request.get(`/config/${id}`)
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
export function createConfig(data: ConfigForm): Promise<ApiResult<any>> {
  return request.post('/config', data)
}

/**
 * 更新配置
 */
export function updateConfig(id: number, data: ConfigForm): Promise<ApiResult<any>> {
  return request.put(`/config/${id}`, data)
}

/**
 * 删除配置
 */
export function deleteConfig(id: number): Promise<ApiResult<boolean>> {
  return request.delete(`/config/${id}`)
}

/**
 * 发布配置到指定机器
 */
export function publishConfig(configId: number, selectedMachines: string[], ticketId?: number, action: string = 'Publish'): Promise<ApiResult<boolean>> {
  return request.post(`/config/${configId}/publish`, {
    ipList: selectedMachines,
    ticketId: ticketId,
    action: action
  })
}

/**
 * 回滚配置到指定机器
 */
export function rollbackConfig(configId: number, selectedMachines: string[], ticketId?: number): Promise<ApiResult<boolean>> {
  return publishConfig(configId, selectedMachines, ticketId, 'Rollback')
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
export function getConfigHistory(id: number, params?: { pageNum?: number; pageSize?: number }): Promise<ApiResult<PageResult<any>>> {
  return request.get(`/config/history`, { configId: id, ...params })
}

/**
 * 切换配置状态
 */
export function toggleConfigStatus(configId: number, status: string): Promise<ApiResult<boolean>> {
  return request.put(`/config/${configId}/status`, { status })
}

/**
 * 验证配置格式
 */
export function validateConfig(configValue: string, dataType: string): Promise<ApiResult<boolean>> {
  return request.post('/config/validate', { configValue, dataType })
}

/**
 * 获取配置键列表
 */
export function getConfigKeys(params: {
  appName: string
  environment: string
  groupName?: string
}): Promise<ApiResult<string[]>> {
  return request.get('/config/keys', params)
}

/**
 * 灰度发布相关API
 */
export const grayReleaseApi = {
  /**
   * 获取灰度发布计划列表
   */
  getPlans(params: any): Promise<ApiResult<any>> {
    return request.get('/gray-release/plan/page', params)
  },

  /**
   * 获取实例列表
   */
  getInstances(params: {
    appName: string
    environment: string
    groupName?: string
    configKey?: string
  }): Promise<ApiResult<any[]>> {
    return request.get('/gray-release/instances', params)
  },

  /**
   * 创建灰度发布计划
   */
  createPlan(data: any): Promise<ApiResult<any>> {
    return request.post('/gray-release/plan', data)
  },

  /**
   * 获取计划详情
   */
  getPlanDetails(planId: number): Promise<ApiResult<any>> {
    return request.get(`/gray-release/plan/${planId}`)
  },

  /**
   * 执行灰度发布计划
   */
  executePlan(planId: number): Promise<ApiResult<boolean>> {
    return request.post(`/gray-release/plan/${planId}/start`)
  },

  /**
   * 完成灰度发布计划
   */
  completePlan(planId: number): Promise<ApiResult<boolean>> {
    return request.post(`/gray-release/plan/${planId}/complete`)
  },

  /**
   * 回滚灰度发布计划
   */
  rollbackPlan(planId: number): Promise<ApiResult<boolean>> {
    return request.post(`/gray-release/plan/${planId}/rollback`)
  }
}

/**
 * 机器分组相关API
 */
export const machineGroupApi = {
  /**
   * 获取应用的所有分组
   */
  getGroups(params: {
    appName: string
    environment: string
  }): Promise<ApiResult<string[]>> {
    return request.get('/machine-groups/groups', params)
  },

  /**
   * 获取指定分组的机器列表
   */
  getMachinesByGroup(params: {
    appName: string
    environment: string
    groupName: string
  }): Promise<ApiResult<any[]>> {
    return request.get('/machine-groups/machines', params)
  },

  /**
   * 获取应用的所有机器（按分组组织）
   */
  getAllMachinesByGroups(params: {
    appName: string
    environment: string
  }): Promise<ApiResult<Record<string, any[]>>> {
    return request.get('/machine-groups/all-machines', params)
  },

  /**
   * 创建机器分组
   */
  createGroup(params: {
    appName: string
    environment: string
    groupName: string
    description?: string
  }): Promise<ApiResult<void>> {
    return request.post('/machine-groups/groups?' + new URLSearchParams(params as any).toString())
  },

  /**
   * 删除机器分组
   */
  deleteGroup(params: {
    appName: string
    environment: string
    groupName: string
  }): Promise<ApiResult<void>> {
    return request.delete('/machine-groups/groups?' + new URLSearchParams(params).toString())
  },

  /**
   * 注册机器到分组
   */
  registerMachine(params: {
    appName: string
    environment: string
    groupName: string
    instanceId: string
  }, instanceInfo: any): Promise<ApiResult<void>> {
    return request.post('/machine-groups/machines?' + new URLSearchParams(params).toString(), instanceInfo)
  },

  /**
   * 从分组中移除机器
   */
  unregisterMachine(params: {
    appName: string
    environment: string
    groupName: string
    instanceId: string
  }): Promise<ApiResult<void>> {
    return request.delete('/machine-groups/machines?' + new URLSearchParams(params).toString())
  }
}

/**
 * 获取订阅容器列表
 */
export function getSubscribedContainers(configId: number, ticketId?: number): Promise<ApiResult<MachineInstance[]>> {
  const params = ticketId ? `?ticketId=${ticketId}` : ''
  return request.get(`/config/${configId}/subscribers${params}`)
}

 