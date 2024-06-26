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

import com.welab.fusion.service.database.entity.JobDbModel;
import com.welab.fusion.service.database.repository.base.BaseRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * @author zane.luo
 * @date 2023/11/24
 */
@Repository
public interface JobRepository extends BaseRepository<JobDbModel, String> {
    @Query(
            value = "select * from #{#entityName} where `status` in ('wait_run','running','wait_stop','wait_success');",
            nativeQuery = true)
    List<JobDbModel> findAllRunningJob();
}
