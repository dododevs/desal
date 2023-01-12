package it.stazionidesal.desal.view;

import android.content.Context;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.LayerDrawable;
import androidx.appcompat.widget.SwitchCompat;
import android.util.AttributeSet;

import it.stazionidesal.desal.R;

public class TransactionDirectionSwitchView extends SwitchCompat {

    private LayerDrawable mThumbDrawable;
    private boolean mIsOutbound = true;

    public TransactionDirectionSwitchView(Context context) {
        super(context);
        initialize();
    }

    public TransactionDirectionSwitchView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initialize();
    }

    public TransactionDirectionSwitchView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initialize();
    }

    private void initialize() {
        setThumbResource(R.drawable.ic_transaction_direction_outbound_thumb);
        mThumbDrawable = (LayerDrawable) getThumbDrawable();

        setOnCheckedChangeListener((buttonView, isChecked) -> {
            animateDirectionChange();
            mIsOutbound = !mIsOutbound;
        });
    }

    private void animateDirectionChange() {
        final AnimatedVectorDrawable animated;
        if (mIsOutbound) {
            setThumbResource(R.drawable.ic_transaction_direction_inbound_thumb);
        } else {
            setThumbResource(R.drawable.ic_transaction_direction_outbound_thumb);
        }
        mThumbDrawable = (LayerDrawable) getThumbDrawable();
        animated = (AnimatedVectorDrawable) mThumbDrawable.getDrawable(2);
        animated.start();
    }

    public boolean isOutbound() {
        return mIsOutbound;
    }
}
