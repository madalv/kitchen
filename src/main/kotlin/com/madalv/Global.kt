package com.madalv

import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.coroutines.channels.Channel
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import mu.KotlinLogging
import java.io.File
import java.util.concurrent.BlockingQueue
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.PriorityBlockingQueue

object Cfg {
    val timeUnit: Long = 100
    val host = "localhost"
    val nrOvens = 3
    val nrStoves = 2
    val sharingUnit: Long = timeUnit
}

val client = HttpClient(CIO) {
    install(ContentNegotiation) {
        json(Json {
            prettyPrint = true
            isLenient = true
        })
    }
}
val logger = KotlinLogging.logger {}
val cooksJson: String =
    File("src/main/kotlin/com/madalv/config/cooks.json").inputStream().readBytes().toString(Charsets.UTF_8)
val menuJson: String =
    File("src/main/kotlin/com/madalv/config/menu.json").inputStream().readBytes().toString(Charsets.UTF_8)
val cooks = Json.decodeFromString(ListSerializer(Cook.serializer()), cooksJson)
val menu = Json { coerceInputValues = true }.decodeFromString(ListSerializer(Food.serializer()), menuJson)
val compareByPriority = compareByDescending<DetailedOrder> { it.priority }
var queue: BlockingQueue<DetailedOrder> = PriorityBlockingQueue(100, compareByPriority)
var unfinishedOrders = ConcurrentHashMap<Int, Pair<Int, DetailedOrder>>()
val complexity1Channel = Channel<OrderItem>()
val complexity2Channel = Channel<OrderItem>()
val complexity3Channel = Channel<OrderItem>()
val channelList = listOf(complexity1Channel, complexity2Channel, complexity3Channel)
val distribChannel = Channel<OrderItem>(10)
val ovenChannel = Channel<OrderItem>(5)
val stoveChannel = Channel<OrderItem>(5)