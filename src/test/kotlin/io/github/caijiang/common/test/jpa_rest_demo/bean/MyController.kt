package io.github.caijiang.common.test.jpa_rest_demo.bean

import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RestController

@RestController
class MyController {

    @GetMapping("/int0")
    fun int0() = 0

}