package mera.com.testapp.ui;

import android.annotation.SuppressLint;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Set;

import mera.com.testapp.R;
import mera.com.testapp.api.models.State;

class ListAdapter extends RecyclerView.Adapter<ListAdapter.ViewHolder> {

    private State[] mDataset;

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

    ListAdapter() {
        mDataset = new State[0];
    }

    public void setData(Set<State> dataset) {
        mDataset = dataset.toArray(new State[dataset.size()]);
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

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.icao24.setText(mDataset[position].getIcao24());
        holder.callsign.setText(mDataset[position].getCallsign());
        holder.origin_country.setText(mDataset[position].getOriginCountry());
        holder.velocity.setText(Float.toString(mDataset[position].getVelocity()));
    }

    @Override
    public int getItemCount() {
        return mDataset.length;
    }
}
