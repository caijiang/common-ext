package io.github.caijiang.common.mysql

import org.springframework.core.io.Resource
import org.springframework.jdbc.core.JdbcTemplate

/**
 * 负责执行 ddl
 * @author CJ
 */
object Ddl {

    enum class ExecutePolicy {
        /**
         * 执行诸如 insert,delete,update 等语句；缺省是忽略 dml
         */
        ExecuteDML,

    }

    /**
     * 执行 script 脚本
     * @see ExecutePolicy
     */
    @JvmStatic
    fun executeScriptResource(template: JdbcTemplate, resource: Resource) {

    }

}