package mera.com.testapp.api.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import mera.com.testapp.api.models.AircraftState;

import static mera.com.testapp.api.db.StateTable.TABLE_STATE;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final int DB_VERSION = 1;

    // double-checked thread-safe singleton implementation (see Wiki ;-) )
    // p.s. no need of keeping static Context here
    private static volatile DatabaseHelper sInstance = null;

    public static DatabaseHelper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (DatabaseHelper.class) {
                if (sInstance == null) {
                    sInstance = new DatabaseHelper(context);
                }
            }
        }

        return sInstance;
    }

    private DatabaseHelper(Context context) {
        // replaced hardcoded version with constant field
        super(context, "database.db", null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StateTable.CREATE_TABLE_STATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // dumb upgrade policy
        db.execSQL(StateTable.DELETE_TABLE_STATE);
        onCreate(db);
    }

    // 1. insert WHAT ??
    // 2. should use more general type if possible
    // 3. what's the reason of returning false if not all states are inserted ?
    public boolean insertStates(List<AircraftState> states) {
        if (states == null || states.isEmpty()) {
            return false;
        }

        SQLiteDatabase db = getWritableDatabase();
        db.beginTransaction();

        try {
            for (AircraftState state : states) {
                ContentValues cv = new ContentValues(4);
                cv.put(StateTable.KEY_STATE_ICAO, state.getIcao24());
                cv.put(StateTable.KEY_STATE_CALLSIGN, state.getCallsign());
                cv.put(StateTable.KEY_STATE_COUNTRY, state.getOriginCountry());
                cv.put(StateTable.KEY_STATE_VELOCITY, state.getVelocity());

                if (db.insert(TABLE_STATE, null, cv) == -1) {
                    return false;
                }
            }

            db.setTransactionSuccessful();
            return true;

        } finally {
            db.endTransaction();
            db.close();
        }
    }

    // query WHAT ??
    // the filtering in the loop is non-optimal. Should use SQL instead
    // sortType should be non-null
    public Set<AircraftState> queryStatesByCountry(String countryFilter, @NonNull StateSortType sortType) {
        // insertion order must be preserved
        Set<AircraftState> result = new LinkedHashSet<>();

        // no need for writable db here
        SQLiteDatabase db = getReadableDatabase();

        String selection = !TextUtils.isEmpty(countryFilter) ?
                "upper(" + StateTable.KEY_STATE_COUNTRY + ")=upper(?)" :
                null;
        String[] selectionArgs = !TextUtils.isEmpty(countryFilter) ?
                new String[]{countryFilter} :
                null;

        Cursor cursor = db.query(TABLE_STATE,
                null,
                selection,
                selectionArgs,
                null,
                null,
                sortType.getSortString());

        while (cursor.moveToNext()) {
            result.add(StateTable.fromCursor(cursor));
        }

        db.close();
        return result;
    }

    // renamed to clear()
    // db not closed
    public void clear() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_STATE, null, null);
        db.close();
    }

    // getSortString(SortType) has been replaced with complete enum implementation (see StateSortType)
}
