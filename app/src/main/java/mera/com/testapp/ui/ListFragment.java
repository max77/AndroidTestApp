package mera.com.testapp.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import mera.com.testapp.R;
import mera.com.testapp.api.WebService;
import mera.com.testapp.api.db.StateSortType;
import mera.com.testapp.api.models.AircraftState;

// db operations moved to background thread
// state saving/restoring added (country filter, countries list)
// country filter improved
public class ListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ListFragment.class.getSimpleName();
    private static final String EXTRA_CHOSEN_COUNTRY = "chosen_country";
    private static final String EXTRA_COUNTRIES = "countries";

    private Context mContext;
    private ArrayList<String> mCountries = new ArrayList<>();
    private String mCountryFilter;

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private RecyclerView mRecyclerView;
    private ListAdapter mAdapter;

    private WebService mWebService;
    private boolean isServiceBound;

    private StatesReceiver mStatesReceiver;

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            isServiceBound = true;

            WebService.LocalBinder localBinder = (WebService.LocalBinder) iBinder;
            mWebService = localBinder.getService();

            // code duplication removed
            updateAircraftListAndCountries();

            if (isServiceAvailable()) {
                mSwipeRefreshLayout.setRefreshing(true);
                mWebService.requestStates();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            isServiceBound = false;
            mWebService = null;
        }
    };

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mCountryFilter = savedInstanceState.getString(EXTRA_CHOSEN_COUNTRY, null);
            mCountries = savedInstanceState.getStringArrayList(EXTRA_COUNTRIES);
        }

        View v = inflater.inflate(R.layout.fragment_list, container, false);

        mContext = getContext();

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.list_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.list);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerView.setLayoutManager(layoutManager);
        mAdapter = new ListAdapter(mContext);
        mRecyclerView.setAdapter(mAdapter);

        registerReceivers();
        mContext.bindService(WebService.createServiceIntent(mContext), mConnection, Context.BIND_AUTO_CREATE);

        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.list_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.list_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        unregisterReceivers();
        try {
            mContext.unbindService(mConnection);

            isServiceBound = false;
            mWebService = null;
        } catch (Exception e) {
            Log.e(TAG, "An error occurred during the service stop.", e);
        }

        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putString(EXTRA_CHOSEN_COUNTRY, mCountryFilter);
        outState.putStringArrayList(EXTRA_COUNTRIES, mCountries);

        super.onSaveInstanceState(outState);
    }

    private void registerReceivers() {
        if (mStatesReceiver == null) {
            mStatesReceiver = new StatesReceiver();
            LocalBroadcastManager.getInstance(mContext)
                    .registerReceiver(mStatesReceiver, new IntentFilter(WebService.STATES_UPDATED_ACTION));
        }
    }

    private void unregisterReceivers() {
        if (mStatesReceiver != null) {
            try {
                LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mStatesReceiver);
            } catch (Exception e) {
                Log.e(TAG, "Can unregister StatesReceiver", e);
            }
        }
        mStatesReceiver = null;
    }

    private class StatesReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "StatesReceiver: onReceive action: " + intent.getAction());
            if (!WebService.isUpdateSuccessful(intent)) {
                showError();
            } else {
                updateAircraftListAndCountries();
            }

            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    // retreiving data from DB in background thread
    // shouldn't lead to leaked activity since the background data fetching is fast enough
    private void updateAircraftListAndCountries() {
        new AsyncTask<Void, Void, Set<AircraftState>>() {
            @Override
            protected Set<AircraftState> doInBackground(Void... voids) {
                Set<AircraftState> states = isServiceAvailable() ?
                        mWebService.getStatesLocal(mCountryFilter, StateSortType.NONE) :
                        null;

                if (states != null && mCountryFilter == null) {
                    SortedSet<String> countrySet = new TreeSet<>();

                    for (AircraftState state : states) {
                        countrySet.add(state.getOriginCountry());
                    }

                    mCountries = new ArrayList<>(countrySet);
                    mCountries.add(0, getString(R.string.country_filter_all));
                }

                return states;
            }

            @Override
            protected void onPostExecute(Set<AircraftState> aircraftStateSet) {
                // check for isAdded() to make sure that target activity exists
                if (isAdded() && aircraftStateSet != null && !aircraftStateSet.isEmpty()) {
                    mAdapter.setData(aircraftStateSet);
                    mRecyclerView.scrollToPosition(0);
                    MainActivity activity = (MainActivity) getActivity();
                    activity.updateActionBar(Integer.toString(aircraftStateSet.size()));
                }
            }
        }.execute();
    }

    private void showError() {
        Toast.makeText(mContext, R.string.update_error, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onRefresh() {
        if (isServiceAvailable()) {
            mSwipeRefreshLayout.setRefreshing(true);
            mWebService.requestStates();
        }
    }

    private void showFilterDialog() {
        if (mCountries.isEmpty()) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        int filterPosition = mCountryFilter != null ? mCountries.indexOf(mCountryFilter) : 0;
        builder.setSingleChoiceItems(mCountries.toArray(new String[0]), Math.max(0, filterPosition),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mCountryFilter = which == 0 ? null : mCountries.get(which);
                        updateAircraftListAndCountries();
                        dialog.dismiss();
                    }
                });
        builder.create().show();
    }

    private boolean isServiceAvailable() {
        return mWebService != null && isServiceBound;
    }
}
