/*
 * Copyright 2021 Tianmian Tech. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
<<<<<<< HEAD
 * http://www.apache.org/licenses/LICENSE-2.0
=======
 *     http://www.apache.org/licenses/LICENSE-2.0
>>>>>>> refs/heads/release-v2.4.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.welab.wefe.common.web;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.SamplingLogger;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.TimeSpan;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.api.base.AbstractApi;
import com.welab.wefe.common.web.api.base.Api;
import com.welab.wefe.common.web.dto.ApiResult;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.BeansException;
import org.springframework.util.MultiValueMap;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The entry class that calls the API
 *
 * @author Zane
 */
public class ApiExecutor {
    protected static final Logger LOG = LoggerFactory.getLogger(ApiExecutor.class);
    protected static final SamplingLogger SLOG;

    private static final String REQUEST_FROM_REFRESH = "request-from-refresh";

    static {
        SLOG = new SamplingLogger(LOG, 1000, TimeSpan.MINUTE);
    }

    /**
     * Implement the API
     */
    public static ApiResult<?> execute(HttpServletRequest httpServletRequest, long start, String apiName, JSONObject params, MultiValueMap<String, MultipartFile> files) {

        MDC.put("requestId", start + "");

        AbstractApi<?, ?> api = null;
        String apiPath = apiName.toLowerCase();
        while (StringUtils.isNotBlank(apiPath) && api == null) {
            try {
                api = Launcher.CONTEXT.getBean(apiPath, AbstractApi.class);
            } catch (BeansException ex) {
                int end = apiPath.lastIndexOf("/");
                if (end < 0) {
                    break;
                }
                apiPath = apiPath.substring(0, end);
            }
        }
        if (api == null) {
            return ApiResult.ofErrorWithStatusCode(StatusCode.REQUEST_API_NOT_FOUND, "接口不存在：" + apiName.toLowerCase());
        }

        Api annotation = api.getClass().getAnnotation(Api.class);
        switch (annotation.logLevel()) {
            case "debug":
                LOG.debug("request({}):{}", apiName.toLowerCase(), params.toString());
                break;
            default:
                LOG.info("request({}):{}", apiName.toLowerCase(), StringUtils.substring(params.toString(), 0, 200000));
        }
        ApiResult<?> result = null;
        try {

            // 检查访问权限
            checkApiPermission(httpServletRequest, annotation, params);

            // Doing things before the API is executed
            if (Launcher.BEFORE_API_EXECUTE_FUNCTION != null) {
                Launcher.BEFORE_API_EXECUTE_FUNCTION.action(api, params);
            }

            // 执行 API
            result = api.execute(httpServletRequest.getMethod(), params, httpServletRequest, files);

        } catch (StatusCodeWithException e) {
            boolean skipLog = e.getStatusCode() == StatusCode.LOGIN_REQUIRED;
            if (!skipLog) {
                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            }
            result = api.fail(e.getStatusCode().getCode(), e.getMessage());
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            result = api.fail(StatusCode.SYSTEM_ERROR.getCode(), e.getMessage());
        } finally {
            if (result == null) {
                result = api.fail(StatusCode.SYSTEM_ERROR.getCode(), "响应失败，疑似程序中发生了死循环。");
            }
            result.spend = System.currentTimeMillis() - start;

            // Doing things after the API is executed
            if (Launcher.AFTER_API_EXECUTE_FUNCTION != null) {
                Launcher.AFTER_API_EXECUTE_FUNCTION.action(httpServletRequest, start, api, params, result);
            }

            // 调用自定义的 api 日志记录器
            if (Launcher.API_LOGGER != null) {
                Launcher.API_LOGGER.action(httpServletRequest, start, api, params, result);
            }

            logResponse(annotation, result);

            MDC.clear();
        }

        result.spend = System.currentTimeMillis() - start;

        return result;
    }

    private static final Map<String, Long> API_LOG_TIME_MAP = new ConcurrentHashMap<>();

    public static void logResponse(Api annotation, ApiResult<?> result) {

        // 是否要省略此次日志打印，以减少磁盘使用。
        boolean omitLog = false;
        if (annotation.logSaplingInterval() > 0) {
            if (!API_LOG_TIME_MAP.containsKey(annotation.path())) {
                API_LOG_TIME_MAP.put(annotation.path(), 0L);
            }

            long interval = TimeSpan
                    .fromMs(System.currentTimeMillis() - API_LOG_TIME_MAP.get(annotation.path()))
                    .toMs();

            if (interval < annotation.logSaplingInterval()) {
                omitLog = true;
            } else {
                API_LOG_TIME_MAP.put(annotation.path(), System.currentTimeMillis());
            }
        }

        String content = result.toLogString(omitLog);

        if ("debug".equals(annotation.logLevel())) {
            LOG.debug("response({}):{}", annotation.path(), content);
        } else {
            LOG.info("response({}):{}", annotation.path(), content);
        }
    }

    /**
     * Check API access permissions
     */
    private static void checkApiPermission(HttpServletRequest httpServletRequest, Api annotation, JSONObject params) throws Exception {
        // If the permission check method is not set, the permission check is not performed.
        if (Launcher.API_PERMISSION_POLICY == null) {
            return;
        }

        Launcher.API_PERMISSION_POLICY.check(httpServletRequest, annotation, params);
    }


}
