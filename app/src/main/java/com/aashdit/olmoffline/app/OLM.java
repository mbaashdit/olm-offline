package com.aashdit.olmoffline.app;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import androidx.multidex.MultiDex;

import com.aashdit.olmoffline.utils.LocaleManager;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.crashlytics.FirebaseCrashlytics;

import io.realm.Realm;
import io.realm.RealmConfiguration;

public class OLM extends Application {

    private static final String TAG = "OLM";

    public static double latitude = 0.0;
    public static double longitude = 0.0;

//    private FirebaseAnalytics mFirebaseAnalytics;

    @Override
    public void onCreate() {
        super.onCreate();

        MultiDex.install(this);
        Realm.init(this);


        RealmConfiguration config = new RealmConfiguration.Builder()
                .allowWritesOnUiThread(true)
                .build();

        Realm.setDefaultConfiguration(config);


//        final RealmConfiguration configuration = new RealmConfiguration.Builder().name("sample.realm").schemaVersion(7).migration(new RealmMigrations()).build();
//        Realm.setDefaultConfiguration(configuration);
//        Realm.getInstance(configuration);

        // Obtain the FirebaseAnalytics instance.
        FirebaseAnalytics.getInstance(this);
        FirebaseCrashlytics.getInstance();

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(LocaleManager.setLocale(base));
    }
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        LocaleManager.setLocale(this);
    }

    @Override
    public void onTerminate() {
        Realm.getDefaultInstance().close();
        super.onTerminate();
    }
}
