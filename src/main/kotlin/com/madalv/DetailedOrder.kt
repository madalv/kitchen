package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class DetailedOrder(val order: Order,
                         @SerialName("cooking_details") var orderItems: MutableList<OrderItem>
                        )

