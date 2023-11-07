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
package com.welab.fusion.service.database.repository.base;

import com.welab.fusion.service.database.entity.AbstractDbModel;
import com.welab.wefe.common.util.ClassUtils;
import com.welab.wefe.common.util.ReflectionsUtil;
import com.welab.wefe.common.web.Launcher;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author zane
 * @date 2021/11/11
 */
public class RepositoryManager {
    /**
     * AbstractMySqlModel : BaseRepository
     */
    private static final Map<Class<?>, Class<?>> MAP = new HashMap();

    public static <T extends BaseRepository> T get(Class<? extends AbstractDbModel> mysqlModelClass) {
        if (MAP.isEmpty()) {
            List<Class<?>> list = ReflectionsUtil
                    .getClassesImplementing(BaseRepository.class, "com.welab.wefe")
                    .stream()
                    .filter(x -> x.isInterface())
                    .collect(Collectors.toList());

            for (Class<?> repoClass : list) {
                Class<?> entityClass = ClassUtils.getGenericClass(repoClass, 0);
                if (entityClass != null) {
                    MAP.put(entityClass, repoClass);
                }
            }

        }

        return (T) Launcher.getBean(MAP.get(mysqlModelClass));
    }
}
