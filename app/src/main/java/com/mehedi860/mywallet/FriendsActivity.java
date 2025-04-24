package com.mehedi860.mywallet;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import java.util.ArrayList;
import java.util.List;

public class FriendsActivity extends AppCompatActivity {
    private DatabaseHelper dbHelper;
    private EditText friendUsernameInput;
    private ListView friendsList;
    private ListView pendingRequestsList;
    private String currentUsername;
    private List<String> friends;
    private List<String> pendingRequests;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friends);

        // Enable back button in action bar
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Friends");
        }

        dbHelper = new DatabaseHelper(this);
        currentUsername = getIntent().getStringExtra("username");

        friendUsernameInput = findViewById(R.id.friendUsernameInput);
        friendsList = findViewById(R.id.friendsList);
        pendingRequestsList = findViewById(R.id.pendingRequestsList);

        Button addFriendButton = findViewById(R.id.addFriendButton);
        addFriendButton.setOnClickListener(v -> sendFriendRequest());

        updateLists();
    }

    private void sendFriendRequest() {
        String friendUsername = friendUsernameInput.getText().toString().trim();
        
        if (friendUsername.isEmpty()) {
            Toast.makeText(this, "Please enter a username", Toast.LENGTH_SHORT).show();
            return;
        }

        if (dbHelper.sendFriendRequest(currentUsername, friendUsername)) {
            Toast.makeText(this, "Friend request sent", Toast.LENGTH_SHORT).show();
            friendUsernameInput.setText("");
        } else {
            Toast.makeText(this, "Failed to send friend request", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLists() {
        // Update friends list
        friends = dbHelper.getFriends(currentUsername);
        if (friends == null) friends = new ArrayList<>();
        ArrayAdapter<String> friendsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, friends);
        friendsList.setAdapter(friendsAdapter);

        // Update pending requests list
        pendingRequests = dbHelper.getPendingFriendRequests(currentUsername);
        if (pendingRequests == null) pendingRequests = new ArrayList<>();
        ArrayAdapter<String> requestsAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_2, android.R.id.text1, pendingRequests) {
            @Override
            public View getView(int position, View convertView, android.view.ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                String username = pendingRequests.get(position);
                
                android.widget.TextView text1 = view.findViewById(android.R.id.text1);
                android.widget.TextView text2 = view.findViewById(android.R.id.text2);
                
                text1.setText(username);
                text2.setText("Tap to accept");
                
                return view;
            }
        };
        
        pendingRequestsList.setAdapter(requestsAdapter);
        pendingRequestsList.setOnItemClickListener((parent, view, position, id) -> {
            String friendUsername = pendingRequests.get(position);
            if (dbHelper.acceptFriendRequest(currentUsername, friendUsername)) {
                Toast.makeText(this, "Friend request accepted", Toast.LENGTH_SHORT).show();
                updateLists();
            } else {
                Toast.makeText(this, "Failed to accept friend request", Toast.LENGTH_SHORT).show();
            }
        });

        // Enable sending money to friends by clicking on their name
        friendsList.setOnItemClickListener((parent, view, position, id) -> {
            String friendUsername = friends.get(position);
            showSendMoneyDialog(friendUsername);
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    private void showSendMoneyDialog(String friendUsername) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        View view = getLayoutInflater().inflate(R.layout.dialog_send_money_friend, null);
        EditText amountInput = view.findViewById(R.id.amountInput);

        builder.setTitle("Send Money to " + friendUsername)
                .setView(view)
                .setPositiveButton("Send", (dialog, which) -> {
                    String amountStr = amountInput.getText().toString();
                    if (amountStr.isEmpty()) {
                        Toast.makeText(this, "Please enter an amount", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    try {
                        double amount = Double.parseDouble(amountStr);
                        if (amount <= 0) {
                            Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        
                        if (dbHelper.sendMoneyToFriend(currentUsername, friendUsername, amount)) {
                            Toast.makeText(this, "Money sent successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(this, "Failed to send money. Check your balance.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        Toast.makeText(this, "Please enter a valid amount", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
