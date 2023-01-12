package it.stazionidesal.desal.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.stations.Fuel;
import it.stazionidesal.desal.api.services.stations.GasPump;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class OwnerNewStationPumpsAdapter extends RecyclerView.Adapter<OwnerNewStationPumpsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mDisplayView;
        final LinearLayout mFuelsContainer;
        final ImageView mTypeView;
        final ImageView mHandleView;
        final ImageView mRemoveView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().verticalMargin(8.f).get());

            mDisplayView = v.findViewById(R.id.item_new_station_pump_display);
            mFuelsContainer = v.findViewById(R.id.item_new_station_pump_fuels_container);
            mTypeView = v.findViewById(R.id.item_new_station_pump_type);
            mHandleView = v.findViewById(R.id.item_new_station_pump_handle);
            mRemoveView = v.findViewById(R.id.item_new_station_pump_remove);
        }
    }

    private Context mContext;
    private final OnItemStartDragListener mDragListener;
    private final OnItemRemovedListener mRemoveListener;
    private final List<GasPump> mPumps;

    public OwnerNewStationPumpsAdapter(List<GasPump> pumps,
                                       OnItemStartDragListener dragListener,
                                       OnItemRemovedListener removedListener) {
        mPumps = pumps;
        mDragListener = dragListener;
        mRemoveListener = removedListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_new_station_pump, null));
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final GasPump pump = mPumps.get(viewHolder.getAdapterPosition());
        switch (pump.getType()) {
            case SELF:
                viewHolder.mTypeView.setImageResource(R.drawable.ic_local_gas_station);
                break;
            case PATP:
                viewHolder.mTypeView.setImageResource(R.drawable.ic_patp);
                break;
        }

        viewHolder.mDisplayView.setText(
                String.format(Locale.ITALIAN, "#%s", pump.getDisplay()).substring(0, 2));

        viewHolder.mFuelsContainer.removeAllViews();
        viewHolder.mFuelsContainer.addView(generateFuelBadge(pump.getAvailableFuel()),
                ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                        .width(32.f).height(32.f)
                            .horizontalMargin(4.f).verticalMargin(2.f).get());

        viewHolder.mRemoveView.setOnClickListener(v -> {
            if (mRemoveListener != null) {
                mRemoveListener.onPumpRemoved(mPumps.get(viewHolder.getAdapterPosition()));
                notifyItemRemoved(viewHolder.getAdapterPosition());
            }
        });
        viewHolder.mHandleView.setOnTouchListener((v, event) -> {
            if (event.getActionMasked() == MotionEvent.ACTION_DOWN) {
                if (mDragListener != null) {
                    mDragListener.onStartDrag(viewHolder);
                }
            }
            return true;
        });
    }

    @Override
    public int getItemCount() {
        return mPumps.size();
    }

    private View generateFuelBadge(Fuel fuel) {
        final TextView badge = (TextView) View.inflate(mContext, R.layout.badge_fuel, null);
        badge.setText(mContext.getString(fuel.getStringResource()).substring(0, 1));
        switch (fuel) {
            case GPL:
                badge.setBackgroundResource(R.drawable.badge_gpl);
                break;
            case UNLEADED:
                badge.setBackgroundResource(R.drawable.badge_unleaded);
                break;
            case DIESEL:
            default:
                badge.setBackgroundResource(R.drawable.badge_diesel);
                break;
        }
        return badge;
    }

    public interface OnItemStartDragListener {
        void onStartDrag(RecyclerView.ViewHolder viewHolder);
    }

    public interface OnItemRemovedListener {
        void onPumpRemoved(final GasPump pump);
    }
}
