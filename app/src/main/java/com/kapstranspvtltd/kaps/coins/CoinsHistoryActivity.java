package com.kapstranspvtltd.kaps.coins;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.coins.BottomSheets.FilterBottomSheet;
import com.kapstranspvtltd.kaps.coins.adapter.CoinsTransactionAdapter;
import com.kapstranspvtltd.kaps.coins.model.CoinTransaction;
import com.kapstranspvtltd.kaps.databinding.ActivityCoinsHistoryBinding;
import com.kapstranspvtltd.kaps.network.VolleySingleton;
import com.kapstranspvtltd.kaps.retrofit.APIClient;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CoinsHistoryActivity extends AppCompatActivity {
    private ActivityCoinsHistoryBinding binding;
    private CoinsTransactionAdapter adapter;
    private PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoinsHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        preferenceManager = new PreferenceManager(this);
        adapter = new CoinsTransactionAdapter();

        setupRecyclerView();
        setupClickEvents();
        fetchTransactionHistory();
    }

    private void setupRecyclerView() {
        binding.rvTransactions.setAdapter(adapter);
        binding.rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        binding.rvTransactions.addItemDecoration(
                new DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        );
    }

    private void fetchTransactionHistory() {
        String customerId = preferenceManager.getStringValue("customer_id");
        String fcmToken = preferenceManager.getStringValue("fcm_token");

        try {
            JSONObject requestBody = new JSONObject();
            requestBody.put("customer_id", customerId);
            requestBody.put("auth", fcmToken);

            JsonObjectRequest request = new JsonObjectRequest(
                    Request.Method.POST,
                    APIClient.baseUrl + "get_coins_history",
                    requestBody,
                    response -> {
                        JSONArray transactionsArray = response.optJSONArray("transactions");
                        List<CoinTransaction> transactions = new ArrayList<>();

                        if (transactionsArray != null) {
                            int totalEarned = 0;
                            int totalUsed = 0;

                            for (int i = 0; i < transactionsArray.length(); i++) {
                                try {
                                    JSONObject json = transactionsArray.getJSONObject(i);

                                    CoinTransaction transaction = new CoinTransaction(
                                            json.getLong("coin_id"),
                                            json.getInt("coins_earned"),
                                            json.getString("earned_at"),
                                            json.optString("expires_at"),
                                            json.has("order_id") ? json.getLong("order_id") : null,
                                            json.optString("remarks"),
                                            json.optBoolean("is_used", false)
                                    );

                                    transactions.add(transaction);

                                    // Update totals
                                    totalEarned += transaction.getCoinsEarned();
                                    if (transaction.isUsed()) {
                                        totalUsed += transaction.getCoinsEarned();
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }

                            // Update summary
                            binding.tvEarned.setText(String.valueOf(totalEarned));
                            binding.tvUsed.setText(String.valueOf(totalUsed));

                            // Show RecyclerView, hide empty state
                            binding.rvTransactions.setVisibility(View.VISIBLE);
                            binding.emptyStateLayout.setVisibility(View.GONE);
                        }

                        adapter.submitList(transactions);
                    },
                    error -> {
                        if (error instanceof VolleyError && error.networkResponse != null &&
                                error.networkResponse.statusCode == 404) {
                            // Handle 404 - No transactions found
                            binding.rvTransactions.setVisibility(View.GONE);
                            binding.emptyStateLayout.setVisibility(View.VISIBLE);
                            binding.tvEarned.setText("0");
                            binding.tvUsed.setText("0");
                        } else {
                            // Handle other errors
                            Toast.makeText(this, "Error fetching transaction history",
                                    Toast.LENGTH_SHORT).show();
                        }
                        error.printStackTrace();
                    }
            ) {
                @Override
                public Map<String, String> getHeaders() {
                    Map<String, String> headers = new HashMap<>();
                    headers.put("Content-Type", "application/json");
                    return headers;
                }
            };

            VolleySingleton.getInstance(this).addToRequestQueue(request);

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    private void setupClickEvents() {
        binding.ivBack.setOnClickListener(v -> finish());

        binding.filterLyt.setOnClickListener(v -> {
            FilterBottomSheet filterSheet = new FilterBottomSheet();
            filterSheet.setFilterListener(new FilterBottomSheet.FilterListener() {
                @Override
                public void onFilterApplied(String dateRange) {
                    // Implement filter logic here
                }

                @Override
                public void onFilterCleared() {
                    fetchTransactionHistory();
                }
            });
            filterSheet.show(getSupportFragmentManager(), "FilterBottomSheet");
        });
    }
}