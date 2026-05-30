package com.example.project;

import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.Map;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.ViewHolder> {

    private Cursor cursor;

    // Category → emoji map
    private static final Map<String, String> ICONS = new HashMap<>();
    static {
        ICONS.put("Food",          "🍔");
        ICONS.put("Transport",     "🚗");
        ICONS.put("Shopping",      "🛍️");
        ICONS.put("Health",        "💊");
        ICONS.put("Entertainment", "🎬");
        ICONS.put("Salary",        "💼");
        ICONS.put("Freelance",     "💻");
        ICONS.put("Investment",    "📈");
        ICONS.put("Bills",         "🧾");
        ICONS.put("Other",         "💳");
    }

    public TransactionAdapter(Cursor cursor) {
        this.cursor = cursor;
    }

    public void updateCursor(Cursor newCursor) {
        if (cursor != null) cursor.close();
        cursor = newCursor;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (cursor == null || !cursor.moveToPosition(position)) return;

        String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
        String note     = cursor.getString(cursor.getColumnIndexOrThrow("note"));
        String type     = cursor.getString(cursor.getColumnIndexOrThrow("type"));
        double amount   = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
        String date     = cursor.getString(cursor.getColumnIndexOrThrow("date"));

        holder.tvCategory.setText(category);
        holder.tvNote.setText(note != null && !note.isEmpty() ? note : category);
        holder.tvDate.setText(date);
        holder.tvCategoryIcon.setText(ICONS.getOrDefault(category, "💳"));

        if (type.equals("income")) {
            holder.tvAmount.setText(String.format("+$%.2f", amount));
            holder.tvAmount.setTextColor(0xFF00C853);
        } else {
            holder.tvAmount.setText(String.format("-$%.2f", amount));
            holder.tvAmount.setTextColor(0xFFFF5252);
        }
    }

    @Override
    public int getItemCount() {
        return cursor == null ? 0 : cursor.getCount();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategoryIcon, tvCategory, tvNote, tvAmount, tvDate;

        ViewHolder(View view) {
            super(view);
            tvCategoryIcon = view.findViewById(R.id.tvCategoryIcon);
            tvCategory     = view.findViewById(R.id.tvCategory);
            tvNote         = view.findViewById(R.id.tvNote);
            tvAmount       = view.findViewById(R.id.tvAmount);
            tvDate         = view.findViewById(R.id.tvDate);
        }
    }
}