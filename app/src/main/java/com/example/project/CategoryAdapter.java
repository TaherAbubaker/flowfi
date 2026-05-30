package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    public static class Category {
        public String name;
        public String emoji;
        public Category(String name, String emoji) {
            this.name = name;
            this.emoji = emoji;
        }
    }

    private final List<Category> categories;
    private int selectedPosition = 0;
    private final OnCategorySelectedListener listener;

    public interface OnCategorySelectedListener {
        void onSelected(String categoryName);
    }

    public CategoryAdapter(List<Category> categories, OnCategorySelectedListener listener) {
        this.categories = categories;
        this.listener = listener;
    }

    public void resetSelection() {
        selectedPosition = 0;
        notifyDataSetChanged();
    }

    public String getSelectedCategory() {
        return categories.get(selectedPosition).name;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category cat = categories.get(position);
        holder.tvEmoji.setText(cat.emoji);
        holder.tvName.setText(cat.name);

        if (position == selectedPosition) {
            holder.tvEmoji.setBackground(
                    holder.itemView.getContext()
                            .getDrawable(R.drawable.bg_category_selected));
            holder.tvName.setTextColor(0xFF00C853);
        } else {
            holder.tvEmoji.setBackground(
                    holder.itemView.getContext()
                            .getDrawable(R.drawable.bg_category_icon));
            holder.tvName.setTextColor(0xFFB0B0B0);
        }

        holder.itemView.setOnClickListener(v -> {
            int prev = selectedPosition;
            selectedPosition = holder.getAdapterPosition();
            notifyItemChanged(prev);
            notifyItemChanged(selectedPosition);
            listener.onSelected(cat.name);
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvName;
        ViewHolder(View view) {
            super(view);
            tvEmoji = view.findViewById(R.id.tvCategoryEmoji);
            tvName  = view.findViewById(R.id.tvCategoryName);
        }
    }
}