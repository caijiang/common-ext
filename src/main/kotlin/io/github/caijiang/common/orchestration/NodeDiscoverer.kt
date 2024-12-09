package io.github.caijiang.common.orchestration

/**
 * 节点发现
 * @author CJ
 */
interface NodeDiscoverer {

    /**
     * @param service 服务
     * @return 服务节点，可能没有元素
     */
    fun discoverNodes(service: Service): List<ServiceNode>

}