package com.example.inventory;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.example.inventory.data.InventoryContract.InvenEntry;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVEN_LOADER = 0;
    InvenCursorAdapter invenCursorAdapter;
    public static final String LOG_TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Setup FAB to open EditorActivity
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                startActivity(intent);
            }
        });

        ListView listView = (ListView) findViewById(R.id.pets_list);

        // Find and set empty view on the ListView, so that it only shows when the list has 0 items.
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        invenCursorAdapter = new InvenCursorAdapter(this, null);
        listView.setAdapter(invenCursorAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                Uri invenUri = Uri.withAppendedPath(InvenEntry.CONTENT_URI, String.valueOf(id));
                intent.putExtra("invenUri", invenUri.toString());
                Log.v(LOG_TAG, "Edit inven with uri : " + invenUri.toString());
                startActivity(intent);
            }
        });

        // start off the loader
        getSupportLoaderManager().initLoader(INVEN_LOADER, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {InvenEntry._ID, InvenEntry.COLUMN_INVEN_NAME,
                InvenEntry.COLUMN_INVEN_DESCRIPTION, InvenEntry.COLUMN_INVEN_PRICE,
                InvenEntry.COLUMN_INVEN_IMAGE};

        return new CursorLoader(this, InvenEntry.CONTENT_URI, projection
                , null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        invenCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // callback called when the data needs to be deleted
        invenCursorAdapter.swapCursor(null);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Insert dummy data" menu option
            case R.id.action_insert_dummy_data:
                insertDummy();
                return true;
            // Respond to a click on the "Delete all" menu option
            case R.id.action_delete_all_entries:
                showDeleteConfirmationDialog();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void insertDummy() {
        ContentValues values = new ContentValues();
        values.put(InvenEntry.COLUMN_INVEN_NAME, "IPhone 6s");
        values.put(InvenEntry.COLUMN_INVEN_DESCRIPTION, "Mobile phone made by apple we mesh byhng");
        values.put(InvenEntry.COLUMN_INVEN_PRICE, 9000);
        values.put(InvenEntry.COLUMN_INVEN_QUANTITY, 3);
        values.put(InvenEntry.COLUMN_INVEN_SUPPLIER, "Souq");
        values.put(InvenEntry.COLUMN_INVEN_SUPPLIER_EMAIL, "karimatef65@gmail.com");

        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.iphone6s);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
        byte[] imageByte = outputStream.toByteArray();

        values.put(InvenEntry.COLUMN_INVEN_IMAGE, imageByte);

        Uri newUri = getContentResolver().insert(InvenEntry.CONTENT_URI, values);

        // Show a toast message depending on whether or not the insertion was successful
        if (newUri == null) {
            // If the new content URI is null, then there was an error with insertion.
            Toast.makeText(this, "Error in inserting dummy data",
                    Toast.LENGTH_SHORT).show();
        } else {
            // Otherwise, the insertion was successful and we can display a toast.
            Toast.makeText(this, "dummy data inserted",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete All ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteAllPets();
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the pet.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public void deleteAllPets() {
        int rowsDeleted = getContentResolver().delete(InvenEntry.CONTENT_URI, null, null);
        if (rowsDeleted > 0) {
            Toast.makeText(this, "All inventory deleted",
                    Toast.LENGTH_SHORT).show();
        }
        Log.v(LOG_TAG, "all inven deleted, deleted rows : " + rowsDeleted);
    }

}
