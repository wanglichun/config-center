// 工单状态枚举
export enum TicketPhase {
  Submit = 'Submit',
  Reviewing	 = 'Reviewing',
  Rejected = 'Rejected',
  Success = 'Success',
  GrayPublish = 'GrayPublish',
  Cancelled = 'Cancelled'
}

// 工单实体
export interface Ticket {
  id: number
  dataId: number
  title: string
  phase: TicketPhase
  applicator: string
  operator?: string
  createTime: long
  updateTime: long
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
  id?: number
  title?: string
  action?: string  // 对应后端的TicketActionEnum
  operator?: string
  newData?: string
} 