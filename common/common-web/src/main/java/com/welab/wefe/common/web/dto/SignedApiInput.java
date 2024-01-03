/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.web.dto;

import com.alibaba.fastjson.JSON;

/**
 * Data is a JSON string called by the API encrypted by the private key on the board side, which may also contain memberId,
 * After receiving the request, THE API searches for the public key through the memberId, and continues to call the API after decrypting the public key
 *
 * @author Jervis
 **/
public class SignedApiInput extends AbstractApiInput {
    public String sign;
    public String data;

    public String toJSONString() {
        return JSON.toJSONString(this);
    }
}
