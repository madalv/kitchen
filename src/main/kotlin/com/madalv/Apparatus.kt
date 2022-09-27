package com.madalv

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay


class Apparatus(
    val name: String,
    val id: Int,
    val channel: Channel<OrderItem>
) {

    suspend fun receiveItem() {
        for (item in channel) {
            cookItem(item)
        }
    }

    private suspend fun cookItem(item: OrderItem) {
        val time: Long = menu[item.foodId - 1].preparationTime * Cfg.timeUnit
        logger.debug { "COOK ${item.cookId} is using the $name for ITEM ${item.foodId} from ORDER ${item.orderId} TIME $time, CMLPX: ${menu[item.foodId - 1].complexity}!" }
        delay(time)
        logger.debug { "$name $id: ITEM ${item.foodId} from ORDER ${item.orderId} is done, COOK ${item.cookId}!" }
        distribChannel.send(item)
    }
}