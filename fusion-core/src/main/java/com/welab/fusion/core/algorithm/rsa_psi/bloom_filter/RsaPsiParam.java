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
package com.welab.fusion.core.algorithm.rsa_psi.bloom_filter;

import com.welab.wefe.common.crypto.Rsa;

import java.math.BigInteger;
import java.util.Objects;

/**
 * 包含 RSA 公钥和私钥参数
 *
 * @author zane.luo
 * @date 2023/11/8
 */
public class RsaPsiParam {
    /**
     * RSA 公钥参数
     */
    // E
    public BigInteger publicExponent;
    // N
    public BigInteger publicModulus;
    /**
     * RSA 私钥参数
     */
    // D
    public BigInteger privateExponent;
    // P
    public BigInteger privatePrimeP;
    // Q
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
     * 抹除私钥相关信息
     */
    public void cleanPrivateKey() {
        privateExponent = null;
        privatePrimeP = null;
        privatePrimeQ = null;

        cp = null;
        cq = null;
        ep = null;
        eq = null;
    }

    /**
     * 预处理，提前计算出后续需要使用的值。
     */
    public void preproccess() {
        if (privatePrimeQ == null || privatePrimeP == null || privateExponent == null) {
            return;
        }

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

    @Override
    public int hashCode() {
        return Objects.hash(
                publicExponent,
                publicModulus,
                privateExponent,
                privatePrimeP,
                privatePrimeQ
        );
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
