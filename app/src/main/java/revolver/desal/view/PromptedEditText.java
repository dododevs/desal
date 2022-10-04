package revolver.desal.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.textfield.TextInputEditText;
import android.text.TextPaint;
import android.util.AttributeSet;

import revolver.desal.R;
import revolver.desal.util.ui.M;

public class PromptedEditText extends TextInputEditText {

    private TextDrawable mTextDrawable;

    public PromptedEditText(Context context) {
        super(context);
    }

    public PromptedEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize(attrs);
    }

    public PromptedEditText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize(attrs);
    }

    public int getPromptWidth() {
        return mTextDrawable.getTextWidth();
    }

    private void initialize(AttributeSet attrs) {
        if (attrs == null) {
            return;
        }

        final TypedArray a = getContext().obtainStyledAttributes(
                attrs, R.styleable.PromptedEditText);
        final String prompt = a.getString(R.styleable.PromptedEditText_promptText);

        mTextDrawable = new TextDrawable(prompt, getTextSize(), Color.GRAY);
        a.recycle();
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);

        mTextDrawable.setTextY(getHeight() - getLineBounds(0, null) - getPaddingBottom() + M.dp(1.5f).intValue());
        setCompoundDrawablesWithIntrinsicBounds(mTextDrawable, null, null, null);
        setCompoundDrawablePadding(mTextDrawable.getTextWidth() + M.dp(6.f).intValue());
    }

    public static class TextDrawable extends Drawable {

        private final String mText;
        private final Paint mPaint;
        private final int mTextWidth;
        private int mTextY;

        TextDrawable(String text, float textSize, @ColorInt int color) {
            mText = text;
            mPaint = new TextPaint();
            mPaint.setTextSize(textSize);
            mPaint.setAntiAlias(true);
            mPaint.setColor(color);
            mPaint.setTextAlign(Paint.Align.LEFT);
            mPaint.setTypeface(Typeface.DEFAULT_BOLD);

            mTextWidth = (int) mPaint.measureText(text);
            setBounds(0, 0, mTextWidth, (int) textSize);
        }

        int getTextWidth() {
            return mTextWidth;
        }

        void setTextY(int textY) {
            mTextY = textY;
        }

        @Override
        public void draw(@NonNull Canvas canvas) {
            canvas.drawText(mText, 0, mTextY, mPaint);
        }

        @Override
        public void setAlpha(int alpha) {
            mPaint.setAlpha(alpha);
        }

        @Override
        public void setColorFilter(@Nullable ColorFilter colorFilter) {
            mPaint.setColorFilter(colorFilter);
        }

        @Override
        public int getOpacity() {
            return PixelFormat.OPAQUE;
        }
    }

}
