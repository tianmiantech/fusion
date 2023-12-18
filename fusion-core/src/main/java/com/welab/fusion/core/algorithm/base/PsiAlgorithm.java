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
package com.welab.fusion.core.algorithm.base;

import com.welab.fusion.core.algorithm.AbstractJobFlow;
import com.welab.fusion.core.algorithm.ecdh_psi.EcdhPsiJobFlow;
import com.welab.fusion.core.algorithm.rsa_psi.RsaPsiJobFlow;

/**
 * @author zane.luo
 * @date 2023/12/11
 */
public enum PsiAlgorithm {
    /**
     * 基于 RSA 加盲的 PSI
     */
    rsa_psi,
    ecdh_psi;

    public AbstractJobFlow createJobFlow() {
        switch (this) {
            case rsa_psi:
                return RsaPsiJobFlow.INSTANCE;
            case ecdh_psi:
                return EcdhPsiJobFlow.INSTANCE;
            default:
                throw new RuntimeException("Unknown algorithm: " + this);
        }
    }
}
