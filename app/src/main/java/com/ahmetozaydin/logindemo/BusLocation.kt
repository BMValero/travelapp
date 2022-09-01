package com.ahmetozaydin.logindemo

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.app.ActivityCompat
import androidx.core.graphics.drawable.toBitmap
import com.ahmetozaydin.logindemo.databinding.ActivityBusLocationBinding
import com.ahmetozaydin.logindemo.model.BusLocationModel
import com.ahmetozaydin.logindemo.model.BusLocations
import com.ahmetozaydin.logindemo.service.BusLocationsAPI
import com.ahmetozaydin.logindemo.view.Stops
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class BusLocation : AppCompatActivity(), OnMapReadyCallback {

    var runnable: Runnable = Runnable {}
    var handler: Handler = Handler(Looper.getMainLooper())
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var markerList = ArrayList<Marker>()
    private var marker: Marker? = null
    private var one = 0
    private lateinit var bitmap: Bitmap


    private lateinit var binding: ActivityBusLocationBinding
    private lateinit var mMap: GoogleMap
    private var busLocationsList: ArrayList<BusLocations>? = null
    private lateinit var location: LatLng
    var latlngList = arrayListOf<LatLng>()
    // var markerOptions = MarkerOptions()
    //var markerList = ArrayList<Marker>()

    companion object {
        const val BASE_URL = "https://tfe-opendata.com/api/v1/"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)


        binding = ActivityBusLocationBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        bitmap =
            baseContext.let {
                AppCompatResources.getDrawable(
                    this@BusLocation,
                    R.drawable.vector_bus
                )!!.toBitmap()
            }
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        getLastKnownLocation()
        fetchData()

    }

    private fun fetchData() {
        runnable = object : Runnable {
            override fun run() {
                val retrofit = Retrofit.Builder()
                    .baseUrl(Stops.BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build()
                val service = retrofit.create(BusLocationsAPI::class.java)
                val call = service.getData()

                call.enqueue(object : Callback<BusLocationModel> {

                    override fun onFailure(call: Call<BusLocationModel>, t: Throwable) {
                        t.printStackTrace()
                    }

                    override fun onResponse(
                        call: Call<BusLocationModel>,
                        response: Response<BusLocationModel>
                    ) {
                        if (response.isSuccessful) {
                            mMap.clear()
                            response.body()?.let {
                                busLocationsList = ArrayList(it.vehicles)
                                val newLatLng = arrayListOf<LatLng>()
                                for (bus: BusLocations in busLocationsList!!) {
                                    val aLatLong = LatLng(bus.latitude, bus.longitude)
                                    newLatLng.add(aLatLong)
                                    try {
                                        if (one == 0) {
                                            latlngList.add(aLatLong)
                                        } else {
                                            latlngList.forEachIndexed { index, latLng ->
                                                if (latlngList[index].latitude != newLatLng[index].latitude || latlngList[index].longitude != newLatLng[index].longitude) {
                                                    println(index)
                                                    println(" $latLng.latitude     " + latLng.longitude)
                                                    val newPosition = LatLng(
                                                        newLatLng[index].latitude,
                                                        newLatLng[index].longitude
                                                    )
                                                    latlngList[index] = newPosition
                                                }
                                            }
                                        }

                                    } catch (exception: Exception) {
                                        exception.printStackTrace()
                                    }


                                    /* location = LatLng(bus.latitude, bus.longitude)

                                     val bitmap =
                                         baseContext.let {
                                             AppCompatResources.getDrawable(
                                                 this@BusLocation,
                                                 R.drawable.vector_bus
                                             )!!.toBitmap()
                                         }

                                     mMap.addMarker(
                                         MarkerOptions()
                                             .position(location)
                                             .snippet(
                                                 "${location.latitude}\n" +
                                                         "${location.latitude}"
                                             )
                                             .icon(bitmap.let { BitmapDescriptorFactory.fromBitmap(it) })
                                             .title("Bus Location")
                                     )*/


                                }
                                latlngList.forEach { LatLng ->


                                    mMap.addMarker(
                                        MarkerOptions()
                                            .position(LatLng)
                                            .snippet(
                                                "${LatLng.latitude}\n" +
                                                        "${LatLng.latitude}"
                                            )
                                            .icon(bitmap.let {
                                                BitmapDescriptorFactory.fromBitmap(bitmap)
                                            })
                                            .title("Bus Location")
                                    )
                                }
                                one = 1
                            }

                        }

                    }
                })

                handler.postDelayed(this, 16000)// this refers to runnable.

            }

        }
        handler.post(runnable)

    }

    private fun getLastKnownLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location ->
                if (location != null) {
                    // use your location object
                    // get latitude , longitude and other info from this
                    val userLocation = LatLng(location.latitude, location.longitude)
                    println(userLocation.toString())
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    val bitmap =
                        baseContext.let {
                            AppCompatResources.getDrawable(
                                this@BusLocation,
                                R.drawable.vector_user_location
                            )!!.toBitmap()
                        }

                    mMap.addMarker(
                        MarkerOptions()
                            .position(userLocation)
                            .icon(bitmap.let { BitmapDescriptorFactory.fromBitmap(it) })
                            .title("Your Location")
                    )


                }

            }

    }


}