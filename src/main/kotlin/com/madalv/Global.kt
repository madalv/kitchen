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

val configJson: String =
    File("config/config.json").inputStream().readBytes().toString(Charsets.UTF_8)


val cfg: Config = Json.decodeFromString(Config.serializer(), configJson)

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
    File("config/cooks.json").inputStream().readBytes().toString(Charsets.UTF_8)
val menuJson: String =
    File("config/menu.json").inputStream().readBytes().toString(Charsets.UTF_8)
val cooks = Json.decodeFromString(ListSerializer(Cook.serializer()), cooksJson)
val menu = Json { coerceInputValues = true }.decodeFromString(ListSerializer(Food.serializer()), menuJson)


val sumProeficieny = getSumProeficiency()

val compareByPriority = compareByDescending<DetailedOrder> { it.priority }
var queue: BlockingQueue<DetailedOrder> = PriorityBlockingQueue(15, compareByPriority)
var unfinishedOrders = ConcurrentHashMap<Int, Pair<Int, DetailedOrder>>()
val complexity1Channel = Channel<OrderItem>(Channel.UNLIMITED)
val complexity2Channel = Channel<OrderItem>(Channel.UNLIMITED)
val complexity3Channel = Channel<OrderItem>(Channel.UNLIMITED)
val channelList = listOf(complexity1Channel, complexity2Channel, complexity3Channel)
val distribChannel = Channel<OrderItem>()
val ovenChannel = Channel<OrderItem>(Channel.UNLIMITED)
val stoveChannel = Channel<OrderItem>(Channel.UNLIMITED)

fun getSumProeficiency(): Int {
    var p = 0
    for (cook in cooks) p += cook.proficiency
    return p
}