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
 * @author zane.luo
 * @date 2023/11/13
 */
public class JobFunctions {
    public GetPartnerProgressFunction getPartnerProgressFunction;
    public SavePsiBloomFilterFunction savePsiBloomFilterFunction;
    public DownloadPsiBloomFilterFunction downloadPsiBloomFilterFunction;
    public EncryptPsiRecordsFunction encryptPsiRecordsFunction;
    public SaveFusionResultFunction saveFusionResultFunction;

    public void check() {
        List<Tuple2> list = Arrays.asList(
                Tuple2.of(getPartnerProgressFunction, GetPartnerProgressFunction.class),
                Tuple2.of(savePsiBloomFilterFunction, SavePsiBloomFilterFunction.class),
                Tuple2.of(downloadPsiBloomFilterFunction, DownloadPsiBloomFilterFunction.class),
                Tuple2.of(encryptPsiRecordsFunction, EncryptPsiRecordsFunction.class),
                Tuple2.of(saveFusionResultFunction, SaveFusionResultFunction.class)
        );

        for (Tuple2 tuple2 : list) {
            if (tuple2.getValue1() == null) {
                throw new RuntimeException("JobFunction 中未设置 " + tuple2.getValue2());
            }
        }


    }
}
