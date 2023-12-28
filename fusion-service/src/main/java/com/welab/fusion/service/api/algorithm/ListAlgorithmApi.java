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
package com.welab.fusion.service.api.algorithm;

import com.welab.fusion.core.Job.algorithm.base.PsiAlgorithm;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

/**
 * @author zane.luo
 * @date 2023/12/18
 */
@Api(path = "algorithm/list", name = "获取所有算法")
public class ListAlgorithmApi extends AbstractApi<ListAlgorithmApi.Input, ListAlgorithmApi.Output> {
    @Override
    protected ApiResult<ListAlgorithmApi.Output> handle(ListAlgorithmApi.Input input) throws Exception {
        PsiAlgorithm[] values = PsiAlgorithm.values();
        return success(Output.of(values));
    }

    public static class Input extends AbstractApiInput {
    }

    public static class Output {
        public PsiAlgorithm[] list;

        public static Output of(PsiAlgorithm[] values) {
            Output output = new Output();
            output.list = values;
            return output;
        }
    }
}
