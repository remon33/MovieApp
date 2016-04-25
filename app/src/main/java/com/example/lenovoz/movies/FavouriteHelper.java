package com.example.lenovoz.movies;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by lenovo on 4/20/2016.
 */
public class FavouriteHelper extends SQLiteOpenHelper {
    public static final String DATABASE_NAME = "favourite.db";
    public static final String TABLE_NAME = "favourite_table";
    public static final String COL_1 = "ID";
    public static final String COL_2 = "POSTER_URL";
    public static final String COL_3 = "DATE";
    public static final String COL_4 = "AVERAGE";
    public static final String COL_5 = "OVERVIEW";
    public static final String COL_6 = "MOVIE_ID";
    public static final String COL_7 = "TITLE";

    public FavouriteHelper(Context context) {
        super(context, DATABASE_NAME, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + TABLE_NAME + " (ID INTEGER PRIMARY KEY AUTOINCREMENT," +
                "POSTER_URL TEXT,DATE TEXT,AVERAGE TEXT,OVERVIEW TEXT,MOVIE_ID TEXT,TITLE TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public void deleteDuplicates(){
        getWritableDatabase().execSQL("delete from favourite_table where ID not in (SELECT MIN(ID ) FROM favourite_table GROUP BY MOVIE_ID)");
    }



    public boolean insertData(String poster,String date,String average, String overview, String movieId, String title) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(COL_2,poster);
        contentValues.put(COL_3,date);
        contentValues.put(COL_4, average);
        contentValues.put(COL_5, overview);
        contentValues.put(COL_6, movieId);
        contentValues.put(COL_7, title);
        long result = db.insert(TABLE_NAME, null ,contentValues);
        if(result == -1)
            return false;
        else
            return true;
    }

    public Cursor getAllData(){
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor result = db.rawQuery("select * from " + TABLE_NAME, null);
        return result;
    }


}

