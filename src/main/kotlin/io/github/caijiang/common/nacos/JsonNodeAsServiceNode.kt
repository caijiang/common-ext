package io.github.caijiang.common.nacos

import com.fasterxml.jackson.databind.JsonNode
import io.github.caijiang.common.Slf4j
import io.github.caijiang.common.Slf4j.Companion.log
import io.github.caijiang.common.orchestration.ServiceNode

/**
 * @author CJ
 */
@Slf4j
data class JsonNodeAsServiceNode(
    val root: JsonNode,
    override val ip: String = root["ip"].textValue(),
    override val port: Int = root["port"].intValue(),
    val weight: Double = root["weight"].doubleValue(),
    val healthy: Boolean = root["healthy"].booleanValue(),
    val enabled: Boolean = root["enabled"].booleanValue(),
    val ephemeral: Boolean = root["ephemeral"].booleanValue(),
    override val ingressLess: Boolean = !healthy || weight <= 0,
) : ServiceNode {
    fun work(): Boolean {
        log.debug("node:{}", this)
        return healthy && enabled
    }
}
