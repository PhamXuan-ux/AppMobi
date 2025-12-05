package com.example.appexpensemanagement.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.adapter.CategoryAdapter;
import com.example.appexpensemanagement.adapter.ExpenseAdapter;
import com.example.appexpensemanagement.model.Expense;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class DashboardActivity extends AppCompatActivity {
    private TextView tvTotalExpenses, tvTotalIncome, tvBalance, tvGreeting, tvTotalExpensesOverview;
    private RecyclerView recyclerViewExpenses, recyclerViewCategories;
    private ExpenseAdapter expenseAdapter;
    private CategoryAdapter categoryAdapter;
    private ExpenseRepository repository;
    private FloatingActionButton fabAdd;
    private TabLayout tabLayout;
    private Spinner spinnerMonth;
    private List<Expense> expenseList;
    private String currentTab = "Chi";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        
        repository = new ExpenseRepository(getApplication());
        expenseList = new ArrayList<>();
        
        initViews();
        setupRecyclerViews();
        setupTabs();
        setupBottomNavigation();
        loadData();
        
        fabAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTransactionTypeDialog();
            }
        });
    }
    
    private void initViews() {
        tvTotalExpenses = findViewById(R.id.tvTotalExpenses);
        tvTotalIncome = findViewById(R.id.tvTotalIncome);
        tvBalance = findViewById(R.id.tvBalance);
        tvGreeting = findViewById(R.id.tvGreeting);
        tvTotalExpensesOverview = findViewById(R.id.tvTotalExpensesOverview);
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses);
        recyclerViewCategories = findViewById(R.id.recyclerViewCategories);
        fabAdd = findViewById(R.id.fabAdd);
        tabLayout = findViewById(R.id.tabLayout);
        spinnerMonth = findViewById(R.id.spinnerMonth);
        
        SharedPreferences prefs = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        String userName = prefs.getString("userName", "Hồng");
        tvGreeting.setText("Xin chào, " + userName);
        
        // Setup month spinner
        String[] months = {"Tháng này", "Tháng trước", "2 tháng trước"};
        ArrayAdapter<String> monthAdapter = new ArrayAdapter<>(this, 
            android.R.layout.simple_spinner_item, months);
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMonth.setAdapter(monthAdapter);
    }
    
    private void setupRecyclerViews() {
        // Expense RecyclerView
        expenseAdapter = new ExpenseAdapter(expenseList, new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Expense expense) {
                Intent intent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
                intent.putExtra("expense", expense);
                startActivity(intent);
            }
        });
        recyclerViewExpenses.setLayoutManager(new LinearLayoutManager(this));
        recyclerViewExpenses.setAdapter(expenseAdapter);
        
        // Category RecyclerView
        categoryAdapter = new CategoryAdapter(new ArrayList<>());
        recyclerViewCategories.setLayoutManager(new LinearLayoutManager(this, 
            LinearLayoutManager.HORIZONTAL, false));
        recyclerViewCategories.setAdapter(categoryAdapter);
    }
    
    private void setupTabs() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getText().toString();
                filterExpenses();
            }
            
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}
            
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
    }
    
    private void setupBottomNavigation() {
        ImageView ivHome = findViewById(R.id.ivHome);
        ImageView ivTransactions = findViewById(R.id.ivTransactions);
        ImageView ivNotifications = findViewById(R.id.ivNotifications);
        ImageView ivProfile = findViewById(R.id.ivProfile);
        
        ivHome.setOnClickListener(v -> {
            // Already on home
        });
        
        ivTransactions.setOnClickListener(v -> {
            startActivity(new Intent(this, StatisticsActivity.class));
        });
        
        ivNotifications.setOnClickListener(v -> {
            // Show notifications
        });
        
        ivProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, ProfileActivity.class));
        });
    }
    
    private void filterExpenses() {
        repository.getAllExpenses(new ExpenseRepository.DataCallback<List<Expense>>() {
            @Override
            public void onDataLoaded(List<Expense> allExpenses) {
                runOnUiThread(() -> {
                    List<Expense> filtered = new ArrayList<>();
                    for (Expense expense : allExpenses) {
                        if (expense.getType().equals(currentTab)) {
                            filtered.add(expense);
                        }
                    }
                    expenseList.clear();
                    expenseList.addAll(filtered);
                    expenseAdapter.notifyDataSetChanged();
                    
                    // Show/hide empty state
                    LinearLayout llEmptyState = findViewById(R.id.llEmptyState);
                    if (llEmptyState != null) {
                        llEmptyState.setVisibility(filtered.isEmpty() ? View.VISIBLE : View.GONE);
                        recyclerViewExpenses.setVisibility(filtered.isEmpty() ? View.GONE : View.VISIBLE);
                    }
                });
            }
        });
    }
    
    private void loadData() {
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
        
        // Load totals asynchronously
        repository.getTotalExpenses(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
            @Override
            public void onDataLoaded(Double totalExpenses) {
                repository.getTotalIncome(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
                    @Override
                    public void onDataLoaded(Double totalIncome) {
                        runOnUiThread(() -> {
                            double balance = totalIncome - totalExpenses;
                            
        // Format without "đ" for main expense display - matching design
        tvTotalExpenses.setText(formatNumber(totalExpenses));
                            tvTotalExpensesOverview.setText(formatCurrency(totalExpenses));
                            tvTotalIncome.setText(formatCurrency(totalIncome));
                            tvBalance.setText(formatCurrency(balance));
                        });
                    }
                });
            }
        });
        
        // Load category spending
        loadCategorySpending(startDate, endDate);
        
        // Load expenses
        filterExpenses();
        
        // Check for budget alerts
        checkBudgetAlerts();
    }
    
    private void loadCategorySpending(Date startDate, Date endDate) {
        repository.getExpensesByDateRange(startDate, endDate, new ExpenseRepository.DataCallback<List<Expense>>() {
            @Override
            public void onDataLoaded(List<Expense> expenses) {
                runOnUiThread(() -> {
                    Map<String, Double> categoryTotals = new HashMap<>();
                    double totalSpending = 0;
                    
                    for (Expense expense : expenses) {
                        if ("Chi".equals(expense.getType())) {
                            String category = expense.getCategory();
                            categoryTotals.put(category, 
                                categoryTotals.getOrDefault(category, 0.0) + expense.getAmount());
                            totalSpending += expense.getAmount();
                        }
                    }
                    
                    List<CategoryAdapter.CategoryItem> categoryItems = new ArrayList<>();
                    // Colors matching the design: Purple, Blue, Orange
                    int[] colors = {0xFF9C27B0, 0xFF2196F3, 0xFFFF9800};
                    
                    int colorIndex = 0;
                    for (Map.Entry<String, Double> entry : categoryTotals.entrySet()) {
                        double percentage = totalSpending > 0 ? (entry.getValue() / totalSpending) * 100 : 0;
                        categoryItems.add(new CategoryAdapter.CategoryItem(
                            entry.getKey(),
                            "", // No icon in new design
                            entry.getValue(),
                            percentage,
                            colors[colorIndex % colors.length]
                        ));
                        colorIndex++;
                    }
                    
                    categoryAdapter = new CategoryAdapter(categoryItems);
                    recyclerViewCategories.setAdapter(categoryAdapter);
                });
            }
        });
    }
    
    private String getCategoryIcon(String category) {
        Map<String, String> iconMap = new HashMap<>();
        iconMap.put("Ăn uống", "🍽️");
        iconMap.put("Đi lại", "🚗");
        iconMap.put("Mua sắm", "🛍️");
        iconMap.put("Giải trí", "🎬");
        iconMap.put("Sức khỏe", "🏥");
        iconMap.put("Giáo dục", "📚");
        iconMap.put("Cần thiết", "💼");
        iconMap.put("Đầu tư", "📈");
        iconMap.put("Hưởng thụ", "🎁");
        return iconMap.getOrDefault(category, "💰");
    }
    
    private void checkBudgetAlerts() {
        Calendar cal = Calendar.getInstance();
        int month = cal.get(Calendar.MONTH) + 1;
        int year = cal.get(Calendar.YEAR);
        
        repository.getExceededBudgets(month, year, new ExpenseRepository.DataCallback<List<com.example.appexpensemanagement.model.Budget>>() {
            @Override
            public void onDataLoaded(List<com.example.appexpensemanagement.model.Budget> exceededBudgets) {
                runOnUiThread(() -> {
                    if (!exceededBudgets.isEmpty()) {
                        StringBuilder message = new StringBuilder("Cảnh báo: Bạn đã vượt quá ngân sách cho ");
                        for (com.example.appexpensemanagement.model.Budget budget : exceededBudgets) {
                            message.append(budget.getCategoryName()).append(", ");
                        }
                        message.setLength(message.length() - 2);
                        
                        android.widget.Toast.makeText(DashboardActivity.this, message.toString(), 
                            android.widget.Toast.LENGTH_LONG).show();
                    }
                });
            }
        });
    }
    
    private String formatCurrency(double amount) {
        return String.format(Locale.getDefault(), "%,.0f đ", amount);
    }
    
    private String formatNumber(double amount) {
        return String.format(Locale.getDefault(), "%,.0f", amount);
    }
    
    private void showTransactionTypeDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_transaction_type, null);
        Button btnIncome = dialogView.findViewById(R.id.btnIncome);
        Button btnExpense = dialogView.findViewById(R.id.btnExpense);
        Button btnCancel = dialogView.findViewById(R.id.btnCancel);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();
        
        btnIncome.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
            intent.putExtra("type", "Thu");
            startActivity(intent);
        });
        
        btnExpense.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(DashboardActivity.this, AddExpenseActivity.class);
            intent.putExtra("type", "Chi");
            startActivity(intent);
        });
        
        btnCancel.setOnClickListener(v -> dialog.dismiss());
        
        dialog.show();
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadData();
    }
}
