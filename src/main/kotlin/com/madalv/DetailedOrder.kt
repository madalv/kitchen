package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.properties.Delegates

@Serializable
data class DetailedOrder(
    @SerialName("order_id") val id: Int,
    @SerialName("table_id") val tableId: Int,
    val items: List<Int>,
    val priority: Int,
    @SerialName("pick_up_time") val pickupTime: Long,
    @SerialName("max_wait") var maxWait: Double,
    @SerialName("waiter_id") var waiterId: Int = -5,
    @SerialName("cooking_details") var orderItems: MutableList<OrderItem>
) {
    @SerialName("cooking_time")
    var cookingTime: Long = 0

    @Transient
    var orderProcessTime: Long = 0
}

