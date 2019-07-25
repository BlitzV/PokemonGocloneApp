package com.app.pokemon_go_clone_app

import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private val USER_LOCATION_REQUEST_CODE = 1000
    private var playerLocation: Location? = null
    private var oldLocationOfPlayer: Location? = null
    private var pokemonCharacters:ArrayList<PokemonCharacter> = ArrayList()
    var locationManager: LocationManager? = null
    var locationListener: LocationListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment

        mapFragment.getMapAsync(this)

        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        locationListener = PlayerLocationListener()

        requestLocationPermission()

        initializePokemonCharacters()
    }

    private fun initializePokemonCharacters() {

        pokemonCharacters.add(PokemonCharacter("Hello, this is c1",
            "I'm powerful", R.drawable.c1, 1.651729,
            31.996134))
        pokemonCharacters.add(PokemonCharacter("Hello, this is c2",
            "I'm powerful", R.drawable.c2, 27.404523,
            29.647654))
        pokemonCharacters.add(PokemonCharacter("Hello, this is c3",
            "I'm powerful", R.drawable.c3, 10.492703,
            10.709112))
        pokemonCharacters.add(PokemonCharacter("Hello, this is c4",
            "I'm powerful", R.drawable.c4, 28.220750,
            1.898764))

    }

    private fun requestLocationPermission() {
        if(Build.VERSION.SDK_INT>=23){
            if(ActivityCompat.checkSelfPermission(this,android.Manifest.permission.ACCESS_FINE_LOCATION)!= PackageManager.PERMISSION_GRANTED){
                requestPermissions(arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),USER_LOCATION_REQUEST_CODE)
            }
        }

        accessUserLocation()
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (requestCode == USER_LOCATION_REQUEST_CODE){

            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                accessUserLocation()
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun accessUserLocation() {

        locationManager?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1, 2f,locationListener!!)

        val newThread = NewThread()
        newThread.start()
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    inner class PlayerLocationListener: LocationListener {

        constructor() {
            playerLocation = Location("Provider")
            playerLocation?.latitude = 0.0
            playerLocation?.latitude = 0.0
        }

        override fun onLocationChanged(location: Location?) {
            playerLocation = location
        }

        override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}

        override fun onProviderEnabled(provider: String?) {}

        override fun onProviderDisabled(provider: String?) {}
    }

    inner class NewThread: Thread {
        constructor(): super() {

            oldLocationOfPlayer = Location("MyProvider")
            oldLocationOfPlayer?.latitude = 0.0
            oldLocationOfPlayer?.longitude = 0.0

        }

        override fun run() {
            super.run()

            while (true) {
                if (oldLocationOfPlayer?.distanceTo(playerLocation) == 0f) {
                    continue
                }
                oldLocationOfPlayer = playerLocation

                try {
                    runOnUiThread {
                        mMap.clear()

                        val pLocation = LatLng(playerLocation!!.latitude, playerLocation!!.longitude)

                        mMap.addMarker(MarkerOptions().position(pLocation)
                            .title("Hi, I am the player")
                            .snippet("Let's go")
                            .icon(BitmapDescriptorFactory.fromResource(R.drawable.player)))

                        mMap.moveCamera(CameraUpdateFactory.newLatLng(pLocation))

                        for (pokemonCharacterIndex in 0.until(pokemonCharacters.size)) {

                            var pc = pokemonCharacters[pokemonCharacterIndex]
                            if (pc.isKilled == false) {

                                var pcLocation = LatLng(pc.location!!.latitude, pc.location!!.longitude)
                                mMap.addMarker(MarkerOptions()
                                    .position(pcLocation)
                                    .title(pc.titleOfPokemon)
                                    .snippet(pc.message)
                                    .icon(BitmapDescriptorFactory.fromResource(pc.iconOfPokemon!!)))

                                if (playerLocation!!.distanceTo(pc.location) < 1) {

                                    Toast.makeText(this@MapsActivity, "${pc.titleOfPokemon} is eliminated", Toast.LENGTH_SHORT).show()

                                    pc.isKilled = true

                                    pokemonCharacters[pokemonCharacterIndex] = pc
                                }
                            }
                        }
                    }
                    //  Thread.sleep(1000)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            }
        }
    }
}
