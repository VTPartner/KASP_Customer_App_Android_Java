package com.kapstranspvtltd.kaps.utility;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class MyDatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "my_database";
    private static final int DATABASE_VERSION = 1;

    // Define your table(s) and column(s) here
    private static final String TABLE_NAME = "my_table";
    private static final String COLUMN_ID = "_id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_PRICE = "price";
    private static final String COLUMN_COUNT = "count";

    private static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + "(" +
                    COLUMN_ID + " INTEGER," +
                    COLUMN_NAME + " TEXT," +
                    COLUMN_PRICE + " TEXT," +
                    COLUMN_COUNT + " INTEGER" +
                    ")";

    public MyDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Handle database upgrades here
    }

    // Insert a new row into the database
    public long insertData(String id,String name, String price, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, id);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PRICE, price);
        values.put(COLUMN_COUNT, count);
        long result = db.insert(TABLE_NAME, null, values);
        Log.e("resule insert","-->"+result);
        db.close();
        return result;
    }

    // Update an existing row in the database
    public int updateData(String id, int count) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_COUNT, count);
        int result;
        if(count==0){
            result = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{id});

        }else {
            result = db.update(TABLE_NAME, values, COLUMN_ID + "=?", new String[]{id});

        }
        db.close();
        return result;
    }

    // Delete a row from the database
    public int deleteData(long id) {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
        return result;
    }

    public int deleteAllData() {
        SQLiteDatabase db = getWritableDatabase();
        int result = db.delete(TABLE_NAME, null, null);
        db.close();
        return result;
    }

    // Get all rows from the database
    @SuppressLint("Range")
    public List<MyData> getAllData() {
        List<MyData> dataList = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_NAME, null);
        if (cursor.moveToFirst()) {
            do {
               long id = cursor.getLong(cursor.getColumnIndex(COLUMN_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_NAME));
                String price = cursor.getString(cursor.getColumnIndex(COLUMN_PRICE));
                int count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));
                MyData data = new MyData(String.valueOf(id), name, price,count);
                dataList.add(data);
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return dataList;
    }

    @SuppressLint("Range")
    public int getData(String ids) {
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{ids});
        if (cursor.moveToFirst()) {

            int count = cursor.getInt(cursor.getColumnIndex(COLUMN_COUNT));

            cursor.close();
            return count;
        } else {
            cursor.close();
            return 0;
        }
    }
    @SuppressLint("Range")
    public int checkId(String ids) {
        int count=0;
        SQLiteDatabase db = this.getWritableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_ID + " = ?";

        Cursor cursor = db.rawQuery(query, new String[]{ids});

        if (cursor != null && cursor.getCount() > 0) {
            while(cursor.moveToNext()) {
                // Get the values for each column

                count = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_COUNT));
                cursor.close();

                // Do something with the values
            }
            return count;
        } else {
            if (cursor != null) {
                cursor.close();
            }
            return 0;
        }
    }
    public double getTotalPrice() {
        SQLiteDatabase db = getReadableDatabase();

        // Run the SUM query on the database and return the result as a double
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_PRICE + " * " +
                COLUMN_COUNT + ") AS total_price FROM " + TABLE_NAME, null);
        cursor.moveToFirst();
        double totalPrice = cursor.getDouble(0);
        cursor.close();

        return totalPrice;
    }
}