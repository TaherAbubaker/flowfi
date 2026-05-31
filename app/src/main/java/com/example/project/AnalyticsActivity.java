package com.example.project;

import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class AnalyticsActivity extends AppCompatActivity {

    private PieChart pieChart;
    private BarChart barChart;
    private TextView tvSummaryIncome, tvSummaryExpense, tvInsight;
    private TextView tabWeek, tabMonth, tabYear;
    private LinearLayout layoutTopCategories;

    private DatabaseHelper db;
    private SessionManager session;
    private int userId;

    // Chart colors — emerald palette
    private static final int[] CHART_COLORS = {
            Color.parseColor("#00C853"),
            Color.parseColor("#FF5252"),
            Color.parseColor("#FFD740"),
            Color.parseColor("#448AFF"),
            Color.parseColor("#EA80FC"),
            Color.parseColor("#18FFFF"),
            Color.parseColor("#FF6D00"),
            Color.parseColor("#69F0AE"),
    };

    private String currentPeriod = "week";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_analytics);

        db      = new DatabaseHelper(this);
        session = new SessionManager(this);
        userId  = session.getUserId();

        bindViews();
        setupTabs();
        loadAnalytics("week");

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        pieChart           = findViewById(R.id.pieChart);
        barChart           = findViewById(R.id.barChart);
        tvSummaryIncome    = findViewById(R.id.tvSummaryIncome);
        tvSummaryExpense   = findViewById(R.id.tvSummaryExpense);
        tvInsight          = findViewById(R.id.tvInsight);
        tabWeek            = findViewById(R.id.tabWeek);
        tabMonth           = findViewById(R.id.tabMonth);
        tabYear            = findViewById(R.id.tabYear);
        layoutTopCategories = findViewById(R.id.layoutTopCategories);
    }

    private void setupTabs() {
        tabWeek.setOnClickListener(v  -> { currentPeriod = "week";  updateTabs(); loadAnalytics("week"); });
        tabMonth.setOnClickListener(v -> { currentPeriod = "month"; updateTabs(); loadAnalytics("month"); });
        tabYear.setOnClickListener(v  -> { currentPeriod = "year";  updateTabs(); loadAnalytics("year"); });
    }

    private void updateTabs() {
        // Reset all
        tabWeek.setBackground(getDrawable(R.drawable.bg_tab_inactive));
        tabWeek.setTextColor(0xFF666666);
        tabMonth.setBackground(getDrawable(R.drawable.bg_tab_inactive));
        tabMonth.setTextColor(0xFF666666);
        tabYear.setBackground(getDrawable(R.drawable.bg_tab_inactive));
        tabYear.setTextColor(0xFF666666);

        // Activate selected
        switch (currentPeriod) {
            case "week":
                tabWeek.setBackground(getDrawable(R.drawable.bg_tab_active));
                tabWeek.setTextColor(0xFF121212);
                break;
            case "month":
                tabMonth.setBackground(getDrawable(R.drawable.bg_tab_active));
                tabMonth.setTextColor(0xFF121212);
                break;
            case "year":
                tabYear.setBackground(getDrawable(R.drawable.bg_tab_active));
                tabYear.setTextColor(0xFF121212);
                break;
        }
    }

    private String getSinceDate(String period) {
        Calendar cal = Calendar.getInstance();
        switch (period) {
            case "week":  cal.add(Calendar.DAY_OF_YEAR, -7);   break;
            case "month": cal.add(Calendar.MONTH, -1);          break;
            case "year":  cal.add(Calendar.YEAR, -1);           break;
        }
        return new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(cal.getTime());
    }

    private void loadAnalytics(String period) {
        String since = getSinceDate(period);

        double income  = db.getTotalByTypeSince(userId, "income",  since);
        double expense = db.getTotalByTypeSince(userId, "expense", since);

        tvSummaryIncome.setText(String.format("$%.2f", income));
        tvSummaryExpense.setText(String.format("$%.2f", expense));

        setupPieChart(since);
        setupBarChart(since, period);
        setupTopCategories(since);
        generateInsight(income, expense, since);
    }

    private void setupPieChart(String since) {
        Cursor cursor = db.getExpenseTotalsByCategory(userId, since);

        List<PieEntry> entries = new ArrayList<>();
        List<Integer>  colors  = new ArrayList<>();
        int colorIndex = 0;

        while (cursor.moveToNext()) {
            String category = cursor.getString(0);
            float  total    = (float) cursor.getDouble(1);
            entries.add(new PieEntry(total, category));
            colors.add(CHART_COLORS[colorIndex % CHART_COLORS.length]);
            colorIndex++;
        }
        cursor.close();

        if (entries.isEmpty()) {
            pieChart.setNoDataText("No expense data yet");
            pieChart.setNoDataTextColor(Color.parseColor("#666666"));
            pieChart.invalidate();
            return;
        }

        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(3f);
        dataSet.setSelectionShift(6f);
        dataSet.setValueTextColor(Color.WHITE);
        dataSet.setValueTextSize(11f);

        PieData data = new PieData(dataSet);

        pieChart.setData(data);
        pieChart.setHoleColor(Color.parseColor("#1E1E1E"));
        pieChart.setHoleRadius(54f);
        pieChart.setTransparentCircleRadius(58f);
        pieChart.setTransparentCircleColor(Color.parseColor("#1E1E1E"));
        pieChart.setDrawHoleEnabled(true);
        pieChart.setCenterText("Spending");
        pieChart.setCenterTextColor(Color.WHITE);
        pieChart.setCenterTextSize(13f);
        pieChart.getDescription().setEnabled(false);
        pieChart.getLegend().setEnabled(false);
        pieChart.setEntryLabelColor(Color.WHITE);
        pieChart.setEntryLabelTextSize(10f);
        pieChart.animateY(800);
        pieChart.invalidate();
    }

    private void setupBarChart(String since, String period) {
        Cursor cursor = db.getDailyTotals(userId, since);

        List<BarEntry> incomeEntries  = new ArrayList<>();
        List<BarEntry> expenseEntries = new ArrayList<>();
        List<String>   labels         = new ArrayList<>();
        int index = 0;

        while (cursor.moveToNext()) {
            String date    = cursor.getString(0);
            float  income  = (float) cursor.getDouble(1);
            float  expense = (float) cursor.getDouble(2);

            incomeEntries.add(new BarEntry(index, income));
            expenseEntries.add(new BarEntry(index, expense));

            // Shorten label
            String shortDate = date.length() >= 10 ? date.substring(5) : date;
            labels.add(shortDate);
            index++;
        }
        cursor.close();

        if (incomeEntries.isEmpty()) {
            barChart.setNoDataText("No data for this period");
            barChart.setNoDataTextColor(Color.parseColor("#666666"));
            barChart.invalidate();
            return;
        }

        BarDataSet incomeSet  = new BarDataSet(incomeEntries,  "Income");
        BarDataSet expenseSet = new BarDataSet(expenseEntries, "Expense");

        incomeSet.setColor(Color.parseColor("#00C853"));
        incomeSet.setValueTextColor(Color.WHITE);
        incomeSet.setValueTextSize(9f);

        expenseSet.setColor(Color.parseColor("#FF5252"));
        expenseSet.setValueTextColor(Color.WHITE);
        expenseSet.setValueTextSize(9f);

        BarData barData = new BarData(incomeSet, expenseSet);
        float groupSpace  = 0.3f;
        float barSpace    = 0.05f;
        float barWidth    = 0.3f;
        barData.setBarWidth(barWidth);

        barChart.setData(barData);
        barChart.groupBars(0f, groupSpace, barSpace);

        // Style
        barChart.getDescription().setEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.setBackgroundColor(Color.parseColor("#1E1E1E"));
        barChart.setDrawBorders(false);
        barChart.setPinchZoom(false);
        barChart.setDoubleTapToZoomEnabled(false);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.parseColor("#666666"));
        xAxis.setTextSize(9f);
        xAxis.setDrawGridLines(false);
        xAxis.setDrawAxisLine(false);

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setTextColor(Color.parseColor("#666666"));
        leftAxis.setGridColor(Color.parseColor("#252525"));
        leftAxis.setAxisMinimum(0f);
        leftAxis.setDrawAxisLine(false);

        barChart.getAxisRight().setEnabled(false);

        barChart.getLegend().setTextColor(Color.WHITE);
        barChart.getLegend().setTextSize(11f);

        barChart.animateY(800);
        barChart.invalidate();
    }

    private void setupTopCategories(String since) {
        layoutTopCategories.removeAllViews();
        Cursor cursor = db.getExpenseTotalsByCategory(userId, since);

        // Get total for percentages
        double grandTotal = 0;
        List<String[]> rows = new ArrayList<>();
        while (cursor.moveToNext()) {
            String cat   = cursor.getString(0);
            double total = cursor.getDouble(1);
            grandTotal  += total;
            rows.add(new String[]{cat, String.valueOf(total)});
        }
        cursor.close();

        if (rows.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No expense data for this period");
            empty.setTextColor(Color.parseColor("#666666"));
            empty.setTextSize(13f);
            layoutTopCategories.addView(empty);
            return;
        }

        int colorIndex = 0;
        for (String[] row : rows) {
            String cat   = row[0];
            double total = Double.parseDouble(row[1]);
            int    pct   = grandTotal > 0 ? (int) ((total / grandTotal) * 100) : 0;
            int    color = CHART_COLORS[colorIndex % CHART_COLORS.length];
            colorIndex++;

            // Container row
            LinearLayout rowLayout = new LinearLayout(this);
            rowLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 0, 0, 14);
            rowLayout.setLayoutParams(lp);

            // Label row
            LinearLayout labelRow = new LinearLayout(this);
            labelRow.setOrientation(LinearLayout.HORIZONTAL);

            TextView tvCat = new TextView(this);
            tvCat.setText(cat);
            tvCat.setTextColor(Color.WHITE);
            tvCat.setTextSize(13f);
            LinearLayout.LayoutParams tvCatLp = new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
            tvCat.setLayoutParams(tvCatLp);

            TextView tvPct = new TextView(this);
            tvPct.setText(String.format("$%.2f  (%d%%)", total, pct));
            tvPct.setTextColor(Color.parseColor("#B0B0B0"));
            tvPct.setTextSize(12f);

            labelRow.addView(tvCat);
            labelRow.addView(tvPct);

            // Progress bar background
            LinearLayout barBg = new LinearLayout(this);
            android.graphics.drawable.GradientDrawable bgShape =
                    new android.graphics.drawable.GradientDrawable();
            bgShape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            bgShape.setColor(Color.parseColor("#252525"));
            bgShape.setCornerRadius(8);
            barBg.setBackground(bgShape);
            LinearLayout.LayoutParams barBgLp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, 10);
            barBgLp.setMargins(0, 6, 0, 0);
            barBg.setLayoutParams(barBgLp);

            // Progress fill
            View barFill = new View(this);
            android.graphics.drawable.GradientDrawable fillShape =
                    new android.graphics.drawable.GradientDrawable();
            fillShape.setShape(android.graphics.drawable.GradientDrawable.RECTANGLE);
            fillShape.setColor(color);
            fillShape.setCornerRadius(8);
            barFill.setBackground(fillShape);
            int fillWidth = (int) ((pct / 100f) *
                    getResources().getDisplayMetrics().widthPixels);
            LinearLayout.LayoutParams fillLp =
                    new LinearLayout.LayoutParams(fillWidth, LinearLayout.LayoutParams.MATCH_PARENT);
            barFill.setLayoutParams(fillLp);

            barBg.addView(barFill);

            rowLayout.addView(labelRow);
            rowLayout.addView(barBg);
            layoutTopCategories.addView(rowLayout);
        }
    }

    private void generateInsight(double income, double expense, String since) {
        if (income == 0 && expense == 0) {
            tvInsight.setText("Add transactions to see your financial insights.");
            return;
        }

        double savingsRate = income > 0 ? ((income - expense) / income) * 100 : 0;
        String insight;

        if (income == 0) {
            insight = "You have only expenses this period. Try logging your income too.";
        } else if (expense == 0) {
            insight = "No expenses logged this period. Great discipline! 🎉";
        } else if (savingsRate >= 50) {
            insight = String.format(Locale.getDefault(),
                    "Excellent! You're saving %.0f%% of your income this period. Keep it up! 🚀", savingsRate);
        } else if (savingsRate >= 20) {
            insight = String.format(Locale.getDefault(),
                    "Good job! You're saving %.0f%% of your income. Try to push it above 50%%.", savingsRate);
        } else if (savingsRate >= 0) {
            insight = String.format(Locale.getDefault(),
                    "You're saving %.0f%% this period. Review your top spending categories to cut back.", savingsRate);
        } else {
            insight = String.format(Locale.getDefault(),
                    "You're spending %.0f%% more than you earn this period. Consider reducing expenses.", Math.abs(savingsRate));
        }

        tvInsight.setText(insight);
    }
}