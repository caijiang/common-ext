package io.github.caijiang.common.nacos

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.http.SimpleHttpResponse
import io.github.caijiang.common.http.SimpleHttpUtils
import java.net.URLEncoder
import java.util.prefs.Preferences

/**
 * @author CJ
 */
@Suppress("HttpUrlsUsage")
object OpenApiHelper {
    private val objectMapper = ObjectMapper()

    private fun <T : Any> work(
        locator: ResourceLocator,
        request: (accessToken: String?) -> SimpleHttpResponse,
        business: ((JsonNode) -> T)? = null,
    ): T? {
        val accessToken = fetchAccessToken(locator)
        val response = request(accessToken)
        log.debug("response : {}", response)

        if (response.status != 200) {
            throw IllegalArgumentException("bad status: ${response.status}:${String(response.body ?: byteArrayOf())}")
        }
        // 不是 json
        val root = objectMapper.readTree(response.body)
        if (root.isNull || !root.isObject) {
            throw IllegalArgumentException("bad response: ${String(response.body ?: byteArrayOf())}")
        }
        val code = root["code"]
        val message = root["message"]

        if (code.isNull || !code.isInt) {
            throw IllegalArgumentException("bad code: ${String(response.body ?: byteArrayOf())}")
        }
        if (code.intValue() != 0) {
            val msg = message?.let {
                if (it.isNull) ""
                else if (it.isTextual) it.textValue()
                else ""
            }
            throw IllegalArgumentException("${code.intValue()}: $msg")
        }
        if (business == null) {
            return null
        }

        return business(root["data"])
    }


    fun changeInstance(
        locator: ResourceLocator,
        serviceName: String,
        ip: String,
        port: Int,
        otherData: Map<String, Any?>? = null
    ) {
        work<Any>(locator, { s ->
            SimpleHttpUtils.httpAccess(
                urlFor(locator, "/nacos/v2/ns/instance", s), "PUT", requestBody = {
                    it.setContentType("application/x-www-form-urlencoded")
                    val allData =
                        (otherData ?: mapOf()) + ("ip" to ip) + ("serviceName" to serviceName) + ("port" to port)
                    it.setParametersFromMap(allData)
                }
            )
        })
    }

    /**
     * 查询指定服务的实例列表
     */
    fun listInstances(locator: ResourceLocator, serviceName: String): ArrayNode? {
        return work(locator, {
            SimpleHttpUtils.httpAccess(
                urlFor(
                    locator, "/nacos/v2/ns/instance/list", it, mapOf(
                        "serviceName" to serviceName
                    )
                )
            )
        }, {
            it["hosts"] as ArrayNode
        })
    }

    private fun urlFor(
        locator: ResourceLocator,
        uri: String,
        accessToken: String?,
        otherParameter: Map<String, String?>? = null
    ): String {
        val url = "http://${locator.serverAddr}$uri"
        val all =
            (otherParameter
                ?: emptyMap()) + ("accessToken" to accessToken) + ("namespaceId" to locator.namespaceId) + ("groupName" to locator.groupName) + ("clusterName" to locator.clusterName)
        val ps = all.entries
            .filter { it.value != null }.joinToString("&") {
                "${it.key}=${URLEncoder.encode(it.value, "UTF-8")}"
            }
        if (ps.isEmpty()) return url
        return "$url?$ps"
    }

    private fun fetchAccessToken(locator: ResourceLocator): String? {
        if (locator.accessToken != null) {
            return locator.accessToken
        }
        if (locator.auth == null) {
            return null
        }
        val preferences = Preferences.userNodeForPackage(OpenApiHelper::class.java)
        val prefix = locator.serverAddr + "_" + locator.auth.username
        val tokenInPreferences = preferences.get("${prefix}_token", "")
        if (System.currentTimeMillis() > preferences.getLong("${prefix}_dead", 0) || tokenInPreferences.trim()
                .isEmpty()
        ) {
            // 重新搞
            val requestTime = System.currentTimeMillis()
            val response =
                SimpleHttpUtils.httpAccess("http://${locator.serverAddr}/nacos/v1/auth/login", "POST", emptyMap(), {
                    it.setContentType("application/x-www-form-urlencoded")
                    it.setParameters("username" to locator.auth.username, "password" to locator.auth.password)
                })
            if (response.status != 200) {
                throw IllegalArgumentException("login failed: ${String(response.body ?: byteArrayOf())}")
            }
            val root = objectMapper.readTree(response.body)
            if (root.isNull || !root.isObject) {
                throw IllegalArgumentException("bad response: ${String(response.body ?: byteArrayOf())}")
            }
            val accessToken = root["accessToken"]
            if (accessToken.isNull || !accessToken.isTextual) {
                throw IllegalArgumentException("bad accessToken: $accessToken")
            }
            val tokenTtl = root["tokenTtl"]
            // ？？ 什么单位 算秒把
            if (!tokenTtl.isNull && tokenTtl.isInt) {
                // 保存起来
                preferences.putLong("${prefix}_dead", requestTime + tokenTtl.asInt() * 1000 - 1000)
                preferences.put("${prefix}_token", accessToken.asText())
                preferences.flush()
            }
            return accessToken.asText()
        }
        return tokenInPreferences
    }

}

private fun SimpleHttpUtils.EntityBuilder.setParametersFromMap(requestData: Map<String, Any?>) {
    val parameters = requestData.entries.filter { it.value != null }
        .map { it.key to it.value.toString() }
        .toTypedArray()
    setParameters(
        *parameters
    )
}
