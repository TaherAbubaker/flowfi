package com.example.project;

import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {

    private RecyclerView rvTransactions;
    private HistoryTransactionAdapter adapter;
    private DatabaseHelper db;
    private SessionManager session;
    private int userId;

    private TextView chipAll, chipIncome, chipExpense;
    private TextView tvResultCount, tvNetAmount;
    private EditText etSearch;

    private String currentFilter = "all";
    private String currentSearch = "";

    private List<HistoryTransactionAdapter.TransactionItem> allItems = new ArrayList<>();

    // ActivityResultLauncher for EditTransactionActivity
    private final ActivityResultLauncher<Intent> editLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == RESULT_OK) {
                            loadTransactions();
                        }
                    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);
        userId  = session.getUserId();

        bindViews();
        setupRecyclerView();
        setupChips();
        setupSearch();
        loadTransactions();

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        rvTransactions = findViewById(R.id.rvTransactions);
        chipAll        = findViewById(R.id.chipAll);
        chipIncome     = findViewById(R.id.chipIncome);
        chipExpense    = findViewById(R.id.chipExpense);
        tvResultCount  = findViewById(R.id.tvResultCount);
        tvNetAmount    = findViewById(R.id.tvNetAmount);
        etSearch       = findViewById(R.id.etSearch);
    }

    private void setupRecyclerView() {
        adapter = new HistoryTransactionAdapter(new ArrayList<>(),
                new HistoryTransactionAdapter.OnActionListener() {
                    @Override
                    public void onEdit(HistoryTransactionAdapter.TransactionItem item) {
                        Intent intent = new Intent(
                                TransactionHistoryActivity.this,
                                EditTransactionActivity.class);
                        intent.putExtra(EditTransactionActivity.EXTRA_ID,       item.id);
                        intent.putExtra(EditTransactionActivity.EXTRA_AMOUNT,   item.amount);
                        intent.putExtra(EditTransactionActivity.EXTRA_CATEGORY, item.category);
                        intent.putExtra(EditTransactionActivity.EXTRA_TYPE,     item.type);
                        intent.putExtra(EditTransactionActivity.EXTRA_NOTE,     item.note);
                        intent.putExtra(EditTransactionActivity.EXTRA_DATE,     item.date);
                        editLauncher.launch(intent);
                    }

                    @Override
                    public void onDelete(HistoryTransactionAdapter.TransactionItem item,
                                         int position) {
                        new AlertDialog.Builder(TransactionHistoryActivity.this)
                                .setTitle("Delete Transaction")
                                .setMessage("Are you sure you want to delete this transaction?")
                                .setPositiveButton("Delete", (dialog, which) -> {
                                    boolean deleted = db.deleteTransaction(item.id);
                                    if (deleted) {
                                        adapter.removeItem(position);
                                        allItems.remove(item);
                                        updateSummary(getCurrentDisplayList());
                                    }
                                })
                                .setNegativeButton("Cancel", null)
                                .show();
                    }
                });

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        rvTransactions.setAdapter(adapter);
    }

    private void setupChips() {
        chipAll.setOnClickListener(v -> {
            currentFilter = "all";
            updateChipStyles();
            applyFilters();
        });
        chipIncome.setOnClickListener(v -> {
            currentFilter = "income";
            updateChipStyles();
            applyFilters();
        });
        chipExpense.setOnClickListener(v -> {
            currentFilter = "expense";
            updateChipStyles();
            applyFilters();
        });
    }

    private void updateChipStyles() {
        // Reset all
        chipAll.setBackground(getDrawable(R.drawable.bg_filter_chip));
        chipAll.setTextColor(0xFFB0B0B0);
        chipIncome.setBackground(getDrawable(R.drawable.bg_filter_chip));
        chipIncome.setTextColor(0xFFB0B0B0);
        chipExpense.setBackground(getDrawable(R.drawable.bg_filter_chip));
        chipExpense.setTextColor(0xFFB0B0B0);

        // Activate selected
        switch (currentFilter) {
            case "all":
                chipAll.setBackground(getDrawable(R.drawable.bg_filter_chip_active));
                chipAll.setTextColor(0xFF00C853);
                break;
            case "income":
                chipIncome.setBackground(getDrawable(R.drawable.bg_filter_chip_active));
                chipIncome.setTextColor(0xFF00C853);
                break;
            case "expense":
                chipExpense.setBackground(getDrawable(R.drawable.bg_filter_chip_active));
                chipExpense.setTextColor(0xFF00C853);
                break;
        }
    }

    private void setupSearch() {
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                currentSearch = s.toString().toLowerCase().trim();
                applyFilters();
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void loadTransactions() {
        allItems.clear();
        Cursor cursor = db.getAllTransactions(userId);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int    id       = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                double amount   = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"));
                String category = cursor.getString(cursor.getColumnIndexOrThrow("category"));
                String type     = cursor.getString(cursor.getColumnIndexOrThrow("type"));
                String note     = cursor.getString(cursor.getColumnIndexOrThrow("note"));
                String date     = cursor.getString(cursor.getColumnIndexOrThrow("date"));

                allItems.add(new HistoryTransactionAdapter.TransactionItem(
                        id, amount, category, type, note, date));
            }
            cursor.close();
        }

        applyFilters();
    }

    private void applyFilters() {
        List<HistoryTransactionAdapter.TransactionItem> filtered = new ArrayList<>();

        for (HistoryTransactionAdapter.TransactionItem item : allItems) {
            // Type filter
            if (!currentFilter.equals("all") && !item.type.equals(currentFilter)) continue;

            // Search filter
            if (!currentSearch.isEmpty()) {
                boolean matchCategory = item.category.toLowerCase().contains(currentSearch);
                boolean matchNote     = item.note != null &&
                        item.note.toLowerCase().contains(currentSearch);
                if (!matchCategory && !matchNote) continue;
            }

            filtered.add(item);
        }

        adapter.updateItems(filtered);
        updateSummary(filtered);
        updateEmptyState(filtered.isEmpty());
    }

    private List<HistoryTransactionAdapter.TransactionItem> getCurrentDisplayList() {
        List<HistoryTransactionAdapter.TransactionItem> filtered = new ArrayList<>();
        for (HistoryTransactionAdapter.TransactionItem item : allItems) {
            if (!currentFilter.equals("all") && !item.type.equals(currentFilter)) continue;
            if (!currentSearch.isEmpty()) {
                boolean matchCategory = item.category.toLowerCase().contains(currentSearch);
                boolean matchNote     = item.note != null &&
                        item.note.toLowerCase().contains(currentSearch);
                if (!matchCategory && !matchNote) continue;
            }
            filtered.add(item);
        }
        return filtered;
    }

    private void updateSummary(List<HistoryTransactionAdapter.TransactionItem> list) {
        int count   = list.size();
        double net  = 0;

        for (HistoryTransactionAdapter.TransactionItem item : list) {
            net += "income".equals(item.type) ? item.amount : -item.amount;
        }

        tvResultCount.setText(count + " transaction" + (count != 1 ? "s" : ""));
        tvNetAmount.setText(String.format("Net: %s$%.2f",
                net >= 0 ? "+" : "", net));
        tvNetAmount.setTextColor(net >= 0 ? 0xFF00C853 : 0xFFFF5252);
    }

    private void updateEmptyState(boolean isEmpty) {
        findViewById(R.id.layoutEmpty).setVisibility(
                isEmpty ? android.view.View.VISIBLE : android.view.View.GONE);
        rvTransactions.setVisibility(
                isEmpty ? android.view.View.GONE : android.view.View.VISIBLE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadTransactions();
    }
}