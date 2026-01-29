package io.github.caijiang.common.job.worker

/**
 * @author CJ
 */
interface JobTypeRunner {
    /**
     * 在当前线程执行这个任务
     */
    fun run(job: SerializableJob)

    /**
     * 执行退出应用前的清理事项，关闭所有非守护线程。
     * @return true 已经成功完成, false 表示无法清理所有资源，调度系统会[Runtime.halt]强制退出
     */
    fun quitApplication(): Boolean
}