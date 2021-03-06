package berlin.htw.augmentedreality.spatialaudio

import android.Manifest
import android.app.Activity
import android.content.IntentSender
import com.google.android.gms.common.api.ResolvableApiException
import android.content.pm.PackageManager
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import com.google.android.gms.location.*

object LocationUtils {
    val LOCATION_REQUEST_CODE = 0
    val ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST = 1

    private var activity: Activity? = null

    fun setup(activity: Activity) {
        if (this.activity != null) return
        this.activity = activity

        val locationRequest = getLocationRequest()

        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        val client = LocationServices.getSettingsClient(activity)
        val task = client.checkLocationSettings(builder.build())

        task.addOnSuccessListener { _ ->
            setupLocationListener()
        }

        task.addOnFailureListener { e ->
            if (e is ResolvableApiException) {
                // Location settings are not satisfied, but this can be fixed
                // by showing the user a dialog.
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    e.startResolutionForResult(activity, LOCATION_REQUEST_CODE)
                } catch (sendEx: IntentSender.SendIntentException) {
                    // Ignore the error.
                }
            }
        }
    }

    private fun getLocationRequest(): LocationRequest {
        val locationRequest = LocationRequest()
        locationRequest.interval = 10000
        locationRequest.fastestInterval = 5000
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        return locationRequest
    }

    fun setupLocationListener() {
        val activity = this.activity ?: return
        // check if we have permission to access fine location
        if (ContextCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    ACCESS_FINE_LOCATION_PERMISSIONS_REQUEST)
            return
        }

        val locationRequest = getLocationRequest()

        val locationCallback = object: LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                val locationResult = locationResult ?: return
                Game.handleOwnLocationChange(locationResult.lastLocation)
            }
        }

        val fusedLocationClient = LocationServices.getFusedLocationProviderClient(activity)
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null /* Looper */)
    }
}