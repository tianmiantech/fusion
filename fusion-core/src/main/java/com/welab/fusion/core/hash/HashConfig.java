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

import com.alibaba.fastjson.JSONObject;
import com.welab.wefe.common.fieldvalidate.AbstractCheckModel;
import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.util.JObject;
import com.welab.wefe.common.util.StringUtil;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author zane.luo
 * @date 2023/11/14
 */
public class HashConfig extends AbstractCheckModel {
    @Check(name = "主键 hash 方案", require = true)
    public List<HashConfigItem> list = new ArrayList<>();

    /**
     * 输出求交结果到 csv 时的表头
     */
    private LinkedHashSet<String> csvHeader = new LinkedHashSet<>();

    public static HashConfig of(HashConfigItem... items) {
        HashConfig config = new HashConfig();
        config.list = Arrays.asList(items);
        return config;
    }

    public String hash(LinkedHashMap<String, Object> row) {
        return list.stream()
                .map(x -> x.hash(row))
                .collect(Collectors.joining());
    }

    public JSONObject toJson() {
        return JObject.create(this);
    }

    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public int hashCode() {
        return Objects.hash(list);
    }


    /**
     * 拼接用于输出到 csv 的主键相关字段列表
     */
    public String getIdHeadersForCsv() {
        if (csvHeader.isEmpty()) {
            for (HashConfigItem item : list) {
                csvHeader.addAll(item.columns);
            }
        }

        return StringUtil.joinByComma(csvHeader);
    }

    /**
     * 拼接用于输出到 csv 的主键相关字段列表
     */
    public String getIdValuesForCsv(LinkedHashMap<String, Object> row) {
        List<String> values = csvHeader.stream()
                .map(x -> row.get(x).toString())
                .collect(Collectors.toList());

        return StringUtil.joinByComma(values);
    }
}
