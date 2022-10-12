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
        val cookingTime = menu[item.foodId - 1].preparationTime * cfg.timeUnit

//        if (cookingTime <= 20 * cfg.timeUnit) {
//            delay(cookingTime)
//            distribChannel.send(item)
//        } else

        if (item.timePasssed < cookingTime) {
            delay(cfg.sharingUnit)
            item.timePasssed += cfg.sharingUnit
            //logger.debug("$name $id>> ITEM ${item.foodId} from ORDER ${item.orderId} COOK ${item.cookId} ${item.timePasssed} / ${cookingTime}, SWITCHING!!!")
            when (menu[item.foodId - 1].complexity) {
                1 -> complexity1Channel.send(item)
                2 -> complexity2Channel.send(item)
                3 -> complexity3Channel.send(item)
            }
        } else {
            distribChannel.send(item)
        }
    }
}