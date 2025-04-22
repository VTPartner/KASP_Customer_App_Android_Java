package com.kapstranspvtltd.kaps.driver_customer_app.activities;

import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.transition.Fade;
import android.util.Log;
import android.view.View;
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
import com.google.android.libraries.places.api.model.Place;
import com.google.android.libraries.places.widget.Autocomplete;
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.snackbar.Snackbar;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.GoodsDriverMapDropLocationActivity;
import com.kapstranspvtltd.kaps.activities.pickup_activities.GoodsPickupMapLocationActivity;
import com.kapstranspvtltd.kaps.databinding.ActivityDriverPickupLocationBinding;
import com.kapstranspvtltd.kaps.databinding.ActivityGoodsPickupMapLocationBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.CustPrograssbar;
import com.kapstranspvtltd.kaps.utility.Pickup;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.Utility;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DriverPickupLocationActivity extends BaseActivity implements OnMapReadyCallback {
    private ActivityDriverPickupLocationBinding binding;
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

    boolean cabService = true;

    Boolean showExactLocation = false;

    ProgressDialog progressDialog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Enable window content transitions
        getWindow().setEnterTransition(new Fade());
        getWindow().setExitTransition(new Fade());

        binding = ActivityDriverPickupLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        categoryId = getIntent().getIntExtra("category_id", 1);
        categoryName = getIntent().getStringExtra("category_name");
        cabService = getIntent().getBooleanExtra("cab",true);

        initializeViews(savedInstanceState);
        setupClickListeners();
    }

    private void initializeViews(Bundle savedInstanceState) {
        custPrograssbar = new CustPrograssbar();
        fusedLocationProviderClient = getFusedLocationProviderClient(this);

        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }

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
            v.setEnabled(false); // Prevent double clicks
            launchPlacesAutocomplete();
            // Re-enable after a delay
            v.postDelayed(() -> v.setEnabled(true), 1000);
        });
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
        } else {
            Task<Location> lastLocation = fusedLocationProviderClient.getLastLocation();
            lastLocation.addOnSuccessListener(this, location1 -> {
                if (location1 != null) {
                    moveCamera(location1.getLatitude(), location1.getLongitude());
                } else {
                    Utility.enableLoc(this);
                    Toast.makeText(this, getString(R.string.location_not_avalible), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

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
            Utility.showProgress(DriverPickupLocationActivity.this);
            addressBundle = new Bundle();
        }

        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {
                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder = new Geocoder(DriverPickupLocationActivity.this, Locale.getDefault());
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
        pickupLabel.setText("Driver Pickup Location");
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
                    showError("Please provide sender contact details");
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

    private void proceedToNextScreen(String name, String mobile) {
        if (latitude != null && longitude != null && addressBundle != null) {
            Pickup pickup = new Pickup();
            pickup.setLat(latitude);
            pickup.setLog(longitude);
            pickup.setAddress(addressBundle.getString("fulladdress"));
            pickup.setRname(name);
            pickup.setRmobile(mobile);

            Intent intent = new Intent(this, DriverDropLocationActivity.class);
            intent.putExtra("pickup", pickup);
            intent.putExtra("category_id", categoryId);
            intent.putExtra("category_name", categoryName);
            intent.putExtra("cab",cabService);
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
}