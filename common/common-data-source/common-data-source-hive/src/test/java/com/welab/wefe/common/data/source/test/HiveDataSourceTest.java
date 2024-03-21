package com.welab.wefe.common.data.source.test;

import com.welab.wefe.common.data.source.HiveDataSourceClient;
import com.welab.wefe.common.data.source.JdbcDataSourceParams;
import com.welab.wefe.common.data.source.Stopwatch;
import com.welab.wefe.common.data.source.SuperDataSourceClient;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
class HiveDataSourceTest {

    static HiveDataSourceClient DATA_SOURCE;
    static JdbcDataSourceParams params = JdbcDataSourceParams.of(
            "10.11.21.50",
            10000,
            null,
            null,
            "test"
    );

    @BeforeAll
    static void before() {
        SuperDataSourceClient.register(HiveDataSourceClient.class);
        DATA_SOURCE = SuperDataSourceClient.create("hive", params);
    }


    @Test
    void testConnectionByModelParams() throws StatusCodeWithException {
        HiveDataSourceClient dataSource = SuperDataSourceClient.create("hive", params);
        String message = dataSource.test();
        if (message == null) {
            System.out.println("connect success");
        } else {
            System.out.println("connect fail: " + message);
        }
    }

    @Test
    void testConnectionByJsonParams() throws StatusCodeWithException {
        HiveDataSourceClient dataSource = SuperDataSourceClient.create("hive", JObject.create(params));
        String message = dataSource.test();
        if (message == null) {
            System.out.println("connect success");
        } else {
            System.out.println("connect fail: " + message);
        }
    }

    @Test
    void base() throws Exception {
        System.out.println("listTables:");
        for (String table : DATA_SOURCE.listTables()) {
            System.out.println(table);
        }
    }


    @Test
    void scan() throws Exception {
        try (Stopwatch ignored = new Stopwatch("scan")) {
            DATA_SOURCE.scan("select * from `test_promoter_10w`",
                    (i, row) -> {
                        // System.out.println(JSON.toJSONString(row));
                        // System.out.println(row.get("id"));
                    }
            );
            System.out.println("scan done!");
        }
    }
}