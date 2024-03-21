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
package com.welab.fusion.service.service;

import com.welab.fusion.service.database.entity.AccountDbModel;
import com.welab.fusion.service.model.CacheObjects;
import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.crypto.Sm2;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 * @date 2023/11/20
 */
@Service
public class InitService extends AbstractService {
    @Autowired
    private GlobalConfigService globalConfigService;

    @Autowired
    private AccountService accountService;

    /**
     * 系统是否已初始化
     */
    public boolean isInitialized() {
        FusionConfigModel config = globalConfigService.getModel(FusionConfigModel.class);
        return config.isInitialized;
    }

    /**
     * 创建超级管理员账号，并初始化系统。
     *
     * @return
     */
    public synchronized AccountDbModel init(String username, String password) throws StatusCodeWithException {
        FusionConfigModel config = globalConfigService.getModel(FusionConfigModel.class);
        if (config.isInitialized) {
            StatusCode.PERMISSION_DENIED.throwException("系统已初始化，无法重复初始化");
        }

        AccountDbModel account = accountService.add(username, password);

        Sm2.Sm2KeyPair sm2KeyPair = Sm2.generateKeyPair();
        config.publicKey = sm2KeyPair.publicKey;
        config.privateKey = sm2KeyPair.privateKey;
        config.isInitialized = true;

        globalConfigService.save(config);

        CurrentAccount.logined(account.getId(), account.getUsername());
        CacheObjects.refresh();
        
        return account;
    }

}
