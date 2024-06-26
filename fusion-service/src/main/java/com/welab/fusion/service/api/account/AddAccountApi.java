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

import com.welab.fusion.service.database.entity.AccountDbModel;
import com.welab.fusion.service.dto.entity.AccountOutputModel;
import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.AccountService;
import com.welab.fusion.service.service.GlobalConfigService;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zane.luo
 * @date 2023/11/7
 */
@Api(path = "account/add", name = "添加账号")
public class AddAccountApi extends AbstractApi<AddAccountApi.Input, AddAccountApi.Output> {

    @Autowired
    private AccountService accountService;

    @Override
    protected ApiResult<AddAccountApi.Output> handle(AddAccountApi.Input input) throws Exception {
        accountService.add(input.username, input.password);
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
