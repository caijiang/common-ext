package io.github.caijiang.common.test.solitary

import com.fasterxml.jackson.databind.ObjectMapper
import com.wix.mysql.EmbeddedMysql
import com.wix.mysql.Sources
import com.wix.mysql.SqlScriptSource
import com.wix.mysql.config.Charset
import com.wix.mysql.config.DownloadConfig
import com.wix.mysql.config.MysqldConfig
import com.wix.mysql.distribution.Version
import org.apache.commons.lang3.RandomStringUtils
import org.slf4j.LoggerFactory
import org.springframework.util.StringUtils
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.net.ServerSocket
import java.util.*
import java.util.function.Function
import kotlin.concurrent.Volatile

/**
 * 孤岛型测试辅助，目前依赖的环境最常用大概是 mysql,redis 所以目前就干这事儿 主要注意这些:
 * - linux跑的时候 需要 yum -y install numactl 安装依赖库
 * - 在个人环境，缓存可以是个人目录，如果是ci服务器，缓存目录会安装在/mysql 切记将这个目录加入到缓存中。
 * @author CJ
 */
object SolitaryHelper {
    @Volatile
    private var instance: EmbeddedMysql? = null

    @Volatile
    private var redisServer: RedisServerEntry? = null

    private val log = LoggerFactory.getLogger(SolitaryHelper.javaClass)


    /**
     * 启动 mysql
     * 会因此产生几个新增的系统属性
     *
     *  * mysql.port
     *  * mysql.database
     *  * mysql.username
     *  * mysql.password
     *
     *
     * @param version                     数据库版本，默认[Version.v5_7_latest]，可选
     * @param serverConfigBuilderFunction 协助构建服务配置，默认就是中国时区，可选
     * @param initScripts                 启动时应该执行的脚本，可选
     * @see Sources.fromURL
     * @see Sources.fromString
     * @see Sources.fromFile
     */
    @Suppress("unused")
    @JvmStatic
    fun mysql(
        version: Version?,
        serverConfigBuilderFunction: Function<MysqldConfig.Builder, MysqldConfig.Builder>?,
        vararg initScripts: SqlScriptSource
    ): EmbeddedMysql {
        if (instance != null) {
            return instance!!
        }
        instance = createMysql(version, serverConfigBuilderFunction, *initScripts)
        return instance!!
    }

    /**
     * 完成后以下系统属性会被设置
     *
     *  * redis.port
     *  * redis.password
     *
     *
     * @param noEmptyButCanBeNullPassword 可null但不可为空字符串的预设密码
     */
    @Suppress("unused")
    @JvmStatic
    fun redis(noEmptyButCanBeNullPassword: String?): RedisServerEntry? {
        if (redisServer != null) {
            return redisServer
        }
        redisServer = createRedis(noEmptyButCanBeNullPassword)
        return redisServer
    }

    @JvmStatic
    fun createRedis(noEmptyButCanBeNullPassword: String?): RedisServerEntry {
        try {
            val port = freePort()
            val password = noEmptyButCanBeNullPassword ?: RandomStringUtils.randomNumeric(6)
            val ss = createRedisServer(port, password)
            System.setProperty("redis.port", java.lang.String.valueOf(port))
            System.setProperty("redis.password", password)
            ss.start()

            val thread = Thread {
                log.info("try to stop redis")
                try {
                    ss.stop()
                } catch (e: Throwable) {
                    log.warn("when stop", e)
                }
            }
            thread.setDaemon(true)
            Runtime.getRuntime().addShutdownHook(thread)
            return ss
        } catch (e: Exception) {
            log.error("启动内置redis 实例", e)
            throw RuntimeException(e)
        }
    }

    private fun createRedisServer(port: Int, password: String): RedisServerEntry {
        val type = try {
            Class.forName("redis.embedded.core.RedisServerBuilder")
        } catch (e: Throwable) {
            Class.forName("redis.embedded.RedisServerBuilder")
        }

        try {
            val portMethod = Arrays.stream(type.declaredMethods)
                .filter { it: Method -> it.name == "port" }
                .findFirst()
                .orElseThrow {
                    IllegalStateException(
                        "找不到 port 方法"
                    )
                }
            val settingMethod = type.getDeclaredMethod("setting", String::class.java)
            val buildMethod = type.getDeclaredMethod("build")

            var builder = type.newInstance()
            builder = portMethod.invoke(builder, port)
            builder = settingMethod.invoke(builder, "requirepass $password")
            return RedisServerEntry(buildMethod.invoke(builder))
        } catch (e: NoSuchMethodException) {
            throw RuntimeException(e)
        } catch (e: InstantiationException) {
            throw RuntimeException(e)
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }


    @JvmStatic
    fun createMysql(
        version: Version?,
        serverConfigBuilderFunction: Function<MysqldConfig.Builder, MysqldConfig.Builder>?,
        vararg initScripts: SqlScriptSource
    ): EmbeddedMysql {
        try {
            // 没有严格说明要求的数据库版本，默认流程较广的 5.7
            val scf = serverConfigBuilderFunction ?: Function.identity()
            val config = scf.apply(
                MysqldConfig.aMysqldConfig(version ?: Version.v5_7_latest)
                    .withCharset(Charset.UTF8)
                    .withFreePort()
                    .withTimeZone("Asia/Shanghai")
                    .withServerVariable("max_connect_errors", 666)
                    .withServerVariable("lower_case_table_names", 2)
                    .withServerVariable("innodb_use_native_aio", false)
                    .withServerVariable(
                        "innodb_rollback_on_timeout",
                        true
                    ) //                    .withServerVariable("lower_case_file_system", true)
                    .withServerVariable("innodb_lock_wait_timeout", 4)
                    .withServerVariable("skip-log-bin", true)
                    .withServerVariable("skip_log_bin", true)
                    .withServerVariable("disable_log_bin", true)
            )
                .build()

            System.setProperty("mysql.port", config.port.toString())
            val database = "database"

            var builder: EmbeddedMysql.Builder = EmbeddedMysql.anEmbeddedMysql(config)
                .addSchema(
                    database, *initScripts
                )

            val aliFlowCaches = runInAliyunFlow()
            if (!aliFlowCaches.isNullOrEmpty()) {
                builder = builder
                    .withDownloadConfig(
                        DownloadConfig.aDownloadConfig()
                            .withCacheDir("${aliFlowCaches.last()}/mysql")
                            .build()
                    )
            } else {
                // 如果在 ci 环境 那就用ci 目录，因为设置良好的ci 目录具备缓存功能，反之则使用默认的用户目录
                val dir = System.getenv("CI_PROJECT_DIR")
                if (StringUtils.hasLength(dir)) {
                    builder = builder
                        .withDownloadConfig(
                            DownloadConfig.aDownloadConfig()
                                .withCacheDir("$dir/mysql")
                                .build()
                        )
                }
            }

            val instance = builder
                .start()

            val username = "username"
            val password = "password"

            instance.executeScripts(
                MysqldConfig.SystemDefaults.SCHEMA,
                Sources.fromString(
                    "create user '$username'@'%' IDENTIFIED by '$password'"
                ),
                Sources.fromString("grant all on *.* to '$username'@'%'")
            )

            System.setProperty("mysql.database", database)
            System.setProperty("mysql.username", username)
            System.setProperty("mysql.password", password)

            return instance
        } catch (e: Exception) {
            log.error("启动内置mysql 实例", e)
            throw RuntimeException(e)
        }
    }

    internal fun runInAliyunFlow(
        toEnv: (String) -> String?
        = { t -> System.getenv(t) }
    ): List<String>? {
        if (toEnv("CI_RUNTIME_VERSION") == null) {
            return null
        }
        //            CI_RUNTIME_VERSION=jdk17 这个似乎是 aliyun flow 的特殊标志
        //            caches=["/root/.m2","/root/.gradle/caches","/root/.npm","/root/.yarn","/go/pkg/mod","/root/.cache"]
        val caches = toEnv("caches")
        if (!StringUtils.hasText(caches)) {
            return null
        }
        return ObjectMapper().readTree(caches)
            .mapNotNull {
                if (it.isTextual) it.textValue()
                else null
            }
    }

    private fun freePort(): Int {
        ServerSocket(0).use { socket ->
            socket.reuseAddress = true
            return (socket.localPort)
        }
    }

}