package be.nosuid.bubble.managers;

import android.content.Context;
import android.os.Build;
import android.util.DisplayMetrics;
import android.util.Log;

import io.reactivex.CompletableObserver;
import io.reactivex.MaybeObserver;
import io.reactivex.SingleObserver;
import io.reactivex.disposables.Disposable;
import io.reactivex.internal.disposables.DisposableContainer;


public final class Utils {
    public static boolean isKitKatOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static boolean isLollipopOrLater() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP;
    }

    public static int getScreenWidth(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(displayMetrics.widthPixels / displayMetrics.density);
    }

    public static int getScreenHeight(Context context) {
        DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
        return Math.round(displayMetrics.heightPixels / displayMetrics.density);
    }

    public static abstract class DefaultObserver<T> {
        protected String mTag;
        protected DisposableContainer mDisposableContainer;

        public DefaultObserver(String tag, DisposableContainer disposableContainer) {
            mTag = tag;
            mDisposableContainer = disposableContainer;
        }

        public void onSubscribe(Disposable d) {
            Log.d(mTag, "onSubscribe");
            mDisposableContainer.add(d);
        }

        public void onError(Throwable e) {
            Log.d(mTag, "onError");
            Log.e(mTag, e.toString());
        }
    }

    public static abstract class DefaultSingleObserver<T> extends DefaultObserver<T> implements SingleObserver<T> {
        public DefaultSingleObserver(String tag, DisposableContainer disposableContainer) {
            super(tag, disposableContainer);
        }

        abstract public void onSuccess(T t);
    }

    public static abstract class DefaultMaybeObserver<T> extends DefaultObserver<T> implements MaybeObserver<T> {
        public DefaultMaybeObserver(String tag, DisposableContainer disposableContainer) {
            super(tag, disposableContainer);
        }

        abstract public void onSuccess(T t);

        abstract public void onComplete();
    }

    public static abstract class DefaultCompletableObserver extends DefaultObserver implements CompletableObserver {
        public DefaultCompletableObserver(String tag, DisposableContainer disposableContainer) {
            super(tag, disposableContainer);
        }

        abstract public void onComplete();
    }
}
