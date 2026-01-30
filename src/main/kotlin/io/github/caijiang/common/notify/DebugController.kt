package io.github.caijiang.common.notify

import io.github.caijiang.common.PostConstruct
import io.github.caijiang.common.Slf4j.Companion.log
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * @author CJ
 */
@RestController
@RequestMapping("/notify/debug")
open class DebugController(
    private val sendNoticeService: SendNoticeService
) {

    @PostConstruct
    fun init() {
        log.debug("初始化")
    }

    @GetMapping
    fun sendDebugNotice() {
        UrgentRole.entries.forEach { role ->
            sendNoticeService.send(object : Notifiable {
                override val urgentRole: UrgentRole
                    get() = role
                override val notifiableMessage: NotifiableMessage
                    get() = object : NotifiableMessage {
                        override val title: String
                            get() = "\uD83D\uDC1E通知系统调试中"
                        override val textContent: String
                            get() = "🚀计划发往${role}的消息"
                        override val specialReminder: SpecialNotifyTarget?
                            get() = null
                    }
            })
        }

    }

}