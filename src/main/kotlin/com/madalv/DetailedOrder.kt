package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.properties.Delegates

@Serializable
data class DetailedOrder(
    val order: Order,
    @SerialName("cooking_details") var orderItems: MutableList<OrderItem>
) {
    @SerialName("cooking_time")
    val cookingTime by Delegates.notNull<Double>()
    var orderProcessTime by Delegates.notNull<Long>()
}

