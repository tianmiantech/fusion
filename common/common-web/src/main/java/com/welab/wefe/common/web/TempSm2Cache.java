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

package com.welab.wefe.common.web;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.crypto.Sm2;
import com.welab.wefe.common.fieldvalidate.secret.Secret;
import com.welab.wefe.common.fieldvalidate.secret.SecretUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.web.util.CurrentAccount;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 临时 SM2 缓存
 * <p>
 * 使用场景：
 * 1. 给前端分配公钥后传输敏感字段到后端解密
 *
 * @author Zane
 */
public class TempSm2Cache {
    private static final Logger LOG = LoggerFactory.getLogger(TempSm2Cache.class);

    /**
     * user id : KeyPair
     */
    private static ExpiringMap<String, Sm2.Sm2KeyPair> KEY_PAIR_MAP_BY_USER = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(60, TimeUnit.MINUTES)
            .build();

    public static void init() {
    }

    /**
     * 获取一个新的公钥
     */
    public static String getPublicKey() {
        Sm2.Sm2KeyPair sm2KeyPair = Sm2.generateKeyPair();
        KEY_PAIR_MAP_BY_USER.put(CurrentAccount.get().getId(), sm2KeyPair);
        return sm2KeyPair.publicKey;
    }

    /**
     * 解密
     */
    public static String decrypt(String ciphertext) throws Exception {
        if (StringUtil.isEmpty(ciphertext)) {
            return ciphertext;
        }
        Sm2.Sm2KeyPair sm2KeyPair = KEY_PAIR_MAP_BY_USER.get(CurrentAccount.get().getId());
        if (sm2KeyPair == null) {
            return ciphertext;
        }
        try {
            return Sm2.decryptHexByPrivateKey(ciphertext, sm2KeyPair.privateKey);
        } catch (Exception e) {
            String message = System.lineSeparator()
                    + "ciphertext:" + ciphertext + System.lineSeparator()
                    + "publicKey:" + sm2KeyPair.publicKey + System.lineSeparator()
                    + "privateKey:" + sm2KeyPair.privateKey + System.lineSeparator();
            LOG.error(message);
            throw e;
        }
    }

    /**
     * 解密，并转为实体。
     */
    public static <T> T decrypt(Map<String, Object> map, Class<T> clazz) throws Exception {
        map = decryptMap(map, clazz);
        return new JSONObject(map).toJavaObject(clazz);
    }

    /**
     * 解密
     */
    public static Map<String, Object> decryptMap(Map<String, Object> map, Class<?> clazz) {
        if (StringUtil.isEmpty(CurrentAccount.token()) || KEY_PAIR_MAP_BY_USER.get(CurrentAccount.id()) == null) {
            return map;
        }

        // 对参数做解密
        for (Map.Entry<String, Object> entry : map.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }

            Secret secret = SecretUtil.getAnnotation(clazz, entry.getKey());
            if (secret == null) {
                continue;
            }

            try {
                String decrypt = decrypt(entry.getValue() + "");
                entry.setValue(decrypt);
            }
            // 可能会存在已经是明文的情况，跳过解密。
            catch (Exception e) {
                continue;
            }
        }
        return map;
    }

    public static void main(String[] args) throws Exception {
        /*String publicKey = "04267bc87fde3d541d64629fa2b641c6a5d0acb9a4c2f40c0f5cd9f28c36311dd65b508c0306d12b63caa47aea9b0f915cc50bd088b2a2de4424712fe3fe00ef0e";
        String privateKey = "63b5aeb4ecfb4a178cf892534a179d44242fcf42f20c8c6f985c50edcd0c44d9";
        System.out.println("publicKey:" + publicKey);
        System.out.println("privateKey:" + privateKey);

        String plaintext = "123";
        System.out.println("plaintext:" + plaintext);
        String ciphertext = SM2Util.encryptByPublicKey(plaintext,publicKey);
        System.out.println("ciphertext:" + ciphertext);

        String decrypt = SM2Util.decryptByPrivateKey(ciphertext, privateKey);
        System.out.println("decrypt:" + decrypt);
*/
        String publicKey = "04267bc87fde3d541d64629fa2b641c6a5d0acb9a4c2f40c0f5cd9f28c36311dd65b508c0306d12b63caa47aea9b0f915cc50bd088b2a2de4424712fe3fe00ef0e";
        String privateKey = "63b5aeb4ecfb4a178cf892534a179d44242fcf42f20c8c6f985c50edcd0c44d9";
        String ciphertext = "04891f22789ba1d69d05bda360a75ed1162f95693d0fced1c2bac5ee01f43c0ebdabba73c57b0762a000dd1ee5f8fd6df5332699899e1f52653775fbf74f6c64fe147be2671c93ec83a194de793efbd6734d179ed44b41ca3dd47a45def4b0d743832d6a";
        String decryptData = Sm2.decryptHexByPrivateKey(ciphertext, privateKey);
        System.out.println("decryptData==" + decryptData);
    }

}
