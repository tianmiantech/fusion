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

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.api.download.base.DownloadConfig;
import com.welab.fusion.service.api.download.base.FileType;
import com.welab.fusion.service.service.JobMemberService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;

/**
 * @author zane.luo
 * @date 2023/11/30
 */
@Api(path = "download/get_file_info", name = "下载文件分片")
public class GetDownloadFileInfoApi extends AbstractApi<GetDownloadFileInfoApi.Input, GetDownloadFileInfoApi.Output> {
    @Autowired
    private JobMemberService jobMemberService;

    @Override
    protected ApiResult<GetDownloadFileInfoApi.Output> handle(GetDownloadFileInfoApi.Input input) throws Exception {
        File file = findFile(input);
        return success(Output.of(file));
    }

    private File findFile(Input input) throws IOException {
        switch (input.fileType) {
            case BloomFilter:
                String jobId = input.bizData.getString("job_id");
                String bloomFilterId = jobMemberService.findMyself(jobId).getBloomFilterId();
                Path dir = FileSystem.PsiBloomFilter.getPath(bloomFilterId);
                return PsiBloomFilter.of(dir).zip();

            default:
                return null;
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "文件类型", require = true)
        public FileType fileType;
        @Check(name = "业务数据", require = true, desc = "用于服务端定位被下载的文件，例如job_id、bloom_filter_id")
        public JSONObject bizData;

        public static Input of(FileType fileType, JObject bizData) {
            Input input = new Input();
            input.fileType = fileType;
            input.bizData = bizData;
            return input;
        }
    }

    public static class Output {
        @Check(name = "文件名", require = true)
        public String filename;
        @Check(name = "文件路径", require = true)
        public String filePath;
        @Check(name = "文件大小（byte）", require = true)
        public long fileLength;
        @Check(name = "分片大小", require = true)
        public long chunkSizeInMb = DownloadConfig.CHUNK_SIZE_IN_MB;
        @Check(name = "分片数量", require = true)
        public int chunkCount;

        public static Output of(File file) {
            Output output = new Output();
            output.filename = file.getName();
            output.filePath = FileSystem.getRelativePath(file);
            output.fileLength = file.length();
            output.chunkCount = (int) Math.ceil(output.fileLength / (double) (output.chunkSizeInMb * 1024 * 1024));
            return output;
        }

        public static void main(String[] args) throws IOException {
            FileSystem.init("D:\\data\\wefe");
            File file = new File("D:\\data\\wefe\\ivenn_10w_20210319_vert_promoter.csv");
            System.out.println(JSON.toJSONString(of(file), true));
        }
    }
}
