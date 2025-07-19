#!/bin/bash

CONTAINER_NAME="simple-container"
CONTAINER_HOSTNAME="simple-host"
CONTAINER_PORT="8081"

echo "=== 普通容器管理脚本 ==="

case "$1" in
    "start")
        echo "启动普通容器..."
        docker start $CONTAINER_NAME
        ;;
    "stop")
        echo "停止普通容器..."
        docker stop $CONTAINER_NAME
        ;;
    "restart")
        echo "重启普通容器..."
        docker restart $CONTAINER_NAME
        ;;
    "status")
        echo "查看容器状态..."
        docker ps -a | grep $CONTAINER_NAME
        ;;
    "logs")
        echo "查看容器日志..."
        docker logs --tail=20 $CONTAINER_NAME
        ;;
    "exec")
        echo "进入容器..."
        docker exec -it $CONTAINER_NAME sh
        ;;
    "info")
        echo "容器详细信息:"
        echo "容器名称: $CONTAINER_NAME"
        echo "主机名: $CONTAINER_HOSTNAME"
        echo "端口映射: $CONTAINER_PORT:80"
        echo "IP地址: $(docker inspect $CONTAINER_NAME --format='{{.NetworkSettings.IPAddress}}')"
        echo "状态: $(docker inspect $CONTAINER_NAME --format='{{.State.Status}}')"
        echo "创建时间: $(docker inspect $CONTAINER_NAME --format='{{.Created}}')"
        ;;
    "volumes")
        echo "查看数据卷..."
        docker volume ls | grep simple
        ;;
    "clean")
        echo "清理容器和数据卷..."
        docker stop $CONTAINER_NAME 2>/dev/null
        docker rm $CONTAINER_NAME 2>/dev/null
        docker volume rm simple_data simple_logs simple_config 2>/dev/null
        echo "清理完成"
        ;;
    *)
        echo "用法: $0 {start|stop|restart|status|logs|exec|info|volumes|clean}"
        echo ""
        echo "命令说明:"
        echo "  start   - 启动容器"
        echo "  stop    - 停止容器"
        echo "  restart - 重启容器"
        echo "  status  - 查看状态"
        echo "  logs    - 查看日志"
        echo "  exec    - 进入容器"
        echo "  info    - 查看详细信息"
        echo "  volumes - 查看数据卷"
        echo "  clean   - 清理容器和数据卷"
        ;;
esac 