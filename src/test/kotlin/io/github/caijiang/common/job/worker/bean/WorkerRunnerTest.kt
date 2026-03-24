package io.github.caijiang.common.job.worker.bean

import io.github.caijiang.common.job.worker.SerializableJob
import kotlin.test.Test

/**
 * @author CJ
 */
class WorkerRunnerTest {
    @Test
    fun valid() {
        WorkerRunner.valid(object : SerializableJob {
            override val type: String
                get() = "product-export-task"
            override val parameters: Map<String, String>
                get() = mapOf("id" to "1", "batch_size" to "20")
        })
    }
}