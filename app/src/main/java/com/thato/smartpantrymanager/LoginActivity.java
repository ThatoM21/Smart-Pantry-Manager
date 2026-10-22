package com.thato.smartpantrymanager;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

// screen 1: the user can login in and create an account
public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private EditText etUsername, etPassword;
    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(this);
        prefs = getSharedPreferences("pantry_prefs", MODE_PRIVATE);

        // if the user is logging in they can go to the pantry page
        if (prefs.getInt("user_id", -1) != -1){
            openPantry();
            return;
        }

        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        Button btnRegister = findViewById(R.id.btnRegister);

        btnLogin.setOnClickListener(v -> doLogin());
        btnRegister.setOnClickListener(v -> doRegister());
    }

    private boolean inputIsValid(String username, String password){

        if(username.isEmpty()){
            etUsername.setError("Please enter a username");
            return false;
        }
        if(password.length() < 4){
            etPassword.setError("Password must be at least 4 characters long");
            return false;
        }
        return true;
    }

    private void doLogin(){
        String username = etUsername.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString();

        if (!inputIsValid(username, password)){
            return;
        }

        int userId = db.loginUser(username, password);
        if (userId == -1) {
            Toast.makeText(this, "Wrong username or password", Toast.LENGTH_SHORT).show();
        }
        else {
            prefs.edit().putInt("user_id", userId).apply();
            openPantry();
        }
    }

    private void doRegister(){
        String username = etUsername.getText().toString().trim().toLowerCase();
        String password = etPassword.getText().toString();

        if (!inputIsValid(username, password)){
            return;
        }

        if (db.registerUser(username, password)){
            Toast.makeText(this, "Account created!", Toast.LENGTH_SHORT).show();
            doLogin();
        }
        else {
            Toast.makeText(this, "That username is already taken", Toast.LENGTH_SHORT).show();
        }
    }

    private void openPantry(){
        startActivity(new Intent(this, PantryActivity.class));
        finish();
    }
}