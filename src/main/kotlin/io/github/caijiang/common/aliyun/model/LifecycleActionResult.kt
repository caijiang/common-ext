package io.github.caijiang.common.aliyun.model

/**
 * @author CJ
 */
enum class LifecycleActionResult {
    /**
     * 继续响应弹性扩张活动，将ECS实例添加至伸缩组；继续响应弹性收缩活动，将ECS实例从伸缩组移除。
     */
    CONTINUE,

    /**
     * 终止弹性扩张活动，直接释放创建出来的ECS实例；继续响应弹性收缩活动，将ECS实例从伸缩组移除。
     */
    ABANDON,

    /**
     * 针对弹性收缩活动，会拒绝释放ECS实例，进行回滚；针对弹性扩张活动，效果同ABANDON一样。
     */
    ROLLBACK,
}