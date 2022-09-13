package com.madalv.plugins

import com.madalv.Cook
import com.madalv.Order
import com.madalv.cfg
import com.madalv.logger
import io.ktor.server.routing.*
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import kotlinx.coroutines.launch
import java.util.concurrent.ThreadLocalRandom

fun Application.configureRouting() {

    routing {
        get("/") {
            call.respondText("Hello Kitchen!")
        }
        post("/order") {
            launch {
                val r = ThreadLocalRandom.current()
                val order: Order = call.receive()
                val cook = Cook(1,
                    1,
                    "Anders Erickson",
                    "Do you know how to make 5 margaritas?",
                    r.nextInt(1, cfg.nrCooks + 1))

                cook.executeOrder(order)
            }
        }
    }
}
