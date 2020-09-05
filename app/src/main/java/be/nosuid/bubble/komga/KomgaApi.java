package be.nosuid.bubble.komga;

import java.util.List;

import io.reactivex.Completable;
import io.reactivex.Maybe;
import io.reactivex.Single;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.PATCH;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface KomgaApi {
    enum SortingMethod {
        NAME_ASC("name,asc"),
        NAME_DESC("name,desc"),
        NUMBER_ASC("number,asc"),
        NUMBER_DESC("number,desc"),

        // From metadata
        // Only usable for Series
        METADATA_TITLE_ASC("metadata.titleSort,asc"),
        // Only usable for Books
        METADATA_NUMBER_ASC("metadata.numberSort,asc");


        public final String sortingMethod;

        SortingMethod(String sortingMethod) {
            this.sortingMethod = sortingMethod;
        }

        @Override
        public String toString() {
            return this.sortingMethod;
        }
    }

    enum ReadStatus {
        UNREAD("UNREAD"),
        READ("READ"),
        IN_PROGRESS("IN_PROGRESS");

        public final String readStatus;

        ReadStatus(String readStatus) {
            this.readStatus = readStatus;
        }

        @Override
        public String toString() {
            return this.readStatus;
        }
    }

    @GET("series/{seriesId}/books")
    Single<Books> getSeriesBooksForPage(@Path("seriesId") String seriesId,
                                        @Query("page") int page,
                                        @Query("size") int pageSize,
                                        @Query("sort") SortingMethod sortingMethod);

    @GET("series/{seriesId}/books")
    Single<Books> getSeriesBooksForPage(@Path("seriesId") String seriesId,
                                        @Query("page") int page,
                                        @Query("size") int pageSize,
                                        @Query("sort") SortingMethod sortingMethod,
                                        @Query("read_status") List<ReadStatus> readStatus);

    @GET("series")
    Single<Series> getSeriesForPage(@Query("library_id") String libraryId,
                                    @Query("page") int page,
                                    @Query("size") int pageSize,
                                    @Query("sort") SortingMethod sortingMethod);

    @GET("books/{bookId}/next")
    Maybe<Books.Content> getBookSiblingNext(@Path("bookId") String bookId);

    @GET("books/{bookId}/previous")
    Maybe<Books.Content> getBookSiblingPrevious(@Path("bookId") String bookId);

    @PATCH("books/{bookId}/read-progress")
    Completable setBookReadProgress(@Path("bookId") String bookId, @Body ReadProgress readProgress);

    @GET("users/me")
    Single<User> getUserMe();
}