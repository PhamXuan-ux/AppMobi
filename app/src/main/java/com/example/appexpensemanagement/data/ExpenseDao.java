package com.example.appexpensemanagement.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.appexpensemanagement.model.Expense;
import java.util.Date;
import java.util.List;

@Dao
public interface ExpenseDao {
    @Query("SELECT * FROM expenses ORDER BY date DESC")
    List<Expense> getAllExpenses();
    
    @Query("SELECT * FROM expenses WHERE type = :type ORDER BY date DESC")
    List<Expense> getExpensesByType(String type);
    
    @Query("SELECT * FROM expenses WHERE category = :category ORDER BY date DESC")
    List<Expense> getExpensesByCategory(String category);
    
    @Query("SELECT * FROM expenses WHERE date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    List<Expense> getExpensesByDateRange(Date startDate, Date endDate);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'Chi' AND date BETWEEN :startDate AND :endDate")
    double getTotalExpenses(Date startDate, Date endDate);
    
    @Query("SELECT SUM(amount) FROM expenses WHERE type = 'Thu' AND date BETWEEN :startDate AND :endDate")
    double getTotalIncome(Date startDate, Date endDate);
    
    @Query("SELECT * FROM expenses WHERE id = :id")
    Expense getExpenseById(long id);
    
    @Insert
    long insertExpense(Expense expense);
    
    @Update
    void updateExpense(Expense expense);
    
    @Delete
    void deleteExpense(Expense expense);
    
}

