package io.github.caijiang.common.json

import com.alibaba.fastjson.JSON
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.gson.Gson
import io.github.caijiang.common.Common
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToStream
import java.io.ByteArrayOutputStream

/**
 * 并非构建独立 json 处理体系，而是使用最小集功能做个多平台桥接
 * @author CJ
 * @since 2.6.0
 */
object SimpleJsonUtils {

    @OptIn(ExperimentalSerializationApi::class)
    @JvmStatic
    inline fun <reified T> writeToBinary(input: T): ByteArray {
        if (Common.isClassPresent("kotlinx.serialization.json.Json")) {
            val buf = ByteArrayOutputStream()
            Json.encodeToStream(input, buf)
            buf.close()
            return buf.toByteArray()
        }
        if (Common.isClassPresent("com.alibaba.fastjson.JSON")) {
            return JSON.toJSONBytes(input)
        }
        if (Common.isClassPresent("com.google.gson.Gson")) {
            return Gson().toJson(input).toByteArray(Charsets.UTF_8)
        }
        if (Common.isClassPresent("com.fasterxml.jackson.databind.ObjectMapper")) {
            return ObjectMapper().writeValueAsBytes(input)
        }
        throw IllegalStateException("啥 Json环境支持都没有")
    }

}