package com.poping520.open.xpreference.storage;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import java.util.Set;

/**
 * Created by WangKZ on 18/07/21.
 *
 * @author poping520
 * @version 1.0.0
 */
public class SharedPreferenceStorage implements Storage {

    private SharedPreferences mSharedPrefs;
    private SharedPreferences.Editor mEditor;

    public SharedPreferenceStorage(SharedPreferences sharedPrefs) {
        mSharedPrefs = sharedPrefs;
        mEditor = mSharedPrefs.edit();
        mEditor.apply();
    }

    @Override
    public boolean saveInt(String key, int value) {
        mEditor.putInt(key, value).apply();
        return true;
    }

    @Override
    public boolean saveLong(String key, long value) {
        mEditor.putLong(key, value).apply();
        return true;
    }

    @Override
    public boolean saveFloat(String key, float value) {
        mEditor.putFloat(key, value).apply();
        return true;
    }

    @Override
    public boolean saveBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value).apply();
        return true;
    }

    @Override
    public boolean saveString(String key, String value) {
        mEditor.putString(key, value).apply();
        return true;
    }

    @Override
    public boolean saveStringSet(String key, Set<String> value) {
        mEditor.putStringSet(key, value).apply();
        return true;
    }

    @Override
    public int getInt(String key, int defaultValue) {
        return mSharedPrefs.getInt(key, defaultValue);
    }

    @Override
    public long getLong(String key, long defaultValue) {
        return mSharedPrefs.getLong(key, defaultValue);
    }

    @Override
    public float getFloat(String key, float defaultValue) {
        return mSharedPrefs.getFloat(key, defaultValue);
    }

    @Override
    public boolean getBoolean(String key, boolean defaultValue) {
        return mSharedPrefs.getBoolean(key, defaultValue);
    }

    @Override
    public String getString(String key, @Nullable String defaultValue) {
        return mSharedPrefs.getString(key, defaultValue);
    }

    @Override
    public Set<String> getStringSet(String key, @Nullable Set<String> defaultValue) {
        return mSharedPrefs.getStringSet(key, defaultValue);
    }

    @Override
    public boolean contains(String key) {
        return mSharedPrefs.contains(key);
    }
}
