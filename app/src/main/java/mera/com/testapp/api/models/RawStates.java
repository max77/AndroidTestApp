package mera.com.testapp.api.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

// renamed to RawStates
// addes explicit JSON field names declaration
// made final
public final class RawStates {
    @SerializedName("time")
    private long mTime;

    @SerializedName("states")
    // Should use more general types if possible
    private List<List<String>> mStates;

    public long getTime() {
        return mTime;
    }

    public List<List<String>> getStates() {
        return mStates;
    }
}
