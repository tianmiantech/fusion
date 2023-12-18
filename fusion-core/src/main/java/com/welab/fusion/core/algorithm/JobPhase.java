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
package com.welab.fusion.core.algorithm;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public enum JobPhase {
    /**
     * 确认成员角色
     */
    ConfirmMemberRole,

    // region RSA-PSI
    
    /**
     * 生成过滤器
     */
    CreatePsiBloomFilter,
    /**
     * 下载过滤器
     */
    DownloadPsiBloomFilter,
    
    // endregion


    // region ECDH-PSI
    /**
     * 加密己方数据
     */
    EncryptMyselfData,
    /**
     * 加密合作方数据
     */
    EncryptPartnerData,
    /**
     * 下载二次加密后的数据
     */
    DownloadSecondaryEncryptedData,
    
    // endregion
    
    
    
    /**
     * 求交
     */
    Intersection,
    /**
     * 下载结果
     */
    SaveResult;

    public boolean isLastPhase() {
        return this == SaveResult;
    }
}
