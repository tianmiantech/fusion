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
import com.welab.wefe.common.util.enums.ContentType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.InputStream;

/**
 * 响应前端资源
 *
 * @author Zane
 */
@RestController
public class WebsiteController {

    private static final Logger LOG = LoggerFactory.getLogger(WebsiteController.class);

    @RequestMapping("/website/**")
    public ResponseEntity<?> response(HttpServletRequest request) {
        String resourceName = extractResourceName(request);

        InputStream inputStream = Thread.currentThread()
                .getContextClassLoader()
                .getResourceAsStream(resourceName);

        // 404
        if (inputStream == null) {
            return ResponseEntity.notFound().build();
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
