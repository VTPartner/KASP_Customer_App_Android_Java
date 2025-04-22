package com.kapstranspvtltd.kaps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import androidx.appcompat.app.AppCompatActivity;

import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.IntroActivity;
import com.kapstranspvtltd.kaps.activities.LoginActivity;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.R;

public class SplashScreenActivity extends AppCompatActivity {

    SessionManager sessionManager;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        preferenceManager = new PreferenceManager(this);

        String customerID = preferenceManager.getStringValue("customer_id");
        String customerName = preferenceManager.getStringValue("customer_name");
        Boolean firstRun = preferenceManager.getBooleanValue("firstRun");

        new Handler().postDelayed(() -> {
            // This method will be executed once the timer is over
            // Start your app main activity
            if (preferenceManager != null && firstRun) {
                if (customerID != null && customerID.isEmpty() == false && customerName != null && customerName.isEmpty() == false) {
                    startActivity(new Intent(SplashScreenActivity.this, HomeActivity.class));
                    finish();
                } else {
                    startActivity(new Intent(SplashScreenActivity.this, LoginActivity.class));
                    finish();
                }
            } else {
                Intent i = new Intent(SplashScreenActivity.this, IntroActivity.class);
                startActivity(i);
            }
            finish();

        }, 3000);
    }
}