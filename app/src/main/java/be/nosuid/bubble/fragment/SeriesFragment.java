package be.nosuid.bubble.fragment;

import android.content.Context;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import be.nosuid.bubble.MainApplication;
import be.nosuid.bubble.R;
import be.nosuid.bubble.activity.ReaderActivity;
import be.nosuid.bubble.managers.LibraryManager.ComicsStatusFilter;
import be.nosuid.bubble.managers.SettingsManager;
import be.nosuid.bubble.managers.SharedResourcesManager;
import be.nosuid.bubble.managers.Utils;
import be.nosuid.bubble.model.Comic;
import be.nosuid.bubble.model.Series;
import be.nosuid.bubble.view.CoverViewHolder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;

public class SeriesFragment extends Fragment {
    private static final String PARAM_SERIES = "PARAM_SERIES";

    private final int ITEM_VIEW_TYPE_COMIC = 1;
    private final int ITEM_VIEW_TYPE_HEADER_LAST_READ = 2;
    private final int ITEM_VIEW_TYPE_HEADER_ALL = 3;

    private final int NUM_HEADERS = 2;

    private SharedResourcesManager mSharedRes;

    private SwipeRefreshLayout mRefreshLayout;

    private Series mSeries;
    private List<Comic> mFilteredComics;
    private List<Comic> mLastReadComics;

    private CompositeDisposable mCompositeDisposable;

    private ComicsStatusFilter mComicsStatusFilter = ComicsStatusFilter.ALL;

    private RecyclerView mComicsGridView;

    public static SeriesFragment create(Series series) {
        SeriesFragment fragment = new SeriesFragment();
        Bundle args = new Bundle();
        args.putSerializable(PARAM_SERIES, series);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedRes = ((MainApplication) getActivity().getApplication()).getSharedResourcesManager();

        mSeries = (Series) getArguments().getSerializable(PARAM_SERIES);
        mFilteredComics = new ArrayList<>();
        mLastReadComics = new ArrayList<>();

        mCompositeDisposable = new CompositeDisposable();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.fragment_series, container, false);

        final int numColumns = mSharedRes.getSettingsManager()
                .getSeriesColumnsCount(Utils.getScreenWidth(getActivity()));
        final int spacing = (int) getResources().getDimension(R.dimen.cover_grid_margin);

        mRefreshLayout = view.findViewById(R.id.fragmentSeriesLayout);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        mRefreshLayout.setOnRefreshListener(() -> {
            refreshLastReadComics();
            refreshFilteredComics();
        });
        mRefreshLayout.setEnabled(true);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                if (getItemViewTypeAtPosition(position) == ITEM_VIEW_TYPE_COMIC) {
                    return 1;
                } else {
                    return numColumns;
                }
            }
        });

        mComicsGridView = view.findViewById(R.id.comicsGridView);
        mComicsGridView.setHasFixedSize(true);
        mComicsGridView.setLayoutManager(layoutManager);
        mComicsGridView.setAdapter(new ComicGridAdapter());
        mComicsGridView.addItemDecoration(new ComicsGridSpacingItemDecoration(numColumns, spacing));

        getActivity().setTitle(mSeries.getName());

        return view;
    }

    @Override
    public void onDestroy() {
        mSharedRes.getPicasso().cancelTag(getActivity());
        if (!mCompositeDisposable.isDisposed()) {
            mCompositeDisposable.dispose();
        }
        super.onDestroy();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshFilteredComics();
        refreshLastReadComics();
    }

    private void refreshFilteredComics() {
        mSharedRes.getLibraryManager()
                .getComicsWithStatus(mSeries.getId(), mComicsStatusFilter)
                .subscribe(new Utils.DefaultSingleObserver<List<Comic>>("refreshFilteredComics", mCompositeDisposable) {
                    @Override
                    public void onSuccess(List<Comic> comics) {
                        Log.d(mTag, "onSuccess");
                        mFilteredComics.clear();
                        mFilteredComics.addAll(comics);
                        mComicsGridView.getAdapter().notifyDataSetChanged();
                        setLoading(false);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        setLoading(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        setLoading(false);
                    }
                });
    }

    private void refreshLastReadComics() {
        mSharedRes.getLibraryManager()
                .getSeriesLastReadComics(mSeries.getId(), SettingsManager.MAX_LAST_READ_COUNT)
                .subscribe(new Utils.DefaultSingleObserver<List<Comic>>("refreshLastReadComics", mCompositeDisposable) {
                    @Override
                    public void onSuccess(List<Comic> comics) {
                        Log.d(mTag, "onSuccess");
                        mLastReadComics.clear();
                        mLastReadComics.addAll(comics);
                        mComicsGridView.getAdapter().notifyDataSetChanged();
                        setLoading(false);
                    }

                    @Override
                    public void onSubscribe(Disposable d) {
                        super.onSubscribe(d);
                        setLoading(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        super.onError(e);
                        setLoading(false);
                    }
                });
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.browser, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_browser_filter_all:
                return changeComicsStatusFilter(item, ComicsStatusFilter.ALL);
            case R.id.menu_browser_filter_read:
                return changeComicsStatusFilter(item, ComicsStatusFilter.READ);
            case R.id.menu_browser_filter_unread:
                return changeComicsStatusFilter(item, ComicsStatusFilter.UNREAD);
            case R.id.menu_browser_filter_unfinished:
                return changeComicsStatusFilter(item, ComicsStatusFilter.UNFINISHED);
            case R.id.menu_browser_filter_reading:
                return changeComicsStatusFilter(item, ComicsStatusFilter.READING);
        }

        return super.onOptionsItemSelected(item);
    }

    private boolean changeComicsStatusFilter(MenuItem item, ComicsStatusFilter filter) {
        mComicsStatusFilter = filter;
        item.setChecked(true);
        refreshFilteredComics();
        return true;
    }

    private void setLoading(boolean isLoading) {
        mRefreshLayout.setRefreshing(isLoading);
    }

    private void openComic(Comic comic) {
        Intent intent = new Intent(getActivity(), ReaderActivity.class);
        intent.putExtra(ReaderFragment.PARAM_COMIC, comic);
        startActivity(intent);
    }

    private Comic getComicAtPosition(int position) {
        if (hasLastRead()) {
            // mLastReadComics are displayed on top of all mFilteredComics
            // First position is the "Last Read" header
            // Position "mLastReadComics.size() + 1" is the "All" header
            if (position > 0 && position < mLastReadComics.size() + 1)
                return mLastReadComics.get(position - 1);
            else
                return mFilteredComics.get(position - mLastReadComics.size() - NUM_HEADERS);
        } else {
            return mFilteredComics.get(position);
        }
    }

    private int getItemViewTypeAtPosition(int position) {
        if (hasLastRead()) {
            if (position == 0)
                return ITEM_VIEW_TYPE_HEADER_LAST_READ;
            else if (position == mLastReadComics.size() + 1)
                return ITEM_VIEW_TYPE_HEADER_ALL;
        }
        return ITEM_VIEW_TYPE_COMIC;
    }

    private boolean hasLastRead() {
        return mComicsStatusFilter == ComicsStatusFilter.ALL
                && mLastReadComics.size() > 0;
    }

    private final class ComicsGridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public ComicsGridSpacingItemDecoration(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);

            if (hasLastRead()) {
                // those are headers
                if (position == 0 || position == mLastReadComics.size() + 1)
                    return;

                if (position > 0 && position < mLastReadComics.size() + 1) {
                    position -= 1;
                } else {
                    position -= (NUM_HEADERS + mLastReadComics.size());
                }
            }

            int column = position % mSpanCount;

            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }

    private final class ComicGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            if (hasLastRead()) {
                return mFilteredComics.size() + mLastReadComics.size() + NUM_HEADERS;
            }
            return mFilteredComics.size();
        }

        @Override
        public int getItemViewType(int position) {
            return getItemViewTypeAtPosition(position);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();

            if (i == ITEM_VIEW_TYPE_HEADER_LAST_READ) {
                TextView view = (TextView) LayoutInflater.from(ctx)
                        .inflate(R.layout.fragment_series_header, viewGroup, false);
                view.setText(R.string.library_header_recent);

                int spacing = (int) getResources().getDimension(R.dimen.cover_grid_margin);
                RecyclerView.LayoutParams lp = (RecyclerView.LayoutParams) view.getLayoutParams();
                lp.setMargins(0, spacing, 0, 0);

                return new HeaderViewHolder(view);
            } else if (i == ITEM_VIEW_TYPE_HEADER_ALL) {
                TextView view = (TextView) LayoutInflater.from(ctx)
                        .inflate(R.layout.fragment_series_header, viewGroup, false);
                view.setText(R.string.library_header_all);

                return new HeaderViewHolder(view);
            }

            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_cover, viewGroup, false);
            return new ComicViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            if (viewHolder.getItemViewType() == ITEM_VIEW_TYPE_COMIC) {
                Comic comic = getComicAtPosition(i);
                ComicViewHolder holder = (ComicViewHolder) viewHolder;
                holder.setupComic(comic);
            }
        }
    }

    private class HeaderViewHolder extends RecyclerView.ViewHolder {
        public HeaderViewHolder(TextView textView) {
            super(textView);
        }
    }

    private class ComicViewHolder extends CoverViewHolder {
        public ComicViewHolder(View view) {
            super(view);
        }

        private void setupComic(Comic comic) {
            setupText(comic.getName(), comic.getPageLastRead(), comic.getPagesCount());
            setupCover(mSharedRes.getPicasso(),
                    mSharedRes.getLibraryManager().getComicThumbnailUri(comic.getId()),
                    getActivity());
        }

        @Override
        public void onClick(View v) {
            Comic comic = getComicAtPosition(getAdapterPosition());
            openComic(comic);
        }
    }
}
