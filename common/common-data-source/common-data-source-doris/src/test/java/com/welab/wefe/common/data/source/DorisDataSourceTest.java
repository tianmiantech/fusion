package com.welab.wefe.common.data.source;

import com.welab.wefe.common.data_table.DataTable;
import com.welab.wefe.common.exception.StatusCodeWithException;
import com.welab.wefe.common.util.JObject;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author zane.luo
 * @date 2023/5/16
 */
class DorisDataSourceTest {

    static DorisDataSourceClient CLIENT;
    static JdbcDataSourceParams params = JdbcDataSourceParams.of(
            "10.11.21.50",
            19030,
            "root",
            "123456",
            "demo"
    );

    @BeforeAll
    static void before() {
        SuperDataSourceClient.register(DorisDataSourceClient.class);
        CLIENT = SuperDataSourceClient.create("doris", params);
    }


    @Test
    void testConnectionByModelParams() throws StatusCodeWithException {
        DorisDataSourceClient dataSource = SuperDataSourceClient.create("doris", params);
        dataSource.test();
    }

    @Test
    void testConnectionByJsonParams() throws StatusCodeWithException {
        DorisDataSourceClient dataSource = SuperDataSourceClient.create("mysql", JObject.create(params));
        dataSource.test();
    }

    @Test
    void base() throws Exception {
        CLIENT.test();

        System.out.println("listTables:");
        for (String table : CLIENT.listTables()) {
            System.out.println(table);
        }
    }


    @Test
    void scan() {
        try (Stopwatch ignored = new Stopwatch("scan")) {
CLIENT.scan("select * from `test_1e`",
        (i, row) -> {
            if (i % 10000 == 0) {
                System.out.println(i);
            }
        }
);
            System.out.println("scan done!");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    void showQueryTimeout() throws Exception {
        try (Stopwatch ignored = new Stopwatch("showQueryTimeout")) {
            DataTable table = CLIENT.scan("SHOW VARIABLES LIKE '%query_timeout%';");
            table.print();
        }
    }
}