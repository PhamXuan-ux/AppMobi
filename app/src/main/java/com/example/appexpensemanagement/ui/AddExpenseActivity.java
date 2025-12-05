package com.example.appexpensemanagement.ui;

import android.app.DatePickerDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.model.Category;
import com.example.appexpensemanagement.model.Expense;
import com.example.appexpensemanagement.repository.ExpenseRepository;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddExpenseActivity extends AppCompatActivity {
    private TextInputEditText etAmount, etNote;
    private TextView tvDate, tvCategory, tvTitle;
    private ImageView ivBack;
    private Button btnSave;
    private LinearLayout llKeyboard;
    private ExpenseRepository repository;
    private Expense currentExpense;
    private Calendar selectedDate;
    private List<Category> categories;
    private String transactionType = "Chi"; // "Chi" or "Thu"
    private Category selectedCategory;
    private String currentAmount = "0";
    
    // Vietnamese day names
    private static final String[] DAY_NAMES = {
        "Chủ nhật", "Thứ Hai", "Thứ Ba", "Thứ Tư", "Thứ Năm", "Thứ Sáu", "Thứ Bảy"
    };
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_expense);
        
        // Get transaction type from intent
        transactionType = getIntent().getStringExtra("type");
        if (transactionType == null) {
            transactionType = "Chi";
        }
        
        repository = new ExpenseRepository(getApplication());
        selectedDate = Calendar.getInstance();
        
        initViews();
        setupKeyboard();
        loadCategories();
        
        // Check if editing existing expense
        currentExpense = (Expense) getIntent().getSerializableExtra("expense");
        if (currentExpense != null) {
            transactionType = currentExpense.getType();
            loadExpenseData();
        }
        
        updateTitle();
        updateDateDisplay();
    }
    
    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        ivBack = findViewById(R.id.ivBack);
        tvDate = findViewById(R.id.tvDate);
        etAmount = findViewById(R.id.etAmount);
        tvCategory = findViewById(R.id.tvCategory);
        etNote = findViewById(R.id.etNote);
        btnSave = findViewById(R.id.btnSave);
        llKeyboard = findViewById(R.id.llKeyboard);
        
        ivBack.setOnClickListener(v -> finish());
        
        tvDate.setOnClickListener(v -> showDatePicker());
        
        tvCategory.setOnClickListener(v -> showCategoryDialog());
        
        etAmount.setOnClickListener(v -> showKeyboard());
        
        btnSave.setOnClickListener(v -> saveExpense());
        
        // Update button state when amount changes
        etAmount.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateSaveButtonState();
            }
            
            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });
    }
    
    private void updateTitle() {
        tvTitle.setText("Tiền " + ("Thu".equals(transactionType) ? "thu" : "chi"));
    }
    
    private void updateDateDisplay() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, dd/MM/yy", new Locale("vi", "VN"));
        String dateStr = sdf.format(selectedDate.getTime());
        // Capitalize first letter
        if (!dateStr.isEmpty()) {
            dateStr = dateStr.substring(0, 1).toUpperCase() + dateStr.substring(1);
        }
        tvDate.setText(dateStr);
    }
    
    private void setupKeyboard() {
        // Quick amount buttons
        findViewById(R.id.btnQuick100).setOnClickListener(v -> addQuickAmount(100000));
        findViewById(R.id.btnQuick200).setOnClickListener(v -> addQuickAmount(200000));
        findViewById(R.id.btnQuick300).setOnClickListener(v -> addQuickAmount(300000));
        
        // Number buttons
        findViewById(R.id.btnNum0).setOnClickListener(v -> appendNumber("0"));
        findViewById(R.id.btnNum1).setOnClickListener(v -> appendNumber("1"));
        findViewById(R.id.btnNum2).setOnClickListener(v -> appendNumber("2"));
        findViewById(R.id.btnNum3).setOnClickListener(v -> appendNumber("3"));
        findViewById(R.id.btnNum4).setOnClickListener(v -> appendNumber("4"));
        findViewById(R.id.btnNum5).setOnClickListener(v -> appendNumber("5"));
        findViewById(R.id.btnNum6).setOnClickListener(v -> appendNumber("6"));
        findViewById(R.id.btnNum7).setOnClickListener(v -> appendNumber("7"));
        findViewById(R.id.btnNum8).setOnClickListener(v -> appendNumber("8"));
        findViewById(R.id.btnNum9).setOnClickListener(v -> appendNumber("9"));
        
        // Operators
        findViewById(R.id.btnClear).setOnClickListener(v -> clearAmount());
        findViewById(R.id.btnPlus).setOnClickListener(v -> performOperation("+"));
        findViewById(R.id.btnMinus).setOnClickListener(v -> performOperation("-"));
        findViewById(R.id.btn000).setOnClickListener(v -> appendNumber("000"));
        findViewById(R.id.btnOK).setOnClickListener(v -> hideKeyboard());
    }
    
    private void showKeyboard() {
        llKeyboard.setVisibility(View.VISIBLE);
    }
    
    private void hideKeyboard() {
        llKeyboard.setVisibility(View.GONE);
    }
    
    private void addQuickAmount(long amount) {
        currentAmount = String.valueOf(amount);
        updateAmountDisplay();
        updateSaveButtonState();
    }
    
    private void appendNumber(String number) {
        if (currentAmount.equals("0")) {
            currentAmount = number;
        } else {
            currentAmount += number;
        }
        updateAmountDisplay();
        updateSaveButtonState();
    }
    
    private void clearAmount() {
        currentAmount = "0";
        updateAmountDisplay();
        updateSaveButtonState();
    }
    
    private void performOperation(String op) {
        // Simple operation - just clear for now
        // Can be enhanced later
        hideKeyboard();
    }
    
    private void updateAmountDisplay() {
        try {
            long amount = Long.parseLong(currentAmount);
            String formatted = String.format(Locale.getDefault(), "%,d đ", amount);
            etAmount.setText(formatted);
        } catch (NumberFormatException e) {
            etAmount.setText("0 đ");
        }
    }
    
    private void updateSaveButtonState() {
        try {
            long amount = Long.parseLong(currentAmount);
            boolean isValid = amount > 0 && selectedCategory != null;
            btnSave.setEnabled(isValid);
        } catch (NumberFormatException e) {
            btnSave.setEnabled(false);
        }
    }
    
    private void loadCategories() {
        repository.getCategoriesByType(transactionType, new ExpenseRepository.DataCallback<List<Category>>() {
            @Override
            public void onDataLoaded(List<Category> loadedCategories) {
                runOnUiThread(() -> {
                    categories = loadedCategories;
                    if (categories.isEmpty()) {
                        createDefaultCategories();
                        repository.getCategoriesByType(transactionType, new ExpenseRepository.DataCallback<List<Category>>() {
                            @Override
                            public void onDataLoaded(List<Category> reloadedCategories) {
                                runOnUiThread(() -> {
                                    categories = reloadedCategories;
                                });
                            }
                        });
                    }
                });
            }
        });
    }
    
    private void createDefaultCategories() {
        if ("Chi".equals(transactionType)) {
            repository.insertCategory(new Category("Cần thiết", "🍽️", "Chi", 0xFF9C27B0));
            repository.insertCategory(new Category("Đào tạo", "📚", "Chi", 0xFF2196F3));
            repository.insertCategory(new Category("Hưởng thụ", "🎬", "Chi", 0xFFFF9800));
            repository.insertCategory(new Category("Ăn uống", "🍽️", "Chi", 0xFFE91E63));
        } else {
            repository.insertCategory(new Category("Lương", "💰", "Thu", 0xFF4CAF50));
            repository.insertCategory(new Category("Thưởng", "🎁", "Thu", 0xFFFF9800));
            repository.insertCategory(new Category("Khác", "💵", "Thu", 0xFF9E9E9E));
        }
    }
    
    private void showCategoryDialog() {
        if (categories == null || categories.isEmpty()) {
            Toast.makeText(this, "Đang tải danh mục...", Toast.LENGTH_SHORT).show();
            return;
        }
        
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_category_selection, null);
        RecyclerView recyclerView = dialogView.findViewById(R.id.recyclerViewCategories);
        ImageView ivClose = dialogView.findViewById(R.id.ivClose);
        Button btnCreateCategory = dialogView.findViewById(R.id.btnCreateCategory);
        
        CategoryDialogAdapter adapter = new CategoryDialogAdapter(categories, category -> {
            selectedCategory = category;
            tvCategory.setText(category.getName());
            tvCategory.setTextColor(getResources().getColor(R.color.text_primary, null));
            updateSaveButtonState();
        });
        
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();
        
        ivClose.setOnClickListener(v -> dialog.dismiss());
        
        btnCreateCategory.setOnClickListener(v -> {
            dialog.dismiss();
            showCreateCategoryDialog();
        });
        
        dialog.show();
    }
    
    private void showCreateCategoryDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_category, null);
        TextInputEditText etCategoryName = dialogView.findViewById(R.id.etCategoryName);
        TextInputEditText etPercentage = dialogView.findViewById(R.id.etPercentage);
        ImageView ivClose = dialogView.findViewById(R.id.ivClose);
        Button btnBack = dialogView.findViewById(R.id.btnBack);
        Button btnUpdate = dialogView.findViewById(R.id.btnUpdate);
        
        btnUpdate.setText("Tạo");
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .create();
        
        ivClose.setOnClickListener(v -> dialog.dismiss());
        btnBack.setOnClickListener(v -> dialog.dismiss());
        
        btnUpdate.setOnClickListener(v -> {
            String name = etCategoryName.getText().toString().trim();
            if (name.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập tên phân loại", Toast.LENGTH_SHORT).show();
                return;
            }
            
            Category newCategory = new Category(name, "📁", transactionType, 0xFF9E9E9E);
            repository.insertCategory(newCategory);
            Toast.makeText(this, "Đã tạo phân loại mới", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            loadCategories();
        });
        
        dialog.show();
    }
    
    private void showDatePicker() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(
            this,
            (view, year, month, dayOfMonth) -> {
                selectedDate.set(year, month, dayOfMonth);
                updateDateDisplay();
            },
            selectedDate.get(Calendar.YEAR),
            selectedDate.get(Calendar.MONTH),
            selectedDate.get(Calendar.DAY_OF_MONTH)
        );
        datePickerDialog.show();
    }
    
    private void loadExpenseData() {
        currentAmount = String.valueOf((long)currentExpense.getAmount());
        updateAmountDisplay();
        etNote.setText(currentExpense.getNote());
        
        selectedDate.setTime(currentExpense.getDate());
        updateDateDisplay();
        
        // Find and set category
        repository.getCategoriesByType(transactionType, new ExpenseRepository.DataCallback<List<Category>>() {
            @Override
            public void onDataLoaded(List<Category> loadedCategories) {
                runOnUiThread(() -> {
                    for (Category cat : loadedCategories) {
                        if (cat.getName().equals(currentExpense.getCategory())) {
                            selectedCategory = cat;
                            tvCategory.setText(cat.getName());
                            tvCategory.setTextColor(getResources().getColor(R.color.text_primary, null));
                            break;
                        }
                    }
                    updateSaveButtonState();
                });
            }
        });
    }
    
    private void saveExpense() {
        if (selectedCategory == null) {
            Toast.makeText(this, "Vui lòng chọn phân loại", Toast.LENGTH_SHORT).show();
            return;
        }
        
        try {
            long amount = Long.parseLong(currentAmount);
            if (amount <= 0) {
                Toast.makeText(this, "Số tiền phải lớn hơn 0", Toast.LENGTH_SHORT).show();
                return;
            }
            
            String note = etNote.getText().toString().trim();
            
            Expense expense = new Expense(
                selectedCategory.getName(),
                amount,
                note,
                selectedDate.getTime(),
                transactionType
            );
            
            if (currentExpense != null) {
                expense.setId(currentExpense.getId());
                repository.updateExpense(currentExpense, expense);
            } else {
                repository.insertExpense(expense);
            }
            
            showSuccessDialog();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Số tiền không hợp lệ", Toast.LENGTH_SHORT).show();
        }
    }
    
    private void showSuccessDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_success, null);
        Button btnBack = dialogView.findViewById(R.id.btnBack);
        Button btnCreateNew = dialogView.findViewById(R.id.btnCreateNew);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)
            .create();
        
        btnBack.setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });
        
        btnCreateNew.setOnClickListener(v -> {
            dialog.dismiss();
            // Reset form
            currentAmount = "0";
            updateAmountDisplay();
            etNote.setText("");
            selectedCategory = null;
            tvCategory.setText("Lựa chọn phân loại");
            tvCategory.setTextColor(getResources().getColor(R.color.text_hint, null));
            updateSaveButtonState();
        });
        
        dialog.show();
    }
    
    // Category Dialog Adapter
    private static class CategoryDialogAdapter extends RecyclerView.Adapter<CategoryDialogAdapter.ViewHolder> {
        private List<Category> categories;
        private OnCategoryClickListener listener;
        
        interface OnCategoryClickListener {
            void onCategoryClick(Category category);
        }
        
        CategoryDialogAdapter(List<Category> categories, OnCategoryClickListener listener) {
            this.categories = categories;
            this.listener = listener;
        }
        
        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_category_dialog, parent, false);
            return new ViewHolder(view);
        }
        
        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Category category = categories.get(position);
            holder.tvCategoryName.setText(category.getName());
            if (holder.tvCategoryIcon != null && category.getIcon() != null) {
                holder.tvCategoryIcon.setText(category.getIcon());
            }
            
            holder.itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onCategoryClick(category);
                }
            });
            
            holder.ivEdit.setOnClickListener(v -> {
                // Show edit dialog
                // Can be implemented later
            });
        }
        
        @Override
        public int getItemCount() {
            return categories.size();
        }
        
        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvCategoryName, tvCategoryIcon;
            ImageView ivEdit;
            
            ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
                tvCategoryIcon = itemView.findViewById(R.id.tvCategoryIcon);
                ivEdit = itemView.findViewById(R.id.ivEdit);
            }
        }
    }
}
