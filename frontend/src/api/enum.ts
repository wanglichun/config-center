import { request } from '@/utils/request'
import type { ApiResult } from '@/types/common'

// 枚举项类型
export interface EnumItem {
  code: string
  description: string
}

// 所有枚举类型
export interface AllEnums {
  EnvironmentEnum: Record<string, string>
  ConfigStatusEnum: Record<string, string>
}

/**
 * 获取所有枚举值
 */
export function getAllEnum(): Promise<ApiResult<AllEnums>> {
  return request.get('/enum/get_all_enum')
}

/**
 * 将枚举对象转换为选项数组
 */
export function enumToOptions(enumObj: Record<string, string>) {
  return Object.entries(enumObj).map(([code, description]) => ({
    value: code,
    label: description
  }))
} 