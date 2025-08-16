package com.kapstranspvtltd.kaps.activities;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.kapstranspvtltd.kaps.fragments.Info1Fragment;
import com.kapstranspvtltd.kaps.fragments.Info2Fragment;
import com.kapstranspvtltd.kaps.fragments.Info3Fragment;
import com.kapstranspvtltd.kaps.model.AppContent;
import com.kapstranspvtltd.kaps.utility.AppContentManager;
import com.kapstranspvtltd.kaps.utility.PreferenceManager;
import com.kapstranspvtltd.kaps.utility.SessionManager;
import com.kapstranspvtltd.kaps.databinding.ActivityIntroBinding;
import com.kapstranspvtltd.kaps.utility.Utility;

import java.util.List;
import java.util.Map;


public class IntroActivity extends AppCompatActivity {
    private ActivityIntroBinding binding;
    private SessionManager sessionManager;
    public static ViewPager vpPager;
    private MyPagerAdapter adapterViewPager;

    PreferenceManager preferenceManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        Utility.applyEdgeToEdgePadding(binding.getRoot());

        // Initialize ViewPager
        preferenceManager = new PreferenceManager(this);
        vpPager = binding.vpPager;
        sessionManager = new SessionManager(IntroActivity.this);
        adapterViewPager = new MyPagerAdapter(getSupportFragmentManager());

        // Check session and redirect if needed
        if (sessionManager.getBooleanData(SessionManager.login) &&
                sessionManager.getBooleanData(SessionManager.intro)) {
            preferenceManager.saveBooleanValue("firstRun",true);
            startActivity(new Intent(IntroActivity.this, LoginActivity.class));
            finish();
            return;
        }

        // Load app content and setup ViewPager
        loadAppContent();
    }

    private void loadAppContent() {
        AppContentManager.getInstance(this).fetchAppContent(this, new AppContentManager.AppContentCallback() {
            @Override
            public void onSuccess(Map<String, List<AppContent>> content) {
                // Setup ViewPager and DotsIndicator
                vpPager.setAdapter(adapterViewPager);
                binding.dotsIndicator.setViewPager(vpPager);

                // Setup click listener
                binding.lvlPhone.setOnClickListener(v -> {
                    preferenceManager.saveBooleanValue("firstRun",true);
                    startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                });

                // Setup page change listener
                vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                                               int positionOffsetPixels) {
                        Log.e("data", "jsadlj");
                    }

                    @Override
                    public void onPageSelected(int position) {
                        // Handle page selection if needed
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        Log.e("sjlkj", "sjahdal");
                    }
                });
            }

            @Override
            public void onError(String error) {
                Log.e("IntroActivity", "Error loading app content: " + error);
                // Setup with default content
                vpPager.setAdapter(adapterViewPager);
                binding.dotsIndicator.setViewPager(vpPager);

                // Setup click listener
                binding.lvlPhone.setOnClickListener(v -> {
                    preferenceManager.saveBooleanValue("firstRun",true);
                    startActivity(new Intent(IntroActivity.this, LoginActivity.class));
                });

                // Setup page change listener
                vpPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                    @Override
                    public void onPageScrolled(int position, float positionOffset,
                                               int positionOffsetPixels) {
                        Log.e("data", "jsadlj");
                    }

                    @Override
                    public void onPageSelected(int position) {
                        // Handle page selection if needed
                    }

                    @Override
                    public void onPageScrollStateChanged(int state) {
                        Log.e("sjlkj", "sjahdal");
                    }
                });
            }
        });
    }

    public class MyPagerAdapter extends FragmentPagerAdapter {
        private static final int NUM_ITEMS = 3;

        public MyPagerAdapter(FragmentManager fragmentManager) {
            super(fragmentManager, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT);
        }

        @Override
        public int getCount() {
            return NUM_ITEMS;
        }

        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    return Info1Fragment.newInstance();
                case 1:
                    return Info2Fragment.newInstance();
                case 2:
                    return Info3Fragment.newInstance();
                default:
                    return null;
            }
        }

        @Override
        public CharSequence getPageTitle(int position) {
            Log.e("page", String.valueOf(position));
            return "Page " + position;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            return super.instantiateItem(container, position);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}