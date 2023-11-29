# Spring Boot 启动事件顺序
1、ApplicationStartingEvent

这个事件在 Spring Boot 应用运行开始时，且进行任何处理之前发送（除了监听器和初始化器注册之外）。

2、ApplicationEnvironmentPreparedEvent

这个事件在当已知要在上下文中使用 Spring 环境（Environment）时，在 Spring 上下文（context）创建之前发送。

3、ApplicationContextInitializedEvent

这个事件在当 Spring 应用上下文（ApplicationContext）准备好了，并且应用初始化器（ApplicationContextInitializers）已经被调用，在 bean 的定义（bean definitions）被加载之前发送。

4、ApplicationPreparedEvent

这个事件是在 Spring 上下文（context）刷新之前，且在 bean 的定义（bean definitions）被加载之后发送。

5、ApplicationStartedEvent

这个事件是在 Spring 上下文（context）刷新之后，且在 application/ command-line runners 被调用之前发送。

6、AvailabilityChangeEvent

这个事件紧随上个事件之后发送，状态：ReadinessState.CORRECT，表示应用已处于活动状态。

7、ApplicationReadyEvent

这个事件在任何 application/ command-line runners 调用之后发送。

8、AvailabilityChangeEvent

这个事件紧随上个事件之后发送，状态：ReadinessState.ACCEPTING_TRAFFIC，表示应用可以开始准备接收请求了。

9、ApplicationFailedEvent

这个事件在应用启动异常时进行发送。