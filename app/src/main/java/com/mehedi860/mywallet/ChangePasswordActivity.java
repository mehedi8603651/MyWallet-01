package com.mehedi860.mywallet;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChangePasswordActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private String username;
    private EditText etCurrentPassword, etNewPassword, etConfirmPassword;
    private Button btnChangePassword, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_password);

        username = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        etCurrentPassword = findViewById(R.id.etCurrentPassword);
        etNewPassword = findViewById(R.id.etNewPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);

        btnChangePassword.setOnClickListener(v -> {
            String currentPassword = etCurrentPassword.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            String confirmPassword = etConfirmPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(confirmPassword)) {
                Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isPasswordStrong(newPassword)) {
                Toast.makeText(this, "Password must be at least 8 characters long and contain letters, numbers, and special characters", Toast.LENGTH_LONG).show();
                return;
            }

            if (dbHelper.checkUser(username, currentPassword, false)) { // false because we're using username, not email
                // Update password in database
                if (dbHelper.updatePassword(username, newPassword)) {
                    Toast.makeText(this, "Password changed successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Failed to change password", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Current password is incorrect", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private boolean isPasswordStrong(String password) {
        // Password must be at least 8 characters long and contain letters, numbers, and special characters
        return password.length() >= 8 &&
                password.matches(".*[A-Za-z].*") &&
                password.matches(".*[0-9].*") &&
                password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
    }
}