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

package com.welab.fusion.service.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.PropertyNamingStrategy;
import com.alibaba.fastjson.serializer.SerializeConfig;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.welab.fusion.service.database.base.Where;
import com.welab.fusion.service.database.entity.GlobalConfigDbModel;
import com.welab.fusion.service.database.repository.GlobalConfigRepository;
import com.welab.fusion.service.model.global_config.base.AbstractConfigModel;
import com.welab.fusion.service.model.global_config.base.ConfigModel;
import com.welab.fusion.service.service.base.AbstractService;
import com.welab.wefe.common.StatusCode;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.web.TempSm2Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author zane
 */
@Service
public class GlobalConfigService extends AbstractService {

    @Autowired
    protected GlobalConfigRepository globalConfigRepository;

    /**
     * 初始化配置项
     */
    public synchronized void init() throws StatusCodeWithException, InstantiationException, IllegalAccessException {
        LOG.info("start init global config");
        long start = System.currentTimeMillis();

        // 获取当前数据库全量的配置项
        List<GlobalConfigDbModel> all = globalConfigRepository.findAll();

        // 遍历所有 ConfigModel，将数据库中没有的配置项添加到数据库。
        AbstractConfigModel.getModelClasses()
                // 并发加速处理
                .parallelStream()
                .forEach(aClass -> {
                    String group = aClass.getAnnotation(ConfigModel.class).group();
                    JSONObject json = buildModelJson(aClass);

                    for (String name : json.keySet()) {
                        boolean contains = all.stream()
                                .anyMatch(x ->
                                        x.getGroup().equals(group)
                                                && x.getName().equals(name)
                                );

                        if (!contains) {
                            // 在数据库中初始化一个配置项
                            try {
                                put(group, name, json.getString(name));
                            } catch (StatusCodeWithException e) {
                                LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
                            }
                        }
                    }

                });

        long spend = System.currentTimeMillis() - start;
        LOG.info("(spend " + spend + "ms)init global config success!");
    }

    /**
     * 根据 class 创建包含默认配置项的 JSONObject
     */
    private JSONObject buildModelJson(Class<? extends AbstractConfigModel> aClass) {
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        String jsonString = null;
        try {
            jsonString = JSON.toJSONString(aClass.newInstance(), config, SerializerFeature.WriteMapNullValue);
        } catch (Exception e) {
            LOG.error(e.getClass().getSimpleName() + " " + e.getMessage(), e);
        }
        JSONObject json = JSON.parseObject(jsonString);
        return json;
    }

    /**
     * Add or update an object (multiple records)
     */
    public void save(AbstractConfigModel model) throws StatusCodeWithException {
        /**
         * 1. The names stored in the database are unified as underscores
         * 2. Since fastjson discards fields with a value of null by default,
         *    it should be set to be preserved during serialization here.
         */
        SerializeConfig config = new SerializeConfig();
        config.propertyNamingStrategy = PropertyNamingStrategy.SnakeCase;
        String json_string = JSON.toJSONString(model, config, SerializerFeature.WriteMapNullValue);

        ConfigModel annotation = model.getClass().getAnnotation(ConfigModel.class);

        JSONObject json = JSON.parseObject(json_string);
        for (String name : json.keySet()) {
            String value = json.getString(name);
            // value 为 null 时说明前端未指定，需要跳过。
            if (value == null) {
                continue;
            }
            save(annotation.group(), name, value, null);
        }
    }

    /**
     * Add or update a record
     */
    private void save(String group, String name, String value, String comment) throws StatusCodeWithException {
        GlobalConfigDbModel one = findOne(group, name);
        put(one, group, name, value, comment);
    }

    private void put(String group, String name, String value) throws StatusCodeWithException {
        put(null, group, name, value, null);
    }

    private void put(GlobalConfigDbModel one, String group, String name, String value, String comment) throws StatusCodeWithException {
        if (one == null) {
            one = new GlobalConfigDbModel();
            one.setGroup(group);
            one.setName(name);
        } else {
            if (one.getValue() != null && value == null) {
                StatusCode.SQL_ERROR.throwException("不能使用 null 覆盖非空值");
            }

            // If there is no need to update, jump out
            if (Objects.equals(one.getValue(), value)) {
                if (comment != null && Objects.equals(one.getComment(), comment)) {
                    return;
                }
            }
        }

        one.setValue(value);

        if (comment != null) {
            one.setComment(comment);
        }

        globalConfigRepository.save(one);
    }

    public GlobalConfigDbModel findOne(String group, String name) {
        Specification<GlobalConfigDbModel> where = Where
                .create()
                .equal("group", group)
                .equal("name", name)
                .build(GlobalConfigDbModel.class);

        return globalConfigRepository.findOne(where).orElse(null);
    }

    /**
     * Query list according to group
     */
    public List<GlobalConfigDbModel> list(String group) {
        return globalConfigRepository.findByGroup(group);
    }

    public <T extends AbstractConfigModel> T getModel(String group) {
        Class<T> clazz = (Class<T>) AbstractConfigModel.getModelClass(group);
        if (clazz == null) {
            throw new RuntimeException("未找到对应的 ConfigModel：" + group);
        }
        return getModel(clazz);
    }

    /**
     * Get the entity corresponding to the specified group
     */
    public <T extends AbstractConfigModel> T getModel(Class<T> clazz) {
        ConfigModel annotation = clazz.getAnnotation(ConfigModel.class);
        List<GlobalConfigDbModel> list = list(annotation.group());
        return toModel(list, clazz);
    }

    /**
     * Turn the list of configuration items into entities
     */
    private <T> T toModel(List<GlobalConfigDbModel> list, Class<T> clazz) {
        if (list == null || list.isEmpty()) {
            return null;
        }

        JSONObject json = new JSONObject();
        for (GlobalConfigDbModel item : list) {
            json.put(item.getName(), item.getValue());
        }
        return json.toJavaObject(clazz);
    }

    /**
     * 将 map 还原为 AbstractConfigModel
     * <p>
     * 这一步会对 @Secret 字段进行解密
     */
    public AbstractConfigModel toModel(String group, Map<String, Object> map) throws Exception {
        Class<? extends AbstractConfigModel> clazz = AbstractConfigModel.getModelClass(group);

        return TempSm2Cache.decrypt(map, clazz);
    }
}
