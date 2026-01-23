package io.github.caijiang.common.notify

/**
 * @author CJ
 * @since 2.5.0
 */
interface SendNoticeService {
    /**
     * 发送通知消息，发送消息并非核心业务逻辑，所以这部分操作会在异步中完成，不会汇报结果也不会丢出异常
     */
    fun send(notify: Notifiable)

    /**
     * 检查异常，如果确认该异常跟[Notifiable]关联的，则发送它
     */
    fun sendThrowable(throwable: Throwable)
}