package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Order(
    val items: List<Int>,
    val priority: Int,
    @SerialName("max_wait") var maxWait: Double,
    @SerialName("created_time") var createdTime: Long,
    @SerialName("order_id") var id: Int,
    @SerialName("table_id") var tableId: Int,
    @SerialName("pick_up_time") var pickupTime: Long,
    @SerialName("waiter_id") var waiterId: Int
)