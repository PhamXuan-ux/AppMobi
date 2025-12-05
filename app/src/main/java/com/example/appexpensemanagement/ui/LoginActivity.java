package com.example.appexpensemanagement.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.appexpensemanagement.R;

import java.util.regex.Pattern;

public class LoginActivity extends AppCompatActivity {
    private EditText etUsername, etPassword;
    private Button btnLogin, btnAnonymousLogin;
    private CheckBox cbRememberMe;
    private TextView tvForgotPassword, tvRegister;
    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    private static final String PREF_NAME = "AppPrefs";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_PASSWORD = "password";
    private static final String KEY_REMEMBER_ME = "rememberMe";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_IS_ANONYMOUS = "isAnonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        initViews();
        sharedPreferences = getSharedPreferences(PREF_NAME, MODE_PRIVATE);
        editor = sharedPreferences.edit();

        // Check if user is already logged in
        if (sharedPreferences.getBoolean(KEY_IS_LOGGED_IN, false)) {
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
            return;
        }

        // Load saved credentials if Remember Me is checked
        loadSavedCredentials();

        setupClickListeners();
    }

    private void initViews() {
        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnAnonymousLogin = findViewById(R.id.btnAnonymousLogin);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        tvForgotPassword = findViewById(R.id.tvForgotPassword);
        tvRegister = findViewById(R.id.tvRegister);
    }

    private void loadSavedCredentials() {
        boolean rememberMe = sharedPreferences.getBoolean(KEY_REMEMBER_ME, false);
        if (rememberMe) {
            String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
            String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
            etUsername.setText(savedUsername);
            etPassword.setText(savedPassword);
            cbRememberMe.setChecked(true);
        }
    }

    private void setupClickListeners() {
        btnLogin.setOnClickListener(v -> handleLogin());

        btnAnonymousLogin.setOnClickListener(v -> handleAnonymousLogin());

        tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());

        tvRegister.setOnClickListener(v -> showRegisterDialog());
    }

    private void handleLogin() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(username)) {
            etUsername.setError("Vui lòng nhập tên đăng nhập hoặc email");
            etUsername.requestFocus();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            etPassword.setError("Vui lòng nhập mật khẩu");
            etPassword.requestFocus();
            return;
        }

        // Check if user exists (in a real app, this would check against a database)
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        String savedPassword = sharedPreferences.getString(KEY_PASSWORD, "");
        String savedEmail = sharedPreferences.getString("registered_email", "");

        // Check credentials
        boolean isValid = false;
        if (!TextUtils.isEmpty(savedUsername) && !TextUtils.isEmpty(savedPassword)) {
            // Check with saved credentials
            if ((username.equals(savedUsername) || username.equals(savedEmail)) &&
                    password.equals(savedPassword)) {
                isValid = true;
            }
        } else if (username.equals("admin") && password.equals("admin123")) {
            // Default admin account
            isValid = true;
        }

        if (isValid) {
            // Save Remember Me preference
            if (cbRememberMe.isChecked()) {
                editor.putString(KEY_USERNAME, username);
                editor.putString(KEY_PASSWORD, password);
                editor.putBoolean(KEY_REMEMBER_ME, true);
            } else {
                editor.remove(KEY_USERNAME);
                editor.remove(KEY_PASSWORD);
                editor.putBoolean(KEY_REMEMBER_ME, false);
            }

            // Mark as logged in
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putBoolean(KEY_IS_ANONYMOUS, false);

            // Set user info
            String savedName = sharedPreferences.getString(KEY_USER_NAME, "Người dùng");
            editor.putString(KEY_USER_NAME, savedName);

            editor.apply();

            Toast.makeText(this, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Tên đăng nhập hoặc mật khẩu không đúng!", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleAnonymousLogin() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng nhập ẩn danh");
        builder.setMessage("Bạn sẽ sử dụng ứng dụng mà không cần tài khoản. Dữ liệu sẽ được lưu cục bộ trên thiết bị của bạn.");
        builder.setPositiveButton("Tiếp tục", (dialog, which) -> {
            // Mark as anonymous user
            editor.putBoolean(KEY_IS_LOGGED_IN, true);
            editor.putBoolean(KEY_IS_ANONYMOUS, true);
            editor.putString(KEY_USER_NAME, "Khách");
            editor.putString(KEY_USER_EMAIL, "");
            editor.apply();

            Toast.makeText(LoginActivity.this, "Đăng nhập ẩn danh thành công!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
            finish();
        });
        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showForgotPasswordDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Quên mật khẩu");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("Nhập email của bạn");
        etEmail.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        layout.addView(etEmail);

        builder.setView(layout);

        builder.setPositiveButton("Gửi", (dialog, which) -> {
            String email = etEmail.getText().toString().trim();
            if (isValidEmail(email)) {
                // In a real app, this would send a reset password email
                // For demo, we'll just show a toast
                Toast.makeText(LoginActivity.this,
                        "Hướng dẫn đặt lại mật khẩu đã được gửi đến email của bạn!",
                        Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(LoginActivity.this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private void showRegisterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng ký tài khoản");

        View view = LayoutInflater.from(this).inflate(R.layout.dialog_register, null);
        final EditText etRegName = view.findViewById(R.id.etRegName);
        final EditText etRegEmail = view.findViewById(R.id.etRegEmail);
        final EditText etRegUsername = view.findViewById(R.id.etRegUsername);
        final EditText etRegPassword = view.findViewById(R.id.etRegPassword);
        final EditText etRegConfirmPassword = view.findViewById(R.id.etRegConfirmPassword);

        builder.setView(view);

        builder.setPositiveButton("Đăng ký", (dialog, which) -> {
            String name = etRegName.getText().toString().trim();
            String email = etRegEmail.getText().toString().trim();
            String username = etRegUsername.getText().toString().trim();
            String password = etRegPassword.getText().toString().trim();
            String confirmPassword = etRegConfirmPassword.getText().toString().trim();

            if (validateRegistration(name, email, username, password, confirmPassword)) {
                // Save registration data
                editor.putString("registered_name", name);
                editor.putString("registered_email", email);
                editor.putString(KEY_USERNAME, username);
                editor.putString(KEY_PASSWORD, password);
                editor.putBoolean(KEY_IS_LOGGED_IN, true);
                editor.putBoolean(KEY_IS_ANONYMOUS, false);
                editor.putString(KEY_USER_NAME, name);
                editor.putString(KEY_USER_EMAIL, email);
                editor.apply();

                Toast.makeText(LoginActivity.this, "Đăng ký thành công!", Toast.LENGTH_SHORT).show();
                startActivity(new Intent(this, DashboardActivity.class));
                finish();
            }
        });

        builder.setNegativeButton("Hủy", null);
        builder.show();
    }

    private boolean validateRegistration(String name, String email, String username,
                                         String password, String confirmPassword) {
        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, "Vui lòng nhập họ tên!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Vui lòng nhập email!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!isValidEmail(email)) {
            Toast.makeText(this, "Email không hợp lệ!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, "Vui lòng nhập tên đăng nhập!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (username.length() < 3) {
            Toast.makeText(this, "Tên đăng nhập phải có ít nhất 3 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Vui lòng nhập mật khẩu!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (password.length() < 6) {
            Toast.makeText(this, "Mật khẩu phải có ít nhất 6 ký tự!", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Mật khẩu xác nhận không khớp!", Toast.LENGTH_SHORT).show();
            return false;
        }

        // Check if username already exists (in a real app)
        String savedUsername = sharedPreferences.getString(KEY_USERNAME, "");
        if (username.equals(savedUsername)) {
            Toast.makeText(this, "Tên đăng nhập đã tồn tại!", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private boolean isValidEmail(String email) {
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
        return Pattern.compile(emailPattern).matcher(email).matches();
    }

    // Method to handle logout (can be called from other activities)
    public static void performLogout(SharedPreferences sharedPreferences) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_ANONYMOUS, false);
        editor.apply();
    }

    // Method to check if user is anonymous
    public static boolean isAnonymousUser(SharedPreferences sharedPreferences) {
        return sharedPreferences.getBoolean(KEY_IS_ANONYMOUS, false);
    }
}
