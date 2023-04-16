package com.example.metcast

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.metcast.data.WeatherModule
import com.example.metcast.screens.MainCard
import com.example.metcast.screens.TabLayout
import com.example.metcast.ui.theme.MetcastTheme
import org.json.JSONObject

const val API_KEY = "b57591f88e1640c188b123207231604"

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MetcastTheme {
                val daysList = remember {
                    mutableStateOf(listOf<WeatherModule>())
                }
                GetData("London", this, daysList)
                Image(
                    painter = painterResource(id = R.drawable.background),
                    contentDescription = "im1",
                    modifier = Modifier
                        .fillMaxSize()
                        .alpha(0.9f),
                    contentScale = ContentScale.FillBounds
                )
                Column {
                    MainCard()
                    TabLayout(daysList)
                }
            }
        }
    }
}

private fun GetData(city: String, context: Context, daysList: MutableState<List<WeatherModule>>) {
    val url = "https://api.weatherapi.com/v1/forecast.json?key=$API_KEY" +
            "&q=$city" +
            "&days=" +
            "3" +
            "&aqi=no&alerts=no"

    val queue = Volley.newRequestQueue(context)

    val request = StringRequest(
        Request.Method.GET,
        url,
        {
            responseBody ->
            val list = GetWeatherByDays(responseBody)
            daysList.value = list
        },
        {
            Log.d("Error", "Error in request: $it" )
        },
    )

    queue.add(request)
}

private fun GetWeatherByDays(response: String): List<WeatherModule> {
    if (response.isEmpty()) return listOf()

    val list = ArrayList<WeatherModule>()
    val mainObj = JSONObject(response)

    val city = mainObj.getJSONObject("location").getString("name")
    val days = mainObj.getJSONObject("forecast").getJSONArray("forecastday")

    for (i in 0 until days.length()) {
        val item = days[i] as JSONObject

        list.add(
            WeatherModule(
                city,
                item.getString("date"),
                "",
                item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("text"),
                item.getJSONObject("day")
                    .getJSONObject("condition")
                    .getString("icon"),
                item.getJSONObject("day").getString("maxtemp_c"),
                item.getJSONObject("day").getString("mintemp_c"),
                item.getJSONArray("hour").toString(),
            )
        )
    }

    list[0] = list[0].copy(
        time = mainObj.getJSONObject("current").getString("last_updated"),
        tempCurrent = mainObj.getJSONObject("current").getString("temp_c")
    )

    return list
}