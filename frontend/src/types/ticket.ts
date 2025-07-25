// 工单状态枚举
export enum TicketStatus {
  PENDING = 'PENDING',      // 待处理
  IN_PROGRESS = 'IN_PROGRESS', // 处理中
  RESOLVED = 'RESOLVED',    // 已解决
  CLOSED = 'CLOSED',        // 已关闭
  REJECTED = 'REJECTED'     // 已拒绝
}

// 工单优先级枚举
export enum TicketPriority {
  LOW = 'LOW',           // 低
  MEDIUM = 'MEDIUM',     // 中
  HIGH = 'HIGH',         // 高
  URGENT = 'URGENT'      // 紧急
}

// 工单类型枚举
export enum TicketType {
  BUG = 'BUG',           // 缺陷
  FEATURE = 'FEATURE',   // 功能需求
  CONFIG = 'CONFIG',     // 配置变更
  DEPLOY = 'DEPLOY',     // 部署问题
  OTHER = 'OTHER'        // 其他
}

// 工单实体
export interface Ticket {
  id: number
  title: string
  description: string
  type: TicketType
  priority: TicketPriority
  status: TicketStatus
  assignee?: string
  reporter: string
  createTime: string
  updateTime: string
  dueTime?: string
  tags?: string[]
  comments: TicketComment[]
  relatedConfigs?: number[] // 关联的配置ID
  environment?: string
  appName?: string
}

// 工单评论
export interface TicketComment {
  id: number
  ticketId: number
  content: string
  author: string
  createTime: string
  isInternal: boolean // 是否内部评论
}

// 工单查询参数
export interface TicketQuery {
  pageNum: number
  pageSize: number
  title?: string
  type?: TicketType
  priority?: TicketPriority
  status?: TicketStatus
  assignee?: string
  reporter?: string
  startTime?: string
  endTime?: string
  tags?: string[]
}

// 工单表单
export interface TicketForm {
  id?: number
  title: string
  description: string
  type: TicketType
  priority: TicketPriority
  assignee?: string
  dueTime?: string
  tags?: string[]
  relatedConfigs?: number[]
  environment?: string
  appName?: string
}

// 工单统计
export interface TicketStats {
  total: number
  pending: number
  inProgress: number
  resolved: number
  closed: number
  rejected: number
  myTickets: number
  overdue: number
}

// 工单操作记录
export interface TicketOperation {
  id: number
  ticketId: number
  operation: string
  operator: string
  operateTime: string
  details?: string
  oldValue?: string
  newValue?: string
}

// 工单模板
export interface TicketTemplate {
  id: number
  name: string
  type: TicketType
  priority: TicketPriority
  description: string
  tags?: string[]
  createTime: string
  updateTime: string
  createBy: string
} 