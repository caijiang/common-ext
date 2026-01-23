package io.github.caijiang.common.notify.config

import io.github.caijiang.common.PreDestroy
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.notify.*
import java.util.concurrent.Executors

/**
 * @author CJ
 */
open class SendNoticeServiceImpl(
    private val notifyConfiguration: NotifyConfiguration
) : SendNoticeService {
    private val pool = Executors.newFixedThreadPool(notifyConfiguration.threadSize)

    @PreDestroy
    fun beforeClose() {
        pool.shutdown()
    }

    private fun sendMessage(targetConfig: NotifyConfiguration.TargetConfig, message: NotifiableMessage) {
        log.debug("准备发送消息:{} 通过:{}", message.title, targetConfig)

        when (targetConfig.channel) {
            NotifyChannel.Feishu -> {
                FeishuNotifyTool.sendMessage(
                    targetConfig.properties!!["url"].toString(),
                    message,
                    targetConfig.properties["key"]?.toString()
                )
            }

            else -> {
                throw IllegalArgumentException("暂不支持通知通道:${targetConfig.channel}")
            }
        }
    }

    override fun send(notify: Notifiable) {
        pool.submit {
            try {
                notify.urgentRole?.let { role ->
                    val excludes = notify.excludeChannels()
                    notifyConfiguration.roles[role]?.filter {
                        excludes == null || !excludes.contains(it.channel)
                    }?.forEach {
                        sendMessage(it, notify.notifiableMessage)
                    }
                }
            } catch (ex: Exception) {
                log.error("发送通知时", ex)
            }
        }
    }

    override fun sendThrowable(throwable: Throwable) {
        sendThrowableCount(throwable, 5)
    }

    private fun sendThrowableCount(throwable: Throwable?, count: Int) {
        val tx = throwable ?: return
        if (tx is Notifiable) {
            send(tx)
        } else if (count <= 0) {
            return
        } else sendThrowableCount(tx.cause, count - 1)
    }
}