#!/bin/bash

# ZooKeeper集群启动脚本
# 用于快速启动和管理ZooKeeper集群

set -e

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 函数：打印彩色消息
print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# 检查Docker是否安装
check_docker() {
    if ! command -v docker &> /dev/null; then
        print_error "Docker未安装，请先安装Docker"
        exit 1
    fi
    
    if ! command -v docker-compose &> /dev/null; then
        print_error "Docker Compose未安装，请先安装Docker Compose"
        exit 1
    fi
    
    print_success "Docker环境检查通过"
}

# 启动ZooKeeper集群
start_cluster() {
    print_info "正在启动ZooKeeper集群..."
    
    # 检查配置文件是否存在
    if [ ! -f "docker-compose-zk-cluster.yml" ]; then
        print_error "docker-compose-zk-cluster.yml 文件不存在"
        exit 1
    fi
    
    # 启动集群
    docker-compose -f docker-compose-zk-cluster.yml up -d
    
    print_success "ZooKeeper集群启动命令已执行"
    
    # 等待集群启动
    print_info "等待集群启动完成..."
    sleep 10
    
    # 检查集群状态
    check_cluster_status
}

# 检查集群状态
check_cluster_status() {
    print_info "检查ZooKeeper集群状态..."
    
    # 检查容器状态
    containers=$(docker-compose -f docker-compose-zk-cluster.yml ps --services)
    all_running=true
    
    for container in $containers; do
        status=$(docker-compose -f docker-compose-zk-cluster.yml ps -q $container | xargs docker inspect -f '{{.State.Status}}' 2>/dev/null || echo "not_found")
        if [ "$status" = "running" ]; then
            print_success "$container 容器正在运行"
        else
            print_error "$container 容器状态异常: $status"
            all_running=false
        fi
    done
    
    if [ "$all_running" = true ]; then
        print_success "所有ZooKeeper节点都在正常运行"
        
        # 检查ZooKeeper服务状态
        print_info "检查ZooKeeper服务状态..."
        check_zk_service
    else
        print_error "部分节点启动失败，请检查日志"
        show_logs
    fi
}

# 检查ZooKeeper服务状态
check_zk_service() {
    local nodes=("zk1:2181" "zk2:2181" "zk3:2181")
    local ports=("2181" "2182" "2183")
    
    for i in ${!nodes[@]}; do
        local node=${nodes[$i]}
        local port=${ports[$i]}
        local container="zk$((i+1))"
        
        print_info "检查 $container (localhost:$port) 状态..."
        
        # 检查端口是否可达
        if nc -z localhost $port 2>/dev/null; then
            print_success "$container 端口 $port 可达"
            
            # 获取ZooKeeper状态
            local zk_status=$(docker exec -t $container /bin/bash -c "echo 'stat' | nc localhost 2181" 2>/dev/null | grep "Mode:" | awk '{print $2}' | tr -d '\r\n' || echo "unknown")
            if [ ! -z "$zk_status" ] && [ "$zk_status" != "unknown" ]; then
                print_success "$container ZooKeeper状态: $zk_status"
            else
                print_warning "$container ZooKeeper状态检查失败"
            fi
        else
            print_error "$container 端口 $port 不可达"
        fi
    done
}

# 停止集群
stop_cluster() {
    print_info "正在停止ZooKeeper集群..."
    docker-compose -f docker-compose-zk-cluster.yml down
    print_success "ZooKeeper集群已停止"
}

# 重启集群
restart_cluster() {
    print_info "正在重启ZooKeeper集群..."
    stop_cluster
    sleep 3
    start_cluster
}

# 显示日志
show_logs() {
    print_info "显示ZooKeeper集群日志..."
    docker-compose -f docker-compose-zk-cluster.yml logs --tail=50
}

# 清理资源
cleanup() {
    print_info "清理ZooKeeper集群资源..."
    docker-compose -f docker-compose-zk-cluster.yml down -v
    print_success "ZooKeeper集群资源已清理"
}

# 进入ZooKeeper Shell
zk_shell() {
    local node=${1:-zk1}
    local port=${2:-2181}
    
    print_info "连接到 $node 的ZooKeeper Shell..."
    print_info "使用以下命令测试:"
    print_info "  ls /"
    print_info "  create /test 'hello'"
    print_info "  get /test"
    print_info "  delete /test"
    print_info "  quit"
    
    docker exec -it $node /opt/kafka/bin/zookeeper-shell.sh localhost:$port
}

# 显示帮助信息
show_help() {
    echo "ZooKeeper集群管理脚本"
    echo ""
    echo "用法: $0 [命令]"
    echo ""
    echo "命令:"
    echo "  start     启动ZooKeeper集群"
    echo "  stop      停止ZooKeeper集群"
    echo "  restart   重启ZooKeeper集群"
    echo "  status    检查集群状态"
    echo "  logs      显示集群日志"
    echo "  cleanup   清理集群资源（包括数据卷）"
    echo "  shell     进入ZooKeeper命令行（默认连接zk1）"
    echo "  help      显示帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 start              # 启动集群"
    echo "  $0 status             # 检查状态"
    echo "  $0 shell              # 连接到zk1"
    echo "  $0 shell zk2          # 连接到zk2"
    echo ""
}

# 主函数
main() {
    case "${1:-help}" in
        start)
            check_docker
            start_cluster
            ;;
        stop)
            stop_cluster
            ;;
        restart)
            check_docker
            restart_cluster
            ;;
        status)
            check_cluster_status
            ;;
        logs)
            show_logs
            ;;
        cleanup)
            cleanup
            ;;
        shell)
            zk_shell ${2:-zk1} ${3:-2181}
            ;;
        help|--help|-h)
            show_help
            ;;
        *)
            print_error "未知命令: $1"
            show_help
            exit 1
            ;;
    esac
}

# 执行主函数
main "$@" 