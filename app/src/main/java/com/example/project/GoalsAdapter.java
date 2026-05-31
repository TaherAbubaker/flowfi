package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.ViewHolder> {

    public static class GoalItem {
        public int    id;
        public String title, deadline;
        public double target, current;

        public GoalItem(int id, String title, double target,
                        double current, String deadline) {
            this.id       = id;
            this.title    = title;
            this.target   = target;
            this.current  = current;
            this.deadline = deadline;
        }
    }

    public interface OnGoalActionListener {
        void onAddFunds(GoalItem goal, int position);
        void onDelete(GoalItem goal, int position);
    }

    private List<GoalItem> items;
    private final OnGoalActionListener listener;

    private static final String[] EMOJIS =
            {"🎯", "🏠", "✈️", "🚗", "💍", "📱", "🎓", "💪", "🌴", "💰"};

    public GoalsAdapter(List<GoalItem> items, OnGoalActionListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void updateItems(List<GoalItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    public void updateItem(int position, double newAmount) {
        items.get(position).current = newAmount;
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_goal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GoalItem goal = items.get(position);

        // Rotate emojis by id
        holder.tvEmoji.setText(EMOJIS[goal.id % EMOJIS.length]);
        holder.tvTitle.setText(goal.title);
        holder.tvDeadline.setText(
                goal.deadline != null && !goal.deadline.isEmpty()
                        ? "🗓 " + goal.deadline : "No deadline set");

        holder.tvCurrent.setText(String.format("$%.2f saved", goal.current));
        holder.tvTarget.setText(String.format("of $%.2f", goal.target));

        int pct = goal.target > 0
                ? (int) Math.min((goal.current / goal.target) * 100, 100) : 0;
        holder.tvPercent.setText(pct + "%");

        if (pct >= 100) {
            holder.tvPercent.setTextColor(0xFF00C853);
            holder.tvPercent.setText("✅ Done!");
        } else {
            holder.tvPercent.setTextColor(0xFF00C853);
        }

        // Animate progress bar width after layout
        holder.progressFill.post(() -> {
            int parentWidth = ((View) holder.progressFill.getParent()).getWidth();
            int fillWidth   = (int) ((pct / 100f) * parentWidth);
            ViewGroup.LayoutParams lp = holder.progressFill.getLayoutParams();
            lp.width = fillWidth;
            holder.progressFill.setLayoutParams(lp);
        });

        holder.btnAddFunds.setOnClickListener(v ->
                listener.onAddFunds(goal, holder.getAdapterPosition()));
        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(goal, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvTitle, tvDeadline, tvCurrent, tvTarget, tvPercent;
        TextView btnAddFunds, btnDelete;
        View     progressFill;

        ViewHolder(View view) {
            super(view);
            tvEmoji      = view.findViewById(R.id.tvGoalEmoji);
            tvTitle      = view.findViewById(R.id.tvGoalTitle);
            tvDeadline   = view.findViewById(R.id.tvGoalDeadline);
            tvCurrent    = view.findViewById(R.id.tvGoalCurrent);
            tvTarget     = view.findViewById(R.id.tvGoalTarget);
            tvPercent    = view.findViewById(R.id.tvGoalPercent);
            btnAddFunds  = view.findViewById(R.id.btnAddFunds);
            btnDelete    = view.findViewById(R.id.btnDeleteGoal);
            progressFill = view.findViewById(R.id.progressFill);
        }
    }
}