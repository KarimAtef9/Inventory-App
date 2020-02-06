package com.example.inventory.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.inventory.data.InventoryContract.InvenEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final String DB_NAME = "inventory.db";
    public static final int DB_VERSION = 1;

    public static final String SQL_CREATE_TABLE = "CREATE TABLE " + InvenEntry.TABLE_NAME
            + " (" + InvenEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + InvenEntry.COLUMN_INVEN_NAME + " TEXT NOT NULL, "
            + InvenEntry.COLUMN_INVEN_DESCRIPTION + " TEXT, "
            + InvenEntry.COLUMN_INVEN_PRICE + " INTEGER NOT NULL, "
            + InvenEntry.COLUMN_INVEN_QUANTITY + " INTEGER NOT NULL DEFAULT 0, "
            + InvenEntry.COLUMN_INVEN_SUPPLIER + " TEXT, "
            + InvenEntry.COLUMN_INVEN_SUPPLIER_EMAIL + " TEXT, "
            + InvenEntry.COLUMN_INVEN_IMAGE + " BLOB"+ ")";

    public static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + InvenEntry.TABLE_NAME;

    public InventoryDbHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(SQL_CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        sqLiteDatabase.execSQL(SQL_DROP_TABLE);
        onCreate(sqLiteDatabase);
    }
}
