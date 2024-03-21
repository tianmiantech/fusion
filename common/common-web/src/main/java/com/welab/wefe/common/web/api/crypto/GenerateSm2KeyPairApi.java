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

package com.welab.wefe.common.web.api.crypto;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.web.TempSm2Cache;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;

@Api(path = "crypto/generate_sm2_key_pair", name = "创建一对新的 sm2 密钥，并得到其中的公钥。")
public class GenerateSm2KeyPairApi extends AbstractNoneInputApi<GenerateSm2KeyPairApi.Output> {
    @Override
    protected ApiResult<Output> handle() throws StatusCodeWithException {
        String publicKey = TempSm2Cache.getPublicKey();
        return success(new Output(publicKey));
    }

    public static class Output {
        @Check(name = "sm2 公钥", desc = "用于将数据加密后传输密文到后台")
        public String publicKey;

        public Output(String publicKey) {
            this.publicKey = publicKey;
        }
    }
}
