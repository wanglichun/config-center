# Ticket 工作流程说明

## 功能概述

当用户点击配置详情页面的"编辑"按钮时，系统会弹出一个对话框，让用户输入工单信息，然后创建ticket并跳转到ticket详情页面。

## 工作流程

### 1. 前端流程

1. **配置详情页面** (`frontend/src/views/config/Detail.vue`)
   - 用户点击"编辑"按钮
   - 弹出创建工单对话框
   - 预填充工单标题和当前配置值

2. **创建工单对话框**
   - 用户输入工单标题
   - 用户输入新的配置值
   - 显示当前配置值（只读）

3. **API调用**
   - 调用 `/api/ticket` POST接口创建工单
   - 传递参数：`dataId`（配置ID）、`title`、`newData`

4. **跳转页面**
   - 创建成功后跳转到 `/ticket/detail/{id}` 页面
   - 显示工单详情信息

### 2. 后端流程

1. **Controller层** (`TicketController.java`)
   - 接收创建工单请求
   - 调用Service层处理业务逻辑

2. **Service层** (`TicketServiceImpl.java`)
   - 根据dataId获取配置信息
   - 构建Ticket对象
   - 调用Mapper层保存数据

3. **Mapper层** (`TicketMapper.java`)
   - 执行数据库插入操作

4. **数据库表** (`ticket.sql`)
   - 存储工单信息
   - 包含旧数据和新数据的对比

## API接口

### 创建工单
```
POST /api/ticket
Content-Type: application/json

{
  "dataId": 123,
  "title": "修改配置项: database.url",
  "newData": "jdbc:mysql://new-host:3306/db"
}
```

### 获取工单详情
```
GET /api/ticket/{id}
```

## 数据库表结构

```sql
CREATE TABLE `ticket` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '工单ID',
  `data_id` bigint(20) NOT NULL COMMENT '关联数据ID',
  `title` varchar(255) NOT NULL COMMENT '工单标题',
  `phase` varchar(50) NOT NULL COMMENT '工单阶段',
  `applicator` varchar(100) NOT NULL COMMENT '申请人',
  `operator` varchar(100) DEFAULT NULL COMMENT '操作人',
  `create_time` bigint(20) NOT NULL COMMENT '创建时间戳',
  `update_time` bigint(20) NOT NULL COMMENT '更新时间戳',
  `old_data` text COMMENT '旧数据(JSON格式)',
  `new_data` text COMMENT '新数据(JSON格式)',
  PRIMARY KEY (`id`)
);
```

## 工单阶段

- `PENDING`: 待处理
- `PROCESSING`: 处理中
- `APPROVED`: 已批准
- `REJECTED`: 已拒绝
- `COMPLETED`: 已完成

## 使用说明

1. 在配置管理页面找到需要修改的配置项
2. 点击配置项进入详情页面
3. 点击"编辑"按钮
4. 在弹出的对话框中输入工单标题和新配置值
5. 点击"提交"创建工单
6. 系统自动跳转到工单详情页面
7. 在工单详情页面可以查看工单状态、操作记录等信息

## 注意事项

1. 工单创建后状态默认为"待处理"
2. 只有管理员或指定操作人可以处理工单
3. 工单处理完成后，配置值才会真正更新
4. 所有操作都有完整的审计日志记录 