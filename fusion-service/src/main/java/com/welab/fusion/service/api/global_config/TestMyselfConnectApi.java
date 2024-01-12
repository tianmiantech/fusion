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
package com.welab.fusion.service.api.global_config;

import com.welab.fusion.service.api.member.TestConnectApi;
import com.welab.fusion.service.service.MemberService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
@Api(path = "global_config/test_connect", name = "测试己方接口地址连通性")
public class TestMyselfConnectApi extends AbstractApi<TestMyselfConnectApi.Input, TestMyselfConnectApi.Output> {
    @Autowired
    private MemberService memberService;

    @Override
    protected ApiResult<TestMyselfConnectApi.Output> handle(TestMyselfConnectApi.Input input) throws Exception {
        if (input.fromBackend) {
            return success();
        }

        testMyself(input.baseUrl);
        return success();
    }

    public void testMyself(String baseUrl) throws Exception {
        String url = baseUrl;
        if (!url.endsWith("/")) {
            url += "/";

        }
        url += TestConnectApi.class.getAnnotation(Api.class).path();
        url += "?" + CurrentAccount.TOKEN_KEY_NAME + "=" + CurrentAccount.token() + "&from_backend=true";
        HttpResponse response = HttpRequest.create(url)
                .setTimeout(3_000)
                .get();

        if (!response.success()) {
            throw response.getError();
        }

        ApiResult apiResult = response.getBodyAsJson().toJavaObject(ApiResult.class);
        if (apiResult.code != StatusCode.SUCCESS.getCode()) {
            throw new Exception("响应失败:" + apiResult.message);
        }
    }

    public static class Input extends AbstractApiInput {
        @Check(name = "对外服务地址", require = true)
        public String baseUrl;
        @Check(name = "请求是否来自后台", require = true)
        public boolean fromBackend;

    }

    public static class Output {
    }
}
