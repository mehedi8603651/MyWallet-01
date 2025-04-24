
package com.mehedi860.mywallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TextView tvBalance;
    private EditText etAmount;
    private Button btnAddMoney, btnSendMoney, btnViewHistory, btnFriends;
    private DatabaseHelper dbHelper;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        tvBalance = findViewById(R.id.tvBalance);
        etAmount = findViewById(R.id.etAmount);
        btnAddMoney = findViewById(R.id.btnAddMoney);
        btnSendMoney = findViewById(R.id.btnSendMoney);
        btnViewHistory = findViewById(R.id.btnViewHistory);
        btnFriends = findViewById(R.id.btnFriends);
        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");

        updateBalanceDisplay();

        btnAddMoney.setOnClickListener(v -> handleAddMoney());
        btnSendMoney.setOnClickListener(v -> handleSendMoney());
        btnViewHistory.setOnClickListener(v -> handleViewHistory());
        btnFriends.setOnClickListener(v -> handleFriends());
    }

    private void handleAddMoney() {
        String amountStr = etAmount.getText().toString();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                double currentBalance = dbHelper.getBalance(username);
                double newBalance = currentBalance + amount;
                if (dbHelper.updateBalance(username, newBalance)) {
                    dbHelper.addTransaction(username, amount, "ADD");
                    Toast.makeText(this, "Money Added!", Toast.LENGTH_SHORT).show();
                    updateBalanceDisplay();
                    etAmount.setText(""); // Clear the input field
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleSendMoney() {
        String amountStr = etAmount.getText().toString();
        if (!amountStr.isEmpty()) {
            try {
                double amount = Double.parseDouble(amountStr);
                if (amount <= 0) {
                    Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    return;
                }
                double currentBalance = dbHelper.getBalance(username);
                if (amount <= currentBalance) {
                    double newBalance = currentBalance - amount;
                    if (dbHelper.updateBalance(username, newBalance)) {
                        dbHelper.addTransaction(username, amount, "SEND");
                        Toast.makeText(this, "Money Sent!", Toast.LENGTH_SHORT).show();
                        updateBalanceDisplay();
                        etAmount.setText(""); // Clear the input field
                    }
                } else {
                    Toast.makeText(this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleViewHistory() {
        Intent intent = new Intent(this, TransactionHistoryActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void handleFriends() {
        Intent intent = new Intent(this, FriendsActivity.class);
        intent.putExtra("username", username);
        startActivity(intent);
    }

    private void updateBalanceDisplay() {
        double balance = dbHelper.getBalance(username);
        tvBalance.setText("Current Balance: $" + String.format("%.2f", balance));
    }
}