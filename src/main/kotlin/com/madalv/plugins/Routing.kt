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
                val order: Order = call.receive()
                val orderItems = mutableListOf<OrderItem>()
                for (itemId in order.items) {
                    orderItems.add(OrderItem(itemId, order.id))
                }
                logger.debug { "--- OrderKitchen ${order.id} received and added to queue! ---" }
                queue.add(DetailedOrder(order, orderItems))
            }
        }
    }
}
