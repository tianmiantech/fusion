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
package com.welab.fusion.core.bloom_filter;

import com.welab.wefe.common.crypto.Rsa;

import java.math.BigInteger;

/**
 * @author zane.luo
 * @date 2023/11/8
 */
public class RsaPsiParam {
    /**
     * RSA 公钥参数
     */
    public BigInteger publicExponent;
    public BigInteger publicModulus;

    /**
     * RSA 私钥参数
     */
    public BigInteger privateExponent;
    public  BigInteger privatePrimeP;
    public BigInteger privatePrimeQ;


    /**
     * 使用私钥计算出来的值
     * 名字都是瞎起的，不要在意。
     */
    private BigInteger cp;
    private BigInteger cq;
    private BigInteger ep;
    private BigInteger eq;

    /**
     * 预处理，提前计算出后续需要使用的值。
     */
    private void preproccess(){
        this.cp = privatePrimeQ.modInverse(privatePrimeP).multiply(privatePrimeQ);
        this.cq = privatePrimeP.modInverse(privatePrimeQ).multiply(privatePrimeP);
        this.ep = privateExponent.remainder(privatePrimeP.subtract(BigInteger.valueOf(1)));
        this.eq = privateExponent.remainder(privatePrimeQ.subtract(BigInteger.valueOf(1)));
    }

    public static RsaPsiParam of(Rsa.RsaKeyPair keyPair) {
        RsaPsiParam param = new RsaPsiParam();
        param.publicExponent = keyPair.getPublicKey().getPublicExponent();
        param.publicModulus = keyPair.getPublicKey().getModulus();

        param.privateExponent = keyPair.getPrivateKey().getPrivateExponent();
        param.privatePrimeP = keyPair.getPrivateKey().getPrimeP();
        param.privatePrimeQ = keyPair.getPrivateKey().getPrimeQ();

        param.preproccess();

        return param;
    }

    // region getter/setter

    public BigInteger getCp() {
        return cp;
    }

    public BigInteger getCq() {
        return cq;
    }

    public BigInteger getEp() {
        return ep;
    }

    public BigInteger getEq() {
        return eq;
    }


    // endregion
}
