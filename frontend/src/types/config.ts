// 配置项
export interface ConfigItem {
  id: number
  groupName: string
  configKey: string
  configValue: string
  dataType: string
  description?: string
  version: number
  encrypted: boolean
  tags?: string
  status: string
  zkPath?: string
  lastPublishTime?: number
  publisher?: string
  createTime: string
  updateTime: string
  createBy: string
  updateBy: string
  remark?: string
}

// 配置查询参数
export interface ConfigQuery {
  pageNum: number
  pageSize: number
  groupName?: string
  keyword?: string
  status?: string
}

// 配置表单
export interface ConfigForm {
  id?: number
  groupName: string
  configKey: string
  configValue: string
  dataType: string
  description?: string
  encrypted: boolean
  tags?: string
  remark?: string
}

// 配置历史
export interface ConfigHistory {
  id: number
  configId: number
  appName: string
  environment: string
  groupName: string
  configKey: string
  oldValue?: string
  newValue: string
  version: number
  operationType: string
  changeReason?: string
  operator: string
  operateTime: number
  clientIp?: string
  rollback: boolean
  createTime: string
}

// 配置历史查询参数
export interface HistoryQuery {
  pageNum: number
  pageSize: number
  configId?: number
  appName?: string
  environment?: string
  operator?: string
  operationType?: string
  startTime?: string
  endTime?: string
}

// 应用信息
export interface AppInfo {
  appName: string
  environments: string[]
  configCount: number
  lastUpdateTime?: string
}

// 环境信息
export interface EnvironmentInfo {
  environment: string
  configCount: number
  groups: string[]
}

// 配置组信息
export interface GroupInfo {
  groupName: string
  configCount: number
  configs: ConfigItem[]
}

// 配置统计
export interface ConfigStats {
  totalConfigs: number
  totalApps: number
  totalEnvironments: number
  recentChanges: number
  publishedConfigs: number
  draftConfigs: number
}

// 批量操作
export interface BatchOperation {
  action: 'publish' | 'delete' | 'export'
  configIds: number[]
  reason?: string
} 