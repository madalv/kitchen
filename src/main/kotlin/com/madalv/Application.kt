package com.madalv

import io.ktor.server.engine.*
import io.ktor.server.netty.*
import com.madalv.plugins.*
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import mu.KotlinLogging
import java.util.Queue
import java.util.concurrent.BlockingQueue
import java.util.concurrent.PriorityBlockingQueue

val client = HttpClient(CIO) {
    install(io.ktor.client.plugins.contentnegotiation.ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}
val logger = KotlinLogging.logger {}
val compareByPriority = compareByDescending<DetailedOrder> { it.order.priority }
var queue: BlockingQueue<DetailedOrder> = PriorityBlockingQueue(100, compareByPriority)
val complexity1Channel = Channel<OrderItem>(Channel.UNLIMITED)
val complexity2Channel = Channel<OrderItem>(Channel.UNLIMITED)
val complexity3Channel = Channel<OrderItem>(Channel.UNLIMITED)
val distribChannel = Channel<OrderItem>()

// TODO notifying procedure
// TODO cooking apparatuses
// TODO better priority formula
// TODO write serializer for OrderKitchen

suspend fun processQueue() {
    while (true) {
        if (queue.isEmpty()) continue
        logger.debug { showQueue(queue) }
        val orderKitchen = queue.remove()
        logger.debug { "Order ${orderKitchen.order.id} is now getting processed..." }
        //CoroutineScope(EmptyCoroutineContext).launch {
            CoroutineName("order${orderKitchen.order.id}") // TODO: make
            for (item in orderKitchen.orderItems) {
                logger.debug { "Sending item ${item.foodId} from order ${item.orderId} to any available cooks..." }
                when (menu[item.foodId - 1].complexity) {
                    1 -> complexity1Channel.send(item)
                    2 -> complexity2Channel.send(item)
                    3 -> complexity3Channel.send(item)
                }
            }
            distribChannel.receive()
            logger.debug { "Order ${orderKitchen.order.id} is now DONEEEEEE!!!!!!!" }
            sendOrder(orderKitchen.order)
        //}
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

        for (i in 0 until Cfg.nrRank1Cooks) {
            val cook = Rank1Cook(2, "John1", "WTF?", i + 10)
            cook.complexity1Channel = complexity1Channel
            repeat(cook.proficiency) {
                launch {
                    CoroutineName("Cook$cook.id")
                    cook.work()
                }
            }
        }

        for (i in 0 until Cfg.nrRank2Cooks) {
            val cook = Rank2Cook(3, "John2", "WTF?", i + 20)
            cook.complexity1Channel = complexity1Channel
            cook.complexity2Channel = complexity2Channel
            repeat(cook.proficiency) {
                launch {
                    CoroutineName("Cook$cook.id")
                    cook.work()
                }
            }
        }

        for (i in 0 until Cfg.nrRank3Cooks) {
            val cook = Rank3Cook(2, "John3", "WTF?", i + 30)
            cook.complexity1Channel = complexity1Channel
            cook.complexity2Channel = complexity2Channel
            cook.complexity3Channel = complexity3Channel
            repeat(cook.proficiency) {
                launch {
                    CoroutineName("Cook$cook.id")
                    cook.work()
                }
            }
        }

        launch {
            processQueue()
        }

    }.start(wait = true)
}


