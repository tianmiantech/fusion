#!/usr/bin/env bash

## -x: Debug mode
## -e: exit the script if any statement returns a non-true return value
[ x"${DEBUG}" == x"true" ] && set -ex || set -e

## application 值为 Jenkins 编译时传入，对应 Jenkins 的 JOB_BASE_NAME，即 APP_NAME
application=$1

## --- 该分割线以上代码不用动 ---

## 检索当前要编译的项目，再去调具体子项目的编译脚本
case "$application" in
    fusion)
        /bin/bash ./fusion-service/assembly/scripts/install.sh $application
        ;;
    *)
        exit 1
        ;;
esac

exit 0
