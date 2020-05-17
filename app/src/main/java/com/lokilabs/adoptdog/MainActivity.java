package com.lokilabs.adoptdog;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.lokilabs.adoptdog.adapter.PetsCursorAdapter;
import com.lokilabs.adoptdog.data.PetsContract;
import com.lokilabs.adoptdog.data.PetsContract.PetEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    ListView listView;
    PetsCursorAdapter petsCursorAdapter;

    private static final int PET_LOADER = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FloatingActionButton floatingActionButton = (FloatingActionButton) findViewById(R.id.floating_action_button);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);
                    startActivity(intent);
                }catch (Exception e){
                    e.getMessage();
                }
            }
        });

        listView = (ListView) findViewById(R.id.listView);
        View emptyView = findViewById(R.id.empty_view);
        listView.setEmptyView(emptyView);

        petsCursorAdapter = new PetsCursorAdapter(this, null);
        listView.setAdapter(petsCursorAdapter);

        getSupportLoaderManager().initLoader(0, null, this);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener(){
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                try {
                    Intent intent = new Intent(MainActivity.this, EditorActivity.class);

                    Uri currentUri = ContentUris.withAppendedId(PetEntry.CONTENT_URI, id);
                    intent.setData(currentUri);

                    startActivity(intent);

                } catch (Exception e) {
                    e.getMessage();
                }
            }
        });

    }

    public void insertData() {

        ContentValues values = new ContentValues();
        values.put(PetsContract.PetEntry.COLUMN_PET_NAME, "Garfield");
        values.put(PetsContract.PetEntry.COLUMN_PET_BREED, "Tabby");
        values.put(PetsContract.PetEntry.COLUMN_PET_GENDER, PetsContract.PetEntry.GENDER_MALE);
        values.put(PetsContract.PetEntry.COLUMN_PET_WEIGHT, 7);

        Uri newURi = getContentResolver().insert(PetEntry.CONTENT_URI, values);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a main_menu option in the app bar overflow main_menu
        switch (item.getItemId()) {
            case R.id.action_insert_dummy_data:
                insertData();
                return true;
            case R.id.action_delete_dummy_data:
                deleteDummyData();
                return true;
            default:
                return super.onOptionsItemSelected(item);

        }
    }

    private void deleteDummyData() {
        int rowDelete = getContentResolver().delete(PetEntry.CONTENT_URI, null, null);

        if(rowDelete == 0){
            Toast.makeText(this, "Error while deleting the pets data", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "All pets ", Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                PetEntry._ID,
                PetEntry.COLUMN_PET_NAME,
                PetEntry.COLUMN_PET_BREED };

        // This loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(this,   // Parent activity context
                PetEntry.CONTENT_URI,   // Provider content URI to query
                projection,             // Columns to include in the resulting Cursor
                null,                   // No selection clause
                null,                   // No selection arguments
                null);                  // Default sort order
    }

    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {
        petsCursorAdapter.swapCursor(cursor);
    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        petsCursorAdapter.swapCursor(null);
    }
}


