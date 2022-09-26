package com.madalv

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.selects.select
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient


@Serializable
class Cook(
    val rank: Int,
    val proficiency: Int,
    val name: String,
    @SerialName("catch-phrase") val catchPhrase: String,
) {
    @Transient
    var id: Int = 0

    @Transient
    val channels = mutableListOf<Channel<OrderItem>>()

    @Transient
    var distribChannel = Channel<OrderItem>()

    fun initialize(index: Int, channelList: List<Channel<OrderItem>>, distChannel: Channel<OrderItem>) {
        id = rank * 10 + index
        assignChannels(channelList)
        distribChannel = distChannel
    }

    suspend fun work() {
        while (true) {
            receiveOrderItem()
        }
    }

    private fun assignChannels(list: List<Channel<OrderItem>>) {
        when (rank) {
            1 -> channels.addAll(List(list.size) { list[0] })
            2 -> channels.addAll(listOf(list[1], list[0], list[1]))
            3 -> channels.addAll(list)
        }
    }

    private suspend fun receiveOrderItem() {
        select {
            channels[2].onReceive {
                cookOrderItem(it)
            }
            channels[1].onReceive {
                cookOrderItem(it)
            }
            channels[0].onReceive {
                cookOrderItem(it)
            }
        }
    }

    private suspend fun cookOrderItem(item: OrderItem) {
        item.cookId = id
        val time: Long = menu[item.foodId - 1].preparationTime * Cfg.timeUnit
        logger.debug { "COOK $id is cooking ITEM ${item.foodId} from ORDER ${item.orderId} TIME $time, CMLPX: ${menu[item.foodId - 1].complexity}" }
        delay(time)
        logger.debug { "COOK $id finished ITEM ${item.foodId} from ORDER ${item.orderId}" }
        distribChannel.send(item)
    }

}