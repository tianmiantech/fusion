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

import com.welab.wefe.common.util.StringUtil;

import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
@DataSource(typeNames = {"hive", "impala"})
public class HiveDataSourceClient extends JdbcDataSourceClient<JdbcDataSourceParams> {

    public HiveDataSourceClient(JdbcDataSourceParams params) {
        super(params);
    }

    @Override
    protected Connection createConnection(boolean batchModel) throws Exception {
        Class.forName("org.apache.hive.jdbc.HiveDriver");
        String url = String.format("jdbc:hive2://%s:%d/%s", params.host, params.port, params.databaseName);

        if (StringUtil.isNotEmpty(params.userName) && StringUtil.isNotEmpty(params.password)) {
            return DriverManager.getConnection(url, params.userName, params.password);
        } else {
            return DriverManager.getConnection(url);
        }

    }

    @Override
    public boolean execute(String sql) throws Exception {
        return super.execute(sql);
    }

    @Override
    protected JdbcScanner createScanner(Connection conn, String sql, long maxReadLine, List<String> returnFields) throws Exception {
        return new HiveScanner(conn, sql, maxReadLine, returnFields);
    }
}
