/**
 * package com.mehedi860.mywallet;
 *
 * public class TransactionHistoryActivity {
 * }
 */
package com.mehedi860.mywallet;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionHistoryActivity extends AppCompatActivity {
    private RecyclerView rvTransactions;
    private DatabaseHelper dbHelper;
    private TransactionAdapter adapter;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_history);

        rvTransactions = findViewById(R.id.rvTransactions);
        dbHelper = new DatabaseHelper(this);
        username = getIntent().getStringExtra("username");

        rvTransactions.setLayoutManager(new LinearLayoutManager(this));
        List<DatabaseHelper.Transaction> transactions = dbHelper.getTransactions(username);
        adapter = new TransactionAdapter(transactions);
        rvTransactions.setAdapter(adapter);
    }
}