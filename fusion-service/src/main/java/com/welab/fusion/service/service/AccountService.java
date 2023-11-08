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
import com.welab.fusion.service.database.repository.AccountRepository;
import com.welab.fusion.service.dto.entity.AccountOutputModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.ModelMapper;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.crypto.Md5;
import com.welab.wefe.common.crypto.Rsa;
import com.welab.wefe.common.crypto.Sha256;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.util.CurrentAccount;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/7
 */
@Service
public class AccountService extends AbstractService {
    @Autowired
    private AccountRepository accountRepository;

    public AccountDbModel findByUsername(String username) {
        return accountRepository.findByUsername(username);
    }

    public List<AccountOutputModel> list() {
        List<AccountDbModel> list = accountRepository.findAll();
        return ModelMapper.maps(list, AccountOutputModel.class);
    }

    public synchronized AccountDbModel initSuperAdminAccount(String username, String password) throws StatusCodeWithException {

        if (!list().isEmpty()) {
            StatusCode.PERMISSION_DENIED.throwException("系统已初始化，无法重复初始化");
        }

        return add(username, password);
    }

    private synchronized AccountDbModel add(String username, String password) throws StatusCodeWithException {

        AccountDbModel byUsername = accountRepository.findByUsername(username);

        if (byUsername != null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("用户名“" + username + "”已存在，不可使用重复的用户名。");
        }

        // 随机生成 rsa 私钥作为 salt
        String salt = Md5.of(Rsa.generateKeyPair().getPrivate().getEncoded());
        // 入库前，先将密码混入 salt ，再进行 hash，确保数据库中的密码不可反解。
        String passwordWithSalt = Sha256.of(password + salt);

        AccountDbModel model = new AccountDbModel();
        model.setUsername(username);
        model.setPassword(passwordWithSalt);
        model.setSalt(salt);

        return accountRepository.save(model);
    }

    /**
     * 登录
     */
    public AccountOutputModel login(String username, String password) throws StatusCodeWithException {
        AccountDbModel model = accountRepository.findByUsername(username);
        if (model == null) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("用户名或密码错误");
        }

        String passwordWithSalt = Sha256.of(password + model.getSalt());
        if (!passwordWithSalt.equals(model.getPassword())) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("用户名或密码错误");
        }

        CurrentAccount.logined(model.getId(), model.getUsername());

        return model.mapTo(AccountOutputModel.class);
    }
}
