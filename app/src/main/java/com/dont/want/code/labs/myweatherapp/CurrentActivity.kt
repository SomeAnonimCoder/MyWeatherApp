package com.dont.want.code.labs.myweatherapp

import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import org.json.JSONObject
import java.net.URL
import java.text.SimpleDateFormat
import java.util.*

class CurrentActivity : AppCompatActivity() {

    private var cityID: Int = 0
    private val apiKey: String = "42af006c49cad6fb2ae56af9fd967928"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // make activity fullscreen TODO deprecated, find newer method
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        setContentView(R.layout.activity_current)

        //remove ActionBar
        supportActionBar?.hide()


        cityID = this.intent.getIntExtra("city_id", 0)
        WeatherLoader().execute()
    }

    // TODO deprecated, find another method
    // Weather data is loaded in background thread asynchronously

    private inner class WeatherLoader : AsyncTask<String, Void, String>() {

        // before loading data make main content gone and make spinner visible
        override fun onPreExecute() {
            super.onPreExecute()
            findViewById<ProgressBar>(R.id.loader).visibility = View.VISIBLE
            findViewById<RelativeLayout>(R.id.main_container).visibility = View.GONE
            findViewById<TextView>(R.id.error_text).visibility = View.GONE
        }

        // load data
        override fun doInBackground(vararg p0: String?): String? {
            var response: String?
            try {
                response =
                    URL("https://api.openweathermap.org/data/2.5/weather?id=$cityID&units=metric&appid=$apiKey")
                        .readText(Charsets.UTF_8)
                Log.println(Log.WARN, "STR", response)
            } catch (e: Exception) {
                response = null
            }
            return response
        }

        // parse json response and display data
        @SuppressLint("CutPasteId")
        override fun onPostExecute(result: String?) = try {
            super.onPostExecute(result)
            // api response
            val json = JSONObject(result!!)
            // reusable datetime formatter
            val formatter = SimpleDateFormat("HH:mm", Locale.ENGLISH)

            //API resonse parsing
            run {
                val mainData = json.getJSONObject("main")
                val sysData = json.getJSONObject("sys")
                val windData = json.getJSONObject("wind")
                val weatherData = json.getJSONArray("weather").getJSONObject(0)

                val country = sysData.getString("country")
                val cityName = json.getString("name")
                val addressString = "$country : $cityName"
                findViewById<TextView>(R.id.city_name).text = addressString

                val updateTime = Date(1000 * json.getLong("dt"))
                val updatedAtString = "Updated at " + formatter.format(updateTime)
                findViewById<TextView>(R.id.updated_at).text = updatedAtString

                val tempString = "${mainData.getString("temp")}°C"
                findViewById<TextView>(R.id.temp).text = tempString

                val feelsLike = mainData.getDouble("feels_like")
                val feelsLikeString = "Feels like $feelsLike°C"
                findViewById<TextView>(R.id.feels_like).text = feelsLikeString

                val windSpeed = windData.getDouble("speed")
                val windSpeedString = "$windSpeed m/s"
                findViewById<TextView>(R.id.wind).text = windSpeedString

                val pressure = mainData.getInt("pressure")
                val pressureString = "$pressure gPa"
                findViewById<TextView>(R.id.pressure).text = pressureString

                val humidity = mainData.getInt("humidity")
                val humidityString = "$humidity %"
                findViewById<TextView>(R.id.humidity).text = humidityString

                val sunrise = sysData.getLong("sunrise")
                val sunrizeString = formatter.format(Date(sunrise * 1000))
                findViewById<TextView>(R.id.sunrise).text = sunrizeString

                val sunset = sysData.getLong("sunset")
                val sunsetString = formatter.format(Date(1000 * sunset))
                findViewById<TextView>(R.id.sunset).text = sunsetString

                //not used for now
                //val weatherDescription = weatherData.getString("description")

                val weatherStatus = """
                ${
                    weatherData.getString("main").uppercase()
                }\n${weatherData.getString("description")}
                """.trimIndent()
                findViewById<TextView>(R.id.status).text = weatherStatus

                val minTemp = mainData.getDouble("temp_min")
                val minTempString = "Min temp $minTemp°C"
                findViewById<TextView>(R.id.temp_min).text = minTempString

                val maxTemp = mainData.getDouble("temp_max")
                val maxTempString = "Min temp $maxTemp°C"
                findViewById<TextView>(R.id.temp_max).text = maxTempString
            }

            // make main content visible and make spinner gone
            findViewById<ProgressBar>(R.id.loader).visibility = View.GONE
            findViewById<RelativeLayout>(R.id.main_container).visibility = View.VISIBLE
            findViewById<TextView>(R.id.error_text).visibility = View.GONE

            // set onClickListener on "go to forecast" button
            val forecastButton = findViewById<LinearLayout>(R.id.more_layout)
            forecastButton.setOnClickListener {
                val intent = Intent(
                    it.context,
                    ForecastActivity::class.java
                )
                val coords = json.getJSONObject("coord")
                val sysData = json.getJSONObject("sys")
                val country = sysData.getString("country")
                val cityName = json.getString("name")

                /*
                 * transfer some data to new Activity: coordinates, city and country name
                 *
                 * Have to transfer coordinates because forecast api calls require them
                 * rather than city id/name.
                 *
                 * No idea why tf openweather map doesn't allow to use town name or id in
                 * forecast api calls, while using them in current weather calls is OK,
                 * but c'est la vie
                 *
                 * City and country name are transferred in order to display in forecast
                 */
                intent.putExtra("lat", coords.getDouble("lat"))
                intent.putExtra("lon", coords.getDouble("lon"))
                intent.putExtra("city_name", cityName)
                intent.putExtra("country_name", country)
                ContextCompat.startActivity(it.context, intent, null)
            }

        }
        // if error - show error message and hide main content
        catch (e: Exception) {
            findViewById<TextView>(R.id.error_text).visibility = View.GONE
            findViewById<TextView>(R.id.error_text).text = e.localizedMessage
        }
    }
}