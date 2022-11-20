package revolver.desal.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import revolver.desal.R;
import revolver.desal.api.services.shifts.Shift;
import revolver.desal.util.ui.ViewUtils;

public class ShiftsArchiveAdapter extends RecyclerView.Adapter<ShiftsArchiveAdapter.ViewHolder> {

    public static final SimpleDateFormat sDateFormatter =
            new SimpleDateFormat("EEEE dd MMMM", Locale.ITALIAN);
    public static final SimpleDateFormat sHourFormatter =
            new SimpleDateFormat("HH:mm", Locale.ITALIAN);

    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mTitleView;
        final TextView mHoursView;
        final TextView mDetailsView;
        final ImageView mIconView;

        final ImageView mRevisionIconView;
        final TextView mRevisionStateView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().verticalMargin(4.f).get());

            mTitleView = v.findViewById(R.id.item_shifts_archive_title);
            mHoursView = v.findViewById(R.id.item_shifts_archive_hours);
            mDetailsView = v.findViewById(R.id.item_shifts_archive_details);
            mIconView = v.findViewById(R.id.item_shifts_archive_icon);

            mRevisionIconView = v.findViewById(R.id.item_shifts_archive_revision_icon);
            mRevisionStateView = v.findViewById(R.id.item_shifts_archive_revision_state);
        }
    }

    private Context mContext;
    private List<Shift> mShifts;

    private OnShiftClickedListener mClickListener;
    private OnShiftLongClickedListener mLongClickListener;

    public ShiftsArchiveAdapter(List<Shift> shifts) {
        mShifts = shifts;
        sortShifts();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        return new ViewHolder(View.inflate(mContext, R.layout.item_shifts_archive, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Shift shift = mShifts.get(i);

        final String formattedDate = sDateFormatter.format(
                new Date(shift.getStart() * 1000));
        String title = mContext.getString(R.string.item_shifts_archive_title, formattedDate);

        final String formattedStartTime = sHourFormatter.format(
                new Date(shift.getStart() * 1000));
        final String formattedEndTime = sHourFormatter.format(
                new Date(shift.getEnd() * 1000));
        String hours = mContext.getString(
                R.string.item_shifts_archive_hours, formattedStartTime, formattedEndTime);

        String details;
        int transactionsCount = shift.getDifferenceData().getTransactions().size();
        if (transactionsCount == 0) {
            details = mContext.getString(R.string.item_shifts_archive_details_no_transactions);
        } else if (transactionsCount == 1) {
            details = mContext.getString(R.string.item_shifts_archive_details_transaction);
        } else {
            details = mContext.getString(R.string.item_shifts_archive_details_transactions, transactionsCount);
        }

        viewHolder.mTitleView.setText(title);
        viewHolder.mHoursView.setText(hours);
        viewHolder.mDetailsView.setText(details);

        viewHolder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onShiftClicked(shift);
            }
        });
        viewHolder.itemView.setOnLongClickListener(v -> {
            if (mLongClickListener != null) {
                mLongClickListener.onShiftLongClicked(shift);
            }
            return true;
        });

        if (shift.getRevision() == null) {
            viewHolder.mRevisionStateView.setText(R.string.item_shifts_archive_not_revised);
            viewHolder.mRevisionIconView.setImageResource(R.drawable.ic_revision);
        } else {
            viewHolder.mRevisionStateView.setText(R.string.item_shifts_archive_revised);
            viewHolder.mRevisionIconView.setImageResource(R.drawable.ic_done);
        }
    }

    @Override
    public int getItemCount() {
        return mShifts.size();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateShifts(List<Shift> shifts) {
        mShifts = shifts;
        sortShifts();
        notifyDataSetChanged();
    }

    private void sortShifts() {
        Collections.sort(mShifts, (s1, s2) -> (int) Math.signum(s2.getStart() - s1.getStart()));
    }

    public void setOnShiftClickedListener(OnShiftClickedListener listener) {
        mClickListener = listener;
    }

    public void setOnShiftLongClickedListener(OnShiftLongClickedListener listener) {
        mLongClickListener = listener;
    }

    public interface OnShiftClickedListener {
        void onShiftClicked(final Shift shift);
    }

    public interface OnShiftLongClickedListener {
        void onShiftLongClicked(final Shift shift);
    }
}
