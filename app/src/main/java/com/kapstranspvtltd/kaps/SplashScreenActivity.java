package com.kapstranspvtltd.kaps;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.activities.HomeActivity;
import com.kapstranspvtltd.kaps.activities.IntroActivity;
import com.kapstranspvtltd.kaps.activities.LoginActivity;
import com.kapstranspvtltd.kaps.model.AppContent;
import com.kapstranspvtltd.kaps.utility.AppContentManager;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.utility.Utility;

public class SplashScreenActivity extends AppCompatActivity {

    SessionManager sessionManager;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        Utility.applyEdgeToEdgePadding(findViewById(R.id.splashParentId));

        preferenceManager = new PreferenceManager(this);

        // Load splash screen content (already fetched in MyApplication)
        loadSplashScreenContent();
        proceedToNextScreen();
    }

    private void loadSplashScreenContent() {
        AppContent splashContent = AppContentManager.getInstance(this)
                .getFirstContentForScreen("customer_splash_screen");
        System.out.println("splashContent::"+splashContent);
        if (splashContent != null && !splashContent.getImageUrl().equals("NA")) {
            ImageView splashLogo = findViewById(R.id.splash_logo);
            
            if (splashLogo != null) {
                if (splashContent.getImageUrl().startsWith("http")) {
                    Glide.with(this)
                            .load(splashContent.getImageUrl())
                            .placeholder(R.drawable.logo)
                            .error(R.drawable.logo)
                            .into(splashLogo);
                } else {
                    try {
                        int resourceId = getResources().getIdentifier(
                                splashContent.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                getPackageName()
                        );
                        if (resourceId != 0) {
                            splashLogo.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        splashLogo.setImageResource(R.drawable.logo);
                    }
                }
            }
        }
    }

    private void proceedToNextScreen() {
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