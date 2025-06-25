// API响应结果
export interface ApiResult<T = any> {
  code: number
  message: string
  data: T
  success: boolean
  timestamp: number
}

// 分页结果
export interface PageResult<T = any> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
  hasNext: boolean
  hasPrevious: boolean
}

// 分页查询参数
export interface PageQuery {
  pageNum: number
  pageSize: number
}

// 选项
export interface Option {
  label: string
  value: string | number
  disabled?: boolean
}

// 表格列配置
export interface TableColumn {
  prop: string
  label: string
  width?: string | number
  minWidth?: string | number
  fixed?: boolean | string
  sortable?: boolean
  formatter?: (row: any, column: any, cellValue: any, index: number) => string
  show?: boolean
}

// 菜单项
export interface MenuItem {
  path: string
  name: string
  title: string
  icon?: string
  hidden?: boolean
  role?: string[]
  children?: MenuItem[]
}

// 面包屑项
export interface BreadcrumbItem {
  title: string
  path?: string
}

// 上传文件
export interface UploadFile {
  name: string
  url: string
  size: number
  type: string
  status: 'ready' | 'uploading' | 'success' | 'error'
}

// 系统配置
export interface SystemConfig {
  title: string
  logo: string
  version: string
  copyright: string
  theme: 'light' | 'dark'
  language: 'zh-CN' | 'en-US'
}

// 环境类型
export type Environment = 'dev' | 'test' | 'prod'

// 数据类型
export type DataType = 'STRING' | 'NUMBER' | 'BOOLEAN' | 'JSON'

// 配置状态
export type ConfigStatus = 'DRAFT' | 'PUBLISHED' | 'DISABLED'

// 用户角色
export type UserRole = 'ADMIN' | 'DEVELOPER' | 'VIEWER'

// 用户状态
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'LOCKED'

// 操作类型
export type OperationType = 'CREATE' | 'UPDATE' | 'DELETE' | 'PUBLISH' | 'ROLLBACK' 