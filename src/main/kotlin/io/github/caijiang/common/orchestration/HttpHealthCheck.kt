package io.github.caijiang.common.orchestration

/**
 * 通过 curl 检查
 * @author CJ
 */
class HttpHealthCheck(
    /**
     * 必须是`/`开头的
     */
    private val uri: String,
    private val code3xxHealthy: Boolean = false,
    private val code2xxHealthy: Boolean = true,
) : HealthCheck {

    override fun toHealthCheckCommand(node: ServiceNode): String {
        @Suppress("HttpUrlsUsage")
        return "curl -o /dev/null -s -w \"%{http_code}\\n\" http://${node.ip}:${node.port}$uri"
    }

    override fun checkHealth(stdOutput: String?, exitCode: Int): Boolean {
        if (exitCode != 0) {
            return false
        }
        if (stdOutput == null) return false
        if (code2xxHealthy && stdOutput.startsWith("2")) {
            return true
        }
        if (code3xxHealthy && stdOutput.startsWith("3")) {
            return true
        }
        return false
    }
}