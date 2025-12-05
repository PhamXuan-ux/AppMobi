package com.example.appexpensemanagement.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.model.Expense;
import com.example.appexpensemanagement.repository.ExpenseRepository;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MonthlyReportActivity extends AppCompatActivity {

    private TextView tvCurrentMonth, tvTotalIncome, tvTotalExpense, tvBalance;
    private Calendar currentMonth;
    private ExpenseRepository expenseRepository;
    private NumberFormat currencyFormatter;

    // Category mapping
    private Map<String, String> categoryIcons = new HashMap<>();
    private Map<String, Integer> categoryColors = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_monthly_report);

        currentMonth = Calendar.getInstance();
        expenseRepository = new ExpenseRepository(getApplication());
        currencyFormatter = NumberFormat.getCurrencyInstance(new Locale("vi", "VN"));

        initCategoryMappings();
        setupViews();
        setupBottomNavigation();
        updateMonthDisplay();
        loadMonthlyData();
    }

    private void initCategoryMappings() {
        // Map category names to icons and colors
        categoryIcons.put("Ăn uống", "@android:drawable/ic_menu_my_calendar");
        categoryIcons.put("Di chuyển", "@android:drawable/ic_menu_directions");
        categoryIcons.put("Mua sắm", "@android:drawable/ic_menu_my_calendar");
        categoryIcons.put("Giải trí", "@android:drawable/ic_menu_my_calendar");
        categoryIcons.put("Y tế", "@android:drawable/ic_menu_my_calendar");
        categoryIcons.put("Giáo dục", "@android:drawable/ic_menu_my_calendar");
        categoryIcons.put("Khác", "@android:drawable/ic_menu_more");

        categoryColors.put("Ăn uống", R.color.category_green);
        categoryColors.put("Di chuyển", R.color.category_blue);
        categoryColors.put("Mua sắm", R.color.category_purple);
        categoryColors.put("Giải trí", R.color.category_orange);
        categoryColors.put("Y tế", R.color.category_red);
        categoryColors.put("Giáo dục", R.color.category_teal);
        categoryColors.put("Khác", R.color.category_orange);
    }

    private void setupViews() {
        tvCurrentMonth = findViewById(R.id.tvCurrentMonth);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvTotalExpense = findViewById(R.id.tvTotalExpense);
        tvBalance = findViewById(R.id.tvBalance);

        ImageView ivPrevMonth = findViewById(R.id.ivPrevMonth);
        ImageView ivNextMonth = findViewById(R.id.ivNextMonth);
        Button btnExportReport = findViewById(R.id.btnExportReport);

        ivPrevMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, -1);
            updateMonthDisplay();
            loadMonthlyData();
        });

        ivNextMonth.setOnClickListener(v -> {
            currentMonth.add(Calendar.MONTH, 1);
            updateMonthDisplay();
            loadMonthlyData();
        });

        btnExportReport.setOnClickListener(v -> {
            Toast.makeText(this, "Xuất báo cáo PDF", Toast.LENGTH_SHORT).show();
            // TODO: Implement PDF export
        });
    }

    private void updateMonthDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("'Tháng' MM, yyyy", new Locale("vi", "VN"));
        tvCurrentMonth.setText(sdf.format(currentMonth.getTime()));
    }

    private void loadMonthlyData() {
        // Get start and end dates for current month
        Calendar cal = Calendar.getInstance();
        cal.setTime(currentMonth.getTime());
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

        // Load expenses for the month
        expenseRepository.getExpensesByDateRange(startDate, endDate, new ExpenseRepository.DataCallback<List<Expense>>() {
            @Override
            public void onDataLoaded(List<Expense> expenses) {
                runOnUiThread(() -> {
                    calculateMonthlySummary(expenses);
                    updateCategoryBreakdown(expenses);
                });
            }
        });
    }

    private void calculateMonthlySummary(List<Expense> expenses) {
        double totalIncome = 0;
        double totalExpense = 0;

        for (Expense expense : expenses) {
            if ("Thu nhập".equals(expense.getType())) {
                totalIncome += expense.getAmount();
            } else {
                totalExpense += expense.getAmount();
            }
        }

        double balance = totalIncome - totalExpense;

        tvTotalIncome.setText(formatCurrency(totalIncome));
        tvTotalExpense.setText(formatCurrency(totalExpense));
        tvBalance.setText(formatCurrency(balance));

        // Set text color based on balance
        if (balance < 0) {
            tvBalance.setTextColor(getResources().getColor(R.color.expense_color));
        } else {
            tvBalance.setTextColor(getResources().getColor(R.color.income_color));
        }
    }

    private void updateCategoryBreakdown(List<Expense> expenses) {
        // Calculate total expense for percentage calculation
        double totalExpense = 0;
        for (Expense expense : expenses) {
            if (!"Thu nhập".equals(expense.getType())) {
                totalExpense += expense.getAmount();
            }
        }

        // Group expenses by category
        Map<String, Double> categoryTotals = new HashMap<>();
        for (Expense expense : expenses) {
            if (!"Thu nhập".equals(expense.getType())) {
                String category = expense.getCategory();
                double currentTotal = categoryTotals.getOrDefault(category, 0.0);
                categoryTotals.put(category, currentTotal + expense.getAmount());
            }
        }

        // TODO: Update the UI with category breakdown
        // You'll need to add RecyclerView or dynamically update LinearLayout
        // to show the actual categories from the database
    }

    private String formatCurrency(double amount) {
        try {
            // Remove currency symbol and format with đ
            String formatted = currencyFormatter.format(amount);
            formatted = formatted.replace("₫", "").trim() + "đ";
            return formatted;
        } catch (Exception e) {
            return String.format(Locale.getDefault(), "%,.0fđ", amount);
        }
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
        loadMonthlyData();
    }
}