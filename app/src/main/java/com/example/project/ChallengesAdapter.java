package com.example.project;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChallengesAdapter extends RecyclerView.Adapter<ChallengesAdapter.ViewHolder> {

    public static class ChallengeItem {
        public int    id, targetDays, currentStreak, bestStreak;
        public String title, description, startDate;
        public boolean isActive;

        public ChallengeItem(int id, String title, String description,
                             int targetDays, int currentStreak, int bestStreak,
                             boolean isActive, String startDate) {
            this.id            = id;
            this.title         = title;
            this.description   = description;
            this.targetDays    = targetDays;
            this.currentStreak = currentStreak;
            this.bestStreak    = bestStreak;
            this.isActive      = isActive;
            this.startDate     = startDate;
        }
    }

    public interface OnChallengeActionListener {
        void onComplete(ChallengeItem item, int position);
        void onDelete(ChallengeItem item, int position);
    }

    private static final String[] EMOJIS =
            {"⚡", "💪", "🎯", "🏆", "🌟", "🔑", "🧘", "🚴", "💡", "🦁"};

    private List<ChallengeItem> items;
    private final OnChallengeActionListener listener;

    public ChallengesAdapter(List<ChallengeItem> items,
                             OnChallengeActionListener listener) {
        this.items    = items;
        this.listener = listener;
    }

    public void updateItems(List<ChallengeItem> newItems) {
        this.items = newItems;
        notifyDataSetChanged();
    }

    public void removeItem(int position) {
        items.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, items.size());
    }

    public void updateStreak(int position, int newStreak) {
        items.get(position).currentStreak = newStreak;
        notifyItemChanged(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_challenge, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChallengeItem item = items.get(position);

        holder.tvEmoji.setText(EMOJIS[item.id % EMOJIS.length]);
        holder.tvTitle.setText(item.title);
        holder.tvDesc.setText(item.description != null && !item.description.isEmpty()
                ? item.description : "Keep it up every day!");
        holder.tvStreak.setText(item.currentStreak + " days");

        int pct = item.targetDays > 0
                ? (int) Math.min((item.currentStreak / (float) item.targetDays) * 100, 100)
                : 0;

        holder.tvProgress.setText(item.currentStreak + " / " + item.targetDays + " days");

        if (pct >= 100) {
            holder.tvStatus.setText("✅ Complete!");
            holder.tvStatus.setTextColor(0xFF00C853);
            holder.btnComplete.setText("Completed 🎉");
            holder.btnComplete.setAlpha(0.5f);
        } else {
            holder.tvStatus.setText(pct + "%");
            holder.tvStatus.setTextColor(0xFF00C853);
            holder.btnComplete.setText("✅ Mark Today Done");
            holder.btnComplete.setAlpha(1f);
        }

        // Progress bar
        holder.progressFill.post(() -> {
            int parentWidth = ((View) holder.progressFill.getParent()).getWidth();
            int fillWidth   = (int) ((pct / 100f) * parentWidth);
            ViewGroup.LayoutParams lp = holder.progressFill.getLayoutParams();
            lp.width = fillWidth;
            holder.progressFill.setLayoutParams(lp);
        });

        holder.btnComplete.setOnClickListener(v -> {
            if (item.currentStreak < item.targetDays) {
                listener.onComplete(item, holder.getAdapterPosition());
            }
        });

        holder.btnDelete.setOnClickListener(v ->
                listener.onDelete(item, holder.getAdapterPosition()));
    }

    @Override
    public int getItemCount() {
        return items == null ? 0 : items.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvEmoji, tvTitle, tvDesc, tvStreak;
        TextView tvProgress, tvStatus;
        TextView btnComplete, btnDelete;
        View     progressFill;

        ViewHolder(View view) {
            super(view);
            tvEmoji       = view.findViewById(R.id.tvChallengeEmoji);
            tvTitle       = view.findViewById(R.id.tvChallengeTitle);
            tvDesc        = view.findViewById(R.id.tvChallengeDesc);
            tvStreak      = view.findViewById(R.id.tvStreak);
            tvProgress    = view.findViewById(R.id.tvChallengeProgress);
            tvStatus      = view.findViewById(R.id.tvChallengeStatus);
            btnComplete   = view.findViewById(R.id.btnComplete);
            btnDelete     = view.findViewById(R.id.btnDeleteChallenge);
            progressFill  = view.findViewById(R.id.challengeProgressFill);
        }
    }
}