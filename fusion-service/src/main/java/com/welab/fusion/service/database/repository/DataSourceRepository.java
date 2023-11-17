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
package com.welab.fusion.service.database.repository;

import com.welab.fusion.service.database.entity.DataSourceDbModel;
import com.welab.fusion.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/16
 */
@Repository
public interface DataSourceRepository extends BaseRepository<DataSourceDbModel, String> {
    @Query(value = "select count(*) from #{#entityName} where name=?1", nativeQuery = true)
    int countByName(String name);

    @Query(value = "select name from #{#entityName} where id=?1")
    String getNameById(String dataSourceId);

    List<DataSourceDbModel> findByHostAndPort(String host, Integer port);

}
