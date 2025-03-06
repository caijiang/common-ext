package io.github.caijiang.common.test.mvc_demo.controller

import io.github.caijiang.common.HttpServletRequest
import io.github.caijiang.common.HttpServletResponse
import io.github.caijiang.common.servlet.logInSlf
import org.springframework.context.annotation.Configuration
import org.springframework.web.bind.annotation.RequestMapping

/**
 * @author CJ
 */
@Configuration
@RequestMapping("/log")
open class LogDemoController {
    @RequestMapping("/**")
    fun go(req: HttpServletRequest, response: HttpServletResponse) {
        req.logInSlf()
        response.sendError(200)
    }
}