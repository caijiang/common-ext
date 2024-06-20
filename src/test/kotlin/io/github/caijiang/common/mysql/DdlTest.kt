package io.github.caijiang.common.mysql

import kotlin.test.Test


/**
 * @author CJ
 */
class DdlTest {

    @Test
    fun executeScriptResource() {
        System.getProperties().list(System.out)
        System.getenv().forEach { (t, u) ->
            println("env: $t $u")
        }
    }
}