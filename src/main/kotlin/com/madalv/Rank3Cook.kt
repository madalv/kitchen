package com.madalv

import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.selects.select

class Rank3Cook(
    proficiency: Int,
    name: String,
    catchPhrase: String,
    id: Int,
) : Rank2Cook (proficiency, name, catchPhrase, id) {
    var complexity3Channel = Channel<OrderItem>()

    override suspend fun receiveOrderItem() {
        select {
            complexity3Channel.onReceive {
                cookOrderItem(it)
            }
            complexity2Channel.onReceive {
                cookOrderItem(it)
            }
            complexity1Channel.onReceive {
                cookOrderItem(it)
            }
        }
    }
}
