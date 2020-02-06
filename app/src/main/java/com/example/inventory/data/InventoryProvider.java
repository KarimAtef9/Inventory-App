package com.example.inventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.inventory.data.InventoryContract.InvenEntry;


public class InventoryProvider extends ContentProvider {
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();
    public InventoryDbHelper mDbHelper;
    /** URI matcher code for the content URI for the inventory table */
    private static final int INVEN = 100;

    /** URI matcher code for the content URI for a single inventory in the inventory table */
    private static final int INVEN_ID = 101;
    /**
     * UriMatcher object to match a content URI to a corresponding code.
     * The input passed into the constructor represents the code to return for the root URI.
     * It's common to use NO_MATCH as the input for this case.
     */
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.INVEN_PATH, INVEN);

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY,InventoryContract.INVEN_PATH + "/#",INVEN_ID);
    }


    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }


    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        // Get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();

        // This cursor will hold the result of the query
        Cursor cursor;

        // Figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVEN:
                // For the INVEN code, query the inventory table directly with the given
                // projection, selection, selection arguments, and sort order. The cursor
                // could contain multiple rows of the pets table.
                cursor = database.query(InvenEntry.TABLE_NAME, projection,
                        selection, selectionArgs,null, null, sortOrder);
                break;
            case INVEN_ID:
                // For the INVEN_ID code, extract out the ID from the URI.
                // For an example URI such as "content://com.example.android.inventory/Inven/3",
                // the selection will be "_id=?" and the selection argument will be a
                // String array containing the actual ID of 3 in this case.
                //
                // For every "?" in the selection, we need to have an element in the selection
                // arguments that will fill in the "?". Since we have 1 question mark in the
                // selection, we have 1 String in the selection arguments' String array.
                selection = InvenEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // This will perform a query on the pets table where the _id equals 3 to return a
                // Cursor containing that row of the table.
                cursor = database.query(InvenEntry.TABLE_NAME, projection, selection, selectionArgs,
                        null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }
        // now if data at this uri changes we will know and update the cursor
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVEN:
                return InvenEntry.CONTENT_LIST_TYPE;
            case INVEN_ID:
                return InvenEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVEN:
                return insertInven(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    /**
     * Insert a inven into the database with the given content values. Return the new content URI
     * for that specific row in the database.
     */
    private Uri insertInven(Uri uri, ContentValues values) {
        String name = values.getAsString(InvenEntry.COLUMN_INVEN_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Pet requires a name");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(InvenEntry.COLUMN_INVEN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Inven requires valid quantity");
        }

        // check that price greater than or equal to 0
        Integer price = values.getAsInteger(InvenEntry.COLUMN_INVEN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Pet requires valid price");
        }

        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final long id = database.insert(InvenEntry.TABLE_NAME, null, values);
        // Once we know the ID of the new row in the table,
        // return the new URI with the ID appended to the end of it
        if (id == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }
        else
            Log.e(LOG_TAG, "Inventory Saved with id: " + id);

        //notify all listeners that the uri has changed
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, id);
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVEN:
                return updateInven(uri, contentValues, selection, selectionArgs);
            case INVEN_ID:
                // For the INVEN_ID code, extract out the ID from the URI,
                // so we know which row to update. Selection will be "_id=?" and selection
                // arguments will be a String array containing the actual ID.
                selection = InvenEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInven(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    /**
     * Update pets in the database with the given content values. Apply the changes to the rows
     * specified in the selection and selection arguments (which could be 0 or 1 or more pets).
     * Return the number of rows that were successfully updated.
     */
    private int updateInven(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        // check that the name value is not null.
        if (values.containsKey(InvenEntry.COLUMN_INVEN_NAME)) {
            String name = values.getAsString(InvenEntry.COLUMN_INVEN_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Inven requires a name");
            }
        }

        // check that the price value is valid.
        if (values.containsKey(InvenEntry.COLUMN_INVEN_PRICE)) {
            Integer price = values.getAsInteger(InvenEntry.COLUMN_INVEN_PRICE);
            if (price == null || price < 0) {
                throw new IllegalArgumentException("Inven requires valid price");
            }
        }

        // check that the quantity value is valid.
        if (values.containsKey(InvenEntry.COLUMN_INVEN_QUANTITY)) {
            Integer quantity = values.getAsInteger(InvenEntry.COLUMN_INVEN_QUANTITY);
            if (quantity == null || quantity < 0) {
                throw new IllegalArgumentException("Inven requires valid quantity");
            }
        }

        // If there are no values to update, then don't try to update the database
        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = database.update(InvenEntry.TABLE_NAME, values, selection, selectionArgs);
        // If 1 or more rows were updated, then notify all listeners that the data at the
        // given URI has changed
        if (rowsUpdated != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        // Get writable database
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        final int match = sUriMatcher.match(uri);
        int rowsDeleted;
        switch (match) {
            case INVEN:
                // Delete all rows that match the selection and selection args
                rowsDeleted = database.delete(InvenEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case INVEN_ID:
                // Delete a single row given by the ID in the URI
                selection = InvenEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                rowsDeleted = database.delete(InvenEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
        // If 1 or more rows were deleted, then notify all listeners that the data at the
        // given URI has changed
        if (rowsDeleted != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsDeleted;
    }
}
