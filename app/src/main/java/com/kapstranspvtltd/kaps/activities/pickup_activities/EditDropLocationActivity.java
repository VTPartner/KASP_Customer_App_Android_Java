package com.kapstranspvtltd.kaps.activities.pickup_activities;

import static android.os.Build.VERSION.SDK_INT;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.OngoingGoodsDetailActivity;
import com.kapstranspvtltd.kaps.databinding.ActivityEditDropLocationBinding;
import com.kapstranspvtltd.kaps.polygon.Point;
import com.kapstranspvtltd.kaps.utility.Utility;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class EditDropLocationActivity extends AppCompatActivity implements OnMapReadyCallback {
    private ActivityEditDropLocationBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private static final int AUTOCOMPLETE_REQUEST_CODE = 1234;
    private double currentLat, currentLng;
    private String currentAddress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditDropLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Get current location details from intent
        currentLat = getIntent().getDoubleExtra("current_lat", 0);
        currentLng = getIntent().getDoubleExtra("current_lng", 0);
        currentAddress = getIntent().getStringExtra("current_address");

        initializeMap();
        setupViews();
    }

    private void initializeMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
    }

    private void setupViews() {
        binding.edSearch.setText(currentAddress);
        binding.editDropLocation.setOnClickListener(v->{launchPlacesAutocomplete();});
        binding.edSearch.setOnClickListener(v->{launchPlacesAutocomplete();});


        binding.imgBack.setOnClickListener(v -> finish());
        binding.btnConfirm.setOnClickListener(v -> confirmLocation());
        binding.imgCurrent.setOnClickListener(v -> getCurrentLocation(mMap));
    }

    private void launchPlacesAutocomplete() {
        try {
            // Disable the search view temporarily to prevent double clicks
            binding.edSearch.setEnabled(false);

            Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            Intent intent = builder.build(this);
            launcher.launch(intent);

            // Re-enable after a delay
            binding.edSearch.postDelayed(() ->
                    binding.edSearch.setEnabled(true), 1000);
        } catch (Exception e) {
            Log.e("Places", "Error launching autocomplete: " + e.getMessage());
            binding.edSearch.setEnabled(true);
        }
    }

    private void getAddressFromLocation(double lat, double lng, TextView addressText) {
        new AsyncTask<Void, Void, String>() {
            @Override
            protected String doInBackground(Void... voids) {
                try {
                    Geocoder geocoder = new Geocoder(EditDropLocationActivity.this);
                    List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
                    if (!addresses.isEmpty()) {
                        return addresses.get(0).getAddressLine(0);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String address) {
                if (address != null) {
                    addressText.setText(address);
                    binding.txtAddress.setText(address);
                }
            }
        }.execute();
    }

    private void getCurrentLocation(GoogleMap mMap) {

        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        Location location = locationManager.getLastKnownLocation(provider);
        if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            moveMapToLocation(location);
        } else {
            LocationManager systemService = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            if (SDK_INT == Build.VERSION_CODES.R) {
                handleAndroidRCurrentLocation(systemService);
            } else {
                handleOlderVersionsLocation();
            }
        }
    }

    private void handleAndroidRCurrentLocation(LocationManager systemService) {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        if (SDK_INT >= Build.VERSION_CODES.R) {
            systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getMainExecutor(),
                    (Consumer<Location>) locationCallback -> {
                        LatLng coordinate = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 100);
                        mMap.animateCamera(yourLocation);
                    });
        }
    }

    private void handleOlderVersionsLocation() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
        lastLocation.addOnSuccessListener(this, location -> {
            if (location != null) {
                moveMapToLocation(location);
            } else {
                Utility.enableLoc(this);
                Toast.makeText(this, getString(R.string.location_not_avalible), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void moveMapToLocation(Location location) {
        if (mMap == null) return;

        mMap.clear();

        LatLng coordinate = new LatLng(location.getLatitude(), location.getLongitude());

        // Smooth camera movement
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(coordinate, 100);
        mMap.animateCamera(cameraUpdate, new GoogleMap.CancelableCallback() {
            @Override
            public void onFinish() {
                // Animation completed
                binding.locationMarkertext.animate()
                        .alpha(1f)
                        .setDuration(200)
                        .start();
            }

            @Override
            public void onCancel() {
                // Animation canceled
            }
        });
    }


    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    try {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        Log.e("TAG", "Place: " + place.getName() + ", " + place.getId());
                        binding.edSearch.setText(place.getName());
//                        showExactLocation = true;
                        mMap.clear();
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 100);
                        mMap.animateCamera(yourLocation);



                    } catch (Exception e) {
                        e.toString();

                    }


                }
            });

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);

        // Set initial location
        LatLng initialLocation = new LatLng(currentLat, currentLng);
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, 15f));

        // Update address on camera idle
        mMap.setOnCameraIdleListener(() -> {
            LatLng center = mMap.getCameraPosition().target;
            getAddressFromLocation(center.latitude, center.longitude,binding.edSearch);
        });
    }

    private void confirmLocation() {
        LatLng selectedLocation = mMap.getCameraPosition().target;
        String address = binding.txtAddress.getText().toString();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("new_lat", selectedLocation.latitude);
        resultIntent.putExtra("new_lng", selectedLocation.longitude);
        resultIntent.putExtra("new_address", address);
        setResult(RESULT_OK, resultIntent);
        finish();
    }


}