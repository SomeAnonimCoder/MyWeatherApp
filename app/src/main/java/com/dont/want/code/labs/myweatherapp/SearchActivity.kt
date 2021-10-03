package com.dont.want.code.labs.myweatherapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import androidx.recyclerview.widget.LinearLayoutManager
import org.json.JSONObject


class SearchActivity : AppCompatActivity() {

    lateinit var progressBar: ProgressBar
    lateinit var searchContainer:LinearLayout
    lateinit var cityData: JSONArray
    lateinit var cityObjectArray: ArrayList<City>
    lateinit var adapter: CityAdapter

    data class City(val name:String, val temp: Double, val humidity:Int, val wind:Double){}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val searchTextEdit = findViewById<EditText>(R.id.city_search)
        val cityRecyclerView = findViewById<RecyclerView>(R.id.city_list)

        searchContainer = findViewById<LinearLayout>(R.id.search_container)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)

        progressBar.visibility = View.VISIBLE
        searchContainer.visibility = View.GONE

        val citiesFile = resources.openRawResource(R.raw.cities)
        val jsonString = citiesFile.bufferedReader().readText()
        val citiesList = JSONArray(jsonString)
        cityData = citiesList

        progressBar.visibility = View.GONE
        searchContainer.visibility = View.VISIBLE


        searchTextEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                try{
                    val currentCityData = cityObjectArray.filter {
                            city -> city.name.lowercase().contains(p0.toString().lowercase()!!)
                    } as ArrayList<City>
                    cityRecyclerView.swapAdapter(CityAdapter(currentCityData), false)
                }
                finally {
                }
            }
        })



        cityObjectArray = ArrayList<City>()
        for (i in 0 until cityData.length()){
            val city:JSONObject = cityData[i] as JSONObject
            cityObjectArray.add(
                City(
                    city.getString("name"),
                    0.0,
                    0,
                    0.0
                )
            )
        }

        adapter = CityAdapter(cityObjectArray)
        cityRecyclerView.adapter = adapter
        cityRecyclerView.setHasFixedSize(true)
        cityRecyclerView.setLayoutManager(LinearLayoutManager(this))
    }



    class CityAdapter(private val dataSet: ArrayList<City>) :
        RecyclerView.Adapter<CityAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name = view.findViewById<TextView>(R.id.listitem_city_name)
            val temp = view.findViewById<TextView>(R.id.listitem_temp)
            val hum = view.findViewById<TextView>(R.id.listitem_humidity)
            val  wind = view.findViewById<TextView>(R.id.listitem_wind)
        }

        // Create new views (invoked by the layout manager)
        override fun onCreateViewHolder(viewGroup: ViewGroup, viewType: Int): ViewHolder {
            // Create a new view, which defines the UI of the list item
            val view = LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.city_item, viewGroup, false)

            return ViewHolder(view)
        }

        // Replace the contents of a view (invoked by the layout manager)
        override fun onBindViewHolder(viewHolder: ViewHolder, position: Int) {

            // Get element from your dataset at this position and replace the
            // contents of the view with that element
            viewHolder.name.text = dataSet[position].name
            viewHolder.hum.text = dataSet[position].humidity.toString() + " %"
            viewHolder.temp.text = dataSet[position].temp.toString() + "°C"
            viewHolder.wind.text = dataSet[position].wind.toString() + "m/s"
        }

        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }



}
