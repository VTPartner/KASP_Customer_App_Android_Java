package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
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
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.transition.Fade;
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

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
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
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.common_activities.adapters.PlaceSuggestionAdapter;
import com.kapstranspvtltd.kaps.common_activities.adapters.RecentSearchAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.RecentSearch;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Drop;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.Utility;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityGoodsPickupMapLocationBinding;

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

public class GoodsPickupMapLocationActivity extends BaseActivity implements OnMapReadyCallback {
    private ActivityGoodsPickupMapLocationBinding binding;
    private MapView mMapView;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationProviderClient;
    private CustPrograssbar custPrograssbar;
    private Double latitude;
    private Double longitude;
    private Bundle addressBundle;

    EditText edName ;
    EditText edMobile ;

    PreferenceManager preferenceManager;
    int categoryId;

    String categoryName;

    boolean cabService;

    Boolean showExactLocation = false;

    ProgressDialog progressDialog;

    private String senderName = "", senderNumber = "";

    private PlacesClient placesClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable window content transitions
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        binding = ActivityGoodsPickupMapLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
        placesClient = Places.createClient(this);

        preferenceManager = new PreferenceManager(this);
        categoryId = getIntent().getIntExtra("category_id", 1);
        categoryName = getIntent().getStringExtra("category_name");
        cabService = getIntent().getBooleanExtra("cab",false);

        initializeViews(savedInstanceState);
        setupClickListeners();
        getSenderDetails();
    }

    private void getSenderDetails() {
        String customerName = preferenceManager.getStringValue("customer_name");
        String customerMobile = preferenceManager.getStringValue("customer_mobile_no");
        String senderName = preferenceManager.getStringValue("sender_name");
        String senderNumber = preferenceManager.getStringValue("sender_number");

        if (senderName == null || senderName.isEmpty() || senderNumber == null || senderNumber.isEmpty()) {
            this.senderName = customerName.split(" ")[0];
            this.senderNumber = customerMobile;
            preferenceManager.saveStringValue("sender_name", customerName);
            preferenceManager.saveStringValue("sender_number", customerMobile);
        } else {
            this.senderName = senderName;
            this.senderNumber = senderNumber;
        }
    }

    private void initializeViews(Bundle savedInstanceState) {
        //custPrograssbar = new CustPrograssbar();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);

//        if (!Places.isInitialized()) {
//            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
//        }

        // Add map settings
        mMapView = binding.map;
        mMapView.onCreate(savedInstanceState);
        mMapView.getMapAsync(this);

        // Improve map performance
        mMapView.setLayerType(View.LAYER_TYPE_HARDWARE, null);

        try {
            MapsInitializer.initialize(getApplicationContext(), MapsInitializer.Renderer.LATEST, renderer -> {
                // Map is ready with latest renderer
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void setupClickListeners() {
        binding.imgBack.setOnClickListener(v -> finish());
        binding.edSearch.setFocusable(false);
        binding.edSearch.setClickable(true);

        binding.edSearch.setOnClickListener(v -> {
            v.setEnabled(false);
            showCustomPlacesSearch();
            v.postDelayed(() -> v.setEnabled(true), 100);
        });

//        binding.edSearch.setOnClickListener(v -> {
//            v.setEnabled(false); // Prevent double clicks
//            launchPlacesAutocomplete();
//            // Re-enable after a delay
//            v.postDelayed(() -> v.setEnabled(true), 1000);
//        });

        binding.btnSend.setOnClickListener(v -> handleConfirmClick());
        binding.txtAddress.setOnClickListener(v -> {
            v.setEnabled(false);
            handleAddressClick();
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });
        binding.imgCurrunt.setOnClickListener(v -> {
            v.animate()
                    .rotationBy(360f)
                    .setDuration(500)
                    .start();
            if (mMap != null) {
                onMapReady(mMap);
            }

        });

    }

    private void launchPlacesAutocomplete() {
        Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        Intent intent = builder.build(this);
        launcher.launch(intent);
    }

    private void handleConfirmClick() {
        showBottomConfirmDialog();
    }

    private void handleAddressClick() {
        Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                AutocompleteActivityMode.FULLSCREEN,
                Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));
        Intent intent = builder.build(this);
        launcher.launch(intent);
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        // Improve map settings
        mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(false);
        mMap.setBuildingsEnabled(true);
        mMap.setMaxZoomPreference(20);
        mMap.setMinZoomPreference(10);

        // Smooth camera movements
        mMap.setOnCameraMoveStartedListener(reason -> {
            if (reason == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                binding.locationMarkertext.animate()
                        .translationY(-50)
                        .alpha(0.5f)
                        .setDuration(300);
            }
        });

        mMap.setOnCameraIdleListener(() -> {
            binding.locationMarkertext.animate()
                    .translationY(0)
                    .alpha(1.0f)
                    .setDuration(300);

            LatLng latLng = mMap.getCameraPosition().target;
            if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                mMap.clear();
                binding.lvlSorry.setVisibility(View.GONE);
                binding.btnSend.setVisibility(View.VISIBLE);
                GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng.latitude, latLng.longitude);
            }
        });

        setupMapListeners();
        getCurrentLocation();
    }

    private void setupMapListeners() {
        mMap.setOnCameraIdleListener(() -> {
            LatLng latLng = mMap.getCameraPosition().target;
            if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                mMap.clear();
                binding.lvlSorry.setVisibility(View.GONE);
                binding.btnSend.setVisibility(View.VISIBLE);
                GetAddressFromLatLng asyncTask = new GetAddressFromLatLng();
                asyncTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, latLng.latitude, latLng.longitude);
            }
        });
    }

    @SuppressLint("MissingPermission")
    private void getCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String provider = locationManager.getBestProvider(criteria, true);
        Location location = locationManager.getLastKnownLocation(provider);

        if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
            moveCamera(location.getLatitude(), location.getLongitude());
            if (Glb.showPickup == false) {
                handleAutoProceed(location.getLatitude(), location.getLongitude());
            }
        } else {
            Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(this, location1 -> {
                if (location1 != null) {
                    moveCamera(location1.getLatitude(), location1.getLongitude());
                    if (Glb.showPickup == false) {
                        handleAutoProceed(location1.getLatitude(), location1.getLongitude());
                    }
                } else {
                    Utility.enableLoc(this);
                    Toast.makeText(this, getString(R.string.location_not_avalible), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    private void handleAutoProceed(double lat, double lng) {
        // Get address from coordinates
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(lat, lng, 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                    sb.append(address.getAddressLine(i)).append(" ");
                }
                String fullAddress = sb.toString().trim();

                // Create pickup object
                Pickup pickup = new Pickup();
                pickup.setLat(lat);
                pickup.setLog(lng);
                pickup.setAddress(fullAddress);
                pickup.setRname(senderName);
                pickup.setRmobile(senderNumber);

                // Proceed to next screen
                Intent intent = new Intent(this, GoodsDriverMapDropLocationActivity.class);
                intent.putExtra("pickup", pickup);
                intent.putExtra("category_id", categoryId);
                intent.putExtra("category_name", categoryName);
                intent.putExtra("cab", cabService);
                startActivity(intent);
                finish();
            }
        } catch (IOException e) {
            e.printStackTrace();
            showError("Error getting address from location");
        }
    }

//    @SuppressLint("MissingPermission")
//    private void getCurrentLocation() {
//        LocationManager locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
//        Criteria criteria = new Criteria();
//        String provider = locationManager.getBestProvider(criteria, true);
//        Location location = locationManager.getLastKnownLocation(provider);
//
//        if (location != null && location.getLatitude() != 0.0 && location.getLongitude() != 0.0) {
//            moveCamera(location.getLatitude(), location.getLongitude());
//        } else {
//            Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
//            lastLocation.addOnSuccessListener(this, location1 -> {
//                if (location1 != null) {
//                    moveCamera(location1.getLatitude(), location1.getLongitude());
//                } else {
//                    Utility.enableLoc(this);
//                    Toast.makeText(this, getString(R.string.location_not_avalible), Toast.LENGTH_SHORT).show();
//                }
//            });
//        }
//    }

    private void moveCamera(double lat, double lng) {
        LatLng coordinate = new LatLng(lat, lng);
        showExactLocation = false;

        // Smooth camera animation
        CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(coordinate, 100);
        mMap.animateCamera(yourLocation, 1000, null);
    }

    private final ActivityResultLauncher<Intent> launcher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    try {
                        Place place = Autocomplete.getPlaceFromIntent(result.getData());

                        if (mMap != null) {
//                            showExactLocation = true;
                            binding.edSearch.setText(place.getName());
                            mMap.clear();
                            CameraUpdate yourLocation = CameraUpdateFactory.newLatLngZoom(place.getLatLng(), 100);
                            mMap.animateCamera(yourLocation);
                        }
                    } catch (Exception e) {
                        Log.e("Error", "Place selection error: " + e.getMessage());
                    }
                }
            });

    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showProgress(GoodsPickupMapLocationActivity.this);
            addressBundle = new Bundle();
        }

        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {
                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder = new Geocoder(GoodsPickupMapLocationActivity.this, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    StringBuilder sb = new StringBuilder();

                    // Build full address
                    for (int i = 0; i <= address.getMaxAddressLineIndex(); i++) {
                        sb.append(address.getAddressLine(i)).append(" ");
                    }

                    addressBundle.putString("fulladdress", sb.toString().trim());
                    return addressBundle;
                }
                return null;
            } catch (IOException e) {
                e.printStackTrace();
                addressBundle.putBoolean("error", true);
                return addressBundle;
            }
        }

        @Override
        protected void onPostExecute(Bundle userAddress) {
            super.onPostExecute(userAddress);
            Utility.hideProgress();
            try {
                if (userAddress != null) {
                    String address = userAddress.getString("fulladdress");
                    if (address != null) {
                        binding.txtAddress.setText(address);
                        if(showExactLocation == false)
                            binding.edSearch.setText(address);
                        binding.locationMarkertext.setVisibility(View.VISIBLE);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void checkPincodeAndShowDialog(double latitude, double longitude,String name,String mobile) {
        System.out.println("checkPincodeAndShowDialog");
        showLoading(true);

        // Get pincode using Geocoder
        Geocoder geocoder = new Geocoder(this, Locale.getDefault());
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            if (addresses != null && !addresses.isEmpty()) {
                String pincode = addresses.get(0).getPostalCode();
                if (pincode != null && !pincode.isEmpty()) {
                    checkPincodeAvailability(pincode,name,mobile);
                } else {
                    showError("Pickup location availability information is currently unavailable.");
                    showLoading(false);
                }
            }
        } catch (Exception e) {
            showError("Error fetching pincode");
            showLoading(false);
        }
    }

/*
    private void checkPincodeAvailability(String pincode,String name,String mobile) {
        try {
            String url = APIClient.baseUrl + "allowed_pin_code";

            JSONObject jsonBody = new JSONObject();
            jsonBody.put("pincode", pincode);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    url,
                    jsonBody,
                    response -> {
                        showLoading(false);
                        try {
                            // Check if results array exists and is not empty
                            if (response.has("results")) {
                                JSONArray results = response.getJSONArray("results");
                                if (results.length() > 0) {
                                    // Get the city_id from the first result
                                    JSONObject result = results.getJSONObject(0);
                                    System.out.println("result::"+result);
                                    String cityId = result.getString("city_id");

                                    // Store cityId if needed
                                    preferenceManager.saveStringValue("city_id", cityId);
                                    proceedToNextScreen(name, mobile);

                                } else {
                                    showError("No service available in this area");
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
                        showLoading(false);
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
            showLoading(false);
            Log.e("PincodeCheck", "Error creating request: " + e.getMessage());
            showError("Failed to check pincode availability");
        }
    }
*/

    private void checkPincodeAvailability(String pincode, String name, String mobile) {
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
                        showLoading(false);
                        try {
                            // Check if results array exists and is not empty
                            if (response.has("results")) {
                                JSONArray results = response.getJSONArray("results");
                                if (results.length() > 0) {
                                    // Get the city_id and outstation_distance from the first result
                                    JSONObject result = results.getJSONObject(0);
                                    String cityId = result.getString("city_id");
                                    String pincodeId = result.getString("pincode_id");
                                    double outstationDistance = result.getDouble("outstation_distance");
                                    System.out.println("cityID :"+cityId+" outstationDistance::"+outstationDistance);
                                    // Store both values
                                    preferenceManager.saveStringValue("city_id", cityId);
                                    preferenceManager.saveStringValue("pincode_id", pincodeId);
                                    preferenceManager.saveFloatValue("outstation_distance", (float) outstationDistance);

                                    proceedToNextScreen(name, mobile);
                                } else {
                                    showError("No service available in this area");
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
                        showLoading(false);
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
            showLoading(false);
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

    private static final int CONTACT_PERMISSION_CODE = 100;
    private static final int CONTACT_PICK_CODE = 101;
    private void showBottomConfirmDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        dialog.setContentView(sheetView);

        String fulladdress = addressBundle.getString("fulladdress");

        TextView pickupLabel = sheetView.findViewById(R.id.labelPickup);
        TextView pickupAddress = sheetView.findViewById(R.id.confirm_pickaddress);

        TextView title = sheetView.findViewById(R.id.textLabel);
        CheckBox chUser = sheetView.findViewById(R.id.ch_user);
        edName = sheetView.findViewById(R.id.ed_name);
        edMobile = sheetView.findViewById(R.id.ed_mobile);
        TextView btnConfirm = sheetView.findViewById(R.id.btn_send);
        LinearLayout contactLayout = sheetView.findViewById(R.id.contactLyt);
        ImageButton btnContacts = sheetView.findViewById(R.id.btnContacts);
        btnContacts.setOnClickListener(v -> checkContactPermission());
if(cabService){
    contactLayout.setVisibility(View.GONE);
    chUser.setVisibility(View.GONE);
    title.setVisibility(View.GONE);
}
        pickupAddress.setText(fulladdress);
        pickupLabel.setText("Pickup Location");
        title.setText("Driver will contact at this pickup location");

        chUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String customerName = preferenceManager.getStringValue("customer_name");
                String customerMobile = preferenceManager.getStringValue("customer_mobile_no");
                edName.setText(customerName);
                edMobile.setText(customerMobile);
            } else {
                edName.setText("");
                edMobile.setText("");
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String name = edName.getText().toString().trim();
            String mobile = edMobile.getText().toString().trim();
            if(cabService == false){
                if(name == null || name.isEmpty() || mobile == null || mobile.isEmpty()){
                    dialog.dismiss();
                    showError("Please provide sender contact details");
                    return;
                }
                if (!isValidIndianMobile(mobile)) {
                    showToastError("Please enter a valid mobile number.");
                    return;
                }
            }
         dialog.dismiss();
//                proceedToNextScreen(name,mobile);
                checkPincodeAndShowDialog(latitude,longitude,name,mobile);

        });

        dialog.show();
    }

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


    private boolean isValidIndianMobile(String mobile) {
        // Remove spaces and dashes
        mobile = mobile.replaceAll("[\\s\\-]", "");

        // +91XXXXXXXXXX
        if (mobile.startsWith("+91") && mobile.length() == 13) {
            mobile = mobile.substring(3);
        } else if (mobile.startsWith("91") && mobile.length() == 12) {
            mobile = mobile.substring(2);
        }

        // Now mobile should be 10 digits and start with 6-9
        return mobile.matches("^[6-9]\\d{9}$");
    }

    private void pickContact() {
        Intent intent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        startActivityForResult(intent, CONTACT_PICK_CODE);
    }

    private void showLoading(boolean show) {
        if (show) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Please wait...");
            progressDialog.setCancelable(false);
            progressDialog.show();
        } else if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

    private boolean validateInputs(String name, String mobile) {
        if (TextUtils.isEmpty(name)) {
            showError("Please enter name");
            return false;
        }

        if (TextUtils.isEmpty(mobile)) {
            showError("Please enter mobile number");
            return false;
        }

        if (mobile.length() != 10) {
            showError("Please enter valid mobile number");
            return false;
        }

        return true;
    }

    private void showError(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorerror))
                .show();
    }

    private void showToastError(String message) {
       Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }

    private void proceedToNextScreen(String name, String mobile) {
        if (latitude != null && longitude != null && addressBundle != null) {
            Pickup pickup = new Pickup();
            pickup.setLat(latitude);
            pickup.setLog(longitude);
            pickup.setAddress(addressBundle.getString("fulladdress"));
            pickup.setRname(name);
            pickup.setRmobile(mobile);

            Intent intent = new Intent(this, GoodsDriverMapDropLocationActivity.class);
            intent.putExtra("pickup", pickup);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("cab", cabService);
            
            // Check if we need to preserve drops
            boolean preserveDrops = getIntent().getBooleanExtra("preserve_drops", false);
            if (preserveDrops) {
                ArrayList<Drop> preservedDrops = getIntent().getParcelableArrayListExtra("current_drops");
                if (preservedDrops != null) {
                    // Restore the preserved drops
                    dropList.clear();
                    dropList.addAll(preservedDrops);
                }
            }
            
            startActivity(intent);
        } else {
            showError("Location information is missing");
        }
    }

    // Lifecycle methods
    @Override
    protected void onResume() {
        super.onResume();
        mMapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mMapView.onPause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mMapView.onDestroy();
        if(dropList != null){
            dropList.clear();
        }
        Glb.addStopClicked = false;
        Glb.showPickup = false;
        // Clean up Places API
        Places.deinitialize();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mMapView.onLowMemory();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CONTACT_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                pickContact();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
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

    private static final String PREF_RECENT_SEARCHES = "recent_searches_goods";
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
                    binding.edSearch.setText(search.getAddress());
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

    private static final String DISTANCE_MATRIX_API_URL = "https://maps.googleapis.com/maps/api/distancematrix/json";

    // Add this method to calculate distance using Distance Matrix API
    private void getDistanceFromDistanceMatrix(double originLat, double originLng,
                                               double destLat, double destLng,
                                               DistanceCallback callback) {
        String url = String.format(Locale.US,
                "%s?origins=%f,%f&destinations=%f,%f&mode=driving&key=%s",
                DISTANCE_MATRIX_API_URL,
                originLat, originLng,
                destLat, destLng,
                getString(R.string.google_maps_key));

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    try {
                        JSONArray rows = response.getJSONArray("rows");
                        JSONObject row = rows.getJSONObject(0);
                        JSONArray elements = row.getJSONArray("elements");
                        JSONObject element = elements.getJSONObject(0);
                        JSONObject distance = element.getJSONObject("distance");
                        int distanceInMeters = distance.getInt("value");
                        String distanceText = distance.getString("text");
                        callback.onDistanceCalculated(distanceInMeters / 1000.0, distanceText);
                    } catch (Exception e) {
                        Log.e("DistanceMatrix", "Error parsing response: " + e.getMessage());
                        callback.onError("Error calculating distance");
                    }
                },
                error -> callback.onError("Error fetching distance")
        );

        VolleySingleton.getInstance(this).addToRequestQueue(request);
    }

    private interface DistanceCallback {
        void onDistanceCalculated(double distanceKm, String distanceText);
        void onError(String message);
    }

}