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
package com.welab.fusion.core.algorithm.base.function;

import com.welab.fusion.core.progress.JobProgress;

/**
 * 结束任务的动作
 *
 * @author zane.luo
 * @date 2024/1/16
 */
@FunctionalInterface
public interface UpdateJobProgressFunction {
    void update(String jobId, JobProgress progress) throws Exception;
}
