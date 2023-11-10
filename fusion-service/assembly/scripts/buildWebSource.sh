#!/usr/bin/env bash


SCRIPT_PATH=$(dirname "$(readlink -f "$0") ")

echo "当前脚本所在的目录为：$SCRIPT_PATH"
# 获取脚本所在路径的父级目录
JAVA_PROJECT_PATH=$(dirname $(dirname "$SCRIPT_PATH")) 


# 将 "fusion-service" 替换为另一个值
WEB_SCRIPT_PATH=${SCRIPT_PATH//fusion-service/fusion-website}

echo "web端脚本所在的目录为：$WEB_SCRIPT_PATH"
# 获取脚本所在路径的父级目录
WEB_PROJECT_PATH=$(dirname $(dirname "$WEB_SCRIPT_PATH")) 

echo "编译文件所在的目录：$WEB_PROJECT_PATH"

# WBE_OLD_DIST=$WEB_PROJECT_PATH/dist
# if [ -d "$WBE_OLD_DIST" ]; then
#   echo "web端dist目录存在，即将进行清理..."
#   rm -r "$WBE_OLD_DIST"
# fi

bash $WEB_SCRIPT_PATH/buildLocal.sh

echo " =======  Web编译执行完毕 ======"

# TARGET_DIST=$JAVA_PROJECT_PATH/src/main/resources/
# if [ -d "$TARGET_DIST/website" ]; then
#   echo "JAVA端目录存在，即将进行清理..."
#   rm -r "$TARGET_DIST/website"
# fi

#找到编译后的 website目录
WBE_DIST=$(find $WEB_PROJECT_PATH/dist -type d -name "website")
if [ -d "$WBE_DIST" ]; then
    echo "资源文件所在目录为:$WBE_DIST"
    
    echo "JAVA资源目录为：$TARGET_DIST"
    cp -rf $WBE_DIST $TARGET_DIST
    echo "目录复制完成"
fi





