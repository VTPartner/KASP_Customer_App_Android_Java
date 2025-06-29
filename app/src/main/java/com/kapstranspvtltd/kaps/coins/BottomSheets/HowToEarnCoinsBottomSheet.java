package com.kapstranspvtltd.kaps.coins.BottomSheets;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.coins.adapter.EarnCoinsPagerAdapter;
import com.kapstranspvtltd.kaps.coins.model.EarnCoinPage;

import java.util.ArrayList;
import java.util.List;

public class HowToEarnCoinsBottomSheet extends BottomSheetDialogFragment {

    private ViewPager2 viewPager;
    private TabLayout tabIndicator;
    private EarnCoinsPagerAdapter adapter;
    private List<EarnCoinPage> pageList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.activity_how_to_earn_coins_bottom_sheet, container, false);

        viewPager = v.findViewById(R.id.viewpager);
        tabIndicator = v.findViewById(R.id.tab_indicator);

        // Dummy data list (replace images with your drawables)
        pageList = new ArrayList<>();
        pageList.add(new EarnCoinPage(R.drawable.logo, "", "Earn 2 coins on every ₹100 spent on Truck or 2-wheeler booking", false));
        pageList.add(new EarnCoinPage(R.drawable.ic_earn_coins, "", "For Porter Credits: 1 Coin = ₹1\nFor Bank Transfer: 1 Coin = ₹0.9", false));
        pageList.add(new EarnCoinPage(R.drawable.ic_image_placeholder, "", "Porter Coins expire after 30 days from the date of credit", true));

        adapter = new EarnCoinsPagerAdapter(pageList, new EarnCoinsPagerAdapter.OnActionListener() {
            @Override
            public void onNextClick(int position) {
                if (position < pageList.size() - 1)
                    viewPager.setCurrentItem(position + 1, true);
            }

            @Override
            public void onDone() {
                dismiss();
            }
        });

        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabIndicator, viewPager, (tab, position) -> {}).attach();

        return v;
    }
}