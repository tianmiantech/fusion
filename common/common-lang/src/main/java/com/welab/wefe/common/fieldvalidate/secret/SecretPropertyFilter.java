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
package com.welab.wefe.common.fieldvalidate.secret;

import com.alibaba.fastjson.serializer.PropertyFilter;

import java.util.Map;

/**
 * 根据字段名判断是否需要序列化
 *
 * @author zane.luo
 * @date 2023/12/21
 */
public class SecretPropertyFilter implements PropertyFilter {
    public static final SecretPropertyFilter instance = new SecretPropertyFilter();

    private static final String PASSWORD = "password";

    @Override
    public boolean apply(Object object, String name, Object value) {
        if (value == null) {
            return true;
        }

        Secret secret = SecretUtil.getAnnotation(object.getClass(), name);

        if (secret == null) {
            return true;
        }

        if (secret.maskStrategy() == MaskStrategy.BLOCK) {
            return false;
        }

        if (secret.maskStrategy() == MaskStrategy.MAP_WITH_PASSWORD) {
            Map map = (Map) value;
            if (map.containsKey(PASSWORD)) {
                map.put(
                        PASSWORD,
                        "***************"
                );
            }
            return true;
        }

        return true;
    }
}
