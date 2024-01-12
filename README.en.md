![status](https://github.com/caijiang/common-ext/actions/workflows/main.yaml/badge.svg)
# common-ext

this is a common extensions written in kotlin, work for jvm(8+). it almost has nothing depends on runtime, user should
declare it by themselves.

There has 2 artifacts for both Java EE and Jakarta EE.

## Requirement-Java-EE

- java 8+

## Requirement-Jakarta-EE

- java 17+

## Getting Start

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
