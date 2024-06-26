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
package com.welab.fusion.core.util;

import cn.hutool.core.codec.Base64;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiRecord;
import com.welab.fusion.core.algorithm.rsa_psi.bloom_filter.PsiBloomFilter;
import org.bouncycastle.math.ec.ECPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class PsiUtils {
    protected static final Logger LOG = LoggerFactory.getLogger(PsiUtils.class);

    /**
     * 碰撞，返回碰撞的数据。
     *
     * @param psiBloomFilter 过滤器对象
     * @param rawRecords     包含原文的记录
     * @param encryptedKeys  加密后的 key
     * @param publicModulus  rsa 公钥模数
     * @return 交集
     */
    public static List<RsaPsiRecord> match(PsiBloomFilter psiBloomFilter, List<RsaPsiRecord> rawRecords, List<String> encryptedKeys, BigInteger publicModulus) {

        // 将明文与密文使用 map 包装为一一对应的 entry，便于下面使用并行流计算。
        Map<String, RsaPsiRecord> map = new HashMap<>(encryptedKeys.size());
        for (int i = 0; i < encryptedKeys.size(); i++) {
            map.put(
                    encryptedKeys.get(i),
                    rawRecords.get(i)
            );
        }

        return map.entrySet()
                // 并行处理
                .parallelStream()
                .filter(entry -> {
                    String encryptedKey = entry.getKey();
                    RsaPsiRecord rawRecord = entry.getValue();

                    BigInteger y = PsiUtils.bytesToBigInteger(Base64.decode(encryptedKey));
                    BigInteger z = y.multiply(rawRecord.inv).mod(publicModulus);
                    return psiBloomFilter.contains(z);
                })
                .map(x -> x.getValue())
                .collect(Collectors.toList());
    }

    /**
     * 过滤器方对数据集方发送过来的数据进行加密
     */
    public static List<String> encryptPsiRecords(PsiBloomFilter psiBloomFilter, List<String> list) {
        BigInteger d = psiBloomFilter.rsaPsiParam.privateExponent;
        BigInteger p = psiBloomFilter.rsaPsiParam.privatePrimeP;
        BigInteger q = psiBloomFilter.rsaPsiParam.privatePrimeQ;
        BigInteger n = psiBloomFilter.rsaPsiParam.publicModulus;
        BigInteger cp = psiBloomFilter.rsaPsiParam.getCp();
        BigInteger cq = psiBloomFilter.rsaPsiParam.getCq();

        return list
                // 并行处理，但是要保证返回结果顺序一致。
                // 暂时取消并行，并行的时候 CPU 打满会导致数据库连接超时。
                .parallelStream()
                .map(item -> {
                    byte[] bytes = Base64.decode(item);
                    BigInteger x = PsiUtils.bytesToBigInteger(bytes);

                    // crt优化后
                    BigInteger rp = x.modPow(d.remainder(p.subtract(BigInteger.valueOf(1))), p);
                    BigInteger rq = x.modPow(d.remainder(q.subtract(BigInteger.valueOf(1))), q);
                    BigInteger y = (rp.multiply(cp).add(rq.multiply(cq))).remainder(n);

                    byte[] encrypted = PsiUtils.bigIntegerToBytes(y);
                    return Base64.encode(encrypted);
                })
                .collect(Collectors.toList());

    }

    /**
     * 生成盲化因子
     *
     * @param publicModulus rsa 公钥模数
     */
    public static BigInteger generateBlindingFactor(BigInteger publicModulus) {
        BigInteger zero = BigInteger.valueOf(0);
        BigInteger one = BigInteger.valueOf(1);

        int length = publicModulus.bitLength() - 1;
        BigInteger gcd;
        BigInteger blindFactor = new BigInteger(length, new SecureRandom());
        do {
            gcd = blindFactor.gcd(publicModulus);
        }
        while (blindFactor.equals(zero) || blindFactor.equals(one) || !gcd.equals(one));

        return blindFactor;
    }

    public static BigInteger stringToBigInteger(String s) {
        byte[] input = s.getBytes(StandardCharsets.UTF_8);
        return new BigInteger(1, input);
    }

    public static BigInteger bytesToBigInteger(byte[] bytes) {
        return new BigInteger(1, bytes);
    }

    public static byte[] bigIntegerToBytes(BigInteger input) {
        byte[] output = input.toByteArray();

        // have ended up with an extra zero byte, copy down.
        if (output[0] == 0) {
            byte[] tmp = new byte[output.length - 1];
            System.arraycopy(output, 1, tmp, 0, tmp.length);
            return tmp;
        }

        return output;
    }

    public static String ecPoint2String(ECPoint point) {
        return Base64.encode(point.getEncoded(true));
    }

}
