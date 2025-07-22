/**
 * 机器实例信息
 */
export interface MachineInstance {
  /** 实例名称 */
  name: string
  /** 实例IP地址 */
  ip: string
  /** 版本号 */
  version: string
  /** 状态 */
  status: string
  /** 最后更新时间 */
  lastUpdateTime: number
} 