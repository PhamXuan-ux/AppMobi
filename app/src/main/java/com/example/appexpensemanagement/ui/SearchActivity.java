package com.example.appexpensemanagement.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.adapter.ExpenseAdapter;
import com.example.appexpensemanagement.model.Expense;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import java.util.ArrayList;
import java.util.List;

public class SearchActivity extends AppCompatActivity {
    private EditText etSearch;
    private RecyclerView recyclerView;
    private ExpenseAdapter adapter;
    private ExpenseRepository repository;
    private List<Expense> allExpenses;
    private List<Expense> filteredExpenses;
    private TextView tvResults;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Tìm kiếm");
        }
        
        repository = new ExpenseRepository(getApplication());
        allExpenses = new ArrayList<>();
        filteredExpenses = new ArrayList<>();
        
        initViews();
        setupRecyclerView();
        loadAllExpenses();
        
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterExpenses(s.toString());
            }
            
            @Override
            public void afterTextChanged(Editable s) {}
        });
    }
    
    private void initViews() {
        etSearch = findViewById(R.id.etSearch);
        recyclerView = findViewById(R.id.recyclerView);
        tvResults = findViewById(R.id.tvResults);
    }
    
    private void setupRecyclerView() {
        adapter = new ExpenseAdapter(filteredExpenses, new ExpenseAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(Expense expense) {
                // Handle click
            }
        });
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
    }
    
    private void loadAllExpenses() {
        repository.getAllExpenses(new ExpenseRepository.DataCallback<List<Expense>>() {
            @Override
            public void onDataLoaded(List<Expense> expenses) {
                runOnUiThread(() -> {
                    allExpenses.clear();
                    allExpenses.addAll(expenses);
                    filteredExpenses.clear();
                    filteredExpenses.addAll(expenses);
                    adapter.notifyDataSetChanged();
                    updateResultsCount();
                });
            }
        });
    }
    
    private void filterExpenses(String query) {
        filteredExpenses.clear();
        
        if (query.isEmpty()) {
            filteredExpenses.addAll(allExpenses);
        } else {
            String lowerQuery = query.toLowerCase();
            for (Expense expense : allExpenses) {
                if (expense.getCategory().toLowerCase().contains(lowerQuery) ||
                    expense.getNote().toLowerCase().contains(lowerQuery) ||
                    String.valueOf(expense.getAmount()).contains(query)) {
                    filteredExpenses.add(expense);
                }
            }
        }
        
        adapter.notifyDataSetChanged();
        updateResultsCount();
    }
    
    private void updateResultsCount() {
        tvResults.setText(String.format("Tìm thấy %d kết quả", filteredExpenses.size()));
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}

