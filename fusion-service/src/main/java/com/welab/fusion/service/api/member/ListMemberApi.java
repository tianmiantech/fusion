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
package com.welab.fusion.service.api.member;

import com.welab.fusion.service.dto.entity.MemberOutputModel;
import com.welab.fusion.service.service.MemberService;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Api(path = "member/list", name = "获取合作伙伴列表")
public class ListMemberApi extends AbstractApi<ListMemberApi.Input, ListMemberApi.Output> {
    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<ListMemberApi.Output> handle(ListMemberApi.Input input) throws Exception {
        List<MemberOutputModel> list = memberService.list(input.name);
        return success(Output.of(list));
    }

    public static class Input extends AbstractApiInput {
        public String name;
    }

    public static class Output {
        public List<MemberOutputModel> list;

        public static Output of(List<MemberOutputModel> list) {
            Output output = new Output();
            output.list = list;
            return output;
        }
    }
}
