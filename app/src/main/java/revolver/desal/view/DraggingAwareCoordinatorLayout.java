package revolver.desal.view;

import android.annotation.SuppressLint;
import android.content.Context;

import androidx.coordinatorlayout.widget.CoordinatorLayout;

import android.util.AttributeSet;
import android.view.MotionEvent;

@SuppressLint("ClickableViewAccessibility")
public class DraggingAwareCoordinatorLayout extends CoordinatorLayout {

    private float mDragX;
    private float mDeltaX;

    private DragCallback mCallback;

    public static final class Direction {
        public static final int START_TO_END = 1;
        public static final int END_TO_START = -1;
        static final int UNDEFINED = 0;
    }
    private int mDirection = Direction.UNDEFINED;

    public DraggingAwareCoordinatorLayout(Context context) {
        super(context);
    }

    public DraggingAwareCoordinatorLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DraggingAwareCoordinatorLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void setDragCallback(final DragCallback callback) {
        mCallback = callback;
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getActionMasked() == MotionEvent.ACTION_DOWN && ev.getPointerCount() == 1) {
            mDragX = ev.getRawX();
            mDeltaX = 0;
            mDirection = Direction.UNDEFINED;
            return true;
        } else if (ev.getActionMasked() == MotionEvent.ACTION_MOVE && ev.getPointerCount() == 1) {
            float x = ev.getRawX();
            float dx = x - mDragX;
            if (mDirection == 0) {
                mDirection = dx >= 0 ? Direction.START_TO_END : Direction.END_TO_START;
                if (mCallback != null) {
                    mCallback.onDragStart(mDirection);
                }
            }
            if (dx * mDirection < 0) {
                mDirection = -mDirection;
                if (mCallback != null) {
                    mCallback.onDragStart(mDirection);
                }
            } else {
                mDeltaX += dx;
                if (mCallback != null) {
                    mCallback.onDrag(mDirection, Math.abs(mDeltaX / getWidth()));
                }
            }
            mDragX = x;
        } else if (ev.getActionMasked() == MotionEvent.ACTION_UP) {
            if (mCallback != null) {
                mCallback.onDragCanceled();
            }
        }
        return true;
    }

    public interface DragCallback {
        void onDragStart(int direction);
        void onDrag(int direction, float progress);
        void onDragCanceled();
    }
}
