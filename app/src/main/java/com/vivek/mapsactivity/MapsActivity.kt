package com.vivek.mapsactivity

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.location.LocationRequest
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.vivek.mapsactivity.databinding.ActivityMapsBinding

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    val lm by lazy {
        getSystemService(LOCATION_SERVICE) as LocationManager
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        //permission to get the location
        requestFineLocation()

        //checking enable location service
        if (isLocationEnabled()) setUpLocationListen()
        else showGps()
    }

    fun isLocationEnabled():Boolean{
        return lm.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                lm.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    /*
    * for gps enable dialog
    * */
    fun showGps(){
        AlertDialog.Builder(this).setTitle("Enable Gps").setMessage("Gps required for map").setCancelable(false).setPositiveButton("Ok"){ dialogInterface: DialogInterface, i: Int ->
            startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
            dialogInterface.dismiss()
        }.show()
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestFineLocation() {
        this.requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION), 999)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 999) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setUpLocationListen()
            } else {
                //request again
                requestFineLocation()
            }
        }

    }

    /*
    * fused location
    * */
    @SuppressLint("MissingPermission")
    private fun setUpLocationListen() {
        val fl = LocationServices.getFusedLocationProviderClient(this)
        val locationRequest = com.google.android.gms.location.LocationRequest()
            .setInterval(2000)
            .setFastestInterval(2000)
            .setSmallestDisplacement(1f)
            .setPriority(PRIORITY_HIGH_ACCURACY)

        fl.requestLocationUpdates(locationRequest,
            object :LocationCallback(){
                override fun onLocationResult(p0: LocationResult) {
                    super.onLocationResult(p0)
                    for(location in p0.locations){
                        if (::mMap.isInitialized) {
                            val latlong = LatLng(location.latitude,location.longitude)
                            mMap.addMarker(MarkerOptions().position(latlong).title("Current location"))
                            mMap.moveCamera(CameraUpdateFactory.newLatLng(latlong))
                        }
                    }
                }
        },
            Looper.myLooper()!!
        )

    }


    /*
    * last known location
    * */
        /* @SuppressLint("MissingPermission")
         private fun setUpLocationListen() {
             val lm = getSystemService(LOCATION_SERVICE) as LocationManager
             val providers = lm.getProviders(true)

             var l: Location? = null
             for (i in providers.indices.reversed()) {
                 l = lm.getLastKnownLocation(providers[i])
                 if (l != null) {
                     break
                 }
             }

             l?.let {
                 if (::mMap.isInitialized) {
                     val sydney = LatLng(it.latitude, it.longitude)
                     mMap.addMarker(MarkerOptions().position(sydney).title("Current location"))
                     mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
                 }
             }

         }*/

    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.uiSettings.apply {
            isZoomControlsEnabled = true
            isZoomGesturesEnabled = true
            isMyLocationButtonEnabled = true
            isCompassEnabled = true
        }
        mMap.setMaxZoomPreference(13f)

        // Add a marker in Sydney and move the camera
        val sydney = LatLng(-34.0, 151.0)
        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
        mMap.addPolyline(
            PolylineOptions()
                .add(sydney, LatLng(20.59, 78.39))
                .color(ContextCompat.getColor(this, R.color.purple_500))
                .width(2f)
        )
    }
}