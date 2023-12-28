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

import com.welab.fusion.core.Job.base.JobPhase;
import com.welab.fusion.core.Job.algorithm.base.PsiAlgorithm;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/12/18
 */
@Api(path = "algorithm/list_phase", name = "获取指定算法的阶段流程")
public class ListPhaseApi extends AbstractApi<ListPhaseApi.Input, ListPhaseApi.Output> {
    @Override
    protected ApiResult<ListPhaseApi.Output> handle(ListPhaseApi.Input input) throws Exception {
        List<JobPhase> list = input.algorithm.createJobFlow().listPhase();
        return success(Output.of(list));
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "算法", require = true)
        public PsiAlgorithm algorithm;
    }

    public static class Output {
        public List<Item> list;

        public static Output of(List<JobPhase> list) {
            Output output = new Output();
            output.list = list.stream().map(Item::of).collect(Collectors.toList());
            return output;
        }
    }

    public static class Item {
        public JobPhase phase;
        public String name;

        public static Item of(JobPhase phase) {
            Item item = new Item();
            item.phase = phase;
            item.name = phase.getLabel();
            return item;
        }
    }
}
