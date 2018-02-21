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
            KEY_STATE_VELOCITY + " TEXT " + ")";

    static State convert(Cursor cursor) {
        String icao = cursor.getString(cursor.getColumnIndex("state_icao"));
        String callsign = cursor.getString(cursor.getColumnIndex("state_callsign"));
        String country = cursor.getString(cursor.getColumnIndex("state_country"));
        String velocity = cursor.getString(cursor.getColumnIndex("state_velocity"));
        return new State(icao, callsign, country, TextUtils.isEmpty(velocity) ? 0f : Float.parseFloat(velocity));
    }
}
