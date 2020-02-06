package com.example.inventory;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.inventory.data.InventoryContract.InvenEntry;

public class InvenCursorAdapter extends CursorAdapter {

    public InvenCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView summary = (TextView) view.findViewById(R.id.summary);
        TextView price = (TextView) view.findViewById(R.id.price);
        ImageView image = (ImageView) view.findViewById(R.id.image);

        String nameCursor = cursor.getString(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_NAME));
        String summaryCursor = cursor.getString(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_DESCRIPTION));
        int priceCursor = cursor.getInt(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_PRICE));
        byte[] imgByte = cursor.getBlob(cursor.getColumnIndex(InvenEntry.COLUMN_INVEN_IMAGE));



        if (summaryCursor == null || summaryCursor.isEmpty())
            summaryCursor = "No Description";

        name.setText(nameCursor);
        summary.setText(summaryCursor);
        price.setText(String.valueOf(priceCursor));
        if (imgByte != null) {
            Bitmap invenImage = BitmapFactory.decodeByteArray(imgByte, 0, imgByte.length);
            image.setImageBitmap(invenImage);
        } else {
            image.setBackgroundResource(R.drawable.no_image);
        }
    }
}
