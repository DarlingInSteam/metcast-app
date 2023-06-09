package com.example.metcast.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.metcast.R
import com.example.metcast.data.WeatherModule
import com.example.metcast.ui.theme.BlueLight
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.pagerTabIndicatorOffset
import com.google.accompanist.pager.rememberPagerState
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject

@Composable
fun MainCard(currDay: MutableState<WeatherModule>, onClickSync: () -> Unit, onClickSearch: () -> Unit) {
    Column(
        modifier = Modifier
            .padding(5.dp)
    ) {
        Card(
            modifier = Modifier.fillMaxWidth(),
            backgroundColor = BlueLight,
            elevation = 0.dp,
            shape = RoundedCornerShape(10.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {

                    Column() {
                        Row() {
                            Text(
                                text = currDay.value.city,
                                style = TextStyle(fontSize = 24.sp),
                                color = Color.White
                            )
                            AsyncImage(
                                model = "https:" + currDay.value.icon,
                                contentDescription = "im2",
                                modifier = Modifier
                                    .size(40.dp)
                                    .padding(5.dp)
                            )
                        }
                    }
                    Text(
                        text =
                        if (currDay.value.tempCurrent.isNotEmpty()) currDay.value.tempCurrent
                        else "${currDay.value.maxTemp}°C/${currDay.value.minTemp}°C",
                        style = TextStyle(fontSize = 54.sp),
                        color = Color.White
                    )
                    Text(
                        text = currDay.value.condition,
                        style = TextStyle(fontSize = 16.sp),
                        color = Color.White
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        IconButton(
                            onClick = {
                                onClickSearch.invoke()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_search),
                                contentDescription = "im3",
                                tint = Color.White
                            )
                        }
                        Text(
                            modifier = Modifier.padding(12.dp),
                            text = "${currDay.value.maxTemp}°C/${currDay.value.minTemp}°C",
                            style = TextStyle(fontSize = 16.sp),
                            color = Color.White
                        )
                        IconButton(
                            onClick = {
                                onClickSync.invoke()
                            }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_sync),
                                contentDescription = "im4",
                                tint = Color.White
                            )
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalPagerApi::class)
@Composable
fun TabLayout(daysList: MutableState<List<WeatherModule>>, currDay: MutableState<WeatherModule>) {
    val tabList = listOf("HOURS", "DAYS")
    val pagerState = rememberPagerState()
    val tabIndex = pagerState.currentPage
    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .padding(5.dp)
    ) {
        TabRow(
            selectedTabIndex = tabIndex,
            indicator = { position ->
                TabRowDefaults.Indicator(
                    Modifier.pagerTabIndicatorOffset(pagerState, position)
                )
            },
            backgroundColor = BlueLight,
            contentColor = Color.White,
            modifier = Modifier
                .clip(RoundedCornerShape(10.dp))
        ) {
            tabList.forEachIndexed { currentPosition, string ->
                Tab(
                    selected = false,
                    onClick = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(currentPosition)
                        }
                    },
                    text = {
                        Text(text = string)
                    }
                )
            }
        }
        HorizontalPager(
            count = tabList.size,
            state = pagerState,
            modifier = Modifier.weight(1.0f)
        ) { index ->
            val list = when (index) {
                0 -> GetWeatherByHours(currDay.value.hours)
                1 -> daysList.value
                else -> daysList.value
            }
            MainList(list, currDay)
        }
    }
}

private fun GetWeatherByHours(hours: String): List<WeatherModule> {
    if (hours.isEmpty()) return emptyList()

    val hoursArr = JSONArray(hours)
    val list = ArrayList<WeatherModule>()

    for (i in 0 until hoursArr.length()) {
        val item = hoursArr[i] as JSONObject

        list.add(
            WeatherModule(
                "",
                item.getString("time"),
                item.getString("temp_c").toFloat().toInt().toString() + "°C",
                item.getJSONObject("condition").getString("text"),
                item.getJSONObject("condition").getString("icon"),
                "",
                "",
                "",
            )
        )
    }

    return list
}