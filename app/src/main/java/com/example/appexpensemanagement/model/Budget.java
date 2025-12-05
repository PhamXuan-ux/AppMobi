package com.example.appexpensemanagement.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

@Entity(tableName = "budgets")
public class Budget {
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    private String categoryName;
    
    @ColumnInfo(name = "budget_limit")
    private double limit;
    
    private double spent;
    private int month;
    private int year;
    
    public Budget() {
    }
    
    @Ignore
    public Budget(String categoryName, double limit, double spent, int month, int year) {
        this.categoryName = categoryName;
        this.limit = limit;
        this.spent = spent;
        this.month = month;
        this.year = year;
    }
    
    // Getters and Setters
    public long getId() {
        return id;
    }
    
    public void setId(long id) {
        this.id = id;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public double getLimit() {
        return limit;
    }
    
    public void setLimit(double limit) {
        this.limit = limit;
    }
    
    public double getSpent() {
        return spent;
    }
    
    public void setSpent(double spent) {
        this.spent = spent;
    }
    
    public int getMonth() {
        return month;
    }
    
    public void setMonth(int month) {
        this.month = month;
    }
    
    public int getYear() {
        return year;
    }
    
    public void setYear(int year) {
        this.year = year;
    }
    
    public boolean isExceeded() {
        return spent > limit;
    }
    
    public double getRemaining() {
        return limit - spent;
    }
    
    public double getPercentage() {
        if (limit == 0) return 0;
        return (spent / limit) * 100;
    }
}

