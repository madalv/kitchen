package com.madalv.plugins

import com.madalv.Order
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Kitchen!")
        }
        post("/order") {
            val order: Order = call.receive()
            println(order)
        }
    }
}
