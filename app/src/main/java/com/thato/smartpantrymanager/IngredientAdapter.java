package com.thato.smartpantrymanager;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;

// This will show all of the pantry ingredients as a row
public class IngredientAdapter extends ArrayAdapter<Ingredient> {

    public interface OnDeleteListener {
        void onDelete(Ingredient ingredient);
    }

    private final OnDeleteListener deleteListener;

    public IngredientAdapter(Context context, List<Ingredient> items, OnDeleteListener listener) {
        super(context, 0, items);
        this.deleteListener = listener;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_ingredient, parent, false);
        }
        final Ingredient ingredient = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvIngName);
        TextView tvAmount = convertView.findViewById(R.id.tvIngAmount);
        Button btnDelete = convertView.findViewById(R.id.btnDelete);

        if (ingredient != null) {
            tvName.setText(ingredient.name);
            tvAmount.setText(ingredient.amountText());
            btnDelete.setOnClickListener(v -> deleteListener.onDelete(ingredient));
        }

        return convertView;
    }
}