package com.dont.want.code.labs.myweatherapp.data.models

import com.google.gson.annotations.SerializedName
import kotlinx.serialization.Serializable
import java.util.*
import kotlin.collections.HashMap


data class HourlyDataPoint(val time: Date,
                           val temp:Double, val humidity:Double, val status:String,
                           val wind:Double)

data class DailyDataPoint(val time: Date,
                          val morning:Double, val day:Double, val evening:Double, val night:Double,
                          val morning_fl:Double, val day_fl:Double, val evening_fl:Double, val night_fl:Double,
                          val pressure:Int, val wind:Double, var humidity:Double, val clouds:Double,
                          val status: String, val sunset: Date, val sunrise: Date
)


@Serializable
data class City(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("country")
    val country: String)

