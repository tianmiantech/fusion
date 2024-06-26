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

import com.welab.fusion.service.dto.entity.MemberInputModel;
import com.welab.fusion.service.service.MemberService;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Api(path = "member/save", name = "添加或更新合作伙伴")
public class SaveMemberApi extends AbstractApi<MemberInputModel, SaveMemberApi.Output> {
    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<SaveMemberApi.Output> handle(MemberInputModel input) throws Exception {
        String id = memberService.save(input).getId();
        return success(Output.of(id));
    }

    public static class Output {
        public String id;

        public static Output of(String id) {
            Output output = new Output();
            output.id = id;
            return output;
        }
    }
}
