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