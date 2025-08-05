#!/bin/bash

echo "🔍 日志查看工具"
echo "================"

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 显示帮助信息
show_help() {
    echo "用法: $0 [选项]"
    echo ""
    echo "选项:"
    echo "  -a, --all              查看所有日志"
    echo "  -t, --trace <traceId>  按TraceId查询"
    echo "  -c, --category <type>  按组件类型查询 (API_REQUEST|REDIS|MYSQL|ZOOKEEPER)"
    echo "  -s, --status <code>    按状态码查询"
    echo "  -d, --duration <ms>    查询响应时间大于指定毫秒的日志"
    echo "  -n, --number <num>     限制返回数量 (默认10)"
    echo "  -h, --help             显示此帮助信息"
    echo ""
    echo "示例:"
    echo "  $0 -a                    # 查看所有日志"
    echo "  $0 -t trace_demo_api     # 查看指定TraceId的日志"
    echo "  $0 -c REDIS              # 查看Redis相关日志"
    echo "  $0 -s 200                # 查看状态码为200的日志"
    echo "  $0 -d 100                # 查看响应时间大于100ms的日志"
}

# 格式化输出日志
format_log() {
    local log_data="$1"
    echo -e "${BLUE}=== 日志详情 ===${NC}"
    echo "$log_data" | jq -r '
        "时间: " + ._source.timestamp +
        "\nTraceId: " + ._source.traceId +
        "\n组件: " + ._source.category +
        "\n操作: " + ._source.operation +
        "\n耗时: " + (.._source.duration | tostring) + "ms"
    '
    
    # 显示详细信息
    if [ "$(echo "$log_data" | jq -r '._source.data')" != "null" ]; then
        echo -e "\n${YELLOW}详细信息:${NC}"
        echo "$log_data" | jq -r '._source.data | to_entries[] | "  " + .key + ": " + (.value | tostring)'
    fi
    
    echo -e "\n${GREEN}---${NC}\n"
}

# 主函数
main() {
    local query=""
    local size=10
    
    # 解析参数
    while [[ $# -gt 0 ]]; do
        case $1 in
            -a|--all)
                query=""
                shift
                ;;
            -t|--trace)
                query="traceId:$2"
                shift 2
                ;;
            -c|--category)
                query="category:$2"
                shift 2
                ;;
            -s|--status)
                query="status_code:$2"
                shift 2
                ;;
            -d|--duration)
                query="duration:>$2"
                shift 2
                ;;
            -n|--number)
                size=$2
                shift 2
                ;;
            -h|--help)
                show_help
                exit 0
                ;;
            *)
                echo -e "${RED}错误: 未知选项 $1${NC}"
                show_help
                exit 1
                ;;
        esac
    done
    
    # 构建查询URL
    local url="localhost:9200/logs-*/_search?pretty&size=$size"
    if [ -n "$query" ]; then
        url="$url&q=$query"
    fi
    
    echo -e "${GREEN}查询URL: $url${NC}"
    echo ""
    
    # 执行查询
    local response=$(curl -s -X GET "$url")
    
    # 检查是否有结果
    local total=$(echo "$response" | jq -r '.hits.total.value')
    
    if [ "$total" -eq 0 ]; then
        echo -e "${YELLOW}没有找到匹配的日志${NC}"
        return
    fi
    
    echo -e "${GREEN}找到 $total 条日志记录${NC}"
    echo ""
    
    # 格式化输出每条日志
    echo "$response" | jq -c '.hits.hits[]' | while read -r log; do
        format_log "$log"
    done
}

# 检查依赖
if ! command -v jq &> /dev/null; then
    echo -e "${RED}错误: 需要安装 jq 工具${NC}"
    echo "安装命令: brew install jq (macOS) 或 apt-get install jq (Ubuntu)"
    exit 1
fi

# 检查Elasticsearch连接
if ! curl -s "localhost:9200" > /dev/null; then
    echo -e "${RED}错误: 无法连接到Elasticsearch，请确保服务正在运行${NC}"
    exit 1
fi

# 运行主函数
main "$@" 