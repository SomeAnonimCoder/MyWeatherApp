package com.dont.want.code.labs.myweatherapp

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray
import org.json.JSONObject


class SearchActivity : AppCompatActivity() {

    lateinit var progressBar: ProgressBar
    lateinit var searchContainer:LinearLayout
    lateinit var cityData: JSONArray
    lateinit var cityObjectArray: ArrayList<City>
    lateinit var adapter: CityAdapter

    data class City(val id:Int, val name:String, val country:String, val lon:Int, val lat:Int)

    override fun onCreate(savedInstanceState: Bundle?) {

        // lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // make activity fullscreen TODO deprecated, find newer method
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN)

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

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

        supportActionBar?.hide()

        searchTextEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                try {
                    val currentCityData = cityObjectArray.filter { city ->
                        city.name.lowercase().contains(p0.toString().lowercase())
                    } as ArrayList<City>
                    cityRecyclerView.swapAdapter(CityAdapter(currentCityData), false)
                } finally {
                }
            }
        })



        cityObjectArray = ArrayList<City>()
        for (i in 0 until cityData.length()) {
            val city: JSONObject = cityData[i] as JSONObject
            cityObjectArray.add(
                City(
                    city.getInt("id"),
                    city.getString("name"),
                    city.getString("country"),
                    city.getJSONObject("coord").getDouble("lat").toInt(),
                    city.getJSONObject("coord").getDouble("lon").toInt()
                )
            )
        }

        adapter = CityAdapter(cityObjectArray)
        cityRecyclerView.adapter = adapter
        cityRecyclerView.setHasFixedSize(true)
        cityRecyclerView.layoutManager = LinearLayoutManager(this)

    }


    class CityAdapter(private val dataSet: ArrayList<City>) :
        RecyclerView.Adapter<CityAdapter.ViewHolder>() {

        /**
         * Provide a reference to the type of views that you are using
         * (custom ViewHolder).
         */
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
            val name = view.findViewById<TextView>(R.id.listitem_city_name)
            val lon = view.findViewById<TextView>(R.id.listitem_lon)
            val country = view.findViewById<TextView>(R.id.listitem_country)
            val lat = view.findViewById<TextView>(R.id.listitem_lat)
            var id=0

            init {
                itemView.setOnClickListener{
                    val intent = Intent(
                        it.context,
                        MainActivity::class.java
                    )
                    intent.putExtra("city_id", id)
                    startActivity(it.context, intent, null)
                }
            }
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
                viewHolder.country.text = dataSet[position].country.toString()
                viewHolder.lon.text = dataSet[position].lon.toString()
                viewHolder.lat.text = dataSet[position].lat.toString()
                viewHolder.id = dataSet[position].id
            }

            // Return the size of your dataset (invoked by the layout manager)
            override fun getItemCount() = dataSet.size

        }

    }


