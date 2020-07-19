package com.example.camerademo;

import android.app.Activity;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @BelongsProject: DBHelper
 * @BelongsPackage: com.example.administrator.myapplication
 * @Author: Administrator
 * @CreateTime: 2020-01-08 10:58
 */

public class DBHelper extends SQLiteOpenHelper {

    private final static int DATABASE_VERSION = 1;
    private final static String DATABASE_NAME = "Data.db";
    public DBHelper(Context context, String name, SQLiteDatabase.CursorFactory factory, int version){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
   	 
        String usersInfo_table = "create table usertable" +
                "(id integer primary key autoincrement, username text," +
                "password text) ";
       db.execSQL(usersInfo_table);
       
       String t_map = "create table t_map" +
               "(id integer primary key autoincrement,source text,longitude text," +
               "latitude text) ";
      db.execSQL(t_map);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("alter table usertable add column other string");
    }

	 
}
