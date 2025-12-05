package com.example.appexpensemanagement.repository;

import android.app.Application;
import androidx.lifecycle.LiveData;
import com.example.appexpensemanagement.data.AppDatabase;
import com.example.appexpensemanagement.data.BudgetDao;
import com.example.appexpensemanagement.data.CategoryDao;
import com.example.appexpensemanagement.data.ExpenseDao;
import com.example.appexpensemanagement.model.Budget;
import com.example.appexpensemanagement.model.Category;
import com.example.appexpensemanagement.model.Expense;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ExpenseRepository {
    private ExpenseDao expenseDao;
    private CategoryDao categoryDao;
    private BudgetDao budgetDao;
    private ExecutorService executor;
    
    public ExpenseRepository(Application application) {
        AppDatabase database = AppDatabase.getDatabase(application);
        expenseDao = database.expenseDao();
        categoryDao = database.categoryDao();
        budgetDao = database.budgetDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    // Expense operations
    public void insertExpense(Expense expense) {
        executor.execute(() -> {
            long id = expenseDao.insertExpense(expense);
            updateBudgetSpent(expense);
        });
    }
    
    public void updateExpense(Expense oldExpense, Expense newExpense) {
        executor.execute(() -> {
            // Revert old expense from budget
            revertBudgetSpent(oldExpense);
            expenseDao.updateExpense(newExpense);
            updateBudgetSpent(newExpense);
        });
    }
    
    public void deleteExpense(Expense expense) {
        executor.execute(() -> {
            revertBudgetSpent(expense);
            expenseDao.deleteExpense(expense);
        });
    }
    
    public void getAllExpenses(DataCallback<List<Expense>> callback) {
        executor.execute(() -> {
            List<Expense> expenses = expenseDao.getAllExpenses();
            callback.onDataLoaded(expenses);
        });
    }
    
    public void getExpensesByType(String type, DataCallback<List<Expense>> callback) {
        executor.execute(() -> {
            List<Expense> expenses = expenseDao.getExpensesByType(type);
            callback.onDataLoaded(expenses);
        });
    }
    
    public void getExpensesByDateRange(Date startDate, Date endDate, DataCallback<List<Expense>> callback) {
        executor.execute(() -> {
            List<Expense> expenses = expenseDao.getExpensesByDateRange(startDate, endDate);
            callback.onDataLoaded(expenses);
        });
    }
    
    public void getTotalExpenses(Date startDate, Date endDate, DataCallback<Double> callback) {
        executor.execute(() -> {
            double total = expenseDao.getTotalExpenses(startDate, endDate);
            callback.onDataLoaded(total);
        });
    }
    
    public void getTotalIncome(Date startDate, Date endDate, DataCallback<Double> callback) {
        executor.execute(() -> {
            double total = expenseDao.getTotalIncome(startDate, endDate);
            callback.onDataLoaded(total);
        });
    }
    
    public interface DataCallback<T> {
        void onDataLoaded(T data);
    }
    
    // Category operations
    public void insertCategory(Category category) {
        executor.execute(() -> categoryDao.insertCategory(category));
    }
    
    public void updateCategory(Category category) {
        executor.execute(() -> categoryDao.updateCategory(category));
    }
    
    public void deleteCategory(Category category) {
        executor.execute(() -> categoryDao.deleteCategory(category));
    }
    
    public void getAllCategories(DataCallback<List<Category>> callback) {
        executor.execute(() -> {
            List<Category> categories = categoryDao.getAllCategories();
            callback.onDataLoaded(categories);
        });
    }
    
    public void getCategoriesByType(String type, DataCallback<List<Category>> callback) {
        executor.execute(() -> {
            List<Category> categories = categoryDao.getCategoriesByType(type);
            callback.onDataLoaded(categories);
        });
    }
    
    // Budget operations
    public void insertBudget(Budget budget) {
        executor.execute(() -> budgetDao.insertBudget(budget));
    }
    
    public void updateBudget(Budget budget) {
        executor.execute(() -> budgetDao.updateBudget(budget));
    }
    
    public void deleteBudget(Budget budget) {
        executor.execute(() -> budgetDao.deleteBudget(budget));
    }
    
    public void getBudgetsByMonth(int month, int year, DataCallback<List<Budget>> callback) {
        executor.execute(() -> {
            List<Budget> budgets = budgetDao.getBudgetsByMonth(month, year);
            // Recalculate spent amounts from expenses
            recalculateBudgetSpent(budgets, month, year);
            callback.onDataLoaded(budgets);
        });
    }
    
    private void recalculateBudgetSpent(List<Budget> budgets, int month, int year) {
        Calendar cal = Calendar.getInstance();
        cal.set(year, month - 1, 1, 0, 0, 0);
        Date startDate = cal.getTime();
        
        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();
        
        List<Expense> expenses = expenseDao.getExpensesByDateRange(startDate, endDate);
        
        // Reset all spent amounts
        for (Budget budget : budgets) {
            budget.setSpent(0);
        }
        
        // Calculate spent for each budget
        for (Expense expense : expenses) {
            if ("Chi".equals(expense.getType())) {
                for (Budget budget : budgets) {
                    if (budget.getCategoryName().equals(expense.getCategory())) {
                        budget.setSpent(budget.getSpent() + expense.getAmount());
                        break;
                    }
                }
            }
        }
        
        // Update database
        for (Budget budget : budgets) {
            budgetDao.updateSpent(budget.getCategoryName(), budget.getSpent(), month, year);
        }
    }
    
    public void getExceededBudgets(int month, int year, DataCallback<List<Budget>> callback) {
        executor.execute(() -> {
            List<Budget> budgets = budgetDao.getExceededBudgets(month, year);
            callback.onDataLoaded(budgets);
        });
    }
    
    private void updateBudgetSpent(Expense expense) {
        if ("Chi".equals(expense.getType())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(expense.getDate());
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            
            Budget budget = budgetDao.getBudgetByCategoryAndMonth(expense.getCategory(), month, year);
            if (budget != null) {
                double newSpent = budget.getSpent() + expense.getAmount();
                budgetDao.updateSpent(expense.getCategory(), newSpent, month, year);
            }
        }
    }
    
    private void revertBudgetSpent(Expense expense) {
        if ("Chi".equals(expense.getType())) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(expense.getDate());
            int month = cal.get(Calendar.MONTH) + 1;
            int year = cal.get(Calendar.YEAR);
            
            Budget budget = budgetDao.getBudgetByCategoryAndMonth(expense.getCategory(), month, year);
            if (budget != null) {
                double newSpent = Math.max(0, budget.getSpent() - expense.getAmount());
                budgetDao.updateSpent(expense.getCategory(), newSpent, month, year);
            }
        }
    }
}

