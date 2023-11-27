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
package com.welab.wefe.common.web;

import cn.hutool.crypto.digest.SM3;
import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.crypto.Sm2;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.SignedApiInput;
import com.welab.wefe.common.web.function.ApiPermissionPolicyFunction;
import com.welab.wefe.common.web.util.CurrentAccount;
import org.apache.commons.lang3.StringUtils;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

/**
 * 默认的接口权限检查策略
 *
 * @author zane.luo
 * @date 2023/11/27
 */
public class DefaultApiPermissionPolicy implements ApiPermissionPolicyFunction {
    private Supplier<String> privateKeySupplier;

    public DefaultApiPermissionPolicy(Supplier<String> privateKeySupplier) {
        this.privateKeySupplier = privateKeySupplier;
    }

    @Override
    public void check(HttpServletRequest request, Api annotation, JSONObject params) throws Exception {
        if (annotation.allowAccessWithNothing()) {
            return;
        }

        // 检查签名
        if (annotation.allowAccessWithSign()) {
            if (params.containsKey("sign") && params.containsKey("data")) {
                if (checkSign(params)) {
                    return;
                }
            }
        }

        // 检查 token
        if (CurrentAccount.get() != null) {
            StatusCode.LOGIN_REQUIRED.throwException("请登录后访问");
        }

    }

    private boolean checkSign(JSONObject params) throws Exception {
        SignedApiInput signedApiInput = params.toJavaObject(SignedApiInput.class);

        if (StringUtils.isEmpty(signedApiInput.getSign())) {
            StatusCode.CHECK_SIGN_ERROR.throwException("验签失败：签名为空");
        }

        String[] arr = signedApiInput.getSign().split("_");
        String hash = arr[0];
        String ciphertext = arr[1];

        // 检查 hash
        String dataHash = SM3.create().digestHex(signedApiInput.getData().getBytes(StandardCharsets.UTF_8));
        if (!hash.equals(dataHash)) {
            StatusCode.CHECK_SIGN_ERROR.throwException("验签失败！");
        }

        // 检查加密
        String decrypt = Sm2.decryptByPrivateKey(ciphertext, privateKeySupplier.get());
        if (!hash.equals(decrypt)) {
            StatusCode.CHECK_SIGN_ERROR.throwException("验签失败!");
        }

        // 签名成功，将 params 中的 data 作为 api 的实际参数。
        params.clear();
        JSONObject data = JSONObject.parseObject(signedApiInput.getData());
        params.putAll(data);

        return true;
    }
}
