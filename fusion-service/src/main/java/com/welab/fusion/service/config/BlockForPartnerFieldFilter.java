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
package com.welab.fusion.service.config;

import com.alibaba.fastjson.serializer.PropertyFilter;

import java.lang.reflect.Field;

/**
 * 阻止字段输出到合作方
 *
 * @author zane.luo
 * @date 2023/12/21
 */
public class BlockForPartnerFieldFilter implements PropertyFilter {
    public static final BlockForPartnerFieldFilter instance = new BlockForPartnerFieldFilter();

    @Override
    public boolean apply(Object object, String name, Object value) {
        try {
            Field field = object.getClass().getField(name);
            BlockForPartnerField annotation = field.getAnnotation(BlockForPartnerField.class);
            return annotation == null;
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }

    }
}
