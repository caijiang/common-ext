package io.github.caijiang.common.test

import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.MediaType
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap
import java.io.InputStream
import java.util.*

/**
 * @since 0.0.5
 * @author CJ
 */
object TestUtils {

    @JvmStatic
    fun createUrlEncodeHttpEntityFromProperties(input: InputStream): HttpEntity<MultiValueMap<String, String>> {
        val data = input.use {
            val ps = Properties()
            ps.load(it)
            ps
        }

        val headers = HttpHeaders()
        headers.contentType = MediaType.APPLICATION_FORM_URLENCODED

        val map: MultiValueMap<String, String> = LinkedMultiValueMap()
        data.stringPropertyNames()
            .forEach {
                map.add(it, data[it].toString())
            }
        return HttpEntity(map, headers)
    }

}