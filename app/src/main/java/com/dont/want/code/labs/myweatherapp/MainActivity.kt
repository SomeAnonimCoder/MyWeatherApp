package com.dont.want.code.labs.myweatherapp

import android.annotation.SuppressLint
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {

    var cityID:Int=0
    val apiKey:String = "42af006c49cad6fb2ae56af9fd967928"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // make activity fullscreen TODO deprecated, find newer method
        window.setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_main)

        supportActionBar?.hide()


        cityID = this.intent.getIntExtra("city_id", 0)
        weatherTask().execute()
    }

    // TODO deprecated, find another method
    inner class weatherTask :AsyncTask<String, Void, String>(){
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility= View.VISIBLE
            findViewById<RelativeLayout>(R.id.main_container).visibility=View.GONE
            findViewById<TextView>(R.id.error_text).visibility = View.GONE
        }

        override fun doInBackground(vararg p0: String?): String? {
            var response:String?
            try {
                response = URL("https://api.openweathermap.org/data/2.5/weather?id=$cityID&units=metric&appid=$apiKey")
                    .readText(Charsets.UTF_8)
                Log.println(Log.WARN,"STR", response)
            }
            catch (e:Exception){
                response=null
            }
            return response
            }

        @SuppressLint("CutPasteId")
        override fun onPostExecute(result: String?) {
            try {
                super.onPostExecute(result)
                val json = JSONObject(result!!)
                val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)

                val mainData = json.getJSONObject("main")
                val sysData = json.getJSONObject("sys")
                val windData = json.getJSONObject("wind")
                val weatherData = json.getJSONArray("weather").getJSONObject(0)

                val country = sysData.getString("country")
                val town = json.getString("name")
                val adressString = country.toString() + " : " + town.toString()
                findViewById<TextView>(R.id.town).text = adressString

                val updateTime = Date(1000 * json.getLong("dt"))
                val updatedAtString = "Updated at " + formatter.format(updateTime)
                findViewById<TextView>(R.id.updated_at).text = updatedAtString

                val tempString = mainData.getString("temp") + "°C"
                findViewById<TextView>(R.id.temp).text = tempString

                val feelsLike = mainData.getDouble("feels_like")
                val feelsLikeString = "Feels like " + feelsLike.toString() + "°C"
                findViewById<TextView>(R.id.feels_like).text = feelsLikeString



                val windSpeed = windData.getDouble("speed")
                val windSpeedString = windSpeed.toString() + " m/s"
                findViewById<TextView>(R.id.wind).text = windSpeedString

                val pressure = mainData.getInt("pressure")
                val pressureString = pressure.toString() + " gPa"
                findViewById<TextView>(R.id.pressure).text = pressureString

                val humidity = mainData.getInt("humidity")
                val humidityString = humidity.toString() + " %"
                findViewById<TextView>(R.id.humidity).text = humidityString

                val sunrise = sysData.getLong("sunrise")
                val sunrizeString = formatter.format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.sunrise).text = sunrizeString

                val sunset = sysData.getLong("sunset")
                val sunsetString = formatter.format(Date(1000 * sunset))
                findViewById<TextView>(R.id.sunset).text = sunsetString

                //not used for now
                //val weatherDescription = weatherData.getString("description")

                val weatherStatus = weatherData.getString("main").uppercase() + "\n"+ weatherData.getString("description")
                findViewById<TextView>(R.id.status).text = weatherStatus

                val minTemp = mainData.getDouble("temp_min")
                val minTempString = "Min temp " + minTemp.toString() + "°C"
                findViewById<TextView>(R.id.temp_min).text = minTempString

                val maxTemp = mainData.getDouble("temp_max")
                val maxTempString = "Min temp " + maxTemp.toString() + "°C"
                findViewById<TextView>(R.id.temp_max).text = maxTempString


                findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
                findViewById<RelativeLayout>(R.id.main_container).visibility = View.VISIBLE
                findViewById<TextView>(R.id.error_text).visibility = View.GONE
            }
            catch (e:Exception){
                findViewById<TextView>(R.id.error_text).visibility = View.GONE
                findViewById<TextView>(R.id.error_text).text = e.localizedMessage
            }
        }
    }
}