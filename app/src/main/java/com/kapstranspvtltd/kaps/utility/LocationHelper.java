package com.kapstranspvtltd.kaps.utility;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationHelper {
    private final Context context;
    private final FusedLocationProviderClient fusedLocationClient;
    private final Geocoder geocoder;

    public LocationHelper(Context context) {
        this.context = context;
        this.fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    public void getCurrentLocation(LocationCallbackListener callbackListener) {
        LocationRequest locationRequest = new LocationRequest()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(5000)
                .setFastestInterval(2000);

        LocationCallback locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null || locationResult.getLastLocation() == null) {
                    callbackListener.onFailure(new Exception("Location not found"));
                    return;
                }
                Location location = locationResult.getLastLocation();
                fusedLocationClient.removeLocationUpdates(this);

                try {
                    List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                    String pinCode = "0";
                    String address = "";
                    if (addresses != null && !addresses.isEmpty()) {
                        pinCode = addresses.get(0).getPostalCode();
                        address = addresses.get(0).getAddressLine(0);
                    }
                    callbackListener.onSuccess(new LocationDetails(location.getLatitude(), location.getLongitude(), address, pinCode));
                } catch (Exception e) {
                    callbackListener.onFailure(e);
                }
            }
        };

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());
    }

    public interface LocationCallbackListener {
        void onSuccess(LocationDetails locationDetails);
        void onFailure(Exception e);
    }

    public static class LocationDetails {
        private final double latitude;
        private final double longitude;
        private final String address;
        private final String postalCode;

        public LocationDetails(double latitude, double longitude, String address, String postalCode) {
            this.latitude = latitude;
            this.longitude = longitude;
            this.address = address;
            this.postalCode = postalCode;
        }

        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getAddress() { return address; }
        public String getPostalCode() { return postalCode; }
    }
}
