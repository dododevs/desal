package it.stazionidesal.desal.ui.adapter;

import android.app.Activity;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.text.format.DateUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.ui.activity.owner.OwnerStationActivity;
import it.stazionidesal.desal.util.ui.TextUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class ExpandedStationsListAdapter extends RecyclerView.Adapter<ExpandedStationsListAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameView;
        final TextView mLastActivityView;
        final ImageView mIconView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().verticalMargin(4.f).get());

            mNameView = v.findViewById(R.id.item_station_expanded_name);
            mLastActivityView = v.findViewById(R.id.item_station_expanded_last_activity);
            mIconView = v.findViewById(R.id.item_station_expanded_icon);
        }
    }

    private final Activity mHost;
    private List<GasStation> mStations;

    public ExpandedStationsListAdapter(Activity host, List<GasStation> stations) {
        mHost = host;
        mStations = stations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(mHost, R.layout.item_station_expanded, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final GasStation station = mStations.get(i);
        viewHolder.mNameView.setText(TextUtils.capitalizeWords(station.getName()));
        if (getHoursSinceLastActivity(station.getLastActivity() * 1000) < 1L) {
            viewHolder.mLastActivityView.setText(
                    mHost.getString(R.string.item_station_expanded_last_activity,
                            mHost.getString(R.string.item_station_expanded_last_activity_less_than_an_hour_ago)));
        } else {
            viewHolder.mLastActivityView.setText(
                    mHost.getString(R.string.item_station_expanded_last_activity,
                            DateUtils.getRelativeTimeSpanString(station.getLastActivity() * 1000,
                                System.currentTimeMillis(), DateUtils.HOUR_IN_MILLIS)).toLowerCase());
        }
        viewHolder.itemView.setOnClickListener(v -> mHost.startActivity(new Intent(mHost, OwnerStationActivity.class)
                        .putExtra("station", mStations.get(viewHolder.getAdapterPosition()))));
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }

    private long getHoursSinceLastActivity(long time) {
        return (System.currentTimeMillis() - time) / 1000 / 60 / 60;
    }

    public void updateStationsList(List<GasStation> stations) {
        mStations = stations;
        notifyDataSetChanged();
    }
}
