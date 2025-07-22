import axios from 'axios'
import type { AxiosInstance, AxiosRequestConfig, AxiosResponse } from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from './auth'
import router from '@/router'
import type { ApiResult } from '@/types/common'

// 创建axios实例
const service: AxiosInstance = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=utf-8'
  }
})

// 请求拦截器
service.interceptors.request.use(
  (config: AxiosRequestConfig) => {
    // 添加认证token
    const token = getToken()
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  (response: AxiosResponse<ApiResult>) => {
    const { data } = response
    
    // 如果是文件下载，直接返回
    if (response.config.responseType === 'blob') {
      return response
    }
    
    // 检查业务状态码
    if (data.code === 0) {
      return data
    } else if (data.code === 401) {
      // 未认证，跳转到登录页
      ElMessage.error('登录已过期，请重新登录')
      removeToken()
      router.push('/login')
      return Promise.reject(new Error(data.message))
    } else if (data.code === 403) {
      // 权限不足
      ElMessage.error('权限不足')
      return Promise.reject(new Error(data.message))
    } else {
      // 其他业务错误
      ElMessage.error(data.message || '请求失败')
      return Promise.reject(new Error(data.message))
    }
  },
  (error) => {
    console.error('响应错误:', error)
    
    if (error.response) {
      const { status, data } = error.response
      
      switch (status) {
        case 400:
          ElMessage.error(data.message || '请求参数错误')
          break
        case 401:
          ElMessage.error('登录已过期，请重新登录')
          removeToken()
          router.push('/login')
          break
        case 403:
          ElMessage.error('权限不足')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error('服务器内部错误')
          break
        default:
          ElMessage.error(data.message || `请求失败: ${status}`)
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请稍后重试')
    } else {
      ElMessage.error('网络错误，请检查网络连接')
    }
    
    return Promise.reject(error)
  }
)

// 请求方法封装
export const request = {
  get<T = any>(url: string, params?: any): Promise<ApiResult<T>> {
    return service.get(url, { params })
  },
  
  post<T = any>(url: string, data?: any, params?: any): Promise<ApiResult<T>> {
    return service.post(url, data, { params })
  },
  
  put<T = any>(url: string, data?: any): Promise<ApiResult<T>> {
    return service.put(url, data)
  },
  
  delete<T = any>(url: string, params?: any): Promise<ApiResult<T>> {
    return service.delete(url, { params })
  },
  
  upload<T = any>(url: string, formData: FormData): Promise<ApiResult<T>> {
    return service.post(url, formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    })
  },
  
  download(url: string, params?: any): Promise<AxiosResponse> {
    return service.get(url, {
      params,
      responseType: 'blob'
    })
  }
}

export default service 