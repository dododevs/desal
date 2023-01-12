package it.stazionidesal.desal.view;

import android.content.Context;
import android.content.res.TypedArray;
import androidx.appcompat.widget.AppCompatTextView;
import android.util.AttributeSet;

import it.stazionidesal.desal.DeSal;
import it.stazionidesal.desal.R;

public class MontserratTextView extends AppCompatTextView {

    private static final int
            BLACK       = 0,
            BOLD        = 1,
            EXTRA_BOLD  = 2,
            LIGHT       = 3,
            MEDIUM      = 4,
            REGULAR     = 5,
            SEMI_BOLD   = 6,
            THIN        = 7;

    public MontserratTextView(Context context) {
        super(context);
    }

    public MontserratTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initializeTypeface(attrs);
    }

    public MontserratTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initializeTypeface(attrs);
    }

    private void initializeTypeface(AttributeSet attributeSet) {
        if (attributeSet == null) {
            setTypeface(DeSal.Fonts.Montserrat.Regular);
        } else {
            final TypedArray a = getContext().obtainStyledAttributes(attributeSet, R.styleable.MontserratTextView);
            switch (a.getInt(R.styleable.MontserratTextView_textStyle, 0)) {
                case BLACK:
                    setTypeface(DeSal.Fonts.Montserrat.Black);
                    break;
                case BOLD:
                    setTypeface(DeSal.Fonts.Montserrat.Bold);
                    break;
                case EXTRA_BOLD:
                    setTypeface(DeSal.Fonts.Montserrat.ExtraBold);
                    break;
                case LIGHT:
                    setTypeface(DeSal.Fonts.Montserrat.Light);
                    break;
                case MEDIUM:
                    setTypeface(DeSal.Fonts.Montserrat.Medium);
                    break;
                default:
                case REGULAR:
                    setTypeface(DeSal.Fonts.Montserrat.Regular);
                    break;
                case SEMI_BOLD:
                    setTypeface(DeSal.Fonts.Montserrat.SemiBold);
                    break;
                case THIN:
                    setTypeface(DeSal.Fonts.Montserrat.Thin);
                    break;
            }
            a.recycle();
        }
    }

}
