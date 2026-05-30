package com.example.project;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HistoryTransactionAdapter
        extends RecyclerView.Adapter<HistoryTransactionAdapter.ViewHolder> {

    public static class TransactionItem {
        public int    id;
        public double amount;
        public String category, type, note, date;

        public TransactionItem(int id, double amount, String category,
                               String type, String note, String date) {
            this.id       = id;
            this.amount   = amount;
            this.category = category;
            this.type     = type;
            this.note     = note;
            this.date     = date;
        }
    }

    public interface OnActionListener {
        void onEdit(TransactionItem item);
        void onDelete(TransactionItem item, int position);
    }

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
        ICONS.put("Education",     "📚");
        ICONS.put("Gift",          "🎁");
        ICONS.put("Other",         "💳");
    }

    private List<TransactionItem> items;
    private final OnActionListener listener;

    public HistoryTransactionAdapter(List<TransactionItem> items,
                                     OnActionListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void updateItems(List<TransactionItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_transaction_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        TransactionItem item = items.get(position);

        holder.tvIcon.setText(ICONS.getOrDefault(item.category, "💳"));
        holder.tvCategory.setText(item.category);
        holder.tvNote.setText(item.note != null && !item.note.isEmpty()
                ? item.note : item.category);
        holder.tvDate.setText(item.date);

        if ("income".equals(item.type)) {
            holder.tvAmount.setText(String.format("+$%.2f", item.amount));
            holder.tvAmount.setTextColor(0xFF00C853);
        } else {
            holder.tvAmount.setText(String.format("-$%.2f", item.amount));
            holder.tvAmount.setTextColor(0xFFFF5252);
        }

        holder.btnEdit.setOnClickListener(v -> listener.onEdit(item));
        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvIcon, tvCategory, tvNote, tvDate, tvAmount, btnEdit, btnDelete;

        ViewHolder(View view) {
            super(view);
            tvIcon     = view.findViewById(R.id.tvIcon);
            tvCategory = view.findViewById(R.id.tvCategory);
            tvNote     = view.findViewById(R.id.tvNote);
            tvDate     = view.findViewById(R.id.tvDate);
            tvAmount   = view.findViewById(R.id.tvAmount);
            btnEdit    = view.findViewById(R.id.btnEdit);
            btnDelete  = view.findViewById(R.id.btnDelete);
        }
    }
}