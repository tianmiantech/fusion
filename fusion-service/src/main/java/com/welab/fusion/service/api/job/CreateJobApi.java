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
package com.welab.fusion.service.api.job;

import com.welab.fusion.core.data_resource.base.DataResourceType;
import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.service.api.bloom_filter.PreviewTableDataSourceApi;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/20
 */
@Api(path = "job/create", name = "创建任务")
public class CreateJobApi extends AbstractApi<CreateJobApi.Input, CreateJobApi.Output> {
    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<CreateJobApi.Output> handle(CreateJobApi.Input input) throws Exception {
        String jobId = jobService.createJob(input);
        return success(Output.of(jobId));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "任务Id", donotShow = true)
        public String jobId;
        @Check(name = "数据量", donotShow = true)
        public long totalDataCount;

        @Check(name = "资源类型", desc = "数据源类型：数据集、过滤器")
        public DataResourceType dataResourceType;

        @Check(name = "输入的过滤器信息")
        public BloomFilterResourceInput bloomFilterResourceInput;
        @Check(name = "输入的数据集信息")
        public TableDataResourceInput tableDataResourceInput;

        @Check(name = "主键 hash 方案", require = true)
        public HashConfig hashConfig;
        @Check(name = "备注")
        public String remark;
    }

    public static class BloomFilterResourceInput extends AbstractApiInput {
        @Check(name = "过滤器Id")
        public String bloomFilterId;
    }

    public static class TableDataResourceInput extends PreviewTableDataSourceApi.Input {

    }

    public static class Output {
        @Check(name = "任务Id")
        public String jobId;

        public static Output of(String jobId) {
            Output output = new Output();
            output.jobId = jobId;
            return output;
        }
    }
}
