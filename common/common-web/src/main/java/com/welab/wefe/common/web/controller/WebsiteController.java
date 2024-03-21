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
import com.welab.wefe.common.util.FileUtil;
import com.welab.wefe.common.util.StringUtil;
import com.welab.wefe.common.util.UrlUtil;
import com.welab.wefe.common.util.enums.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;
import java.net.URI;

/**
 * 响应前端资源
 *
 * @author Zane
 */
@RestController
public class WebsiteController {

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteController.class);

    @Value("${server.servlet.context-path:}")
    private String contextPath;

    @RequestMapping("/website/**")
    public ResponseEntity<?> response(HttpServletRequest request) {
        String resourceName = extractResourceName(request);

        // 如果指定的是目录，则默认返回 index.html
        if (resourceName.endsWith("/")) {
            resourceName += "index.html";
        } else if (StringUtil.isEmpty(FileUtil.getFileSuffix(resourceName))) {
            resourceName += "/index.html";
        }

        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(resourceName);

        // 404
        if (inputStream == null) {

            // 404 的时候跳转到首页，并拼接当前的请求路径到 redirect 参数供前端使用。
            String redirectUrl = request.getRequestURI();
            if (StringUtil.isNotEmpty(request.getQueryString())) {
                redirectUrl += "?" + request.getQueryString();
            }
            String location = contextPath + "/website/index.html?redirect=" + UrlUtil.encode(redirectUrl);

            HttpHeaders headers = new HttpHeaders();
            headers.setLocation(URI.create(location));
            return new ResponseEntity<>(headers, HttpStatus.SEE_OTHER);
        }

        return ResponseEntity
                .ok()
                .contentType(MediaType.parseMediaType(ContentType.of(resourceName)))
                .body(new InputStreamResource(
                        inputStream
                ));

    }

    /**
     * 从请求路径中提取资源名称
     */
    private static String extractResourceName(HttpServletRequest request) {
        // e.g: /website/index.html
        String path = StrUtil.subAfter(request.getServletPath(), "/website", false);
        path = path.replace("//", "/");
        String fileName = StrUtil.subBefore(path, "?", false);
        if (fileName.startsWith("/")) {
            fileName = fileName.substring(1);
        }
        if (StrUtil.isEmpty(fileName)) {
            fileName = "index.html";
        }
        fileName = "website/" + fileName;
        return fileName;
    }

}
