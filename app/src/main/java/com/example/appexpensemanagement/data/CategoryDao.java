package com.example.appexpensemanagement.data;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;
import com.example.appexpensemanagement.model.Category;
import java.util.List;

@Dao
public interface CategoryDao {
    @Query("SELECT * FROM categories")
    List<Category> getAllCategories();
    
    @Query("SELECT * FROM categories WHERE type = :type")
    List<Category> getCategoriesByType(String type);
    
    @Query("SELECT * FROM categories WHERE id = :id")
    Category getCategoryById(long id);
    
    @Query("SELECT * FROM categories WHERE name = :name")
    Category getCategoryByName(String name);
    
    @Insert
    long insertCategory(Category category);
    
    @Update
    void updateCategory(Category category);
    
    @Delete
    void deleteCategory(Category category);
}

