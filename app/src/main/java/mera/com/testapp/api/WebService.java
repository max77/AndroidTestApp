package mera.com.testapp.api;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.Set;

import mera.com.testapp.api.db.DatabaseHelper;
import mera.com.testapp.api.models.State;
import mera.com.testapp.api.models.States;
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

    public Set<State> getStatesLocal(Context context, String countryFilter, DatabaseHelper.SortType sortType) {
        return DatabaseHelper.getInstance(context).query(countryFilter, sortType);
    }

    public void requestStates(final Context context)
    {
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                ArrayList<State> statesArray = new ArrayList<>();
                WebApiManager wm = new WebApiManager();
                Call<States> call = wm.getWebApiInterface().getStates();
                try
                {
                    States states = wm.execute(call);
                    if (states != null)
                    {
                        ArrayList<ArrayList<String>> statesRaw = states.getStates();
                        for (ArrayList<String> stateRaw : statesRaw)
                        {
                            statesArray.add(State.parse(stateRaw));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }

                DatabaseHelper helper = DatabaseHelper.getInstance(context);
                helper.delete();
                helper.insert(statesArray);

                sendBroadcast(new Intent(STATES_UPDATED_ACTION));
            }
        }).start();
    }
}
