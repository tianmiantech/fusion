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
package com.welab.fusion.service.api.account;

import com.welab.fusion.service.service.AccountService;
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
@Api(path = "account/init_super_admin", name = "初始化超级管理员账号")
public class InitSuperAdminAccountApi extends AbstractApi<InitSuperAdminAccountApi.Input, InitSuperAdminAccountApi.Output> {
    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<InitSuperAdminAccountApi.Output> handle(InitSuperAdminAccountApi.Input input) throws Exception {
        accountService.initSuperAdminAccount(input.username, input.password);
        return success();
    }

    public static class Input extends AbstractApiInput {
        @Check(require = true)
        public String username;
        @Check(require = true)
        public String password;
    }

    public static class Output {

    }
}
