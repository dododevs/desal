package revolver.desal.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;

import androidx.annotation.ColorInt;
import androidx.annotation.DrawableRes;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import revolver.desal.R;
import revolver.desal.util.ui.M;
import revolver.desal.util.ui.ViewUtils;

public class DeSalToolbarBlob extends LinearLayout {

    private final TextView mTextView;
    private final ImageView mIconView;

    private int mPrimaryColor = Color.WHITE;
    private int mAccentColor = Color.BLACK;

    public DeSalToolbarBlob(Context context) {
        this(context, null);
    }

    public DeSalToolbarBlob(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DeSalToolbarBlob(Context context, AttributeSet attrs, int defAttrStyle) {
        super(context, attrs, defAttrStyle);

        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DeSalToolbarBlob);
        mPrimaryColor = a.getColor(R.styleable.DeSalToolbarBlob_blobPrimaryColor, mPrimaryColor);
        mAccentColor = a.getColor(R.styleable.DeSalToolbarBlob_blobAccentColor, mAccentColor);

        int padding = M.dp(4.f).intValue();
        setBackgroundResource(R.drawable.blob_background);
        getBackground().setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);

        setPadding(padding, padding, padding, padding);

        mTextView = new TextView(getContext());
        mTextView.setTextColor(mAccentColor);
        mTextView.setTypeface(Typeface.DEFAULT_BOLD);
        mTextView.setTextSize(12.8f);
        mTextView.setText(a.getText(R.styleable.DeSalToolbarBlob_blobText));

        mIconView = new ImageView(getContext());
        mIconView.setColorFilter(mAccentColor);
        mIconView.setImageDrawable(a.getDrawable(R.styleable.DeSalToolbarBlob_blobIcon));

        addView(mIconView, ViewUtils.newLayoutParams(LinearLayout.LayoutParams.class)
                .width(16.f).height(16.f).gravity(Gravity.CENTER_VERTICAL).get());
        addView(mTextView, ViewUtils.newLayoutParams(LinearLayout.LayoutParams.class)
                .wrapContentInWidth().wrapContentInHeight().weight(0.8f).startMargin(2.f)
                    .gravity(Gravity.CENTER_VERTICAL).get());

        a.recycle();
    }

    public void setText(String text) {
        mTextView.setText(text);
    }

    public void setIcon(@DrawableRes int icon) {
        mIconView.setImageResource(icon);
    }

    public void setPrimaryColor(@ColorInt int color) {
        mPrimaryColor = color;
        getBackground().mutate().setColorFilter(mPrimaryColor, PorterDuff.Mode.SRC_ATOP);
    }

    public void setAccentColor(@ColorInt int color) {
        mAccentColor = color;
        mTextView.setTextColor(mAccentColor);
        mIconView.setColorFilter(mAccentColor);
    }

}
