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

import java.util.ArrayList;
import java.util.List;

public class SavingsGoalsActivity extends AppCompatActivity {

    private RecyclerView rvGoals;
    private GoalsAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;
    private int userId;

    private TextView tvTotalSaved, tvTotalTarget, tvGoalCount;
    private LinearLayout layoutEmpty;

    private List<GoalsAdapter.GoalItem> goalItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_savings_goals);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);
        userId  = session.getUserId();

        bindViews();
        setupRecyclerView();
        loadGoals();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> showAddGoalDialog());
    }

    private void bindViews() {
        rvGoals       = findViewById(R.id.rvGoals);
        tvTotalSaved  = findViewById(R.id.tvTotalSaved);
        tvTotalTarget = findViewById(R.id.tvTotalTarget);
        tvGoalCount   = findViewById(R.id.tvGoalCount);
        layoutEmpty   = findViewById(R.id.layoutEmpty);
    }

    private void setupRecyclerView() {
        adapter = new GoalsAdapter(new ArrayList<>(),
                new GoalsAdapter.OnGoalActionListener() {

                    @Override
                    public void onAddFunds(GoalsAdapter.GoalItem goal, int position) {
                        showAddFundsDialog(goal, position);
                    }

                    @Override
                    public void onDelete(GoalsAdapter.GoalItem goal, int position) {
                        new AlertDialog.Builder(SavingsGoalsActivity.this)
                                .setTitle("Delete Goal")
                                .setMessage("Delete \"" + goal.title + "\"?")
                                .setPositiveButton("Delete", (d, w) -> {
                                    db.deleteGoal(goal.id);
                                    adapter.removeItem(position);
                                    goalItems.remove(goal);
                                    updateSummary();
                                    updateEmptyState();
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });

        rvGoals.setLayoutManager(new LinearLayoutManager(this));
        rvGoals.setAdapter(adapter);
    }

    private void loadGoals() {
        goalItems.clear();
        Cursor cursor = db.getAllGoals(userId);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int    id      = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String title   = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                double target  = cursor.getDouble(cursor.getColumnIndexOrThrow("target_amount"));
                double current = cursor.getDouble(cursor.getColumnIndexOrThrow("current_amount"));
                String deadline = cursor.getString(cursor.getColumnIndexOrThrow("deadline"));
                goalItems.add(new GoalsAdapter.GoalItem(id, title, target, current, deadline));
            }
            cursor.close();
        }

        adapter.updateItems(goalItems);
        updateSummary();
        updateEmptyState();
    }

    private void showAddGoalDialog() {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_goal, null);

        EditText etTitle    = dialogView.findViewById(R.id.etGoalTitle);
        EditText etTarget   = dialogView.findViewById(R.id.etGoalTarget);
        EditText etDeadline = dialogView.findViewById(R.id.etGoalDeadline);

        new AlertDialog.Builder(this)
                .setTitle("New Savings Goal")
                .setView(dialogView)
                .setPositiveButton("Create", (dialog, which) -> {
                    String title    = etTitle.getText().toString().trim();
                    String targetStr = etTarget.getText().toString().trim();
                    String deadline = etDeadline.getText().toString().trim();

                    if (title.isEmpty()) {
                        Toast.makeText(this, "Enter a goal title", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (targetStr.isEmpty()) {
                        Toast.makeText(this, "Enter a target amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    double target;
                    try {
                        target = Double.parseDouble(targetStr);
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Invalid amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    boolean saved = db.addGoal(userId, title, target, deadline);
                    if (saved) {
                        Toast.makeText(this, "Goal created!", Toast.LENGTH_SHORT).show();
                        loadGoals();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddFundsDialog(GoalsAdapter.GoalItem goal, int position) {
        View dialogView = LayoutInflater.from(this)
                .inflate(R.layout.dialog_add_funds, null);

        EditText etAmount = dialogView.findViewById(R.id.etFundsAmount);
        TextView tvCurrent = dialogView.findViewById(R.id.tvCurrentProgress);
        tvCurrent.setText(String.format("Current: $%.2f / $%.2f",
                goal.current, goal.target));

        new AlertDialog.Builder(this)
                .setTitle("Add Funds to " + goal.title)
                .setView(dialogView)
                .setPositiveButton("Add", (dialog, which) -> {
                    String amtStr = etAmount.getText().toString().trim();
                    if (amtStr.isEmpty()) return;

                    double add;
                    try { add = Double.parseDouble(amtStr); }
                    catch (NumberFormatException e) { return; }

                    double newAmount = goal.current + add;
                    db.updateGoalAmount(goal.id, newAmount);
                    adapter.updateItem(position, newAmount);
                    goalItems.get(position).current = newAmount;
                    updateSummary();

                    if (newAmount >= goal.target) {
                        Toast.makeText(this, "🎉 Goal reached!", Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(this,
                                String.format("$%.2f added!", add), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void updateSummary() {
        double totalSaved  = 0;
        double totalTarget = 0;
        for (GoalsAdapter.GoalItem g : goalItems) {
            totalSaved  += g.current;
            totalTarget += g.target;
        }
        tvTotalSaved.setText(String.format("$%.2f", totalSaved));
        tvTotalTarget.setText(String.format("$%.2f", totalTarget));
        tvGoalCount.setText(String.valueOf(goalItems.size()));
    }

    private void updateEmptyState() {
        boolean empty = goalItems.isEmpty();
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        rvGoals.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}