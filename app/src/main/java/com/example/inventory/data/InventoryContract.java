package com.example.inventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class InventoryContract {
    //URI constants
    public static final String CONTENT_AUTHORITY = "com.example.inventory";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String INVEN_PATH = "Inven";


    //has one table so we make one inner class
    public static abstract class InvenEntry implements BaseColumns {
        public static final String TABLE_NAME = "Inven";

        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_INVEN_NAME = "name";
        public static final String COLUMN_INVEN_DESCRIPTION = "description";
        public static final String COLUMN_INVEN_PRICE = "price";
        public static final String COLUMN_INVEN_QUANTITY = "quantity";
        public static final String COLUMN_INVEN_SUPPLIER = "supplier";
        public static final String COLUMN_INVEN_SUPPLIER_EMAIL = "supplierEmail";
        public static final String COLUMN_INVEN_IMAGE = "image";

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, INVEN_PATH);

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of pets.
         */
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVEN_PATH;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single pet.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + INVEN_PATH;
    }
}
