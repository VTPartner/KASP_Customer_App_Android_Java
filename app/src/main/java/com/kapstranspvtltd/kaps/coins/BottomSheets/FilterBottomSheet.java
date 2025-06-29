package com.kapstranspvtltd.kaps.coins.BottomSheets;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.kapstranspvtltd.kaps.R;

public class FilterBottomSheet extends BottomSheetDialogFragment {

    private TextView tvClear;
    private RadioGroup rgDateRange;
    private Button btnApply;

    private FilterListener listener;

    public interface FilterListener {
        void onFilterApplied(String dateRange);
        void onFilterCleared();
    }

    public void setFilterListener(FilterListener listener) {
        this.listener = listener;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.activity_filter_bottom_sheet, container, false);

        tvClear = view.findViewById(R.id.tv_clear);
        rgDateRange = view.findViewById(R.id.rg_date_range);
        btnApply = view.findViewById(R.id.btn_apply);

        setupClickListeners();

        return view;
    }

    private void setupClickListeners() {
        tvClear.setOnClickListener(v -> {
            if (listener != null) {
                listener.onFilterCleared();
            }
            dismiss();
        });

        btnApply.setOnClickListener(v -> {
            int selectedId = rgDateRange.getCheckedRadioButtonId();
            String dateRange = "";

            if (selectedId == R.id.rb_last_week) {
                dateRange = "Last Week";
            } else if (selectedId == R.id.rb_last_month) {
                dateRange = "Last Month";
            } else if (selectedId == R.id.rb_last_3_months) {
                dateRange = "Last 3 Months";
            } else if (selectedId == R.id.rb_custom) {
                dateRange = "Custom";
            }

            if (listener != null) {
                listener.onFilterApplied(dateRange);
            }
            dismiss();
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        if (getDialog() != null) {
            getDialog().getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}