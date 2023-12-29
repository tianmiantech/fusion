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
package com.welab.fusion.service.database.base;

import com.welab.wefe.common.util.StringUtil;

import javax.persistence.AttributeConverter;
import java.util.LinkedHashSet;

/**
 * @author zane.luo
 * @date 2023/12/29
 */
public class StringSetConverter implements AttributeConverter<LinkedHashSet<String>, String> {
    @Override
    public String convertToDatabaseColumn(LinkedHashSet<String> strings) {
        return StringUtil.joinByComma(strings);
    }

    @Override
    public LinkedHashSet<String> convertToEntityAttribute(String str) {
        return new LinkedHashSet<>(StringUtil.splitWithoutEmptyItem(str, ","));
    }
}
