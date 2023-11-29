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

import cn.hutool.crypto.digest.SM3;
import com.alibaba.fastjson.JSONObject;
import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.crypto.Sm2;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.FusionNodeInfo;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
@Service
public class GatewayService extends AbstractService {

    /**
     * 请求合作方接口
     *
     * @param targetUrl        目标地址
     * @param partnerPublicKey 合作方公钥
     */
    public HttpResponse requestOtherFusionNode(String targetUrl, String partnerPublicKey) throws StatusCodeWithException {
        FusionConfigModel config = globalConfigService.getFusionConfig();
        if (StringUtil.isEmpty(config.publicServiceBaseUrl)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("尚未设置我方“对外服务地址”，请在全局设置中设置。");
        }

        FusionNodeInfo myself = FusionNodeInfo.of(config.publicKey, config.publicServiceBaseUrl);

        // 在请求参数中带上自己的身份信息
        JSONObject params = new JSONObject();
        params.put("partnerCaller", myself);

        // 使用对方的公钥加密替代签名
        String data = params.toJSONString();
        String sign = SM3.create().digestHex(data);
        sign += "_" + Sm2.encryptByPublicKey(sign, partnerPublicKey);

        // 重新组装签名后的参数
        JSONObject signedParams = new JSONObject();
        signedParams.put("data", data);
        signedParams.put("sign", sign);

        HttpResponse response = HttpRequest
                .create(targetUrl)
                .setBody(signedParams.toJSONString())
                .post();

        if (!response.success()) {
            StatusCode
                    .REMOTE_SERVICE_ERROR
                    .throwException(
                            "访问合作方失败：" + response.getMessage()
                                    + System.lineSeparator()
                                    + targetUrl
                    );
        }
        return response;
    }

    public void callOtherFusionNode(FusionNodeInfo target, Class<? extends AbstractApi> apiClass, AbstractApiInput input) {
        if (input.fromOtherFusionNode()) {
            return;
        }
    }
}
