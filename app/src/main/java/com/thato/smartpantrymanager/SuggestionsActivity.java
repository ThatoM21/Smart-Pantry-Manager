package com.thato.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

// screen 3 - shows the recipes the user can make now as well as an almost there list
public class SuggestionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_suggestions);
        setTitle("Suggested Recipes");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        SharedPreferences prefs = getSharedPreferences("pantry_prefs", MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);
        boolean showAlmost = prefs.getBoolean("show_almost", true);

        DatabaseHelper db = new DatabaseHelper(this);
        List<Ingredient> pantry = db.getIngredient(userId);
        List<Recipe> allRecipes = db.getAllRecipes();

        List<Recipe> ready = new ArrayList<>();
        List<Recipe> almost = new ArrayList<>();

        // Check every recipe in the database against the user's pantry
        for (Recipe recipe : allRecipes) {
            List<String> missing = RecipeMatcher.findMissing(recipe, pantry);
            recipe.missingCount = missing.size();

            if (missing.isEmpty()) {
                // STRICT MATCH: nothing missing
                recipe.infoText = "You have everything you need!";
                ready.add(recipe);
            } else if (missing.size() <= 3 && missing.size() < recipe.ingredients.size()) {
                // CLOSE MATCH: missing 1-3 (but the user has at least one ingredient)
                recipe.infoText = "Missing " + missing.size() + ": " + TextUtils.join(", ", missing);
                almost.add(recipe);
            }
        }

        // Closest matches first
        Collections.sort(almost, new Comparator<Recipe>() {
            @Override
            public int compare(Recipe a, Recipe b) {
                return a.missingCount - b.missingCount;
            }
        });

        TextView tvEmpty = findViewById(R.id.tvEmpty);
        TextView tvAlmostHeader = findViewById(R.id.tvAlmostHeader);
        ListView listReady = findViewById(R.id.listReady);
        ListView listAlmost = findViewById(R.id.listAlmost);

        listReady.setAdapter(new RecipeAdapter(this, ready));
        listAlmost.setAdapter(new RecipeAdapter(this, almost));

        // Message when nothing matches the pantry (strictly)
        if (ready.isEmpty()) {
            listReady.setVisibility(View.GONE);
            tvEmpty.setVisibility(View.VISIBLE);
        }

        // Hide the "Almost There" section if switched off in Settings or empty
        if (!showAlmost || almost.isEmpty()) {
            tvAlmostHeader.setVisibility(View.GONE);
            listAlmost.setVisibility(View.GONE);
        }

        AdapterView.OnItemClickListener openDetail = (parent, view, position, id) -> {
            Recipe recipe = (Recipe) parent.getItemAtPosition(position);
            Intent intent = new Intent(SuggestionsActivity.this, RecipeDetailActivity.class);
            intent.putExtra("recipe_id", recipe.id);
            startActivity(intent);
        };
        listReady.setOnItemClickListener(openDetail);
        listAlmost.setOnItemClickListener(openDetail);
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
