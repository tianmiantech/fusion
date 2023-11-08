/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.welab.wefe.common.web.api.dev;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * 在打包之前，通过脚本输出 git log 到 resources 目录，通过此接口浏览 git log。
 *
 * # 输出 git 日志到 resources 文件夹
 * if command -v git >/dev/null 2>&1; then
 * echo 'exists git'
 * echo "${workdir}/board/board-service/src/main/resources/git.log"
 * rm -f ${workdir}/board/board-service/src/main/resources/git.log
 * git log --stat -5 > ${workdir}/board/board-service/src/main/resources/git.log
 * else
 * echo 'no exists git'
 * fi
 *
 * @author zane.luo
 * @date 2023/2/28
 */
@Api(path = "git_log", name = "展示 git log", desc = "git log 来自打包时脚本输出到 resources/git.log 文件")
public class GitLogApi extends AbstractNoneInputApi<GitLogApi.Output> {
    @Override
    protected ApiResult<GitLogApi.Output> handle() throws StatusCodeWithException {
        String log;
        try {
            log = IOUtils.toString(
                    Thread
                            .currentThread()
                            .getContextClassLoader()
                            .getResourceAsStream("git.log"),
                    StandardCharsets.UTF_8
            );
        } catch (IOException e) {
            log = "no git.log file.";
        }
        return success(new Output(log));
    }

    public static class Output {
        public String log;

        public Output() {
        }

        public Output(String log) {
            this.log = log;
        }
    }
}
