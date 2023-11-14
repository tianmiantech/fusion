#!/usr/bin/env bash
SCRIPT_PATH=$(dirname "$(readlink -f "$0") ")
WEB_PROJECT_CODE="$1"
# 获取脚本所在路径的父级目录
PARENT_PATH=$(dirname $(dirname "$SCRIPT_PATH")) 
echo "脚本路径为：$PARENT_PATH"


cd $PARENT_PATH
bash $PARENT_PATH/assembly/scripts/install.sh "$WEB_PROJECT_CODE"