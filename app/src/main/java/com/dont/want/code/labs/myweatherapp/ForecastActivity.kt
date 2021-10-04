package com.dont.want.code.labs.myweatherapp

import android.graphics.Color
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.TextView
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.BarGraphSeries
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import org.json.JSONArray
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.Format
import java.text.SimpleDateFormat
import java.util.*


class ForecastActivity : AppCompatActivity() {

    lateinit var graphView:GraphView
    var Lat:Double=0.0
    var Lon:Double=0.0
    var cityName:String = "Nowhere"
    val apiKey:String = "42af006c49cad6fb2ae56af9fd967928"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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

        Lat = this.intent.getDoubleExtra("lat", 0.0)
        Lon = this.intent.getDoubleExtra("lon", 0.0)
        cityName = this.intent.getStringExtra("city_name")!!
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
            val hourly = result.getJSONArray("hourly")


            val daily = result.getJSONArray("daily")
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

}