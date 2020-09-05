package be.nosuid.bubble.managers;

import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import be.nosuid.bubble.komga.Books;
import be.nosuid.bubble.komga.KomgaApi;
import be.nosuid.bubble.komga.ReadProgress;
import be.nosuid.bubble.model.Comic;
import be.nosuid.bubble.model.Series;
import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.exceptions.UndeliverableException;
import io.reactivex.plugins.RxJavaPlugins;
import io.reactivex.schedulers.Schedulers;

public class LibraryManager {
    private SharedResourcesManager mSharedRes;

    public LibraryManager(SharedResourcesManager srm) {
        mSharedRes = srm;

        // https://github.com/ReactiveX/RxJava/wiki/What's-different-in-2.0#error-handling
        // TODO: Got some InterruptedIOException ...
        RxJavaPlugins.setErrorHandler(e -> {
            if (e instanceof UndeliverableException) {
                e = e.getCause();
            }
            if ((e instanceof IOException) || (e instanceof SocketException)) {
                // fine, irrelevant network problem or API that throws on cancellation
                return;
            }
            if ((e instanceof InterruptedException) || (e instanceof InterruptedIOException)) {
                // fine, some blocking code was interrupted by a dispose call
                return;
            }
            if ((e instanceof NullPointerException) || (e instanceof IllegalArgumentException)) {
                // that's likely a bug in the application
                Thread.currentThread()
                        .getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
            if (e instanceof IllegalStateException) {
                // that's a bug in RxJava or in a custom operator
                Thread.currentThread()
                        .getUncaughtExceptionHandler()
                        .uncaughtException(Thread.currentThread(), e);
                return;
            }
            Log.w("LibraryManager", "Undeliverable exception received, not sure what to do", e);
        });
    }

    private int mPageSize = 20;

    public enum ComicsStatusFilter {
        ALL,
        READ, // Where pageLastRead == pagesCount
        UNREAD, // Where pageLastRead == 0
        UNFINISHED, // Where pageLastRead != pagesCount
        READING // Where UNREAD || UNFINISHED
    }

    private Series toSeriesMapper(be.nosuid.bubble.komga.Series.Content series) {
        // TODO: Last read book ~= number of books read
        return new Series(series.getId(), series.getName(),
                series.getBooksReadCount(), series.getBooksCount());
    }

    private List<Series> toSeriesListMapper(be.nosuid.bubble.komga.Series series) {
        List<Series> al = new ArrayList<>();
        for (be.nosuid.bubble.komga.Series.Content c : series.getContent()) {
            al.add(toSeriesMapper(c));
        }
        return al;
    }

    private List<Series> toSeriesListMapper(List<be.nosuid.bubble.komga.Series> seriesList) {
        List<Series> al = new ArrayList<>();
        for (be.nosuid.bubble.komga.Series s : seriesList) {
            al.addAll(toSeriesListMapper(s));
        }
        return al;
    }

    private Comic toComicMapper(Books.Content book) {
        return new Comic(book.getId(), book.getName(), book.getSeriesId(),
                book.getNumber(), book.getReadProgress().getPage(), book.getMedia().getPagesCount());
    }

    private List<Comic> toComicListMapper(Books books) {
        List<Comic> os = new ArrayList<>();
        for (Books.Content c : books.getContent()) {
            os.add(toComicMapper(c));
        }
        return os;
    }

    private List<Comic> toComicListMapper(List<Books> booksList) {
        List<Comic> al = new ArrayList<>();
        for (Books b : booksList) {
            al.addAll(toComicListMapper(b));
        }
        return al;
    }


    private static class RxJava2Sched {
        // http://reactivex.io/documentation/scheduler.html
        // Calling these will :
        //  - set the initial running thread to Schedulers.io()
        //  - change the running thread to AndroidSchedulers.mainThread() (the UI thread)
        // It should be call as late as possible to process everything in the
        //  Schedulers.io() thread and not freeze the UI running in AndroidSchedulers.mainThread().

        private static <T> SingleSource<T> applySchedulers(Single<T> upstream) {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        private static <T> Maybe<T> applySchedulers(Maybe<T> upstream) {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }

        private static Completable applySchedulers(Completable upstream) {
            return upstream
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread());
        }
    }

    private Single<List<Series>> getSeriesUnpaged(KomgaApi.SortingMethod sortingMethod) {
        String libraryId = mSharedRes.getSettingsManager().getKomgaLibraryId();

        // TODO: handle paging in Adapters
        Observable<be.nosuid.bubble.komga.Series> stream = Observable.create(subscriber -> {
            int pageNum = 0;
            be.nosuid.bubble.komga.Series page;

            do {
                page = mSharedRes.getKomgaApi()
                        .getSeriesForPage(libraryId, pageNum, mPageSize, sortingMethod)
                        .blockingGet();
                subscriber.onNext(page);
                pageNum += 1;
            } while (!page.isLast());

            subscriber.onComplete();
        });

        return stream
                .toList()
                .map(this::toSeriesListMapper);
    }

    private Single<List<Comic>> getComicsUnpaged(String seriesId, KomgaApi.SortingMethod sortingMethod) {
        return getComicsUnpaged(seriesId, sortingMethod, null);
    }

    private Single<List<Comic>> getComicsUnpaged(String seriesId, KomgaApi.SortingMethod sortingMethod, List<KomgaApi.ReadStatus> readStatus) {
        // TODO: handle paging in Adapters
        Observable<Books> stream = Observable.create(subscriber -> {
            int pageNum = 0;
            Books page;

            do {
                if (readStatus != null) {
                    page = mSharedRes.getKomgaApi()
                            .getSeriesBooksForPage(seriesId, pageNum, mPageSize, sortingMethod, readStatus)
                            .blockingGet();
                } else {
                    page = mSharedRes.getKomgaApi()
                            .getSeriesBooksForPage(seriesId, pageNum, mPageSize, sortingMethod)
                            .blockingGet();
                }

                if (subscriber.isDisposed()) {
                    return;
                }

                subscriber.onNext(page);
                pageNum += 1;
            } while (!page.isLast());

            subscriber.onComplete();
        });

        return stream
                .toList()
                .map(this::toComicListMapper);
    }

    public Single<List<Series>> getSeries() {
        return getSeriesUnpaged(KomgaApi.SortingMethod.METADATA_TITLE_ASC)
                .compose(RxJava2Sched::applySchedulers);
    }

    public Single<List<Comic>> getComics(String seriesId) {
        return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC)
                .compose(RxJava2Sched::applySchedulers);
    }

    public Single<List<Comic>> getComicsWithStatus(String seriesId, ComicsStatusFilter statusFilter) {
        switch (statusFilter) {
            case READ:
                return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC,
                        Arrays.asList(KomgaApi.ReadStatus.READ))
                        .compose(RxJava2Sched::applySchedulers);
            case UNREAD:
                return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC,
                        Arrays.asList(KomgaApi.ReadStatus.UNREAD))
                        .compose(RxJava2Sched::applySchedulers);
            case UNFINISHED:
                return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC,
                        Arrays.asList(KomgaApi.ReadStatus.IN_PROGRESS))
                        .compose(RxJava2Sched::applySchedulers);
            case READING:
                return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC,
                        Arrays.asList(KomgaApi.ReadStatus.UNREAD, KomgaApi.ReadStatus.IN_PROGRESS))
                        .compose(RxJava2Sched::applySchedulers);
            case ALL:
            default:
                return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC)
                        .compose(RxJava2Sched::applySchedulers);
        }
    }

    public Single<List<Comic>> getSeriesLastReadComics(String seriesId, int historySize) {
        return getComicsUnpaged(seriesId, KomgaApi.SortingMethod.METADATA_NUMBER_ASC,
                Arrays.asList(KomgaApi.ReadStatus.READ, KomgaApi.ReadStatus.IN_PROGRESS))
                .toObservable()
                .flatMapIterable(comics -> comics)
                .takeLast(historySize)
                .toSortedList(new Comic.ChapterReverseOrderComparator())
                .compose(RxJava2Sched::applySchedulers);
    }

    public Maybe<Comic> getSerieNextComic(String seriesId, String previousComicId) {
        return mSharedRes.getKomgaApi()
                .getBookSiblingNext(previousComicId)
                //TODO: Error (404 ?) -> Maybe.Empty()
                .onErrorComplete()
                .map(this::toComicMapper)
                .compose(RxJava2Sched::applySchedulers);
    }

    public Maybe<Comic> getSeriePreviousComic(String seriesId, String nextComicId) {
        return mSharedRes.getKomgaApi()
                .getBookSiblingPrevious(nextComicId)
                //TODO: Error (404 ?) -> Maybe.Empty()
                .onErrorComplete()
                .map(this::toComicMapper)
                .compose(RxJava2Sched::applySchedulers);
    }

    public Completable setComicLastReadPage(String comidId, int pageNum) {
        ReadProgress readProgress = new ReadProgress();
        readProgress.setPage(pageNum);

        return mSharedRes.getKomgaApi()
                .setBookReadProgress(comidId, readProgress)
                .onErrorComplete()
                .compose(RxJava2Sched::applySchedulers);
    }


    /*
     * Uri Builder for Picasso
     */

    public Uri getSerieThumbnailUri(String seriesId) {
        String baseUrl = mSharedRes.getSettingsManager().getKomgaApiUrl();
        return Uri.parse(String.format("%sseries/%s/thumbnail", baseUrl, seriesId));
    }

    public Uri getComicThumbnailUri(String comicId) {
        String baseUrl = mSharedRes.getSettingsManager().getKomgaApiUrl();
        return Uri.parse(String.format("%sbooks/%s/thumbnail", baseUrl, comicId));
    }

    public Uri getComicPageUri(String comicId, int pageNum) {
        assert pageNum > 0;

        String baseUrl = mSharedRes.getSettingsManager().getKomgaApiUrl();
        return Uri.parse(String.format("%sbooks/%s/pages/%d", baseUrl, comicId, pageNum));
    }

    public Maybe<Boolean> ping() {
        // Test authenticated access to the API,
        return mSharedRes.getKomgaApi()
                .getUserMe()
                .toMaybe()
                .onErrorComplete()
                .map(user -> true)
                .compose(RxJava2Sched::applySchedulers);
    }
}
