package com.example.finalproject

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.core.content.ContextCompat.getSystemService
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FirstFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FirstFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_first, container, false)
        val trainNoText = rootView.findViewById<TextView>(R.id.trainNoText)
            trainNoText.inputType = InputType.TYPE_CLASS_NUMBER

        trainNoText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val inputValue = trainNoText.text.toString()
                TrainTimetable(inputValue) { TrainTimeTableList ->
                    val TrainTimeTableListView = rootView.findViewById<ListView>(R.id.ListView)
                    val TrainTimeTableAdapter = ArrayAdapter(
                        requireContext(),
                        android.R.layout.simple_list_item_1,
                        TrainTimeTableList
                    )
                    TrainTimeTableListView.adapter = TrainTimeTableAdapter
                    TrainTimeTableAdapter.notifyDataSetChanged()
                }

                val inputMethodManager =
                    requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(trainNoText.windowToken, 0)

                true
            } else {
                false
            }
        }


        return rootView
    }

    private fun TrainTimetable(
        stationID: String,
        callback: (TrainTimeTable: List<String>) -> Unit
    ) {
        val TrainTimeTableList: MutableList<String> = mutableListOf()
        val accessToken = (activity as? MainActivity)?.accessToken
        val client = OkHttpClient()
        val url =
            "https://tdx.transportdata.tw/api/basic/v3/Rail/TRA/GeneralTrainTimetable/TrainNo/${stationID}?%24format=JSON"
        val request = Request.Builder()
            .url(url)
            .addHeader("accept", "application/json")
            .addHeader(
                "Authorization",
                "Bearer $accessToken"
            )
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val gson = Gson()
                    val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                    val TrainTimetables = jsonObject.getAsJsonArray("TrainTimetables")
                    val TrainInfo = TrainTimetables
                        .get(0)
                        .asJsonObject
                        .getAsJsonObject("TrainInfo")

                    val TrainNo = TrainInfo
                        .asJsonObject
                        .get("TrainNo")
                        .asInt

                    val TrainTypeName = TrainInfo
                        .asJsonObject
                        .getAsJsonObject("TrainTypeName")
                        .get("Zh_tw")
                        .asString

                    val StopTimes = TrainTimetables
                        .get(0)
                        .asJsonObject
                        .getAsJsonArray("StopTimes")

                    for (i in 0 until StopTimes.size()) {
                        val StationName = StopTimes
                            .get(i)
                            .asJsonObject
                            .getAsJsonObject("StationName")
                            .get("Zh_tw")
                            .asString

                        val ArrivalTime = StopTimes
                            .get(i)
                            .asJsonObject
                            .get("ArrivalTime")

                        val DepartureTime = StopTimes
                            .get(i)
                            .asJsonObject
                            .get("DepartureTime")

                        val numSpaces = 10
                        val spaces = " ".repeat(numSpaces)
                        val numSpaces1 = 5
                        val spaces1 = " ".repeat(numSpaces1)
                        TrainTimeTableList.add(spaces+StationName + spaces + ArrivalTime + spaces + DepartureTime)
                    }
                    activity?.runOnUiThread {
                        callback(TrainTimeTableList)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FirstFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FirstFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}