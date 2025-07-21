# 用户邮箱Cookie功能说明

## 功能概述

在前端服务中，用户登录成功后会自动将用户邮箱保存到Cookie中，key为`USER_EMAIL`，value为用户邮箱地址。

## 功能特性

### 🔐 安全性
- **Secure Flag**: 在生产环境（HTTPS）中自动启用secure标志
- **SameSite**: 设置为`strict`，防止CSRF攻击
- **过期时间**: 7天自动过期

### 🔄 自动化管理
- **自动保存**: 用户登录时自动保存邮箱到Cookie
- **自动更新**: 获取用户信息时自动更新Cookie
- **自动清除**: 用户登出时自动清除Cookie

### 🛠️ 工具函数
提供完整的Cookie操作工具函数：

```typescript
// 设置用户邮箱
setUserEmail(email: string): void

// 获取用户邮箱
getUserEmail(): string | undefined

// 移除用户邮箱
removeUserEmail(): void

// 检查是否存在邮箱Cookie
hasUserEmail(): boolean

// 获取所有Cookie（调试用）
getAllCookies(): Record<string, string>
```

## 实现细节

### 1. Cookie工具函数 (`src/utils/cookie.ts`)

```typescript
import Cookies from 'js-cookie'

const USER_EMAIL_KEY = 'USER_EMAIL'
const USER_EMAIL_EXPIRES = 7 // 7天

export function setUserEmail(email: string): void {
  Cookies.set(USER_EMAIL_KEY, email, { 
    expires: USER_EMAIL_EXPIRES,
    secure: process.env.NODE_ENV === 'production',
    sameSite: 'strict'
  })
}
```

### 2. 用户Store集成 (`src/stores/user.ts`)

```typescript
// 登录时自动保存邮箱
const userLogin = async (loginForm: LoginForm) => {
  // ... 登录逻辑
  if (response.data.userInfo.email) {
    setUserEmail(response.data.userInfo.email)
  }
}

// 登出时自动清除邮箱
const logout = () => {
  // ... 登出逻辑
  removeUserEmail()
}
```

### 3. 组件使用示例

```vue
<template>
  <div>
    <p>用户邮箱: {{ userEmail }}</p>
    <el-button @click="copyEmail">复制邮箱</el-button>
  </div>
</template>

<script setup>
import { getUserEmail } from '@/utils/cookie'

const userEmail = getUserEmail()
</script>
```

## 使用场景

### 1. 快速获取用户邮箱
```typescript
import { getUserEmail } from '@/utils/cookie'

// 在任何地方快速获取用户邮箱
const email = getUserEmail()
if (email) {
  // 使用邮箱信息
}
```

### 2. 表单预填充
```typescript
// 在表单中预填充用户邮箱
const form = reactive({
  email: getUserEmail() || ''
})
```

### 3. 用户标识
```typescript
// 使用邮箱作为用户标识
const userIdentifier = getUserEmail() || 'anonymous'
```

## 测试页面

### 1. 个人中心页面
在个人中心的"邮箱Cookie信息"标签页中可以查看和管理邮箱Cookie。

### 2. 测试页面
访问 `/test-email-cookie` 页面可以进行完整的功能测试。

## 浏览器开发者工具验证

### 1. 查看Cookie
1. 打开浏览器开发者工具
2. 切换到Application/Storage标签
3. 在左侧找到Cookies
4. 查看当前域名下的Cookie
5. 应该能看到`USER_EMAIL`键值对

### 2. 验证安全设置
```javascript
// 在控制台中查看Cookie详情
document.cookie.split(';').forEach(cookie => {
  if (cookie.includes('USER_EMAIL')) {
    console.log('USER_EMAIL cookie:', cookie.trim())
  }
})
```

## 注意事项

### 1. 隐私保护
- Cookie中只存储邮箱地址，不包含敏感信息
- 7天自动过期，减少数据泄露风险
- 支持用户手动清除

### 2. 兼容性
- 使用js-cookie库，兼容所有现代浏览器
- 自动处理Cookie编码/解码

### 3. 安全性
- 生产环境自动启用HTTPS
- SameSite设置防止CSRF攻击
- 不存储密码等敏感信息

## 故障排除

### 1. Cookie未保存
- 检查用户是否成功登录
- 检查用户信息中是否包含邮箱
- 检查浏览器是否支持Cookie

### 2. Cookie被清除
- 检查是否手动清除了Cookie
- 检查是否超过了7天过期时间
- 检查用户是否执行了登出操作

### 3. 安全警告
- 确保生产环境使用HTTPS
- 检查SameSite设置是否正确
- 验证Cookie域名设置

## 扩展功能

### 1. 自定义过期时间
```typescript
// 可以修改USER_EMAIL_EXPIRES常量来调整过期时间
const USER_EMAIL_EXPIRES = 30 // 30天
```

### 2. 添加更多用户信息
```typescript
// 可以扩展存储更多用户信息
setUserInfo({
  email: user.email,
  username: user.username,
  role: user.role
})
```

### 3. 加密存储
```typescript
// 可以对敏感信息进行加密
import CryptoJS from 'crypto-js'

function setEncryptedUserEmail(email: string) {
  const encrypted = CryptoJS.AES.encrypt(email, 'secret-key').toString()
  Cookies.set('USER_EMAIL_ENCRYPTED', encrypted)
}
``` 