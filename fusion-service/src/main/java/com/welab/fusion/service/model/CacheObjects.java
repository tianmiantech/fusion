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

import com.welab.fusion.service.model.global_config.FusionConfigModel;
import com.welab.fusion.service.service.GlobalConfigService;
import com.welab.wefe.common.web.Launcher;

/**
 * @author zane.luo
 * @date 2023/11/27
 */
public class CacheObjects {

    private static String privateKey;

    /**
     * 获取私钥
     */
    public synchronized static String getPrivateKey() {
        if (privateKey == null) {
            FusionConfigModel config = Launcher.getBean(GlobalConfigService.class).getFusionConfig();
            if (!config.isInitialized) {
                throw new RuntimeException("Fusion service is not initialized");
            }
            privateKey = config.privateKey;
        }
        return privateKey;
    }
}
