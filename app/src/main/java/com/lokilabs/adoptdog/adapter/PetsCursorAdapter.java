package com.lokilabs.adoptdog.adapter;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.lokilabs.adoptdog.R;
import com.lokilabs.adoptdog.data.PetsContract;

public class PetsCursorAdapter extends CursorAdapter {

    public PetsCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_view_items, parent, false);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        TextView nameText = (TextView)view.findViewById(R.id.dog_name);
        TextView breedText = (TextView)view.findViewById(R.id.dog_breed);

        try {
            int nameColumnIndex = cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_BREED);

            String petName = cursor.getString(nameColumnIndex);
            String petBreed = cursor.getString(breedColumnIndex);

            nameText.setText(petName);
            breedText.setText(petBreed);
        }catch (Exception e){
            e.getMessage();
        }
    }

}
