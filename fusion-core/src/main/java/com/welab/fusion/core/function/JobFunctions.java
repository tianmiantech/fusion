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
package com.welab.fusion.core.function;

import com.welab.wefe.common.tuple.Tuple2;

import java.util.Arrays;
import java.util.List;

/**
 * 任务执行过程中各种动作的抽象声明
 *
 * @author zane.luo
 * @date 2023/11/13
 */
public class JobFunctions {
    public GetPartnerProgressFunction getPartnerProgressFunction;

    // region rsa_psi

    public SaveMyPsiBloomFilterFunction saveMyPsiBloomFilterFunction;
    public DownloadPartnerPsiBloomFilterFunction downloadPartnerPsiBloomFilterFunction;
    public EncryptRsaPsiRecordsFunction encryptRsaPsiRecordsFunction;

    // endregion

    // region ecdh_psi



    // endregion

    public SaveFusionResultFunction saveFusionResultFunction;
    public FinishJobFunction finishJobFunction;

    public void check() {
        List<Tuple2> list = Arrays.asList(
                Tuple2.of(getPartnerProgressFunction, GetPartnerProgressFunction.class),
                Tuple2.of(saveMyPsiBloomFilterFunction, SaveMyPsiBloomFilterFunction.class),
                Tuple2.of(downloadPartnerPsiBloomFilterFunction, DownloadPartnerPsiBloomFilterFunction.class),
                Tuple2.of(encryptRsaPsiRecordsFunction, EncryptRsaPsiRecordsFunction.class),
                Tuple2.of(saveFusionResultFunction, SaveFusionResultFunction.class),
                Tuple2.of(finishJobFunction, FinishJobFunction.class)
        );

        for (Tuple2 tuple2 : list) {
            if (tuple2.getValue1() == null) {
                throw new RuntimeException("JobFunction 中未设置 " + tuple2.getValue2());
            }
        }
    }
}
