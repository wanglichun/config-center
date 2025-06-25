# 企业级配置中心 (Config Center)

基于 Spring Boot + ZooKeeper 构建的企业级分布式配置管理系统，提供配置的集中管理、实时推送、版本控制、权限管理等功能。

## 🚀 核心特性

### 📋 配置管理
- **集中管理**: 统一管理所有应用的配置信息
- **环境隔离**: 支持 dev/test/prod 等多环境配置隔离
- **分组管理**: 按功能模块对配置进行分组
- **版本控制**: 完整的配置变更历史和版本管理
- **回滚功能**: 支持一键回滚到历史版本

### 🔒 安全特性
- **权限控制**: 基于角色的访问控制(RBAC)
- **配置加密**: 敏感配置支持AES加密存储
- **操作审计**: 完整的操作日志记录
- **JWT认证**: 安全的用户身份验证

### 🌐 实时推送
- **ZooKeeper**: 基于ZooKeeper实现配置实时推送
- **WebSocket**: 支持Web界面实时配置变更通知
- **客户端SDK**: 提供Java客户端SDK

### 🎨 用户体验
- **现代化UI**: 基于Vue.js + Element UI的现代化管理界面
- **操作友好**: 直观的配置编辑和管理界面
- **搜索功能**: 强大的配置搜索和过滤功能
- **批量操作**: 支持配置的批量导入导出

## 🏗️ 技术架构

### 后端技术栈
- **Spring Boot 2.7.14**: 应用框架
- **ZooKeeper + Curator**: 配置存储和分发
- **MyBatis**: ORM框架
- **MySQL**: 数据持久化
- **Redis**: 缓存和会话管理
- **Spring Security**: 安全框架
- **JWT**: 身份认证
- **WebSocket**: 实时通信

### 前端技术栈
- **Vue.js 3**: 前端框架
- **Element Plus**: UI组件库
- **Axios**: HTTP客户端
- **Vuex**: 状态管理
- **Vue Router**: 路由管理

## 📦 快速开始

### 环境要求
- Java 8+
- Maven 3.6+
- MySQL 5.7+ / 8.0+
- ZooKeeper 3.7+
- Redis 6.0+
- Node.js 16+ (前端开发)

### 1. 环境准备

#### 启动 ZooKeeper
```bash
# 下载并启动ZooKeeper
wget https://downloads.apache.org/zookeeper/zookeeper-3.7.1/apache-zookeeper-3.7.1-bin.tar.gz
tar -xzf apache-zookeeper-3.7.1-bin.tar.gz
cd apache-zookeeper-3.7.1-bin
cp conf/zoo_sample.cfg conf/zoo.cfg
bin/zkServer.sh start
```

#### 启动 MySQL
```bash
# 创建数据库并导入初始化脚本
mysql -u root -p
source src/main/resources/sql/init.sql
```

#### 启动 Redis
```bash
redis-server
```

### 2. 后端启动

```bash
# 克隆项目
git clone <repository-url>
cd config-center

# 修改配置文件
vim src/main/resources/application.yml
# 配置数据库、ZooKeeper、Redis连接信息

# 编译和启动
mvn clean compile
mvn spring-boot:run
```

### 3. 访问系统

- **管理界面**: http://localhost:8080/config-center
- **API文档**: http://localhost:8080/config-center/swagger-ui.html
- **监控面板**: http://localhost:8080/config-center/actuator

**默认管理员账号**:
- 用户名: admin
- 密码: admin123

## 📚 使用指南

### 配置管理

#### 创建应用
1. 登录系统后，点击"应用管理"
2. 点击"新增应用"，填写应用信息
3. 设置负责人和相关信息

#### 配置管理
1. 选择应用和环境
2. 创建配置组（如：database、redis、common）
3. 添加配置项，支持字符串、数字、布尔值、JSON等类型
4. 设置配置描述和标签便于管理

#### 配置发布
1. 编辑配置后点击"发布"
2. 配置会实时推送到ZooKeeper
3. 客户端会自动获取最新配置

#### 版本管理
1. 每次配置变更都会自动创建版本
2. 在"配置历史"中查看所有变更记录
3. 支持一键回滚到任意历史版本

### 权限管理

#### 用户管理
- **管理员(ADMIN)**: 拥有所有权限
- **开发者(DEVELOPER)**: 可以管理配置，不能管理用户
- **查看者(VIEWER)**: 只能查看配置

#### 操作审计
- 所有配置变更都有完整的审计日志
- 记录操作者、操作时间、变更内容等
- 支持按时间、操作者等条件查询

## 🔌 客户端接入

### Java SDK使用

```xml
<dependency>
    <groupId>com.example</groupId>
    <artifactId>config-center-client</artifactId>
    <version>1.0.0</version>
</dependency>
```

```java
@Configuration
@EnableConfigCenter
public class ConfigCenterConfig {
    
    @Value("${config.center.server-addr:localhost:8080}")
    private String serverAddr;
    
    @Value("${config.center.app-name}")
    private String appName;
    
    @Value("${config.center.environment}")
    private String environment;
    
    @Bean
    public ConfigCenterClient configCenterClient() {
        return ConfigCenterClient.builder()
            .serverAddr(serverAddr)
            .appName(appName)
            .environment(environment)
            .build();
    }
}
```

### 配置使用

```java
@Component
public class DatabaseConfig {
    
    @ConfigValue("database.jdbc.url")
    private String jdbcUrl;
    
    @ConfigValue("database.jdbc.username")
    private String username;
    
    @ConfigValue(value = "database.connection.pool.size", defaultValue = "10")
    private Integer poolSize;
    
    // 配置变更监听
    @ConfigChangeListener("database.*")
    public void onDatabaseConfigChange(ConfigChangeEvent event) {
        System.out.println("Database config changed: " + event.getKey() + " = " + event.getValue());
        // 重新初始化数据源等操作
    }
}
```

### REST API 接入

```bash
# 获取应用配置
curl -X GET "http://localhost:8080/config-center/api/configs?appName=demo-app&environment=dev"

# 获取单个配置
curl -X GET "http://localhost:8080/config-center/api/config?appName=demo-app&environment=dev&groupName=database&configKey=jdbc.url"

# 监听配置变更 (WebSocket)
ws://localhost:8080/config-center/ws/config-change
```

## 🔧 配置说明

### 应用配置文件 (application.yml)

```yaml
server:
  port: 8080
  servlet:
    context-path: /config-center

spring:
  datasource:
    url: jdbc:mysql://localhost:3306/config_center
    username: root
    password: root
  
  redis:
    host: localhost
    port: 6379

# ZooKeeper配置
zookeeper:
  connect-string: localhost:2181
  session-timeout: 60000
  connection-timeout: 15000
  namespace: config-center

# JWT配置
jwt:
  secret: configCenterJwtSecret2023
  expiration: 86400000  # 24小时

# 配置中心特定配置
config-center:
  backup:
    enabled: true
    interval: 3600000  # 1小时备份一次
  audit:
    enabled: true
    retention-days: 90  # 保留90天审计日志
```

## 📊 系统监控

### 内置监控指标
- 配置项数量统计
- 配置变更频率
- 用户操作统计
- 系统性能指标
- ZooKeeper连接状态

### 健康检查
- 数据库连接状态
- ZooKeeper连接状态
- Redis连接状态
- 应用运行状态

访问 `/actuator/health` 查看系统健康状态

## 🛠️ 开发指南

### 项目结构
```
src/
├── main/
│   ├── java/
│   │   └── com/example/configcenter/
│   │       ├── config/          # 配置类
│   │       ├── controller/      # 控制器
│   │       ├── entity/          # 实体类
│   │       ├── mapper/          # MyBatis映射
│   │       ├── service/         # 服务层
│   │       └── utils/           # 工具类
│   └── resources/
│       ├── mapper/              # MyBatis XML
│       ├── sql/                 # 数据库脚本
│       └── application.yml      # 配置文件
└── test/                        # 测试代码
```

### 扩展开发

#### 添加自定义配置类型
```java
@Component
public class CustomConfigValidator implements ConfigValidator {
    
    @Override
    public boolean supports(String dataType) {
        return "CUSTOM".equals(dataType);
    }
    
    @Override
    public boolean validate(String value) {
        // 自定义验证逻辑
        return true;
    }
}
```

#### 添加配置变更监听器
```java
@Component
public class CustomConfigChangeListener implements ConfigChangeListener {
    
    @Override
    public void onConfigChanged(String path, String data, String eventType) {
        // 自定义处理逻辑
        log.info("Config changed: {} = {}", path, data);
    }
}
```

## 🚀 部署指南

### Docker 部署

```dockerfile
FROM openjdk:8-jre-slim

COPY target/config-center-1.0.0.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# 构建镜像
docker build -t config-center:1.0.0 .

# 运行容器
docker run -d \
  --name config-center \
  -p 8080:8080 \
  -e SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/config_center \
  -e ZOOKEEPER_CONNECT_STRING=zookeeper:2181 \
  config-center:1.0.0
```

### Kubernetes 部署

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: config-center
spec:
  replicas: 2
  selector:
    matchLabels:
      app: config-center
  template:
    metadata:
      labels:
        app: config-center
    spec:
      containers:
      - name: config-center
        image: config-center:1.0.0
        ports:
        - containerPort: 8080
        env:
        - name: SPRING_DATASOURCE_URL
          value: "jdbc:mysql://mysql:3306/config_center"
        - name: ZOOKEEPER_CONNECT_STRING
          value: "zookeeper:2181"
```

## 🤝 贡献指南

1. Fork 项目
2. 创建功能分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 创建 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🆘 支持

- 📚 [用户文档](docs/user-guide.md)
- 🔧 [开发文档](docs/developer-guide.md)
- 🐛 [问题反馈](https://github.com/your-org/config-center/issues)
- 💬 [讨论区](https://github.com/your-org/config-center/discussions)

## 📈 路线图

- [ ] **v1.1.0**
  - [ ] 配置模板功能
  - [ ] 配置比较和合并
  - [ ] 多数据源支持

- [ ] **v1.2.0**
  - [ ] 图形化配置依赖管理
  - [ ] 配置影响分析
  - [ ] 智能配置推荐

- [ ] **v2.0.0**
  - [ ] 微服务架构重构
  - [ ] 多租户支持
  - [ ] 云原生部署支持

---

**配置中心团队** - 让配置管理更简单、更安全、更高效！
