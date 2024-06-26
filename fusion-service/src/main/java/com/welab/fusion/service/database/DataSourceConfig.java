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

import com.welab.fusion.service.FusionService;
import com.welab.fusion.service.database.repository.base.BaseRepositoryFactoryBean;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.sqlite.SQLiteConfig;
import org.sqlite.javax.SQLiteConnectionPoolDataSource;

import javax.sql.DataSource;

/**
 * @author zane.luo
 **/
@Configuration
@EntityScan(basePackageClasses = FusionService.class)
@EnableJpaRepositories(
        basePackageClasses = FusionService.class,
        repositoryFactoryBeanClass = BaseRepositoryFactoryBean.class
        // entityManagerFactoryRef = "entityManagerFactoryRefBoard",
        // transactionManagerRef = "transactionManagerRefWefeBoard"
)
public class DataSourceConfig {
    //
    // @Autowired
    // protected JpaProperties mProperties;
    //
    // protected LocalContainerEntityManagerFactoryBean entityManagerFactoryRef(
    //         EntityManagerFactoryBuilder builder
    //         , DataSource ds
    //         , JpaProperties props
    //         , Class<?>... basePackageClasses) {
    //
    //
    //     return builder.dataSource(ds)
    //             .properties(props.getProperties())
    //             .packages(basePackageClasses)
    //             .persistenceUnit("SQLitePersistenceUnit")
    //             .build();
    // }
    //
    // @Bean
    // @Primary
    // DataSource createDataSource() {
    //     // DruidDataSource dataSource = DruidDataSourceBuilder.create().build();
    //     // // SQLite 是文件数据库，不支持并发。
    //     // dataSource.setMaxActive(2);
    //     // dataSource.setMaxWait(1000 * 10);
    //     // dataSource.getProxyFilters().add(new SqliteMonitor());
    //
    //     SQLiteConfig config = new SQLiteConfig();
    //     config.setJournalMode(SQLiteConfig.JournalMode.WAL);
    //     config.setSynchronous(SQLiteConfig.SynchronousMode.FULL);
    //
    //     SQLiteConnectionPoolDataSource dataSource = new SQLiteConnectionPoolDataSource(config);
    //     dataSource.setUrl("jdbc:sqlite:D:\\data\\fusion\\fusion1.db");
    //
    //     return dataSource;
    //     // return new MyDataSource();
    // }

    //
    //
    // @Bean("entityManagerFactoryRefBoard")
    // @Primary
    // LocalContainerEntityManagerFactoryBean entityManagerFactoryRefWefeBoard(
    //         EntityManagerFactoryBuilder builder, DataSource dataSource) {
    //     Map<String, String> pros = mProperties.getProperties();
    //     pros.put("hibernate.physical_naming_strategy", SpringPhysicalNamingStrategy.class.getName());
    //     pros.put("hibernate.implicit_naming_strategy", SpringImplicitNamingStrategy.class.getName());
    //
    //     return entityManagerFactoryRef(
    //             builder,
    //             dataSource,
    //             mProperties,
    //             FusionService.class
    //     );
    // }
    //
    // @Bean
    // @Primary
    // PlatformTransactionManager transactionManagerRefWefeBoard(
    //         @Qualifier("entityManagerFactoryRefBoard") LocalContainerEntityManagerFactoryBean factoryBean) {
    //
    //     return new JpaTransactionManager(factoryBean.getObject());
    // }

}
