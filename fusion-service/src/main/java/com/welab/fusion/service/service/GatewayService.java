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
import com.welab.fusion.service.config.fastjson.BlockForPartnerFieldUtil;
import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.crypto.Sm2;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.http.HttpRequest;
import com.welab.wefe.common.http.HttpResponse;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.Launcher;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.*;
import org.springframework.stereotype.Service;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
@Service
public class GatewayService extends AbstractService {

    /**
     * 请求合作方接口
     */
    private HttpResponse requestOtherFusionNode(FusionNodeInfo target, Class<? extends AbstractApi> apiClass) throws StatusCodeWithException {
        return requestOtherFusionNode(target, apiClass, new JSONObject());
    }

    /**
     * 请求合作方接口
     *
     * @param target   目标节点信息
     * @param apiClass 目标接口
     */
    public HttpResponse requestOtherFusionNode(FusionNodeInfo target, Class<? extends AbstractApi> apiClass, JSONObject params) throws StatusCodeWithException {
        FusionConfigModel config = globalConfigService.getFusionConfig();
        if (StringUtil.isEmpty(config.publicServiceBaseUrl)) {
            StatusCode.PARAMETER_VALUE_INVALID.throwException("尚未设置我方“对外服务地址”，请在全局设置中设置。");
        }

        FusionNodeInfo myself = FusionNodeInfo.of(config.publicKey, config.publicServiceBaseUrl);

        // 在请求参数中带上自己的身份信息
        params.put("caller", myself);

        // 使用对方的公钥加密替代签名
        String data = params.toJSONString();
        String sign = SM3.create().digestHex(data);
        sign += "_" + Sm2.encryptByPublicKey(sign, target.publicKey);

        // 重新组装签名后的参数
        SignedApiInput signedApiInput = new SignedApiInput();
        signedApiInput.sign = sign;
        signedApiInput.data = data;

        String url = target.baseUrl + "/" + apiClass.getAnnotation(Api.class).path();
        HttpResponse response = HttpRequest
                .create(url)
                .setBody(signedApiInput.toJSONString())
                .setConnectTimeout(1_000 * 5)
                .setSocketTimeout(1_000 * 30)
                .postJson();

        if (!response.success()) {
            StatusCode
                    .REMOTE_SERVICE_ERROR
                    .throwException(
                            "合作方异常：" + response.getMessage()
                                    + System.lineSeparator()
                                    + url
                    );
        }
        return response;
    }

    /**
     * 调用其他节点接口
     */
    public <IN extends AbstractApiInput, OUT> void callOtherFusionNode(FusionNodeInfo target, Class<? extends AbstractApi<IN, OUT>> apiClass) throws StatusCodeWithException {
        callOtherFusionNode(target, apiClass, new NoneApiInput());
    }


    /**
     * 调用其他节点接口
     *
     * @param target   目标节点间
     * @param apiClass 接口
     * @param input    请求参数
     */
    public <IN extends AbstractApiInput, OUT> OUT callOtherFusionNode(FusionNodeInfo target, Class<? extends AbstractApi<IN, OUT>> apiClass, AbstractApiInput input) throws StatusCodeWithException {
        if (input.isRequestFromPartner()) {
            return null;
        }

        // 使用过滤器对需要保护的字段进行脱敏
        JSONObject params = BlockForPartnerFieldUtil.toJson(input);

        HttpResponse httpResponse = requestOtherFusionNode(target, apiClass, params);
        JSONObject json = httpResponse.getBodyAsJson();
        ApiResult apiResult = json.toJavaObject(ApiResult.class);
        if (apiResult.code != 0) {
            StatusCode
                    .REMOTE_SERVICE_ERROR
                    .throwException("访问合作方失败(" + apiResult.code + ")：" + apiResult.message);
        }

        JSONObject data = json.getJSONObject("data");

        if (data == null) {
            return null;
        }

        Class<OUT> resultClass = Launcher.getBean(apiClass).getOutputClass();
        if (resultClass == null) {
            return null;
        }

        if (resultClass == JSONObject.class) {
            return (OUT) data;
        }
        if (resultClass == JObject.class) {
            return (OUT) JObject.create(data);
        }

        return data.toJavaObject(resultClass);
    }
}
