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
package com.welab.fusion.service.model;

import com.welab.fusion.core.Job.AbstractPsiJob;
import com.welab.fusion.service.database.entity.JobDbModel;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * 在内存中管理 FusionJob 对象
 *
 * @author zane.luo
 * @date 2023/11/29
 */
public class FusionJobManager {
    private static ExpiringMap<String, AbstractPsiJob> JOBS = ExpiringMap
            .builder()
            .expiration(30, TimeUnit.MINUTES)
            .build();

    public static void start(AbstractPsiJob job) throws Exception {
        JOBS.put(job.getJobId(), job);
        job.start();
    }

    public static AbstractPsiJob get(String jobId) {
        return JOBS.get(jobId);
    }

    public static void remove(String jobId) {
        JOBS.remove(jobId);
    }
}
