package be.nosuid.bubble;

import android.app.Application;

import be.nosuid.bubble.managers.SharedResourcesManager;


public class MainApplication extends Application {
    private SharedResourcesManager mSharedResourcesManager;

    @Override
    public void onCreate() {
        super.onCreate();
        mSharedResourcesManager = new SharedResourcesManager(this);
    }

    public SharedResourcesManager getSharedResourcesManager() {
        return mSharedResourcesManager;
    }
}
