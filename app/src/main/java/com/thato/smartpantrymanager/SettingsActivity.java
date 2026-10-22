package com.thato.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

// screen 5 - settings. users can toggle the almost there list or log out button
public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        setTitle("Settings");
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        final SharedPreferences prefs = getSharedPreferences("pantry_prefs", MODE_PRIVATE);

        SwitchCompat swAlmost = findViewById(R.id.swAlmost);
        swAlmost.setChecked(prefs.getBoolean("show_almost", true));
        swAlmost.setOnCheckedChangeListener((button, isChecked) ->
                prefs.edit().putBoolean("show_almost", isChecked).apply());

        Button btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            prefs.edit().remove("user_id").apply();
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}
