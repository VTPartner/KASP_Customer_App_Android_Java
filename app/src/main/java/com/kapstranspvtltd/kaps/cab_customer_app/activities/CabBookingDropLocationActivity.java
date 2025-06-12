package com.kapstranspvtltd.kaps.cab_customer_app.activities;

import static android.os.Build.VERSION.SDK_INT;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkError;
import com.android.volley.NoConnectionError;
import com.android.volley.Request;
import com.android.volley.ServerError;
import com.android.volley.TimeoutError;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.tasks.Task;
import com.google.android.libraries.places.api.Places;
import com.google.android.libraries.places.api.model.AutocompleteSessionToken;
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.api.net.FetchPlaceRequest;
import com.google.android.libraries.places.api.net.FindAutocompletePredictionsRequest;
import com.google.android.libraries.places.api.net.PlacesClient;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.GoodsDriverMapDropLocationActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.GoodsPickupMapLocationActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.ReviewMapActivity;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.common_activities.adapters.PlaceSuggestionAdapter;
import com.kapstranspvtltd.kaps.common_activities.adapters.RecentSearchAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.RecentSearch;
import com.kapstranspvtltd.kaps.databinding.ActivityCabBookingDropLocationBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityDriverDropLocationBinding;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.DriverDropLocationActivity;
import com.kapstranspvtltd.kaps.driver_customer_app.activities.DriverRouteDetailsActivity;
import com.kapstranspvtltd.kaps.model.User;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.polygon.Point;
import com.kapstranspvtltd.kaps.polygon.Polygon;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.utility.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.function.Consumer;

public class CabBookingDropLocationActivity extends BaseActivity implements OnMapReadyCallback {
    private ActivityCabBookingDropLocationBinding binding;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CustPrograssbar custPrograssbar;
    private SessionManager sessionManager;
    private User user;
    private Polygon polygon;
    private Double latitude;
    private Double longitude;
    private Bundle addressBundle;
    private boolean currentLocation = false;
    private Pickup pickup;

    boolean cabService = true;

    PreferenceManager preferenceManager;

    boolean showExactLocation = false;

    boolean proceedToNextScreen = false;

    ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    Intent data = result.getData();
                    try {
                        Place place = Autocomplete.getPlaceFromIntent(data);
                        Log.e("TAG", "Place: " + place.getName() + ", " + place.getId());
//                        binding.edSearch.setText(place.getName());
//                        showExactLocation = true;
                        mMap.clear();
                        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 100);
                        mMap.animateCamera(yourLocation);

                        Point point = new Point(place.getLatLng().latitude, place.getLatLng().longitude);
                        if (polygon != null) {
//                            boolean contains = polygon.contains(point);
//                            Log.e("resulr", "---> " + contains);
                            if (true) {

                                binding.lvlSorry.setVisibility(View.GONE);
                                binding.btnSend.setVisibility(View.VISIBLE);
                            } else {
                                binding.lvlSorry.setVisibility(View.VISIBLE);
                                binding.btnSend.setVisibility(View.GONE);

                            }
                        } else {
                            binding.btnSend.setVisibility(View.GONE);
                        }

                    } catch (Exception e) {
                        e.toString();

                    }


                }
            });

int categoryId;
String categoryName;

    private PlacesClient placesClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCabBookingDropLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
        placesClient = Places.createClient(this);

        preferenceManager = new PreferenceManager(this);
        categoryId = getIntent().getIntExtra("category_id", 1);
        categoryName = getIntent().getStringExtra("category_name");
        pickup = getIntent().getParcelableExtra("pickup");
        if(pickup == null){
            finish();
            return;
        }

        checkPincodeAndShowDialog(pickup.getLat(),pickup.getLog());
        initializeViews();
        setupClickListeners();

        setupMap();
    }

    private void initializeViews() {
        pickup = getIntent().getParcelableExtra("pickup");
        cabService = getIntent().getBooleanExtra("cab",true);
//        custPrograssbar = new CustPrograssbar();
        sessionManager = new SessionManager(this);
        String address = pickup.getAddress();
        if(address == null || address.isEmpty()){
            Toast.makeText(this,"Please re-confirm your pickup location",Toast.LENGTH_LONG).show();
            return;
        }else{

            binding.pickupLocation.setText(address);
        }
//        user = sessionManager.getUserDetails();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);

//        if (!Places.isInitialized()) {
//            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
//        }
    }

    private void setupClickListeners() {
        // Improve click handling
        View.OnClickListener singleClickListener = new View.OnClickListener() {
            private static final long CLICK_TIME_THRESHOLD = 300;
            private long lastClickTime = 0;

            @Override
            public void onClick(View v) {
                long currentTime = System.currentTimeMillis();
                if (currentTime - lastClickTime > CLICK_TIME_THRESHOLD) {
                    lastClickTime = currentTime;
                    handleClick(v);
                }
            }
        };

        binding.imgBack.setOnClickListener(singleClickListener);
        binding.edSearch.setOnClickListener(singleClickListener);
        binding.btnSend.setOnClickListener(singleClickListener);
        binding.imgCurrunt.setOnClickListener(singleClickListener);
        binding.editPickupLocation.setOnClickListener(singleClickListener);
        binding.editDropLocation.setOnClickListener(singleClickListener);

        // Make search EditText more responsive
        binding.edSearch.setFocusable(false);
        binding.edSearch.setClickable(true);
    }

    private void handleClick(View v) {
        if (v.getId() == R.id.img_back) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        } else if (v.getId() == R.id.ed_search) {
            v.setEnabled(false);
            showCustomPlacesSearch();
            v.postDelayed(() -> v.setEnabled(true), 100);
//            launchPlacesAutocomplete();
        }
        else if (v.getId() == R.id.editDropLocation) {
            v.setEnabled(false);
            showCustomPlacesSearch();
            v.postDelayed(() -> v.setEnabled(true), 100);
//            launchPlacesAutocomplete();
        }
        else if (v.getId() == R.id.btn_send) {
            if(!proceedToNextScreen){
                showError("Service not available for this pickup location");
                return;
            }
            showBottomConfirmDialog();
        } else if (v.getId() == R.id.img_currunt) {
            animateCurrentLocationButton();
            getCurrentLocation();
        }else if (v.getId() == R.id.editPickupLocation) {

            Glb.showPickup = true;
            Intent intent = new Intent(CabBookingDropLocationActivity.this, CabBookingPickupLocationActivity.class);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("cab", categoryId == 2);
            startActivity(intent);
            finish();

        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        finish();
    }

    private void animateCurrentLocationButton() {
        binding.imgCurrunt.animate()
                .rotationBy(360f)
                .setDuration(300)
                .withEndAction(() -> {
                    if (mMap != null) {
                        currentLocation = true;
                        onMapReady(mMap);
                    }
                })
                .start();
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

    private void showBottomConfirmDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        dialog.setContentView(sheetView);
        String fulladdress = "";
        if(addressBundle != null)
            fulladdress = addressBundle.getString("drop_fulladdress");

        TextView pickupLabel = sheetView.findViewById(R.id.labelPickup);
        TextView pickupAddress = sheetView.findViewById(R.id.confirm_pickaddress);

        TextView title = sheetView.findViewById(R.id.textLabel);
        CheckBox chUser = sheetView.findViewById(R.id.ch_user);
        edName = sheetView.findViewById(R.id.ed_name);
        edMobile = sheetView.findViewById(R.id.ed_mobile);
        TextView btnConfirm = sheetView.findViewById(R.id.btn_send);
        LinearLayout contactLyt = sheetView.findViewById(R.id.contactLyt);
        ImageButton btnContacts = sheetView.findViewById(R.id.btnContacts);
        btnContacts.setOnClickListener(v -> checkContactPermission());

        if(cabService){
            contactLyt.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            chUser.setVisibility(View.GONE);
        }

        pickupAddress.setText(fulladdress);
        pickupLabel.setText("Destination Location");

        chUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String customer_name = preferenceManager.getStringValue("customer_name");
                String customer_mobile_no = preferenceManager.getStringValue("customer_mobile_no");
                edName.setText(customer_name);
                edMobile.setText(customer_mobile_no);
            } else {
                edName.setText("");
                edMobile.setText("");
            }
        });

        btnConfirm.setOnClickListener(v -> {

            if ( cabService == false && !TextUtils.isEmpty(edMobile.getText()) && !TextUtils.isEmpty(edName.getText())) {
                if(addressBundle == null || addressBundle.isEmpty()){
                    dialog.dismiss();
                    showError("Please select the drop location first");
                    return;
                }
                dialog.dismiss();
                Drop drop = new Drop();
                drop.setLat(latitude);
                drop.setLog(longitude);
                drop.setAddress(addressBundle.getString("drop_fulladdress"));
                drop.setRname(edName.getText().toString().trim());
                drop.setRmobile(edMobile.getText().toString().trim());
                dropList.add(drop);

                startActivity(new Intent(this, CabRouteDetailsActivity.class)
                        .putExtra("category_id",categoryId)
                        .putExtra("category_name",categoryName)
                        .putExtra("cab",cabService)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));

            }else{
                if(addressBundle == null || addressBundle.isEmpty()){
                    dialog.dismiss();
                    showError("Please select the drop location first");
                    return;
                }
                dialog.dismiss();
                Drop drop = new Drop();
                drop.setLat(latitude);
                drop.setLog(longitude);
                drop.setAddress(addressBundle.getString("drop_fulladdress"));
                drop.setRname("");
                drop.setRmobile("");
                dropList.add(drop);

                startActivity(new Intent(this, CabRouteDetailsActivity.class)
                        .putExtra("category_id",categoryId)
                        .putExtra("category_name",categoryName)
                        .putExtra("cab",cabService)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));
            }
        });

        dialog.show();
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorerror))
                .show();
    }


    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            // Use async map loading
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                configureMap();
                onMapReady(googleMap);
            });
        }
    }

    private void configureMap() {
        if (mMap == null) return;

        // Optimize map settings for smooth performance
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.setBuildingsEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.getUiSettings().setTiltGesturesEnabled(true);
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(10);

        // Enable hardware acceleration for the map
        View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView != null) {
            mapView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;

        mMap.setOnCameraIdleListener(() -> {
            LatLng latLng = mMap.getCameraPosition().target;
            if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                mMap.clear();
                Point point = new Point(latLng.latitude, latLng.longitude);
//                boolean contains = polygon.contains(point);
                if (true) {
                    binding.lvlSorry.setVisibility(View.GONE);
                    binding.btnSend.setVisibility(View.VISIBLE);
                    new GetAddressFromLatLng().executeOnExecutor(
                            AsyncTask.THREAD_POOL_EXECUTOR,
                            latLng.latitude,
                            latLng.longitude
                    );
                } else {
                    binding.lvlSorry.setVisibility(View.VISIBLE);
                    binding.btnSend.setVisibility(View.GONE);
                }
            } else {
                handleAndroidRLocation();
            }
        });
        getCurrentLocation();
        if (currentLocation) {
            getCurrentLocation();
        }
    }

    private void handleAndroidRLocation() {
        if (SDK_INT == Build.VERSION_CODES.R) {
            try {
                LocationManager systemService = (LocationManager) getSystemService(LOCATION_SERVICE);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                systemService.getCurrentLocation(LocationManager.NETWORK_PROVIDER, null, getMainExecutor(),
                        (Consumer<Location>) locationCallback -> {
                            LatLng latLng = new LatLng(locationCallback.getLatitude(), locationCallback.getLongitude());
                            Point point = new Point(latLng.latitude, latLng.longitude);
//                            boolean contains = polygon.contains(point);
                            if (true) {
                                binding.lvlSorry.setVisibility(View.GONE);
                                binding.btnSend.setVisibility(View.VISIBLE);
                                new GetAddressFromLatLng().executeOnExecutor(
                                        AsyncTask.THREAD_POOL_EXECUTOR,
                                        latLng.latitude,
                                        latLng.longitude
                                );
                            } else {
                                binding.lvlSorry.setVisibility(View.VISIBLE);
                                binding.btnSend.setVisibility(View.GONE);
                            }
                        });
            } catch (Exception e) {
                Log.e("error", "->" + e.toString());
            }
        }
    }

    private void getCurrentLocation() {
        currentLocation = false;
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
        showExactLocation = false;
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

    // Add permission handling
    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (mMap != null) {
                    getCurrentLocation();
                }
            } else {
                Toast.makeText(this, "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    // Add lifecycle methods for location updates
    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && currentLocation) {
            getCurrentLocation();
        }
    }

    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showProgress(CabBookingDropLocationActivity.this);
            addressBundle = new Bundle();
        }

        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {
                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder = new Geocoder(CabBookingDropLocationActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    String subLocality = address.getSubLocality();
                    addressBundle.putString(getString(R.string.addressline2),
                            subLocality != null ? subLocality : address.getFeatureName());

                    appendAddressComponent(sb, address.getAddressLine(0));
                    appendAddressComponent(sb, address.getLocality());
                    appendAddressComponent(sb, address.getAdminArea());
                    appendAddressComponent(sb, address.getCountryName());
                    appendAddressComponent(sb, address.getPostalCode());

                    addressBundle.putString("drop_fulladdress", sb.toString().trim());
                    return addressBundle;
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                addressBundle.putBoolean("error", true);
                return addressBundle;
            }
        }

        private void appendAddressComponent(StringBuilder sb, String component) {
            if (component != null) {
                sb.append(component).append(" ");
            }
        }

        @Override
        protected void onPostExecute(Bundle userAddress) {
            super.onPostExecute(userAddress);
            try {
                Utility.hideProgress();
                if (userAddress != null) {
                    String address = userAddress.getString("drop_fulladdress");
                    if (address != null) {
                        binding.txtAddress.setText(address);
//                        if(showExactLocation == false)
//                            binding.edSearch.setText(address);
                        binding.locationMarkertext.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (custPrograssbar != null) {
            custPrograssbar.closePrograssBar();
        }
        if (placesClient != null) {
            Places.deinitialize();
        }

    }

    private static final int CONTACT_PERMISSION_CODE = 100;
    private static final int CONTACT_PICK_CODE = 101;

    EditText edName ;
    EditText edMobile ;
    private void checkContactPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_CONTACTS},
                    CONTACT_PERMISSION_CODE);
        } else {
            pickContact();
        }
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }



    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CONTACT_PICK_CODE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    getContactDetails(uri);
                }
            }
        }
    }

    @SuppressLint("Range")
    private void getContactDetails(Uri uri) {
        String name = "";
        String phoneNumber = "";

        ContentResolver contentResolver = getContentResolver();
        Cursor cursor = contentResolver.query(uri, null, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            String id = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts._ID));
            name = cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

            if (Integer.parseInt(cursor.getString(cursor.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                Cursor phoneCursor = contentResolver.query(
                        ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                        null,
                        ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                        new String[]{id},
                        null
                );

                if (phoneCursor != null && phoneCursor.moveToFirst()) {
                    phoneNumber = phoneCursor.getString(phoneCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                    phoneCursor.close();
                }
            }
            cursor.close();



            if (edName != null && edMobile != null) {
                edName.setText(name);
                edMobile.setText(phoneNumber.replaceAll("[^0-9]", "")); // Remove non-numeric characters
            }
        }
    }

    private void checkPincodeAndShowDialog(double latitude, double longitude) {
        System.out.println("checkPincodeAndShowDialog");


        // Get pincode using Geocoder
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String pincode = addresses.get(0).getPostalCode();
                if (pincode != null && !pincode.isEmpty()) {
                    checkPincodeAvailability(pincode);
                } else {
                    showError("Pickup location availability information is currently unavailable.");

                }
            }
        } catch (Exception e) {
            showError("Error fetching pincode");

        }
    }


    private void checkPincodeAvailability(String pincode) {
        try {
            String url = APIClient.baseUrl + "allowed_pin_code";

            String customerId = preferenceManager.getStringValue("customer_id");
            String fcmToken = preferenceManager.getStringValue("fcm_token");

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("pincode", pincode);
            jsonBody.put("customer_id", customerId);
            jsonBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {

                        try {
                            // Check if results array exists and is not empty
                            if (response.has("results")) {
                                JSONArray results = response.getJSONArray("results");
                                if (results.length() > 0) {
                                    // Get the city_id and outstation_distance from the first result
                                    JSONObject result = results.getJSONObject(0);
                                    String cityId = result.getString("city_id");
                                    double outstationDistance = result.getDouble("outstation_distance");
                                    System.out.println("cityID :"+cityId+" outstationDistance::"+outstationDistance);
                                    // Store both values
                                    preferenceManager.saveStringValue("city_id", cityId);
                                    preferenceManager.saveFloatValue("outstation_distance", (float) outstationDistance);

                                    proceedToNextScreen = true;
                                } else {
                                    showError("No service available in this area");
                                    proceedToNextScreen = false;
                                }
                            } else if (response.has("message")) {
                                // Show error message from API
                                showError(response.getString("message"));
                            } else {
                                showError("Service not available in this area");
                            }
                        } catch (Exception e) {
                            Log.e("PincodeCheck", "Error parsing response: " + e.getMessage());
                            showError("Error processing response");
                        }
                    },
                    error -> {

                        if (error.networkResponse != null && error.networkResponse.statusCode == 404) {
                            showError("Service not available in this area");
                        } else {
                            handleVolleyError(error);
                        }
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            request.setRetryPolicy(new DefaultRetryPolicy(
                    30000,
                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            ));

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (Exception e) {

            Log.e("PincodeCheck", "Error creating request: " + e.getMessage());
            showError("Failed to check pincode availability");
        }
    }

    private void handleVolleyError(VolleyError error) {
        String message;
        if (error.networkResponse != null) {
            switch (error.networkResponse.statusCode) {
                case 400:
                    message = "Invalid request";
                    break;
                case 404:
                    message = "Service not available in this area";
                    break;
                case 405:
                    message = "Invalid request method";
                    break;
                case 500:
                    message = "Server error, please try again later";
                    break;
                default:
                    message = "Error: " + error.networkResponse.statusCode;
                    break;
            }
        } else if (error instanceof TimeoutError || error instanceof NoConnectionError) {
            message = "Connection timeout. Please check your internet connection";
        } else if (error instanceof NetworkError) {
            message = "Network error. Please check your internet connection";
        } else if (error instanceof ServerError) {
            message = "Server error. Please try again later";
        } else {
            message = "An unexpected error occurred";
        }

        showError(message);
        Log.e("VolleyError", "Error: " + message);
    }

    private static final String PREF_RECENT_SEARCHES = "recent_cab_drop_searches";
    private static final int MAX_RECENT_SEARCHES = 10;



    private void showCustomPlacesSearch() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.custom_places_search_dialog, null);
        dialog.setContentView(view);

        setupFullscreenDialog(dialog, view);

        EditText searchEditText = view.findViewById(R.id.searchEditText);
        RecyclerView recyclerView = view.findViewById(R.id.recentSearchesRecyclerView);
        TextView recentSearchesTitle = view.findViewById(R.id.recentSearchesTitle);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create adapters
        PlaceSuggestionAdapter suggestionsAdapter = new PlaceSuggestionAdapter(
                prediction -> fetchPlaceDetails(prediction.getPlaceId(), dialog));

        List<RecentSearch> recentSearches = getRecentSearches();
        RecentSearchAdapter recentAdapter = new RecentSearchAdapter(recentSearches,
                search -> {
                    dialog.dismiss();
                    moveCamera(search.getLatitude(), search.getLongitude());
//                    binding.edSearch.setText(search.getAddress());
                });

        // Show recent searches initially
        if (!recentSearches.isEmpty()) {
            recentSearchesTitle.setVisibility(View.VISIBLE);
            recyclerView.setAdapter(recentAdapter);
        } else {
            recentSearchesTitle.setVisibility(View.GONE);
        }

        // Setup search functionality
        searchEditText.addTextChangedListener(new TextWatcher() {
            private Handler handler = new Handler();
            private Runnable runnable;

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                handler.removeCallbacks(runnable);
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (s.length() > 0) {
                    recentSearchesTitle.setVisibility(View.GONE);
                    recyclerView.setAdapter(suggestionsAdapter);
                    runnable = () -> performPlacesSearch(s.toString(), suggestionsAdapter);
                    handler.postDelayed(runnable, 600);
                } else {
                    // Show recent searches when search text is empty
                    if (!recentSearches.isEmpty()) {
                        recentSearchesTitle.setVisibility(View.VISIBLE);
                        recyclerView.setAdapter(recentAdapter);
                    } else {
                        recentSearchesTitle.setVisibility(View.GONE);
                    }
                    suggestionsAdapter.setPredictions(new ArrayList<>());
                }
            }
        });

        // Auto focus and show keyboard
        searchEditText.requestFocus();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);

        dialog.show();
    }



    private void performPlacesSearch(String query, PlaceSuggestionAdapter adapter) {
        try {
            if (query.length() < 2) {
                adapter.setPredictions(new ArrayList<>());
                return;
            }

            if (!Places.isInitialized()) {
                Places.initialize(getApplicationContext(), getString(R.string.google_maps_key));
                placesClient = Places.createClient(this);
            }

            AutocompleteSessionToken token = AutocompleteSessionToken.newInstance();
            FindAutocompletePredictionsRequest request = FindAutocompletePredictionsRequest.builder()
                    .setSessionToken(token)
                    .setQuery(query)
                    .build();

            placesClient.findAutocompletePredictions(request)
                    .addOnSuccessListener(response -> {
                        if (!response.getAutocompletePredictions().isEmpty()) {
                            adapter.setPredictions(response.getAutocompletePredictions());
                        } else {
                            adapter.setPredictions(new ArrayList<>());
                        }
                    })
                    .addOnFailureListener(exception -> {
                        adapter.setPredictions(new ArrayList<>());
                        Log.e("PlacesAPI", "Error fetching predictions: " + exception.getMessage());
                    });
        } catch (Exception e) {
            Log.e("PlacesAPI", "Error in performPlacesSearch: " + e.getMessage());
            adapter.setPredictions(new ArrayList<>());
        }
    }
    private void setupFullscreenDialog(BottomSheetDialog dialog, View view) {
        BottomSheetBehavior<View> behavior = BottomSheetBehavior.from((View) view.getParent());
        behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
        behavior.setSkipCollapsed(true);

        View bottomSheet = dialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
        if (bottomSheet != null) {
            BottomSheetBehavior.from(bottomSheet).setPeekHeight(
                    Resources.getSystem().getDisplayMetrics().heightPixels);
            ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
            layoutParams.height = Resources.getSystem().getDisplayMetrics().heightPixels;
            bottomSheet.setLayoutParams(layoutParams);
        }
    }


    private void fetchPlaceDetails(String placeId, BottomSheetDialog dialog) {
        List<Place.Field> placeFields = Arrays.asList(
                Place.Field.LAT_LNG,
                Place.Field.ADDRESS,
                Place.Field.NAME,
                Place.Field.BUSINESS_STATUS
        );

        FetchPlaceRequest request = FetchPlaceRequest.builder(placeId, placeFields).build();

        placesClient.fetchPlace(request)
                .addOnSuccessListener(response -> {
                    Place place = response.getPlace();
                    LatLng latLng = place.getLatLng();
                    String address = place.getAddress();
                    String name = place.getName();

                    if (latLng != null) {
                        // Get current location
                        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                            fusedLocationProviderClient.getLastLocation()
                                    .addOnSuccessListener(currentLocation -> {
                                        if (currentLocation != null) {
                                            double distance = calculateDistance(
                                                    currentLocation.getLatitude(),
                                                    currentLocation.getLongitude(),
                                                    latLng.latitude,
                                                    latLng.longitude
                                            );

                                            saveRecentSearch(new RecentSearch(
                                                    name,
                                                    address,
                                                    latLng.latitude,
                                                    latLng.longitude,
                                                    distance
                                            ));

                                            dialog.dismiss();
                                            moveCamera(latLng.latitude, latLng.longitude);
                                            binding.edSearch.setText(name != null ? name : address);
                                        }
                                    });
                        }
                    }
                })
                .addOnFailureListener(exception -> {
                    if (exception instanceof ApiException) {
                        Log.e("PlacesAPI", "Place not found: " + exception.getMessage());
                    }
                });
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        Location location1 = new Location("");
        location1.setLatitude(lat1);
        location1.setLongitude(lon1);

        Location location2 = new Location("");
        location2.setLatitude(lat2);
        location2.setLongitude(lon2);

        float distanceInMeters = location1.distanceTo(location2);
        return distanceInMeters / 1000; // Convert to kilometers
    }
    private void saveRecentSearch(RecentSearch search) {
        List<RecentSearch> searches = getRecentSearches();

        // Remove duplicate if exists
        searches.removeIf(s -> s.getAddress().equals(search.getAddress()));

        // Add new search at the beginning
        searches.add(0, search);

        // Keep only MAX_RECENT_SEARCHES
        if (searches.size() > MAX_RECENT_SEARCHES) {
            searches = searches.subList(0, MAX_RECENT_SEARCHES);
        }

        // Save to SharedPreferences
        Gson gson = new Gson();
        String json = gson.toJson(searches);
        preferenceManager.saveStringValue(PREF_RECENT_SEARCHES, json);
    }

    private List<RecentSearch> getRecentSearches() {
        String json = preferenceManager.getStringValue(PREF_RECENT_SEARCHES);
        if (TextUtils.isEmpty(json)) {
            return new ArrayList<>();
        }

        try {
            Type type = new TypeToken<List<RecentSearch>>(){}.getType();
            return new Gson().fromJson(json, type);
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private void moveCamera(double lat, double lng) {
        if (mMap == null) return;

        mMap.clear();
        showExactLocation = false;
        LatLng coordinate = new LatLng(lat, lng);

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
}