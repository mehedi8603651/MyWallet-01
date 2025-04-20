/**
 * package com.mehedi860.mywallet;
 *
 * import android.os.Bundle;
 *
 * import androidx.activity.EdgeToEdge;
 * import androidx.appcompat.app.AppCompatActivity;
 * import androidx.core.graphics.Insets;
 * import androidx.core.view.ViewCompat;
 * import androidx.core.view.WindowInsetsCompat;
 *
 * public class MainActivity extends AppCompatActivity {
 *
 *     @Override
 *     protected void onCreate(Bundle savedInstanceState) {
 *         super.onCreate(savedInstanceState);
 *         EdgeToEdge.enable(this);
 *         setContentView(R.layout.activity_main);
 *         ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
 *             Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
 *             v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
 *             return insets;
 *         });
 *     }
 * }
 */
package com.mehedi860.mywallet;

import android.content.Intent;
import android.os.Bundle;
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

        btnAddMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = etAmount.getText().toString();
                if (!amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);
                    double currentBalance = dbHelper.getBalance(username);
                    double newBalance = currentBalance + amount;
                    if (dbHelper.updateBalance(username, newBalance)) {
                        dbHelper.addTransaction(username, amount, "ADD");
                        Toast.makeText(MainActivity.this, "Money Added!", Toast.LENGTH_SHORT).show();
                        updateBalanceDisplay();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter Amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSendMoney.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String amountStr = etAmount.getText().toString();
                if (!amountStr.isEmpty()) {
                    double amount = Double.parseDouble(amountStr);
                    double currentBalance = dbHelper.getBalance(username);
                    if (amount <= currentBalance) {
                        double newBalance = currentBalance - amount;
                        if (dbHelper.updateBalance(username, newBalance)) {
                            dbHelper.addTransaction(username, amount, "SEND");
                            Toast.makeText(MainActivity.this, "Money Sent!", Toast.LENGTH_SHORT).show();
                            updateBalanceDisplay();
                        }
                    } else {
                        Toast.makeText(MainActivity.this, "Insufficient Balance", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Enter Amount", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnViewHistory.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TransactionHistoryActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });

        btnFriends.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, FriendsActivity.class);
                intent.putExtra("username", username);
                startActivity(intent);
            }
        });
    }

    private void updateBalanceDisplay() {
        double balance = dbHelper.getBalance(username);
        tvBalance.setText("Current Balance: $" + String.format("%.2f", balance));
    }
}