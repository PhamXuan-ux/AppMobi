package com.example.appexpensemanagement.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.appexpensemanagement.R;

public class LoginActivity extends AppCompatActivity {
    private Button btnAnonymousLogin;
    private SharedPreferences sharedPreferences;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        sharedPreferences = getSharedPreferences("AppPrefs", MODE_PRIVATE);
        
        // Check if user is already logged in
        if (sharedPreferences.getBoolean("isLoggedIn", false)) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }
        
        btnAnonymousLogin = findViewById(R.id.btnAnonymousLogin);
        btnAnonymousLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Anonymous login - just mark as logged in
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean("isLoggedIn", true);
                editor.putString("userName", "Xuan Cung");
                editor.apply();
                
                Toast.makeText(LoginActivity.this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                finish();
            }
        });
    }
}

