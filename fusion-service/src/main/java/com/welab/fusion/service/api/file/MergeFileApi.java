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

import cn.hutool.core.io.unit.DataSize;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.file.security.FileSecurityScanner;
import com.welab.wefe.common.CommonThreadPool;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

/**
 * @author zane.luo
 */
@Api(path = "file/merge", name = "Merge the chunks after the file is uploaded")
public class MergeFileApi extends AbstractApi<MergeFileApi.Input, MergeFileApi.Output> {

    @Override
    protected ApiResult<Output> handle(Input input) throws Exception {

        String mergedFileName = UUID.randomUUID() + "-" + input.filename;

        File dir = FileSystem.getBaseDir(input.uploadFileUseType)
                .resolve(input.identifier)
                .toFile();

        if (!dir.exists()) {
            StatusCode.DATA_NOT_FOUND.throwException("文件不存在：" + input.identifier);
        }
        
        File[] parts = dir.listFiles();

        File mergedFile = FileSystem.getBaseDir(input.uploadFileUseType)
                .resolve(mergedFileName)
                .toFile();

        if (mergedFile.exists()) {
            return success(Output.of(null, mergedFileName));
        }

        try {
            for (int i = 1; i <= parts.length; i++) {
                File part = FileSystem.getBaseDir(input.uploadFileUseType)
                        .resolve(input.identifier)
                        .resolve(i + ".part")
                        .toFile();

                // append chunk to the target file
                FileOutputStream stream = new FileOutputStream(mergedFile, true);
                FileUtils.copyFile(part, stream);
                stream.close();
            }

            // delete chunk
            FileUtils.deleteDirectory(dir);

            // 更新最后修改时间，避免被垃圾文件清理模块删除。
            mergedFile.setLastModified(System.currentTimeMillis());

        } catch (IOException e) {
            throw new StatusCodeWithException(StatusCode.SYSTEM_ERROR, e.getMessage());
        }

        // 出于性能考虑，不要用 excel 做太大的事，资源占用很多，用户也要等很久。
        if (FileUtil.isExcel(mergedFile)) {
            if (DataSize.ofBytes(mergedFile.length()).toMegabytes() > 12) {
                mergedFile.delete();
                StatusCode.PARAMETER_VALUE_INVALID
                        .throwException("仅支持小于 12M 的 Excel 文件，大文件请转 csv 后重试。");
            }
        }

        // 检查上传的文件是否安全
        String scanSessionId = FileSecurityScanner.createScanSession();
        // 图片文件同步执行
        if (FileUtil.isImage(mergedFile)) {
            FileSecurityScanner.scan(scanSessionId, mergedFile);
        }
        // 非图片文件异步执行
        else {
            CommonThreadPool.run(() -> FileSecurityScanner.scan(scanSessionId, mergedFile));
        }


        return success(Output.of(scanSessionId, mergedFileName));
    }

    public static class Output {
        public String filename;
        public String scanSessionId;

        public static Output of(String scanSessionId, String filename) {
            Output output = new Output();
            output.filename = filename;
            output.scanSessionId = scanSessionId;
            return output;
        }
    }

    public static class Input extends AbstractApiInput {
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
