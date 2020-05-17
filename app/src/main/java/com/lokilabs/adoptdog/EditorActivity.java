package com.lokilabs.adoptdog;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.LoaderManager;
import android.support.v4.app.NavUtils;
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
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.lokilabs.adoptdog.data.PetsContract;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    EditText nameText;
    EditText breedText;
    EditText weightText;
    Spinner spinner;

    private int genderValue = PetsContract.PetEntry.GENDER_UNKNOWN;
    boolean mPetHasChanged = false;

    private static Uri currentUri;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        try {
            Intent intent = getIntent();
            currentUri = intent.getData();
        } catch (Exception e) {
            Log.d(e.toString(), e.getMessage());
        }

        if (currentUri == null) {
            setTitle("Add a Pet");
            invalidateOptionsMenu();

        } else {
            setTitle("Edit pet info");
            getSupportLoaderManager().initLoader(1, null, this).forceLoad();
        }

        nameText = (EditText) findViewById(R.id.name_input_text);
        breedText = (EditText) findViewById(R.id.breed_input_text);
        weightText = (EditText) findViewById(R.id.weight_input_text);
        spinner = (Spinner) findViewById(R.id.spinner_gender);

        nameText.setOnTouchListener(mTouchListener);
        breedText.setOnTouchListener(mTouchListener);
        weightText.setOnTouchListener(mTouchListener);
        spinner.setOnTouchListener(mTouchListener);

        setSpinner();

    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent event) {
            mPetHasChanged = true;
            return false;
        }
    };

    private void setSpinner() {

        ArrayAdapter spinnerAdapter = ArrayAdapter.createFromResource(this, R.array.array_gender_options,
                android.R.layout.simple_spinner_item);

        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(spinnerAdapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedString = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selectedString)) {
                    if (selectedString.equals(getString(R.string.gender_male))) {
                        genderValue = PetsContract.PetEntry.GENDER_MALE;
                    } else if (selectedString.equals(getString(R.string.gender_female))) {
                        genderValue = PetsContract.PetEntry.GENDER_FEMALE;
                    } else {
                        genderValue = PetsContract.PetEntry.GENDER_UNKNOWN;
                    }
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                genderValue = PetsContract.PetEntry.GENDER_UNKNOWN;
            }
        });

    }

    private void savePet() {

        String name = nameText.getText().toString().trim();
        String breed = breedText.getText().toString().trim();
        int weight = Integer.parseInt(weightText.getText().toString().trim());

        if (currentUri == null &&
                TextUtils.isEmpty(name) && TextUtils.isEmpty(breed)
                && TextUtils.isEmpty(String.valueOf(weight)) && genderValue == PetsContract.PetEntry.GENDER_UNKNOWN) {
            Toast.makeText(this, "All data is to be filled", Toast.LENGTH_LONG).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(PetsContract.PetEntry.COLUMN_PET_NAME, name);
        values.put(PetsContract.PetEntry.COLUMN_PET_BREED, breed);
        values.put(PetsContract.PetEntry.COLUMN_PET_GENDER, genderValue);
        values.put(PetsContract.PetEntry.COLUMN_PET_WEIGHT, weight);

        if (currentUri == null) {
            Uri newUri = getContentResolver().insert(PetsContract.PetEntry.CONTENT_URI, values);

            if (newUri == null) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error the insertion of pet info failed",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Pet info added Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        } else {
            int updateRow = getContentResolver().update(currentUri, values, null, null);

            if (updateRow == 0) {
                // If the new content URI is null, then there was an error with insertion.
                Toast.makeText(this, "Error while update",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the insertion was successful and we can display a toast.
                Toast.makeText(this, "Pet info Updated Successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.editors_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new pet, hide the "Delete" menu item.
        if (currentUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public void onBackPressed() {
        if (!mPetHasChanged) {
            super.onBackPressed();
            return;
        }

        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                };

        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.alert_message_unsaved_data);
        alertDialogBuilder.setPositiveButton(getString(R.string.alert_Dialog_dicard_button_text), discardButtonClickListener);
        alertDialogBuilder.setNegativeButton(getString(R.string.alert_dialog_keepediting_button_text), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                // save pet info to database
                savePet();
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
                // Do nothing for now
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // Navigate back to parent activity (CatalogActivity)
                if (!mPetHasChanged) {
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }

                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };

                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void showDeleteConfirmationDialog() {
        
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setMessage(R.string.alert_message_delete_data);
        alertDialogBuilder.setPositiveButton(getString(R.string.alert_delete_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deletePet();
                        finish();
                    }
                });
        alertDialogBuilder.setNegativeButton(getString(R.string.alert_canceldelete_button),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if(dialog != null){
                            dialog.dismiss();
                        }
                    }
                });

        AlertDialog alertDialog = alertDialogBuilder.create();
        alertDialog.show();
    }

    private void deletePet() {
        int deleteRow = getContentResolver().delete(currentUri, null, null);

        if(deleteRow == 0){
            Toast.makeText(this, "Error while deleting the pet data", Toast.LENGTH_LONG).show();
        }
        else{
            Toast.makeText(this, "Pet data deleted", Toast.LENGTH_LONG).show();
        }
    }

    @NonNull
    @Override
    public Loader<Cursor> onCreateLoader(int i, @Nullable Bundle bundle) {
        String[] projection = {
                PetsContract.PetEntry._ID,
                PetsContract.PetEntry.COLUMN_PET_NAME,
                PetsContract.PetEntry.COLUMN_PET_BREED,
                PetsContract.PetEntry.COLUMN_PET_GENDER,
                PetsContract.PetEntry.COLUMN_PET_WEIGHT
        };

        return new CursorLoader(this,
                currentUri,
                projection,
                null,
                null,
                null
        );

    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onLoadFinished(@NonNull Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_NAME);
            int breedColumnIndex = cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_BREED);
            int genderColumnIndex = cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_GENDER);
            int weightColumnIndex = cursor.getColumnIndex(PetsContract.PetEntry.COLUMN_PET_WEIGHT);

            String name0 = cursor.getString(nameColumnIndex);
            String breed0 = cursor.getString(breedColumnIndex);
            int gender0 = cursor.getInt(genderColumnIndex);
            int weight0 = cursor.getInt(weightColumnIndex);

            nameText.setText(name0);
            breedText.setText(breed0);
            weightText.setText(Integer.toString(weight0));

            switch (gender0) {
                case PetsContract.PetEntry.GENDER_MALE:
                    spinner.setSelection(1);
                    break;
                case PetsContract.PetEntry.GENDER_FEMALE:
                    spinner.setSelection(2);
                    break;
                default:
                    spinner.setSelection(0);
                    break;
            }
        }

    }

    @Override
    public void onLoaderReset(@NonNull Loader<Cursor> loader) {
        nameText.setText("");
        breedText.setText("");
        weightText.setText("");
        spinner.setSelection(0);
    }

}
