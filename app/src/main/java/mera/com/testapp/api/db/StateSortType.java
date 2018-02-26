package mera.com.testapp.api.db;

/**
 * Created by max77 on 2/26/18.
 */
public enum StateSortType {
    NONE(""),
    VEL_ASC(StateTable.KEY_STATE_VELOCITY + " ASC"),
    VEL_DESC(StateTable.KEY_STATE_VELOCITY + " DESC"),
    SIGN_ASC(StateTable.KEY_STATE_CALLSIGN + " ASC"),
    SIGN_DESC(StateTable.KEY_STATE_CALLSIGN + " DESC"),
    DEFAULT(StateTable.KEY_STATE_COUNTRY);

    private final String mSortString;

    StateSortType(String sortString) {
        this.mSortString = sortString;
    }

    String getSortString() {
        return mSortString;
    }
}
