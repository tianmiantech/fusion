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
package com.welab.fusion.service.api.download;

import com.alibaba.fastjson.annotation.JSONField;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.base.DownloadConfig;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author zane.luo
 * @date 2023/11/30
 */
@Api(path = "download/chunk", name = "下载文件分片")
public class DownloadChunkApi extends AbstractApi<DownloadChunkApi.Input, DownloadChunkApi.Output> {
    @Override
    protected ApiResult<DownloadChunkApi.Output> handle(DownloadChunkApi.Input input) throws Exception {
        File file = input.getFile();
        // 根据 input 指定的分片号对 file 进行分片
        FileInputStream fileInputStream = new FileInputStream(file);
        fileInputStream.skip(input.chunkIndex * DownloadConfig.CHUNK_SIZE_IN_MB * 1024 * 1024);
// byte[] bytes = new byte[DownloadConfig.CHUNK_SIZE_IN_MB * 1024 * 1024];

        // return ResponseEntity
        //         .ok()
        //         .contentType(MediaType.parseMediaType(ContentType.of(resourceName)))
        //         .body(new InputStreamResource(
        //                 inputStream
        //         ));
        return null;
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "文件路径", require = true)
        public String filePath;
        @Check(name = "分片序号", require = true, desc = "分片序号，从0开始")
        public int chunkIndex;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();
            File file = getFile();
            if (!file.exists()) {
                StatusCode.FILE_DOES_NOT_EXIST.throwException("文件不存在：" + filePath);
            }
        }

        @JSONField(serialize = false)
        public File getFile() throws StatusCodeWithException {
            return FileSystem.getRootDir().resolve(filePath).toFile();
        }
    }

    public static class Output {

    }
}
