# 在三台容器中运行监听器

## 当前状态
✅ **三台容器已启动并运行：**
- machine-001: 运行中
- machine-002: 运行中  
- machine-003: 运行中

✅ **监听脚本已复制到容器：**
- ConfigCenterPushListenerEn-fixed-v2.java
- apache-zookeeper-3.7.1-bin/ (ZooKeeper库)

## 手动运行监听器

### 在 machine-001 中运行监听器：
```bash
docker exec -it machine-001 bash
cd /app
export INSTANCE_ID=machine-001
java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2
```

### 在 machine-002 中运行监听器：
```bash
docker exec -it machine-002 bash
cd /app
export INSTANCE_ID=machine-002
java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2
```

### 在 machine-003 中运行监听器：
```bash
docker exec -it machine-003 bash
cd /app
export INSTANCE_ID=machine-003
java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2
```

## 后台运行监听器

### 在 machine-001 后台运行：
```bash
docker exec -d machine-001 bash -c 'cd /app && export INSTANCE_ID=machine-001 && nohup java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2 > listener-001.log 2>&1 &'
```

### 在 machine-002 后台运行：
```bash
docker exec -d machine-002 bash -c 'cd /app && export INSTANCE_ID=machine-002 && nohup java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2 > listener-002.log 2>&1 &'
```

### 在 machine-003 后台运行：
```bash
docker exec -d machine-003 bash -c 'cd /app && export INSTANCE_ID=machine-003 && nohup java -cp ".:apache-zookeeper-3.7.1-bin/lib/*" ConfigCenterPushListenerEn-fixed-v2 > listener-003.log 2>&1 &'
```

## 检查监听器状态

### 检查进程：
```bash
# 检查 machine-001 进程
docker exec machine-001 ps aux | grep java

# 检查 machine-002 进程  
docker exec machine-002 ps aux | grep java

# 检查 machine-003 进程
docker exec machine-003 ps aux | grep java
```

### 查看日志：
```bash
# 查看 machine-001 日志
docker logs machine-001

# 查看 machine-002 日志
docker logs machine-002

# 查看 machine-003 日志
docker logs machine-003
```

### 查看监听器日志：
```bash
# 查看 machine-001 监听器日志
docker exec machine-001 cat /app/listener-001.log

# 查看 machine-002 监听器日志
docker exec machine-002 cat /app/listener-002.log

# 查看 machine-003 监听器日志
docker exec machine-003 cat /app/listener-003.log
```

## 监听器功能

每个监听器将：
1. 连接到ZooKeeper服务器 (config-center-zookeeper:2181)
2. 监听配置变更通知 (/config-center/notifications)
3. 将配置保存到本地文件 (./configs/)
4. 报告配置状态到ZooKeeper (/config-center/container-status)

## 环境变量

- `INSTANCE_ID`: 机器标识 (machine-001, machine-002, machine-003)
- `ZK_SERVER`: ZooKeeper服务器地址 (默认: config-center-zookeeper:2181)
- `LOCAL_CONFIG_DIR`: 本地配置目录 (默认: ./configs)

## 停止监听器

```bash
# 停止所有监听器进程
docker exec machine-001 pkill -f ConfigCenterPushListenerEn
docker exec machine-002 pkill -f ConfigCenterPushListenerEn  
docker exec machine-003 pkill -f ConfigCenterPushListenerEn
``` 