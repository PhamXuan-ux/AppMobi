package com.example.appexpensemanagement.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.adapter.BudgetAdapter;
import com.example.appexpensemanagement.model.Budget;
import com.example.appexpensemanagement.model.Category;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class BudgetActivity extends AppCompatActivity {
    private RecyclerView recyclerView;
    private BudgetAdapter adapter;
    private ExpenseRepository repository;
    private List<Budget> budgetList;
    private int currentMonth, currentYear;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_budget);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        
        repository = new ExpenseRepository(getApplication());
        Calendar cal = Calendar.getInstance();
        currentMonth = cal.get(Calendar.MONTH) + 1;
        currentYear = cal.get(Calendar.YEAR);
        
        budgetList = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        loadBudgets();
    }
    
    private void initViews() {
        recyclerView = findViewById(R.id.recyclerViewBudgets);
        TextView tvMonth = findViewById(R.id.tvMonth);
        tvMonth.setText(String.format("Tháng %d/%d", currentMonth, currentYear));
    }
    
    private void setupRecyclerView() {
        adapter = new BudgetAdapter(budgetList, new BudgetAdapter.OnBudgetClickListener() {
            @Override
            public void onEditClick(Budget budget) {
                showEditBudgetDialog(budget);
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadBudgets() {
        // Ensure categories exist
        repository.getCategoriesByType("Chi", new ExpenseRepository.DataCallback<List<Category>>() {
            @Override
            public void onDataLoaded(List<Category> categories) {
                if (categories.isEmpty()) {
                    // Create default categories
                    repository.insertCategory(new Category("Ăn uống", "🍽️", "Chi", 0xFFE91E63));
                    repository.insertCategory(new Category("Đi lại", "🚗", "Chi", 0xFF2196F3));
                    repository.insertCategory(new Category("Mua sắm", "🛍️", "Chi", 0xFFFF9800));
                    repository.insertCategory(new Category("Giải trí", "🎬", "Chi", 0xFF9C27B0));
                    repository.insertCategory(new Category("Sức khỏe", "🏥", "Chi", 0xFF4CAF50));
                    repository.insertCategory(new Category("Giáo dục", "📚", "Chi", 0xFF00BCD4));
                    repository.getCategoriesByType("Chi", new ExpenseRepository.DataCallback<List<Category>>() {
                        @Override
                        public void onDataLoaded(List<Category> reloadedCategories) {
                            loadBudgetsForCategories(reloadedCategories);
                        }
                    });
                } else {
                    loadBudgetsForCategories(categories);
                }
            }
        });
    }
    
    private void loadBudgetsForCategories(List<Category> categories) {
        repository.getBudgetsByMonth(currentMonth, currentYear, new ExpenseRepository.DataCallback<List<Budget>>() {
            @Override
            public void onDataLoaded(List<Budget> budgets) {
                runOnUiThread(() -> {
                    if (budgets.isEmpty()) {
                        // Create budgets for all expense categories
                        for (Category category : categories) {
                            Budget budget = new Budget(category.getName(), 0, 0, currentMonth, currentYear);
                            repository.insertBudget(budget);
                        }
                        repository.getBudgetsByMonth(currentMonth, currentYear, new ExpenseRepository.DataCallback<List<Budget>>() {
                            @Override
                            public void onDataLoaded(List<Budget> newBudgets) {
                                runOnUiThread(() -> {
                                    budgetList.clear();
                                    budgetList.addAll(newBudgets);
                                    adapter.notifyDataSetChanged();
                                    checkBudgetAlerts();
                                });
                            }
                        });
                    } else {
                        budgetList.clear();
                        budgetList.addAll(budgets);
                        adapter.notifyDataSetChanged();
                        checkBudgetAlerts();
                    }
                });
            }
        });
    }
    
    private void checkBudgetAlerts() {
        repository.getExceededBudgets(currentMonth, currentYear, new ExpenseRepository.DataCallback<List<Budget>>() {
            @Override
            public void onDataLoaded(List<Budget> exceededBudgets) {
                runOnUiThread(() -> {
                    for (Budget budget : exceededBudgets) {
                        if (budget.getLimit() > 0) {
                            Toast.makeText(BudgetActivity.this, 
                                String.format("Cảnh báo: %s đã vượt quá ngân sách!", budget.getCategoryName()),
                                Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
        });
    }
    
    private void showEditBudgetDialog(Budget budget) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_edit_budget, null);
        builder.setView(dialogView);
        
        TextView tvCategory = dialogView.findViewById(R.id.tvCategory);
        EditText etLimit = dialogView.findViewById(R.id.etLimit);
        Button btnSave = dialogView.findViewById(R.id.btnSave);
        
        tvCategory.setText(budget.getCategoryName());
        etLimit.setText(String.valueOf((long)budget.getLimit()));
        
        android.app.AlertDialog dialog = builder.create();
        
        btnSave.setOnClickListener(v -> {
            String limitStr = etLimit.getText().toString().trim();
            if (limitStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập hạn mức", Toast.LENGTH_SHORT).show();
                return;
            }
            
            double limit = Double.parseDouble(limitStr);
            budget.setLimit(limit);
            repository.updateBudget(budget);
            
            Toast.makeText(this, "Cập nhật hạn mức thành công!", Toast.LENGTH_SHORT).show();
            loadBudgets();
            dialog.dismiss();
        });
        
        dialog.show();
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        loadBudgets();
    }
}

