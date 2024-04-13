package com.example.weatherapp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.activityViewModels
import com.example.weatherapp.DialogManager
import com.example.weatherapp.MainViewModel
import com.example.weatherapp.adapters.VpAdapter
import com.example.weatherapp.databinding.FragmentMainBinding
import com.example.weatherapp.retrofit.RequestApi
import com.example.weatherapp.retrofit.WeatherModel
import com.example.weatherapp.retrofit.WeatherResponse
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.math.roundToInt

const val API_KEY = "cad9164de5444353a8770509240903"

class MainFragment : Fragment() {
    private lateinit var fLocationClient: FusedLocationProviderClient
    private val fList = listOf(
        HoursFragment.newInstance(),
        DaysFragment.newInstance()
    )
    private val tList = listOf(
        "Hours",
        "Days"
    )
    private lateinit var pLauncher: ActivityResultLauncher<String>
    private lateinit var binding: FragmentMainBinding
    private val model: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        checkPermission()
        init()
        updateCurrentCard()
    }

    override fun onResume() {
        super.onResume()
        checkLocation()
    }

    private fun init() = with(binding) {
        fLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        val adapter = VpAdapter(activity as FragmentActivity, fList)
        vp.adapter = adapter
        TabLayoutMediator(tabLayout, vp) { tab, pos ->
            tab.text = tList[pos]
        }.attach()
        ibSync.setOnClickListener {
            tabLayout.selectTab(tabLayout.getTabAt(0))
            checkLocation()
        }
        ibSearch.setOnClickListener {
            DialogManager.searchByNameDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    name?.let { it1 -> requestWeatherData(it1) }
                }
            })
        }
    }

    private fun checkLocation() {
        if (isLocationEnabled()) {
            getLocation()
        } else {
            DialogManager.locationSettingsDialog(requireContext(), object : DialogManager.Listener {
                override fun onClick(name: String?) {
                    startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
                }
            })
        }
    }

    private fun isLocationEnabled(): Boolean {
        val lm = activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER)
    }

    private fun getLocation() {
        val ct = CancellationTokenSource()
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fLocationClient
            .getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, ct.token)
            .addOnCompleteListener {
                requestWeatherData("${it.result.latitude}, ${it.result.longitude}")
            }
    }

    private fun updateCurrentCard() = with(binding) {
        model.liveDataCurrent.observe(viewLifecycleOwner) {
            val maxMinTemp = "${it.maxTemp}°C/${it.minTemp}°C"
            val maxMinTempRound =
                "${it.maxTemp.toDouble().roundToInt()}°C/${it.minTemp.toDouble().roundToInt()}°C"
            val currentTemp = if (it.currentTemp.isNotEmpty()) {
                "${it.currentTemp.toDouble().roundToInt()}°C"
            } else {
                ""
            }
            tvData.text = it.time
            tvCity.text = it.city
            tvCurrentTemp.text = if (it.currentTemp.isEmpty()) maxMinTempRound else currentTemp
            tvCondition.text = it.condition
            tvMaxMin.text = if (it.currentTemp.isEmpty()) "" else maxMinTemp
            Picasso.get().load("https:" + it.imageUrl).into(imWeather)
        }
    }

    private fun permissionListener() {
        pLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            Toast.makeText(activity, "Permission is $it", Toast.LENGTH_LONG).show()
        }
    }

    private fun checkPermission() {
        if (!isPermissionGranted(Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionListener()
            pLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun requestWeatherData(city: String) {
        val interceptor = HttpLoggingInterceptor()
        interceptor.level = HttpLoggingInterceptor.Level.BODY

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        val gson: Gson = GsonBuilder().create()

        val requestWeatherData = Retrofit.Builder()
            .baseUrl("https://api.weatherapi.com/v1/").client(client)
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        val requestValue = requestWeatherData.create(RequestApi::class.java)

        CoroutineScope(Dispatchers.IO).launch {
            val value = requestValue.getWeather(
                key = API_KEY,
                q = city,
                days = "3",
                aqi = "no",
                alerts = "no"
            )
            convertedResponse(value)
        }
    }

    private fun convertedResponse(response: WeatherResponse) {
        val list = parseDays(response)
        parseCurrentData(response, list[0])
    }

    private fun parseDays(response: WeatherResponse): List<WeatherModel> {
        val list = ArrayList<WeatherModel>()

        for (i in 0 until response.forecast.forecastday.size) {
            val logged = response.forecast.forecastday[i].hour
            val item = WeatherModel(
                city = response.location.name,
                time = response.forecast.forecastday[i].date,
                condition = response.forecast.forecastday[i].day.condition.text,
                currentTemp = "",
                maxTemp = response.forecast.forecastday[i].day.maxtemp_c,
                minTemp = response.forecast.forecastday[i].day.mintemp_c,
                imageUrl = response.forecast.forecastday[i].day.condition.icon,
                logged,
            )
            list.add(item)
        }
        model.liveDataList.postValue(list)
        return list
    }

    private fun parseCurrentData(response: WeatherResponse, weatherItem: WeatherModel) {
        val item = WeatherModel(
            city = response.location.name,
            time = response.current.last_updated,
            condition = response.current.condition.text,
            currentTemp = response.current.temp_c.toString(),
            maxTemp = weatherItem.maxTemp,
            minTemp = weatherItem.minTemp,
            imageUrl = response.current.condition.icon,
            hours = weatherItem.hours,
        )
        model.liveDataCurrent.postValue(item)

    }

    companion object {
        @JvmStatic
        fun newInstance() = MainFragment()
    }
}