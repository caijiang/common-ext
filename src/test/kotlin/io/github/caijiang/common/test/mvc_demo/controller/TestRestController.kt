package io.github.caijiang.common.test.mvc_demo.controller

import org.springframework.http.HttpHeaders
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Controller
import org.springframework.web.bind.annotation.*

/**
 * @author CJ
 */
@Controller
class TestRestController {

    /**
     * 显示输入的请求参数
     */
    @RequestMapping("/echoUrlEncoded")
    @ResponseBody
    fun echoUrlEncode(@RequestParam p1: String, @RequestParam p2: String): String {
        return "$p1+$p2"
    }

    @GetMapping("/setCookie")
    fun setCookie(): ResponseEntity<String> {
        return ResponseEntity<String>("well", HttpHeaders().apply {
            this[HttpHeaders.SET_COOKIE] = "current=foo"
        }, HttpStatus.OK)
    }

    @GetMapping("/readCookie")
    @ResponseBody
    fun readCookie(@CookieValue current: String?) = current

    @GetMapping("/readString")
    @ResponseBody
    fun readString() = "String"

    @GetMapping("/readList")
    @ResponseBody
    fun readList() = listOf(
        mapOf("key1" to "value1", "key2" to "value2"),
        mapOf("key3" to "value3", "key4" to "value4"),
    )

    @GetMapping("/readObject")
    @ResponseBody
    fun readObject() = mapOf("key1" to "value1", "key2" to "value2")

    @GetMapping("/readComplexList")
    @ResponseBody
    fun readComplexList() = listOf(
        mapOf(
            "key1" to "value1", "key2" to mapOf(
                "key3" to "value3",
            )
        ),
    )

    @GetMapping("/readComplexObject")
    @ResponseBody
    fun readComplexObject() = mapOf(
        "key1" to "value1", "key2" to listOf(
            mapOf("key3" to "value3", "key4" to "value4"),
        )
    )
}