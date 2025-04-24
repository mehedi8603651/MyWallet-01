/**
 * package com.mehedi860.mywallet;
 *
 * public class DatabaseHelper {
 * }
 */
package com.mehedi860.mywallet;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "WalletDB";
    private static final int DATABASE_VERSION = 5;
    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_EMAIL = "email";
    private static final String COLUMN_PASSWORD = "password";
    private static final String COLUMN_BALANCE = "balance";
    private static final String COLUMN_FULL_NAME = "full_name";
    private static final String COLUMN_PROFILE_PIC = "profile_pic";
    private static final String COLUMN_EMAIL_VERIFIED = "email_verified";
    private static final String COLUMN_VERIFICATION_TOKEN = "verification_token";
    private static final String COLUMN_RESET_TOKEN = "reset_token";
    private static final String COLUMN_LAST_LOGIN = "last_login";
    private static final String TABLE_SETTINGS = "settings";
    private static final String COLUMN_SETTING_KEY = "setting_key";
    private static final String COLUMN_SETTING_VALUE = "setting_value";
    private static final String TABLE_TRANSACTIONS = "transactions";
    private static final String COLUMN_AMOUNT = "amount";
    private static final String COLUMN_TYPE = "type";
    private static final String COLUMN_DATE = "date";
    private static final String TABLE_FRIENDS = "friends";
    private static final String COLUMN_USER1 = "user1";
    private static final String COLUMN_USER2 = "user2";
    private static final String COLUMN_STATUS = "status";
    private static final String STATUS_PENDING = "pending";
    private static final String STATUS_ACCEPTED = "accepted";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @SuppressLint("SQLiteString")
    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT UNIQUE, " +
                COLUMN_EMAIL + " TEXT UNIQUE, " +
                COLUMN_PASSWORD + " TEXT, " +
                COLUMN_BALANCE + " REAL, " +
                COLUMN_FULL_NAME + " TEXT, " +
                COLUMN_PROFILE_PIC + " TEXT, " +
                COLUMN_EMAIL_VERIFIED + " INTEGER DEFAULT 0, " +
                COLUMN_VERIFICATION_TOKEN + " TEXT, " +
                COLUMN_RESET_TOKEN + " TEXT, " +
                COLUMN_LAST_LOGIN + " TEXT)";
        db.execSQL(createUsersTable);

        String createSettingsTable = "CREATE TABLE " + TABLE_SETTINGS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_SETTING_KEY + " TEXT, " +
                COLUMN_SETTING_VALUE + " TEXT, " +
                "UNIQUE(" + COLUMN_USERNAME + ", " + COLUMN_SETTING_KEY + "))";
        db.execSQL(createSettingsTable);

        String createTransactionsTable = "CREATE TABLE " + TABLE_TRANSACTIONS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_AMOUNT + " REAL, " +
                COLUMN_TYPE + " TEXT, " +
                COLUMN_DATE + " TEXT)";
        db.execSQL(createTransactionsTable);

        String createFriendsTable = "CREATE TABLE " + TABLE_FRIENDS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USER1 + " TEXT, " +
                COLUMN_USER2 + " TEXT, " +
                COLUMN_STATUS + " TEXT)";
        db.execSQL(createFriendsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop all existing tables
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRANSACTIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_FRIENDS);
        
        // Recreate all tables
        onCreate(db);
    }

    public boolean addUser(String username, String email, String password, String fullName, double balance) {
        if (isUsernameExists(username) || isEmailExists(email)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_EMAIL, email);
        values.put(COLUMN_PASSWORD, hashPassword(password));
        values.put(COLUMN_FULL_NAME, fullName);
        values.put(COLUMN_BALANCE, balance);
        values.put(COLUMN_VERIFICATION_TOKEN, generateVerificationToken());
        values.put(COLUMN_EMAIL_VERIFIED, 0);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public double getBalance(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COLUMN_BALANCE},
                COLUMN_USERNAME + "=?", new String[]{username},
                null, null, null);
        if (cursor.moveToFirst()) {
            double balance = cursor.getDouble(0);
            cursor.close();
            return balance;
        }
        cursor.close();
        return 0.0;
    }

    public boolean updateBalance(String username, double newBalance) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_BALANCE, newBalance);
        int result = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        return result > 0;
    }

    private String hashPassword(String password) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(password.getBytes("UTF-8"));
            StringBuilder hexString = new StringBuilder();
            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private String generateVerificationToken() {
        return UUID.randomUUID().toString();
    }

    public boolean checkUser(String usernameOrEmail, String password, boolean isEmail) {
        SQLiteDatabase db = this.getReadableDatabase();
        String column = isEmail ? COLUMN_EMAIL : COLUMN_USERNAME;
        
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_USERNAME, COLUMN_ID},
                column + "=? AND " + COLUMN_PASSWORD + "=?",
                new String[]{usernameOrEmail, hashPassword(password)},
                null, null, null);
        
        boolean exists = cursor.moveToFirst();
        String username = exists ? cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USERNAME)) : null;
        cursor.close();
        
        if (exists && username != null) {
            updateLastLogin(username);
        }
        return exists;
    }

    private void updateLastLogin(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
    }

    public boolean isEmailExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_EMAIL + "=?",
                new String[]{email.trim()},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean verifyEmail(String username, String token) {
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=? AND " + COLUMN_VERIFICATION_TOKEN + "=?",
                new String[]{username, token},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();

        if (exists) {
            ContentValues values = new ContentValues();
            values.put(COLUMN_EMAIL_VERIFIED, 1);
            values.putNull(COLUMN_VERIFICATION_TOKEN);
            db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
            return true;
        }
        return false;
    }

    public boolean requestPasswordReset(String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        String resetToken = generateVerificationToken();
        ContentValues values = new ContentValues();
        values.put(COLUMN_RESET_TOKEN, resetToken);
        int updated = db.update(TABLE_USERS, values, COLUMN_EMAIL + "=?", new String[]{email});
        return updated > 0;
    }

    public boolean updatePassword(String username, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, hashPassword(newPassword));
        int updated = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        return updated > 0;
    }

    public boolean resetPassword(String resetToken, String newPassword) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PASSWORD, hashPassword(newPassword));
        values.putNull(COLUMN_RESET_TOKEN);
        int updated = db.update(TABLE_USERS, values, COLUMN_RESET_TOKEN + "=?", new String[]{resetToken});
        return updated > 0;
    }

    public boolean isUsernameExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS,
                new String[]{COLUMN_ID},
                COLUMN_USERNAME + "=?",
                new String[]{username.trim()},
                null, null, null);
        boolean exists = cursor.moveToFirst();
        cursor.close();
        return exists;
    }

    public boolean addTransaction(String username, double amount, String type) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_AMOUNT, amount);
        values.put(COLUMN_TYPE, type);
        values.put(COLUMN_DATE, new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date()));
        long result = db.insert(TABLE_TRANSACTIONS, null, values);
        return result != -1;
    }

    @SuppressLint("Range")
    public List<Transaction> getTransactions(String username) {
        List<Transaction> transactions = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRANSACTIONS,
                new String[]{COLUMN_AMOUNT, COLUMN_TYPE, COLUMN_DATE},
                COLUMN_USERNAME + "=?", new String[]{username},
                null, null, COLUMN_DATE + " DESC");
        while (cursor.moveToNext()) {
            double amount = cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_AMOUNT));
            String type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE));
            String date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE));
            transactions.add(new Transaction(amount, type, date));
        }
        cursor.close();
        return transactions;
    }

    public boolean sendFriendRequest(String fromUser, String toUser) {
        if (fromUser.equals(toUser)) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if request already exists
        Cursor cursor = db.query(TABLE_FRIENDS,
                new String[]{COLUMN_STATUS},
                "(" + COLUMN_USER1 + "=? AND " + COLUMN_USER2 + "=?) OR (" +
                COLUMN_USER1 + "=? AND " + COLUMN_USER2 + "=?)",
                new String[]{fromUser, toUser, toUser, fromUser},
                null, null, null);

        if (cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        cursor.close();

        ContentValues values = new ContentValues();
        values.put(COLUMN_USER1, fromUser);
        values.put(COLUMN_USER2, toUser);
        values.put(COLUMN_STATUS, STATUS_PENDING);

        long result = db.insert(TABLE_FRIENDS, null, values);
        return result != -1;
    }

    public boolean acceptFriendRequest(String user, String friendUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_STATUS, STATUS_ACCEPTED);

        int result = db.update(TABLE_FRIENDS, values,
                COLUMN_USER1 + "=? AND " + COLUMN_USER2 + "=? AND " + COLUMN_STATUS + "=?",
                new String[]{friendUser, user, STATUS_PENDING});
        return result > 0;
    }

    public boolean rejectFriendRequest(String user, String friendUser) {
        SQLiteDatabase db = this.getWritableDatabase();
        int result = db.delete(TABLE_FRIENDS,
                COLUMN_USER1 + "=? AND " + COLUMN_USER2 + "=? AND " + COLUMN_STATUS + "=?",
                new String[]{friendUser, user, STATUS_PENDING});
        return result > 0;
    }

    @SuppressLint("Range")
    public List<String> getFriends(String username) {
        List<String> friends = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get friends where user is either user1 or user2 and status is accepted
        Cursor cursor = db.query(TABLE_FRIENDS,
                new String[]{COLUMN_USER1, COLUMN_USER2},
                "(" + COLUMN_USER1 + "=? OR " + COLUMN_USER2 + "=?) AND " + COLUMN_STATUS + "=?",
                new String[]{username, username, STATUS_ACCEPTED},
                null, null, null);

        while (cursor.moveToNext()) {
            String user1 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER1));
            String user2 = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER2));
            friends.add(user1.equals(username) ? user2 : user1);
        }
        cursor.close();
        return friends;
    }

    @SuppressLint("Range")
    public List<String> getPendingFriendRequests(String username) {
        List<String> requests = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Get pending requests where user is the recipient
        Cursor cursor = db.query(TABLE_FRIENDS,
                new String[]{COLUMN_USER1},
                COLUMN_USER2 + "=? AND " + COLUMN_STATUS + "=?",
                new String[]{username, STATUS_PENDING},
                null, null, null);

        while (cursor.moveToNext()) {
            requests.add(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_USER1)));
        }
        cursor.close();
        return requests;
    }

    public boolean sendMoneyToFriend(String fromUser, String toUser, double amount) {
        SQLiteDatabase db = this.getReadableDatabase();

        // Check if they are friends
        Cursor cursor = db.query(TABLE_FRIENDS,
                new String[]{COLUMN_ID},
                "((" + COLUMN_USER1 + "=? AND " + COLUMN_USER2 + "=?) OR (" +
                COLUMN_USER1 + "=? AND " + COLUMN_USER2 + "=?)) AND " + COLUMN_STATUS + "=?",
                new String[]{fromUser, toUser, toUser, fromUser, STATUS_ACCEPTED},
                null, null, null);

        if (!cursor.moveToFirst()) {
            cursor.close();
            return false;
        }
        cursor.close();

        // Get sender's balance
        double senderBalance = getBalance(fromUser);
        if (senderBalance < amount) {
            return false;
        }

        // Get recipient's balance
        double recipientBalance = getBalance(toUser);

        // Update balances and add transactions
        db = this.getWritableDatabase();
        db.beginTransaction();
        try {
            // Update sender's balance
            updateBalance(fromUser, senderBalance - amount);
            addTransaction(fromUser, -amount, "Sent to " + toUser);

            // Update recipient's balance
            updateBalance(toUser, recipientBalance + amount);
            addTransaction(toUser, amount, "Received from " + fromUser);

            db.setTransactionSuccessful();
            return true;
        } finally {
            db.endTransaction();
        }
    }

    public boolean updateUserProfile(String username, String fullName, String email) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (fullName != null) values.put(COLUMN_FULL_NAME, fullName);
        if (email != null && !isEmailExists(email)) {
            values.put(COLUMN_EMAIL, email);
            values.put(COLUMN_EMAIL_VERIFIED, 0);
            values.put(COLUMN_VERIFICATION_TOKEN, generateVerificationToken());
        }
        int updated = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        return updated > 0;
    }

    public boolean updateProfilePicture(String username, String picturePath) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_PROFILE_PIC, picturePath);
        int updated = db.update(TABLE_USERS, values, COLUMN_USERNAME + "=?", new String[]{username});
        return updated > 0;
    }

    public boolean setSetting(String username, String key, String value) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_SETTING_KEY, key);
        values.put(COLUMN_SETTING_VALUE, value);

        try {
            return db.insertWithOnConflict(TABLE_SETTINGS, null, values,
                    SQLiteDatabase.CONFLICT_REPLACE) != -1;
        } catch (Exception e) {
            return false;
        }
    }

    public String getSetting(String username, String key) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SETTINGS,
                new String[]{COLUMN_SETTING_VALUE},
                COLUMN_USERNAME + "=? AND " + COLUMN_SETTING_KEY + "=?",
                new String[]{username, key},
                null, null, null);

        String value = null;
        if (cursor.moveToFirst()) {
            value = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_SETTING_VALUE));
        }
        cursor.close();
        return value;
    }

    public static class Transaction {
        private double amount;
        private String type;
        private String date;

        public Transaction(double amount, String type, String date) {
            this.amount = amount;
            this.type = type;
            this.date = date;
        }

        public double getAmount() {
            return amount;
        }

        public String getType() {
            return type;
        }

        public String getDate() {
            return date;
        }
    }
}