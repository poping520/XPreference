package com.poping520.open.xpreference;


import android.content.Context;
import android.content.SharedPreferences;

import com.poping520.open.xpreference.storage.SharedPrefsDataManager;
import com.poping520.open.xpreference.storage.Storage;

public class PreferenceManager {

    private Context mContext;

    private long mNextId = -1;

    private Storage mStorage;

    private SharedPreferences mSharedPrefs;

    private PreferenceScreen mPreferenceScreen;

    PreferenceManager(Context context) {
        mContext = context;
    }

    public void setStorage(Storage storage) {
        mStorage = storage;
    }

    public Storage getStorage() {
        if (mStorage != null) return mStorage;

        if (mSharedPrefs == null) {
            mSharedPrefs = mContext.getSharedPreferences(mContext.getPackageName() + "_preference", Context.MODE_PRIVATE);
        }
        mStorage = new SharedPrefsDataManager(mSharedPrefs);
        return mStorage;
    }

    public boolean setPreferenceScreen(PreferenceScreen preferenceScreen) {
        if (preferenceScreen != mPreferenceScreen) {
            if (mPreferenceScreen != null) mPreferenceScreen.onDetached();
            mPreferenceScreen = preferenceScreen;
            return true;
        }
        return false;
    }

    public PreferenceScreen getPreferenceScreen() {
        return mPreferenceScreen;
    }

    public long getNextId() {
        synchronized (this) {
            return ++mNextId;
        }
    }
}
