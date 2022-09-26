package com.madalv

import com.madalv.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*


// TODO cooking apparatuses
//  ------ TODO better priority formula
// TODO write serializer for OrderKitchen

suspend fun processQueue() {
    while (true) {
        if (queue.isEmpty()) continue
        logger.debug { showQueue(queue) }
        val orderKitchen = queue.remove()
        orderKitchen.orderProcessTime = System.currentTimeMillis()
        logger.debug { "Order ${orderKitchen.order.id} is now getting processed..." }

        // add to orders in process, first int of pair is nr of completed items off order
        // once it equals items.size, order can be shipped
        unfinishedOrders[orderKitchen.order.id] = Pair(0, orderKitchen)

        for (item in orderKitchen.orderItems) {
            logger.debug { "Sending item ${item.foodId} from order ${item.orderId} to any available cooks..." }
            when (menu[item.foodId - 1].complexity) {
                1 -> complexity1Channel.send(item)
                2 -> complexity2Channel.send(item)
                3 -> complexity3Channel.send(item)
            }
        }
    }
}

suspend fun receiveItems() {
    for (item in distribChannel) {
        unfinishedOrders.computeIfPresent(item.orderId) { _, pair -> Pair(pair.first + 1, pair.second) }
        val pair = unfinishedOrders.getValue(item.orderId)
        logger.debug { "Order ${pair.second.order.id} ${pair.first} / ${pair.second.orderItems.size}" }
        if (pair.first == pair.second.orderItems.size) {
            sendOrder(pair.second.order)
            logger.debug {
                "Order ${pair.second.order.id} DONE AND SENT TO DINING HALL! Cooking time " +
                        "${System.currentTimeMillis() - pair.second.orderProcessTime}"
            }
        }
    }
}

fun showQueue(q: Queue<DetailedOrder>): String {
    var result = "QUEUE ORDERS: "
    for (o in q) result += "ID ${o.order.id} - P ${o.order.priority} - NRITEMS ${o.orderItems.size}: ${o.order.items} | "
    return result
}

suspend fun sendOrder(order: Order) {
    logger.debug { Json.encodeToJsonElement(order) }
    client.post {
        url {
            protocol = URLProtocol.HTTP
            host = Cfg.host
            path("/distribution")
            port = 8081
        }
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToJsonElement(order))
    }
}

fun main() {
    embeddedServer(Netty, port = 8082, host = "0.0.0.0") {
        configureRouting()
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }


        cooks.forEachIndexed { index, cook ->
            cook.initialize(index, channelList, distribChannel)

            repeat(cook.proficiency) {
                launch {
                    CoroutineName("Cook${cook.id}")
                    cook.work()
                }
            }
        }

        launch {
            processQueue()
        }

        launch {
            receiveItems()
        }

    }.start(wait = true)
}


