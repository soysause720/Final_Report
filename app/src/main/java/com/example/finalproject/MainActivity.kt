package com.example.finalproject

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.ui.NavigationUI.setupWithNavController
import com.example.finalproject.databinding.ActivityMainBinding
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.JsonObject
import okhttp3.*
import java.io.IOException


var authentication: String = "eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJER2lKNFE5bFg4WldFajlNNEE2amFVNm9JOGJVQ3RYWGV6OFdZVzh3ZkhrIn0.eyJleHAiOjE2ODY1NzkzMjQsImlhdCI6MTY4NjQ5MjkyNCwianRpIjoiMDUyNzdlZjUtMDBhZS00ZWIyLWExMmMtY2Y2OTgzNDU4ZjRhIiwiaXNzIjoiaHR0cHM6Ly90ZHgudHJhbnNwb3J0ZGF0YS50dy9hdXRoL3JlYWxtcy9URFhDb25uZWN0Iiwic3ViIjoiYzY3YTRmNDMtMDJkZC00YzZkLWJkNjYtOTAwZGNjZmY3ZWM4IiwidHlwIjoiQmVhcmVyIiwiYXpwIjoiUzA4MjIwMjItNDVlMjVmZDctNWU2My00OWY0IiwiYWNyIjoiMSIsInJlYWxtX2FjY2VzcyI6eyJyb2xlcyI6WyJzdGF0aXN0aWMiLCJwcmVtaXVtIiwibWFhcyIsImFkdmFuY2VkIiwidmFsaWRhdG9yIiwiaGlzdG9yaWNhbCIsImJhc2ljIl19LCJzY29wZSI6InByb2ZpbGUgZW1haWwiLCJ1c2VyIjoiNGNmZjU3NWQifQ.gZ9DfTogUMQXeaZBB2bFV73PtGIjrEMa2yuwRnjYjo9MqgVwX0oWLa4JS9wiGyOUCDmuBRNSmGpplS0hNFq2yCP64xHuVIkV9HDVJPaZF8Eo7BJHQ1-z8fN58qB4uyPBXEIs2wKbZ-758jK3AW1DjInhblUxv9rDyA3kL91RYqAWASt6343-nNRLe2IhsNhAJGa5eN5IKLtUINhLBg7E4RfjqGb9Ehd37tESiBBGqo8PcyTtkSTz_5zSq-IYHKYrRN8CXcK2WkgbmHg66DlhgD8BeI2X6VCuReULiacg4l6nEg-skvw6lFlfo2giw7QH8KsMdmBigMqcoP6L011Y4w"
class MainActivity : AppCompatActivity() {
    var accessToken: String? = null
    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        navController= Navigation.findNavController(this,R.id.activity_main_nav_fragment)
        setupWithNavController(binding.bottomNavigationView,navController)

        val fab = findViewById<FloatingActionButton>(R.id.floatingActionButton2)
        fab.setOnClickListener {
            val intent = Intent()
            intent.setClass(this@MainActivity, MainActivity2::class.java)
            startActivity(intent)
        }


        AccessToken { accessToken ->
            this@MainActivity.accessToken = accessToken
            val firstFragment = FirstFragment.newInstance("param1", "param2")
            val secondFragment = SecondFragment.newInstance("param1", "param2")
            val thirdFragment = ThirdFragment.newInstance("param1", "param2")
            supportFragmentManager.beginTransaction()
                .replace(R.id.activity_main_nav_fragment,thirdFragment)
                .add(R.id.activity_main_nav_fragment, secondFragment)
                .add(R.id.activity_main_nav_fragment, firstFragment)
                .commit()
        }
    }
}

fun AccessToken(callback: (accessToken: String?) -> Unit){
    val client = OkHttpClient()
    val url = "https://tdx.transportdata.tw/auth/realms/TDXConnect/protocol/openid-connect/token"
    val requestBody = FormBody.Builder()
        .add("grant_type", "client_credentials")
        .add("client_id", "S0822022-45e25fd7-5e63-49f4")
        .add("client_secret", "f378d993-61a7-4a10-b6d8-c25f3b58a6d0")
        .build()
    val request = Request.Builder()
        .url(url)
        .addHeader("content-type","application/x-www-form-urlencoded")
        .post(requestBody)
        .build()
    client.newCall(request).enqueue(object : Callback {
        override fun onFailure(call: Call, e: IOException) {
            e.printStackTrace()
            callback(null)
        }
        override fun onResponse(call: Call, response: Response) {
            val responseBody = response.body?.string()
            val gson = Gson()
            val jsonObject  = gson.fromJson(responseBody, JsonObject::class.java)
            var accessToken : String? = null
            accessToken = jsonObject.get("access_token").asString
            callback(accessToken)
        }
    })
}