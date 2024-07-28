package com.example.uasrayhansafarti4a

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.gson.annotations.SerializedName
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.util.Locale

class MainActivity : AppCompatActivity(), OnMapReadyCallback {
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val REQUEST_LOCATION_PERMISSION = 1
    private lateinit var locationText: TextView
    private lateinit var weatherText: TextView
    private lateinit var weatherImage: ImageView
    private lateinit var mapView: MapView
    private lateinit var googleMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            getLastLocation()
            insets
        }

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationText = findViewById(R.id.locationText)
        weatherText = findViewById(R.id.weatherText)
        mapView = findViewById(R.id.mapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)

        supportActionBar?.setDisplayShowTitleEnabled(false)

        val fab: View = findViewById(R.id.fab)
        fab.setOnClickListener { view ->
            requestLocationPermission()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun requestLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_PERMISSION)
        } else {
            getLastLocation()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getLastLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    val latitude = location.latitude
                    val longitude = location.longitude
                    // Use Geocoder to get address
                    val geocoder = Geocoder(this, Locale.getDefault())
                    val addresses = geocoder.getFromLocation(latitude, longitude, 1)
                    val address: String = if (addresses?.isNotEmpty() == true) {
                        addresses[0].getAddressLine(0)
                    } else {
                        "Address not found"
                    }
                    locationText.text = address

                    // Show location on map
                    val userLocation = LatLng(latitude, longitude)
                    googleMap.addMarker(MarkerOptions().position(userLocation).title("Your Location"))
                    googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

                    // Fetch weather information
                    fetchWeather(latitude, longitude)
                } else {
                    Toast.makeText(this, "Location not available", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun fetchWeather(latitude: Double, longitude: Double) {
        val apiKey = "e905f4acf3f2fef9c35faf26f4d5ea00"
        val retrofit = Retrofit.Builder()
            .baseUrl("https://api.openweathermap.org/data/2.5/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        val weatherService = retrofit.create(WeatherService::class.java)

        weatherService.getWeather(latitude, longitude, apiKey, "metric").enqueue(object : retrofit2.Callback<WeatherResponse> {
            override fun onResponse(call: Call<WeatherResponse>, response: retrofit2.Response<WeatherResponse>) {
                if (response.isSuccessful) {
                    val weather = response.body()
                    weather?.let {
                        val weatherDescription = it.weather.firstOrNull()?.description ?: "Unknown"
                        val temperature = it.main.temp
                        weatherText.text = "$weatherDescription\n\n$temperature°C"
                        weatherImage = findViewById(R.id.weatherImage)

                        when {
                            weatherDescription.contains("clear", true) || weatherDescription.contains("sun", true) -> {
                                Toast.makeText(this@MainActivity, "The weather is nice! Lets touch the grass", Toast.LENGTH_LONG).show()
                                weatherImage.setImageResource(R.drawable.sunnyday)
                            }
                            weatherDescription.contains("cloud", true) -> {
                                Toast.makeText(this@MainActivity, "It's a bit cloudy! Make sure to bring your Umbrella", Toast.LENGTH_LONG).show()
                                weatherImage.setImageResource(R.drawable.cloudyday)
                            }
                            else -> {
                                Toast.makeText(this@MainActivity, "It's better to stay at home! Netflix sound fun isn't ", Toast.LENGTH_LONG).show()
                                weatherImage.setImageResource(R.drawable.rainyday)
                            }
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WeatherResponse>, t: Throwable) {
                Toast.makeText(this@MainActivity, "Failed to fetch weather information", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    interface WeatherService {
        @GET("weather")
        fun getWeather(
            @Query("lat") lat: Double,
            @Query("lon") lon: Double,
            @Query("appid") apiKey: String,
            @Query("units") units: String
        ): Call<WeatherResponse>
    }

    data class WeatherResponse(
        @SerializedName("weather") val weather: List<Weather>,
        @SerializedName("main") val main: Main
    )

    data class Weather(
        @SerializedName("description") val description: String
    )

    data class Main(
        @SerializedName("temp") val temp: Float
    )
}
