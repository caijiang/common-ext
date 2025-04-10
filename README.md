![构建状态](https://github.com/caijiang/common-ext/actions/workflows/main.yaml/badge.svg)

# 常用扩展

不包含任何运行时依赖的常用扩展，尽可能提供你每个入口的运行依赖版本推荐。
制品包含 2 个分支，一个是为 Java EE 构建另一个则会是 Jakarta EE

查看[版本历史](https://github.com/caijiang/common-ext/releases)。

## 环境要求-Java-EE

- java 8+

## 环境要求-Jakarta-EE

- java 17+

## 引入

maven

```xml

<dependency>
    <groupId>io.github.caijiang</groupId>
    <artifactId>common-ext-java</artifactId>
    <version>[last-version]</version>
</dependency>
```

```xml

<dependency>
    <groupId>io.github.caijiang</groupId>
    <artifactId>common-ext-jakarta</artifactId>
    <version>[last-version]</version>
</dependency>
```

gradle

```groovy
implementation 'io.github.caijiang:common-ext-java:[last-version]'
```

```groovy
implementation 'io.github.caijiang:common-ext-jakarta:[last-version]'
```

gradle

```kotlin
implementation("io.github.caijiang:common-ext-java:[last-version]")
```

```kotlin
implementation("io.github.caijiang:common-ext-jakarta:[last-version]")
```

## 功能介绍

### http-client

可以生成 curl 的 client 拦截器。

### lock

基于`spring LockRegistry`的业务自动锁，标注在方法上即可获得业务锁能力, 它会尽可能在 `Transactional`之前开始工作。

#### 配置

- 锁名称, 业务进行时会用到的锁，支持多个，如果缺省则采用当前方法的签名作为锁名称；如果多个则会同时上多个锁。
- key算法，是一段 `Spring EL` 其结果即为锁的 key，如果缺省则取第一个参数的 toString (没有就为空)

通过在Java配置中添加

```java

@EnableAutoLock
@Configuration
public class AnyConfigClass {
}
````

启用

#### 调试

通过调整日志`io.github.caijiang.common.lock`的等级即可在日志中获取相关信息。

### debounce 业务防抖

在业务实践中经常会发现一些业务，具备以下通性：

- 要求一定的实时性，但支持少量的延迟
- 除了业务主键没有其他业务数据
- 重复执行跟单次执行的业务结果没有任何差异

这些业务就可以使用防抖进行业务保护。

定义几个概念:

- 防抖时间: 是个时间范围(Duration)表示这个时间内重复业务发生，则延迟作业
- 死时间: 是个时间范围(Duration)表示这个时间内必须完成作业

主要技术手段是 MQ 和 redis(RedisConnectionFactory)。流程如下：

- 需执行防抖业务时,原子化检查业务主键是否已关联事务 id
  1. 不存在事务 id,新增，发出`死时间`延迟 MQ
  2. 事务内标记防抖结束时间戳；发出`防抖时间`延迟 MQ
- 收到MQ时
  1. 检查关联事务是否存在，不存在表示该项作业已完成，结束
  2. 为`防抖时间` MQ 且 标记防抖结束时间大于当前时间，防抖时间内业务重复发生，结束
  3. 移除所有事务缓存，并且实施作业

#### 对接流程

1. 设置必要系统参数
2. 引入 com.fasterxml.jackson.module:jackson-module-kotlin
3. 引入 rocketMQ (可通过 rocketmq-spring-boot-starter)
4. 引入 RedisConnectionFactory (可通过 spring-boot-starter-data-redis)
5. 新增 spring bean `DebounceCallbackService` 负责实施业务作业
6. 原业务入口用现有 spring bean `DebounceService` 代替实施