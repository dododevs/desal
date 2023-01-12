package it.stazionidesal.desal.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.List;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.GasStation;
import it.stazionidesal.desal.util.ui.TextUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class CollapsedStationsListAdapter extends RecyclerView.Adapter<CollapsedStationsListAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams()
                    .matchParentInWidth().wrapContentInHeight().get());

            mNameView = v.findViewById(R.id.item_station_expanded_name);
        }
    }

    private List<GasStation> mStations;
    private GasStation mSelectedStation;
    private OnStationSelectedListener mListener;

    public CollapsedStationsListAdapter(List<GasStation> stations) {
        mStations = stations;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_station_collapsed, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final String name = TextUtils.capitalizeWords(mStations.get(i).getName());
        viewHolder.mNameView.setText(name);

        viewHolder.itemView.setOnClickListener(v -> {
            mSelectedStation = mStations.get(viewHolder.getAdapterPosition());
            if (mListener != null) {
                mListener.onStationSelected(mSelectedStation);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mStations.size();
    }

    public void updateStationsList(List<GasStation> stations) {
        mStations = stations;
        notifyDataSetChanged();
    }

    public void setOnStationSelectedListener(OnStationSelectedListener l) {
        mListener = l;
    }

    public interface OnStationSelectedListener {
        void onStationSelected(GasStation s);
    }
}
