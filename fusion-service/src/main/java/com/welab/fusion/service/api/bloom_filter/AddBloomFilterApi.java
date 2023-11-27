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

import com.welab.fusion.core.hash.HashConfig;
import com.welab.fusion.service.constans.AddMethod;
import com.welab.fusion.core.progress.Progress;
import com.welab.fusion.service.service.BloomFilterService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Api(path = "bloom_filter/add", name = "添加布隆过滤器", desc = "添加过程为异步执行，需要通过 progress/get 接口查询进度")
public class AddBloomFilterApi extends AbstractApi<AddBloomFilterApi.Input, AddBloomFilterApi.Output> {
    @Autowired
    private BloomFilterService bloomFilterService;

    @Override
    protected ApiResult<AddBloomFilterApi.Output> handle(AddBloomFilterApi.Input input) throws Exception {
        Progress progress = bloomFilterService.add(input);

        return success(Output.of(progress.getSessionId()));
    }

    public static class Input extends PreviewTableDataSourceApi.Input {
        @Check(name = "名称", regex = "^.{4,50}$", messageOnInvalid = "名称长度不能少于4，不能大于50")
        public String name;
        @Check(name = "描述", regex = "^[\\s\\S]{0,3072}$", messageOnInvalid = "你写的描述太多了~")
        public String description;

        @Check(name = "主键 hash 方案", require = true)
        public HashConfig hashConfig;

        @Override
        public void checkAndStandardize() throws StatusCodeWithException {
            super.checkAndStandardize();

            if (hashConfig.isEmpty()) {
                throw new StatusCodeWithException(StatusCode.PARAMETER_VALUE_INVALID, "请设置主键");
            }

            if (addMethod == AddMethod.Database) {
                if (MapUtils.isEmpty(dataSourceParams)) {
                    StatusCode.PARAMETER_VALUE_INVALID.throwException("请设置数据库连接信息");
                }

                if (StringUtils.isEmpty(sql)) {
                    throw new StatusCodeWithException(StatusCode.PARAMETER_CAN_NOT_BE_EMPTY, "请输入 sql 查询语句");
                }

            } else {
                if (StringUtils.isEmpty(dataSourceFile)) {
                    throw new StatusCodeWithException(StatusCode.PARAMETER_CAN_NOT_BE_EMPTY, "请指定数据源文件");
                }
            }
        }
    }

    public static class Output {
        @Check(desc = "用于使用 progress/get 接口查询进度")
        public String sessionId;

        public static Output of(String sessionId) {
            Output output = new Output();
            output.sessionId = sessionId;
            return output;
        }
    }
}
