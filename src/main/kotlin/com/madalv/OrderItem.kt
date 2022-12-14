package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.properties.Delegates

@Serializable
data class OrderItem(
    @SerialName("food_id") val foodId: Int,
    @Transient val orderId: Int = -5
) {
    @Transient
    var timePasssed: Long = 0

    @SerialName("cook_id")
    var cookId = 0

    override fun toString(): String {
        return "$foodId $orderId"
    }
}