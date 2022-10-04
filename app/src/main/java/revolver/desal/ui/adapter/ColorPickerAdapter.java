package revolver.desal.ui.adapter;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import revolver.desal.R;
import revolver.desal.util.ui.ViewUtils;

public class ColorPickerAdapter extends RecyclerView.Adapter<ColorPickerAdapter.ViewHolder> {

    static class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView mColorView;

        ViewHolder(View v) {
            super(v);

            v.setLayoutParams(ViewUtils.newLayoutParams(ViewGroup.MarginLayoutParams.class)
                    .matchParentInWidth().wrapContentInHeight()
                        .verticalMargin(16.f).horizontalMargin(16.f).get());

            mColorView = v.findViewById(R.id.item_color_icon);
        }
    }

    private final Context mContext;
    private final int[] mColors;

    private int mSelectedColorIndex = 0;
    private OnColorSelectedListener mListener;

    public ColorPickerAdapter(Context context, @Nullable int[] colors) {
        mContext = context;
        if (colors == null || colors.length < 1) {
            colors = generateDefaultColors();
        }
        mColors = colors;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ViewHolder(View.inflate(mContext, R.layout.item_color, null));
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder viewHolder, int i) {
        viewHolder.mColorView.setColorFilter(mColors[i]);
        viewHolder.mColorView.setOnClickListener(v -> {
            mSelectedColorIndex = viewHolder.getAdapterPosition();
            if (mListener != null) {
                mListener.onColorSelected(mColors[viewHolder.getAdapterPosition()]);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColors.length;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        mListener = listener;
    }

    private int[] generateDefaultColors() {
        return new int[] {
                ContextCompat.getColor(mContext, R.color.bittersweetRed),
                ContextCompat.getColor(mContext, R.color.coralRed),
                ContextCompat.getColor(mContext, R.color.mikadoYellow),
                ContextCompat.getColor(mContext, R.color.pastelGreen),
                ContextCompat.getColor(mContext, R.color.lightBlue),
                ContextCompat.getColor(mContext, R.color.darkBlue),
                ContextCompat.getColor(mContext, R.color.liverGray),
                ContextCompat.getColor(mContext, R.color.midGray),
                ContextCompat.getColor(mContext, android.R.color.black)
        };
    }

    public interface OnColorSelectedListener {
        void onColorSelected(int color);
    }

}
