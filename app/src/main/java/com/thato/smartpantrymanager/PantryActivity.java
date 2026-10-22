package com.thato.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

// screen 2 - this is the user's pantry - the user can add, edit, delete and list ingredient
public class PantryActivity extends AppCompatActivity {
    private DatabaseHelper db;
    private SharedPreferences prefs;
    private int userId;

    private EditText etName, etQty;
    private Spinner spUnit;
    private Button btnSave, btnCancel;
    private ListView listPantry;
    private TextView tvEmptyPantry;

    private final List<Ingredient> items = new ArrayList<>();
    private IngredientAdapter adapter;
    private String[] units;

    // -1 means adding a new ingredient and any other number is the id being edited
    private int editingId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pantry);
        setTitle("My Pantry");

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("pantry_prefs", MODE_PRIVATE);
        userId = prefs.getInt("user_id", -1);

        // not logged in? the user is sent back to the login screen when they are not logged in
        if (userId == -1) {
            goToLogin();
            return;
        }

        etName = findViewById(R.id.etName);
        etQty = findViewById(R.id.etQty);
        spUnit = findViewById(R.id.spUnit);
        btnSave = findViewById(R.id.btnSave);
        btnCancel = findViewById(R.id.btnCancel);
        listPantry = findViewById(R.id.listPantry);
        tvEmptyPantry = findViewById(R.id.tvEmptyPantry);
        Button btnFind = findViewById(R.id.btnFindRecipes);

        // unit drop-down (g, kg, ml, l, pcs)
        units = getResources().getStringArray(R.array.units);
        ArrayAdapter<String> unitAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_dropdown_item, units);
        spUnit.setAdapter(unitAdapter);

        // list + adapter (the Delete button inside each row calls this listener)
        adapter = new IngredientAdapter(this, items, ingredient -> {
            db.deleteIngredient(ingredient.id);
            if (editingId == ingredient.id) {
                clearForm();
            }
            Toast.makeText(this, ingredient.name + " deleted", Toast.LENGTH_SHORT).show();
            loadIngredients();
        });
        listPantry.setAdapter(adapter);

        // tapping a row loads it into the form so it can be edited
        listPantry.setOnItemClickListener((parent, view, position, id) -> {
            Ingredient item = items.get(position);
            editingId = item.id;
            etName.setText(item.name);
            etQty.setText(item.quantityText());
            for (int i = 0; i < units.length; i++) {
                if (units[i].equals(item.unit)) {
                    spUnit.setSelection(i);
                }
            }
            btnSave.setText("Update Ingredient");
            btnCancel.setVisibility(View.VISIBLE);
        });

        btnSave.setOnClickListener(v -> saveIngredient());
        btnCancel.setOnClickListener(v -> clearForm());
        btnFind.setOnClickListener(v -> openSuggestions());

        loadIngredients();
    }

    // reads the form, checks it, then adds or updates
    private void saveIngredient() {
        String name = etName.getText().toString().trim();
        String qtyText = etQty.getText().toString().trim();

        if (name.isEmpty()) {
            etName.setError("Please enter an ingredient name");
            return;
        }

        double qty;
        try {
            qty = Double.parseDouble(qtyText);
        } catch (NumberFormatException e) {
            etQty.setError("Please enter a valid number");
            return;
        }
        if (qty <= 0) {
            etQty.setError("Quantity must be more than 0");
            return;
        }

        String unit = spUnit.getSelectedItem().toString();

        if (editingId == -1) {
            db.addIngredient(userId, name, qty, unit);
            Toast.makeText(this, name + " added", Toast.LENGTH_SHORT).show();
        } else {
            db.updateIngredient(editingId, name, qty, unit);
            Toast.makeText(this, name + " updated", Toast.LENGTH_SHORT).show();
        }

        clearForm();
        loadIngredients();
    }

    private void clearForm() {
        editingId = -1;
        etName.setText("");
        etQty.setText("");
        etName.setError(null);
        etQty.setError(null);
        spUnit.setSelection(0);
        btnSave.setText("Add Ingredient");
        btnCancel.setVisibility(View.GONE);
    }

    // reloads the list from the database
    private void loadIngredients() {
        items.clear();
        items.addAll(db.getIngredient(userId));
        adapter.notifyDataSetChanged();

        if (items.isEmpty()) {
            tvEmptyPantry.setVisibility(View.VISIBLE);
        } else {
            tvEmptyPantry.setVisibility(View.GONE);
        }
    }

    private void openSuggestions() {
        startActivity(new Intent(this, SuggestionsActivity.class));
    }

    private void goToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    // ---- toolbar menu ----
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.pantry_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_recipes) {
            openSuggestions();
            return true;
        } else if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        } else if (id == R.id.action_logout) {
            prefs.edit().remove("user_id").apply();
            goToLogin();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }


}
