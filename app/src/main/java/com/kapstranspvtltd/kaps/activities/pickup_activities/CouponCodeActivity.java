package com.kapstranspvtltd.kaps.activities.pickup_activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.adapters.CouponAdapter;
import com.kapstranspvtltd.kaps.model.Coupon;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CouponCodeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    PreferenceManager preferenceManager;

    private boolean isCouponApplied = false;
    private CouponAdapter couponAdapter;
    private List<Coupon> couponList;
    private RequestQueue requestQueue;

    int categoryId;

    private ShimmerFrameLayout shimmerLayout;
    private LinearLayout noCouponsLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_coupon_code);
        preferenceManager = new PreferenceManager(this);
        // Initialize views
        recyclerView = findViewById(R.id.recycler_view);
        shimmerLayout = findViewById(R.id.shimmerLayout);
        noCouponsLayout = findViewById(R.id.noCouponsLayout);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        categoryId = getIntent().getIntExtra("category_id", 1);
        couponList = new ArrayList<>();
        requestQueue = Volley.newRequestQueue(this);

        ImageView imgBack = findViewById(R.id.img_back);
        imgBack.setOnClickListener(v -> finish());

        // Start loading
        showLoading();
        fetchCoupons();
    }

    private void showLoading() {
        shimmerLayout.setVisibility(View.VISIBLE);
        shimmerLayout.startShimmer();
        recyclerView.setVisibility(View.GONE);
        noCouponsLayout.setVisibility(View.GONE);
    }

    private void showNoCoupons() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.GONE);
        noCouponsLayout.setVisibility(View.VISIBLE);
    }

    private void showCoupons() {
        shimmerLayout.stopShimmer();
        shimmerLayout.setVisibility(View.GONE);
        recyclerView.setVisibility(View.VISIBLE);
        noCouponsLayout.setVisibility(View.GONE);
    }

    private void fetchCoupons() {
        String url = APIClient.baseUrl + "all_coupons";
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        JSONObject postData = new JSONObject();
        try {
            postData.put("category_id", categoryId);
            postData.put("customer_id", customerId);
            postData.put("auth", fcmToken);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(
                Request.Method.POST,
                url,
                postData,
                response -> {
                    try {
                        JSONArray couponsArray = response.getJSONArray("coupons_details");
                        for (int i = 0; i < couponsArray.length(); i++) {
                            JSONObject couponObject = couponsArray.getJSONObject(i);
                            int status = couponObject.getInt("status");
                            if(status == 1) {
                                Coupon coupon = new Coupon(
                                        couponObject.getInt("coupon_id"),
                                        couponObject.getString("coupon_code"),
                                        couponObject.getString("coupon_title"),
                                        couponObject.getString("coupon_description"),
                                        couponObject.getInt("category_id"),
                                        couponObject.getString("discount_type"),
                                        couponObject.getDouble("discount_value"),
                                        couponObject.getDouble("min_order_value"),
                                        couponObject.getDouble("max_discount"),
                                        couponObject.getInt("usage_limit"),
                                        couponObject.getInt("used_count"),
                                        couponObject.getString("start_date"),
                                        couponObject.getString("end_date"),
                                        couponObject.getInt("status"),
                                        couponObject.getDouble("time_created_at")
                                );
                                couponList.add(coupon);
                            }
                        }

                        if (couponList.isEmpty()) {
                            showNoCoupons();
                        } else {
                            couponAdapter = new CouponAdapter(couponList, coupon -> {
                                if (!isCouponApplied) {
                                    Intent resultIntent = new Intent();
                                    resultIntent.putExtra("selected_coupon", coupon);
                                    setResult(RESULT_OK, resultIntent);
                                    isCouponApplied = true;
                                    finish();
                                }
                            });

                            couponAdapter.setCouponApplied(isCouponApplied);
                            recyclerView.setAdapter(couponAdapter);
                            showCoupons();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        showNoCoupons();
                        Toast.makeText(CouponCodeActivity.this, "Error parsing data", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {
                    showNoCoupons();
//                    Toast.makeText(CouponCodeActivity.this, "Failed to load coupons", Toast.LENGTH_SHORT).show();
                }
        );

        requestQueue.add(jsonObjectRequest);
    }
}