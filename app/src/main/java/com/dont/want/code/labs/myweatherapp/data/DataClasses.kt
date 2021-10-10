package com.dont.want.code.labs.myweatherapp.data

import java.util.*


data class HourlyDataPoint(val time: Date, val cityName:String, val country:String,
                           val temp:Double, val humidity:Double, val status:String,
                           val wind:Double)

data class DailyDataPoint(val time: Date,
                          val morning:Double, val day:Double, val evening:Double, val night:Double,
                          val morning_fl:Double, val day_fl:Double, val evening_fl:Double, val night_fl:Double,
                          val pressure:Int, val wind:Double, var humidity:Double, val clouds:Double,
                          val status: String, val sunset: Date, val sunrise: Date
)

data class City(val id: Int, val name: String, val country: String, val lon: Int, val lat: Int)
