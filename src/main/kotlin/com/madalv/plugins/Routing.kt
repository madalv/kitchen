package com.madalv.plugins

import com.madalv.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch


fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Kitchen!")
        }
        post("/order") {
            launch {
                call.respond(OrderResponse(sumProeficieny, queue.size + 1 + unfinishedOrders.size, cfg.nrOvens + cfg.nrStoves))
                println("${queue.size} ${unfinishedOrders.size}")
                val order: Order = call.receive()
                val orderItems = mutableListOf<OrderItem>()
                for (itemId in order.items) {
                    orderItems.add(OrderItem(itemId, order.id))
                }
                println("--- OrderKitchen ${order.id} received! ---")
                queue.add(
                    DetailedOrder(
                        order.id,
                        order.tableId,
                        order.items,
                        order.priority,
                        order.pickupTime,
                        order.maxWait,
                        order.waiterId,
                        orderItems
                    )
                )
                println("--- OrderKitchen ${order.id} added to queue! ---")
            }
        }
    }
}
