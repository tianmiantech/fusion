# SuperDataSourceClient
由于 board、serving、fusion 都有添加数据源的功能，所以新建此模块以统一维护，减少重复开发。

此模块的优点：
1. 统一了语义，不再需要针对不同的数据库类型写 if-else。
2. 按需引用，避免引入不需要的数据库驱动导致 jar 包体积无意义增大。
3. 经验复用，踩坑只踩一遍。
4. 小细节，例如数据源类型指定 PostgreSQL、postgresql 和 pg 都好使。

坑举例：
- mysql 批量写入需要指定额外的数据库链接参数才能生效
- mysql 流式读取需要一套复杂的设置才会生效，普通方式查询会返回整个表所有数据，内存直接爆炸。
- doris 无法设置 maxReadLine，导致预览查询功能会返回整表全量数据，且 connection 无法强制关闭，关闭的时候会读完数据才关。
- doris 默认 query timeout 只有 5 分钟，数据量大的时候生成数据集或者过滤器半道上就失败了。

## 添加引用

```xml
<!-- 必须引用 core 模块 -->
<dependency>
    <groupId>com.welab.wefe</groupId>
    <artifactId>common-data-source-core</artifactId>
    <version>1.0.0</version>
</dependency>

<!-- 各种数据库类型根据项目需要选择引用 -->
<dependency>
    <groupId>com.welab.wefe</groupId>
    <artifactId>common-data-source-mysql</artifactId>
    <version>1.0.0</version>
</dependency>
<dependency>
    <groupId>com.welab.wefe</groupId>
    <artifactId>common-data-source-doris</artifactId>
    <version>1.0.0</version>
</dependency>
……
```

## 使用 SuperDataSourceClient

考虑到基于反射的自动加载性能不好，需要在程序启动的时候指定注册哪些数据源类型。
```java
// 在程序启动时注册需要使用的数据源类型
SuperDataSourceClient.register(MySqlDataSourceClient.class);
SuperDataSourceClient.register(DorisDataSourceClient.class);
```
<br>

创建 client 对象，目前数据源都是 Jdbc 类型的。
```java
JdbcDataSourceParams params = JdbcDataSourceParams.of(
        "10.11.21.50",
        19030,
        "root",
        "123456",
        "demo"
);

// 指定数据源类型和连接信息
JdbcDataSourceClient client = SuperDataSourceClient.create("doris", params);

// 连接信息支持 json 格式，很灵活。
JdbcDataSourceClient client = SuperDataSourceClient.create("doris", JObject.create(params));

// 泛型自动适配，可以直接声明获取子类 client。
DorisDataSourceClient client = SuperDataSourceClient.create("doris", JObject.create(params));

// 直接创建指定类型的数据源
MySqlDataSourceClient client = new MySqlDataSourceClient(
        host,
        port,
        username,
        password,
        database
);
```

<br>

常用方法展示：
```java
// 测试连通性
String message = client.test();
String message = client.testSql(sql);

// 获取指定表的字段列表
List<String> fields = client.listTableFields(tableName)
// 获取当前库的表列表
List<String> tables = client.listTables()
// 获取指定 sql 返回的字段列表。
List<String> headers = client.getHeaders(sql);

// 删除表
client.dropTable(tableName);

// 流式查询
client.scan("select * from `test_1e`",
    // 使用委托方法消费查询到的数据
    (rowIndex, row) -> {
        if (i % 10000 == 0) {
            System.out.println(i);
        }
    }
);

// 批量写入
client.saveBatch(sql, models, x -> {
    // 拼接 values
    return new Object[]{ x.getName(), x.getAge() };
});
```