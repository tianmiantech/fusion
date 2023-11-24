/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.fusion.service.api.file;


import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.file.security.FileSecurityScanner;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractWithFilesApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.FileUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * The front end uses the simple-uploader component
 * doc：https://github.com/simple-uploader/Uploader/blob/develop/README_zh-CN.md#%E5%A4%84%E7%90%86-get-%E6%88%96%E8%80%85-test-%E8%AF%B7%E6%B1%82
 *
 * @author zane.luo
 */
@Api(
        path = "file/upload",
        name = "分片上传文件",
        desc = "支持大文件分片上传，支持断点续传，所有分片上传完毕后需调用 file/merge 合并分片。\n" +
                "POST请求：上传分片\n" +
                "GET请求：检查分片是否已存在， http code 299 表示已存在，http code 200 表示不存在。"
)
public class UploadFileApi extends AbstractApi<UploadFileApi.Input, UploadFileApi.Output> {

    @Override
    protected ApiResult<Output> handle(Input input) throws StatusCodeWithException {

        // 检查文件是否是支持的文件类型
        try {
            FileSecurityScanner.checkIsAllowFileType(input.filename);
        } catch (Exception e) {
            return fail(e)
                    .setHttpCode(599);
        }

        switch (input.method) {
            case "POST":
                return saveChunk(input);

            case "GET":
                return checkChunk(input);

            default:
                throw new StatusCodeWithException(StatusCode.UNEXPECTED_ENUM_CASE);
        }


    }

    /**
     * Check if the chunk already exists
     */
    private ApiResult<Output> checkChunk(Input input) {
        Integer chunkId = input.chunkNumber;
        if (chunkId == null) {
            chunkId = 0;
        }

        File outFile = FileSystem.getTempDir()
                .resolve(input.identifier)
                .resolve(chunkId + ".part")
                .toFile();

        if (outFile.exists()) {
            return success()
                    .setMessage("该分片已存在");
        } else {
            return success()
                    .setHttpCode(299)
                    .setMessage("该分片不存在");
        }
    }

    /**
     * save chunk
     */
    private ApiResult<Output> saveChunk(Input input) throws StatusCodeWithException {
        MultipartFile inputFile = input.getFirstFile();

        Integer chunkNumber = input.chunkNumber;
        if (chunkNumber == null) {
            chunkNumber = 0;
        }

        Path outputDir = FileSystem.getTempDir()
                .resolve(input.identifier);
        FileUtil.createDir(outputDir.toString());

        File outFile = outputDir.resolve(chunkNumber + ".part").toFile();

        try {
            InputStream inputStream = inputFile.getInputStream();
            FileUtils.copyInputStreamToFile(inputStream, outFile);
        } catch (IOException e) {
            LOG.error(e.getMessage(), e);
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        return success(new Output(inputFile.getSize()));
    }

    public static class Output {
        private long length;

        public Output(long length) {
            this.length = length;
        }

        public long getLength() {
            return length;
        }

        public void setLength(long length) {
            this.length = length;
        }
    }

    public static class Input extends AbstractWithFilesApiInput {
        @Check(name = "分片编号")
        public Integer chunkNumber;
        @Check(name = "分片大小")
        public Long chunkSize;
        @Check(name = "当前分片大小")
        public Long currentChunkSize;
        @Check(name = "总大小")
        public Long totalSize;
        @Check(name = "文件标识", require = true)
        public String identifier;
        @Check(name = "文件名", require = true)
        public String filename;
        @Check(name = "文件用途", require = true)
        public FileSystem.UseType uploadFileUseType;
    }
}
