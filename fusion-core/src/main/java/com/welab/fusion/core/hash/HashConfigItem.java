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
package com.welab.fusion.core.hash;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zane.luo
 * @date 2023/11/8
 */
public class HashConfigItem {
    public List<String> columns;
    public HashMethod method;

    public String hash(Map<String, Object> data) {
        String values = buildHashString(data);
        if (values == null) {
            return null;
        }

        return method.hash(values);
    }

    @Override
    public int hashCode() {
        return Objects.hash(columns, method);
    }

    private String buildHashString(Map<String, Object> data) {
        // 提升性能，少创建一个 StringBuilder。
        if (columns.size() == 1) {
            Object value = data.get(columns.get(0));
            if (value == null) {
                return null;
            }
            return value.toString();
        }

        StringBuilder builder = new StringBuilder(32);
        for (String column : columns) {
            Object value = data.get(column);
            if (value == null) {
                return null;
            }
            builder.append(value);
        }

        return builder.toString();
    }

    public static HashConfigItem of(HashMethod options, String... columns) {
        HashConfigItem config = new HashConfigItem();
        config.columns = Arrays.asList(columns);
        config.method = options;
        return config;
    }
}
