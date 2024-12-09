package io.github.caijiang.common.orchestration

/**
 * 所谓声明健康就是一个可重复执行的指令，返回 0 即为健康
 * @author CJ
 */
interface HealthCheck {
    /**
     * @return 健康检查指令
     */
    fun toHealthCheckCommand(node: ServiceNode): String

    /**
     * @return 是否健康
     * @param stdOutput [toHealthCheckCommand] 指令的结果
     * @param exitCode 指令退出代码
     */
    fun checkHealth(stdOutput: String?, exitCode: Int): Boolean
}