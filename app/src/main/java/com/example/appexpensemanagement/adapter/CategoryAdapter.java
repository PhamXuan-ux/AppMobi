package com.example.appexpensemanagement.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.example.appexpensemanagement.R;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder> {
    private List<CategoryItem> categories;
    
    public static class CategoryItem {
        public String name;
        public String icon;
        public double amount;
        public double percentage;
        public int color;
        
        public CategoryItem(String name, String icon, double amount, double percentage, int color) {
            this.name = name;
            this.icon = icon;
            this.amount = amount;
            this.percentage = percentage;
            this.color = color;
        }
    }
    
    public CategoryAdapter(List<CategoryItem> categories) {
        this.categories = categories;
    }
    
    @NonNull
    @Override
    public CategoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
            .inflate(R.layout.item_category, parent, false);
        return new CategoryViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull CategoryViewHolder holder, int position) {
        CategoryItem item = categories.get(position);
        holder.bind(item);
    }
    
    @Override
    public int getItemCount() {
        return categories.size();
    }
    
    class CategoryViewHolder extends RecyclerView.ViewHolder {
        private TextView tvCategoryName, tvCategoryAmount, tvCategoryPercent;
        private CardView cardView;
        
        CategoryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategoryName = itemView.findViewById(R.id.tvCategoryName);
            tvCategoryAmount = itemView.findViewById(R.id.tvCategoryAmount);
            tvCategoryPercent = itemView.findViewById(R.id.tvCategoryPercent);
            cardView = (androidx.cardview.widget.CardView) itemView;
        }
        
        void bind(CategoryItem item) {
            tvCategoryName.setText(item.name);
            tvCategoryAmount.setText(formatCurrency(item.amount));
            tvCategoryPercent.setText(String.format(Locale.getDefault(), "%.0f%%", item.percentage));
            
            // Set background color based on category
            if (item.color != 0) {
                cardView.setCardBackgroundColor(item.color);
                // Make text white for better contrast
                tvCategoryName.setTextColor(0xFFFFFFFF);
                tvCategoryAmount.setTextColor(0xFFFFFFFF);
                tvCategoryPercent.setTextColor(0xFFFFFFFF);
            }
        }
        
        private String formatCurrency(double amount) {
            return String.format(Locale.getDefault(), "%,.0f", amount);
        }
    }
}

