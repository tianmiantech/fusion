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
package com.welab.wefe.common.crypto;

import cn.hutool.core.codec.Base64;
import cn.hutool.crypto.SecureUtil;

import java.security.KeyPair;
import java.security.PrivateKey;

/**
 * @author zane.luo
 * @date 2023/11/7
 */
public class Rsa {

    /**
     * 生成密钥对
     */
    public static KeyPair generateKeyPair() {
        return SecureUtil.generateKeyPair("RSA", 2048);
    }

    /**
     * 生成文本格式的密钥对
     */
    public static StringKeyPair generateStringKeyPair() {
        return new StringKeyPair(generateKeyPair());
    }

    /**
     * 文本格式的密钥对
     */
    public static class StringKeyPair {
        private String publicKey;
        private String privateKey;

        public StringKeyPair(KeyPair keyPair) {
            this.publicKey = Base64.encode(keyPair.getPublic().getEncoded());
            this.privateKey = Base64.encode(keyPair.getPrivate().getEncoded());
        }

        public String getPublicKey() {
            return publicKey;
        }

        public String getPrivateKey() {
            return privateKey;
        }
    }

    public static void main(String[] args) {
        PrivateKey privateKey = Rsa.generateKeyPair().getPrivate();
        String encode = Base64.encode(privateKey.getEncoded());
        System.out.println(encode);
    }
}
