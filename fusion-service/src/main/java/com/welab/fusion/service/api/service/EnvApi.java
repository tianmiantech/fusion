/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.fusion.service.api.service;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.HostUtil;
import com.welab.wefe.common.web.api.base.AbstractNoneInputApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;

import java.util.TreeMap;

/**
 * @author Zane
 */
@Api(path = "service/env", name = "服务环境信息")
public class EnvApi extends AbstractNoneInputApi<TreeMap<String, Object>> {

    private static final TreeMap<String, Object> ENV = new TreeMap<>();
    /**
     * 使用缓存，一个小时更新一次。
     */
    private static final long CACHE_TIMEOUT = 1000 * 60 * 60;
    private static long lastUpdateTime = 0;

    @Override
    protected ApiResult<TreeMap<String, Object>> handle() throws StatusCodeWithException {
        if (System.currentTimeMillis() - lastUpdateTime > CACHE_TIMEOUT) {
            updateCache();
        }

        return success(ENV);
    }

    private static void updateCache() {
        ENV.put("local_ip", HostUtil.getLocalIp());
        ENV.put("internet_ip", HostUtil.getInternetIp());
        lastUpdateTime = System.currentTimeMillis();
    }


}
