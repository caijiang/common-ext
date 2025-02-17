package io.github.caijiang.common.orchestration

/**
 * 单节点的部署阶段
 * @author CJ
 * @since 2.1.0
 */
enum class NodeDeployStage {
    /**
     * 准备阶段
     */
    Prepare,

    /**
     * 流量下线
     */
    SuspendIngress,

    /**
     * 流量检查
     */
    CheckIngress,

    /**
     * 执行更新
     */
    Execute,

    /**
     * 健康检查
     */
    HealthCheck,

    /**
     * 流量上线
     */
    ResumeIngress,

    /**
     * 检查可用以及结束
     */
    Post,

    /**
     * 并非一个阶段，而是一个完成的事件
     */
    Done,
}