package com.example.appexpensemanagement.ui;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewParent;
import android.widget.EditText;
import android.widget.ImageView;  // THÊM IMPORT NÀY
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ProfileActivity extends AppCompatActivity {
    private PieChart progressChart;
    private TextView tvCurrentAmount, tvProgressPercent, tvTargetAmount;
    private ExpenseRepository repository;
    private SharedPreferences prefs;
    private static final String PREFS_NAME = "AppPrefs";
    private static final String KEY_PERSONAL_GOAL = "personalGoal";
    private static final String KEY_USER_NAME = "userName";
    private static final String KEY_USER_EMAIL = "userEmail";
    private static final String KEY_IS_LOGGED_IN = "isLoggedIn";
    private static final String KEY_IS_ANONYMOUS = "isAnonymous";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        repository = new ExpenseRepository(getApplication());
        prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);

        initViews();
        loadPersonalGoal();
        setupBottomNavigation();
        setupMenuItems();
        setupPersonalGoalClick();
    }

    private void initViews() {
        progressChart = findViewById(R.id.progressChart);
        tvCurrentAmount = findViewById(R.id.tvCurrentAmount);
        tvProgressPercent = findViewById(R.id.tvProgressPercent);
        tvTargetAmount = findViewById(R.id.tvTargetAmount);
    }

    private void setupPersonalGoalClick() {
        // Đơn giản hóa: thêm click listener trực tiếp cho FrameLayout chứa PieChart
        View parent = (View) progressChart.getParent();
        if (parent instanceof android.widget.FrameLayout) {
            android.widget.FrameLayout frameLayout = (android.widget.FrameLayout) parent;
            frameLayout.setOnClickListener(v -> showGoalSettingDialog());
        } else {
            // Nếu không tìm thấy, thêm click listener cho chính PieChart
            progressChart.setOnClickListener(v -> showGoalSettingDialog());
        }
    }

    private void setupMenuItems() {
        LinearLayout llPersonalInfo = findViewById(R.id.llPersonalInfo);
        LinearLayout llMonthlyReport = findViewById(R.id.llMonthlyReport);
        LinearLayout llFixedExpenses = findViewById(R.id.llFixedExpenses);
        LinearLayout llHelp = findViewById(R.id.llHelp);

        llPersonalInfo.setOnClickListener(v -> openPersonalInfo());
        llMonthlyReport.setOnClickListener(v -> openMonthlyReport());
        llFixedExpenses.setOnClickListener(v -> openFixedExpenses());
        llHelp.setOnClickListener(v -> openHelp());
    }

    private void showGoalSettingDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thiết lập mục tiêu tiết kiệm");

        // Tạo layout động
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        TextView textView = new TextView(this);
        textView.setText("Nhập mục tiêu tiết kiệm hàng tháng");
        textView.setTextSize(16);
        textView.setTextColor(getResources().getColor(R.color.text_primary));
        textView.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        layout.addView(textView);

        final EditText etGoalAmount = new EditText(this);
        etGoalAmount.setHint("Số tiền");
        etGoalAmount.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        etGoalAmount.setPadding(20, 20, 20, 20);
        etGoalAmount.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        etGoalAmount.getLayoutParams().height = 120;

        // Hiển thị mục tiêu hiện tại
        double currentGoal = Double.longBitsToDouble(prefs.getLong(KEY_PERSONAL_GOAL, Double.doubleToLongBits(7000000)));
        etGoalAmount.setText(String.valueOf((int) currentGoal));

        layout.addView(etGoalAmount);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String amountStr = etGoalAmount.getText().toString();
            if (!amountStr.isEmpty()) {
                try {
                    double newGoal = Double.parseDouble(amountStr);
                    if (newGoal > 0) {
                        savePersonalGoal(newGoal);
                        loadPersonalGoal();
                        Toast.makeText(this, "Đã cập nhật mục tiêu", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, "Mục tiêu phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
                }
            }
        });

        builder.setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void savePersonalGoal(double goal) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putLong(KEY_PERSONAL_GOAL, Double.doubleToLongBits(goal));
        editor.apply();
    }

    private void loadPersonalGoal() {
        double targetAmount = Double.longBitsToDouble(prefs.getLong(KEY_PERSONAL_GOAL, Double.doubleToLongBits(7000000)));

        // Calculate current spent amount for this month
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.DAY_OF_MONTH, 1);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        Date startDate = cal.getTime();

        cal.add(Calendar.MONTH, 1);
        cal.add(Calendar.DAY_OF_MONTH, -1);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        Date endDate = cal.getTime();

        repository.getTotalExpenses(startDate, endDate, new ExpenseRepository.DataCallback<Double>() {
            @Override
            public void onDataLoaded(Double currentSpent) {
                runOnUiThread(() -> {
                    // Update UI
                    tvCurrentAmount.setText(formatCurrency(currentSpent));
                    tvTargetAmount.setText(formatCurrency(targetAmount));

                    // Calculate progress percentage
                    double progress = targetAmount > 0 ? (currentSpent / targetAmount) * 100 : 0;
                    if (progress > 100) progress = 100;

                    tvProgressPercent.setText(String.format(Locale.getDefault(), "%.0f%%", progress));

                    // Update progress chart
                    setupProgressChart(progress);
                });
            }
        });
    }

    private void setupProgressChart(double progressPercent) {
        progressChart.getDescription().setEnabled(false);
        progressChart.setRotationEnabled(false);
        progressChart.setHoleRadius(75f);
        progressChart.setTransparentCircleRadius(0f);
        progressChart.getLegend().setEnabled(false);
        progressChart.setDrawEntryLabels(false);
        progressChart.setRotationAngle(0f);
        progressChart.setTouchEnabled(false);

        float progress = (float) progressPercent;
        float remaining = 100f - progress;

        List<PieEntry> entries = new ArrayList<>();
        if (progress > 0) entries.add(new PieEntry(progress, ""));
        if (remaining > 0) entries.add(new PieEntry(remaining, ""));

        if (entries.isEmpty()) {
            entries.add(new PieEntry(100, ""));
        }

        PieDataSet dataSet = new PieDataSet(entries, "");

        int[] colors;
        if (progressPercent >= 80) {
            colors = new int[]{
                    Color.parseColor("#F44336"), // Red for high usage
                    Color.parseColor("#E0E0E0")
            };
        } else if (progressPercent >= 50) {
            colors = new int[]{
                    Color.parseColor("#FF9800"), // Orange for medium usage
                    Color.parseColor("#E0E0E0")
            };
        } else {
            colors = new int[]{
                    Color.parseColor("#2196F3"), // Blue for low usage
                    Color.parseColor("#E0E0E0")
            };
        }

        dataSet.setColors(colors);
        dataSet.setSliceSpace(0f);
        dataSet.setDrawValues(false);

        PieData pieData = new PieData(dataSet);
        progressChart.setData(pieData);
        progressChart.invalidate();
    }

    private String formatCurrency(double amount) {
        try {
            NumberFormat formatter = NumberFormat.getInstance(new Locale("vi", "VN"));
            return formatter.format(amount) + "đ";
        } catch (Exception e) {
            return String.format(Locale.getDefault(), "%,.0fđ", amount);
        }
    }

    private void openPersonalInfo() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Thông tin cá nhân");

        // Tạo layout động
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        // Tên
        TextView tvName = new TextView(this);
        tvName.setText("Họ và tên");
        tvName.setTextSize(16);
        tvName.setTextColor(getResources().getColor(R.color.text_primary));
        layout.addView(tvName);

        final EditText etName = new EditText(this);
        etName.setHint("Nhập họ tên");
        etName.setPadding(20, 20, 20, 20);
        etName.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        etName.getLayoutParams().height = 120;
        layout.addView(etName);

        // Email
        TextView tvEmail = new TextView(this);
        tvEmail.setText("Email");
        tvEmail.setTextSize(16);
        tvEmail.setTextColor(getResources().getColor(R.color.text_primary));
        tvEmail.setPadding(0, 20, 0, 0);
        layout.addView(tvEmail);

        final EditText etEmail = new EditText(this);
        etEmail.setHint("Nhập email");
        etEmail.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        etEmail.setPadding(20, 20, 20, 20);
        etEmail.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        ));
        etEmail.getLayoutParams().height = 120;
        layout.addView(etEmail);

        // Load current info
        String currentName = prefs.getString(KEY_USER_NAME, "Người dùng");
        String currentEmail = prefs.getString(KEY_USER_EMAIL, "");
        etName.setText(currentName);
        etEmail.setText(currentEmail);

        builder.setView(layout);

        builder.setPositiveButton("Lưu", (dialog, which) -> {
            String name = etName.getText().toString();
            String email = etEmail.getText().toString();

            SharedPreferences.Editor editor = prefs.edit();
            editor.putString(KEY_USER_NAME, name);
            editor.putString(KEY_USER_EMAIL, email);
            editor.apply();

            Toast.makeText(this, "Đã lưu thông tin", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("Hủy", null);

        builder.setNeutralButton("Đăng xuất", (dialog, which) -> showLogoutConfirmation());

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void openMonthlyReport() {
        Intent intent = new Intent(this, MonthlyReportActivity.class);
        startActivity(intent);
    }

    private void openFixedExpenses() {
        Intent intent = new Intent(this, FixedExpensesActivity.class);
        startActivity(intent);
    }

    private void openHelp() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Trợ giúp");
        builder.setMessage("Liên hệ hỗ trợ:\n" +
                "Email: support@expensemanagement.com\n" +
                "Hotline: 1900 1234\n\n" +
                "Phiên bản: 1.0.0");
        builder.setPositiveButton("OK", null);
        builder.show();
    }

    private void showLogoutConfirmation() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Đăng xuất");
        builder.setMessage("Bạn có chắc chắn muốn đăng xuất?");

        builder.setPositiveButton("Đăng xuất", (dialog, which) -> performLogout());
        builder.setNegativeButton("Hủy", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void performLogout() {
        // Clear user session
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean(KEY_IS_LOGGED_IN, false);
        editor.putBoolean(KEY_IS_ANONYMOUS, false);
        editor.apply();

        // Navigate to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void setupBottomNavigation() {
        // SỬA: Dùng ImageView thay vì TextView
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
            // Already here
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadPersonalGoal();
    }
}