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
import com.welab.wefe.common.util.ClassUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
public class SuperDataSourceClient {
    private static final Logger LOG = LoggerFactory.getLogger(SuperDataSourceClient.class);

    private static final Map<String, Class<? extends AbstractDataSource>> CLASS_MAPS = new HashMap<>();

    /**
     * 获取已注册的类型名称列表
     */
    public static Set<String> registeredTypes() {
        return CLASS_MAPS.keySet();
    }

    public static void register(Class<? extends AbstractDataSource>... classes) {
        for (Class<? extends AbstractDataSource> clazz : classes) {
            register(clazz);
        }
    }

    /**
     * 在使用某数据源之前，需要调用此方法进行注册。
     *
     * 通常在程序启动后调用一次即可
     */
    public static void register(Class<? extends AbstractDataSource> clazz) {
        DataSource annotation = clazz.getAnnotation(DataSource.class);
        if (annotation == null) {
            throw new RuntimeException("类型 " + clazz.getSimpleName() + " 未找到 @DataSource 注解");
        }

        for (String typeName : annotation.typeNames()) {
            String type = typeName.toLowerCase();
            if (CLASS_MAPS.containsKey(type)) {
                throw new RuntimeException("数据源类型 " + type + " 已注册，无法重新注册。");
            }

            CLASS_MAPS.put(type, clazz);
        }
    }

    /**
     * 创建一个数据源实例
     */
    public static <T extends AbstractDataSource> T create(Class<? extends AbstractDataSource> clazz, DataSourceParams params) {
        try {
            params.checkAndStandardize();
        } catch (StatusCodeWithException e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
            throw new RuntimeException(e);
        }
        try {
            Constructor<? extends AbstractDataSource> constructor = clazz.getConstructor(params.getClass());
            return (T) constructor.newInstance(params);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 根据数据源类型获取对应的数据源 Class
     *
     * @param dataSourceType 数据源名称（mysql、hive...）
     *                       这里对名称做了大小写兼容，不论传递大写还是小写均可。
     */
    private static Class<? extends AbstractDataSource> getDataSourceClass(String dataSourceType) {
        String type = dataSourceType.toLowerCase();

        Class<? extends AbstractDataSource> clazz = CLASS_MAPS.get(type);
        if (clazz == null) {
            throw new RuntimeException("未找到 " + dataSourceType + " 类型的数据源，请确认该类型是否已注册。");
        }

        return clazz;
    }

    // region 重载方法：create()

    public static <T extends AbstractDataSource> T create(String dataSourceType, Map<String, Object> params) {
        return create(dataSourceType, new JSONObject(params));
    }

    public static <T extends AbstractDataSource> T create(String dataSourceType, DataSourceParams params) {
        Class<? extends AbstractDataSource> clazz = getDataSourceClass(dataSourceType);
        return create(clazz, params);
    }

    public static <T extends AbstractDataSource> T create(String dataSourceType, JSONObject jsonParams) {
        Class<? extends AbstractDataSource> clazz = getDataSourceClass(dataSourceType);

        Class<?> paramsClass = ClassUtils.getGenericClass(clazz, 0);
        DataSourceParams params = (DataSourceParams) jsonParams.toJavaObject(paramsClass);

        return create(clazz, params);
    }

    public static <T extends DataSourceParams> Class<T> getParamsClass(String dataSourceType) {
        Class<? extends AbstractDataSource> clazz = getDataSourceClass(dataSourceType);

        return (Class<T>) ClassUtils.getGenericClass(clazz, 0);
    }


    // endregion
}
