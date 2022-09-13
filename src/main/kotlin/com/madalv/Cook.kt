package com.madalv

import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.server.util.*
import kotlinx.coroutines.*
import java.util.concurrent.ThreadLocalRandom

class Cook(val rank: Int,
           val proficieny: Int,
           val name: String,
           val catchPhrase: String,
           val id: Int
) {
    suspend fun executeOrder(order: Order) {
        logger.debug { "GOT order ${order.id} from DINING HALL" }
        cookOrder(order)
        sendOrder(order)
        logger.debug { "SENT order ${order.id} to DINING HALL" }
    }

    private suspend fun cookOrder(order: Order) {
        val r = ThreadLocalRandom.current()
        val time: Long = r.nextLong(10, 61) * cfg.timeUnit
        logger.debug { "COOK $id is cooking order ${order.id}: TIME $time" }

        delay(time) // TODO: change to actual time
        logger.debug { "Order ${order.id} is DONE" }
    }

    private suspend fun sendOrder(order: Order) {
        client.post {
            url {
                protocol = URLProtocol.HTTP
                host = cfg.host
                path("/distribution")
                port = 8081
            }
            contentType(ContentType.Application.Json)
            setBody(order)
        }
    }
}