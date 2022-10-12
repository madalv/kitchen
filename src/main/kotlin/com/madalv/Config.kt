package com.madalv

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
class Config(
    @SerialName("timeunit")
    val timeUnit: Long,
    @SerialName("nr_ovens")
    val nrOvens: Int,
    @SerialName("nr_stoves")
    val nrStoves: Int,
    val dhall: String,
    val port: Int
) {
    val sharingUnit: Long = timeUnit / 2
}