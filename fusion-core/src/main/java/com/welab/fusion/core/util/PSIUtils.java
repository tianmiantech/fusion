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
import com.welab.fusion.core.Job.FusionJobRole;
import com.welab.fusion.core.bloom_filter.PsiBloomFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class PSIUtils {
    protected static final Logger LOG = LoggerFactory.getLogger(PSIUtils.class);


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

        List<String> result = new ArrayList<>(list.size());
        for (int i = 0; i < list.size(); i++) {
            byte[] bytes = Base64.decode(list.get(i));
            BigInteger x = PSIUtils.bytesToBigInteger(bytes, 0, bytes.length);

            // crt优化后
            BigInteger rp = x.modPow(d.remainder(p.subtract(BigInteger.valueOf(1))), p);
            BigInteger rq = x.modPow(d.remainder(q.subtract(BigInteger.valueOf(1))), q);
            BigInteger y = (rp.multiply(cp).add(rq.multiply(cq))).remainder(n);

            byte[] encrypted = PSIUtils.bigIntegerToBytes(y);
            String base64 = Base64.encode(encrypted);
            result.add(base64);
        }

        return result;
    }

    public static BigInteger stringToBigInteger(String s) {
        byte[] input = s.getBytes(StandardCharsets.UTF_8);
        return new BigInteger(1, input);
    }

    public static BigInteger bytesToBigInteger(byte[] bytes, int inOff, int inLen) {

        if (inOff == 0 || inLen == bytes.length) {
            return new BigInteger(1, bytes);
        } else {
            byte[] block = new byte[inLen];
            System.arraycopy(bytes, inOff, block, 0, inLen);
            return new BigInteger(1, block);
        }
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

}
