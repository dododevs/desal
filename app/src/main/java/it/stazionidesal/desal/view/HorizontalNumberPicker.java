package it.stazionidesal.desal.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.Typeface;
import androidx.annotation.ColorInt;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import it.stazionidesal.desal.R;
import it.stazionidesal.desal.util.ui.ColorUtils;
import it.stazionidesal.desal.util.ui.IconUtils;
import it.stazionidesal.desal.util.ui.M;
import it.stazionidesal.desal.util.ui.ViewUtils;

public class HorizontalNumberPicker extends LinearLayout {

    private int mMin, mMax;
    private int mColor;

    private int mValue;

    private TextView mCurrent;
    private ImageView mMinusButton;
    private ImageView mPlusButton;

    private OnValueChangeListener mOnValueChangeListener;

    public HorizontalNumberPicker(Context context) {
        this(context, null);
    }

    public HorizontalNumberPicker(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HorizontalNumberPicker(Context context, AttributeSet attrs, int defAttrStyle) {
        super(context, attrs, defAttrStyle);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.HorizontalNumberPicker);
        setMinimumValue(a.getInt(R.styleable.HorizontalNumberPicker_minimumValue, 0));
        setMaximumValue(a.getInt(R.styleable.HorizontalNumberPicker_maximumValue, 1000));
        setColor(a.getColor(R.styleable.HorizontalNumberPicker_buttonDrawableColor, Color.BLACK));

        a.recycle();
        initialize();
    }

    public void setMinimumValue(int min) {
        mMin = min;
        mValue = min;
    }

    public void setMaximumValue(int max) {
        mMax = max;
    }

    public void setColor(@ColorInt int color) {
        mColor = color;
    }

    private void initialize() {
        setOrientation(HORIZONTAL);
        setGravity(Gravity.CENTER_VERTICAL);

        int padding = M.dp(4.f).intValue();

        mPlusButton = new ImageView(getContext());
        mPlusButton.setPadding(padding, padding, padding, padding);
        mPlusButton.setBackground(ViewUtils.getSelectableBackgroundDrawable(getContext()));
        mPlusButton.setFocusable(true);
        mPlusButton.setClickable(true);
        mPlusButton.setImageDrawable(ColorUtils.dyeDrawable(
                IconUtils.drawable(getContext(), R.drawable.ic_add), mColor));
        mPlusButton.setLayoutParams(ViewUtils.newLayoutParams(LinearLayout.LayoutParams.class)
                .width(0.f).height(32.f).weight(1.f).gravity(Gravity.CENTER_VERTICAL).get());

        mMinusButton = new ImageView(getContext());
        mMinusButton.setPadding(padding, padding, padding, padding);
        mMinusButton.setBackground(ViewUtils.getSelectableBackgroundDrawable(getContext()));
        mMinusButton.setFocusable(true);
        mMinusButton.setClickable(true);
        mMinusButton.setImageDrawable(ColorUtils.dyeDrawable(
                IconUtils.drawable(getContext(), R.drawable.ic_remove), mColor));
        mMinusButton.setLayoutParams(ViewUtils.newLayoutParams(LinearLayout.LayoutParams.class)
                .width(0.f).height(32.f).weight(1.f).gravity(Gravity.CENTER_VERTICAL).get());

        mCurrent = new TextView(getContext());
        mCurrent.setLayoutParams(ViewUtils.newLayoutParams(LinearLayout.LayoutParams.class)
                .width(0.f).wrapContentInHeight().weight(1.f)
                    .gravity(Gravity.CENTER_VERTICAL).horizontalMargin(4.f).get());
        mCurrent.setGravity(Gravity.CENTER);
        mCurrent.setTypeface(Typeface.DEFAULT_BOLD);
        mCurrent.setTextColor(Color.BLACK);
        mCurrent.setTextSize(20.f);
        mCurrent.setText(String.valueOf(mValue));

        mPlusButton.setOnClickListener(v -> {
            final int oldValue = mValue;
            if (mValue + 1 < mMax) {
                mValue++;
                mCurrent.setText(String.valueOf(mValue));
            } else if (mValue + 1 == mMax) {
                mValue++;
                mCurrent.setText(String.valueOf(mValue));
                mPlusButton.setEnabled(false);
            }

            if (!mMinusButton.isEnabled()) {
                mMinusButton.setEnabled(true);
            }

            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChange(
                        HorizontalNumberPicker.this, oldValue, mValue);
            }
        });

        mMinusButton.setOnClickListener(v -> {
            final int oldValue = mValue;
            if (mValue - 1 > mMin) {
                mValue--;
                mCurrent.setText(String.valueOf(mValue));
            } else if (mValue - 1 == mMin) {
                mValue--;
                mCurrent.setText(String.valueOf(mValue));
                mMinusButton.setEnabled(false);
            }

            if (!mPlusButton.isEnabled()) {
                mPlusButton.setEnabled(true);
            }

            if (mOnValueChangeListener != null) {
                mOnValueChangeListener.onValueChange(
                        HorizontalNumberPicker.this, oldValue, mValue);
            }
        });

        addView(mMinusButton);
        addView(mCurrent);
        addView(mPlusButton);
    }

    public int getValue() {
        return mValue;
    }

    public void setValue(int value) {
        mValue = Math.max(Math.min(value, mMax), mMin);
        mCurrent.setText(String.valueOf(mValue));

        if (mValue == mMax) {
            mPlusButton.setEnabled(false);
        } else if (mValue == mMin) {
            mMinusButton.setEnabled(false);
        }
    }

    public void setOnValueChangedListener(OnValueChangeListener listener) {
        mOnValueChangeListener = listener;
    }

    public interface OnValueChangeListener {
        void onValueChange(HorizontalNumberPicker picker, int oldVal, int newVal);
    }

}
