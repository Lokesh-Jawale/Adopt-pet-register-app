package com.lokilabs.adoptdog.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.Nullable;

public class PetsDbHelper extends SQLiteOpenHelper {

    //DataBase file name
    public static final String DATABASE_NAME = "shelter.db";

    //Database version. If you change the database schema, you must increment the database version.
    public static final int DATABASE_VERSION = 1;

    public PetsDbHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        //CREATE  a string that haves the sql statement to create a table
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE "+ PetsContract.PetEntry.TABLE_NAME + " ("
                + PetsContract.PetEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
                + PetsContract.PetEntry.COLUMN_PET_NAME + " TEXT NOT NULL, "
                + PetsContract.PetEntry.COLUMN_PET_BREED + " TEXT,"
                + PetsContract.PetEntry.COLUMN_PET_GENDER + " INTEGER NOT NULL,"
                + PetsContract.PetEntry.COLUMN_PET_WEIGHT + " INTEGER NOT NULL DEFAULT 0 );";

        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

}
