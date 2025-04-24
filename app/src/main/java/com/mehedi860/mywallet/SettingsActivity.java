package com.mehedi860.mywallet;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class SettingsActivity extends AppCompatActivity {
    private static final String PREF_DARK_MODE = "dark_mode";
    private static final String PREF_NOTIFICATIONS = "notifications";
    private static final String PREF_BIOMETRIC = "biometric_auth";
    
    private DatabaseHelper dbHelper;
    private String username;
    private EditText etFullName, etEmail;
    private Switch switchDarkMode, switchNotifications, switchBiometric;
    private Button btnUpdateProfile, btnChangePassword, btnBack;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        username = getIntent().getStringExtra("username");
        dbHelper = new DatabaseHelper(this);

        // Initialize views
        etFullName = findViewById(R.id.etFullName);
        etEmail = findViewById(R.id.etEmail);
        switchDarkMode = findViewById(R.id.switchDarkMode);
        switchNotifications = findViewById(R.id.switchNotifications);
        switchBiometric = findViewById(R.id.switchBiometric);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        btnChangePassword = findViewById(R.id.btnChangePassword);
        btnBack = findViewById(R.id.btnBack);

        // Load current settings
        loadSettings();

        // Set up listeners
        switchDarkMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.setSetting(username, PREF_DARK_MODE, String.valueOf(isChecked));
            AppCompatDelegate.setDefaultNightMode(
                isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        });

        switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.setSetting(username, PREF_NOTIFICATIONS, String.valueOf(isChecked));
        });

        switchBiometric.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dbHelper.setSetting(username, PREF_BIOMETRIC, String.valueOf(isChecked));
        });

        btnUpdateProfile.setOnClickListener(v -> {
            String fullName = etFullName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            
            if (fullName.isEmpty() && email.isEmpty()) {
                Toast.makeText(this, "Please fill at least one field", Toast.LENGTH_SHORT).show();
                return;
            }

            if (dbHelper.updateUserProfile(username, fullName, email)) {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show();
                if (!email.isEmpty()) {
                    Toast.makeText(this, "Verification email sent to new email address", Toast.LENGTH_LONG).show();
                }
            } else {
                Toast.makeText(this, "Failed to update profile", Toast.LENGTH_SHORT).show();
            }
        });

        btnChangePassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ChangePasswordActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
        });

        btnBack.setOnClickListener(v -> finish());
    }

    private void loadSettings() {
        // Load dark mode setting
        String darkMode = dbHelper.getSetting(username, PREF_DARK_MODE);
        if (darkMode != null) {
            boolean isDarkMode = Boolean.parseBoolean(darkMode);
            switchDarkMode.setChecked(isDarkMode);
            AppCompatDelegate.setDefaultNightMode(
                isDarkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
        }

        // Load notifications setting
        String notifications = dbHelper.getSetting(username, PREF_NOTIFICATIONS);
        if (notifications != null) {
            switchNotifications.setChecked(Boolean.parseBoolean(notifications));
        }

        // Load biometric auth setting
        String biometric = dbHelper.getSetting(username, PREF_BIOMETRIC);
        if (biometric != null) {
            switchBiometric.setChecked(Boolean.parseBoolean(biometric));
        }
    }
}
