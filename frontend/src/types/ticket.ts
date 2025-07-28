// 工单状态枚举
export enum TicketPhase {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  COMPLETED = 'COMPLETED'
}

// 工单实体
export interface Ticket {
  id: number
  dataId: number
  title: string
  phase: TicketPhase
  applicator: string
  operator?: string
  createTime: string
  updateTime: string
  createBy?: string
  updateBy?: string
  oldData?: string
  newData?: string
}

// 工单查询请求
export interface TicketQueryRequest {
  pageNum: number
  pageSize: number
  title?: string
  phase?: string
  applicator?: string
}

// 工单创建请求
export interface TicketCreateRequest {
  dataId: number
  title: string
  newData: string
}

// 工单更新请求
export interface TicketUpdateRequest {
  id: number
  title?: string
  phase?: TicketPhase
  operator?: string
  newData?: string
} 