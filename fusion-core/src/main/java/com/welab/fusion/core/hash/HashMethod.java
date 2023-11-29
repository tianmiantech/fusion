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
package com.welab.fusion.core.hash;

import com.welab.wefe.common.crypto.Md5;
import com.welab.wefe.common.crypto.Sha256;
import com.welab.wefe.common.util.StringUtil;

/**
 * @author zane.luo
 * @date 2023/11/8
 */
public enum HashMethod {
    /**
     * 使用 MD5 哈希
     */
    MD5,
    SHA256,

    /**
     * 不 hash
     */
    NONE;

    /**
     * 对字符串进行 hash
     */
    public String hash(String str) {
        if (StringUtil.isEmpty(str)) {
            return "";
        }

        switch (this) {
            case MD5:
                return Md5.of(str);
            case SHA256:

                return Sha256.of(str);
            case NONE:
                return str;
            default:
                throw new RuntimeException("Unsupported hash method: " + this);
        }
    }
}
