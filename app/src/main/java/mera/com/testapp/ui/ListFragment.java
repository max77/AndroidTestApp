package mera.com.testapp.ui;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
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

import java.util.Set;

import mera.com.testapp.R;
import mera.com.testapp.api.WebService;
import mera.com.testapp.api.db.DatabaseHelper;
import mera.com.testapp.api.models.State;

public class ListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = ListFragment.class.getSimpleName();

    private Context mContext;
    private static final String[] COUNTRIES = new String[]{"All", "Germany", "United States"};

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private ListAdapter mAdapter;

    private WebService service;
    private boolean is_service_bound;

    private StatesReceiver mStatesReceiver;

    private String mCountryFilter;

    private int mChosenFilterPosition;

    public ListFragment(Context context) {
        mContext = context;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_list, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.list_refresh);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        RecyclerView recyclerView = (RecyclerView) v.findViewById(R.id.list);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        recyclerView.setLayoutManager(layoutManager);

        mAdapter = new ListAdapter();
        recyclerView.setAdapter(mAdapter);

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
        if(itemId == R.id.list_filter) {
            showFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unregisterReceivers();
        try {
            mContext.unbindService(mConnection);

            is_service_bound = false;
            service = null;
        } catch (Exception e) {
            Log.e(TAG, "An error occurred during the service stop.", e);
        }
    }

    private void registerReceivers() {
        if (mStatesReceiver == null) {
            mStatesReceiver = new StatesReceiver();
            mContext.registerReceiver(mStatesReceiver, new IntentFilter(WebService.STATES_UPDATED_ACTION));
        }
    }

    private void unregisterReceivers() {
        if (mStatesReceiver != null) {
            try {
                mContext.unregisterReceiver(mStatesReceiver);
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
            Set<State> localStates = service.getStatesLocal(mContext, mCountryFilter, DatabaseHelper.SortType.NONE);
            if (localStates != null && !localStates.isEmpty()) {
                mAdapter.setData(localStates);
                MainActivity activity = (MainActivity) getActivity();
                activity.updateActionBar(Integer.toString(localStates.size()));
            }
            mSwipeRefreshLayout.setRefreshing(false);
        }
    }

    @Override
    public void onRefresh() {
        if (isServiceAvailable()) {
            mSwipeRefreshLayout.setRefreshing(true);
            service.requestStates(mContext);
        }
    }

    private void showFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setSingleChoiceItems(COUNTRIES, mChosenFilterPosition, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                mChosenFilterPosition = which;
                if(which == 0) {
                    mCountryFilter = "";
                } else {
                    mCountryFilter = COUNTRIES[which];
                }
                getContext().sendBroadcast(new Intent(WebService.STATES_UPDATED_ACTION));
                dialog.dismiss();
            }
        });
        builder.create().show();
    }

    private boolean isServiceAvailable() {
        return service != null && is_service_bound;
    }

    private ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
            is_service_bound = true;

            WebService.LocalBinder localBinder = (WebService.LocalBinder) iBinder;
            service = localBinder.getService();
            Set<State> localStates = service.getStatesLocal(mContext, mCountryFilter, DatabaseHelper.SortType.NONE);
            if (localStates != null && !localStates.isEmpty()) {
                mAdapter.setData(localStates);
                MainActivity activity = (MainActivity) getActivity();
                activity.updateActionBar(Integer.toString(localStates.size()));
            }

            if (isServiceAvailable()) {
                mSwipeRefreshLayout.setRefreshing(true);
                service.requestStates(mContext);
            }
         }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            is_service_bound = false;
            service = null;
        }
    };
}
