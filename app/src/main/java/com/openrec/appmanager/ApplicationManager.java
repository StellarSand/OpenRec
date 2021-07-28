package com.openrec.appmanager;


import android.app.Application;
import android.os.Build;
import androidx.appcompat.app.AppCompatDelegate;

import com.openrec.R;
import com.openrec.preferences.PreferenceManager;

import static com.openrec.preferences.PreferenceManager.THEME_PREF;

public class ApplicationManager extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager preferenceManager=new PreferenceManager(this);

        // THEME
        if (preferenceManager.getInt(THEME_PREF)==0){
            if (Build.VERSION.SDK_INT>=29){
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            }
            else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        }

        else if (preferenceManager.getInt(THEME_PREF)==R.id.option_default){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else if (preferenceManager.getInt(THEME_PREF)==R.id.option1){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else if (preferenceManager.getInt(THEME_PREF)==R.id.option2){
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
    }
}
