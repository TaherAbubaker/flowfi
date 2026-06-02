package com.example.project;

import android.app.AlertDialog;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ChallengesActivity extends AppCompatActivity {

    private RecyclerView rvChallenges;
    private ChallengesAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;
    private int userId;

    private TextView tvBestStreak, tvActiveCount;
    private LinearLayout layoutEmpty;

    private List<ChallengesAdapter.ChallengeItem> challengeItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_challenges);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);
        userId  = session.getUserId();

        bindViews();
        setupRecyclerView();
        loadChallenges();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddChallengeDialog());
    }

    private void bindViews() {
        rvChallenges  = findViewById(R.id.rvChallenges);
        tvBestStreak  = findViewById(R.id.tvBestStreak);
        tvActiveCount = findViewById(R.id.tvActiveCount);
        layoutEmpty   = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        adapter = new ChallengesAdapter(new ArrayList<>(),
                new ChallengesAdapter.OnChallengeActionListener() {

                    @Override
                    public void onComplete(ChallengesAdapter.ChallengeItem item,
                                           int position) {
                        db.incrementStreak(item.id);
                        int newStreak = item.currentStreak + 1;
                        adapter.updateStreak(position, newStreak);
                        challengeItems.get(position).currentStreak = newStreak;
                        updateSummary();

                        if (newStreak >= item.targetDays) {
                            Toast.makeText(ChallengesActivity.this,
                                    "🏆 Challenge complete! Amazing!",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(ChallengesActivity.this,
                                    "🔥 Day " + newStreak + " done! Keep going!",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onDelete(ChallengesAdapter.ChallengeItem item,
                                         int position) {
                        new AlertDialog.Builder(ChallengesActivity.this)
                                .setTitle("Delete Challenge")
                                .setMessage("Delete \"" + item.title + "\"?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    db.deleteChallenge(item.id);
                                    adapter.removeItem(position);
                                    challengeItems.remove(item);
                                    updateSummary();
                                    updateEmptyState();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });

        rvChallenges.setLayoutManager(new LinearLayoutManager(this));
        rvChallenges.setAdapter(adapter);
    }

    private void loadChallenges() {
        challengeItems.clear();
        Cursor cursor = db.getAllChallenges(userId);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int    id     = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title  = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                String desc   = cursor.getString(cursor.getColumnIndexOrThrow("description"));
                int    target = cursor.getInt(cursor.getColumnIndexOrThrow("target_days"));
                int    streak = cursor.getInt(cursor.getColumnIndexOrThrow("current_streak"));
                int    best   = cursor.getInt(cursor.getColumnIndexOrThrow("best_streak"));
                int    active = cursor.getInt(cursor.getColumnIndexOrThrow("is_active"));
                String start  = cursor.getString(cursor.getColumnIndexOrThrow("start_date"));

                challengeItems.add(new ChallengesAdapter.ChallengeItem(
                        id, title, desc, target, streak, best, active == 1, start));
            }
            cursor.close();
        }

        adapter.updateItems(challengeItems);
        updateSummary();
        updateEmptyState();
    }

    private void showAddChallengeDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_challenge, null);

        EditText etTitle  = dialogView.findViewById(R.id.etChallengeTitle);
        EditText etDesc   = dialogView.findViewById(R.id.etChallengeDesc);
        EditText etDays   = dialogView.findViewById(R.id.etTargetDays);

        new AlertDialog.Builder(this)
                .setTitle("New Challenge")
                .setView(dialogView)
                .setPositiveButton("Start", (dialog, which) -> {
                    String title   = etTitle.getText().toString().trim();
                    String desc    = etDesc.getText().toString().trim();
                    String daysStr = etDays.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Enter a challenge title",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (daysStr.isEmpty()) {
                        Toast.makeText(this, "Enter target days",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    int days;
                    try {
                        days = Integer.parseInt(daysStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid number",
                                Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String today = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                            .format(new Date());

                    boolean saved = db.addChallenge(userId, title, desc, days, today);
                    if (saved) {
                        Toast.makeText(this, "Challenge started! 💪",
                                Toast.LENGTH_SHORT).show();
                        loadChallenges();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSummary() {
        int bestStreak  = 0;
        int activeCount = 0;

        for (ChallengesAdapter.ChallengeItem item : challengeItems) {
            if (item.currentStreak > bestStreak) bestStreak = item.currentStreak;
            if (item.isActive && item.currentStreak < item.targetDays) activeCount++;
        }

        tvBestStreak.setText(bestStreak + " days");
        tvActiveCount.setText(String.valueOf(activeCount));
    }

    private void updateEmptyState() {
        boolean empty = challengeItems.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvChallenges.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}