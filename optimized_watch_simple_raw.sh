#!/bin/bash

# 最简化原样提取版事件驱动ZooKeeper Watch脚本
set -e

ZK_HOST="config-center-zookeeper-native:2181"
ROOT_PATH="/config-center/configs"
CONFIG_BASE_DIR="/app/config/subscribed"
LOG_FILE="/app/logs/optimized_watch_simple_raw.log"
ZK_CLI="/app/zookeeper-bin/bin/zkCli.sh"

WATCH_NODE="${1:-test/test_key_1}"
FULL_NODE_PATH="$ROOT_PATH/$WATCH_NODE"

WATCH_TIMEOUT=30
MAX_RETRY_COUNT=3

mkdir -p "$CONFIG_BASE_DIR"
mkdir -p "/app/logs"

log() {
    echo "[$(date '+%Y-%m-%d %H:%M:%S')] $1" | tee -a "$LOG_FILE"
}

# 最简化原样提取函数 - 提取ZK CLI显示的任何内容
get_node_value_simple_raw() {
    local node_path="$1"
    local temp_file="/tmp/zk_simple_raw_$$"
    local retry_count=0
    
    while [ $retry_count -lt $MAX_RETRY_COUNT ]; do
        echo "get $node_path" | "$ZK_CLI" -server "$ZK_HOST" > "$temp_file" 2>&1
        sleep 2
        
        # 最简化提取逻辑：查找]后面的所有非空行，直到[zk:出现
        local value=""
        local found_start=false
        
        while IFS= read -r line; do
            # 如果找到了 ] get /config-center... 这样的行，开始提取下一行
            if [[ "$line" == *"] get /config-center/"* ]]; then
                found_start=true
                continue
            fi
            
            # 如果找到了下一个 [zk: 提示符，停止提取
            if [[ "$line" == *"[zk:"* ]] && [ "$found_start" = true ]; then
                break
            fi
            
            # 如果在提取区间内，并且不是空行和日志行，就是我们要的数据
            if [ "$found_start" = true ]; then
                if [[ ! "$line" =~ ^[0-9]{2}:[0-9]{2}:[0-9]{2} ]] && [[ "$line" != "" ]] && [[ "$line" != *"DEBUG"* ]] && [[ "$line" != *"INFO"* ]]; then
                    # 累积多行内容
                    if [ -z "$value" ]; then
                        value="$line"
                    else
                        value="$value$line"
                    fi
                fi
            fi
        done < "$temp_file"
        
        # 清理空格
        value=$(echo "$value" | sed 's/^[ \t]*//;s/[ \t]*$//')
        
        if [ -n "$value" ] && [ "$value" != "null" ]; then
            rm -f "$temp_file"
            echo "$value"
            return 0
        fi
        
        retry_count=$((retry_count + 1))
        log "⚠️ 重试获取配置值 ($retry_count/$MAX_RETRY_COUNT) - 提取到: '$value'"
        sleep 1
    done
    
    rm -f "$temp_file"
    log "❌ 提取失败"
    echo ""
}

create_config_file() {
    local zk_path="$1"
    local config_value="$2"
    
    local relative_path=$(echo "$zk_path" | sed "s|$ROOT_PATH/||g")
    local file_path="$CONFIG_BASE_DIR/$relative_path"
    local dir_path=$(dirname "$file_path")
    
    mkdir -p "$dir_path"
    echo "$config_value" > "$file_path"
    chmod 644 "$file_path"
    
    log "✅ Config updated (SIMPLE-RAW): $file_path = $config_value"
}

# 简化版事件驱动watch
simple_raw_event_watch() {
    local node_path="$1"
    log "🚀 Starting SIMPLE-RAW event watch on: $node_path"
    
    while true; do
        local temp_file="/tmp/zk_watch_simple_raw_$$"
        
        log "📡 Setting up SIMPLE-RAW watch..."
        
        (echo "get -w $node_path"; sleep $WATCH_TIMEOUT) | "$ZK_CLI" -server "$ZK_HOST" > "$temp_file" 2>&1 &
        local zk_pid=$!
        
        sleep 3
        
        # 使用简化原样函数提取当前值
        local current_value=$(get_node_value_simple_raw "$node_path")
        
        if [ -n "$current_value" ] && [ "$current_value" != "null" ]; then
            log "📋 SIMPLE-RAW Current value: $current_value"
            create_config_file "$node_path" "$current_value"
        fi
        
        log "👁️ Waiting for events (SIMPLE-RAW)..."
        local wait_time=0
        local event_detected=false
        
        while [ $wait_time -lt $WATCH_TIMEOUT ]; do
            if grep -q "NodeDataChanged" "$temp_file" 2>/dev/null; then
                log "🔔 NodeDataChanged event detected! Processing immediately..."
                event_detected=true
                break
            fi
            
            sleep 1
            wait_time=$((wait_time + 1))
        done
        
        kill $zk_pid 2>/dev/null || true
        wait $zk_pid 2>/dev/null || true
        rm -f "$temp_file"
        
        if [ "$event_detected" = true ]; then
            log "🔄 事件检测到，立即获取新值..."
            local new_value=$(get_node_value_simple_raw "$node_path")
            if [ -n "$new_value" ] && [ "$new_value" != "null" ]; then
                log "🆕 New SIMPLE-RAW value: $new_value"
                create_config_file "$node_path" "$new_value"
            fi
        else
            log "⏰ Watch超时(30s)，重新设置..."
        fi
        
        sleep 1
    done
}

cleanup() {
    log "🛑 Cleanup SIMPLE-RAW watch..."
    rm -f /tmp/zk_*_simple_raw_$$
    exit 0
}

trap cleanup SIGTERM SIGINT

log "🎯 === SIMPLE-RAW Event-Driven Watch Started ==="
log "📍 Watching: $FULL_NODE_PATH"
log "📝 Mode: 最简化原样提取，保持ZK CLI输出的任何内容"

# 获取初始值
initial_value=$(get_node_value_simple_raw "$FULL_NODE_PATH")
if [ -n "$initial_value" ] && [ "$initial_value" != "null" ]; then
    log "🔰 Initial SIMPLE-RAW value: $initial_value"
    create_config_file "$FULL_NODE_PATH" "$initial_value"
fi

# 启动简化原样版监听
simple_raw_event_watch "$FULL_NODE_PATH"
