package com.madalv

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.SynchronousQueue


class Apparatus(
    val name: String,
    val id: Int,
    val channel: Channel<OrderItem>
) {

    private var itemQueue: BlockingQueue<OrderItem> = SynchronousQueue()
    suspend fun receiveItem() {
        for (item in channel) {

            itemQueue.add(item)
            println(itemQueue)
            //cookItem(item)
        }
    }

    suspend fun cookItems() {
        while (true) {
            if (itemQueue.isNotEmpty()) for (item in itemQueue) {
                println("$name $id>> queue $itemQueue")
                if (item.timePasssed < menu[item.foodId - 1].preparationTime * Cfg.timeUnit) {
                    delay(Cfg.timeUnit)
                    item.timePasssed += Cfg.timeUnit
                    println("$name $id>> item ITEM ${item.foodId} from ORDER ${item.orderId} ${item.timePasssed} / ${menu[item.foodId - 1].preparationTime * Cfg.timeUnit}")
                } else {
                    distribChannel.send(item)
                    itemQueue.remove(item)
                }
            }
        }

    }
}


//    private suspend fun cookItem(item: OrderItem) {
////        var timePassed: Long = 0
////        val cookingTime = menu[item.foodId - 1].preparationTime * Cfg.timeUnit
//
//
//        //logger.debug { "COOK ${item.cookId} is using the $name for ITEM ${item.foodId} from ORDER ${item.orderId} TIME $cookingTime, CMLPX: ${menu[item.foodId - 1].complexity}!" }
//
////        while (timePassed < cookingTime) {
////            logger.debug("$name $id>> ITEM ${item.foodId} from ORDER ${item.orderId}  $timePassed / $cookingTime")
////            if (cookingTime - timePassed > Cfg.sharingUnit) {
////                delay(Cfg.sharingUnit)
////                timePassed += Cfg.sharingUnit
////                logger.debug("$name $id>> ITEM ${item.foodId} from ORDER ${item.orderId}  $timePassed / $cookingTime, SWITCHING!!!}")
////                yield()
////            } else {
////                delay(cookingTime - timePassed)
////                timePassed += cookingTime
////                distribChannel.send(item)
////                logger.debug { "$name $id: ITEM ${item.foodId} from ORDER ${item.orderId} is done, COOK ${item.cookId}!" }
////            }
////        }
//
////        logger.debug { "COOK ${item.cookId} is using the $name for ITEM ${item.foodId} from ORDER ${item.orderId} TIME $cookingTime, CMLPX: ${menu[item.foodId - 1].complexity}!" }
////        delay(cookingTime)
////        logger.debug { "$name $id: ITEM ${item.foodId} from ORDER ${item.orderId} is done, COOK ${item.cookId}!" }
////        distribChannel.send(item)
//    }
//}