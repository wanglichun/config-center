import { request } from '@/utils/request'
import type { Ticket, TicketQueryRequest, TicketCreateRequest, TicketUpdateRequest } from '@/types/ticket'
import type { PageResult } from '@/types/common'
import type { ApiResult } from '@/types/common'

// 分页查询工单列表
export function getTicketPage(params: TicketQueryRequest): Promise<ApiResult<PageResult<Ticket>>> {
  return request.get('/ticket/page', params)
}

// 根据ID获取工单详情
export function getTicketById(id: number): Promise<ApiResult<Ticket>> {
  return request.get(`/ticket/${id}`)
}

// 创建工单
export function createTicket(data: TicketCreateRequest): Promise<ApiResult<number>> {
  return request.post('/ticket', {
    dataId: data.dataId,
    title: data.title,
    newData: data.newData
  })
}

/**
 * 获取配置的进行中ticket
 */
export function getProcessingTicket(configId: number): Promise<ApiResult<any>> {
  return request.get(`/ticket/${configId}/get_processing_ticket`)
}

// 更新工单
export function updateTicket(id: number, data: TicketUpdateRequest): Promise<ApiResult<boolean>> {
  return request.put(`/ticket/${id}`, data)
}

// 删除工单
export function deleteTicket(id: number): Promise<ApiResult<boolean>> {
  return request.delete(`/ticket/${id}`)
} 