package com.example.project;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class EditTransactionActivity extends AppCompatActivity {

    public static final String EXTRA_ID       = "transaction_id";
    public static final String EXTRA_AMOUNT   = "transaction_amount";
    public static final String EXTRA_CATEGORY = "transaction_category";
    public static final String EXTRA_TYPE     = "transaction_type";
    public static final String EXTRA_NOTE     = "transaction_note";
    public static final String EXTRA_DATE     = "transaction_date";

    private TextView btnExpense, btnIncome, tvDate, btnSave, btnBack;
    private EditText etAmount, etNote;
    private RecyclerView rvCategories;
    private CategoryAdapter categoryAdapter;

    private String selectedType = "expense";
    private String selectedDate = "";
    private int transactionId   = -1;

    private DatabaseHelper db;

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
        setContentView(R.layout.activity_edit_transaction);

        db = new DatabaseHelper(this);

        bindViews();
        loadFromIntent();
        setupToggle();
        setupDatePicker();
        setupSave();

        btnBack.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        btnExpense   = findViewById(R.id.btnExpense);
        btnIncome    = findViewById(R.id.btnIncome);
        tvDate       = findViewById(R.id.tvDate);
        btnSave      = findViewById(R.id.btnSave);
        btnBack      = findViewById(R.id.btnBack);
        etAmount     = findViewById(R.id.etAmount);
        etNote       = findViewById(R.id.etNote);
        rvCategories = findViewById(R.id.rvCategories);
    }

    private void loadFromIntent() {
        transactionId = getIntent().getIntExtra(EXTRA_ID, -1);
        double amount = getIntent().getDoubleExtra(EXTRA_AMOUNT, 0);
        String category = getIntent().getStringExtra(EXTRA_CATEGORY);
        String type     = getIntent().getStringExtra(EXTRA_TYPE);
        String note     = getIntent().getStringExtra(EXTRA_NOTE);
        String date     = getIntent().getStringExtra(EXTRA_DATE);

        selectedType = type != null ? type : "expense";
        selectedDate = date != null ? date : "";

        etAmount.setText(String.valueOf(amount));
        etNote.setText(note);
        tvDate.setText(selectedDate);

        // Set toggle state
        if ("income".equals(selectedType)) {
            setIncomeActive();
            loadCategoryGrid(INCOME_CATS, category);
        } else {
            setExpenseActive();
            loadCategoryGrid(EXPENSE_CATS, category);
        }
    }

    private void loadCategoryGrid(List<CategoryAdapter.Category> cats, String preselect) {
        // Find index of preselected category
        int startIndex = 0;
        for (int i = 0; i < cats.size(); i++) {
            if (cats.get(i).name.equals(preselect)) {
                startIndex = i;
                break;
            }
        }
        final int selected = startIndex;
        categoryAdapter = new CategoryAdapter(cats, name -> {});
        // Pre-select by simulating a click after attach
        rvCategories.setLayoutManager(new GridLayoutManager(this, 4));
        rvCategories.setAdapter(categoryAdapter);
        // Manually trigger selection
        rvCategories.post(() -> {
            categoryAdapter.notifyDataSetChanged();
            // set selected via reflection-free approach: re-init with selected index
        });
        // Re-create adapter with correct starting selection
        categoryAdapter = new CategoryAdapter(cats, name -> {}) {
            {
                // set initial selected position
                selectedPosition = selected;
            }
            int selectedPosition = selected;

            @Override
            public void onBindViewHolder(
                    @androidx.annotation.NonNull ViewHolder holder, int position) {
                CategoryAdapter.Category cat = cats.get(position);
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
                });
            }

            @Override
            public String getSelectedCategory() {
                return cats.get(selectedPosition).name;
            }
        };
        rvCategories.setAdapter(categoryAdapter);
    }

    private void setupToggle() {
        btnExpense.setOnClickListener(v -> {
            selectedType = "expense";
            setExpenseActive();
            loadCategoryGrid(EXPENSE_CATS, null);
        });
        btnIncome.setOnClickListener(v -> {
            selectedType = "income";
            setIncomeActive();
            loadCategoryGrid(INCOME_CATS, null);
        });
    }

    private void setExpenseActive() {
        btnExpense.setBackground(getDrawable(R.drawable.bg_toggle_active));
        btnExpense.setTextColor(0xFF121212);
        btnIncome.setBackground(getDrawable(R.drawable.bg_toggle_inactive));
        btnIncome.setTextColor(0xFFB0B0B0);
    }

    private void setIncomeActive() {
        btnIncome.setBackground(getDrawable(R.drawable.bg_toggle_active));
        btnIncome.setTextColor(0xFF121212);
        btnExpense.setBackground(getDrawable(R.drawable.bg_toggle_inactive));
        btnExpense.setTextColor(0xFFB0B0B0);
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

            if (amountStr.isEmpty()) {
                etAmount.setError("Enter an amount");
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

            boolean updated = db.updateTransaction(
                    transactionId, amount, category, selectedType, note, selectedDate);

            if (updated) {
                Toast.makeText(this, "Transaction updated!", Toast.LENGTH_SHORT).show();
                setResult(RESULT_OK);
                finish();
            } else {
                Toast.makeText(this, "Update failed. Try again.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}