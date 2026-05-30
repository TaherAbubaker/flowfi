package com.example.project;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class SignUpActivity extends AppCompatActivity {

    private EditText nameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button signUpButton;
    private TextView loginText;
    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        db = new DatabaseHelper(this);

        nameEditText            = findViewById(R.id.nameEditText);
        emailEditText           = findViewById(R.id.emailEditText);
        passwordEditText        = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        signUpButton            = findViewById(R.id.signUpButton);
        loginText               = findViewById(R.id.loginText);

        signUpButton.setOnClickListener(v -> attemptSignUp());

        loginText.setOnClickListener(v -> {
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        });
    }

    private void attemptSignUp() {
        String name     = nameEditText.getText().toString().trim();
        String email    = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();
        String confirm  = confirmPasswordEditText.getText().toString().trim();

        if (name.isEmpty()) {
            nameEditText.setError("Name required"); return;
        }
        if (email.isEmpty()) {
            emailEditText.setError("Email required"); return;
        }
        if (password.isEmpty()) {
            passwordEditText.setError("Password required"); return;
        }
        if (password.length() < 6) {
            passwordEditText.setError("Minimum 6 characters"); return;
        }
        if (!password.equals(confirm)) {
            confirmPasswordEditText.setError("Passwords don't match"); return;
        }
        if (db.emailExists(email)) {
            emailEditText.setError("Email already registered"); return;
        }

        boolean success = db.registerUser(name, email, password);

        if (success) {
            Toast.makeText(this, "Account created! Please log in.", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(SignUpActivity.this, LoginActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Sign up failed. Try again.", Toast.LENGTH_SHORT).show();
        }
    }
}