// 登录表单
export interface LoginForm {
  username: string
  password: string
  remember?: boolean
}

// 用户信息
export interface UserInfo {
  id: number
  username: string
  realName: string
  email: string
  role: string
  status: string
  avatar?: string
  createTime: string
  updateTime: string
  lastLoginTime?: string
  lastLoginIp?: string
}

// 登录响应
export interface LoginResponse {
  token: string
  userInfo: UserInfo
}

// 用户列表查询参数
export interface UserQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  role?: string
  status?: string
}

// 用户创建/更新表单
export interface UserForm {
  id?: number
  username: string
  password?: string
  realName: string
  email: string
  role: string
  status: string
  remark?: string
}

// 修改密码表单
export interface ChangePasswordForm {
  oldPassword: string
  newPassword: string
  confirmPassword: string
} 