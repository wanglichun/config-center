# 配置中心前端管理界面

基于 Vue 3 + TypeScript + Element Plus 构建的现代化配置中心管理平台。

## 技术栈

- **框架**: Vue 3 + TypeScript
- **构建工具**: Vite
- **UI组件库**: Element Plus
- **状态管理**: Pinia
- **路由**: Vue Router 4
- **图表**: ECharts + Vue-ECharts
- **HTTP客户端**: Axios
- **样式**: SCSS
- **代码规范**: ESLint + Prettier

## 功能特性

### 🎯 核心功能
- **用户认证**: JWT Token认证，支持记住密码
- **权限管理**: 基于角色的访问控制(RBAC)
- **配置管理**: 配置的增删改查、发布、回滚
- **多环境支持**: dev/test/prod环境隔离
- **实时监控**: 配置变更历史和统计图表
- **搜索过滤**: 支持多维度搜索和筛选

### 🎨 界面特性
- **响应式设计**: 支持PC端和移动端
- **暗色主题**: 支持明暗主题切换
- **现代化UI**: 简洁美观的界面设计
- **交互友好**: 丰富的交互反馈和动画效果

### 📊 数据可视化
- **统计图表**: 配置状态分布、环境分布等
- **实时数据**: 动态更新的统计信息
- **活动记录**: 最近操作记录展示

## 快速开始

### 环境要求
- Node.js >= 16.0.0
- npm >= 7.0.0 或 yarn >= 1.22.0

### 安装依赖
```bash
cd frontend
npm install
# 或
yarn install
```

### 开发模式
```bash
npm run dev
# 或
yarn dev
```

访问 http://localhost:3000

### 构建生产版本
```bash
npm run build
# 或
yarn build
```

### 代码检查
```bash
npm run lint
# 或
yarn lint
```

### 类型检查
```bash
npm run type-check
# 或
yarn type-check
```

## 项目结构

```
frontend/
├── public/                 # 静态资源
├── src/
│   ├── api/               # API接口
│   ├── components/        # 公共组件
│   ├── layout/           # 布局组件
│   ├── router/           # 路由配置
│   ├── stores/           # 状态管理
│   ├── styles/           # 全局样式
│   ├── types/            # TypeScript类型定义
│   ├── utils/            # 工具函数
│   ├── views/            # 页面组件
│   ├── App.vue           # 根组件
│   └── main.ts           # 入口文件
├── index.html            # HTML模板
├── package.json          # 项目配置
├── tsconfig.json         # TypeScript配置
├── vite.config.ts        # Vite配置
└── README.md             # 项目说明
```

## 环境配置

### 开发环境 (.env.development)
```bash
# API基础URL
VITE_API_BASE_URL=http://localhost:8080/api

# 应用标题
VITE_APP_TITLE=配置中心管理平台

# 是否显示调试信息
VITE_DEBUG=true
```

### 生产环境 (.env.production)
```bash
# API基础URL
VITE_API_BASE_URL=/api

# 应用标题
VITE_APP_TITLE=配置中心管理平台

# 是否显示调试信息
VITE_DEBUG=false
```

## 页面说明

### 🏠 仪表盘 (/dashboard)
- 系统统计概览
- 配置状态分布图表
- 最近活动记录
- 快速操作入口

### ⚙️ 配置管理 (/config)
- 配置列表查看
- 配置增删改查
- 配置发布和回滚
- 批量操作支持

### 📝 变更历史 (/history)
- 配置变更记录
- 操作审计日志
- 版本对比功能

### 👥 用户管理 (/users)
- 用户账号管理
- 角色权限分配
- 用户状态控制

### 📊 系统监控 (/monitor)
- 系统运行状态
- 性能指标监控
- 健康检查结果

## 默认账号

- **管理员**: admin / admin123
- **开发者**: developer / dev123
- **查看者**: viewer / view123

## 部署说明

### Nginx配置示例
```nginx
server {
    listen 80;
    server_name your-domain.com;
    root /path/to/frontend/dist;
    index index.html;

    # 处理Vue Router的history模式
    location / {
        try_files $uri $uri/ /index.html;
    }

    # API代理
    location /api {
        proxy_pass http://localhost:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    # 静态资源缓存
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

### Docker部署
```dockerfile
# 构建阶段
FROM node:16-alpine as build-stage
WORKDIR /app
COPY package*.json ./
RUN npm ci --only=production
COPY . .
RUN npm run build

# 生产阶段
FROM nginx:alpine as production-stage
COPY --from=build-stage /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/nginx.conf
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

## 开发指南

### 代码规范
- 使用 TypeScript 进行类型检查
- 遵循 Vue 3 Composition API 规范
- 使用 ESLint + Prettier 保证代码质量
- 组件命名采用 PascalCase
- 文件命名采用 kebab-case

### 提交规范
- feat: 新功能
- fix: 修复bug
- docs: 文档更新
- style: 代码格式化
- refactor: 代码重构
- test: 测试相关
- chore: 构建/工具相关

## 浏览器支持

- Chrome >= 87
- Firefox >= 78
- Safari >= 14
- Edge >= 88

## 许可证

MIT License 