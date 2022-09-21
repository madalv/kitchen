package com.madalv

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select

open class Rank2Cook(
    proficiency: Int,
    name: String,
    catchPhrase: String,
    id: Int,
) : Rank1Cook (proficiency, name, catchPhrase, id) {
    var complexity2Channel = Channel<OrderItem>()


    override suspend fun receiveOrderItem() {
        select {
            complexity2Channel.onReceive {
                cookOrderItem(it)
            }
            complexity1Channel.onReceive {
                cookOrderItem(it)
            }
        }
    }
}
