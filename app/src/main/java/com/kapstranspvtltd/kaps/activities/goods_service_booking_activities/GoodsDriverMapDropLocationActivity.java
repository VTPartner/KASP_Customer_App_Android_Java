package com.kapstranspvtltd.kaps.activities.goods_service_booking_activities;

import static android.os.Build.VERSION.SDK_INT;
import static com.google.android.gms.location.LocationServices.getFusedLocationProviderClient;
import static com.kapstranspvtltd.kaps.utility.SessionManager.dropList;

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
import android.widget.RelativeLayout;
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
import com.kapstranspvtltd.kaps.activities.BaseActivity;
import com.kapstranspvtltd.kaps.adapters.DropLocationAdapter;
import com.kapstranspvtltd.kaps.common_activities.Glb;
import com.kapstranspvtltd.kaps.common_activities.adapters.PlaceSuggestionAdapter;
import com.kapstranspvtltd.kaps.common_activities.adapters.RecentSearchAdapter;
import com.kapstranspvtltd.kaps.common_activities.models.RecentSearch;
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
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityGoodsDriverMapDropLocationBinding;

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

public class GoodsDriverMapDropLocationActivity extends BaseActivity implements OnMapReadyCallback {
    private static  int MAX_DROP_LOCATIONS = 3;
    private ActivityGoodsDriverMapDropLocationBinding binding;
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

    boolean cabService;

    PreferenceManager preferenceManager;

    boolean showExactLocation = false;

    private boolean isInitialLoad = true;

    // Track current drop being edited
    private int currentEditingDropIndex = 0;

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

    private String senderName = "", senderNumber = "";

    boolean proceedToNextScreen = false,showRecentSearchAddress = false;
    private PlacesClient placesClient;

    private static final int REQUEST_EDIT_DROP = 1002;
    private static final int REQUEST_SEARCH_DROP = 1003;
    private DropLocationAdapter dropLocationAdapter;
    private int dropIndex =0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoodsDriverMapDropLocationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Utility.applyEdgeToEdgePadding(binding.getRoot());
        preferenceManager = new PreferenceManager(this);
        if (!Places.isInitialized()) {
            Places.initialize(getApplicationContext(), getString(R.string.google_maps_key), Locale.US);
        }
        placesClient = Places.createClient(this);
        categoryId = getIntent().getIntExtra("category_id", 1);
        categoryName = getIntent().getStringExtra("category_name");
        cabService = getIntent().getBooleanExtra("cab",false);
        pickup = getIntent().getParcelableExtra("pickup");
        if(pickup == null){
            finish();
            return;
        }

        // Initialize dropList with one empty Drop if empty
        if (dropList == null) {
            dropList = new ArrayList<>();
        }
        if (dropList.isEmpty()) {
            Drop initialDrop = new Drop();
            dropList.add(initialDrop);
        }

        // Ensure dropIndex is valid
        if (dropIndex >= dropList.size()) {
            dropIndex = dropList.size() - 1;
        }

        String dropsValue = preferenceManager.getStringValue("multiple_drops", "3");

        try {
            MAX_DROP_LOCATIONS = Integer.parseInt(dropsValue);
        } catch (NumberFormatException e) {
            MAX_DROP_LOCATIONS = 3; // fallback value
        }

        setupDropRecycler();

        // Initialize current editing drop index
        currentEditingDropIndex = dropIndex;

        checkPincodeAndShowDialog(pickup.getLat(),pickup.getLog());
        getSenderDetails();
        initializeViews();
        setupClickListeners();

        setupMap();
        showCustomPlacesSearch();

    }



    private void removeDrop(int position) {
        // Don't remove first drop (position 0)
        if (position == 0) return;

        // Check if position is valid
        if (position >= 0 && position < dropList.size()) {
            dropList.remove(position);
            dropLocationAdapter.setEditingIndex(-1);
            dropLocationAdapter.notifyDataSetChanged();

            // Update dropIndex to a valid value
            if (dropIndex >= dropList.size()) {
                dropIndex = dropList.size() - 1;
            }
            if (dropIndex < 0) dropIndex = 0;

            // Update current editing index
            currentEditingDropIndex = dropIndex;

            // Update UI
            updateDropLocationTitle();

            // Update camera position to show the last remaining drop location
            updateCameraAfterDropRemoval();
        }
    }

    private void editDrop(int position) {
        dropIndex = position;
        currentEditingDropIndex = position;
        showCustomPlacesSearch();
    }

    private void searchDrop(int position) {
        dropIndex = position;
        currentEditingDropIndex = position;
        showCustomPlacesSearch();
    }

    private void addNewDrop() {
        Glb.addStopClicked = true;
        if (dropList.size() >= MAX_DROP_LOCATIONS) {
            showError("Maximum " + MAX_DROP_LOCATIONS + " drop locations allowed");
            return;
        }
        dropList.add(new Drop());

        // Set the new drop as the current editing index
        dropIndex = dropList.size() - 1;
        currentEditingDropIndex = dropIndex;
        dropLocationAdapter.setEditingIndex(dropIndex);
        updateDropLocationTitle();

        dropLocationAdapter.notifyDataSetChanged();
    }


    private void getSenderDetails() {
        String customerName = preferenceManager.getStringValue("customer_name");
        String customerMobile = preferenceManager.getStringValue("customer_mobile_no");
//        String senderName = preferenceManager.getStringValue("sender_name");
//        String senderNumber = preferenceManager.getStringValue("sender_number");

        if(pickup !=null){
            this.senderName = pickup.getRname();
            this.senderNumber = pickup.getRmobile();
        }else{
            this.senderName = customerName.split(" ")[0];
            this.senderNumber = customerMobile;
        }
//        if (senderName == null || senderName.isEmpty() || senderNumber == null || senderNumber.isEmpty()) {
//            this.senderName = customerName.split(" ")[0];
//            this.senderNumber = customerMobile;
//            preferenceManager.saveStringValue("sender_name", customerName);
//            preferenceManager.saveStringValue("sender_number", customerMobile);
//        } else {
//            this.senderName = senderName;
//            this.senderNumber = senderNumber;
//        }
    }

    private void initializeViews() {
        pickup = getIntent().getParcelableExtra("pickup");
        cabService = getIntent().getBooleanExtra("cab",false);
        sessionManager = new SessionManager(this);

        if(pickup == null || pickup.getAddress() == null || pickup.getAddress().isEmpty()){
            Toast.makeText(this,"Please re-confirm your pickup location",Toast.LENGTH_LONG).show();
            return;
        }

        // Set pickup details in the UI
        updatePickupLocationDisplay();

        fusedLocationProviderClient = getFusedLocationProviderClient(this);
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
        binding.btnSend.setOnClickListener(singleClickListener);
        binding.imgCurrunt.setOnClickListener(singleClickListener);

        // Make pickup location clickable
        binding.txtPickupAddress.setOnClickListener(v -> editPickupLocation());


    }

    private void handleClick(View v) {
        if (v.getId() == R.id.img_back) {
            finish();
            overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
        }
        else if (v.getId() == R.id.ed_search) {

            v.setEnabled(false);
            showCustomPlacesSearch();
            v.postDelayed(() -> v.setEnabled(true), 100);

//            launchPlacesAutocomplete();
        }else if (v.getId() == R.id.editDropLocation) {
            v.setEnabled(false);
            showCustomPlacesSearch();
            v.postDelayed(() -> v.setEnabled(true), 100);
//            launchPlacesAutocomplete();
        }

        else if (v.getId() == R.id.btn_send) {
            if(!proceedToNextScreen){
                showSuccess("Service not available for this pickup location");
                return;
            }

            showBottomConfirmDialog();
        } else if (v.getId() == R.id.img_currunt) {
            animateCurrentLocationButton();
            getCurrentLocation();
        } else if (v.getId() == R.id.editPickupLocation) {
            editPickupLocation();
        }

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
//            binding.edSearch.setEnabled(false);

            Autocomplete.IntentBuilder builder = new Autocomplete.IntentBuilder(
                    AutocompleteActivityMode.FULLSCREEN,
                    Arrays.asList(Place.Field.ID, Place.Field.NAME, Place.Field.LAT_LNG));

            Intent intent = builder.build(this);
            launcher.launch(intent);

            // Re-enable after a delay
//            binding.edSearch.postDelayed(() ->
//                    binding.edSearch.setEnabled(true), 1000);
        } catch (Exception e) {
            Log.e("Places", "Error launching autocomplete: " + e.getMessage());
//            binding.edSearch.setEnabled(true);
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
                                    String pincodeId = result.getString("pincode_id");
                                    double outstationDistance = result.getDouble("outstation_distance");
                                    System.out.println("cityID :"+cityId+" outstationDistance::"+outstationDistance);
                                    // Store both values
                                    preferenceManager.saveStringValue("city_id", cityId);
                                    preferenceManager.saveStringValue("pincode_id", pincodeId);
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

    private void showToastError(String message) {
        Toast.makeText(this,message,Toast.LENGTH_SHORT).show();
    }
    private void showBottomConfirmDialog() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View sheetView = getLayoutInflater().inflate(R.layout.customeconfirmdetails, null);
        dialog.setContentView(sheetView);
        String fulladdress = "";
        System.out.println("D_showBottomConfirmDialog:: currentEditingDropIndex::"+currentEditingDropIndex+"  dropList.size()::"+dropList.size());
        System.out.println("addressBundle::"+addressBundle);
        boolean condition = currentEditingDropIndex < dropList.size();
        System.out.println("currentEditingDropIndex < dropList.size()::"+condition);

        // Ensure dropList is initialized and has at least one element
        if (dropList == null) {
            dropList = new ArrayList<>();
        }
        if (dropList.isEmpty()) {
            dropList.add(new Drop());
        }

        // Get address from the current drop being edited
//        if (currentEditingDropIndex >= 0 && currentEditingDropIndex < dropList.size()) {
        if (currentEditingDropIndex >= 0 && currentEditingDropIndex < dropList.size()) {
            Drop currentDrop = dropList.get(currentEditingDropIndex);
            if (currentDrop.getAddress() != null && !currentDrop.getAddress().isEmpty()) {
                fulladdress = currentDrop.getAddress();
            } else if (addressBundle != null) {
                fulladdress = addressBundle.getString("drop_fulladdress");
            }
        } else if (addressBundle != null) {
            fulladdress = addressBundle.getString("drop_fulladdress");
        }

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

        if (cabService) {
            contactLyt.setVisibility(View.GONE);
            title.setVisibility(View.GONE);
            chUser.setVisibility(View.GONE);
        }

        pickupAddress.setText(fulladdress);
        int dropCurrentIndex = currentEditingDropIndex + 1;
        if(currentEditingDropIndex > 0)
            pickupLabel.setText("Drop Location " + dropCurrentIndex);
        else
            pickupLabel.setText("Drop Location");

        if (currentEditingDropIndex < dropList.size()) {
            Drop drop = dropList.get(currentEditingDropIndex);
            if (drop.getRname() != null) edName.setText(drop.getRname());
            if (drop.getRmobile() != null) edMobile.setText(drop.getRmobile());
        }

        chUser.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                String customer_name = preferenceManager.getStringValue("customer_name");
                String customer_mobile_no = preferenceManager.getStringValue("customer_mobile_no");
                edName.setText(customer_name);
                edMobile.setText(customer_mobile_no);
            } else {
                if (currentEditingDropIndex < dropList.size()) {
                    Drop drop = dropList.get(currentEditingDropIndex);
                    if (drop.getRname() != null) edName.setText(drop.getRname());
                    if (drop.getRmobile() != null) edMobile.setText(drop.getRmobile());
                } else {
                    edName.setText("");
                    edMobile.setText("");
                }
            }
        });

        btnConfirm.setOnClickListener(v -> {
            String name = edName.getText().toString().trim();
            String mobile = edMobile.getText().toString().trim();

            if (edMobile.getText().toString().trim().isEmpty() || edName.getText().toString().trim().isEmpty()) {
                showError("Please provide the receiver contact details.");
                return;
            }
            if (!isValidIndianMobile(mobile)) {
                showToastError("Please enter a valid mobile number.");
                return;
            }
            if (addressBundle == null || addressBundle.isEmpty() || TextUtils.isEmpty(addressBundle.getString("drop_fulladdress"))) {
                showError("Please select the drop location first");
                return;
            }

            // Defensive: ensure dropList has at least currentEditingDropIndex element
            while (dropList.size() <= currentEditingDropIndex) {
                dropList.add(new Drop());
            }

            // Update the correct drop in the list
            Drop drop = dropList.get(currentEditingDropIndex);
            drop.setLat(latitude);
            drop.setLog(longitude);
            drop.setAddress(addressBundle.getString("drop_fulladdress"));
            drop.setRname(edName.getText().toString().trim());
            drop.setRmobile(edMobile.getText().toString().trim());
            dropList.set(currentEditingDropIndex, drop);
            dropLocationAdapter.notifyItemChanged(currentEditingDropIndex);

            // Update pickup display if needed
            updatePickupLocationDisplay();

            // Validate all drops have non-empty address AND contact details
            for (int i = 0; i < dropList.size(); i++) {
                Drop d = dropList.get(i);
                if (TextUtils.isEmpty(d.getAddress())) {
                    showError("Please fill address for Drop " + (i + 1) +"\nClick On Edit");
                    dialog.dismiss();
                    return;
                }
                if (TextUtils.isEmpty(d.getRname()) || TextUtils.isEmpty(d.getRmobile())) {
                    showError("Please provide contact details for Drop " + (i + 1));
                    dialog.dismiss();
                    return;
                }
            }

            dialog.dismiss();

            // Proceed to next screen
            startActivity(new Intent(this, ReviewMapActivity.class)
                    .putExtra("cab", cabService)
                    .putExtra("pickup", pickup)
                    .putExtra("drop", drop));
        });

        dialog.show();
    }

    /*private void showBottomConfirmDialog() {
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
        int dropCurrentIndex = dropIndex + 1;
        if(dropIndex > 0)
            pickupLabel.setText("Drop Location " + dropCurrentIndex);
        else
            pickupLabel.setText("Drop Location");

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
if(edMobile.getText().toString().trim().isEmpty() || edName.getText().toString().trim().isEmpty()){
    dialog.dismiss();
    showError("Please provide the receiver contact details.");
    return;
}
            if ( cabService == false && !edMobile.getText().toString().trim().isEmpty() && !TextUtils.isEmpty(edName.getText())) {
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

                int multipleDrops;
                try {
                    multipleDrops = Integer.parseInt(preferenceManager.getStringValue("multiple_drops", "3"));
                } catch (NumberFormatException e) {
                    multipleDrops = 3; // Fallback
                }

                if (dropList.size() == multipleDrops) {
                    // Replace the last drop
                    dropList.set(dropList.size() - 1, drop);
                    Toast.makeText(this, "Last drop location updated", Toast.LENGTH_SHORT).show();
                } else {
                    dropList.add(drop);
                }



                startActivity(new Intent(this, ReviewMapActivity.class)
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

                startActivity(new Intent(this, ReviewMapActivity.class)
                        .putExtra("cab",cabService)
                        .putExtra("pickup", pickup)
                        .putExtra("drop", drop));
            }
        });

        dialog.show();
    }

    */

    private void showSuccess(String message) {
        Snackbar.make(binding.getRoot(), message, Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(R.color.colorPrimary))
                .show();
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
//                googleMap.setPadding(0,0,0,100);
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

        mMap.setPadding(0,0,0,100);

        // Enable hardware acceleration for the map
        View mapView = getSupportFragmentManager().findFragmentById(R.id.map).getView();
        if (mapView != null) {
            mapView.setLayerType(View.LAYER_TYPE_HARDWARE, null);
        }
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        mMap = googleMap;
        updateDropLocationTitle();

        mMap.setOnCameraIdleListener(() -> {
            LatLng latLng = mMap.getCameraPosition().target;
            if (latLng != null && latLng.latitude != 0.0 && latLng.longitude != 0.0) {
                mMap.clear();
                Point point = new Point(latLng.latitude, latLng.longitude);
//                boolean contains = polygon.contains(point);
                if (true) {
                    binding.lvlSorry.setVisibility(View.GONE);
                    binding.btnSend.setVisibility(View.VISIBLE);
                    // Set isInitialLoad to false before getting address
                    isInitialLoad = false;
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

    @Override
    protected void onResume() {
        super.onResume();
        if (mMap != null && currentLocation) {
            getCurrentLocation();
        }

        // Refresh the drop location adapter to reflect any changes from ReviewMapActivity
        refreshDropLocationAdapter();
    }

    private void refreshDropLocationAdapter() {
        if (dropLocationAdapter != null) {
            dropLocationAdapter.notifyDataSetChanged();

            // Update dropIndex to a valid value if needed
            if (dropIndex >= dropList.size()) {
                dropIndex = dropList.size() - 1;
            }
            if (dropIndex < 0) dropIndex = 0;

            // Update current editing drop index
            currentEditingDropIndex = dropIndex;

            // Update the title text
            updateDropLocationTitle();

            // Update camera position to show the current drop location
            updateCameraAfterDropRemoval();
        }
    }

    private void updatePickupLocationDisplay() {
        if (pickup != null) {
            String pickupContact = pickup.getRname() + " • " + pickup.getRmobile();
            binding.txtPickupContact.setText(pickupContact);
            binding.txtPickupAddress.setText(pickup.getAddress());
        }
    }



    private void setupDropRecycler() {
        dropLocationAdapter = new DropLocationAdapter(dropList, MAX_DROP_LOCATIONS, new DropLocationAdapter.DropActionListener() {
            @Override
            public void onEdit(int position) {
                // Set the current drop index and update UI
                dropIndex = position;
                currentEditingDropIndex = position;
                dropLocationAdapter.setEditingIndex(position);
                updateDropLocationTitle();
                Toast.makeText(GoodsDriverMapDropLocationActivity.this, "Editing Drop " + (position + 1), Toast.LENGTH_SHORT).show();
                editDrop(position);
            }
            @Override
            public void onSearch(int position) {
                // Set the current drop index and open search
                dropIndex = position;
                currentEditingDropIndex = position;
                dropLocationAdapter.setEditingIndex(position);
                updateDropLocationTitle();
                searchDrop(position);
            }

            @Override
            public void onRemove(int position) {
                removeDrop(position);
            }

            @Override
            public void onAddStop() {
                // Add new drop
                addNewDrop();
            }
        });

        binding.recyclerDropLocations.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerDropLocations.setAdapter(dropLocationAdapter);
    }



    private void editPickupLocation() {
        // Save current drop list to preserve it during pickup editing
        ArrayList<Drop> currentDrops = new ArrayList<>(dropList);

        // Go to pickup location editing screen
        Glb.showPickup = true;
        Intent intent = new Intent(GoodsDriverMapDropLocationActivity.this, GoodsPickupMapLocationActivity.class);
        intent.putExtra("category_id", categoryId);
        intent.putExtra("category_name", categoryName);
        intent.putExtra("cab", cabService);
        intent.putExtra("preserve_drops", true);
        intent.putParcelableArrayListExtra("current_drops", currentDrops);
        startActivity(intent);
        finish();
    }

    private void searchDropLocation() {
        dropIndex = 0; // For single drop, always use index 0
        showCustomPlacesSearch();
    }

    private void updateDropLocationTitle() {
        int dropCurrentIndex = currentEditingDropIndex + 1;
        if(currentEditingDropIndex > 0)
            binding.dropmaplocation.setText("Drop Location " + dropCurrentIndex);
        else
            binding.dropmaplocation.setText("Drop Location");
    }



    private void updateCameraAfterDropRemoval() {
        if (mMap == null || dropList == null || dropList.isEmpty()) return;

        try {
            // Find the last valid drop location
            Drop lastValidDrop = null;
            for (int i = dropList.size() - 1; i >= 0; i--) {
                Drop drop = dropList.get(i);
                if (drop.getLat() != 0.0 && drop.getLog() != 0.0 && !TextUtils.isEmpty(drop.getAddress())) {
                    lastValidDrop = drop;
                    break;
                }
            }

            if (lastValidDrop != null) {
                // Move camera to the last valid drop location
                LatLng lastLocation = new LatLng(lastValidDrop.getLat(), lastValidDrop.getLog());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(lastLocation, 100);
                mMap.animateCamera(cameraUpdate);

                // Update the current dropIndex to the last valid drop
                for (int i = 0; i < dropList.size(); i++) {
                    if (dropList.get(i) == lastValidDrop) {
                        dropIndex = i;
                        break;
                    }
                }

                // Update the title text
                updateDropLocationTitle();

            } else {
                // If no valid drops, show pickup location
                if (pickup != null) {
                    LatLng pickupLocation = new LatLng(pickup.getLat(), pickup.getLog());
                    CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickupLocation, 15);
                    mMap.animateCamera(cameraUpdate);
                }

                // Reset to first drop
                dropIndex = 0;
                updateDropLocationTitle();
            }

        } catch (Exception e) {
            // Fallback: show pickup location
            if (pickup != null) {
                LatLng pickupLocation = new LatLng(pickup.getLat(), pickup.getLog());
                CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(pickupLocation, 15);
                mMap.animateCamera(cameraUpdate);
            }
        }
    }
    private class GetAddressFromLatLng extends AsyncTask<Double, Void, Bundle> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Utility.showProgress(GoodsDriverMapDropLocationActivity.this);
            addressBundle = new Bundle();
        }

        @Override
        protected Bundle doInBackground(Double... doubles) {
            try {
                latitude = doubles[0];
                longitude = doubles[1];
                Geocoder geocoder = new Geocoder(GoodsDriverMapDropLocationActivity.this, Locale.getDefault());
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

                        // Update the current drop with the address from map
                        if (currentEditingDropIndex >= 0 && currentEditingDropIndex < dropList.size()) {
                            Drop drop = dropList.get(currentEditingDropIndex);
                            drop.setLat(latitude);
                            drop.setLog(longitude);
                            drop.setAddress(address);
                            dropList.set(currentEditingDropIndex, drop);
                            dropLocationAdapter.notifyItemChanged(currentEditingDropIndex);
                        }

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
        if(dropList != null){
            dropList.clear();
        }
        // Clean up Places API
        Places.deinitialize();

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

            // Clean phone number - remove all non-numeric characters
            String cleanPhoneNumber = phoneNumber.replaceAll("[^0-9]", "");

            // Handle different phone number formats
            if (cleanPhoneNumber.startsWith("91") && cleanPhoneNumber.length() == 12) {
                cleanPhoneNumber = cleanPhoneNumber.substring(2);
            } else if (cleanPhoneNumber.startsWith("+91") && cleanPhoneNumber.length() == 13) {
                cleanPhoneNumber = cleanPhoneNumber.substring(3);
            }

            if (edName != null && edMobile != null) {
                edName.setText(name);
                edMobile.setText(cleanPhoneNumber);

                // Update the drop list with contact details
                if (dropIndex >= 0 && dropIndex < dropList.size()) {
                    Drop drop = dropList.get(dropIndex);
                    drop.setRname(name);
                    drop.setRmobile(cleanPhoneNumber);
                    dropList.set(dropIndex, drop);
                    dropLocationAdapter.notifyItemChanged(dropIndex);
                }
            }
        }
    }


    private static final String PREF_RECENT_SEARCHES = "recent_drop_searches_goods";
    private static final int MAX_RECENT_SEARCHES = 10;



    private void showCustomPlacesSearch() {
        BottomSheetDialog dialog = new BottomSheetDialog(this);
        View view = getLayoutInflater().inflate(R.layout.custom_places_search_dialog, null);
        dialog.setContentView(view);

        setupFullscreenDialog(dialog, view);

        EditText searchEditText = view.findViewById(R.id.searchEditText);
        RecyclerView recyclerView = view.findViewById(R.id.recentSearchesRecyclerView);
        TextView recentSearchesTitle = view.findViewById(R.id.recentSearchesTitle);
        int dropCurrentIndex = currentEditingDropIndex + 1;

        if(currentEditingDropIndex > 0)
            searchEditText.setHint("Where is your Drop " + dropCurrentIndex + " Location? ");
        else{
            searchEditText.setHint("Where is your Drop Location? ");
        }
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // Create adapters
//        PlaceSuggestionAdapter suggestionsAdapter = new PlaceSuggestionAdapter(
//                prediction -> fetchPlaceDetails(prediction.getPlaceId(), dialog));
        // When a place is selected from search, it's not initial load
        PlaceSuggestionAdapter suggestionsAdapter = new PlaceSuggestionAdapter(
                prediction -> {
                    isInitialLoad = false; // Reset flag when search is used
                    fetchPlaceDetails(prediction.getPlaceId(), dialog);
                });

        List<RecentSearch> recentSearches = getRecentSearches();
        RecentSearchAdapter recentAdapter = new RecentSearchAdapter(recentSearches,
                search -> {
                    // Ensure dropList is properly initialized
                    if (dropList == null) {
                        dropList = new ArrayList<>();
                    }
                    if (dropList.isEmpty()) {
                        dropList.add(new Drop());
                    }
                    // Update the correct drop in the list
                    System.out.println("D_currentEditingDropIndex::"+currentEditingDropIndex +" dropList.size()::"+dropList.size());
                    if (currentEditingDropIndex >= 0 && currentEditingDropIndex < dropList.size()) {
                        Drop drop = dropList.get(currentEditingDropIndex);
                        drop.setLat(search.getLatitude());
                        drop.setLog(search.getLongitude());
                        drop.setAddress(search.getAddress());
                        dropList.set(currentEditingDropIndex, drop);
                        dropLocationAdapter.notifyItemChanged(currentEditingDropIndex);
                    }
                    dialog.dismiss();
                    moveCamera(search.getLatitude(), search.getLongitude());
                    showBottomConfirmDialog();
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
                                            // Ensure dropList is properly initialized
                                            if (dropList == null) {
                                                dropList = new ArrayList<>();
                                            }
                                            if (dropList.isEmpty()) {
                                                dropList.add(new Drop());
                                            }

                                            System.out.println("D_from_place_search_result_clicked - currentEditingDropIndex::"+currentEditingDropIndex+"  dropList.size()::"+dropList.size());
                                            // Update the correct drop in the list
                                            if (currentEditingDropIndex >= 0 && currentEditingDropIndex < dropList.size()) {
                                                Drop drop = dropList.get(currentEditingDropIndex);
                                                drop.setLat(latLng.latitude);
                                                drop.setLog(latLng.longitude);
                                                drop.setAddress(address);
                                                dropList.set(currentEditingDropIndex, drop);
                                                dropLocationAdapter.notifyItemChanged(currentEditingDropIndex);
                                            }

                                            dialog.dismiss();
                                            moveCamera(latLng.latitude, latLng.longitude);
                                            showBottomConfirmDialog();
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

        // Update current coordinates for the bottom sheet dialog
        latitude = lat;
        longitude = lng;

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