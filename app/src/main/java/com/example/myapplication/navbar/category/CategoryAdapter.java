package com.example.myapplication.navbar.category;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.myapplication.R;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private List<Category> categories;

    public CategoryAdapter(List<Category> categories) {
        this.categories = categories;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.label.setText(category.getDisplayName());
        holder.icon.setImageResource(category.getIconResId());

        // Optional: highlight if activated
        holder.itemView.setAlpha(category.isActivated() ? 1f : 0.5f);

        // Optional: click to toggle activated
        holder.itemView.setOnClickListener(v -> {
            category.setActivated(!category.isActivated());
            notifyItemChanged(position);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView label;
        ImageView icon;

        ViewHolder(View v) {
            super(v);
            label = v.findViewById(R.id.categoryLabel);
            icon = v.findViewById(R.id.categoryIcon);
        }
    }
}


