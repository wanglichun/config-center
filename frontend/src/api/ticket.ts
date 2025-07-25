import { request } from '@/utils/request'
import { TicketStatus, TicketPriority, TicketType } from '@/types/ticket'
import type { 
  Ticket, 
  TicketQuery, 
  TicketForm, 
  TicketStats, 
  TicketComment, 
  TicketOperation,
  TicketTemplate
} from '@/types/ticket'
import type { PageResult } from '@/types/common'
import type { ApiResult } from '@/types/api'

// Mock数据
const mockTickets: Ticket[] = [
  {
    id: 1,
    title: '配置中心登录页面显示异常',
    description: '用户反馈登录页面在某些浏览器下显示异常，需要修复UI布局问题',
    type: TicketType.BUG,
    priority: TicketPriority.HIGH,
    status: TicketStatus.IN_PROGRESS,
    assignee: 'developer1',
    reporter: 'user1',
    createTime: '2024-01-15 10:30:00',
    updateTime: '2024-01-16 14:20:00',
    dueTime: '2024-01-20 18:00:00',
    tags: ['UI', '登录'],
    comments: [
      {
        id: 1,
        ticketId: 1,
        content: '已确认问题，正在修复中',
        author: 'developer1',
        createTime: '2024-01-16 14:20:00',
        isInternal: false
      }
    ],
    environment: 'production',
    appName: 'config-center'
  },
  {
    id: 2,
    title: '新增配置项批量导入功能',
    description: '需要开发配置项批量导入功能，支持Excel文件上传',
    type: TicketType.FEATURE,
    priority: TicketPriority.MEDIUM,
    status: TicketStatus.PENDING,
    assignee: 'developer2',
    reporter: 'admin',
    createTime: '2024-01-14 09:15:00',
    updateTime: '2024-01-14 09:15:00',
    tags: ['功能', '批量导入'],
    comments: [],
    environment: 'development',
    appName: 'config-center'
  },
  {
    id: 3,
    title: '数据库连接池配置优化',
    description: '当前数据库连接池配置不够优化，需要调整以提高性能',
    type: TicketType.CONFIG,
    priority: TicketPriority.MEDIUM,
    status: TicketStatus.RESOLVED,
    assignee: 'developer3',
    reporter: 'system',
    createTime: '2024-01-13 16:45:00',
    updateTime: '2024-01-15 11:30:00',
    tags: ['数据库', '性能'],
    comments: [
      {
        id: 2,
        ticketId: 3,
        content: '已优化连接池配置，性能提升明显',
        author: 'developer3',
        createTime: '2024-01-15 11:30:00',
        isInternal: false
      }
    ],
    environment: 'production',
    appName: 'config-center'
  }
]

const mockTemplates: TicketTemplate[] = [
  {
    id: 1,
    name: 'Bug修复模板',
    type: TicketType.BUG,
    priority: TicketPriority.HIGH,
    description: '用于创建Bug修复工单的标准模板',
    tags: ['Bug', '修复'],
    createTime: '2024-01-10 10:00:00',
    updateTime: '2024-01-10 10:00:00',
    createBy: 'admin'
  },
  {
    id: 2,
    name: '功能需求模板',
    type: TicketType.FEATURE,
    priority: TicketPriority.MEDIUM,
    description: '用于创建功能需求工单的标准模板',
    tags: ['功能', '需求'],
    createTime: '2024-01-10 10:00:00',
    updateTime: '2024-01-10 10:00:00',
    createBy: 'admin'
  }
]

// 获取工单列表
export function getTicketList(params: TicketQuery): Promise<PageResult<Ticket>> {
  // Mock实现
  return new Promise((resolve) => {
    setTimeout(() => {
      let filteredTickets = [...mockTickets]
      
      // 过滤逻辑
      if (params.title) {
        filteredTickets = filteredTickets.filter(t => 
          t.title.toLowerCase().includes(params.title!.toLowerCase())
        )
      }
      if (params.type) {
        filteredTickets = filteredTickets.filter(t => t.type === params.type)
      }
      if (params.priority) {
        filteredTickets = filteredTickets.filter(t => t.priority === params.priority)
      }
      if (params.status) {
        filteredTickets = filteredTickets.filter(t => t.status === params.status)
      }
      if (params.assignee) {
        filteredTickets = filteredTickets.filter(t => t.assignee === params.assignee)
      }
      
      const start = (params.pageNum - 1) * params.pageSize
      const end = start + params.pageSize
      const data = filteredTickets.slice(start, end)
      
      resolve({
        records: data,
        total: filteredTickets.length,
        pageNum: params.pageNum,
        pageSize: params.pageSize
      })
    }, 300)
  })
}

// 获取工单详情
export function getTicketById(id: number): Promise<ApiResult<Ticket>> {
  return request({
    url: `/api/ticket/${id}`,
    method: 'get'
  })
}

// 创建工单
export function createTicket(data: {
  configId: number
  title: string
  newData: string
}): Promise<ApiResult<number>> {
  return request({
    url: '/api/ticket',
    method: 'post',
    data: {
      dataId: data.configId,
      title: data.title,
      newData: data.newData
    }
  })
}

// 更新工单
export function updateTicket(id: number, data: TicketForm): Promise<Ticket> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = mockTickets.findIndex(t => t.id === id)
      if (index !== -1) {
        mockTickets[index] = {
          ...mockTickets[index],
          ...data,
          updateTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
        }
        resolve(mockTickets[index])
      } else {
        reject(new Error('工单不存在'))
      }
    }, 300)
  })
}

// 更新工单状态
export function updateTicketStatus(id: number, status: TicketStatus): Promise<void> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const ticket = mockTickets.find(t => t.id === id)
      if (ticket) {
        ticket.status = status
        ticket.updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        resolve()
      } else {
        reject(new Error('工单不存在'))
      }
    }, 200)
  })
}

// 分配工单
export function assignTicket(id: number, assignee: string): Promise<void> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const ticket = mockTickets.find(t => t.id === id)
      if (ticket) {
        ticket.assignee = assignee
        ticket.updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        resolve()
      } else {
        reject(new Error('工单不存在'))
      }
    }, 200)
  })
}

// 添加评论
export function addComment(ticketId: number, content: string, isInternal: boolean = false): Promise<TicketComment> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const ticket = mockTickets.find(t => t.id === ticketId)
      if (ticket) {
        const comment: TicketComment = {
          id: Math.max(...ticket.comments.map(c => c.id), 0) + 1,
          ticketId,
          content,
          author: 'current_user',
          createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
          isInternal
        }
        ticket.comments.push(comment)
        ticket.updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        resolve(comment)
      } else {
        reject(new Error('工单不存在'))
      }
    }, 200)
  })
}

// 获取工单统计
export function getTicketStats(): Promise<TicketStats> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const stats: TicketStats = {
        total: mockTickets.length,
        pending: mockTickets.filter(t => t.status === TicketStatus.PENDING).length,
        inProgress: mockTickets.filter(t => t.status === TicketStatus.IN_PROGRESS).length,
        resolved: mockTickets.filter(t => t.status === TicketStatus.RESOLVED).length,
        closed: mockTickets.filter(t => t.status === TicketStatus.CLOSED).length,
        rejected: mockTickets.filter(t => t.status === TicketStatus.REJECTED).length,
        myTickets: mockTickets.filter(t => t.assignee === 'current_user').length,
        overdue: mockTickets.filter(t => {
          if (!t.dueTime) return false
          return new Date(t.dueTime) < new Date() && t.status !== TicketStatus.CLOSED
        }).length
      }
      resolve(stats)
    }, 200)
  })
}

// 批量操作
export function batchUpdateTickets(ids: number[], operation: string, value?: any): Promise<void> {
  return new Promise((resolve) => {
    setTimeout(() => {
      ids.forEach(id => {
        const ticket = mockTickets.find(t => t.id === id)
        if (ticket) {
          if (operation === 'status') {
            ticket.status = value
          } else if (operation === 'assignee') {
            ticket.assignee = value
          }
          ticket.updateTime = new Date().toISOString().replace('T', ' ').substring(0, 19)
        }
      })
      resolve()
    }, 300)
  })
}

// 获取工单模板列表
export function getTicketTemplates(): Promise<TicketTemplate[]> {
  return new Promise((resolve) => {
    setTimeout(() => {
      resolve([...mockTemplates])
    }, 200)
  })
}

// 创建工单模板
export function createTicketTemplate(data: Omit<TicketTemplate, 'id' | 'createTime' | 'updateTime'>): Promise<TicketTemplate> {
  return new Promise((resolve) => {
    setTimeout(() => {
      const newTemplate: TicketTemplate = {
        id: Math.max(...mockTemplates.map(t => t.id)) + 1,
        ...data,
        createTime: new Date().toISOString().replace('T', ' ').substring(0, 19),
        updateTime: new Date().toISOString().replace('T', ' ').substring(0, 19)
      }
      mockTemplates.push(newTemplate)
      resolve(newTemplate)
    }, 300)
  })
}

// 删除工单模板
export function deleteTicketTemplate(id: number): Promise<void> {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const index = mockTemplates.findIndex(t => t.id === id)
      if (index !== -1) {
        mockTemplates.splice(index, 1)
        resolve()
      } else {
        reject(new Error('模板不存在'))
      }
    }, 200)
  })
} 