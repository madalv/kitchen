package com.madalv

import com.madalv.plugins.configureRouting
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.newSingleThreadContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.util.*


suspend fun processQueue() {
    while (true) {
        if (queue.isEmpty() || queue.size < cfg.queueLimit) continue
        logger.debug(showQueue(queue))
        val orderKitchen = queue.remove()
        orderKitchen.orderProcessTime = System.currentTimeMillis()
        logger.debug("Order ${orderKitchen.id} is now getting processed...")

        // add to orders in process, first int of pair is nr of completed items off order
        // once it equals items.size, order can be shipped
        unfinishedOrders[orderKitchen.id] = Pair(0, orderKitchen)

        for (item in orderKitchen.orderItems) {
            logger.debug("Sending item ${item.foodId} from order ${item.orderId} to any available cooks...")
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
        try {
            unfinishedOrders.computeIfPresent(item.orderId) { _, pair -> Pair(pair.first + 1, pair.second) }
            val pair = unfinishedOrders.getValue(item.orderId)
            logger.debug("Order ${pair.second.id} ${pair.first} / ${pair.second.orderItems.size}")
            if (pair.first == pair.second.orderItems.size) {
                pair.second.cookingTime = System.currentTimeMillis() - pair.second.orderProcessTime
                sendOrder(pair.second)
                logger.debug(
                    "Order ${pair.second.id} DONE AND SENT TO DINING HALL! Cooking time " +
                            "${pair.second.cookingTime}"
                )
                unfinishedOrders.remove(item.orderId)
            }
        } catch (e: Exception) {
            logger.debug { e }
        }
    }
}

fun showQueue(q: Queue<DetailedOrder>): String {
    var result = "QUEUE (${q.size}) ORDERS: "
    for (o in q) result += "ID ${o.id} - P ${o.priority} - NRITEMS ${o.orderItems.size}: ${o.items} | "
    return result
}

suspend fun sendOrder(order: DetailedOrder) {
    logger.debug(Json.encodeToJsonElement(order).toString())
    println(cfg.dhall)
    client.post("http://${cfg.dhall}/distribution") {
        contentType(ContentType.Application.Json)
        setBody(Json.encodeToJsonElement(order))
    }
}

@OptIn(DelicateCoroutinesApi::class)
fun main() {
    embeddedServer(Netty, port = cfg.port, host = "0.0.0.0") {
        configureRouting()
        install(ContentNegotiation) {
            json(Json {
                prettyPrint = true
                isLenient = true
            })
        }

        println(menu)
        val apparatusCtx = newSingleThreadContext("ApparatusThread")

        for (i in 0 until cfg.nrStoves) {
            val app = Apparatus("stove", i, stoveChannel)
            launch(apparatusCtx) { app.receiveItem() }
        }

        for (i in 0 until cfg.nrOvens) {
            val app = Apparatus("oven", i, ovenChannel)
            launch(apparatusCtx) { app.receiveItem() }
        }


        cooks.forEachIndexed { index, cook ->
            cook.initialize(index, channelList, distribChannel)
            repeat(cook.proficiency) {
                launch { cook.work() }
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


