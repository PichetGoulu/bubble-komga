package be.nosuid.bubble.managers;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.squareup.picasso.OkHttp3Downloader;
import com.squareup.picasso.Picasso;

import be.nosuid.bubble.R;
import be.nosuid.bubble.komga.KomgaApi;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

public class SharedResourcesManager implements SettingsManager.OnSettingsChangeListener {

    private Context mAppContext;
    private SettingsManager mSettingsManager;
    private OkHttpClient mOkHttpClient;
    private Picasso mPicasso;
    private KomgaApi mKomgaApi;
    private LibraryManager mLibraryManager;

    public SharedResourcesManager(Context appContext) {
        mAppContext = appContext;
        SharedPreferences sharedPrefs = mAppContext.getSharedPreferences(
                appContext.getString(R.string.setting_key_preferences_file), Context.MODE_PRIVATE);

        mSettingsManager = new SettingsManager(sharedPrefs, mAppContext.getResources(), this);

        onSettingsChange();
    }

    @Override
    public void onSettingsChange() {
        Log.d("SharedResourcesManager", "onSettingsChange");
        mOkHttpClient = createHttpClient();
        if (mPicasso != null) {
            mPicasso.shutdown();
        }
        mPicasso = createPicasso();
        mKomgaApi = createKomgaApi();

        mLibraryManager = createLibraryManager();
    }

    private OkHttpClient createHttpClient() {
        String authToken = Credentials.basic(
                mSettingsManager.getKomgaUsername(),
                mSettingsManager.getKomgaPassword());

        //TODO: Check if bad user/pass : java.net.ProtocolException: Too many follow-up requests: 21

        return new OkHttpClient.Builder()
                .authenticator((route, response) -> response.request().newBuilder()
                        .header("Authorization", authToken)
                        .build())
                .addInterceptor(chain -> {
                    Request request = chain.request();
                    Log.d("OkHttpClient", String.format("requesting: %s", request.url()));
                    return chain.proceed(request);
                })
                .build();
    }

    private Picasso createPicasso() {
        if (getOkHttpClient() == null) {
            return null;
        }

        return new Picasso.Builder(mAppContext)
                .downloader(new OkHttp3Downloader(getOkHttpClient()))
                .build();

    }

    private KomgaApi createKomgaApi() {
        if (getOkHttpClient() == null) {
            return null;
        }

        return new Retrofit.Builder()
                .baseUrl(mSettingsManager.getKomgaApiUrl())
                .client(getOkHttpClient())
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
                .create(KomgaApi.class);
    }

    private LibraryManager createLibraryManager() {
        if (getKomgaApi() == null) {
            return null;
        }

        return new LibraryManager(this);
    }


    private OkHttpClient getOkHttpClient() {
        return mOkHttpClient;
    }

    public SettingsManager getSettingsManager() {
        return mSettingsManager;
    }

    public Picasso getPicasso() {
        return mPicasso;
    }

    public KomgaApi getKomgaApi() {
        return mKomgaApi;
    }

    public LibraryManager getLibraryManager() {
        return mLibraryManager;
    }
}