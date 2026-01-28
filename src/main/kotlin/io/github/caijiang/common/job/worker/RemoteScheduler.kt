package io.github.caijiang.common.job.worker

import io.github.caijiang.common.http.SimpleHttpUtils
import io.github.caijiang.common.job.scheduler.Scheduler
import io.github.caijiang.common.json.SimpleJsonUtils
import java.net.URLEncoder
import java.util.*

/**
 * - 临时:POST /t/env/hostname/type
 * - 永久: PUT /p/env/hostname/type/name?cron,timezone(ID)
 * @author CJ
 */
class RemoteScheduler(private val url: String) : Scheduler {
    override fun submitTemporaryJob(env: String, hostname: String, job: TemporaryJob) {
        val response = SimpleHttpUtils.httpAccess(
            "$url/t/${env}/${hostname}/${job.type}", "POST", mapOf("Content-Type" to "application/json")
        ) {
            it.setBinary(SimpleJsonUtils.writeToBinary(job.parameters))
        }
        if (response.status / 100 != 2) {
            throw IllegalStateException("远程调度响应失败:${response.status}:${response.body?.toString(Charsets.UTF_8)}")
        }
    }

    override fun submitPersistentJob(
        env: String,
        hostname: String,
        cron: String,
        job: PersistentJob,
        timezone: TimeZone
    ) {
        val response = SimpleHttpUtils.httpAccess(
            "$url/p/${env}/${hostname}/${job.type}/${job.name}?cron=${
                URLEncoder.encode(
                    cron,
                    "UTF-8"
                )
            }&timezone=${URLEncoder.encode(timezone.id, "UTF-8")}",
            "POST",
            mapOf("Content-Type" to "application/json")
        ) {
            it.setBinary(SimpleJsonUtils.writeToBinary(job.parameters))
        }
        if (response.status / 100 != 2) {
            throw IllegalStateException("远程调度响应失败:${response.status}:${response.body?.toString(Charsets.UTF_8)}")
        }
    }
}