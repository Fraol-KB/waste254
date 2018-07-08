package com.example.fra.waste254sp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by KidusMT on 12/29/2016.
 */

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "un_members.db";//made a change here-members.db
    private static final String TABLE_NAME = "members";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "uname";
    private static final String COLUMN_PASSWORD = "pass";
    private static final String COLUMN_SECURITY_RESET = "security_question";

    SQLiteDatabase db;

    private static final String TABLE_CREATE = "create table members (id integer primary key not null , " +
            "uname text not null, pass text not null, security_question text not null );";

    public DatabaseHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE);
        this.db = db;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        String query = "DROP TABLE IF EXISTS "+TABLE_NAME;
        db.execSQL(query);
        this.onCreate(db);
    }

    //for inserting into the database
    public void insertMembers(Members m) {
        db = getWritableDatabase();
        ContentValues values = new ContentValues();

        String query = "select * from "+TABLE_NAME;
        Cursor cursor = db.rawQuery(query,null);
        int count = cursor.getCount();

        values.put(COLUMN_ID, count);
        values.put(COLUMN_USERNAME, m.getName());
        values.put(COLUMN_PASSWORD, m.getPword());
        values.put(COLUMN_SECURITY_RESET, m.getSecutiry_questions());

        db.insert(TABLE_NAME,null, values);
        db.close();
    }

    public String searchPass(String uname) {
        db = getReadableDatabase();
        String query = "select uname, pass from "+TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        String a, b;
        b = "not found";
        if(cursor.moveToFirst()){
            do {
                a = cursor.getString(0);

                if(a.equals(uname)){
                    b = cursor.getString(1);
                    break;
                }
            }
            while (cursor.moveToNext());
        }
        return b;
    }
}
