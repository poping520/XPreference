package com.poping520.open.xpreference.datastore;

import android.support.annotation.Nullable;

import java.util.Set;

/**
 * Created by WangKZ on 18/07/20.
 *
 * @author poping520
 * @version 1.0.0
 */

public interface Storage {


    boolean saveInt(String key, int value);


    boolean saveLong(String key, long value);


    boolean saveFloat(String key, float value);


    boolean saveBoolean(String key, boolean value);


    boolean saveString(String key, String value);


    boolean saveStringSet(String key, Set<String> value);


    int getInt(String key, int defaultValue);


    long getLong(String key, long defaultValue);


    float getFloat(String key, float defaultValue);


    boolean getBoolean(String key, boolean defaultValue);


    String getString(String key, @Nullable String defaultValue);


    Set<String> getStringSet(String key, @Nullable Set<String> defaultValue);


    boolean contains(String key);
}
