package mera.com.testapp.api.models;

import com.google.gson.annotations.Expose;

import java.util.ArrayList;

public class States {
    @Expose
    private long time;
    @Expose
    private ArrayList<ArrayList<String>> states;

    public long getTime() {
        return time;
    }

    public ArrayList<ArrayList<String>> getStates() {
        return states;
    }
}
