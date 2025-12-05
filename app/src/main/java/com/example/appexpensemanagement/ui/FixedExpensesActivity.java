package com.example.appexpensemanagement.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appexpensemanagement.R;

public class FixedExpensesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fixed_expenses);

        setupViews();
        setupBottomNavigation();
    }

    private void setupViews() {
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
            startActivity(new Intent(this, ProfileActivity.class));
            finish();
        });
    }
}