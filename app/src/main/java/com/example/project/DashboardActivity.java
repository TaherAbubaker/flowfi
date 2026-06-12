package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.Calendar;

public class DashboardActivity extends AppCompatActivity {

    private TextView tvGreeting, tvUserName, tvAvatar;
    private TextView tvBalance, tvTotalIncome, tvTotalExpense;
    private TextView tvStreakCount, tvStreakSub;
    private TextView tvNoTransactions, tvSeeAll;
    private RecyclerView rvRecentTransactions;
    private TransactionAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;
    private int userId;

    // ActivityResultLauncher for AddTransactionActivity
    private final ActivityResultLauncher<Intent> addTransactionLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            refreshDashboard(); // refresh when transaction added
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        FloatingActionButton btnChatbot = findViewById(R.id.btnChatbot);

        btnChatbot.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, ChatbotActivity.class);
            startActivity(intent);
        });

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);
        userId  = session.getUserId();

        bindViews();
        setupGreeting();
        setupRecyclerView();
        setupBottomNav();
        setupFab();
        setupSeeAll();
        refreshDashboard();
    }

    private void bindViews() {
        tvGreeting           = findViewById(R.id.tvGreeting);
        tvUserName           = findViewById(R.id.tvUserName);
        tvAvatar             = findViewById(R.id.tvAvatar);
        tvBalance            = findViewById(R.id.tvBalance);
        tvTotalIncome        = findViewById(R.id.tvTotalIncome);
        tvTotalExpense       = findViewById(R.id.tvTotalExpense);
        tvStreakCount        = findViewById(R.id.tvStreakCount);
        tvStreakSub          = findViewById(R.id.tvStreakSub);
        tvNoTransactions     = findViewById(R.id.tvNoTransactions);
        tvSeeAll             = findViewById(R.id.tvSeeAll);
        rvRecentTransactions = findViewById(R.id.rvRecentTransactions);
    }

    private void setupGreeting() {
        // Time-based greeting
        int hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        String greeting;
        if (hour < 12)      greeting = "Good morning,";
        else if (hour < 17) greeting = "Good afternoon,";
        else                greeting = "Good evening,";
        tvGreeting.setText(greeting);

        // Load user name from DB
        String name = db.getUserName(userId);
        tvUserName.setText(name);

        // Avatar initial
        if (!name.isEmpty()) {
            tvAvatar.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }

    private void setupRecyclerView() {
        rvRecentTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvRecentTransactions.setNestedScrollingEnabled(false);
        Cursor cursor = db.getRecentTransactions(userId, 5);
        adapter = new TransactionAdapter(cursor);
        rvRecentTransactions.setAdapter(adapter);
    }

    private void refreshDashboard() {
        // Balance
        double income  = db.getTotalByType(userId, "income");
        double expense = db.getTotalByType(userId, "expense");
        double balance = income - expense;

        tvBalance.setText(String.format("$%.2f", balance));
        tvTotalIncome.setText(String.format("$%.2f", income));
        tvTotalExpense.setText(String.format("$%.2f", expense));

        // Balance color — red if negative
        tvBalance.setTextColor(balance >= 0 ? 0xFF00C853 : 0xFFFF5252);

        // Streak
        int streak = db.getBestStreak(userId);
        if (streak > 0) {
            tvStreakCount.setText(streak + " days");
            tvStreakSub.setText("Keep it up! You're on a roll.");
        } else {
            tvStreakCount.setText("0 days");
            tvStreakSub.setText("Start a challenge to build your streak");
        }

        // Recent transactions
        Cursor cursor = db.getRecentTransactions(userId, 5);
        adapter.updateCursor(cursor);

        // Empty state
        if (adapter.getItemCount() == 0) {
            tvNoTransactions.setVisibility(View.VISIBLE);
            rvRecentTransactions.setVisibility(View.GONE);
        } else {
            tvNoTransactions.setVisibility(View.GONE);
            rvRecentTransactions.setVisibility(View.VISIBLE);
        }
    }

    private void setupFab() {
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(v -> {
            Intent intent = new Intent(DashboardActivity.this, AddTransactionActivity.class);
            addTransactionLauncher.launch(intent);
        });
    }

    private void setupSeeAll() {
        tvSeeAll.setOnClickListener(v ->
                startActivity(new Intent(DashboardActivity.this,
                        TransactionHistoryActivity.class)));
    }


    private void setupBottomNav() {
        BottomNavigationView bottomNav = findViewById(R.id.bottomNav);
        bottomNav.setSelectedItemId(R.id.nav_home);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home) {
                return true; // already here

            } else if (id == R.id.nav_history) {
                startActivity(new Intent(this, TransactionHistoryActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_analytics) {
                startActivity(new Intent(this, AnalyticsActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_goals) {
                startActivity(new Intent(this, SavingsGoalsActivity.class));
                overridePendingTransition(0, 0);
                return true;

            } else if (id == R.id.nav_chatbot) {
                startActivity(new Intent(this, ChallengesActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return false;
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshDashboard();
    }
}