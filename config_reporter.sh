#!/bin/bash

# 配置上报脚本 - 每10秒上报一次订阅的配置内容
# 功能：扫描本地订阅配置，在内容前后加单引号后上报到ZooKeeper

SCRIPT_NAME="config_reporter"
MACHINE_NAME="${MACHINE_NAME:-machine-001}"
ZK_HOST="config-center-zookeeper-native:2181"
ZK_CLI="/app/zookeeper-bin/bin/zkCli.sh"
REPORT_INTERVAL=10

# 日志函数
log_info() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] 📋 $1"
}

log_error() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ❌ $1"
}

log_success() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] ✅ $1"
}

# 创建ZK节点路径
create_zk_path() {
    local path="$1"
    local parent_path=$(dirname "$path")
    
    if [ "$parent_path" != "/" ] && [ "$parent_path" != "." ]; then
        create_zk_path "$parent_path"
    fi
    
    # 尝试创建节点（如果不存在）
    echo "create $path ''" | "$ZK_CLI" -server "$ZK_HOST" >/dev/null 2>&1
}

# 上报单个配置到ZK（内容前后加单引号）
report_config_to_zk() {
    local local_file="$1"
    local zk_path="$2"
    
    if [ ! -f "$local_file" ]; then
        log_error "本地配置文件不存在: $local_file"
        return 1
    fi
    
    # 读取配置内容
    local content=$(cat "$local_file")
    
    if [ -z "$content" ]; then
        log_error "配置文件为空: $local_file"
        return 1
    fi
    
    log_info "上报配置: $zk_path"
    log_info "原始内容长度: ${#content} 字符"
    
    # 在内容前后加上单引号
    local quoted_content="'$content'"
    log_info "加单引号后长度: ${#quoted_content} 字符"
    
    # 确保父路径存在
    create_zk_path "$zk_path"
    
    # 先尝试创建节点（如果不存在）
    echo "create $zk_path ''" | "$ZK_CLI" -server "$ZK_HOST" >/dev/null 2>&1
    
    # 使用双引号包围的内容进行set操作
    local set_result=$(echo "set $zk_path $quoted_content" | "$ZK_CLI" -server "$ZK_HOST" 2>&1)
    
    # 检查结果
    if echo "$set_result" | grep -q "Exception\|Error\|InvalidACL"; then
        log_error "上报失败: $zk_path"
        echo "$set_result" | tail -3
        return 1
    else
        log_success "上报成功: $zk_path (已加单引号)"
        return 0
    fi
}

# 扫描并上报所有订阅的配置
scan_and_report_configs() {
    local base_dir="/app/config/subscribed"
    local report_count=0
    
    log_info "开始扫描订阅配置目录: $base_dir"
    
    if [ ! -d "$base_dir" ]; then
        log_error "订阅配置目录不存在: $base_dir"
        return 1
    fi
    
    # 查找所有配置文件（避免子shell问题）
    local config_files=($(find "$base_dir" -type f))
    
    for config_file in "${config_files[@]}"; do
        # 计算相对路径
        local rel_path=${config_file#$base_dir/}
        
        # 构造ZK上报路径
        local zk_report_path="/config-center/machines/$MACHINE_NAME/$rel_path"
        
        # 上报配置
        if report_config_to_zk "$config_file" "$zk_report_path"; then
            ((report_count++))
        fi
    done
    
    log_info "本轮上报完成，共处理 $report_count 个配置"
}

# 主循环
main() {
    log_info "启动配置上报服务（单引号模式）"
    log_info "机器名称: $MACHINE_NAME"
    log_info "上报间隔: ${REPORT_INTERVAL}秒"
    log_info "ZooKeeper: $ZK_HOST"
    
    # 信号处理
    trap 'log_info "收到停止信号，正在退出..."; exit 0' TERM INT
    
    while true; do
        log_info "========== 开始第 $((++cycle_count)) 轮配置上报 =========="
        
        scan_and_report_configs
        
        log_info "等待 ${REPORT_INTERVAL}秒后进行下一轮上报..."
        sleep "$REPORT_INTERVAL"
    done
}

# 初始化变量
cycle_count=0

# 启动主程序
main "$@"
