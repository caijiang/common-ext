![构建状态](https://github.com/caijiang/common-ext/actions/workflows/main.yaml/badge.svg)

# 常用扩展

不包含任何运行时依赖的常用扩展，尽可能提供你每个入口的运行依赖版本推荐。
制品包含 2 个分支，一个是为 Java EE 构建另一个则会是 Jakarta EE

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