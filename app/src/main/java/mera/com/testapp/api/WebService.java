package mera.com.testapp.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.content.LocalBroadcastManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mera.com.testapp.api.db.DatabaseHelper;
import mera.com.testapp.api.db.StateSortType;
import mera.com.testapp.api.models.AircraftState;
import mera.com.testapp.api.models.RawStates;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WebService extends Service {
    public static final String STATES_UPDATED_ACTION = "states_updated";
    public static final String EXTRA_UPDATE_SUCCESS = "update_success";

    private final LocalBinder binder = new LocalBinder();
    private DatabaseHelper mDatabaseHelper;
    private WebApiInterface mWebApiInterface;
    private Call<RawStates> mCurrentCall;
    private final Object mLock = new Object();

    public class LocalBinder extends Binder {
        public WebService getService() {
            return WebService.this;
        }
    }

    public static Intent createServiceIntent(@NonNull Context context) {
        return new Intent(context, WebService.class);
    }

    public static boolean isUpdateSuccessful(@NonNull Intent broadcastIntent) {
        return broadcastIntent.getBooleanExtra(EXTRA_UPDATE_SUCCESS, true);
    }

    // added initialization code
    @Override
    public void onCreate() {
        super.onCreate();
        mDatabaseHelper = new DatabaseHelper(this);
        mWebApiInterface = new WebApiManager().getWebApiInterface();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // no need to provide context as a parameter
    // database access should be synchronized
    public Set<AircraftState> getStatesLocal(String countryFilter, StateSortType sortType) {
        synchronized (mLock) {
            return mDatabaseHelper.queryStatesByCountry(countryFilter, sortType);
        }
    }

    // just a convenience method
    public synchronized boolean isRequestPending() {
        return mCurrentCall != null && mCurrentCall.isExecuted();
    }

    // cancel current pending request to prevent queueing
    // use Retrofit's async request instead of sync+Thread
    // some basic error handling added
    public synchronized void requestStates() {
        if (isRequestPending()) {
            mCurrentCall.cancel();
        }

        mCurrentCall = mWebApiInterface.getStates();
        mCurrentCall.enqueue(new Callback<RawStates>() {
            @Override
            public void onResponse(Call<RawStates> call, Response<RawStates> response) {
                if (response.isSuccessful()) {
                    updateDatabaseAndSendBroadcast(response);
                } else {
                    broadcastStatus(false);
                }
            }

            @Override
            public void onFailure(Call<RawStates> call, Throwable t) {
                broadcastStatus(false);
            }
        });
    }

    private void updateDatabaseAndSendBroadcast(final Response<RawStates> response) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (!response.isSuccessful()) {
                    broadcastStatus(false);
                } else try {
                    List<AircraftState> states = new ArrayList<>();

                    List<List<String>> statesRaw = response.body().getStates();
                    for (List<String> stateRaw : statesRaw) {
                        states.add(AircraftState.fromStrings(stateRaw));
                    }

                    synchronized (mLock) {
                        mDatabaseHelper.clear();
                        mDatabaseHelper.insertStates(states);
                    }

                    broadcastStatus(true);
                } catch (Exception e) {
                    broadcastStatus(false);
                }
            }
        }).start();
    }

    // using faster and safer LocalBroadcastManager
    private void broadcastStatus(boolean success) {
        LocalBroadcastManager.getInstance(WebService.this)
                .sendBroadcast(new Intent(STATES_UPDATED_ACTION)
                        .putExtra(EXTRA_UPDATE_SUCCESS, success));
    }
}
