package com.example.appexpensemanagement.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.model.Budget;
import com.example.appexpensemanagement.model.Expense;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private PieChart progressChart;
    private TextView tvCurrentAmount, tvProgressPercent, tvTargetAmount;
    private ExpenseRepository repository;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        repository = new ExpenseRepository(getApplication());
        
        initViews();
        loadPersonalGoal();
        setupBottomNavigation();
    }
    
    private void initViews() {
        progressChart = findViewById(R.id.progressChart);
        tvCurrentAmount = findViewById(R.id.tvCurrentAmount);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvTargetAmount = findViewById(R.id.tvTargetAmount);
        
        // Menu item click listeners
        LinearLayout llPersonalInfo = findViewById(R.id.llPersonalInfo);
        LinearLayout llMonthlyReport = findViewById(R.id.llMonthlyReport);
        LinearLayout llFixedExpenses = findViewById(R.id.llFixedExpenses);
        LinearLayout llHelp = findViewById(R.id.llHelp);
        
        llPersonalInfo.setOnClickListener(v -> {
            // Handle click
        });
        
        llMonthlyReport.setOnClickListener(v -> {
            // Handle click
        });
        
        llFixedExpenses.setOnClickListener(v -> {
            // Handle click
        });
        
        llHelp.setOnClickListener(v -> {
            // Handle click
        });
    }
    
    private void loadPersonalGoal() {
        // Get target from SharedPreferences or use total budget as target
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        double targetAmount = Double.longBitsToDouble(prefs.getLong("personalGoal", Double.doubleToLongBits(7000000)));
        
        // Calculate current spent amount for this month
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
        
        repository.getTotalExpenses(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
            @Override
            public void onDataLoaded(Double currentSpent) {
                runOnUiThread(() -> {
                    // Update UI
                    tvCurrentAmount.setText(formatNumber(currentSpent));
                    tvTargetAmount.setText(formatNumber(targetAmount));
                    
                    // Calculate progress percentage
                    double progress = targetAmount > 0 ? (currentSpent / targetAmount) * 100 : 0;
                    if (progress > 100) progress = 100;
                    
                    tvProgressPercent.setText(String.format(Locale.getDefault(), "%.0f", progress));
                    
                    // Update progress chart
                    setupProgressChart(progress);
                });
            }
        });
    }
    
    private void setupProgressChart(double progressPercent) {
        progressChart.getDescription().setEnabled(false);
        progressChart.setRotationEnabled(false);
        progressChart.setHoleRadius(75f);
        progressChart.setTransparentCircleRadius(0f);
        progressChart.getLegend().setEnabled(false);
        progressChart.setDrawEntryLabels(false);
        progressChart.setRotationAngle(0f);
        
        float progress = (float) progressPercent;
        float remaining = 100f - progress;
        
        List<PieEntry> entries = new ArrayList<>();
        entries.add(new PieEntry(progress, ""));
        entries.add(new PieEntry(remaining, ""));
        
        PieDataSet dataSet = new PieDataSet(entries, "");
        dataSet.setColors(new int[]{
            Color.parseColor("#2196F3"), // Blue for progress
            Color.parseColor("#E0E0E0")  // Gray for remaining
        });
        dataSet.setSliceSpace(0f);
        dataSet.setDrawValues(false);
        
        PieData pieData = new PieData(dataSet);
        progressChart.setData(pieData);
        progressChart.invalidate();
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
            startActivity(new Intent(this, StatisticsActivity.class));
            finish();
        });
        
        ivAdd.setOnClickListener(v -> {
            startActivity(new Intent(this, AddExpenseActivity.class));
        });
        
        ivWallet.setOnClickListener(v -> {
            startActivity(new Intent(this, BudgetActivity.class));
            finish();
        });
        
        ivProfile.setOnClickListener(v -> {
            // Already here
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadPersonalGoal();
    }
}
