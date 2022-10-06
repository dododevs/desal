package revolver.desal.ui.adapter;

import android.content.res.ColorStateList;
import android.graphics.Color;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;
import java.util.Locale;

import revolver.desal.R;
import revolver.desal.api.services.inventory.Item;
import revolver.desal.util.ui.TextUtils;
import revolver.desal.util.ui.ViewUtils;

public class OilItemsAdapter extends RecyclerView.Adapter<OilItemsAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView mIconView;
        final TextView mNameView;
        final TextView mPriceView;
        final LinearLayout mContainer;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight()
                        .horizontalMargin(16.f).verticalMargin(16.f).get());

            mIconView = v.findViewById(R.id.item_oil_icon);
            mNameView = v.findViewById(R.id.item_oil_name);
            mPriceView = v.findViewById(R.id.item_oil_price);
            mContainer = v.findViewById(R.id.item_oil_container);
        }
    }

    private List<Item> mItems;

    private OnItemClickListener mClickListener;
    private OnItemLongClickListener mLongClickListener;

    public OilItemsAdapter(List<Item> items) {
        mItems = items;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(viewGroup.getContext(), R.layout.item_oil, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        final Item item = mItems.get(i);
        final int themeColor = Color.parseColor(item.getDescription());
        viewHolder.mIconView.setImageTintList(ColorStateList.valueOf(themeColor));
        viewHolder.mContainer.setBackgroundColor(themeColor);
        viewHolder.mNameView.setText(TextUtils.capitalizeFirst(item.getName()));
        viewHolder.mPriceView.setText(String.format(Locale.ITALIAN, "%.2f €/L", item.getPrice()));
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
    }

    @Override
    public int getItemCount() {
        return mItems.size();
    }

    public void updateItems(List<Item> items) {
        mItems = items;
        notifyDataSetChanged();
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        mClickListener = listener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener listener) {
        mLongClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClicked(View v, Item item);
    }

    public interface OnItemLongClickListener {
        void onItemLongClicked(View v, Item item);
    }
}
