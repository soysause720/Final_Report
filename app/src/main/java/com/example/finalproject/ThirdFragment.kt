package com.example.finalproject

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
 * Use the [ThirdFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class ThirdFragment : Fragment() {
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
        val rootView = inflater.inflate(R.layout.fragment_third, container, false)
        val SelectStationText = rootView.findViewById<TextView>(R.id.SelectStationText)
        SelectStationText.visibility = View.VISIBLE
        SelectStationText.setOnClickListener {
            showDropdownDialog() { selectedItem: String ->
                SelectStationText.text = selectedItem
                StationIDSearchtemp(selectedItem) { stationID: String ->
                    TrainInfo(stationID) { TrainInfo ->
                        val TrainInfoListView =
                            rootView.findViewById<ListView>(R.id.TrainInfoListView)
                        val TrainInfoAdapter = ArrayAdapter(
                            requireContext(),
                            android.R.layout.simple_list_item_1,
                            TrainInfo
                        )
                        TrainInfoListView.adapter = TrainInfoAdapter
                        TrainInfoAdapter.notifyDataSetChanged()
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

    private fun TrainInfo(
        AssignStationID: String?,
        callback: (TrainInfo: List<String>) -> Unit
    ) {
        val TrainInfo: MutableList<String> = mutableListOf()
        val accessToken = (activity as? MainActivity)?.accessToken
        val client = OkHttpClient()
        val url1 =
            "https://tdx.transportdata.tw/api/basic/v2/Rail/TRA/LiveBoard/Station/$AssignStationID?%24format=JSON"
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
                        val TrainNo = jsonArray
                            .get(i)
                            .asJsonObject
                            .get("TrainNo")
                            .asString

                        val TrainTypeName = jsonArray
                            .get(i)
                            .asJsonObject
                            .getAsJsonObject("TrainTypeName")
                            .asJsonObject
                            .get("Zh_tw")
                            .asString

                        val EndingStationName = jsonArray
                            .get(i)
                            .asJsonObject
                            .getAsJsonObject("EndingStationName")
                            .asJsonObject
                            .get("Zh_tw")
                            .asString

                        val ScheduledDepartureTime = jsonArray
                            .get(i)
                            .asJsonObject
                            .get("ScheduledDepartureTime")
                            .asString
                        val DelayTime = jsonArray
                            .get(i)
                            .asJsonObject
                            .get("DelayTime")
                            .asString

                        val numSpaces = 10
                        val spaces = " ".repeat(numSpaces)
                        val numSpaces1 = 5
                        val spaces1 = " ".repeat(numSpaces1)

                        val CharactersDevice = TrainTypeName.substring(0,2)
                        if(CharactersDevice == "自強"){
                            TrainInfo.add(
                                "${TrainTypeName.substring(0,2)}$spaces1$TrainNo${spaces}往$EndingStationName\n" +
                                        "出發時間 $ScheduledDepartureTime 誤點 ${DelayTime}分鐘"
                            )
                        }else{
                            TrainInfo.add(
                                "${TrainTypeName}$spaces1$TrainNo${spaces}往$EndingStationName\n" +
                                        "出發時間 $ScheduledDepartureTime 誤點 ${DelayTime}分鐘"
                            )
                        }
                        activity?.runOnUiThread {
                            callback(TrainInfo)
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

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ThrisFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ThirdFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}