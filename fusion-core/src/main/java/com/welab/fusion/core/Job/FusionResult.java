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
package com.welab.fusion.core.Job;

import java.io.File;
import java.util.Date;

/**
 * @author zane.luo
 * @date 2023/11/15
 */
public class FusionResult {
    public String jobId;
    /**
     * 结果文件：包含己方 Key 相关字段和对方的附加字段
     */
    public File resultFile;
    public long fusionCount;
    public Date startTime;
    public Date endTime;
    public long costTime;

    public static FusionResult of(String jobId) {
        FusionResult fusionResult = new FusionResult();
        fusionResult.jobId = jobId;
        fusionResult.startTime = new Date();
        return fusionResult;
    }

    public void finish() {
        this.endTime = new Date();
        this.costTime = endTime.getTime() - startTime.getTime();
    }
}
