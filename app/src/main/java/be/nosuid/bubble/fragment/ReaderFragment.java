package be.nosuid.bubble.fragment;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.util.SparseArray;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import be.nosuid.bubble.MainApplication;
import be.nosuid.bubble.R;
import be.nosuid.bubble.activity.ReaderActivity;
import be.nosuid.bubble.managers.SettingsManager;
import be.nosuid.bubble.managers.SharedResourcesManager;
import be.nosuid.bubble.managers.Utils;
import be.nosuid.bubble.model.Comic;
import be.nosuid.bubble.view.ComicSeekBar;
import be.nosuid.bubble.view.PageImageView;
import be.nosuid.bubble.view.SwipeOutViewPager;
import io.reactivex.disposables.CompositeDisposable;


public class ReaderFragment extends Fragment {
    public static final String PARAM_COMIC = "PARAM_COMIC";
    private static final String STATE_FULLSCREEN = "STATE_FULLSCREEN";

    private SharedResourcesManager mSharedRes;

    private SwipeOutViewPager mViewPager;
    private ComicSeekBar mSeekBar;
    private ComicPagerAdapter mPagerAdapter;

    private LinearLayout mPageNavLayout;
    private TextView mPageNavTextView;

    private GestureDetector mGestureDetector;

    private Comic mComic;
    private int mCurrentPageNum;

    private boolean mIsFullscreen;

    private CompositeDisposable mCompositeDisposable;

    public static ReaderFragment create(Comic comic) {
        ReaderFragment fragment = new ReaderFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_COMIC, comic);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedRes = ((MainApplication) getActivity().getApplication()).getSharedResourcesManager();

        Bundle bundle = getArguments();
        mComic = (Comic) bundle.getSerializable(PARAM_COMIC);

        mCompositeDisposable = new CompositeDisposable();

        mPagerAdapter = new ComicPagerAdapter(mComic.getPagesCount());
        mGestureDetector = new GestureDetector(getActivity(), new FingerTapListener());

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_reader, container, false);

        mPageNavLayout = getActivity().findViewById(R.id.pageNavLayout);

        mSeekBar = mPageNavLayout.findViewById(R.id.pageSeekBar);
        mSeekBar.setMaxPageCount(mComic.getPagesCount());
        mSeekBar.setProgressToRight(!mSharedRes.getSettingsManager().getIsReadingLeftToRight());
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    setCurrentPage(linearIdxToPage(progress));
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                mSharedRes.getPicasso().pauseTag(getActivity());
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mSharedRes.getPicasso().resumeTag(getActivity());
            }
        });

        mPageNavTextView = mPageNavLayout.findViewById(R.id.pageNavTextView);

        mViewPager = view.findViewById(R.id.viewPager);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);
        mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return mGestureDetector.onTouchEvent(event);
            }
        });
        mViewPager.addOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                setCurrentPage(linearIdxToPage(position));
            }
        });
        mViewPager.setOnSwipeOutListener(new SwipeOutViewPager.OnSwipeOutListener() {
            @Override
            public void onSwipeOutAtStart() {
                if (mSharedRes.getSettingsManager().getIsReadingLeftToRight()) {
                    toPreviousComic();
                } else {
                    toNextComic();
                }
            }

            @Override
            public void onSwipeOutAtEnd() {
                if (mSharedRes.getSettingsManager().getIsReadingLeftToRight()) {
                    toNextComic();
                } else {
                    toPreviousComic();
                }
            }
        });

        mCurrentPageNum = mComic.getPageLastRead();
        if (mCurrentPageNum == 0) {
            // First time we open this comic
            mCurrentPageNum += 1;
        }
        setCurrentPage(mCurrentPageNum);

        if (savedInstanceState != null) {
            boolean fullscreen = savedInstanceState.getBoolean(STATE_FULLSCREEN);
            setFullscreen(fullscreen);
        } else {
            setFullscreen(true);
        }
        getActivity().setTitle(mComic.getName());

        return view;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(STATE_FULLSCREEN, mIsFullscreen);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onDestroy() {
        mSharedRes.getPicasso().cancelTag(getActivity());
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
        super.onDestroy();
    }

    private int pageToLinearIdx(int pageNum) {
        // Views order in the adapters are always in the LeftToRight order
        if (mSharedRes.getSettingsManager().getIsReadingLeftToRight()) {
            return pageNum - 1;
        } else {
            return mComic.getPagesCount() - pageNum;
        }
    }

    private int linearIdxToPage(int position) {
        // Views order in the adapters are always in the LeftToRight order
        if (mSharedRes.getSettingsManager().getIsReadingLeftToRight()) {
            return position + 1;
        } else {
            return mComic.getPagesCount() - position;
        }
    }

    private void setCurrentPage(int pageNum) {
        setCurrentPage(pageNum, true);
    }

    private void setCurrentPage(int pageNum, boolean animated) {
        mCurrentPageNum = pageNum;
        mViewPager.setCurrentItem(pageToLinearIdx(pageNum), animated);
        mSeekBar.setPageProgress(pageNum);
        mPageNavTextView.setText(String.format("%d/%d", pageNum, mComic.getPagesCount()));

        setLastReadPage(mCurrentPageNum);
    }

    private void setLastReadPage(int pageNum) {
        mSharedRes.getLibraryManager()
                .setComicLastReadPage(mComic.getId(), pageNum)
                .subscribe(new Utils.DefaultCompletableObserver("setLastReadPage", mCompositeDisposable) {
                    @Override
                    public void onComplete() {
                        Log.d(mTag, "onComplete");
                        mComic.setPageLastRead(pageNum);
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.reader, menu);

        switch (mSharedRes.getSettingsManager().getPageViewMode()) {
            case ASPECT_FILL:
                menu.findItem(R.id.view_mode_aspect_fill).setChecked(true);
                break;
            case ASPECT_FIT:
                menu.findItem(R.id.view_mode_aspect_fit).setChecked(true);
                break;
            case FIT_WIDTH:
                menu.findItem(R.id.view_mode_fit_width).setChecked(true);
                break;
        }

        if (mSharedRes.getSettingsManager().getIsReadingLeftToRight()) {
            menu.findItem(R.id.reading_left_to_right).setChecked(true);
        } else {
            menu.findItem(R.id.reading_right_to_left).setChecked(true);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_mode_aspect_fill:
                changePageViewMode(SettingsManager.PageViewMode.ASPECT_FILL, mViewPager);
                break;
            case R.id.view_mode_aspect_fit:
                changePageViewMode(SettingsManager.PageViewMode.ASPECT_FIT, mViewPager);
                break;
            case R.id.view_mode_fit_width:
                changePageViewMode(SettingsManager.PageViewMode.FIT_WIDTH, mViewPager);
                break;

            case R.id.reading_left_to_right:
                changeReadingDirection(true);
                break;
            case R.id.reading_right_to_left:
                changeReadingDirection(false);
                break;
        }

        item.setChecked(true);
        return super.onOptionsItemSelected(item);
    }

    private void changePageViewMode(SettingsManager.PageViewMode mode, ViewGroup parentView) {
        mSharedRes.getSettingsManager().setPageViewMode(mode);

        // Announce the PageViewMode change to the existing PageImageViews
        for (int i = 0; i < parentView.getChildCount(); i++) {
            final View child = parentView.getChildAt(i);
            if (child instanceof ViewGroup) {
                changePageViewMode(mode, (ViewGroup) child);
            } else if (child instanceof PageImageView) {
                PageImageView pageImageView = (PageImageView) child;
                pageImageView.setViewMode(
                        mSharedRes.getSettingsManager().getPageViewMode(),
                        mSharedRes.getSettingsManager().getIsReadingLeftToRight());
            }
        }
    }

    private void changeReadingDirection(boolean isLeftToRight) {
        mSharedRes.getSettingsManager().setIsReadingLeftToRight(isLeftToRight);
        mSeekBar.setProgressToRight(!isLeftToRight);

        setCurrentPage(mCurrentPageNum, false);
    }

    private class ComicPagerAdapter extends PagerAdapter {
        private SparseArray<ComicPageViewTarget> mPageViewTargets;

        public ComicPagerAdapter(int pageCount) {
            mPageViewTargets = new SparseArray<>(pageCount);
        }

        @Override
        public int getItemPosition(Object object) {
            return POSITION_NONE;
        }

        @Override
        public int getCount() {
            return mComic.getPagesCount();
        }

        @Override
        public boolean isViewFromObject(View view, Object o) {
            return view == o;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            final LayoutInflater inflater = (LayoutInflater) getActivity()
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            View view = inflater.inflate(R.layout.fragment_reader_page, container, false);

            PageImageView pageImageView = view.findViewById(R.id.pageImageView);
            pageImageView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return mGestureDetector.onTouchEvent(event);
                }
            });
            pageImageView.setViewMode(
                    mSharedRes.getSettingsManager().getPageViewMode(),
                    mSharedRes.getSettingsManager().getIsReadingLeftToRight());

            container.addView(view);

            ComicPageViewTarget t = new ComicPageViewTarget(view, position);
            loadPageImage(t, linearIdxToPage(position));
            mPageViewTargets.put(position, t);

            return view;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            mSharedRes.getPicasso().cancelRequest(mPageViewTargets.get(position));

            mPageViewTargets.delete(position);

            View view = (View) object;
            container.removeView(view);

            ImageView iv = view.findViewById(R.id.pageImageView);
            Drawable drawable = iv.getDrawable();
            if (drawable instanceof BitmapDrawable) {
                BitmapDrawable bd = (BitmapDrawable) drawable;
                Bitmap bm = bd.getBitmap();
                if (bm != null) {
                    bm.recycle();
                }
            }
        }
    }

    private void loadPageImage(ComicPageViewTarget t, int pageNum) {
        mSharedRes.getPicasso()
                .load(mSharedRes.getLibraryManager().getComicPageUri(mComic.getId(), pageNum))
                .memoryPolicy(MemoryPolicy.NO_STORE)
                .tag(getActivity())
                .into(t);
    }

    private class ComicPageViewTarget implements Target {
        //TODO: From docs picasso.Target must have a proper Object#equals(Object) and Object#hashCode()
        private View mPageView;
        private int mPageNum;

        public ComicPageViewTarget(View pageView, int pageNum) {
            mPageView = pageView;
            mPageNum = pageNum;
        }

        private void setVisibility(int imageView, int progressBar, int reloadButton) {
            mPageView.findViewById(R.id.pageImageView).setVisibility(imageView);
            mPageView.findViewById(R.id.pageProgressBar).setVisibility(progressBar);
            mPageView.findViewById(R.id.reloadButton).setVisibility(reloadButton);
        }

        @Override
        public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from) {
            setVisibility(View.VISIBLE, View.GONE, View.GONE);
            ImageView iv = mPageView.findViewById(R.id.pageImageView);
            iv.setImageBitmap(bitmap);
        }

        @Override
        public void onBitmapFailed(Exception e, Drawable errorDrawable) {
            setVisibility(View.GONE, View.GONE, View.VISIBLE);
            ImageButton reloadButton = mPageView.findViewById(R.id.reloadButton);

            reloadButton.setOnClickListener((View v) -> {
                setVisibility(View.GONE, View.VISIBLE, View.GONE);
                loadPageImage(this, mPageNum);
            });
        }

        @Override
        public void onPrepareLoad(Drawable placeHolderDrawable) {

        }
    }

    private class FingerTapListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            if (!mIsFullscreen) {
                setFullscreen(true);
                return true;
            }

            float x = e.getX();
            boolean isReadingLeftToRight = mSharedRes.getSettingsManager().getIsReadingLeftToRight();

            // Tap left edge
            if (x < (float) mViewPager.getWidth() / 3) {
                if (isReadingLeftToRight) {
                    if (mCurrentPageNum == 1) {
                        toPreviousComic();
                    } else {
                        setCurrentPage(mCurrentPageNum - 1);
                    }
                } else {
                    if (mCurrentPageNum == mComic.getPagesCount()) {
                        toNextComic();
                    } else {
                        setCurrentPage(mCurrentPageNum + 1);
                    }
                }
            }
            // Tap right edge
            else if (x > (float) mViewPager.getWidth() / 3 * 2) {
                if (isReadingLeftToRight) {
                    if (mCurrentPageNum == mComic.getPagesCount()) {
                        toNextComic();
                    } else {
                        setCurrentPage(mCurrentPageNum + 1);
                    }
                } else {
                    if (mCurrentPageNum == 1) {
                        toPreviousComic();
                    } else {
                        setCurrentPage(mCurrentPageNum - 1);
                    }
                }
            }
            // Tap center
            else {
                setFullscreen(false);
            }

            return true;
        }
    }

    private void setFullscreen(boolean fullscreen) {
        mIsFullscreen = fullscreen;

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();

        if (mIsFullscreen) {
            if (actionBar != null) actionBar.hide();

            int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
                flag |= View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.INVISIBLE);
        } else {
            if (actionBar != null) actionBar.show();

            int flag = View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN;
            if (Utils.isKitKatOrLater()) {
                flag |= View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION;
            }
            mViewPager.setSystemUiVisibility(flag);

            mPageNavLayout.setVisibility(View.VISIBLE);

            // status bar & navigation bar background won't show in some cases
            if (Utils.isLollipopOrLater()) {
                new Handler().postDelayed(() -> {
                    Window w = getActivity().getWindow();
                    w.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
                    w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
                }, 300);
            }
        }
    }

    private void toPreviousComic() {
        mSharedRes.getLibraryManager()
                .getSeriePreviousComic(mComic.getSeriesId(), mComic.getId())
                .subscribe(new Utils.DefaultMaybeObserver<Comic>("toPreviousComic", mCompositeDisposable) {
                    @Override
                    public void onSuccess(Comic comic) {
                        Log.d(mTag, "onSuccess");
                        if (mSharedRes.getSettingsManager().getIsSwitchComicConfirmation()) {
                            confirmSwitchToComic(comic, R.string.switch_prev_confirm_comic);
                        } else {
                            switchToComic(comic, R.string.switch_prev_comic);
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.d(mTag, "onComplete");
                        noMoreComic(R.string.switch_previous_no_more_comic);
                    }
                });
    }

    private void toNextComic() {
        mSharedRes.getLibraryManager()
                .getSerieNextComic(mComic.getSeriesId(), mComic.getId())
                .subscribe(new Utils.DefaultMaybeObserver<Comic>("toNextComic.getSerieNextComic", mCompositeDisposable) {
                    @Override
                    public void onSuccess(Comic comic) {
                        Log.d(mTag, "onSuccess");
                        if (mSharedRes.getSettingsManager().getIsSwitchComicConfirmation()) {
                            confirmSwitchToComic(comic, R.string.switch_next_confirm_comic);
                        } else {
                            switchToComic(comic, R.string.switch_next_comic);
                        }
                    }

                    @Override
                    public void onComplete() {
                        Log.d(mTag, "onComplete");
                        noMoreComic(R.string.switch_next_no_more_comic);
                    }
                });
    }

    private void confirmSwitchToComic(Comic newComic, int titleId) {
        new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setTitle(titleId)
                .setMessage(newComic.getName())
                .setPositiveButton(R.string.switch_action_positive, (d, which) -> {
                    ReaderActivity activity = (ReaderActivity) getActivity();
                    activity.setFragment(ReaderFragment.create(newComic));
                })
                .setNegativeButton(R.string.switch_action_negative, (dialog, which) -> {
                })
                .create()
                .show();
    }

    private void switchToComic(Comic newComic, int titleId) {
        String text = String.format("%s: %s",
                getActivity().getResources().getString(titleId),
                newComic.getName());

        ReaderActivity activity = (ReaderActivity) getActivity();
        activity.setFragment(ReaderFragment.create(newComic));
        Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
    }

    private void noMoreComic(int messageId) {
        new AlertDialog.Builder(getActivity(), R.style.AppCompatAlertDialogStyle)
                .setMessage(messageId)
                .setNeutralButton(R.string.switch_action_ok, (dialog, which) -> {
                })
                .create()
                .show();
    }
}
