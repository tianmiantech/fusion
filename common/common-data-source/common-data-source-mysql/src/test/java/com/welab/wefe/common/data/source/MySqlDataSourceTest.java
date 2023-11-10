package com.welab.wefe.common.data.source;

import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
class MySqlDataSourceTest {

    static MySqlDataSourceClient DATA_SOURCE;
    static JdbcDataSourceParams params = JdbcDataSourceParams.of(
            "10.11.20.252",
            31766,
            "wefe",
            "OJnx^B3x3DDx",
            "wefe_board_1"
    );

    @BeforeAll
    static void before() {
        SuperDataSourceClient.register(MySqlDataSourceClient.class);
        DATA_SOURCE = SuperDataSourceClient.create("mysql", params);
    }


    @Test
    void testConnectionByModelParams() throws StatusCodeWithException {
        MySqlDataSourceClient dataSource = SuperDataSourceClient.create("mysql", params);
        dataSource.test();
    }

    @Test
    void testConnectionByJsonParams() throws StatusCodeWithException {
        MySqlDataSourceClient dataSource = SuperDataSourceClient.create("mysql", JObject.create(params));
        dataSource.test();
    }

    @Test
    void base() throws Exception {
        DATA_SOURCE.test();

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