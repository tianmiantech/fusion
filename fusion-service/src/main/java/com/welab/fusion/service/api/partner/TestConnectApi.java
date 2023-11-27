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

import com.welab.fusion.service.dto.entity.PartnerInputModel;
import com.welab.fusion.service.service.PartnerService;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
@Api(path = "partner/test_connect", name = "测试连通性", desc = "测试A|B双方互相发起请求的连通性")
public class TestConnectApi extends AbstractApi<TestConnectApi.Input, TestConnectApi.Output> {
    @Autowired
    private PartnerService partnerService;
    @Override
    protected ApiResult<TestConnectApi.Output> handle(TestConnectApi.Input input) throws Exception {
        partnerService.testConnection(input);
        return null;
    }

    public static class Input extends PartnerInputModel {

    }

    public static class Output {
    }
}
