package com.example.project;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AddTransactionActivity extends AppCompatActivity {

    private TextView btnExpense, btnIncome, tvDate, btnSave, btnBack;
    private EditText etAmount, etNote;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;

    private String selectedType = "expense";
    private String selectedDate = "";

    private DatabaseHelper db;
    private SessionManager session;

    // Expense categories
    private static final List<CategoryAdapter.Category> EXPENSE_CATS = new ArrayList<>();
    private static final List<CategoryAdapter.Category> INCOME_CATS  = new ArrayList<>();

    static {
        EXPENSE_CATS.add(new CategoryAdapter.Category("Food",          "🍔"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Transport",     "🚗"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Shopping",      "🛍️"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Health",        "💊"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Entertainment", "🎬"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Bills",         "🧾"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Education",     "📚"));
        EXPENSE_CATS.add(new CategoryAdapter.Category("Other",         "💳"));

        INCOME_CATS.add(new CategoryAdapter.Category("Salary",     "💼"));
        INCOME_CATS.add(new CategoryAdapter.Category("Freelance",  "💻"));
        INCOME_CATS.add(new CategoryAdapter.Category("Investment", "📈"));
        INCOME_CATS.add(new CategoryAdapter.Category("Gift",       "🎁"));
        INCOME_CATS.add(new CategoryAdapter.Category("Other",      "💰"));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_transaction);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);

        bindViews();
        setTodayDate();
        setupCategoryGrid(EXPENSE_CATS);
        setupToggle();
        setupDatePicker();
        setupSave();

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        btnExpense    = findViewById(R.id.btnExpense);
        btnIncome     = findViewById(R.id.btnIncome);
        tvDate        = findViewById(R.id.tvDate);
        btnSave       = findViewById(R.id.btnSave);
        btnBack       = findViewById(R.id.btnBack);
        etAmount      = findViewById(R.id.etAmount);
        etNote        = findViewById(R.id.etNote);
        rvCategories  = findViewById(R.id.rvCategories);
    }

    private void setTodayDate() {
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        selectedDate = sdf.format(cal.getTime());
        tvDate.setText(selectedDate);
    }

    private void setupCategoryGrid(List<CategoryAdapter.Category> cats) {
        categoryAdapter = new CategoryAdapter(cats, name -> {});
        rvCategories.setLayoutManager(new GridLayoutManager(this, 4));
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupToggle() {
        btnExpense.setOnClickListener(v -> {
            selectedType = "expense";
            // Active style
            btnExpense.setBackground(getDrawable(R.drawable.bg_toggle_active));
            btnExpense.setTextColor(0xFF121212);
            // Inactive style
            btnIncome.setBackground(getDrawable(R.drawable.bg_toggle_inactive));
            btnIncome.setTextColor(0xFFB0B0B0);
            // Swap categories
            setupCategoryGrid(EXPENSE_CATS);
        });

        btnIncome.setOnClickListener(v -> {
            selectedType = "income";
            // Active style
            btnIncome.setBackground(getDrawable(R.drawable.bg_toggle_active));
            btnIncome.setTextColor(0xFF121212);
            // Inactive style
            btnExpense.setBackground(getDrawable(R.drawable.bg_toggle_inactive));
            btnExpense.setTextColor(0xFFB0B0B0);
            // Swap categories
            setupCategoryGrid(INCOME_CATS);
        });
    }

    private void setupDatePicker() {
        tvDate.setOnClickListener(v -> {
            Calendar cal = Calendar.getInstance();
            new DatePickerDialog(this,
                    (view, year, month, day) -> {
                        selectedDate = String.format(Locale.getDefault(),
                                "%04d-%02d-%02d", year, month + 1, day);
                        tvDate.setText(selectedDate);
                    },
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)
            ).show();
        });
    }

    private void setupSave() {
        btnSave.setOnClickListener(v -> {

            String amountStr = etAmount.getText().toString().trim();
            String note      = etNote.getText().toString().trim();
            String category  = categoryAdapter.getSelectedCategory();

            // Validate
            if (amountStr.isEmpty()) {
                etAmount.setError("Enter an amount");
                etAmount.requestFocus();
                return;
            }

            double amount;
            try {
                amount = Double.parseDouble(amountStr);
            } catch (NumberFormatException e) {
                etAmount.setError("Invalid amount");
                return;
            }

            if (amount <= 0) {
                etAmount.setError("Amount must be greater than 0");
                return;
            }

            int userId = session.getUserId();
            boolean saved = db.addTransaction(
                    userId, amount, category, selectedType, note, selectedDate);

            if (saved) {
                Toast.makeText(this, "Transaction saved!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Failed to save. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}