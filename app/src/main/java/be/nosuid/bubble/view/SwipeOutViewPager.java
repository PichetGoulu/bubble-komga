package be.nosuid.bubble.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;

import androidx.viewpager.widget.ViewPager;

public class SwipeOutViewPager extends ViewPager {
    private float mStartX = 0;
    private OnSwipeOutListener mSwipeOutListener;

    public interface OnSwipeOutListener {
        void onSwipeOutAtStart();

        void onSwipeOutAtEnd();
    }

    public SwipeOutViewPager(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public SwipeOutViewPager(Context context) {
        super(context);
    }

    public void setOnSwipeOutListener(OnSwipeOutListener listener) {
        mSwipeOutListener = listener;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_UP) {
            float diff = ev.getX() - mStartX;

            if (diff > 0 && getCurrentItem() == 0) {
                if (mSwipeOutListener != null)
                    mSwipeOutListener.onSwipeOutAtStart();
            } else if (diff < 0 && getCurrentItem() == (getAdapter().getCount() - 1)) {
                if (mSwipeOutListener != null)
                    mSwipeOutListener.onSwipeOutAtEnd();
            }
        }
        return super.onTouchEvent(ev);
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        if (ev.getAction() == MotionEvent.ACTION_DOWN) {
            mStartX = ev.getX();
        }

        return super.onInterceptTouchEvent(ev);
    }
}
