package be.nosuid.bubble.managers;

import android.content.SharedPreferences;
import android.content.res.Resources;

import java.util.HashMap;

import be.nosuid.bubble.R;

import static java.lang.Integer.parseInt;

public class SettingsManager {
    public enum PageViewMode {
        ASPECT_FILL,
        ASPECT_FIT,
        FIT_WIDTH
    }

    public static final String SETTINGS_PAGE_VIEW_MODE = "SETTINGS_PAGE_VIEW_MODE";
    public static final String SETTINGS_READING_LEFT_TO_RIGHT = "SETTINGS_READING_LEFT_TO_RIGHT";
    public static final int MAX_LAST_READ_COUNT = 5;

    private SharedPreferences mSharedPreferences;
    private Resources mResources;
    private OnSettingsChangeListener mOnSettingsChangeListener;

    private final static HashMap<Integer, PageViewMode> RESOURCE_VIEW_MODE;

    static {
        RESOURCE_VIEW_MODE = new HashMap<Integer, PageViewMode>();
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fill, PageViewMode.ASPECT_FILL);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_aspect_fit, PageViewMode.ASPECT_FIT);
        RESOURCE_VIEW_MODE.put(R.id.view_mode_fit_width, PageViewMode.FIT_WIDTH);
    }

    public interface OnSettingsChangeListener {
        void onSettingsChange();
    }

    public SettingsManager(SharedPreferences sharedPrefs, Resources res,
                           OnSettingsChangeListener oscl) {
        mSharedPreferences = sharedPrefs;
        mResources = res;
        mOnSettingsChangeListener = oscl;
    }

    public String getKomgaApiUrl() {
        String url = mSharedPreferences.getString(
                mResources.getString(R.string.setting_key_komga_url),
                "http://example/");

        if (!url.endsWith("/api/v1/")) {
            url += "/api/v1/";
        }

        return url;
    }

    public String getKomgaUsername() {
        return mSharedPreferences.getString(
                mResources.getString(R.string.setting_key_komga_username),
                "username");
    }

    public String getKomgaPassword() {
        return mSharedPreferences.getString(
                mResources.getString(R.string.setting_key_komga_password),
                "password");
    }

    public String getKomgaLibraryId() {
        return mSharedPreferences.getString(
                mResources.getString(R.string.setting_key_komga_library_id),
                "1");
    }

    public void setIsReadingLeftToRight(boolean isReadingLeftToRight) {
        mSharedPreferences.edit()
                .putBoolean(SETTINGS_READING_LEFT_TO_RIGHT, isReadingLeftToRight)
                .apply();
    }

    public boolean getIsReadingLeftToRight() {
        return mSharedPreferences.getBoolean(SETTINGS_READING_LEFT_TO_RIGHT, true);
    }

    public void setPageViewMode(PageViewMode pv) {
        mSharedPreferences.edit()
                .putInt(SETTINGS_PAGE_VIEW_MODE, pv.ordinal())
                .apply();
    }

    public PageViewMode getPageViewMode() {
        return PageViewMode.values()[mSharedPreferences.getInt(
                SETTINGS_PAGE_VIEW_MODE,
                PageViewMode.ASPECT_FIT.ordinal())];
    }

    public int getLibraryColumnsCount(int deviceWidth) {
        int minColumnWidth = mResources.getInteger(R.integer.cover_grid_library_min_column_width);
        int maxColumnCount = mResources.getInteger(R.integer.cover_grid_library_max_column_count);

        int columnsCount = Math.round((float) deviceWidth / minColumnWidth);
        if (columnsCount <= maxColumnCount) {
            return columnsCount;
        } else {
            return maxColumnCount;
        }
    }

    public int getSeriesColumnsCount(int deviceWidth) {
        int minColumnWidth = mResources.getInteger(R.integer.cover_grid_series_min_column_width);
        int maxColumnCount = mResources.getInteger(R.integer.cover_grid_series_max_column_count);

        int columnsCount = Math.round((float) deviceWidth / minColumnWidth);
        if (columnsCount <= maxColumnCount) {
            return columnsCount;
        } else {
            return maxColumnCount;
        }
    }

    public boolean getIsSwitchComicConfirmation() {
        return false;
    }
}
