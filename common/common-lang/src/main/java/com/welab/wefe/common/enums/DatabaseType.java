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

package com.welab.wefe.common.enums;

import java.util.*;

/**
 * Database type
 *
 * @author hunter.zhao
 */
public enum DatabaseType {
    /**
     * 枚举值命名规范
     * 1. 考虑到前端直接展示的需要，统一首字母大写。
     * 2. 参考各数据库官网书面称呼
     */
    MySQL,
    PostgreSQL,
    Hive,
    Kudu,
    HBase,
    Impala,
    Cassandra,
    SQLServer,
    ClickHouse,
    Elasticsearch,
    Oracle,
    Redis,
    Kafka,
    Doris;

    /**
     * Presto 支持的数据库类型
     * <p>
     * 如果支持，则意味着可以刷新元数据、添加 sql_table、参与联邦 SQL 任务。
     */
    public static List<DatabaseType> PRESTO_SUPPORT_DATABASE_TYPES = Arrays.asList(
            MySQL,
            Hive,
            ClickHouse
    );

    /**
     * 程序本地支持的数据库类型
     * <p>
     * 如果支持，则意味着可以从该类型的数据库读取数据创建数据集、过滤器。
     */
    public static List<DatabaseType> LOCAL_SUPPORT_DATABASE_TYPES = Arrays.asList(
            MySQL,
            Doris,
            Hive
    );


    public boolean supportPresto() {
        return PRESTO_SUPPORT_DATABASE_TYPES.contains(this);
    }

    public boolean supportLocal() {
        return LOCAL_SUPPORT_DATABASE_TYPES.contains(this);
    }

    public static Collection<DatabaseType> allSupport() {
        Set<DatabaseType> set = new HashSet<>(PRESTO_SUPPORT_DATABASE_TYPES.size() + LOCAL_SUPPORT_DATABASE_TYPES.size());
        set.addAll(PRESTO_SUPPORT_DATABASE_TYPES);
        set.addAll(LOCAL_SUPPORT_DATABASE_TYPES);
        return set;
    }
}
