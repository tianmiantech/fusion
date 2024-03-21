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
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPrivateCrtKey;
import org.bouncycastle.jcajce.provider.asymmetric.rsa.BCRSAPublicKey;

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
    public static RsaKeyPair generateKeyPair() {
        KeyPair keyPair = SecureUtil.generateKeyPair("RSA", 2048);
        return new RsaKeyPair(keyPair);
    }


    public static class RsaKeyPair {
        BCRSAPublicKey publicKey;
        BCRSAPrivateCrtKey privateKey;

        public RsaKeyPair(KeyPair keyPair) {
            this.publicKey = (BCRSAPublicKey) keyPair.getPublic();
            this.privateKey = (BCRSAPrivateCrtKey) keyPair.getPrivate();
        }

        public BCRSAPublicKey getPublicKey() {
            return publicKey;
        }

        public BCRSAPrivateCrtKey getPrivateKey() {
            return privateKey;
        }

        public String getPublicKeyAsString() {
            return Base64.encode(publicKey.getEncoded());
        }

        public String getPrivateKeyAsString() {
            return Base64.encode(privateKey.getEncoded());
        }
    }


    public static void main(String[] args) {
        RsaKeyPair keyPair = Rsa.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivateKey();
        String encode = Base64.encode(privateKey.getEncoded());
        System.out.println(encode);
    }
}
