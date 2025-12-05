package com.example.appexpensemanagement.ui;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.model.Expense;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class StatisticsActivity extends AppCompatActivity {
    private LineChart lineChart;
    private PieChart pieChart;
    private Button btnDay, btnWeek, btnMonth, btnYear;
    private TextView tvIncome, tvExpense, tvTotalBudget, tvCategory1;
    private ExpenseRepository repository;
    private String currentPeriod = "Tháng";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);
        
        repository = new ExpenseRepository(getApplication());
        
        initViews();
        loadSummaryData();
        setupCharts();
        setupBottomNavigation();
    }
    
    private void initViews() {
        lineChart = findViewById(R.id.lineChart);
        pieChart = findViewById(R.id.pieChart);
        btnDay = findViewById(R.id.btnDay);
        btnWeek = findViewById(R.id.btnWeek);
        btnMonth = findViewById(R.id.btnMonth);
        btnYear = findViewById(R.id.btnYear);
        tvIncome = findViewById(R.id.tvIncome);
        tvExpense = findViewById(R.id.tvExpense);
        tvTotalBudget = findViewById(R.id.tvTotalBudget);
        tvCategory1 = findViewById(R.id.tvCategory1);
        
        btnDay.setOnClickListener(v -> selectPeriod("Ngày"));
        btnWeek.setOnClickListener(v -> selectPeriod("Tuần"));
        btnMonth.setOnClickListener(v -> selectPeriod("Tháng"));
        btnYear.setOnClickListener(v -> selectPeriod("Năm"));
        
        // Set default to Month
        selectPeriod("Tháng");
    }
    
    private void loadSummaryData() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();
        
        // Load income
        repository.getTotalIncome(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
            @Override
            public void onDataLoaded(Double totalIncome) {
                runOnUiThread(() -> {
                    if (tvIncome != null) {
                        tvIncome.setText(formatNumber(totalIncome));
                    }
                });
            }
        });
        
        // Load expenses
        repository.getTotalExpenses(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
            @Override
            public void onDataLoaded(Double totalExpenses) {
                runOnUiThread(() -> {
                    if (tvExpense != null) {
                        tvExpense.setText(formatNumber(totalExpenses));
                    }
                });
            }
        });
        
        // Load budget overview
        loadBudgetOverview();
    }
    
    private void loadBudgetOverview() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        
        repository.getBudgetsByMonth(month, year, new ExpenseRepository.DataCallback<List<com.example.appexpensemanagement.model.Budget>>() {
            @Override
            public void onDataLoaded(List<com.example.appexpensemanagement.model.Budget> budgets) {
                runOnUiThread(() -> {
                    double totalBudget = 0;
                    for (com.example.appexpensemanagement.model.Budget budget : budgets) {
                        totalBudget += budget.getLimit();
                    }
                    
                    if (tvTotalBudget != null) {
                        tvTotalBudget.setText(formatNumber(totalBudget));
                    }
                    
                    // Load category breakdown for pie chart
                    loadCategoryData();
                });
            }
        });
    }
    
    private void loadCategoryData() {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();
        
        repository.getExpensesByDateRange(startDate, endDate, new ExpenseRepository.DataCallback<List<Expense>>() {
            @Override
            public void onDataLoaded(List<Expense> expenses) {
                runOnUiThread(() -> {
                    Map<String, Double> categoryTotals = new HashMap<>();
                    for (Expense expense : expenses) {
                        if ("Chi".equals(expense.getType())) {
                            categoryTotals.put(expense.getCategory(),
                                categoryTotals.getOrDefault(expense.getCategory(), 0.0) + expense.getAmount());
                        }
                    }
                    
                    // Update pie chart
                    setupPieChart(categoryTotals);
                    
                    // Update category breakdown text
                    List<Map.Entry<String, Double>> sorted = new ArrayList<>(categoryTotals.entrySet());
                    sorted.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));
                    
                    if (!sorted.isEmpty() && tvCategory1 != null) {
                        tvCategory1.setText(sorted.get(0).getKey() + ": " + formatNumber(sorted.get(0).getValue()));
                    }
                    if (sorted.size() > 1) {
                        View llCategory2 = findViewById(R.id.llCategory2);
                        if (llCategory2 != null) {
                            TextView tvCat2 = llCategory2.findViewById(R.id.tvCategory2);
                            if (tvCat2 != null) {
                                tvCat2.setText(sorted.get(1).getKey() + ": " + formatNumber(sorted.get(1).getValue()));
                            }
                            llCategory2.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }
        });
    }
    
    private void selectPeriod(String period) {
        currentPeriod = period;
        
        // Reset all buttons
        btnDay.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white, null)));
        btnDay.setTextColor(getResources().getColor(R.color.text_primary, null));
        btnWeek.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white, null)));
        btnWeek.setTextColor(getResources().getColor(R.color.text_primary, null));
        btnMonth.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white, null)));
        btnMonth.setTextColor(getResources().getColor(R.color.text_primary, null));
        btnYear.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.white, null)));
        btnYear.setTextColor(getResources().getColor(R.color.text_primary, null));
        
        // Highlight selected button
        Button selectedBtn = null;
        if ("Ngày".equals(period)) selectedBtn = btnDay;
        else if ("Tuần".equals(period)) selectedBtn = btnWeek;
        else if ("Tháng".equals(period)) selectedBtn = btnMonth;
        else if ("Năm".equals(period)) selectedBtn = btnYear;
        
        if (selectedBtn != null) {
            selectedBtn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(getResources().getColor(R.color.primary, null)));
            selectedBtn.setTextColor(getResources().getColor(R.color.white, null));
        }
        
        loadLineChartData();
    }
    
    private void setupCharts() {
        setupLineChart();
        // Pie chart will be loaded after data is available
    }
    
    private void loadLineChartData() {
        Calendar cal = Calendar.getInstance();
        List<Date> dateRanges = new ArrayList<>();
        List<String> labels = new ArrayList<>();
        
        if ("Tháng".equals(currentPeriod)) {
            // Last 6 months
            for (int i = 5; i >= 0; i--) {
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.MONTH, -i);
                dateRanges.add(cal.getTime());
                labels.add("Tháng " + (cal.get(Calendar.MONTH) + 1));
            }
        } else if ("Năm".equals(currentPeriod)) {
            // Last 6 years
            for (int i = 5; i >= 0; i--) {
                cal.set(Calendar.MONTH, 0);
                cal.set(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.YEAR, -i);
                dateRanges.add(cal.getTime());
                labels.add(String.valueOf(cal.get(Calendar.YEAR)));
            }
        } else {
            // For Day/Week, show last 7 days/weeks
            for (int i = 6; i >= 0; i--) {
                cal.add(Calendar.DAY_OF_YEAR, -i);
                dateRanges.add(cal.getTime());
                labels.add("Ngày " + (cal.get(Calendar.DAY_OF_MONTH)));
            }
        }
        
        loadLineChartEntries(dateRanges, labels);
    }
    
    private void loadLineChartEntries(List<Date> dateRanges, List<String> labels) {
        List<Entry> entries = new ArrayList<>();
        final int[] loadedCount = {0};
        final int total = dateRanges.size() - 1;
        
        for (int i = 0; i < dateRanges.size() - 1; i++) {
            Date startDate = dateRanges.get(i);
            Date endDate = dateRanges.get(i + 1);
            
            repository.getTotalExpenses(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
                @Override
                public void onDataLoaded(Double total) {
                    runOnUiThread(() -> {
                        entries.add(new Entry(loadedCount[0], total.floatValue() / 1000000f)); // Convert to millions
                        loadedCount[0]++;
                        
                        if (loadedCount[0] == total) {
                            updateLineChart(entries, labels);
                        }
                    });
                }
            });
        }
    }
    
    private void updateLineChart(List<Entry> entries, List<String> labels) {
        if (entries.isEmpty()) {
            entries.add(new Entry(0, 0f));
        }
        
        LineDataSet dataSet = new LineDataSet(entries, "Chi tiêu");
        dataSet.setColor(Color.parseColor("#2196F3"));
        dataSet.setLineWidth(2f);
        dataSet.setCircleColor(Color.parseColor("#2196F3"));
        dataSet.setCircleRadius(4f);
        dataSet.setDrawCircleHole(false);
        dataSet.setValueTextSize(10f);
        dataSet.setValueTextColor(Color.parseColor("#757575"));
        
        LineData lineData = new LineData(dataSet);
        lineChart.setData(lineData);
        
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setValueFormatter(new IndexAxisValueFormatter(labels));
        
        lineChart.invalidate();
    }
    
    private void setupLineChart() {
        lineChart.getDescription().setEnabled(false);
        lineChart.setTouchEnabled(true);
        lineChart.setDragEnabled(true);
        lineChart.setScaleEnabled(false);
        lineChart.setPinchZoom(false);
        lineChart.setDrawGridBackground(false);
        lineChart.getLegend().setEnabled(false);
        
        // X-axis
        XAxis xAxis = lineChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        
        // Y-axis
        lineChart.getAxisLeft().setDrawGridLines(false);
        lineChart.getAxisRight().setEnabled(false);
    }
    
    private void setupPieChart(Map<String, Double> categoryTotals) {
        pieChart.getDescription().setEnabled(false);
        pieChart.setRotationEnabled(false);
        pieChart.setHoleRadius(60f);
        pieChart.setTransparentCircleRadius(0f);
        pieChart.getLegend().setEnabled(false);
        pieChart.setDrawEntryLabels(false);
        
        if (categoryTotals.isEmpty()) {
            return;
        }
        
        double total = categoryTotals.values().stream().mapToDouble(Double::doubleValue).sum();
        if (total == 0) {
            return;
        }
        
        List<PieEntry> entries = new ArrayList<>();
        List<String> categories = new ArrayList<>(categoryTotals.keySet());
        int[] colors = {
            Color.parseColor("#9C27B0"),
            Color.parseColor("#2196F3"),
            Color.parseColor("#FF9800"),
            Color.parseColor("#E91E63"),
            Color.parseColor("#4CAF50"),
            Color.parseColor("#00BCD4")
        };
        
        for (int i = 0; i < categories.size() && i < 6; i++) {
            String category = categories.get(i);
            double amount = categoryTotals.get(category);
            float percentage = (float) ((amount / total) * 100);
            entries.add(new PieEntry(percentage, ""));
        }
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(colors);
        dataSet.setSliceSpace(2f);
        
        PieData pieData = new PieData(dataSet);
        pieChart.setData(pieData);
        pieChart.invalidate();
    }
    
    private String formatNumber(double amount) {
        return String.format(Locale.getDefault(), "%,.0f", amount);
    }
    
    private void setupBottomNavigation() {
        ImageView ivHome = findViewById(R.id.ivHome);
        ImageView ivStatistics = findViewById(R.id.ivStatistics);
        ImageView ivAdd = findViewById(R.id.ivAdd);
        ImageView ivWallet = findViewById(R.id.ivWallet);
        ImageView ivProfile = findViewById(R.id.ivProfile);
        
        ivHome.setOnClickListener(v -> {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        });
        
        ivStatistics.setOnClickListener(v -> {
            // Already here
        });
        
        ivAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddExpenseActivity.class));
        });
        
        ivWallet.setOnClickListener(v -> {
            startActivity(new Intent(this, BudgetActivity.class));
            finish();
        });
        
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadSummaryData();
        loadLineChartData();
    }
}
