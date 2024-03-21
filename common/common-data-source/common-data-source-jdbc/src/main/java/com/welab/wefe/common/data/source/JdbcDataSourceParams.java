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

import com.welab.wefe.common.fieldvalidate.annotation.Check;
import com.welab.wefe.common.fieldvalidate.secret.MaskStrategy;
import com.welab.wefe.common.fieldvalidate.secret.Secret;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
public class JdbcDataSourceParams extends DataSourceParams {
    @Check(messageOnEmpty = "数据库名称不能为空", require = true)
    public String databaseName;

    @Check(name = "用户名")
    public String userName;

    @Check(name = "密碼")
    @Secret(maskStrategy = MaskStrategy.PASSWORD)
    public String password;

    public static JdbcDataSourceParams of(String host, Integer port, String userName, String password, String databaseName) {
        JdbcDataSourceParams params = new JdbcDataSourceParams();
        params.host = host;
        params.port = port;
        params.databaseName = databaseName;
        params.userName = userName;
        params.password = password;

        return params;
    }
}
