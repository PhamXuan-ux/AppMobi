package com.example.appexpensemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.model.Expense;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ExpenseAdapter extends RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder> {
    private List<Expense> expenses;
    private OnItemClickListener listener;
    
    public interface OnItemClickListener {
        void onItemClick(Expense expense);
    }
    
    public ExpenseAdapter(List<Expense> expenses, OnItemClickListener listener) {
        this.expenses = expenses;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ExpenseViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_expense, parent, false);
        return new ExpenseViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ExpenseViewHolder holder, int position) {
        Expense expense = expenses.get(position);
        holder.bind(expense);
    }
    
    @Override
    public int getItemCount() {
        return expenses.size();
    }
    
    class ExpenseViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory, tvAmount, tvNote;
        private View viewCategoryDot;
        
        ExpenseViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvAmount = itemView.findViewById(R.id.tvAmount);
            tvNote = itemView.findViewById(R.id.tvNote);
            viewCategoryDot = itemView.findViewById(R.id.viewCategoryDot);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onItemClick(expenses.get(position));
                }
            });
        }
        
        void bind(Expense expense) {
            tvCategory.setText(expense.getCategory());
            tvAmount.setText(formatCurrency(expense.getAmount()));
            tvNote.setText(expense.getNote().isEmpty() ? "" : expense.getNote());
            
            // Set color based on type
            int color = "Thu".equals(expense.getType()) ? 
                0xFF4CAF50 : 0xFFE91E63; // Green for income, Pink for expense
            tvAmount.setTextColor(color);
            
            // Set category dot color based on category
            if (viewCategoryDot != null) {
                int dotColor = getCategoryColor(expense.getCategory());
                android.graphics.drawable.GradientDrawable drawable = 
                    (android.graphics.drawable.GradientDrawable) viewCategoryDot.getBackground();
                if (drawable != null) {
                    drawable.setColor(dotColor);
                } else {
                    viewCategoryDot.setBackgroundTintList(android.content.res.ColorStateList.valueOf(dotColor));
                }
            }
            
            // Hide note if empty (matching design - no note shown in list)
            if (tvNote != null) {
                tvNote.setVisibility(View.GONE);
            }
        }
        
        private int getCategoryColor(String category) {
            // Map categories to colors matching the design
            Map<String, Integer> colorMap = new HashMap<>();
            colorMap.put("Cần thiết", 0xFF9C27B0); // Purple
            colorMap.put("Đào tạo", 0xFF2196F3); // Blue
            colorMap.put("Hưởng thụ", 0xFFFF9800); // Orange
            colorMap.put("Ăn uống", 0xFFE91E63); // Pink
            colorMap.put("Đi lại", 0xFF2196F3); // Blue
            colorMap.put("Mua sắm", 0xFFFF9800); // Orange
            return colorMap.getOrDefault(category, 0xFF2196F3); // Default blue
        }
        
        private String formatCurrency(double amount) {
            // Format without "đ" to match design (shows "250.000" not "250.000 đ")
            return String.format(Locale.getDefault(), "%,.0f", amount);
        }
        
    }
}

