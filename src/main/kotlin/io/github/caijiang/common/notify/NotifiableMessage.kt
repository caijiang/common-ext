package io.github.caijiang.common.notify

/**
 * 消息内容
 * @author CJ
 */
interface NotifiableMessage {

    /**
     * 需要通知标题
     */
    val title: String?

    /**
     * 需要通知的内容
     */
    val textContent: String?

    /**
     * 特别通知的对象
     */
    val specialReminder: SpecialNotifyTarget?
}