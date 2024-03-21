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

package com.welab.wefe.common.web.controller;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.util.enums.ContentType;
import com.welab.wefe.common.web.ApiExecutor;
import com.welab.wefe.common.web.dto.ApiResult;
import com.welab.wefe.common.web.util.CurrentAccount;
import org.apache.catalina.connector.RequestFacade;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileCopyUtils;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.support.StandardMultipartHttpServletRequest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Zane
 */
@RestController
public class BaseController {

    private static final Logger LOG = LoggerFactory.getLogger(BaseController.class);

    @GetMapping(value = "**", produces = "application/json; charset=UTF-8")
    public ResponseEntity<?> get(HttpServletRequest httpServletRequest) throws IOException {
        // If static resources are requested, return the file.
        if (httpServletRequest.getServletPath().startsWith("/static/")) {

            ClassPathResource resource = new ClassPathResource(httpServletRequest.getServletPath());
            byte[] data = FileCopyUtils.copyToByteArray(resource.getInputStream());

            return ResponseEntity
                    .ok()
                    .contentLength(data.length)
                    .contentType(MediaType.IMAGE_PNG)
                    .body(data);
        }


        return post(httpServletRequest);
    }

    @PostMapping(value = "**", produces = "application/json; charset=UTF-8")
    public ResponseEntity<ApiResult<?>> post(HttpServletRequest httpServletRequest) throws IOException {
        long start = System.currentTimeMillis();

        try {
            /**
             * 从 header 中取出 token，并储存到当前线程中，使得接口内可以使用 CurrentAccount 获取当前用户信息。
             */
            CurrentAccount.token(httpServletRequest);

            JSONObject params;
            MultiValueMap<String, MultipartFile> files = null;

            // Ordinary request
            if (httpServletRequest instanceof RequestFacade) {
                RequestFacade request = (RequestFacade) httpServletRequest;
                JSONObject bodyParams = getBodyParamsFromHttpRequest(request);
                params = buildRequestParams(request.getParameterMap(), bodyParams);
            }

            // A request to include a file
            else if (httpServletRequest instanceof StandardMultipartHttpServletRequest) {
                StandardMultipartHttpServletRequest request = (StandardMultipartHttpServletRequest) httpServletRequest;
                params = buildRequestParams(request.getParameterMap(), null);
                files = request.getMultiFileMap();
            }

            // Other requests are not supported
            else {
                throw new UnsupportedOperationException("Unsupported request types：" + httpServletRequest.getClass().getSimpleName());
            }

            String path = httpServletRequest.getPathInfo() == null ? httpServletRequest.getServletPath() : httpServletRequest.getPathInfo();
            // Multi-level paths under/Tools are supported. Eg: the tools/a/b/c
            path = StringUtil.trim(path, '/');

            ApiResult<?> response = ApiExecutor.execute(httpServletRequest, start, path, params, files);
            response.spend = System.currentTimeMillis() - start;

            if (response.data instanceof ResponseEntity) {
                return (ResponseEntity) response.data;
            } else {
                return ResponseEntity.status(response.httpCode).body(response);
            }
        } catch (Exception e) {
            ApiResult<Object> result = ApiResult.ofErrorWithStatusCode(
                    StatusCode.SYSTEM_ERROR,
                    e.getClass().getSimpleName() + " " + e.getMessage()
            );
            return ResponseEntity.status(result.httpCode).body(result);
        } finally {
            CurrentAccount.leave();
        }

    }


    /**
     * Merge get arguments with POST
     * In case of merge conflicts, the POST parameter prevails.
     */
    private JSONObject buildRequestParams(Map<String, String[]> queryStringParams, JSONObject bodyParams) {
        TreeMap<String, Object> getParams = new TreeMap<>();

        if (queryStringParams != null && !queryStringParams.isEmpty()) {
            queryStringParams.forEach((key, values) -> {
                for (int i = 0; i < values.length; i++) {
                    values[i] = UrlUtil.decode(values[i]);
                }

                if (values.length == 1) {
                    getParams.put(key, "null".equalsIgnoreCase(values[0]) ? null : values[0]);
                } else {
                    getParams.put(key, values);
                }
            });
        }

        JSONObject result = new JSONObject(getParams);

        // Fill the body parameter
        if (bodyParams != null) {
            result.putAll(bodyParams);
        }

        return result;
    }

    /**
     * Gets the list of parameters from the request object
     */
    private JSONObject getBodyParamsFromHttpRequest(RequestFacade request) throws IOException {
        BufferedReader reader = request.getReader();
        StringBuilder body = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            body.append(line);
        }
        return JSON.parseObject(body.toString(), Feature.OrderedField);
    }


}
