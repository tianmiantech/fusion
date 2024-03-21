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

import com.welab.fusion.service.database.entity.BloomFilterDbModel;
import com.welab.fusion.service.dto.entity.BloomFilterOutputModel;
import com.welab.fusion.service.service.BloomFilterService;
import com.welab.wefe.common.ModelMapper;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/17
 */
@Api(path = "bloom_filter/detail", name = "获取布隆过滤器详情")
public class DetailsBloomFilterApi extends AbstractApi<DetailsBloomFilterApi.Input, BloomFilterOutputModel> {
    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<BloomFilterOutputModel> handle(DetailsBloomFilterApi.Input input) throws Exception {
        BloomFilterDbModel model = bloomFilterService.findOneById(input.id);
        return success(ModelMapper.map(model, BloomFilterOutputModel.class));
    }

    public static class Input extends AbstractApiInput {
        public String id;
    }

}
