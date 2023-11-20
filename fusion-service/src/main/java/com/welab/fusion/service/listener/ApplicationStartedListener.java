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

package com.welab.fusion.service.listener;

import com.welab.fusion.core.io.FileSystem;
import com.welab.fusion.service.service.GlobalConfigService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.event.ApplicationStartedEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Monitor the message queue and process chat messages
 *
 * @author Johnny.lin
 */
@Component
public class ApplicationStartedListener implements ApplicationListener<ApplicationStartedEvent> {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationStartedListener.class);

    @Value("${fusion.file-system.base-dir:}")
    private String fileSystemBaseDir;
    @Autowired
    private GlobalConfigService globalConfigService;

    @Override
    public void onApplicationEvent(ApplicationStartedEvent event) {

        // 初始化全局配置
        try {
            globalConfigService.init();
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            System.exit(-1);
        }

        // 初始化文件系统
        try {
            FileSystem.init(fileSystemBaseDir);
        } catch (IOException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            System.exit(-1);
        }
    }
}