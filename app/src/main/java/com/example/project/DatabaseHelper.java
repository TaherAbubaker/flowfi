package com.example.project;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "flowfi.db";
    private static final int DATABASE_VERSION = 2;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "name TEXT NOT NULL," +
                "email TEXT UNIQUE NOT NULL," +
                "password TEXT NOT NULL," +
                "currency TEXT DEFAULT 'USD'," +
                "created_at TEXT DEFAULT (datetime('now')))");

        db.execSQL("CREATE TABLE transactions (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "amount REAL NOT NULL," +
                "category TEXT NOT NULL," +
                "type TEXT CHECK(type IN ('income','expense')) NOT NULL," +
                "note TEXT," +
                "date TEXT NOT NULL," +
                "created_at TEXT DEFAULT (datetime('now'))," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE savings_goals (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "target_amount REAL NOT NULL," +
                "current_amount REAL DEFAULT 0," +
                "deadline TEXT," +
                "created_at TEXT DEFAULT (datetime('now'))," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");

        db.execSQL("CREATE TABLE challenges (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "user_id INTEGER NOT NULL," +
                "title TEXT NOT NULL," +
                "description TEXT," +
                "target_days INTEGER NOT NULL," +
                "current_streak INTEGER DEFAULT 0," +
                "best_streak INTEGER DEFAULT 0," +
                "is_active INTEGER DEFAULT 1," +
                "start_date TEXT," +
                "FOREIGN KEY(user_id) REFERENCES users(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        db.execSQL("DROP TABLE IF EXISTS transactions");
        db.execSQL("DROP TABLE IF EXISTS savings_goals");
        db.execSQL("DROP TABLE IF EXISTS challenges");
        onCreate(db);
    }

    // ── USER METHODS ──────────────────────────────────────────

    public boolean registerUser(String name, String email, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("name", name);
        cv.put("email", email);
        cv.put("password", password);
        long result = db.insert("users", null, cv);
        db.close();
        return result != -1;
    }

    public boolean emailExists(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE email = ?", new String[]{email});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        db.close();
        return exists;
    }

    public int loginUser(String email, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT id FROM users WHERE email = ? AND password = ?",
                new String[]{email, password});
        int userId = -1;
        if (cursor.moveToFirst()) {
            userId = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return userId;
    }

    public String getUserName(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT name FROM users WHERE id = ?",
                new String[]{String.valueOf(userId)});
        String name = "User";
        if (cursor.moveToFirst()) name = cursor.getString(0);
        cursor.close();
        db.close();
        return name;
    }

    // ── TRANSACTION METHODS ───────────────────────────────────

    public boolean addTransaction(int userId, double amount, String category,
                                  String type, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("amount", amount);
        cv.put("category", category);
        cv.put("type", type);
        cv.put("note", note);
        cv.put("date", date);
        long result = db.insert("transactions", null, cv);
        db.close();
        return result != -1;
    }

    public Cursor getAllTransactions(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM transactions WHERE user_id = ? ORDER BY date DESC",
                new String[]{String.valueOf(userId)});
    }

    public double getTotalByType(int userId, String type) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions WHERE user_id = ? AND type = ?",
                new String[]{String.valueOf(userId), type});
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }

    public boolean deleteTransaction(int transactionId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("transactions", "id = ?",
                new String[]{String.valueOf(transactionId)});
        db.close();
        return rows > 0;
    }

    public boolean updateTransaction(int id, double amount, String category,
                                     String type, String note, String date) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("amount", amount);
        cv.put("category", category);
        cv.put("type", type);
        cv.put("note", note);
        cv.put("date", date);
        int rows = db.update("transactions", cv, "id = ?",
                new String[]{String.valueOf(id)});
        db.close();
        return rows > 0;
    }

    // ── SAVINGS GOAL METHODS ──────────────────────────────────

    public boolean addGoal(int userId, String title, double target, String deadline) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("title", title);
        cv.put("target_amount", target);
        cv.put("deadline", deadline);
        long result = db.insert("savings_goals", null, cv);
        db.close();
        return result != -1;
    }

    public Cursor getAllGoals(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM savings_goals WHERE user_id = ? ORDER BY created_at DESC",
                new String[]{String.valueOf(userId)});
    }

    public boolean updateGoalAmount(int goalId, double newAmount) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("current_amount", newAmount);
        int rows = db.update("savings_goals", cv, "id = ?",
                new String[]{String.valueOf(goalId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteGoal(int goalId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("savings_goals", "id = ?",
                new String[]{String.valueOf(goalId)});
        db.close();
        return rows > 0;
    }

    // ── DASHBOARD METHODS ─────────────────────────────────────

    public Cursor getRecentTransactions(int userId, int limit) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM transactions WHERE user_id = ? " +
                        "ORDER BY date DESC LIMIT ?",
                new String[]{String.valueOf(userId), String.valueOf(limit)});
    }

    public int getBestStreak(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT MAX(current_streak) FROM challenges WHERE user_id = ?",
                new String[]{String.valueOf(userId)});
        int streak = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) {
            streak = cursor.getInt(0);
        }
        cursor.close();
        db.close();
        return streak;
    }

    // ── ANALYTICS METHODS ─────────────────────────────────────

    public double getTotalByTypeSince(int userId, String type, String sinceDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT SUM(amount) FROM transactions " +
                        "WHERE user_id = ? AND type = ? AND date >= ?",
                new String[]{String.valueOf(userId), type, sinceDate});
        double total = 0;
        if (cursor.moveToFirst() && !cursor.isNull(0)) total = cursor.getDouble(0);
        cursor.close();
        db.close();
        return total;
    }

    public Cursor getExpenseTotalsByCategory(int userId, String sinceDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT category, SUM(amount) as total FROM transactions " +
                        "WHERE user_id = ? AND type = 'expense' AND date >= ? " +
                        "GROUP BY category ORDER BY total DESC",
                new String[]{String.valueOf(userId), sinceDate});
    }

    public Cursor getDailyTotals(int userId, String sinceDate) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT date, " +
                        "SUM(CASE WHEN type='income'  THEN amount ELSE 0 END) as income, " +
                        "SUM(CASE WHEN type='expense' THEN amount ELSE 0 END) as expense " +
                        "FROM transactions WHERE user_id = ? AND date >= ? " +
                        "GROUP BY date ORDER BY date ASC",
                new String[]{String.valueOf(userId), sinceDate});
    }

    // ── CHALLENGE METHODS ─────────────────────────────────────

    public boolean addChallenge(int userId, String title, String description,
                                int targetDays, String startDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("user_id", userId);
        cv.put("title", title);
        cv.put("description", description);
        cv.put("target_days", targetDays);
        cv.put("current_streak", 0);
        cv.put("best_streak", 0);
        cv.put("is_active", 1);
        cv.put("start_date", startDate);
        long result = db.insert("challenges", null, cv);
        db.close();
        return result != -1;
    }

    public Cursor getAllChallenges(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery(
                "SELECT * FROM challenges WHERE user_id = ? ORDER BY is_active DESC, start_date DESC",
                new String[]{String.valueOf(userId)});
    }

    public boolean incrementStreak(int challengeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Increment current_streak, update best_streak if needed
        db.execSQL(
                "UPDATE challenges SET " +
                        "current_streak = current_streak + 1, " +
                        "best_streak = MAX(best_streak, current_streak + 1) " +
                        "WHERE id = ?",
                new String[]{String.valueOf(challengeId)});
        db.close();
        return true;
    }

    public boolean resetStreak(int challengeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("current_streak", 0);
        int rows = db.update("challenges", cv, "id = ?",
                new String[]{String.valueOf(challengeId)});
        db.close();
        return rows > 0;
    }

    public boolean deleteChallenge(int challengeId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete("challenges", "id = ?",
                new String[]{String.valueOf(challengeId)});
        db.close();
        return rows > 0;
    }

    public boolean setChallengeActive(int challengeId, boolean active) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("is_active", active ? 1 : 0);
        int rows = db.update("challenges", cv, "id = ?",
                new String[]{String.valueOf(challengeId)});
        db.close();
        return rows > 0;
    }
}