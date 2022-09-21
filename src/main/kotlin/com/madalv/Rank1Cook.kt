package com.madalv

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select

open class Rank1Cook(
    val proficiency: Int,
    val name: String,
    val catchPhrase: String,
    private val id: Int,
) {

    var complexity1Channel = Channel<OrderItem>()

    suspend fun work() {
        while (true) {
            receiveOrderItem()
        }
    }

    protected suspend fun cookOrderItem(item: OrderItem) {
        item.cookId = id
        val time: Long = menu[item.foodId - 1].preparationTime * Cfg.timeUnit
        logger.debug { "COOK $id is cooking ITEM ${item.foodId} from ORDER ${item.orderId} TIME $time, CMLPX: ${menu[item.foodId - 1].complexity}" }
        delay(time)
        logger.debug { "COOK $id finished ITEM ${item.foodId} from ORDER ${item.orderId}" }
        if (item.isLastItem.get()) {
            distribChannel.send(item)
        }
    }

    open suspend fun receiveOrderItem() {
        select {
            complexity1Channel.onReceive {
                cookOrderItem(it)
            }
        }
    }

}