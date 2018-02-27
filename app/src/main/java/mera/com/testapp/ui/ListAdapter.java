package mera.com.testapp.ui;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;

import mera.com.testapp.R;
import mera.com.testapp.api.models.AircraftState;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private AircraftState[] mDataset = new AircraftState[0];
    private Context mContext;

    static class ViewHolder extends RecyclerView.ViewHolder {
        View rootView;
        TextView icao24;
        TextView callsign;
        TextView origin_country;
        TextView velocity;

        ViewHolder(View view) {
            super(view);
            rootView = view;
        }
    }

    ListAdapter(Context context) {
        mContext = context;
    }

    @Override
    public long getItemId(int position) {
        return super.getItemId(position);
    }

    public void setData(Set<AircraftState> dataset) {
        mDataset = dataset.toArray(new AircraftState[dataset.size()]);
        notifyDataSetChanged();
    }

    @Override
    public ListAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item, parent, false);
        ViewHolder vh = new ViewHolder(v);

        vh.icao24 = (TextView) v.findViewById(R.id.icao24);
        vh.callsign = (TextView) v.findViewById(R.id.callsign);
        vh.origin_country = (TextView) v.findViewById(R.id.origin_country);
        vh.velocity = (TextView) v.findViewById(R.id.velocity);

        return vh;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        AircraftState state = mDataset[position];

        holder.icao24.setText(state.getIcao24());
        holder.callsign.setText(state.getCallsign());
        holder.origin_country.setText(state.getOriginCountry());
        holder.velocity.setText(mContext.getString(R.string.velocity, state.getVelocity()));
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
