package mera.com.testapp.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import mera.com.testapp.api.db.DatabaseHelper;
import mera.com.testapp.api.db.StateSortType;
import mera.com.testapp.api.models.AircraftState;
import mera.com.testapp.api.models.RawStates;
import retrofit2.Call;

public class WebService extends Service
{
    public static final String STATES_UPDATED_ACTION = "states_updated";

    private LocalBinder binder = new LocalBinder();

    public class LocalBinder extends Binder
    {
        public WebService getService()
        {
            return WebService.this;
        }
    }

    public static Intent createServiceIntent(Context context)
    {
        return new Intent(context, WebService.class);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent)
    {
        return binder;
    }

    public Set<AircraftState> getStatesLocal(Context context, String countryFilter, StateSortType sortType) {
        return DatabaseHelper.getInstance(context).queryStatesByCountry(countryFilter, sortType);
    }

    public void requestStates(final Context context)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ArrayList<AircraftState> statesArray = new ArrayList<>();
                WebApiManager wm = new WebApiManager();
                Call<RawStates> call = wm.getWebApiInterface().getStates();
                try
                {
                    RawStates rawStates = wm.execute(call);
                    if (rawStates != null)
                    {
                        List<List<String>> statesRaw = rawStates.getStates();
                        for (List<String> stateRaw : statesRaw)
                        {
                            statesArray.add(AircraftState.fromStrings(stateRaw));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DatabaseHelper helper = DatabaseHelper.getInstance(context);
                helper.clear();
                helper.insertStates(statesArray);

                sendBroadcast(new Intent(STATES_UPDATED_ACTION));
            }
        }).start();
    }
}
