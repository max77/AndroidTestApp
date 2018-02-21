package mera.com.testapp.api.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import mera.com.testapp.api.models.State;

import static mera.com.testapp.api.db.StateTable.TABLE_STATE;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static DatabaseHelper sInstance;
    private static Context sContext;

    public static DatabaseHelper getInstance(Context context) {
        sContext = context;
        if (sInstance == null) {
            sInstance = new DatabaseHelper();
        }

        return sInstance;
    }

    public DatabaseHelper() {
        super(sContext, "database.db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(StateTable.CREATE_TABLE_STATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}

    public boolean insert(ArrayList<State> states) {
        if (states == null || states.isEmpty()) {
            return false;
        }

        boolean isAllInserted = true;

        SQLiteDatabase db = getWritableDatabase();
        for (State state : states) {
            ContentValues cv = new ContentValues(10);
            cv.put(StateTable.KEY_STATE_ICAO, state.getIcao24());
            cv.put(StateTable.KEY_STATE_CALLSIGN, state.getCallsign());
            cv.put(StateTable.KEY_STATE_COUNTRY, state.getOriginCountry());
            cv.put(StateTable.KEY_STATE_VELOCITY, state.getVelocity());

            long rowId = db.insert(TABLE_STATE, null, cv);
            if (rowId == -1) {
                isAllInserted = false;
                break;
            }
        }

        return isAllInserted;
    }

    public Set<State> query(String countryFilter, SortType type) {
        Set<State> result = new HashSet<>();

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_STATE, null, null, null, null, null, getSortString(type));
        while (cursor.moveToNext()) {
            result.add(StateTable.convert(cursor));
        }

        if(!TextUtils.isEmpty(countryFilter)) {
            for (State state : result) {
                if (!state.getOriginCountry().equals(countryFilter)) {
                    result.remove(state);
                }
            }
        }

        return result;
    }

    public void delete() {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_STATE, null, null);
    }

    private String getSortString(SortType sortType) {
        if(sortType == SortType.NONE) {
            return "";
        }

        String field;

        switch(sortType) {
            case VEL_ASC:
                field = StateTable.KEY_STATE_VELOCITY;
                break;
            case VEL_DESC:
                field = StateTable.KEY_STATE_VELOCITY;
                break;
            case SIGN_ASC:
                field = StateTable.KEY_STATE_CALLSIGN;
            case SIGN_DESC:
                field = StateTable.KEY_STATE_CALLSIGN;
                break;
            default:
                field = StateTable.KEY_STATE_COUNTRY;
                break;
        }

        String orderString = sortType.name().contains("ASC") ? "ASC" : "DESC";

        return field + " " + orderString;
    }

    public enum SortType {
        NONE, VEL_ASC, VEL_DESC, SIGN_ASC, SIGN_DESC,
    }
}
