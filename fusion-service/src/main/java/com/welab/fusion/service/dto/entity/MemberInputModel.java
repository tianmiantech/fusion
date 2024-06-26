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
package com.welab.fusion.service.dto.entity;

import com.welab.fusion.service.service.MemberService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.dto.AbstractApiInput;
import com.welab.wefe.common.web.dto.FusionNodeInfo;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
public class MemberInputModel extends AbstractApiInput {
    @Check(name = "名称")
    private String member_name;
    @Check(name = "公钥", require = true)
    private String publicKey;
    @Check(name = "接口地址", require = true)
    private String baseUrl;

    public static MemberInputModel of(String publicKey, String baseUrl) {
        MemberInputModel input = new MemberInputModel();
        input.publicKey = publicKey;
        input.baseUrl = baseUrl;
        return input;
    }

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        if (MemberService.MYSELF_NAME.equals(member_name)) {
            StatusCode
                    .PARAMETER_VALUE_INVALID
                    .throwException("名称不能为：" + MemberService.MYSELF_NAME);
        }
    }

    public FusionNodeInfo toFusionNodeInfo() {
        return FusionNodeInfo.of(publicKey, baseUrl);
    }

    // region getter/setter

    public String getMember_name() {
        return member_name;
    }

    public void setMember_name(String member_name) {
        this.member_name = member_name;
    }

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }


    // endregion
}
