package io.github.caijiang.common.test.mvc_demo.controller

import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

/**
 * @author CJ
 */
@RestController
class TestRestController {

    /**
     * 显示输入的请求参数
     */
    @RequestMapping("/echoUrlEncoded")
    fun echoUrlEncode(@RequestParam p1: String, @RequestParam p2: String): String {
        return "$p1+$p2"
    }
}