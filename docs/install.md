
## 安装

### 关键目录说明
- `common`：后端公共代码
- `fusion-core`：后端核心代码
- `fusion-service`：后端核心代码
- `fusion-website`：前端页面以及资源
  
### 本地调试-后端

后端为基于 spring-boot 的 java web 程序，依赖 JDK8 和 Maven 环境。

本地调试 MainClass：`com.welab.fusion.service.FusionService`

相关配置项：
```properties
# ************************************************
# web server
# ************************************************
server.port=8080
server.servlet.context-path=/fusion
spring.servlet.multipart.max-file-size=1024MB
spring.servlet.multipart.max-request-size=1024MB

# 此工具存储文件的根目录，不需要提前创建。
# 存储的文件有过滤器、对齐结果等。
# 默认值：
# - Linux: /data/fusion
# - Windows: D:/data/fusion
fusion.file-system.base-dir=

# ************************************************
# logging
# ************************************************
logging.level.root=info
logging.level.com.ibatis=info
logging.file.name=/data/logs/fusion/fusion.log
logging.file.max-history=60
logging.file.max-size=20GB
logging.pattern.console=%clr(%d{${LOG_DATEFORMAT_PATTERN:-yyyy-MM-dd HH:mm:ss.SSS}}){faint} %clr(${LOG_LEVEL_PATTERN:-%5p}) [%X{requestId}] %clr([%15.15t]){faint} %clr([%F:%L]){cyan} %clr(:){faint} %m%n${LOG_EXCEPTION_CONVERSION_WORD:-%wEx}
logging.pattern.file=%d{yyyy-MM-dd HH:mm:ss.SSS} [%level] [%X{requestId}] ${PID:- } [%15.15t] %-40.40logger{39}[%F:%L] : %m%n


# ************************************************
# spring jpa
# ************************************************
# 驱动名称
spring.datasource.driver-class-name=org.sqlite.JDBC
spring.datasource.hikari.maximum-pool-size=1
spring.datasource.hikari.auto-commit=true
spring.datasource.hikari.max-wait=60000
spring.datasource.hikari.connection-timeout=120000
#显示数据库操作记录
spring.jpa.show-sql=false
#每次启动更改数据表结构
spring.jpa.hibernate.ddl-auto=update
# 连接字符串
spring.datasource.url=jdbc:sqlite:fusion.db
```


### 本地调试-前端
1. 进入到前端目录 `cd fusion-website`
2. 安装依赖 `npm install` 
3. 启动前端服务 `npm run dev` 
   
***注意:*** 前端默认使用 `window.location.origin` 为接口地址，如需修改请在 `fusion-website\src\utils\request.ts` 中修改 `getRequestBaseURL()`函数。



### 部署
1. 执行脚本 `bash funsion-service\assembly\scripts\buildWebSource`  将前端资源编译到 后端项目的 `fusion-service\src\main\resources\website` 目录下
2. 执行 `mvn -T 1C clean install -Dmaven.test.skip=true -am -pl fusion-service` 打包后端项目
3. 运行后端项目 `java -jar fusion-service\target\fusion.jar`
4. 访问 `http://localhost:8080/fusion/website` 页面

***注意：*** windows电脑上无法执行shell脚本，需要手动将前端资源编译到后端项目的 `fusion-service\src\main\resources\website` 目录下

#### 前端单独编译
详见 [前端单独编译](../fusion-website/README.md).