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
package com.welab.fusion.core.Job.algorithm.base.function;

import com.welab.fusion.core.Job.base.JobPhase;

import java.io.File;
import java.util.function.Consumer;

/**
 * @author zane.luo
 * @date 2023/12/19
 */
public interface DownloadPartnerFileFunction {
    /**
     * 从合作方下载文件
     *
     * @param jobPhase             任务阶段
     * @param jobId                任务Id
     * @param partnerId            合作方id
     * @param totalSizeConsumer    用于更新总大小的消费者
     * @param downloadSizeConsumer 用于更新已下载大小的消费者
     * @return 下载的文件
     */
    File download(
            JobPhase jobPhase,
            String jobId,
            String partnerId,
            Consumer<Long> totalSizeConsumer,
            Consumer<Long> downloadSizeConsumer
    ) throws Exception;
}
