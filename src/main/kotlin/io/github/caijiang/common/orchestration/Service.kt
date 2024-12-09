package io.github.caijiang.common.orchestration

/**
 * 在容器编排中的服务,可以参考 https://kubernetes.io/zh-cn/docs/concepts/services-networking/
 * @author CJ
 */
interface Service {
    /**
     * 唯一服务编码
     */
    val id: String

    /**
     * ### 部署指令
     * 肯定是设备上的可执行指令，部署完成即可结束并且返回 0，任何错误发生都必须返回非0
     * 其必须接受的参数列表如下
     * - 服务id
     * - 服务 ip
     * - 服务 port
     * - 镜像地址
     * - 镜像 tag
     * - 环境变量名称 1
     * - 环境变量值 1
     * - ...
     *
     *
     *
     */
    val deployCommand: String

    /**
     * 健康检查机制
     */
    val healthCheck: HealthCheck
}