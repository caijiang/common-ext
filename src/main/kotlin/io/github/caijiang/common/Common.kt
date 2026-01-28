package io.github.caijiang.common

/**
 * @author CJ
 */
object Common {

    /**
     * @return 类是否存在
     */
    @JvmStatic
    fun isClassPresent(className: String, classLoader: ClassLoader? = null): Boolean {
        return try {
            Class.forName(
                className,
                /* initialize = */ false,
                classLoader ?: Thread.currentThread().contextClassLoader
            )
            true
        } catch (e: ClassNotFoundException) {
            false
        }
    }

}