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

import com.welab.fusion.core.progress.Progress;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;

import java.util.concurrent.TimeUnit;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
public class ProgressManager {
    /**
     * sessionId : Progress
     */
    private static ExpiringMap<String, Progress> CACHE = ExpiringMap
            .builder()
            .expirationPolicy(ExpirationPolicy.ACCESSED)
            .expiration(10, TimeUnit.MINUTES)
            .build();

    public static Progress startNew(String modelId) {
        Progress progress = Progress.of(modelId, 0);
        CACHE.put(progress.getSessionId(), progress);
        return progress;
    }

    public static Progress get(String sessionId) {
        return CACHE.get(sessionId);
    }

    public static void remove(Progress progress) {
        throw new UnsupportedOperationException("考虑到任务完成之后前端也需要展示状态，所以禁止主动删除，由过期策略自动删除。");
    }
}
