package be.nosuid.bubble.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatSeekBar;

public class ComicSeekBar extends AppCompatSeekBar {
    private boolean mProgressToRight = false;

    public ComicSeekBar(Context context) {
        super(context);
    }

    public ComicSeekBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ComicSeekBar(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void setProgressToRight(boolean toRight) {
        mProgressToRight = toRight;

        // Force redraw the progress bar
        onSizeChanged(getWidth(), getHeight(), 0, 0);
    }

    public void setMaxPageCount(int pageCount) {
        // Progress is from 0 to (pageCount -1)
        setMax(pageCount - 1);
    }

    public void setPageProgress(int pageNum) {
        // Progress is from 0 to (pageCount -1)
        setProgress(pageNum - 1);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mProgressToRight) {
            float px = this.getWidth() / 2.0f;
            float py = this.getHeight() / 2.0f;

            canvas.scale(-1, 1, px, py);
        }
        super.onDraw(canvas);
    }
}
