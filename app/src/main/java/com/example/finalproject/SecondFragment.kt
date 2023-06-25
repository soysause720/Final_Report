package com.example.finalproject

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import com.google.gson.JsonSyntaxException
import okhttp3.*
import java.io.IOException

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SecondFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SecondFragment : Fragment() {
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

    @SuppressLint("MissingInflatedId", "ResourceType")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_second, container, false)
        var StartStationID: String? = null
        var EndStationID: String? = null

        val TicketText = rootView.findViewById<TextView>(R.id.textView4)
            TicketText.text = "確定要購買此票嗎?"
        val TicketLayout = rootView.findViewById<ConstraintLayout>(R.id.TicketLayout)
            TicketLayout.visibility = View.GONE
        val StartStationText = rootView.findViewById<TextView>(R.id.StartStationType)
        val EndStationText = rootView.findViewById<TextView>(R.id.EndStationType)
        StartStationText.setOnClickListener {
            showDropdownDialog() { selectedItem: String ->
                StartStationText.text = selectedItem
                StationIDSearchtemp(selectedItem) { stationID: String ->
                    StartStationID = stationID
                    println("StartStationID:$StartStationID")
                }
            }
        }
        EndStationText.setOnClickListener {
            showDropdownDialog() { selectedItem: String ->
                EndStationText.text = selectedItem
                StationIDSearchtemp(selectedItem) { stationID: String ->
                    EndStationID = stationID
                }
            }
        }


        var StartCalendar: String? = null
        val startCalenderText = rootView.findViewById<TextView>(R.id.textView)
        val startCalender = rootView.findViewById<CalendarView>(R.id.calendarView)
        startCalender.visibility = View.GONE
        startCalenderText.setOnClickListener {
            startCalender.visibility = View.VISIBLE
            startCalender.setOnDateChangeListener { view, year, month, dayOfMonth ->
                if (month > 8) {
                    if (dayOfMonth > 9) {
                        val selectedDate = "$year-${month + 1}-$dayOfMonth"
                        startCalenderText.text = selectedDate
                        StartCalendar = selectedDate
                    } else {
                        val selectedDate = "$year-${month + 1}-0$dayOfMonth"
                        startCalenderText.text = selectedDate
                        StartCalendar = selectedDate
                    }

                } else {
                    if (dayOfMonth > 9) {
                        val selectedDate = "$year-0${month + 1}-$dayOfMonth"
                        startCalenderText.text = selectedDate
                        StartCalendar = selectedDate
                    } else {
                        val selectedDate = "$year-0${month + 1}-0$dayOfMonth"
                        startCalenderText.text = selectedDate
                        StartCalendar = selectedDate
                    }
                }
                startCalender.visibility = View.GONE
            }
        }


        val test = listOf("111", "222", "333")
        val TestListView = rootView.findViewById<ListView>(R.id.TestListView)
        val TestAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, test)
        TestListView.adapter = TestAdapter
        TestListView.visibility = View.GONE

        val SelectStationLayout = rootView.findViewById<ConstraintLayout>(R.id.SelectStationLayout)
        val SelectCalederLayout = rootView.findViewById<ConstraintLayout>(R.id.SelectCalederLayout)
        val CalenderViewLayout = rootView.findViewById<ConstraintLayout>(R.id.CalenderViewLayout)
        val SearchButtonLayout = rootView.findViewById<ConstraintLayout>(R.id.SearchButtonLayout)
        val SearchButton = rootView.findViewById<Button>(R.id.button3)
        SearchButton.setOnClickListener {
            SelectStationLayout.visibility = View.GONE
            SelectCalederLayout.visibility = View.GONE
            CalenderViewLayout.visibility = View.GONE
            SearchButtonLayout.visibility = View.GONE

            TrainTimetable(StartStationID, EndStationID, StartCalendar) { trainNos ->
                val TrainNoListView = rootView.findViewById<ListView>(R.id.TrainNoListView)
                val TrainNoAdapter =
                    ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, trainNos)
                TrainNoListView.adapter = TrainNoAdapter
                TrainNoAdapter.notifyDataSetChanged()
                TrainNoListView.setOnItemClickListener { parent, view, position, id ->
                    // 在這裡處理選項選擇事件
                    val selectedItem = parent.getItemAtPosition(position) as String
                    TrainNoListView.visibility = View.INVISIBLE
                    TicketLayout.visibility = View.VISIBLE
                    val ConfirmButton = rootView.findViewById<Button>(R.id.button)
                    val CancelButton = rootView.findViewById<Button>(R.id.button2)

                    ConfirmButton.setOnClickListener {
                        val intent = Intent(activity, MainActivity2::class.java)
                        val data = selectedItem
                        intent.putExtra("data_key", data)
                        activity?.startActivity(intent)
                    }

                    CancelButton.setOnClickListener{
                        TrainNoListView.visibility = View.VISIBLE
                        TicketLayout.visibility = View.GONE
                    }


                }
            }

        }



        return rootView
    }


    private fun showDropdownDialog(callback: (selectedItem: String) -> Unit) {
        val items = resources.getStringArray(R.array.station)
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("选择一个选项")
        builder.setItems(items) { _, which ->
            callback(items[which])
        }
        builder.show()
    }

    private fun TrainTimetable(
        startStation: String?,
        endStation: String?,
        startCalendar: String?,
        callback: (trainNos: List<String>) -> Unit
    ) {
        val trainNos: MutableList<String> = mutableListOf()
        val accessToken = (activity as? MainActivity)?.accessToken
        val client = OkHttpClient()
        val url1 =
            "https://tdx.transportdata.tw/api/basic/v2/Rail/TRA/DailyTimetable/OD/$startStation/to/$endStation/$startCalendar?&%24format=JSON"
        val request1 = Request.Builder()
            .url(url1)
            .addHeader("accept", "application/json")
            .addHeader(
                "Authorization",
                "Bearer $accessToken"
            )
            .build()

        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
                callback(emptyList())
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                try {
                    val gson = Gson()
                    val jsonArray = gson.fromJson(responseBody, JsonArray::class.java)
                    for (i in 0 until jsonArray.size()) {
                        val DailyTrainInfo = jsonArray
                            .get(i)
                            .asJsonObject
                            .getAsJsonObject("DailyTrainInfo")

                        val trainNo = DailyTrainInfo
                            .get("TrainNo")
                            .asString

                        val StartingStationName = DailyTrainInfo
                            .getAsJsonObject("StartingStationName")
                            .get("Zh_tw")
                            .asString

                        val EndingStationName = DailyTrainInfo
                            .getAsJsonObject("EndingStationName")
                            .get("Zh_tw")
                            .asString
                        val TrainTypeName = DailyTrainInfo
                            .getAsJsonObject("TrainTypeName")
                            .get("Zh_tw")
                            .asString

                        val OriginStopTime = jsonArray
                            .get(i)
                            .asJsonObject
                            .getAsJsonObject("OriginStopTime")
                            .get("ArrivalTime")
                            .asString
                        val DestinationStopTime = jsonArray
                            .get(i)
                            .asJsonObject
                            .getAsJsonObject("DestinationStopTime")
                            .get("ArrivalTime")
                            .asString

                        val numSpaces = 10
                        val spaces = " ".repeat(numSpaces)
                        val numSpaces1 = 5
                        val spaces1 = " ".repeat(numSpaces1)
                        trainNos.add("$trainNo$spaces\n$spaces1$StartingStationName$spaces->$spaces$EndingStationName\n$spaces1$OriginStopTime$spaces->$spaces$DestinationStopTime")

                        activity?.runOnUiThread {
                            callback(trainNos)
                        }
                    }
                } catch (e: JsonSyntaxException) {
                    e.printStackTrace()
                    println("JSON 解析错误")
                } catch (e: NullPointerException) {
                    e.printStackTrace()
                    println("找不到資料!!!!!!!!")
                } catch (e: Exception) {
                    e.printStackTrace()
                    println("出現錯誤啦!!!!!!!!")
                }

            }
        })

    }

    private fun StationIDSearchtemp(startStation: String, callback: (stationID: String) -> Unit) {
        val accessToken = (activity as? MainActivity)?.accessToken
        val client = OkHttpClient()
        val url1 =
            "https://tdx.transportdata.tw/api/basic/v3/Rail/TRA/Station?%24select=StationID%2CStationName&%24filter=contains(StationName/Zh_tw,'$startStation')&%24format=JSON"
        val request1 = Request.Builder()
            .url(url1)
            .addHeader("accept", "application/json")
            .addHeader(
                "Authorization",
                "Bearer $accessToken"
            )
            .build()

        client.newCall(request1).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                e.printStackTrace()
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                // 使用 Gson 解析 JSON 字串
                val gson = Gson()
                val jsonObject = gson.fromJson(responseBody, JsonObject::class.java)
                val stations = jsonObject.getAsJsonArray("Stations")
                val stationID = stations
                    .get(0)
                    .asJsonObject
                    .get("StationID")
                    .asString
                callback(stationID)
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
         * @return A new instance of fragment SecondFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SecondFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}