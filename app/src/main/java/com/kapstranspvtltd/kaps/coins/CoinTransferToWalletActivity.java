package com.kapstranspvtltd.kaps.coins;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.databinding.ActivityCoinTransferToWalletBinding;

public class CoinTransferToWalletActivity extends AppCompatActivity {
private ActivityCoinTransferToWalletBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityCoinTransferToWalletBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
    }
}