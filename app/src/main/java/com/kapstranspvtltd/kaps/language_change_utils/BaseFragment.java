package com.kapstranspvtltd.kaps.language_change_utils;

import android.content.Context;
import android.content.res.Resources;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

public abstract class BaseFragment extends Fragment {
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(requireContext());
        
        // Update locale when fragment attaches
        String language = LocaleHelper.getLanguage(requireContext());
        LocaleHelper.setLocale(requireContext(), language);
    }

    @Override
    public void onResume() {
        super.onResume();
        // Refresh views if needed
        updateViewsForLanguage();
    }

    protected void updateViewsForLanguage() {
        // Override this in fragments that need special handling
        if (getView() != null) {
            // Force refresh text views
            refreshViewGroup((ViewGroup) getView());
        }
    }

    private void refreshViewGroup(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof ViewGroup) {
                refreshViewGroup((ViewGroup) child);
            } else if (child instanceof TextView) {
                refreshTextView((TextView) child);
            }
        }
    }

    private void refreshTextView(TextView textView) {
        // Get the text resource ID if it exists
        try {
            if (textView.getId() != View.NO_ID) {
                String resourceEntryName = getResources().getResourceEntryName(textView.getId());
                int resId = getResources().getIdentifier(resourceEntryName, "string", requireContext().getPackageName());
                if (resId != 0) {
                    textView.setText(resId);
                }
            }
        } catch (Resources.NotFoundException ignored) {
            // Resource not found, keep existing text
        }
    }
}