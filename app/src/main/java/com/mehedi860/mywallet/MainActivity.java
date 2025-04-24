
package com.mehedi860.mywallet;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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

        // If no username, redirect to login
        if (username == null) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
            return;
        }

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
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_send_money, null);
        builder.setView(dialogView);

        Spinner friendSpinner = dialogView.findViewById(R.id.friendSpinner);
        EditText amountInput = dialogView.findViewById(R.id.amountInput);

        // Get friends list
        List<String> friends = dbHelper.getFriends(username);
        if (friends.isEmpty()) {
            Toast.makeText(this, "Add friends first to send money", Toast.LENGTH_SHORT).show();
            return;
        }

        // Setup spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, friends);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        friendSpinner.setAdapter(adapter);

        builder.setTitle("Send Money")
                .setPositiveButton("Send", (dialog, which) -> {
                    String selectedFriend = (String) friendSpinner.getSelectedItem();
                    String amountStr = amountInput.getText().toString();

                    if (amountStr.isEmpty()) {
                        Toast.makeText(this, "Enter Amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        double currentBalance = dbHelper.getBalance(username);
                        if (amount <= currentBalance) {
                            if (dbHelper.sendMoneyToFriend(username, selectedFriend, amount)) {
                                Toast.makeText(this, "Money sent to " + selectedFriend, Toast.LENGTH_SHORT).show();
                                updateBalanceDisplay();
                                etAmount.setText(""); // Clear the main amount field
                            } else {
                                Toast.makeText(this, "Failed to send money", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null);

        builder.create().show();
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra("username", username);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}