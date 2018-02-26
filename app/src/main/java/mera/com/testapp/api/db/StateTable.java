package mera.com.testapp.api.db;

import android.database.Cursor;
import android.text.TextUtils;

import mera.com.testapp.api.models.State;

class StateTable {
    static final String TABLE_STATE = "state_table";
    static final String KEY_STATE_ICAO = "state_icao";
    static final String KEY_STATE_CALLSIGN = "state_callsign";
    static final String KEY_STATE_COUNTRY = "state_country";
    static final String KEY_STATE_VELOCITY = "state_velocity";

    static final String CREATE_TABLE_STATE = "CREATE TABLE IF NOT EXISTS " + TABLE_STATE + " (" +
            KEY_STATE_ICAO + " TEXT, " +
            KEY_STATE_CALLSIGN + " TEXT, " +
            KEY_STATE_COUNTRY + " TEXT, " +
            // why store text value for velocity ??
            KEY_STATE_VELOCITY + " REAL " + ")";

    static final String DELETE_TABLE_STATE = "DROP TABLE IF EXISTS " + TABLE_STATE;

    // 1. method has been given a meaningful name
    // 2. method has been changed according to new table scheme (REAL velocity)
    static State fromCursor(Cursor cursor) {
        String icao = cursor.getString(cursor.getColumnIndex(KEY_STATE_ICAO));
        String callsign = cursor.getString(cursor.getColumnIndex(KEY_STATE_CALLSIGN));
        String country = cursor.getString(cursor.getColumnIndex(KEY_STATE_COUNTRY));

        float velocity;

        try {
            velocity = cursor.getFloat(cursor.getColumnIndex(KEY_STATE_VELOCITY));
        } catch (Exception e) {
            velocity = 0;
        }

        return new State(icao, callsign, country, velocity);
    }
}
