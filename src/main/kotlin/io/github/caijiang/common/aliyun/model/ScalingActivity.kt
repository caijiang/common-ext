package io.github.caijiang.common.aliyun.model

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import java.time.ZonedDateTime

/**
 * @author CJ
 */
@JsonIgnoreProperties(ignoreUnknown = true)
data class ScalingActivity(
    @JsonProperty("ScalingGroupId")
    val groupId: String,
    @JsonProperty("ScalingActivityId")
    val id: String,
//    这个数据 没有比有好
//    val progress: Double,
    @JsonProperty("Description")
    val description: String,
    @JsonProperty("StartTime")
    val startTime: ZonedDateTime,
    @JsonProperty("StartedInstances")
    val startedInstances: ScalingActivityStartedInstance?,
    @JsonProperty("CreatedInstances")
    val createdInstances: ScalingActivityCreatedInstance?,
    @JsonProperty("DestroyedInstances")
    val destroyedInstances: ScalingActivityDestroyedInstance?,
)

data class ScalingActivityStartedInstance(
    @JsonProperty("StartedInstance")
    val instances: List<String> = emptyList(),
)

data class ScalingActivityCreatedInstance(
    @JsonProperty("CreatedInstance")
    val instances: List<String> = emptyList(),
)

data class ScalingActivityDestroyedInstance(
    @JsonProperty("DestroyedInstance")
    val instances: List<String> = emptyList(),
)
