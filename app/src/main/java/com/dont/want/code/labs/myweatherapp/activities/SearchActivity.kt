package com.dont.want.code.labs.myweatherapp.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import android.os.AsyncTask
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
import com.dont.want.code.labs.myweatherapp.R
import com.dont.want.code.labs.myweatherapp.data.City
import org.json.JSONArray
import org.json.JSONObject


class SearchActivity : AppCompatActivity() {

    // search container and progress bar for visibility manipulation
    private lateinit var progressBar: ProgressBar
    private lateinit var searchContainer: LinearLayout

    private lateinit var adapter: CityAdapter

    // search field and results field
    private lateinit var searchTextEdit: EditText
    private lateinit var cityRecyclerView: RecyclerView


    override fun onCreate(savedInstanceState: Bundle?) {

        // lock orientation
        requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        // make activity fullscreen TODO deprecated, find newer method
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        )

        //Remove title bar
        this.requestWindowFeature(Window.FEATURE_NO_TITLE)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchTextEdit = findViewById(R.id.city_search)
        cityRecyclerView = findViewById(R.id.city_list)


        supportActionBar?.hide()

        searchContainer = findViewById(R.id.search_container)
        progressBar = findViewById(R.id.progress_bar)
        CityListLoader().execute()


        cityRecyclerView.setHasFixedSize(true)
        cityRecyclerView.layoutManager = LinearLayoutManager(this)
    }

    private inner class CityListLoader : AsyncTask<Void, Void, Void>() {

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            searchContainer.visibility = View.GONE
        }

        override fun doInBackground(vararg p0: Void?): Void? {

            searchContainer = findViewById(R.id.search_container)
            progressBar = findViewById(R.id.progress_bar)

            progressBar.visibility = View.VISIBLE
            searchContainer.visibility = View.GONE

            val citiesFile = resources.openRawResource(R.raw.cities)
            val jsonString = citiesFile.bufferedReader().readText()
            val citiesList = JSONArray(jsonString)
            val cityObjectArray = ArrayList<City>()
            for (i in 0 until citiesList.length()) {
                val city: JSONObject = citiesList[i] as JSONObject
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

            return null
        }

        override fun onPostExecute(result: Void?) {
            super.onPostExecute(result)
            progressBar.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE
        }

    }


    // adapter for feeding recyclerview
    class CityAdapter(private val dataSet: ArrayList<City>) :
        RecyclerView.Adapter<CityAdapter.ViewHolder>() {

        // class for filling view with data
        class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {

            val name = view.findViewById<TextView>(R.id.listitem_city_name)
            val country = view.findViewById<TextView>(R.id.listitem_country)
            var id=0

            init {
                itemView.setOnClickListener {
                    val intent = Intent(
                        it.context,
                        CurrentActivity::class.java
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
                viewHolder.id = dataSet[position].id
            }
            
        // Return the size of your dataset (invoked by the layout manager)
        override fun getItemCount() = dataSet.size

    }

}


