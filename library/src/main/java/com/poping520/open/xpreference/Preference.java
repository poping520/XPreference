package com.poping520.open.xpreference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.poping520.open.xpreference.storage.DataManager;


public class Preference {

    private static final String TAG = "Preference";

    private Context mContext;

    private DataManager mDataManager;

    protected int iconResId;

    protected String key;

    protected String title;

    protected String summary;


    private Intent mIntent;
    private Bundle mExtras;

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);

        iconResId = a.getResourceId(R.styleable.Preference_android_icon, 0);
        key = a.getString(R.styleable.Preference_android_key);
        title = a.getString(R.styleable.Preference_android_title);
        summary = a.getString(R.styleable.Preference_android_summary);

        Log.e(TAG, "Preference: " + toString());
        a.recycle();
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public Preference(Context context) {
        this(context, null);
    }

    public boolean hasKey() {
        return !TextUtils.isEmpty(key);
    }

    public boolean shouldPersist() {
        return hasKey();
    }

    protected boolean persistString(String value) {
        if (!shouldPersist()) return false;

        if (TextUtils.equals(value, getPersistedString(null)))
            return true;
        return mDataManager.saveString(key, value);
    }

    protected String getPersistedString(String defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mDataManager.getString(key, defaultValue);
    }

    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {

    }

    @Override
    public String toString() {
        return "Preference{" +
                "key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public Bundle getExtras() {
        return mExtras;
    }
}
