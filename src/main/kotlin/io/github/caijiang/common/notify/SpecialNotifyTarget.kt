package io.github.caijiang.common.notify

/**
 * 特别通知的对象
 * @author CJ
 */
interface SpecialNotifyTarget {

    val name: String

    /**
     * 可以是openId或者 userId,[参考链接](https://open.feishu.cn/document/client-docs/bot-v3/add-custom-bot?lang=zh-CN#756b882f)
     */
    fun toFeishuId(): String? = null
}