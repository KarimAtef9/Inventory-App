package com.example.inventory;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.inventory.data.InventoryContract.InvenEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class EditorActivity extends AppCompatActivity
        implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final int INVEN_LOADER = 0;
    private static int RESULT_LOAD_IMAGE = 1;
    public static final String LOG_TAG = EditorActivity.class.getSimpleName();
    private Uri invenUri;
    private TextView nameTextView;
    private TextView descriptionTextView;
    private TextView priceTextView;
    private TextView quantityTextView;
    private TextView supplierTextView;
    private TextView supplierEmailTextView;
    private ImageView imageView;
    private byte[] defaultImageByte = null;

    public static final int MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 123;


    private boolean invenHasChanged = false;
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            invenHasChanged = true;
            return false;
        }
    };

    @SuppressLint({"ClickableViewAccessibility", "RestrictedApi"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_order);

        //asking for permission for images
        checkPermissionREAD_EXTERNAL_STORAGE(this);

        String uri = getIntent().getStringExtra("invenUri");
        if (uri == null) {
            setTitle("Add inventory");
            fab.setVisibility(View.GONE);
        } else {
            setTitle("Edit inventory");
            invenUri = Uri.parse(uri);
            // start off the loader
            getSupportLoaderManager().initLoader(INVEN_LOADER, null, this);
        }

        nameTextView = findViewById(R.id.name_text);
        descriptionTextView = findViewById(R.id.text_description);
        priceTextView = findViewById(R.id.text_price);
        quantityTextView = findViewById(R.id.text_quantity);
        supplierTextView = findViewById(R.id.text_supplier);
        supplierEmailTextView = findViewById(R.id.text_supplier_email);
        imageView = findViewById(R.id.image_view);

        //set touch listeners
        nameTextView.setOnTouchListener(mTouchListener);
        descriptionTextView.setOnTouchListener(mTouchListener);
        priceTextView.setOnTouchListener(mTouchListener);
        quantityTextView.setOnTouchListener(mTouchListener);
        supplierTextView.setOnTouchListener(mTouchListener);
        supplierEmailTextView.setOnTouchListener(mTouchListener);
        imageView.setOnTouchListener(mTouchListener);


        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(
                        Intent.ACTION_PICK,
                        android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

                startActivityForResult(i, RESULT_LOAD_IMAGE);
            }
        });

        final Object context = this;

        // Setup FAB to open a new mail to the supplier
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TextUtils.isEmpty(supplierEmailTextView.getText()))
                    Toast.makeText((Context) context, "Supplier Email Required",
                            Toast.LENGTH_SHORT).show();
                else {
                    Intent intent = new Intent(Intent.ACTION_SENDTO);
                    intent.setData(Uri.parse("mailto:" + supplierEmailTextView.getText())); // only email apps should handle this
                    intent.putExtra(Intent.EXTRA_SUBJECT,"More quantity required");
                    if (intent.resolveActivity(getPackageManager()) != null) {
                        startActivity(intent);
                        Log.v("EditorActivity.java", "Go to Gmail");
                    }
                }
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

            if (resultCode == RESULT_OK) {
                Uri imageUri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 0, outputStream);
                    defaultImageByte = outputStream.toByteArray();
                    Log.v(LOG_TAG, "New Image bitmap uploaded : " + bitmap);
                    imageView.setImageBitmap(bitmap);
                    imageView.postInvalidate();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {InvenEntry._ID, InvenEntry.COLUMN_INVEN_NAME,
                InvenEntry.COLUMN_INVEN_DESCRIPTION, InvenEntry.COLUMN_INVEN_PRICE,
                InvenEntry.COLUMN_INVEN_QUANTITY, InvenEntry.COLUMN_INVEN_SUPPLIER,
                InvenEntry.COLUMN_INVEN_SUPPLIER_EMAIL, InvenEntry.COLUMN_INVEN_IMAGE};

        return new CursorLoader(this, invenUri, projection
                , null, null, null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor.moveToFirst()) {
            String nameCursor = cursor.getString(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_NAME));
            String descriptionCursor = cursor.getString(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_DESCRIPTION));
            int price = cursor.getInt(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_PRICE));
            int quantity = cursor.getInt(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_QUANTITY));
            String supplierCursor = cursor.getString(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_SUPPLIER));
            String supplierEmailCursor = cursor.getString(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_SUPPLIER_EMAIL));


            nameTextView.setText(nameCursor);
            descriptionTextView.setText(descriptionCursor);
            priceTextView.setText(String.valueOf(price));
            quantityTextView.setText(String.valueOf(quantity));
            supplierTextView.setText(supplierCursor);
            supplierEmailTextView.setText(supplierEmailCursor);


            byte[] imgByte = cursor.getBlob(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_IMAGE));
            if (imgByte != null) {
                Bitmap imgBitmap = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
                imageView.setImageBitmap(imgBitmap);
            }


        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        nameTextView.setText("");
        descriptionTextView.setText("");
        priceTextView.setText("");
        quantityTextView.setText("");
        supplierTextView.setText("");
        supplierEmailTextView.setText("");
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.iphone6s);
        imageView.setImageBitmap(bitmap);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (invenUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                boolean saved = saveInven();
                if (saved) finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the inven hasn't changed, continue with navigating up to parent activity
                // which is the {@link CatalogActivity}.
                if (!invenHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }

                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                // Show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public boolean saveInven() {
        if (TextUtils.isEmpty(nameTextView.getText())) {
            Toast.makeText(this, "Name Required",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(priceTextView.getText())) {
            Toast.makeText(this, "Price Required",
                    Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(quantityTextView.getText())) {
            Toast.makeText(this, "Quantity Required",
                    Toast.LENGTH_SHORT).show();
            return false;
        }

        ContentValues values = new ContentValues();
        values.put(InvenEntry.COLUMN_INVEN_NAME, String.valueOf(nameTextView.getText()).trim());
        values.put(InvenEntry.COLUMN_INVEN_PRICE, String.valueOf(priceTextView.getText()));
        values.put(InvenEntry.COLUMN_INVEN_QUANTITY, String.valueOf(quantityTextView.getText()));
        if (!TextUtils.isEmpty(descriptionTextView.getText())) {
            values.put(InvenEntry.COLUMN_INVEN_DESCRIPTION,
                    String.valueOf(descriptionTextView.getText()).trim());
        }
        if (!TextUtils.isEmpty(supplierTextView.getText())) {
            values.put(InvenEntry.COLUMN_INVEN_SUPPLIER,
                    String.valueOf(supplierTextView.getText()).trim());
        }
        if (!TextUtils.isEmpty(supplierEmailTextView.getText())) {
            values.put(InvenEntry.COLUMN_INVEN_SUPPLIER_EMAIL,
                    String.valueOf(supplierEmailTextView.getText()).trim());
        }

        if (defaultImageByte != null) {
            values.put(InvenEntry.COLUMN_INVEN_IMAGE, defaultImageByte);
        }

        //check for update or insertion
        if (invenUri == null) { //insert
            getContentResolver().insert(InvenEntry.CONTENT_URI, values);
            Toast.makeText(this, "Inventory Inserted",
                    Toast.LENGTH_SHORT).show();
        } else { //update
            int rowsAffected = getContentResolver().update(invenUri, values, null, null);
            if (rowsAffected != 0)
                Toast.makeText(this, "Inventory Updated Successfully",
                        Toast.LENGTH_SHORT).show();
            else
                Toast.makeText(this, "Updated Failed",
                        Toast.LENGTH_SHORT).show();
        }


        return true;
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Delete this inventory ?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the pet.
                deleteInven();
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

    private void deleteInven() {
        int deletedRows = getContentResolver().delete(invenUri, null, null);
        Log.v(LOG_TAG,"deleted rows : " + deletedRows);
        if (deletedRows != 0)
            Toast.makeText(this, "Inventory Deleted",
                    Toast.LENGTH_SHORT).show();
        else
            Toast.makeText(this, "Error Deleting Inventory",
                    Toast.LENGTH_SHORT).show();
        finish();
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Discard your changes and quit editing?");
        builder.setPositiveButton("Discard", discardButtonClickListener);
        builder.setNegativeButton("Keep Editing", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
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

    @Override
    public void onBackPressed() {
        // If the pet hasn't changed, continue with handling back button press
        if (!invenHasChanged) {
            super.onBackPressed();
            return;
        }

        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };

        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }






    public boolean checkPermissionREAD_EXTERNAL_STORAGE(
            final Context context) {
        int currentAPIVersion = Build.VERSION.SDK_INT;
        if (currentAPIVersion >= android.os.Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(context,
                    Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(
                        (Activity) context,
                        Manifest.permission.READ_EXTERNAL_STORAGE)) {
                    showDialog("External storage", context,
                            Manifest.permission.READ_EXTERNAL_STORAGE);

                } else {
                    ActivityCompat
                            .requestPermissions(
                                    (Activity) context,
                                    new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
                                    MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                }
                return false;
            } else {
                return true;
            }

        } else {
            return true;
        }
    }

    public void showDialog(final String msg, final Context context,
                           final String permission) {
        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(context);
        alertBuilder.setCancelable(true);
        alertBuilder.setTitle("Permission necessary");
        alertBuilder.setMessage(msg + " permission is necessary");
        alertBuilder.setPositiveButton(android.R.string.yes,
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        ActivityCompat.requestPermissions((Activity) context,
                                new String[] { permission },
                                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE);
                    }
                });
        AlertDialog alert = alertBuilder.create();
        alert.show();
    }

}
