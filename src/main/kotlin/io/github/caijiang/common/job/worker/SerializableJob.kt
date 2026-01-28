package io.github.caijiang.common.job.worker

/**
 * 可序列化的任务
 * 任务可以分为类型，参数 2 个部分
 */
interface SerializableJob {
    /**
     * 类型，必须符合[RFC 1123](https://datatracker.ietf.org/doc/html/rfc1123)
     */
    val type: String

    /**
     * 参数
     * 其健名必须符合`[A-Za-z_][A-Za-z0-9_]*`的规则
     */
    val parameters: Map<String, String>

}

/**
 * 临时任务
 */
interface TemporaryJob : SerializableJob

/**
 * 持久任务
 */
interface PersistentJob : SerializableJob {
    /**
     * 全域唯一的任务名称,必须符合[RFC 1123](https://datatracker.ietf.org/doc/html/rfc1123)
     */
    val name: String
}