package com.dont.want.code.labs.myweatherapp.activities

import android.content.pm.ActivityInfo
import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.dont.want.code.labs.myweatherapp.R
import com.dont.want.code.labs.myweatherapp.data.DailyDataPoint
import com.dont.want.code.labs.myweatherapp.data.HourlyDataPoint
import com.dont.want.code.labs.myweatherapp.databinding.ActivityCurrentBinding
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class ForecastActivity : AppCompatActivity() {

    lateinit var graphView:GraphView
    var Lat:Double=0.0
    var Lon:Double=0.0
    var cityName:String = "Nowhere"
    var country:String = "Nowhere"
    val apiKey:String = "42af006c49cad6fb2ae56af9fd967928"
    lateinit var hourlyForecastRecyclerView:RecyclerView
    lateinit var dailyForecastRecyclerView:RecyclerView

    private var _binding: ActivityCurrentBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        _binding = ActivityCurrentBinding.inflate(layoutInflater)


        // lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // make activity fullscreen TODO deprecated, find newer method
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(binding.root)

        supportActionBar?.hide()





        graphView = findViewById(R.id.graph)

        // set time-style labels on X axis(instead of unix long)
        graphView.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                if (isValueX) {
                    val formatter: Format = SimpleDateFormat("HH:mm")
                    return formatter.format(value)
                }
                return super.formatLabel(value, isValueX)
            }
        }

        hourlyForecastRecyclerView = findViewById(R.id.hourly_recycler_view)
        hourlyForecastRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        dailyForecastRecyclerView = findViewById(R.id.daily_recycler_view)
        dailyForecastRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)



        Lat = this.intent.getDoubleExtra("lat", 0.0)
        Lon = this.intent.getDoubleExtra("lon", 0.0)
        cityName = this.intent.getStringExtra("city_name")!!
        country = this.intent.getStringExtra("country_name")!!
        forecastDataLoader().execute()
    }

    // TODO deprecated, find another method
    inner class forecastDataLoader : AsyncTask<String, Void, String>(){

        override fun doInBackground(vararg p0: String?): String {
            var response:String?
            try {
                response = URL("https://api.openweathermap.org/data/2.5/onecall?lat=$Lat&lon=$Lon&units=metric&appid=$apiKey")
                    .readText(Charsets.UTF_8)
                Log.println(Log.WARN,"STR", response)
            }
            catch (e: Exception){
                response=null
                Log.println(Log.ERROR, "ASYNC_LOAD", e.localizedMessage)
            }
            return response!!
        }

        override fun onPostExecute(result: String?) {
            super.onPostExecute(result)
            val result = JSONObject(result!!)
            val current = result.getJSONObject("current")

            val locationTextView = binding.cityName
            locationTextView.text = cityName

            val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val updateTime = Date(1000 * current.getLong("dt"))
            val updatedAtString = "Updated at " + formatter.format(updateTime)
           binding.updatedAt.text = updatedAtString

            // show minute rain forecast
            try {
                val minutely = result.getJSONArray("minutely")
                plot_graph(minutely)
            }
            //catch exception if no data or all rainfalls = 0 or any else
            catch (e:Exception){
                Log.println(Log.DEBUG, "MIN_ERROR", e.localizedMessage)
                graphView.visibility= View.GONE
            }

            // show hourly forecast
            try {
                val hourly = result.getJSONArray("hourly")
                val hourlyDataSet = getHourlyDataset(hourly)
                hourlyForecastRecyclerView.adapter = HourlyForecastAdapter(hourlyDataSet)
                hourlyForecastRecyclerView.setHasFixedSize(true)
            }
            catch (e:Exception){
                Log.println(Log.DEBUG, "HOUR_ERR", e.localizedMessage)

            }

            val dailyJSON = result.getJSONArray("daily")
            val dailyDataset = detDailyDataset(dailyJSON)
            dailyForecastRecyclerView.adapter = DailyForecastAdapter(dailyDataset)
            hourlyForecastRecyclerView.setHasFixedSize(true)
        }



        private fun detDailyDataset(daily:JSONArray):ArrayList<DailyDataPoint>{
            val result = ArrayList<DailyDataPoint>()
            for(i in 0 until daily.length()){
                val element = daily.getJSONObject(i)

                val time = Date(element.getLong("dt")*1000)

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

                val sunset = Date(element.getLong("sunset")*1000)
                val sunrise = Date(element.getLong("sunrise")*1000)

                val dp = DailyDataPoint(time,
                    mourning, day, evening, night,
                    mourning_fl, day_fl, evening_fl, night_fl,
                    pressure, wind, humidity, clouds, status, sunset, sunrise
                )
                result.add(dp)
            }
            return result
        }

        private fun getHourlyDataset(hourly: JSONArray): ArrayList<HourlyDataPoint> {
            val result = ArrayList<HourlyDataPoint>()
            for (i in 0 until hourly.length()){
                val elem = hourly[i] as JSONObject
                val date = Date(elem.getLong("dt")*1000)
                val temp = elem.getDouble("temp")
                val humidity = elem.getDouble("humidity")
                val status = (elem.getJSONArray("weather").get(0) as JSONObject).getString("description")
                val wind = elem.getDouble("wind_speed")
                val dp = HourlyDataPoint(date, cityName, country, temp,
                    humidity, status, wind)
                result.add(dp)
            }
            return result
        }

        private fun plot_graph(minutely:JSONArray){

            val data = LineGraphSeries<DataPoint>()
            data.isDrawBackground = true

            for (i in 0 until minutely.length()){
                val jsonData:JSONObject = minutely[i] as JSONObject
                val dataPoint = DataPoint(Date(jsonData.getLong("dt")*1000),jsonData.getDouble("precipitation"))
                data.appendData(dataPoint, false,100,true)
            }
            graphView.title = "Rainfall, mm"
            // 20 sp, same as other forecast names, casted to px with density
            graphView.titleTextSize = (20.0F* resources.displayMetrics.density)
            graphView.titleColor = Color.parseColor("#EEEEEE")
            graphView.addSeries(data)

            if (data.lowestValueY==data.highestValueY || data.highestValueY==0.0){
                throw Exception("No sense in showing this data")
            }
            graphView.viewport.setMinY(data.lowestValueY)
            graphView.viewport.setMaxY(data.highestValueY+0.01)
            graphView.viewport.isYAxisBoundsManual = true

        }
    }


    class HourlyForecastAdapter(private val dataSet: ArrayList<HourlyDataPoint>) :
        RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.forecast_item, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {
            val dataPoint = dataSet[position]

            val dt = dataPoint.time
            val formatter = SimpleDateFormat("HH:mm dd MMM", Locale.ENGLISH)
            val dtString = formatter.format(dt)
            viewHolder.itemView.findViewById<TextView>(R.id.time).text = dtString

            val temp = dataPoint.temp
            viewHolder.itemView.findViewById<TextView>(R.id.temp).text = "$temp°C"

            val humidity = dataPoint.humidity
            viewHolder.itemView.findViewById<TextView>(R.id.humidity).text = "$humidity %"

            val status = dataPoint.status
            viewHolder.itemView.findViewById<TextView>(R.id.status).text = status

            val wind = dataPoint.wind
            viewHolder.itemView.findViewById<TextView>(R.id.wind).text = "$wind m/s"

        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }

    class DailyForecastAdapter(private val dailyDataset: ArrayList<DailyDataPoint>) : RecyclerView.Adapter<DailyForecastAdapter.ViewHolder>() {
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view)

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.dayly_forecast_item, viewGroup, false)

            return ViewHolder(view)
        }


        override fun onBindViewHolder(holder: ViewHolder, position: Int) {

            val v = holder.itemView
            val dateFormatter = SimpleDateFormat("dd MMM", Locale.ENGLISH)
            val timeFormatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)


            val dp = dailyDataset[position]
            val time = dp.time
            v.findViewById<TextView>(R.id.time).text = dateFormatter.format(time)

            val morning = dp.morning
            v.findViewById<TextView>(R.id.morning).text = "$morning °C"
            val day = dp.day
            v.findViewById<TextView>(R.id.day).text = "$day°C"
            val evening = dp.evening
            v.findViewById<TextView>(R.id.evening).text = "$evening°C"
            val night = dp.night
            v.findViewById<TextView>(R.id.night).text = "$night°C"

            val morning_fl = dp.morning_fl
            v.findViewById<TextView>(R.id.morning_sl).text = "$morning_fl°C"
            val day_fl = dp.day_fl
            v.findViewById<TextView>(R.id.day_sl).text = "$day_fl°C"
            val evening_fl = dp.evening_fl
            v.findViewById<TextView>(R.id.evening_sl).text = "$evening_fl°C"
            val night_fl = dp.night_fl
            v.findViewById<TextView>(R.id.night_sl).text = "$night_fl°C"

            val pressure = dp.pressure
            v.findViewById<TextView>(R.id.pressure).text = "$pressure gPa"
            val wind = dp.wind
            v.findViewById<TextView>(R.id.wind).text = "$wind m/s"
            val humidity = dp.humidity
            v.findViewById<TextView>(R.id.humidity).text = "$humidity %"
            val clouds = dp.clouds
            v.findViewById<TextView>(R.id.clouds).text = "$clouds %"
            val status = dp.status
            v.findViewById<TextView>(R.id.status).text = status

            val sunset = dp.sunset
            v.findViewById<TextView>(R.id.sunset).text = timeFormatter.format(sunset)
            val sunrise = dp.sunrise
            v.findViewById<TextView>(R.id.sunrise).text = timeFormatter.format(sunrise)


        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dailyDataset.size

    }


}