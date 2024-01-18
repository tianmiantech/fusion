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

import com.welab.fusion.service.dto.base.PagingOutput;
import com.welab.fusion.service.dto.entity.JobOutputModel;
import com.welab.fusion.service.service.JobService;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 * @date 2024/1/16
 */
@Api(path = "job/list_auditing", name = "查询审核中的任务列表")
public class ListAuditingJobApi extends AbstractApi<ListAuditingJobApi.Input, ListAuditingJobApi.Output> {
    private static List<JobOutputModel> cache;
    private static long lastUpdateTime;
    @Autowired
    private JobService jobService;

    @Override
    protected ApiResult<ListAuditingJobApi.Output> handle(ListAuditingJobApi.Input input) throws Exception {
        // 间歇性强制刷新，避免手动更新缓存漏调用。
        if (System.currentTimeMillis() - lastUpdateTime > 1000 * 60 * 5) {
            cache = null;
            lastUpdateTime = System.currentTimeMillis();
        }
        if (cache == null) {
            QueryJobApi.Input where = QueryJobApi.Input.ofAuditing();
            PagingOutput<JobOutputModel> page = jobService.query(where);
            cache = page.getList();
        }

        return success(Output.of(cache));
    }

    public static void cleanCache() {
        cache = null;
    }

    public static class Input extends AbstractApiInput {
    }

    public static class Output {
        public List<JobOutputModel> list;

        public static Output of(List<JobOutputModel> list) {
            Output output = new Output();
            output.list = list;
            return output;
        }
    }
}
