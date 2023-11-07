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
package com.welab.wefe.common.web.config;

import com.welab.wefe.common.util.StringUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Compression;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.boot.web.servlet.server.ConfigurableServletWebServerFactory;
import org.springframework.util.unit.DataSize;

/**
 * 自定义 WebServer 配置
 *
 * @author zane.luo
 * @date 2023/3/1
 */
public class MyWebServerFactoryCustomizer implements WebServerFactoryCustomizer<ConfigurableServletWebServerFactory> {
    @Value("${server.servlet.context-path:}")
    private String serverServletContextPath;

    @Override
    public void customize(ConfigurableServletWebServerFactory factory) {
        if (StringUtil.isNotEmpty(serverServletContextPath)) {
            factory.setDisplayName(StringUtil.trim(serverServletContextPath, '/', '\\'));
        }

        // 启用 response 压缩
        Compression compression = new Compression();
        compression.setEnabled(true);
        compression.setMinResponseSize(DataSize.ofKilobytes(10L));

        factory.setCompression(compression);
    }
}
