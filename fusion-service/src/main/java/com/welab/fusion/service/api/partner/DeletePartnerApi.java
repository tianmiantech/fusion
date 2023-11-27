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
package com.welab.fusion.service.api.partner;

import com.welab.fusion.service.service.PartnerService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Api(path = "partner/delete", name = "删除合作伙伴")
public class DeletePartnerApi extends AbstractApi<DeletePartnerApi.Input, DeletePartnerApi.Output> {
    @Autowired
    private PartnerService partnerService;

    @Override
    protected ApiResult<Output> handle(DeletePartnerApi.Input input) throws Exception {
        boolean deleted = partnerService.delete(input.name);
        return success(Output.of(deleted));
    }

    public static class Output {
        @Check(name = "是否已删除", require = true, desc = "如果指定的名称不存在，返回 false")
        public boolean deleted;

        public static Output of(boolean deleted) {
            Output output = new Output();
            output.deleted = deleted;
            return output;
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "合作方名称", require = true)
        public String name;
    }
}
