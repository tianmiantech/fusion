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
package com.welab.wefe.common.data.source;

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.fieldvalidate.secret.SecretUtil;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
public class DataSourceParams extends AbstractCheckModel {
    public String name;
    @Check(messageOnEmpty = "Host 不能为空", require = true)
    public String host;
    @Check(messageOnEmpty = "端口不能为空", require = true)
    public Integer port;

    @Override
    public void checkAndStandardize() throws StatusCodeWithException {
        super.checkAndStandardize();

        if (StringUtil.isEmpty(name)) {
            name = host + ":" + port;
        }
    }

    public JSONObject toJson() {
        return JObject.create(this);
    }

    /**
     * 根据实体类中的 @Secret 注解，将敏感信息替脱敏。
     * @return
     */
    public JSONObject toOutputJson() {
        return SecretUtil.toJson(this);
    }
    
    // region getter/setter

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    // endregion
}
