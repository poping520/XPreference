package com.poping520.open.xpreference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;

import com.poping520.open.xpreference.storage.Storage;


public class Preference implements Comparable<Preference> {

    private static final String TAG = "Preference";

    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    private Context mContext;

    protected PreferenceManager preferenceManager;

    private Storage mStorage;


    private long mId;
    private boolean mHasId;


    protected int iconResId;
    protected String key;
    protected CharSequence title;
    protected String summary;
    protected int layoutResId;
    protected boolean persistent;

    private Object defaultValue;
    private int mOrder;

    private PreferenceGroup mParentGroup;

    private Intent mIntent;
    private Bundle mExtras;


    private OnPreferenceChangeInternalListener mListener;
    private int mWidgetLayoutResource;




    public interface OnPreferenceClickListener {

        boolean onPreferenceClick(Preference preference);
    }

    interface OnPreferenceChangeInternalListener {
        /**
         * Called when this Preference has changed.
         *
         * @param preference This preference.
         */
        void onPreferenceChange(Preference preference);

        /**
         * Called when this group has added/removed {@link Preference}(s).
         *
         * @param preference This Preference.
         */
        void onPreferenceHierarchyChange(Preference preference);

        /**
         * Called when this preference has changed its visibility.
         *
         * @param preference This Preference.
         */
        void onPreferenceVisibilityChange(Preference preference);
    }

    void setOnPreferenceChangeInternalListener(PreferenceGroupAdapter listener) {
        mListener = listener;
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        mContext = context;

        final TypedArray a = mContext.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);

        iconResId = a.getResourceId(R.styleable.Preference_android_icon, 0);
        key = a.getString(R.styleable.Preference_android_key);
        title = a.getText(R.styleable.Preference_android_title);
        summary = a.getString(R.styleable.Preference_android_summary);
        layoutResId = a.getResourceId(R.styleable.Preference_android_layout, R.layout.xpreference_material);
        persistent = a.getBoolean(R.styleable.Preference_android_persistent, true);
        mOrder = a.getInt(R.styleable.Preference_android_order, DEFAULT_ORDER);

        if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            defaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }


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


    /**
     * different Preference maybe has different value type.
     * so, if your sub Preference has default value, you should
     * overwrite this method to confirm the value type which it is.
     */
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }


    void assignParent(@Nullable PreferenceGroup parentGroup) {
        mParentGroup = parentGroup;
    }

    public void onAttached() {

    }


    public boolean hasKey() {
        return !TextUtils.isEmpty(key);
    }

    private boolean shouldPersist() {
        return persistent && hasKey();
    }

    protected boolean persistString(String value) {
        if (!shouldPersist()) return false;

        if (TextUtils.equals(value, getPersistedString(null)))
            return true;
        return mStorage.saveString(key, value);
    }

    protected String getPersistedString(String defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getString(key, defaultValue);
    }

    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.preferenceManager = preferenceManager;
        mStorage = preferenceManager.getStorage();
        if (!mHasId) {
            mId = preferenceManager.getNextId();
        }
        dispatchSetInitialValue();
    }

    void onAttachedToHierarchy(PreferenceManager preferenceManager, long id) {
        mId = id;
        mHasId = true;
        onAttachedToHierarchy(preferenceManager);
        mHasId = false;
    }

    private void dispatchSetInitialValue() {
        if (!shouldPersist() || !mStorage.contains(key)) {
            if (defaultValue != null) {
                onSetInitialValue(false, defaultValue);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }

    protected void onSetInitialValue(boolean restorePersistedValue, Object defaultValue) {
    }

    protected void notifyHierarchyChanged() {
        if (mListener != null) mListener.onPreferenceHierarchyChange(this);
    }

    public void onDetached() {

    }

    @Override
    public String toString() {
        return "Preference{" +
                "key='" + key + '\'' +
                ", title='" + title + '\'' +
                ", summary='" + summary + '\'' +
                '}';
    }

    /**
     * Compares Preference objects based on order (if set), otherwise alphabetically on the titles.
     *
     * @param another The Preference to compare to this one.
     * @return 0 if the same; less than 0 if this Preference sorts ahead of <var>another</var>;
     * greater than 0 if this Preference sorts after <var>another</var>.
     */
    @Override
    public int compareTo(@NonNull Preference another) {
        if (mOrder != another.mOrder) {
            // Do order comparison
            return mOrder - another.mOrder;
        } else if (title == another.title) {
            // If titles are null or share same object comparison
            return 0;
        } else if (title == null) {
            return 1;
        } else if (another.title == null) {
            return -1;
        } else {
            // Do name comparison
            return title.toString().compareToIgnoreCase(another.title.toString());
        }
    }

    public void setIntent(Intent intent) {
        mIntent = intent;
    }

    public Bundle getExtras() {
        return mExtras;
    }


    public String getKey() {
        return key;
    }

    public void setOrder(int order) {
        if (order != mOrder) {
            mOrder = order;

            // Reorder list
            notifyHierarchyChanged();
        }
    }

    public int getOrder() {
        return mOrder;
    }

    public int getLayoutResource() {
        return layoutResId;
    }

    public int getWidgetLayoutResource() {
        return 0;
    }
}
