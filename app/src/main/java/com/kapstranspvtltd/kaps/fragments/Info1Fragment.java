package com.kapstranspvtltd.kaps.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.model.AppContent;
import com.kapstranspvtltd.kaps.utility.AppContentManager;



public class Info1Fragment extends Fragment {


    public static Info1Fragment newInstance() {
        Info1Fragment fragment = new Info1Fragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_info1, container, false);
        
        // Load dynamic content
        loadDynamicContent(view);
        
        return view;
    }

    private void loadDynamicContent(View view) {
        AppContent content = AppContentManager.getInstance(requireContext())
                .getFirstContentForScreen("onboarding_1");
        
        if (content != null) {
            ImageView imageView = view.findViewById(R.id.onboarding_image);
            TextView titleView = view.findViewById(R.id.onboarding_title);
            TextView descriptionView = view.findViewById(R.id.onboarding_description);
            
            // Set title
            if (titleView != null && !content.getTitle().equals("NA")) {
                titleView.setText(content.getTitle());
            }
            
            // Set description
            if (descriptionView != null && !content.getDescription().equals("NA")) {
                descriptionView.setText(content.getDescription());
            }
            
            // Set image
            if (imageView != null && !content.getImageUrl().equals("NA")) {
                if (content.getImageUrl().startsWith("http")) {
                    Glide.with(this)
                            .load(content.getImageUrl())
                            .placeholder(R.drawable.img1)
                            .error(R.drawable.img1)
                            .into(imageView);
                } else {
                    // Handle local drawable resources
                    try {
                        int resourceId = getResources().getIdentifier(
                                content.getImageUrl().replace("@drawable/", ""),
                                "drawable",
                                requireContext().getPackageName()
                        );
                        if (resourceId != 0) {
                            imageView.setImageResource(resourceId);
                        }
                    } catch (Exception e) {
                        // Fallback to default image
                        imageView.setImageResource(R.drawable.img1);
                    }
                }
            }
        }
    }
}
