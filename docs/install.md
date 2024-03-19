
## 安装

### 关键目录说明
- `common`：后端公共代码
- `fusion-core`：后端核心代码
- `fusion-service`：后端核心代码
- `fusion-website`：前端页面以及资源
  
### 本地调试-后端

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