package com.poping520.open.xpreference;


import android.content.Context;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;

import com.poping520.open.xpreference.storage.SharedPrefsDataManager;
import com.poping520.open.xpreference.storage.Storage;

import java.util.ArrayList;
import java.util.List;

public class PreferenceManager {

    private Context mContext;

    private long mNextId = -1;

    private Storage mStorage;

    private SharedPreferences mSharedPrefs;

    private PreferenceScreen mPreferenceScreen;

    private PreferenceComparisonCallback mPreferenceComparisonCallback;
    private OnPreferenceTreeClickListener mOnPreferenceTreeClickListener;
    private OnDisplayPreferenceDialogListener mOnDisplayPreferenceDialogListener;
    private OnNavigateToScreenListener mOnNavigateToScreenListener;


    public static abstract class PreferenceComparisonCallback {

        public abstract boolean arePreferenceItemsTheSame(Preference p1, Preference p2);

        public abstract boolean arePreferenceContentsTheSame(Preference p1, Preference p2);
    }

    public interface OnPreferenceTreeClickListener {

        boolean onPreferenceTreeClick(Preference preference);
    }

    public interface OnDisplayPreferenceDialogListener {

        void onDisplayPreferenceDialog(Preference preference);
    }

    public interface OnNavigateToScreenListener {

        void onNavigateToScreen(PreferenceScreen preferenceScreen);
    }

    public interface OnActivityDestroyListener {

        void onActivityDestroy();
    }


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

    public PreferenceComparisonCallback getPreferenceComparisonCallback() {
        return mPreferenceComparisonCallback;
    }

    public void setOnPreferenceTreeClickListener(OnPreferenceTreeClickListener listener) {
        mOnPreferenceTreeClickListener = listener;
    }

    public OnPreferenceTreeClickListener getOnPreferenceTreeClickListener() {
        return mOnPreferenceTreeClickListener;
    }

    public void setOnDisplayPreferenceDialogListener(OnDisplayPreferenceDialogListener listener) {
        mOnDisplayPreferenceDialogListener = listener;
    }

    public OnNavigateToScreenListener getOnNavigateToScreenListener() {
        return mOnNavigateToScreenListener;
    }

    @Nullable
    private List<OnActivityDestroyListener> mActivityDestroyListeners;

    void registerOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (mActivityDestroyListeners == null) {
                mActivityDestroyListeners = new ArrayList<>();
            }

            if (!mActivityDestroyListeners.contains(listener)) {
                mActivityDestroyListeners.add(listener);
            }
        }
    }

    void unregisterOnActivityDestroyListener(OnActivityDestroyListener listener) {
        synchronized (this) {
            if (mActivityDestroyListeners != null) {
                mActivityDestroyListeners.remove(listener);
            }
        }
    }
}
