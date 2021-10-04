package com.dont.want.code.labs.myweatherapp

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
    lateinit var hourlyRecyclerView:RecyclerView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        // lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // make activity fullscreen TODO deprecated, find newer method
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_current)

        supportActionBar?.hide()


        setContentView(R.layout.activity_forecast)



        graphView = findViewById<GraphView>(R.id.graph)

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

        hourlyRecyclerView = findViewById<RecyclerView>(R.id.hourly_recycler_view)
        hourlyRecyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        Lat = this.intent.getDoubleExtra("lat", 0.0)
        Lon = this.intent.getDoubleExtra("lon", 0.0)
        cityName = this.intent.getStringExtra("city_name")!!
        country = this.intent.getStringExtra("country_name")!!
        forecastLoader().execute()
    }

    // TODO deprecated, find another method
    inner class forecastLoader : AsyncTask<String, Void, String>(){

        override fun onPreExecute() {
            super.onPreExecute()
        }

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

            val locationTextView = findViewById<TextView>(R.id.city_name)
            locationTextView.text = cityName

            val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)
            val updateTime = Date(1000 * current.getLong("dt"))
            val updatedAtString = "Updated at " + formatter.format(updateTime)
            findViewById<TextView>(R.id.updated_at).text = updatedAtString

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
                hourlyRecyclerView.adapter = HourlyForecastAdapter(hourlyDataSet)
                hourlyRecyclerView.setHasFixedSize(true)
            }
            catch (e:Exception){
                Log.println(Log.DEBUG, "HOUR_ERR", e.localizedMessage)

            }

            val daily = result.getJSONArray("daily")
        }

        fun getHourlyDataset(hourly: JSONArray): ArrayList<HourlyDataPoint> {
            var result = ArrayList<HourlyDataPoint>()
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

        fun plot_graph(minutely:JSONArray){

            var data = LineGraphSeries<DataPoint>()
            data.isDrawBackground = true

            for (i in 0 until minutely.length()){
                val jsonData:JSONObject = minutely[i] as JSONObject
                val dataPoint = DataPoint(Date(jsonData.getLong("dt")*1000),jsonData.getDouble("precipitation"))
                data.appendData(dataPoint, false,100,true)
            }
            graphView.title = "Rainfall, mm"
            // 20 sp, same as other forecast names, casted to px with density
            graphView.titleTextSize = (20.0F*getResources().getDisplayMetrics().density) as Float
            graphView.titleColor = Color.parseColor("#EEEEEE")
            graphView.addSeries(data)

            if (data.lowestValueY==data.highestValueY || data.highestValueY==0.0){
                throw Exception("No sense in showing this data")
            }
            graphView.viewport.setMinY(data.lowestValueY)
            graphView.viewport.setMaxY(data.highestValueY+0.01)
            graphView.getViewport().setYAxisBoundsManual(true);

        }
    }


    class HourlyForecastAdapter(private val dataSet: ArrayList<HourlyDataPoint>) :
        RecyclerView.Adapter<HourlyForecastAdapter.ViewHolder>() {

        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {}

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
            viewHolder.itemView.findViewById<TextView>(R.id.temp).text = temp.toString() + "°C"

            val humidity = dataPoint.humidity
            viewHolder.itemView.findViewById<TextView>(R.id.humidity).text = humidity.toString() + " %"

            val status = dataPoint.status
            viewHolder.itemView.findViewById<TextView>(R.id.status).text = status

            val wind = dataPoint.wind
            viewHolder.itemView.findViewById<TextView>(R.id.wind).text = wind.toString()+" m/s"



        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }

    data class HourlyDataPoint(val time:Date, val cityName:String, val country:String, val temp:Double, val humidity:Double, val status:String,val wind:Double)


}