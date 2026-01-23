package io.github.caijiang.common.notify

/**
 * 可被通知的消息，通常推荐由异常实现
 * @author CJ
 */
interface Notifiable {
    /**
     * 紧急通知的角色
     */
    val urgentRole: UrgentRole?

    /**
     * 它不应该通知到这个渠道
     */
    fun excludeChannels(): Set<NotifyChannel>? = null

    /**
     * 内容
     */
    val notifiableMessage: NotifiableMessage
}