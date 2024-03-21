# 前后端敏感数据保护方案


> 概述：
> 通过自定义 fastjson 过滤器，实现敏感字段对外输出时进行拦截。
> 通过 SM2 公钥对前端提交的敏感字段加密，服务器端使用相应的私钥解密。


# 对输出的敏感字段进行保护

对敏感字段进行保护需要执行两个动作：
1. 被输出的数据需要是实体类对象，框架层会在实体序列化为 json 字符串时对敏感字段做保护，非实体类数据不受此策略影响。
2. 在实体类中对需要保护的字段添加 `@Secret` 注解。

使用示例：
```java
public class ClickHouseStorageConfigModel {
    @Secret(maskStrategy = MaskStrategy.PASSWORD)
    public String password;
}
```

`MaskStrategy` 中的四种保护策略：
1. BLOCK：阻止字段输出任何字节，输出 null。
2. PASSWORD：输出常量 "***************"。
3. PHONE_NUMBER：根据安全部门提供的规范，中间五位替换为星号。（e.g：138800000088 -> 1388*****88）
4. EMAIL：根据安全部门提供的规范，@ 符号左边打4位替换为星号。（e.g：Abc-hel@qq.com -> Abc****@qq.com）



实现原理：
1. 新增 `SecretValueFilter`，在其中对包含 @Secret 注解的字段进行保护。
2. 自定义 `HttpMessageConverters`，使用 fastjson 对 API 输出的内容做序列化。
3. 对 fastjson 添加 Filter，追加 `SecretValueFilter` 到 `FastJsonConfig`，使其生效。

`SecretValueFilter` 中关键代码：
```java
@Override
public Object process(Object object, String name, Object value) {
    Secret secret = SecretUtil.getAnnotation(object.getClass(), name);

    // 如果字段包含 @Secret 注解，使用注解声明的保护方式自定义输出内容。
    if (secret == null) {
        return value;
    }

    return secret.maskStrategy().get(value);
}
```


# 对输入的敏感字段进行保护

当前端向后台传输敏感字段时，根据安全要求，不能以明文方式传输。

本方案采用非对称加密保护敏感字段，需要前后端配合。

前端相关动作：
1. 请求 `crypto/generate_sm2_key_pair` 接口获取 sm2 公钥。
2. 使用公钥对需要保护的敏感字段加密。
3. 提交加密后的参数

后台相关动作：
1. 对前端提交的参数创建对应的实体类，并对敏感字段添加 @Secret 注解。
2. 根据实体类中的注解，找到需要解密的字段。
3. 从 `TempSm2Cache` 中获取 sm2 私钥，对加密字段进行解密。
4. 将解密后的 json 对象转为实体类，供后续业务使用。

后台部分整个过程在框架层面实现，使用时无感，且解密行为发生在进入 API 逻辑的最后一个阶段，请求参数在输出到日志文件中时为密文。

