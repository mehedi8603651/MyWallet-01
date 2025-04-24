/**
 * package com.mehedi860.mywallet;
 *
 * public class LoginActivity {
 * }
 */
package com.mehedi860.mywallet;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import android.util.Patterns;

public class LoginActivity extends AppCompatActivity {
    private static final String PREF_REMEMBER_ME = "remember_me";
    private static final String PREF_USERNAME = "saved_username";
    private static final String PREF_PASSWORD = "saved_password";
    private EditText etUsername, etPassword;
    private Button btnLogin, btnGoToRegister, btnForgotPassword;
    private CheckBox cbRememberMe;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnForgotPassword = findViewById(R.id.btnForgotPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        dbHelper = new DatabaseHelper(this);

        // Load saved credentials if they exist
        SharedPreferences prefs = getSharedPreferences("MyWalletPrefs", MODE_PRIVATE);
        if (prefs.getBoolean(PREF_REMEMBER_ME, false)) {
            etUsername.setText(prefs.getString(PREF_USERNAME, ""));
            etPassword.setText(prefs.getString(PREF_PASSWORD, ""));
            cbRememberMe.setChecked(true);
        }

        btnForgotPassword.setOnClickListener(v -> {
            String username = etUsername.getText().toString().trim();
            if (!username.isEmpty()) {
                // Request password reset
                if (dbHelper.requestPasswordReset(username)) {
                    Toast.makeText(LoginActivity.this, "Password reset instructions sent to your email", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "User not found", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(LoginActivity.this, "Please enter your username or email", Toast.LENGTH_SHORT).show();
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String usernameOrEmail = etUsername.getText().toString().trim();
                String password = etPassword.getText().toString().trim();
                if (usernameOrEmail.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Check if input is email or username
                    boolean isEmail = Patterns.EMAIL_ADDRESS.matcher(usernameOrEmail).matches();
                    if (dbHelper.checkUser(usernameOrEmail, password, isEmail)) {
                        // Save credentials if remember me is checked
                        if (cbRememberMe.isChecked()) {
                            SharedPreferences.Editor editor = getSharedPreferences("MyWalletPrefs", MODE_PRIVATE).edit();
                            editor.putBoolean(PREF_REMEMBER_ME, true);
                            editor.putString(PREF_USERNAME, usernameOrEmail);
                            editor.putString(PREF_PASSWORD, password);
                            editor.apply();
                        } else {
                            // Clear saved credentials
                            SharedPreferences.Editor editor = getSharedPreferences("MyWalletPrefs", MODE_PRIVATE).edit();
                            editor.clear();
                            editor.apply();
                        }

                        Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        intent.putExtra("username", usernameOrEmail);
                        startActivity(intent);
                        finish();
                    } else {
                        Toast.makeText(LoginActivity.this, "Invalid Username or Password", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });

        btnGoToRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }
}