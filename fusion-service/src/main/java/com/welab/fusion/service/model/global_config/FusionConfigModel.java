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

package com.welab.fusion.service.model.global_config;

import com.welab.fusion.service.model.global_config.base.AbstractConfigModel;
import com.welab.fusion.service.model.global_config.base.ConfigGroupConstant;
import com.welab.fusion.service.model.global_config.base.ConfigModel;
import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;

/**
 * @author zane.luo
 */
@ConfigModel(group = ConfigGroupConstant.FUSION)
public class FusionConfigModel extends AbstractConfigModel {
    /**
     * 服务是否已初始化
     */
    public boolean isInitialized;
    /**
     * 公钥
     */
    public String publicKey;
    /**
     * 私钥
     */
    @Secret(maskStrategy = MaskStrategy.BLOCK)
    public String privateKey;
}
