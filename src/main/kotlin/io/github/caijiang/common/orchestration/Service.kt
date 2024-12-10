package io.github.caijiang.common.orchestration

/**
 * 在容器编排中的服务,可以参考 https://kubernetes.io/zh-cn/docs/concepts/services-networking/
 * @author CJ
 */
interface Service {
    /**
     * 唯一服务编码，简单字符集合包括大小写拉丁字母，阿拉伯数字以及短杆
     */
    val id: String

    /**
     * 服务类型，简单字符集合包括大小写拉丁字母，阿拉伯数字以及短杆
     * 表示服务的基础特性集合，比如提供特定端口，特定协议入口等等。
     * 由客户端代码自行定义
     */
    val type: String?

    /**
     * ### 部署指令
     * 肯定是设备上的可执行指令，部署完成即可结束并且返回 0，任何错误发生都必须返回非0
     * 其必须接受的参数列表如下
     * - 服务id
     * - 服务 ip
     * - 服务 port
     * - 镜像地址
     * - 镜像 tag
     * - 服务类型(缺省会传入空值)
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