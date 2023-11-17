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
package com.welab.fusion.service.api.bloom_filter;

import com.welab.fusion.service.dto.entity.BloomFilterOutputModel;
import com.welab.fusion.service.service.BloomFilterService;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/17
 */
@Api(path = "bloom_filter/delete", name = "删除布隆过滤器")
public class DeleteBloomFilterApi extends AbstractApi<DeleteBloomFilterApi.Input, BloomFilterOutputModel> {
    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<BloomFilterOutputModel> handle(DeleteBloomFilterApi.Input input) throws Exception {
        bloomFilterService.delete(input.id);
        return success();
    }

    public static class Input extends AbstractApiInput {
        public String id;
    }

}
