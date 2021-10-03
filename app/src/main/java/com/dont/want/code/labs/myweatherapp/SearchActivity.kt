package com.dont.want.code.labs.myweatherapp

import android.os.AsyncTask
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import org.json.JSONArray


class SearchActivity : AppCompatActivity() {

    lateinit var progressBar: ProgressBar
    lateinit var searchContainer:LinearLayout


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)


        val searchTextEdit = findViewById<EditText>(R.id.city_search)
        val cityRecyclerView = findViewById<RecyclerView>(R.id.city_list)

        searchContainer = findViewById<LinearLayout>(R.id.search_container)
        progressBar = findViewById<ProgressBar>(R.id.progress_bar)

            searchTextEdit.addTextChangedListener(object : TextWatcher {

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun afterTextChanged(p0: Editable?) {
                // TODO filter recycleview
            }
        })


    }

    inner class fileLoader():AsyncTask<Void, Void, JSONArray>(){

        override fun onPreExecute() {
            super.onPreExecute()
            progressBar.visibility = View.VISIBLE
            searchContainer.visibility = View.GONE
        }

        override fun doInBackground(vararg p0: Void?): JSONArray {
            val citiesFile = resources.openRawResource(R.raw.cities)
            val jsonString = citiesFile.bufferedReader().readText()
            val citiesList = JSONArray(jsonString)
            return  citiesList
        }

        override fun onPostExecute(result: JSONArray?) {
            super.onPostExecute(result)
            progressBar.visibility = View.GONE
            searchContainer.visibility = View.VISIBLE
        }

    }

}
