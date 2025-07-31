package com.kapstranspvtltd.kaps.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.kapstranspvtltd.kaps.R;
import com.kapstranspvtltd.kaps.utility.Drop;

import java.util.List;

public class DropLocationAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    public interface DropActionListener {
        void onEdit(int position);
        void onSearch(int position);
        void onRemove(int position);
        void onAddStop();
    }

    private static final int TYPE_DROP = 0;
    private static final int TYPE_ADD_STOP = 1;

    private List<Drop> dropList;
    private int maxDrops;
    private DropActionListener listener;
    private int editingIndex = -1;

    public void setEditingIndex(int editingIndex) {
        this.editingIndex = editingIndex;
        notifyDataSetChanged();
    }

    public DropLocationAdapter(List<Drop> dropList, int maxDrops, DropActionListener listener) {
        this.dropList = dropList;
        this.maxDrops = maxDrops;
        this.listener = listener;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < dropList.size()) return TYPE_DROP;
        return TYPE_ADD_STOP;
    }

    @Override
    public int getItemCount() {
        return dropList.size() < maxDrops ? dropList.size() + 1 : dropList.size();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_DROP) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_drop_location, parent, false);
            return new DropViewHolder(v);
        } else {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_add_stop, parent, false);
            return new AddStopViewHolder(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof DropViewHolder) {
            Drop drop = dropList.get(position);
            DropViewHolder vh = (DropViewHolder) holder;
            int dropIndex = position+1;
            vh.txtDropIndex.setText(dropIndex+"");
            // Set drop details
            if (drop.getAddress() != null && !drop.getAddress().isEmpty()) {
                vh.txtDropAddress.setText(drop.getAddress());
                vh.txtDropAddress.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.black));
                
                // Show contact details if available
                if (drop.getRname() != null && !drop.getRname().isEmpty() && 
                    drop.getRmobile() != null && !drop.getRmobile().isEmpty()) {
//                    vh.contactDetailsLayout.setVisibility(View.VISIBLE);
                    vh.txtDropContactDetails.setText(drop.getRname() + " • " + drop.getRmobile());
                } else {
//                    vh.contactDetailsLayout.setVisibility(View.GONE);
                }
            } else {
                vh.txtDropAddress.setText("Where is your Drop "+dropIndex+" ?");

                vh.txtDropAddress.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.grey));
//                vh.contactDetailsLayout.setVisibility(View.GONE);
            }
            
            // Set click listeners
            vh.btnSearch.setOnClickListener(v -> listener.onSearch(position));
            vh.btnEdit.setOnClickListener(v -> listener.onEdit(position));
            
            // Make drop address clickable for search
            vh.txtDropAddress.setOnClickListener(v -> listener.onSearch(position));
            
            // Show/hide remove button (don't show for first drop)
            if (position == 0) {
                vh.btnRemove.setVisibility(View.GONE);
            } else {
                vh.btnRemove.setVisibility(View.VISIBLE);
                vh.btnRemove.setOnClickListener(v -> listener.onRemove(position));
            }
            

            
        } else if (holder instanceof AddStopViewHolder) {
            holder.itemView.setOnClickListener(v -> listener.onAddStop());
        }
    }

    static class DropViewHolder extends RecyclerView.ViewHolder {
        TextView txtDropAddress, txtDropContact, txtDropContactDetails,txtDropIndex;
        ImageView btnEdit, btnSearch, btnRemove;
//        LinearLayout contactDetailsLayout;
        
        DropViewHolder(View v) {
            super(v);
            txtDropAddress = v.findViewById(R.id.txtDropAddress);
            txtDropIndex = v.findViewById(R.id.dropTextIndex);

//            txtDropContact = v.findViewById(R.id.txtDropContact);
            txtDropContactDetails = v.findViewById(R.id.txtDropContactDetails);
//            contactDetailsLayout = v.findViewById(R.id.contactDetailsLayout);
            btnEdit = v.findViewById(R.id.btnEdit);
            btnSearch = v.findViewById(R.id.btnSearch);
            btnRemove = v.findViewById(R.id.btnRemove);
        }
    }

    static class AddStopViewHolder extends RecyclerView.ViewHolder {
        AddStopViewHolder(View v) { super(v); }
    }
}