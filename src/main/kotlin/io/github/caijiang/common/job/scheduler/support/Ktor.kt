package io.github.caijiang.common.job.scheduler.support

import io.github.caijiang.common.job.scheduler.Scheduler
import io.github.caijiang.common.job.worker.PersistentJob
import io.github.caijiang.common.job.worker.TemporaryJob
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import java.util.*

class KtorTemporaryJob(override val type: String, override val parameters: Map<String, String>) : TemporaryJob
class KtorPersistentJob(
    override val type: String,
    override val name: String,
    override val parameters: Map<String, String>
) : PersistentJob

/**
 * 安装调度器
 * @since 2.6.0
 */
@Deprecated("别用了，因为 ktor 跨版本的 api并不稳定;看代码直接复制好了")
fun Application.installScheduler(serverConfig: ServerConfig, scheduler: Scheduler) {
    routing {
        post((serverConfig.prefix ?: "") + "/t/{env}/{hostname}/{type}") {
            val job = KtorTemporaryJob(
                call.parameters["type"]!!, call.receive()
            )
            scheduler.submitTemporaryJob(
                call.parameters["env"]!!,
                call.parameters["hostname"]!!,
                job
            )
            call.respond(HttpStatusCode.NoContent)
        }
        put((serverConfig.prefix ?: "") + "/p/{env}/{hostname}/{type}/{name}") {
            val job = KtorPersistentJob(
                call.parameters["type"]!!,
                call.parameters["name"]!!,
                call.receive()
            )
            scheduler.submitPersistentJob(
                call.parameters["env"]!!,
                call.parameters["hostname"]!!,
                call.parameters["cron"]!!,
                job,
                call.parameters["timezone"]?.let {
                    TimeZone.getTimeZone(it)
                } ?: TimeZone.getDefault()
            )
            call.respond(HttpStatusCode.NoContent)
        }
    }

}
