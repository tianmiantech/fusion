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
package com.welab.fusion.core.Job.base;

/**
 * @author zane.luo
 * @date 2023/11/10
 */
public enum JobPhase {

    InitJob("初始化任务"),

    // region RSA-PSI

    CreatePsiBloomFilter("生成过滤器"),

    DownloadPsiBloomFilter("下载过滤器"),

    // endregion


    // region ECDH-PSI

    ECEncryptMyselfData("加密己方数据"),

    DownloadPartnerECEncryptedData("下载合作方加密后的数据"),

    ECEncryptPartnerData("加密合作方数据"),

    DownloadSecondaryECEncryptedData("下载二次加密后的数据"),

    // endregion

    Intersection("求交"),
    DownloadIntersection("下载交集"),
    AppendMyselfAdditionalResultColumns("追加我方附加列"),
    AppendPartnerAdditionalResultColumns("追加合作方附加列"),
    SaveResult("下载并保存结果");

    private final String label;

    JobPhase(String label) {
        this.label = label;
    }

    public boolean isLastPhase() {
        return this == SaveResult;
    }

    public String getLabel() {
        return label;
    }
}
