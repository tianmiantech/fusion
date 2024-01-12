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

package com.welab.fusion.service.database;

import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import com.welab.fusion.service.FusionService;
import com.welab.fusion.service.database.repository.base.BaseRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import javax.sql.DataSource;

/**
 * @author zane.luo
 **/
@Configuration
@EntityScan(basePackageClasses = FusionService.class)
@EnableJpaRepositories(
        basePackageClasses = FusionService.class,
        repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class
)
public class DataSourceConfig {

    @Bean
    @Primary
    DataSource createDataSource() {
        DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
        // SQLite 是文件数据库，不支持并发。
        dataSource.setMaxActive(1);

        return dataSource;
    }

}
