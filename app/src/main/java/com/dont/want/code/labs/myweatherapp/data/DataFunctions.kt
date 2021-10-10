import com.dont.want.code.labs.myweatherapp.data.DailyDataPoint
import com.dont.want.code.labs.myweatherapp.data.HourlyDataPoint
import org.json.JSONArray
import org.json.JSONObject
import java.util.*

object DatasetProvider {
    fun getDailyDataset(daily: JSONArray): ArrayList<DailyDataPoint> {
        val result = ArrayList<DailyDataPoint>()
        for (i in 0 until daily.length()) {
            val element = daily.getJSONObject(i)

            val time = Date(element.getLong("dt") * 1000)

            val temp = element.getJSONObject("temp")
            val mourning = temp.getDouble("morn")
            val day = temp.getDouble("day")
            val evening = temp.getDouble("eve")
            val night = temp.getDouble("night")

            val fl = element.getJSONObject("feels_like")
            val mourning_fl = fl.getDouble("morn")
            val day_fl = fl.getDouble("day")
            val evening_fl = fl.getDouble("eve")
            val night_fl = fl.getDouble("night")

            val pressure = element.getInt("pressure")
            val wind = element.getDouble("wind_speed")
            val humidity = element.getDouble("humidity")
            val clouds = element.getDouble("clouds")
            val status = element.getJSONArray("weather").getJSONObject(0).getString("description")

            val sunset = Date(element.getLong("sunset") * 1000)
            val sunrise = Date(element.getLong("sunrise") * 1000)

            val dp = DailyDataPoint(
                time,
                mourning, day, evening, night,
                mourning_fl, day_fl, evening_fl, night_fl,
                pressure, wind, humidity, clouds, status, sunset, sunrise
            )
            result.add(dp)
        }
        return result
    }

    fun getHourlyDataset(hourly: JSONArray): ArrayList<HourlyDataPoint> {
        val result = ArrayList<HourlyDataPoint>()
        for (i in 0 until hourly.length()) {
            val elem = hourly[i] as JSONObject
            val date = Date(elem.getLong("dt") * 1000)
            val temp = elem.getDouble("temp")
            val humidity = elem.getDouble("humidity")
            val status =
                (elem.getJSONArray("weather").get(0) as JSONObject).getString("description")
            val wind = elem.getDouble("wind_speed")
            val dp = HourlyDataPoint(
                date, temp,
                humidity, status, wind
            )
            result.add(dp)
        }
        return result
    }
}