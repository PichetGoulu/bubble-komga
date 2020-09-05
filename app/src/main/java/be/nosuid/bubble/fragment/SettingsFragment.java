package be.nosuid.bubble.fragment;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import be.nosuid.bubble.MainApplication;
import be.nosuid.bubble.R;
import be.nosuid.bubble.managers.SharedResourcesManager;
import be.nosuid.bubble.managers.Utils;
import io.reactivex.disposables.CompositeDisposable;


public class SettingsFragment extends PreferenceFragmentCompat implements SharedPreferences.OnSharedPreferenceChangeListener {

    private SharedResourcesManager mSharedRes;
    private CompositeDisposable mCompositeDisposable;

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        MainApplication app = (MainApplication) getActivity().getApplication();
        mSharedRes = app.getSharedResourcesManager();
        mCompositeDisposable = new CompositeDisposable();

        PreferenceManager preferenceManager = getPreferenceManager();
        preferenceManager.setSharedPreferencesName(app.getString(R.string.setting_key_preferences_file));
        preferenceManager.setSharedPreferencesMode(Context.MODE_PRIVATE);
        preferenceManager.getSharedPreferences().registerOnSharedPreferenceChangeListener(this);

        addPreferencesFromResource(R.xml.app_preferences);

        findPreference(getString(R.string.setting_key_komga_test_button)).setOnPreferenceClickListener(preference -> {
            mSharedRes.getLibraryManager()
                    .ping()
                    .subscribe(new Utils.DefaultMaybeObserver<Boolean>("testKomgaSettings", mCompositeDisposable) {
                        @Override
                        public void onSuccess(Boolean aBoolean) {
                            Toast.makeText(app, R.string.komga_test_api_ok, Toast.LENGTH_SHORT)
                                    .show();
                        }

                        @Override
                        public void onComplete() {
                            Toast.makeText(app, R.string.komga_test_api_error, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    });
            return true;
        });
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        mSharedRes.onSettingsChange();
    }

    @Override
    public void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
}