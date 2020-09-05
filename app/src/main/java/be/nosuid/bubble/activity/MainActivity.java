package be.nosuid.bubble.activity;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;

import be.nosuid.bubble.R;
import be.nosuid.bubble.fragment.AboutFragment;
import be.nosuid.bubble.fragment.LibraryFragment;
import be.nosuid.bubble.fragment.SettingsFragment;
import be.nosuid.bubble.managers.Utils;


public class MainActivity extends AppCompatActivity implements FragmentManager.OnBackStackChangedListener {
    private final static String STATE_CURRENT_MENU_ITEM = "STATE_CURRENT_MENU_ITEM";

    private DrawerLayout mDrawerLayout;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentNavItem;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportFragmentManager().addOnBackStackChangedListener(this);

        if (Utils.isLollipopOrLater()) {
            toolbar.setElevation(8);
        }

        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        NavigationView navigationView = findViewById(R.id.navigation_view);
        setupNavigationView(navigationView);

        mDrawerLayout = findViewById(R.id.drawer_layout);
        mDrawerToggle = new ActionBarDrawerToggle(
                this, mDrawerLayout,
                R.string.drawer_open, R.string.drawer_close);
        mDrawerLayout.setDrawerListener(mDrawerToggle);


        if (savedInstanceState == null) {
            setFragment(new LibraryFragment());
            mCurrentNavItem = R.id.drawer_menu_library;
            navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
        } else {
            onBackStackChanged();  // force-call method to ensure indicator is shown properly
            mCurrentNavItem = savedInstanceState.getInt(STATE_CURRENT_MENU_ITEM);
            navigationView.getMenu().findItem(mCurrentNavItem).setChecked(true);
        }
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mDrawerToggle.syncState();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_MENU_ITEM, mCurrentNavItem);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        mDrawerToggle.onConfigurationChanged(newConfig);
    }

    private void setFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() >= 1) {
            fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        }

        fragmentManager
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();
    }

    public void pushFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.content_frame, fragment)
                .addToBackStack(null)
                .commit();
    }

    private boolean popFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        if (fragmentManager.getBackStackEntryCount() > 0) {
            fragmentManager.popBackStack();
            return true;
        }
        return false;
    }

    private void setupNavigationView(NavigationView view) {
        view.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if (mCurrentNavItem == menuItem.getItemId()) {
                    mDrawerLayout.closeDrawers();
                    return true;
                }

                switch (menuItem.getItemId()) {
                    case R.id.drawer_menu_library:
                        setFragment(new LibraryFragment());
                        break;
                    case R.id.drawer_menu_settings:
                        setTitle(R.string.menu_settings);
                        setFragment(new SettingsFragment());
                        break;
                    case R.id.drawer_menu_about:
                        setTitle(R.string.menu_about);
                        setFragment(new AboutFragment());
                        break;
                }

                mCurrentNavItem = menuItem.getItemId();
                menuItem.setChecked(true);
                mDrawerLayout.closeDrawers();
                return true;
            }
        });
    }

    @Override
    public void onBackStackChanged() {
        mDrawerToggle.setDrawerIndicatorEnabled(getSupportFragmentManager().getBackStackEntryCount() == 0);
    }

    @Override
    public void onBackPressed() {
        if (!popFragment()) {
            finish();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        if (!popFragment()) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.START))
                mDrawerLayout.closeDrawers();
            else
                mDrawerLayout.openDrawer(GravityCompat.START);
        }
        return super.onSupportNavigateUp();
    }
}
