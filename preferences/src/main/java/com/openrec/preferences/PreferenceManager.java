package com.openrec.preferences;

import android.content.Context;
import android.content.SharedPreferences;

public class PreferenceManager {

    private final SharedPreferences sharedPreferences;

    // SHARED PREF KEYS
    public static final String SORT_PREF="sort";
    public static final String ASC_DSC_PREF="asc_dsc";
    public static final String VIEW_GRID_PREF ="view_grid";
    public static final String THEME_PREF="theme";
    public static final String FORMAT_PREF="format";

    public PreferenceManager(Context context){
        sharedPreferences=context.getSharedPreferences(context.getPackageName() + "_preferences", Context.MODE_PRIVATE);
    }

    public boolean getBoolean(String key){
        return sharedPreferences.getBoolean(key, false);
    }

    public void setBoolean(String key, boolean bool){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putBoolean(key, bool);
        editor.apply();
    }

    public int getInt(String key){
        return sharedPreferences.getInt(key,0);
    }

    public void setInt(String key, int integer){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putInt(key, integer);
        editor.apply();
    }

    public String getString(String key){
        return sharedPreferences.getString(key, "descending");
    }

    public void setString(String key, String value){
        SharedPreferences.Editor editor=sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }
}
