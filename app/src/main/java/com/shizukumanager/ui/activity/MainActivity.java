package com.shizukumanager.app.ui.activity;

import android.os.Bundle;
import android.view.View;
import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;
import com.shizukumanager.app.R;
import com.shizukumanager.app.ShizukuManagerApplication;
import com.shizukumanager.app.di.AppContainer;
import com.shizukumanager.app.pkg.PackageEventReceiver;
import com.shizukumanager.app.ui.view.SoftGradientBackgroundView;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigationrail.NavigationRailView;

// Activity chinh quan ly dieu huong Navigation Component, EdgeToEdge va su kien he thong
public class MainActivity extends AppCompatActivity {

    private AppContainer appContainer;
    private PackageEventReceiver packageEventReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        appContainer = ((ShizukuManagerApplication) getApplication()).getAppContainer();

        SoftGradientBackgroundView bgView = findViewById(R.id.softGradientBackground);
        appContainer.getPreferencesManager().observeAmbientOrbsEnabled().observe(this, enabled -> {
            if (bgView != null && enabled != null) {
                bgView.setAmbientOrbsEnabled(enabled);
            }
        });

        setupNavigationAndInsets();

        packageEventReceiver = new PackageEventReceiver(pkg -> appContainer.getAppRepository().refreshPackages());
        packageEventReceiver.register(this);

        // Quet du lieu thuc te ngay khi khoi dong
        appContainer.getAppRepository().refreshPackages();
    }

    private void setupNavigationAndInsets() {
        NavHostFragment navHost = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.navHostFragment);
        if (navHost == null) {
            return;
        }
        NavController navController = navHost.getNavController();

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigation);
        NavigationRailView navRail = findViewById(R.id.navigationRail);
        View navHostView = findViewById(R.id.navHostFragment);

        if (bottomNav != null) {
            NavigationUI.setupWithNavController(bottomNav, navController);
            ViewCompat.setOnApplyWindowInsetsListener(navHostView, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, bars.right, 0);
                return insets;
            });
            ViewCompat.setOnApplyWindowInsetsListener(bottomNav, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, 0, bars.right, bars.bottom);
                return insets;
            });
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                int id = destination.getId();
                boolean hideBottom = id == R.id.appDetailsFragment
                        || id == R.id.historyFragment
                        || id == R.id.settingsFragment;
                bottomNav.setVisibility(hideBottom ? View.GONE : View.VISIBLE);
            });
        } else if (navRail != null) {
            NavigationUI.setupWithNavController(navRail, navController);
            ViewCompat.setOnApplyWindowInsetsListener(navRail, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(bars.left, bars.top, 0, bars.bottom);
                return insets;
            });
            ViewCompat.setOnApplyWindowInsetsListener(navHostView, (v, insets) -> {
                Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                v.setPadding(0, bars.top, bars.right, bars.bottom);
                return insets;
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        appContainer.getShizukuConnectionManager().refreshStatus();
        if (appContainer.getPreferencesManager().isAutoRefreshOnResume()) {
            appContainer.getAppRepository().refreshPackages();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (packageEventReceiver != null) {
            packageEventReceiver.unregister(this);
        }
    }
}
