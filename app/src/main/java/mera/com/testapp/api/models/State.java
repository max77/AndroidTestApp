package mera.com.testapp.api.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import java.util.ArrayList;

public class State implements Parcelable {
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

    public State(String icao, String callsign, String country, float velocity) {
        this.mIcao24 = icao;
        this.mCallsign = callsign;
        this.mOriginCountry = country;
        this.mVelocity = velocity;
    }

    private State(Parcel parcel) {
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

    public static final Parcelable.Creator<State> CREATOR = new Parcelable.Creator<State>() {
        public State createFromParcel(Parcel in) {
            return new State(in);
        }
        public State[] newArray(int size) {
            return new State[size];
        }
    };

    public static State parse(ArrayList<String> stateRaw) {
        return new State(stateRaw.get(0), stateRaw.get(1),
                stateRaw.get(2), Float.parseFloat(stateRaw.get(10)));
    }

    @Override
    public boolean equals(Object obj) {
        State state = (State) obj;
        return TextUtils.equals(mIcao24, state.mIcao24)
                && TextUtils.equals(mCallsign, state.mCallsign)
                && mOriginCountry == state.mOriginCountry
                && mVelocity == state.mVelocity;
    }

    @Override
    public int hashCode() {
        return (int) mVelocity;
    }
}
