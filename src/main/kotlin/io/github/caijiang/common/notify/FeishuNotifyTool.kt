package io.github.caijiang.common.notify

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.http.SimpleHttpUtils
import java.util.*
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec

/**
 * @author CJ
 */
object FeishuNotifyTool {
    private val mapper = ObjectMapper()

    @JvmStatic
    fun sendMessage(url: String, message: NotifiableMessage, key: String? = null) {
        if (message.title == null) {
            // 直接发送文本
            val textMessage = mapper.createObjectNode()
            textMessage.put("msg_type", "text")
            textMessage.putObject("content").apply {
                put("text", "${message.textContent ?: "空消息"} ${message.specialReminder?.let { atInText(it) } ?: ""}")
            }
            sendOriginMessage(url, textMessage, key)
            return
        }
        val postMessage = mapper.createObjectNode()
        postMessage.put("msg_type", "post")
        postMessage.putObject("content").apply {
            putObject("post").apply {
                putObject("zh_cn").apply {
                    put("title", message.title)
                    putArray("content").apply {
                        addArray().apply {
                            // tag,text
                            addObject().apply {
                                put("tag", "text")
                                put("text", message.textContent ?: "空消息")
                            }
                            // add at
                            message.specialReminder?.let { t ->
                                t.toFeishuId()?.let {
                                    addObject().apply {
                                        put("tag", "at")
                                        put("user_id", it)
                                        put("user_name", t.name)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        sendOriginMessage(url, postMessage, key)
    }

    private fun atInText(specialReminder: SpecialNotifyTarget): String {
        val id = specialReminder.toFeishuId() ?: return ""
        return "<at user_id=\"${id}\">${specialReminder.name}</at>"
    }

    @JvmStatic
    fun sendOriginMessage(url: String, message: ObjectNode, key: String? = null) {
        key?.let {
            val time = (System.currentTimeMillis() / 1000)
            val toSign = "${time}\n$it"
            val mac = Mac.getInstance("HmacSHA256")
            mac.init(SecretKeySpec(toSign.toByteArray(), "HmacSHA256"))
            val x = mac.doFinal(byteArrayOf())
            val sign = Base64.getEncoder().encodeToString(x)
            // 开启签名验证后发送文本消息
            message.put("timestamp", time.toString())
            message.put("sign", sign)
        }
        val response = SimpleHttpUtils.httpAccess(
            url, "POST", mapOf(
                "Content-Type" to "application/json; charset=utf-8",
            )
        ) {
            it.setBinary(mapper.writeValueAsBytes(message))
        }
        if (response.status != 200) {
            log.warn("发送飞书消息:{} 时服务端响应:{}", url, response.status)
            throw IllegalStateException("飞书response响应了:${response.status}")
        }
        val root = mapper.readTree(response.body)
        if (root == null || !root.isObject) {
            log.warn("发送飞书消息:{} 时飞书响应内容非标准:{}", url, root)
            throw IllegalStateException("飞书response body 响应了:${root}")
        }
        val code = root.get("code")
        if (code == null || !code.isNumber) {
            log.warn("发送飞书消息:{} 时飞书响应code为:{}", url, code)
            throw IllegalStateException("飞书response body-code 响应了:${code}")
        }
        if (code.intValue() != 0) {
            log.warn("发送飞书消息:{} 时飞书响应code为:{}", url, code)
            throw IllegalStateException("飞书response body-code 响应了:${root}")
        }
    }

}