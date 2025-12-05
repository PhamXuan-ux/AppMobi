package com.example.appexpensemanagement.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.appexpensemanagement.model.Budget;
import java.util.List;

@Dao
public interface BudgetDao {
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year")
    List<Budget> getBudgetsByMonth(int month, int year);
    
    @Query("SELECT * FROM budgets WHERE categoryName = :categoryName AND month = :month AND year = :year")
    Budget getBudgetByCategoryAndMonth(String categoryName, int month, int year);
    
    @Query("SELECT * FROM budgets WHERE month = :month AND year = :year AND spent > budget_limit")
    List<Budget> getExceededBudgets(int month, int year);
    
    @Insert
    long insertBudget(Budget budget);
    
    @Update
    void updateBudget(Budget budget);
    
    @Delete
    void deleteBudget(Budget budget);
    
    @Query("UPDATE budgets SET spent = :spent WHERE categoryName = :categoryName AND month = :month AND year = :year")
    void updateSpent(String categoryName, double spent, int month, int year);
    
    @Query("UPDATE budgets SET budget_limit = :limit WHERE categoryName = :categoryName AND month = :month AND year = :year")
    void updateLimit(String categoryName, double limit, int month, int year);
}

