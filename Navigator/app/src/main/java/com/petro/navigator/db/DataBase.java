package com.petro.navigator.db;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 *
 * BaseClass do banco de dados
 */
public class DataBase extends SQLiteOpenHelper {
    private static final String DBNAME = "banco.db";
    private static final int VERSAO = 1;

    public DataBase(Context context) {
        super(context, DBNAME, null, VERSAO);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String sql;

        // creating localidade table
        sql = "create table LOCALIDADE (" +
                "ID integer primary key autoincrement," +
                "TITULO text," +
                "DESCRICAO text," +
                "LAT real," +
                "LON real)";
        db.execSQL(sql);

        // creating usuario table
        sql = "create table USUARIO (" +
                "ID integer primary key autoincrement," +
                "NOME text," +
                "USUARIO text)";
        db.execSQL(sql);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion != newVersion) {
            db.execSQL("drop table if exists LOCALIDADE");
            db.execSQL("drop table if exists USUARIO");

            onCreate(db);
        }

    }
}