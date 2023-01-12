package it.stazionidesal.desal.ui.adapter;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.api.services.inventory.Item;
import it.stazionidesal.desal.api.services.inventory.Unit;
import it.stazionidesal.desal.ui.fragment.employee.EmployeeInventoryFragment;
import it.stazionidesal.desal.util.ui.TextUtils;
import it.stazionidesal.desal.util.ui.ViewUtils;

@SuppressLint("NotifyDataSetChanged")
public class InventoryItemsAdapter extends RecyclerView.Adapter<InventoryItemsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView mNameView;
        final TextView mDescriptionView;
        final TextView mPriceView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight().horizontalMargin(8.f).get());

            mNameView = v.findViewById(R.id.item_shop_name);
            mDescriptionView = v.findViewById(R.id.item_shop_description);
            mPriceView = v.findViewById(R.id.item_shop_price);
        }
    }

    private List<Item> mCompleteDataSet;
    private List<Item> mItems;

    private EmployeeInventoryFragment.Filter mFilter;

    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public InventoryItemsAdapter(List<Item> items) {
        mCompleteDataSet = items;
        mItems = new ArrayList<>(mCompleteDataSet);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_shop, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Item item = mItems.get(i);
        final String name = TextUtils.capitalizeFirst(item.getName());
        viewHolder.mNameView.setText(name);
        viewHolder.mPriceView.setText(String.format(Locale.ITALIAN, "€%.2f", item.getPrice()));
        viewHolder.mDescriptionView.setText(item.getDescription());
        viewHolder.itemView.setOnClickListener(v -> {
            if (mClickListener != null) {
                mClickListener.onItemClicked(v, mItems.get(viewHolder.getAdapterPosition()));
            }
        });
        viewHolder.itemView.setOnLongClickListener(v -> {
            if (mLongClickListener != null) {
                mLongClickListener.onItemLongClicked(v, mItems.get(viewHolder.getAdapterPosition()));
            }
            return true;
        });
        if (item.getUnit() == Unit.LITERS) {
            viewHolder.mDescriptionView.setText(R.string.item_shop_oil);
        }
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateItems(List<Item> items) {
        mCompleteDataSet = items;
        mItems = new ArrayList<>(mCompleteDataSet);
        notifyDataSetChanged();

        if (mFilter != null) {
            applyFilter(mFilter);
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public void applyAndStoreFilter(EmployeeInventoryFragment.Filter filter) {
        mFilter = filter;
        applyFilter(mFilter);
    }

    private void applyFilter(EmployeeInventoryFragment.Filter filter) {
        final List<Item> currentDataSet = new ArrayList<>(mItems);
        mItems = new ArrayList<>(mCompleteDataSet);
        for (int i = mItems.size() - 1; i >= 0; i--) {
            final Item item = mItems.get(i);
            if (!currentDataSet.contains(item)){
                notifyItemChanged(i);
            }
            if (!filter.include(mItems.get(i))) {
                mItems.remove(i);
                notifyItemRemoved(i);
            }
        }
    }

    public interface OnItemClickListener {
        void onItemClicked(View v, Item item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(View v, Item item);
    }
}
