export enum TicketPhase {
  PENDING = 'PENDING',
  PROCESSING = 'PROCESSING',
  REVIEWING = 'REVIEWING',
  APPROVED = 'APPROVED',
  REJECTED = 'REJECTED',
  PUBLISHING = 'PUBLISHING',
  LOADING = 'LOADING',
  SUCCESS = 'SUCCESS',
  FAILED = 'FAILED',
  COMPLETED = 'COMPLETED'
}

export interface Ticket {
  id: number
  dataId: number
  title: string
  phase: TicketPhase
  applicator: string
  operator?: string
  createTime: number
  updateTime: number
  oldData: string
  newData: string
  description?: string
  remark?: string
  action?: string[]
}

export interface TicketQueryRequest {
  page: number
  size: number
  title?: string
  phase?: TicketPhase
  applicator?: string
  operator?: string
  startTime?: number
  endTime?: number
}

export interface TicketCreateRequest {
  dataId: number
  title: string
  oldData: string
  newData: string
  description?: string
  remark?: string
}

export interface TicketUpdateRequest {
  phase?: TicketPhase
  operator?: string
  remark?: string
} 