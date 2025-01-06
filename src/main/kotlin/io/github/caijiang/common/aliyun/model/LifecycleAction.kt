package io.github.caijiang.common.aliyun.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

/**
 * https://help.aliyun.com/zh/auto-scaling/developer-reference/api-describelifecycleactions?spm=a2c4g.11186623.help-menu-25855.d_5_0_0_14_4.30cc143e5QjKMN&scm=20140722.H_202473._.OR_help-T_cn~zh-V_1
 * @author CJ
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class LifecycleAction(
    @JsonProperty("LifecycleHookId")
    val hookId: String,
    @JsonProperty("LifecycleActionToken")
    val token: String,
    @JsonProperty("InstanceIds")
    val instanceIds: LifecycleActionInstances? = null,
)

data class LifecycleActionInstances(
    @JsonProperty("InstanceId")
    val instances: List<String> = emptyList(),
)