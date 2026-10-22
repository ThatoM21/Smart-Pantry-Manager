package com.thato.smartpantrymanager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;

import java.util.List;
public class RecipeAdapter extends ArrayAdapter<Recipe> {

    public RecipeAdapter(Context context, List<Recipe> recipes) {
        super(context, 0, recipes);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext())
                    .inflate(R.layout.item_recipe, parent, false);
        }

        Recipe recipe = getItem(position);

        TextView tvName = convertView.findViewById(R.id.tvRecipeName);
        TextView tvInfo = convertView.findViewById(R.id.tvRecipeInfo);

        tvName.setText(recipe.name);
        tvInfo.setText(recipe.infoText);

        return convertView;
    }
}
