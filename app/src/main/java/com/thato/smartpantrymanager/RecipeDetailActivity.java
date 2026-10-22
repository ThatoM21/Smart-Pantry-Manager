package com.thato.smartpantrymanager;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import java.util.List;

public class RecipeDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recipe_detail);
        setTitle("Recipe");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // The recipe id was sent from the Suggestions screen using an Intent
        int recipeId = getIntent().getIntExtra("recipe_id", -1);

        SharedPreferences prefs = getSharedPreferences("pantry_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        DatabaseHelper db = new DatabaseHelper(this);
        Recipe recipe = db.getRecipe(recipeId);
        if (recipe == null) {
            finish();
            return;
        }

        List<String> missing = RecipeMatcher.findMissing(recipe, db.getIngredient(userId));

        StringBuilder sb = new StringBuilder();
        for (Ingredient ing : recipe.ingredients) {
            sb.append("- ").append(ing.name).append(": ").append(ing.amountText());
            if (missing.contains(ing.name)) {
                sb.append("   (missing)");
            }
            sb.append("\n");
        }

        ((TextView) findViewById(R.id.tvDetailName)).setText(recipe.name);
        ((TextView) findViewById(R.id.tvDetailIngredients)).setText(sb.toString().trim());
        ((TextView) findViewById(R.id.tvDetailSteps)).setText(recipe.steps);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }


}
