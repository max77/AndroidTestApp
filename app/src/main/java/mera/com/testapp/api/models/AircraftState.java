package mera.com.testapp.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;

// renamed to AircraftState
// made final
public final class AircraftState implements Parcelable {
    private String mIcao24;
    private String mCallsign;
    private String mOriginCountry;
    private float time_position;
    private float time_velocity;
    private float longitude;
    private float latitude;
    private float altitude;
    private boolean mIsOnGround;
    private float mVelocity;
    private float heading;
    private float vertical_rate;

    public AircraftState(String icao, String callsign, String country, float velocity) {
        this.mIcao24 = icao;
        this.mCallsign = callsign;
        this.mOriginCountry = country;
        this.mVelocity = velocity;
    }

    private AircraftState(Parcel parcel) {
        mIcao24 = parcel.readString();
        mCallsign = parcel.readString();
        mOriginCountry = parcel.readString();
        time_position = parcel.readFloat();
        time_velocity = parcel.readFloat();
        longitude = parcel.readFloat();
        latitude = parcel.readFloat();
        altitude = parcel.readFloat();
        mIsOnGround = parcel.readInt() == 1;
        mVelocity = parcel.readFloat();
        heading = parcel.readFloat();
        vertical_rate = parcel.readFloat();
    }

    public String getIcao24() {
        return mIcao24;
    }

    public String getCallsign() {
        return mCallsign;
    }

    public String getOriginCountry() {
        return mOriginCountry;
    }

    public float getVelocity() {
        return mVelocity;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mIcao24);
        dest.writeString(mCallsign);
        dest.writeString(mOriginCountry);
        dest.writeFloat(time_position);
        dest.writeFloat(time_velocity);
        dest.writeFloat(longitude);
        dest.writeFloat(latitude);
        dest.writeFloat(altitude);
        dest.writeInt(mIsOnGround ? 1 : 0);
        dest.writeFloat(mVelocity);
        dest.writeFloat(heading);
        dest.writeFloat(vertical_rate);
    }

    public static final Parcelable.Creator<AircraftState> CREATOR = new Parcelable.Creator<AircraftState>() {
        public AircraftState createFromParcel(Parcel in) {
            return new AircraftState(in);
        }

        public AircraftState[] newArray(int size) {
            return new AircraftState[size];
        }
    };

    public static AircraftState fromStrings(List<String> stateRaw) {
        return new AircraftState(stateRaw.get(0), stateRaw.get(1),
                stateRaw.get(2), Float.parseFloat(stateRaw.get(10)));
    }

    // equals() and hashCode() has been replaced with Android Studio generated versions
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AircraftState that = (AircraftState) o;

        if (Float.compare(that.mVelocity, mVelocity) != 0) return false;
        if (!mIcao24.equals(that.mIcao24)) return false;
        if (!mCallsign.equals(that.mCallsign)) return false;
        return mOriginCountry.equals(that.mOriginCountry);
    }

    @Override
    public int hashCode() {
        int result = mIcao24.hashCode();
        result = 31 * result + mCallsign.hashCode();
        result = 31 * result + mOriginCountry.hashCode();
        result = 31 * result + (mVelocity != +0.0f ? Float.floatToIntBits(mVelocity) : 0);
        return result;
    }
}
