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
package com.welab.fusion.service.api.service;

import com.welab.fusion.service.api.account.AddAccountApi;
import com.welab.fusion.service.api.account.LoginApi;
import com.welab.fusion.service.database.entity.AccountDbModel;
import com.welab.fusion.service.dto.entity.AccountOutputModel;
import com.welab.fusion.service.service.InitService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/7
 */
@Api(path = "service/init", name = "注册超级管理员账号，初始化服务。", allowAccessWithNothing = true)
public class InitServiceApi extends AbstractApi<InitServiceApi.Input, LoginApi.Output> {
    @Autowired
    private InitService initService;

    @Override
    protected ApiResult<LoginApi.Output> handle(InitServiceApi.Input input) throws Exception {
        AccountDbModel account = initService.init(input.username, input.password);
        LoginApi.Output output = LoginApi.Output.of(account.mapTo(AccountOutputModel.class));
        return success(output);
    }

    public static class Input extends AddAccountApi.Input {

    }


}
