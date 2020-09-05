package be.nosuid.bubble.fragment;

import android.content.Context;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

import be.nosuid.bubble.MainApplication;
import be.nosuid.bubble.R;
import be.nosuid.bubble.activity.MainActivity;
import be.nosuid.bubble.managers.SharedResourcesManager;
import be.nosuid.bubble.managers.Utils;
import be.nosuid.bubble.model.Series;
import be.nosuid.bubble.view.CoverViewHolder;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;


public class LibraryFragment extends Fragment {

    private SwipeRefreshLayout mRefreshLayout;
    private View mEmptyView;
    private RecyclerView mSeriesGridView;

    private SharedResourcesManager mSharedRes;

    private CompositeDisposable mCompositeDisposable;
    private List<Series> mSeries;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSharedRes = ((MainApplication) getActivity().getApplication()).getSharedResourcesManager();

        mCompositeDisposable = new CompositeDisposable();
        mSeries = new ArrayList<>();

        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        final ViewGroup view = (ViewGroup) inflater.inflate(R.layout.fragment_library, container, false);

        final int numColumns = mSharedRes.getSettingsManager()
                .getLibraryColumnsCount(Utils.getScreenWidth(getActivity()));
        final int spacing = (int) getResources().getDimension(R.dimen.cover_grid_margin);

        mRefreshLayout = view.findViewById(R.id.fragmentLibraryLayout);
        mRefreshLayout.setColorSchemeColors(getResources().getColor(R.color.primary));
        mRefreshLayout.setOnRefreshListener(() -> {
            refreshSeries();
        });
        mRefreshLayout.setEnabled(true);

        GridLayoutManager layoutManager = new GridLayoutManager(getActivity(), numColumns);
        layoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return 1;
            }
        });

        mSeriesGridView = view.findViewById(R.id.seriesGridView);
        mSeriesGridView.setHasFixedSize(true);
        mSeriesGridView.setLayoutManager(layoutManager);
        mSeriesGridView.setAdapter(new SeriesGridAdapter());
        mSeriesGridView.addItemDecoration(new SeriesGridSpacingItemDecoration(numColumns, spacing));

        mEmptyView = view.findViewById(R.id.fragmentLibraryEmptyLayout);

        getActivity().setTitle(R.string.menu_library);

        showEmptyMessage(false);

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
        refreshSeries();
    }


    private void refreshSeries() {
        mSharedRes.getLibraryManager()
                .getSeries()
                .subscribe(new Utils.DefaultSingleObserver<List<Series>>("refreshSeries", mCompositeDisposable) {
                    @Override
                    public void onSuccess(List<Series> series) {
                        Log.d(mTag, "onSuccess");
                        mSeries.clear();
                        mSeries.addAll(series);
                        mSeriesGridView.getAdapter().notifyDataSetChanged();
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
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    private void setLoading(boolean isLoading) {
        mRefreshLayout.setRefreshing(isLoading);
        showEmptyMessage(!isLoading && mSeries.size() == 0);
    }

    private void openSeries(Series series) {
        SeriesFragment fragment = SeriesFragment.create(series);
        ((MainActivity) getActivity()).pushFragment(fragment);
    }

    private void showEmptyMessage(boolean show) {
        mEmptyView.setVisibility(show ? View.VISIBLE : View.GONE);
        mRefreshLayout.setEnabled(!show);
    }

    private Series getSeriesAtPosition(int position) {
        return mSeries.get(position);
    }

    private final class SeriesGridSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private int mSpanCount;
        private int mSpacing;

        public SeriesGridSpacingItemDecoration(int spanCount, int spacing) {
            mSpanCount = spanCount;
            mSpacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view);
            int column = position % mSpanCount;

            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            outRect.right = (column + 1) * mSpacing / mSpanCount;

            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            outRect.bottom = mSpacing;
        }
    }

    private final class SeriesGridAdapter extends RecyclerView.Adapter {
        @Override
        public int getItemCount() {
            return mSeries.size();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
            Context ctx = viewGroup.getContext();
            View view = LayoutInflater.from(ctx)
                    .inflate(R.layout.card_cover, viewGroup, false);
            return new SeriesViewHolder(view);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, int i) {
            Series series = getSeriesAtPosition(i);
            SeriesViewHolder holder = (SeriesViewHolder) viewHolder;
            holder.setupSeries(series);
        }
    }

    private class SeriesViewHolder extends CoverViewHolder {
        public SeriesViewHolder(View view) {
            super(view);
        }

        private void setupSeries(Series series) {
            setupText(series.getName(), series.getBooksLastRead(), series.getBooksCount());
            setupCover(mSharedRes.getPicasso(),
                    mSharedRes.getLibraryManager().getSerieThumbnailUri(series.getId()),
                    getActivity());
        }

        @Override
        public void onClick(View v) {
            Series series = getSeriesAtPosition(getAdapterPosition());
            openSeries(series);
        }
    }
}
