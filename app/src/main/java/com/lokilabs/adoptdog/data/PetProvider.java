package com.lokilabs.adoptdog.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.lokilabs.adoptdog.data.PetsContract.PetEntry;

import java.util.Objects;

public class PetProvider extends ContentProvider {

    PetsDbHelper petsDbHelper;

    private static final int PETS = 100;
    private static final int PETS_ID = 101;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS, PETS);
        uriMatcher.addURI(PetsContract.CONTENT_AUTHORITY, PetsContract.PATH_PETS + "/#", PETS_ID);
    }

    @Override
    public boolean onCreate() {
        petsDbHelper = new PetsDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection, @Nullable String[] selectionArgs, @Nullable String sortOrder) {

        SQLiteDatabase database = petsDbHelper.getReadableDatabase();
        Cursor cursor;

        int match = uriMatcher.match(uri);

        switch (match) {
            case PETS:
                cursor = database.query(
                        PetsContract.PetEntry.TABLE_NAME
                        , projection
                        , selection
                        , selectionArgs
                        , null
                        , null
                        , sortOrder);
                break;

            case PETS_ID:
                selection = PetsContract.PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                //noinspection DuplicateBranchesInSwitch
                cursor = database.query(
                        PetsContract.PetEntry.TABLE_NAME
                        , projection
                        , selection
                        , selectionArgs
                        , null
                        , null
                        , sortOrder);
                break;

            default:
                cursor = null;
        }

        assert cursor != null;
        cursor.setNotificationUri(Objects.requireNonNull(getContext()).getContentResolver(), uri);

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        final int match = uriMatcher.match(uri);

        switch (match){
            case PETS:
                return PetEntry.CONTENT_LIST_TYPE;
            case PETS_ID:
                return PetEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalArgumentException("The Uri is invalid");
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        final int match = uriMatcher.match(uri);

        //noinspection SwitchStatementWithTooFewBranches
        switch (match) {
            case PETS:
                assert values != null;
                return insertPet(uri, values);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertPet(Uri uri, ContentValues values) {

        SQLiteDatabase database = petsDbHelper.getWritableDatabase();

        String name = values.getAsString(PetsContract.PetEntry.COLUMN_PET_NAME);
        if (name == null) {
            throw new IllegalArgumentException("No input given for Name");
        }
        String breed = values.getAsString(PetsContract.PetEntry.COLUMN_PET_BREED);
        if (breed == null) {
            throw new IllegalArgumentException("No input given for breed");
        }
        int gender = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_GENDER);
        if (gender == 0 || !PetsContract.isValidGender(gender)) {
            throw new IllegalArgumentException("Pet requires valid gender");
        }
        Integer weight = values.getAsInteger(PetsContract.PetEntry.COLUMN_PET_WEIGHT);
        if (weight != null && weight < 0) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }
        if (weight == null) {
            throw new IllegalArgumentException("Pet requires valid weight");
        }

        long id = database.insert(PetsContract.PetEntry.TABLE_NAME, null, values);

        if (id == -1) {
            Log.e("aDebugTag", "Failed to insert row for " + uri);
            return null;
        }

        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(PetsContract.PetEntry.CONTENT_URI, id);
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);

        SQLiteDatabase database = petsDbHelper.getWritableDatabase();

        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        switch (match) {

            case PETS:
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return database.delete(PetEntry.TABLE_NAME, selection, selectionArgs);

            default:
                throw new IllegalArgumentException("Invalid uri " + uri);
        }

    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {
        final int match = uriMatcher.match(uri);

        switch (match) {
            case PETS:
                return updatePet(uri, values, selection, selectionArgs);
            case PETS_ID:
                selection = PetEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updatePet(uri, values, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Unvalid update entry uri");
        }

    }

    private int updatePet(@NonNull Uri uri, @Nullable ContentValues values, @Nullable String selection, @Nullable String[] selectionArgs) {

        assert values != null;
        if (values.containsKey(PetEntry.COLUMN_PET_NAME)) {
            String name = values.getAsString(PetEntry.COLUMN_PET_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Pet requires a name");
            }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_GENDER)) {
            Integer gender = values.getAsInteger(PetEntry.COLUMN_PET_GENDER);
            if (gender == null || !PetsContract.isValidGender(gender)) {
                throw new IllegalArgumentException("Pet requires valid gender");
            }
        }

        if (values.containsKey(PetEntry.COLUMN_PET_WEIGHT)) {
            Integer weight = values.getAsInteger(PetEntry.COLUMN_PET_WEIGHT);
            if (weight != null && weight < 0) {
                throw new IllegalArgumentException("Pet requires valid weight");
            }
            if (weight == null) {
                throw new IllegalArgumentException("Pet requires valid weight input");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = petsDbHelper.getWritableDatabase();

        Objects.requireNonNull(getContext()).getContentResolver().notifyChange(uri, null);

        return database.update(PetEntry.TABLE_NAME, values, selection, selectionArgs);
    }

}
