![构建状态](https://github.com/caijiang/common-ext/actions/workflows/main.yaml/badge.svg)
# 常用扩展

不包含任何运行时依赖的常用扩展，尽可能提供你每个入口的运行依赖版本推荐。

## 环境要求

- java 8+

## 引入

maven

```xml
<dependency>
  <groupId>io.github.caijiang</groupId>
  <artifactId>common-ext</artifactId>
  <version>last-version</version>
</dependency>
```

gradle

```groovy
implementation 'io.github.caijiang:common-ext:last-version'
```

gradle

```kotlin
implementation("io.github.caijiang:common-ext:last-version")
```

## 功能介绍

### http-client

可以生成 curl 的 client 拦截器。