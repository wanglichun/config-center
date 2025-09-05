#!/bin/bash

# MySQL UTF-8 连接脚本
# 用法: ./mysql-utf8.sh [SQL命令]

if [ $# -eq 0 ]; then
    # 交互模式
    echo "进入MySQL交互模式 (UTF-8编码)"
    docker exec -it -e LC_ALL=C.UTF-8 config-center-mysql mysql -uroot -proot123 --default-character-set=utf8mb4
else
    # 执行SQL命令
    docker exec -e LC_ALL=C.UTF-8 config-center-mysql mysql -uroot -proot123 --default-character-set=utf8mb4 -e "SET NAMES utf8mb4; $*"
fi
