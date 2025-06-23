package com.kapstranspvtltd.kaps.adapters;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public interface ItemTouchHelperAdapter {
    boolean onItemMove(int fromPosition, int toPosition);
}


