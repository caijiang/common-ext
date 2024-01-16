package io.github.caijiang.common.test

import org.springframework.core.io.Resource
import org.springframework.http.HttpEntity
import org.springframework.util.MultiValueMap
import java.io.InputStream


fun InputStream.createUrlEncodeHttpEntityFromProperties(): HttpEntity<MultiValueMap<String, String>> {
    return TestUtils.createUrlEncodeHttpEntityFromProperties(this)
}

fun Resource.createUrlEncodeHttpEntityFromProperties(): HttpEntity<MultiValueMap<String, String>> {
    return TestUtils.createUrlEncodeHttpEntityFromProperties(this.inputStream)
}