package com.example.appexpensemanagement.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import com.example.appexpensemanagement.model.Budget;
import java.util.List;
import java.util.Locale;

public class BudgetAdapter extends RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder> {
    private List<Budget> budgets;
    private OnBudgetClickListener listener;
    
    public interface OnBudgetClickListener {
        void onEditClick(Budget budget);
    }
    
    public BudgetAdapter(List<Budget> budgets, OnBudgetClickListener listener) {
        this.budgets = budgets;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public BudgetViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_budget, parent, false);
        return new BudgetViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull BudgetViewHolder holder, int position) {
        Budget budget = budgets.get(position);
        holder.bind(budget);
    }
    
    @Override
    public int getItemCount() {
        return budgets.size();
    }
    
    class BudgetViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategory, tvLimit, tvSpent, tvRemaining, tvPercentage;
        private ProgressBar progressBar;
        private CardView cardView;
        
        BudgetViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            tvLimit = itemView.findViewById(R.id.tvLimit);
            tvSpent = itemView.findViewById(R.id.tvSpent);
            tvRemaining = itemView.findViewById(R.id.tvRemaining);
            tvPercentage = itemView.findViewById(R.id.tvPercentage);
            progressBar = itemView.findViewById(R.id.progressBar);
            cardView = itemView.findViewById(R.id.cardView);
            
            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) {
                    listener.onEditClick(budgets.get(position));
                }
            });
        }
        
        void bind(Budget budget) {
            tvCategory.setText(budget.getCategoryName());
            tvLimit.setText(formatCurrency(budget.getLimit()));
            tvSpent.setText(formatCurrency(budget.getSpent()));
            tvRemaining.setText(formatCurrency(budget.getRemaining()));
            
            double percentage = budget.getPercentage();
            tvPercentage.setText(String.format(Locale.getDefault(), "%.0f%%", percentage));
            
            progressBar.setMax(100);
            progressBar.setProgress((int) percentage);
            
            // Change color if exceeded
            if (budget.isExceeded()) {
                cardView.setCardBackgroundColor(0xFFFFEBEE); // Light red
                tvSpent.setTextColor(0xFFD32F2F); // Red
                progressBar.getProgressDrawable().setColorFilter(
                    Color.RED, android.graphics.PorterDuff.Mode.SRC_IN);
            } else {
                cardView.setCardBackgroundColor(Color.WHITE);
                tvSpent.setTextColor(0xFF212121);
                progressBar.getProgressDrawable().setColorFilter(
                    Color.parseColor("#4CAF50"), android.graphics.PorterDuff.Mode.SRC_IN);
            }
        }
        
        private String formatCurrency(double amount) {
            return String.format(Locale.getDefault(), "%,.0f đ", amount);
        }
    }
}

