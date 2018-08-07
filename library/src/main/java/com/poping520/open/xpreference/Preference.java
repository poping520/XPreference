package com.poping520.open.xpreference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.AbsSavedState;
import android.view.View;
import android.widget.TextView;

import com.poping520.open.xpreference.PreferenceGroupAdapter.PreferenceViewHolder;
import com.poping520.open.xpreference.storage.Storage;

import java.util.List;


public class Preference implements Comparable<Preference> {

    private static final String TAG = "Preference";

    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    private Context mContext;

    protected PreferenceManager preferenceManager;

    private Storage mStorage;

    private boolean mVisible = true;

    private boolean mWasDetached;
    private boolean mBaseMethodCalled;

    private int mViewId;

    private long mId;
    private boolean mHasId;


    protected int iconResId;
    protected String key;
    protected CharSequence title;
    protected String summary;
    protected int layoutResId;
    protected boolean persistent;
    private String mDependencyKey;
    private String mFragment;
    private boolean mEnable;


    private Object defaultValue;
    private int mOrder;


    private boolean mHasSingleLineTitleAttr;
    private boolean mSingleLineTitle = true;

    private PreferenceGroup mParentGroup;

    private Intent mIntent;
    private Bundle mExtras;

    private List<Preference> mDependents;


    private OnPreferenceChangeInternalListener mListener;
    private int mWidgetLayoutResource;

    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {

    }

    public void onPrepareForRemoval() {
        unregisterDependency();
    }

    private void unregisterDependency() {
        if (mDependencyKey != null) {
            Preference old = findPreferenceInHierarchy(mDependencyKey);
            if (old != null) old.unregisterDependent(this);
        }
    }

    protected Preference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || preferenceManager == null)
            return null;
        return preferenceManager.getPreferenceScreen().findPreference(key);
    }

    private void unregisterDependent(Preference dependent) {
        if (mDependents != null) {
            mDependents.remove(dependent);
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public boolean isEnabled() {
        return mEnable;
    }

    public void onParentChanged(PreferenceGroup preferenceGroup, boolean b) {

    }

    public void notifyHierarchyChanged(boolean disableDependents) {

    }

    void dispatchSaveInstanceState(Bundle container) {

    }

    @Nullable
    public PreferenceGroup getParent() {
        return mParentGroup;
    }

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
        mFragment = a.getString(R.styleable.Preference_android_fragment);
        mDependencyKey = a.getString(R.styleable.Preference_android_dependency);

        if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            defaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }

        mHasSingleLineTitleAttr = a.hasValue(R.styleable.Preference_singleLineTitle);
        if (mHasSingleLineTitleAttr) {
            mSingleLineTitle = a.getBoolean(R.styleable.Preference_singleLineTitle, true);
        }

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

    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.itemView.setOnClickListener(v -> performClick(v));
        holder.itemView.setId(mViewId);

        Log.e(TAG, "onBindViewHolder: ===" );

        TextView tv = holder.findViewById(R.id.title);
        if (tv != null) {
            if (!TextUtils.isEmpty(title)) {
                tv.setText(title);
                tv.setVisibility(View.VISIBLE);

                if (mHasSingleLineTitleAttr)
                    tv.setSingleLine(mSingleLineTitle);

            } else {
                tv.setVisibility(View.GONE);
            }
        }
    }


    void assignParent(@Nullable PreferenceGroup parentGroup) {
        mParentGroup = parentGroup;
    }

    public void onAttached() {
        registerDependency();
    }

    private void registerDependency() {
        if (TextUtils.isEmpty(mDependencyKey)) return;

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

    void clearWasDetached() {
        mWasDetached = true;
    }

    public void restoreHierarchyState(Bundle bundle) {
        dispatchRestoreInstanceState(bundle);
    }

    void dispatchRestoreInstanceState(Bundle bundle) {
        if (!hasKey()) return;
        Parcelable state = bundle.getParcelable(key);
        if (state != null) {
            mBaseMethodCalled = false;
            onRestoreInstanceState(state);
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
    }


    protected void performClick(View view) {


    }


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

    public Intent getIntent() {
        return mIntent;
    }

    public void setFragment(String fragment) {
        mFragment = fragment;
    }

    public String getFragment() {
        return mFragment;
    }

    public Bundle getExtras() {
        if (mExtras == null)
            mExtras = new Bundle();
        return mExtras;
    }

    public Bundle peekExtras() {
        return mExtras;
    }

    public void setLayoutResId(int layoutResId) {
        this.layoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return layoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResource) {
        mWidgetLayoutResource = widgetLayoutResource;
    }

    public int getWidgetLayoutResource() {
        return mWidgetLayoutResource;
    }


    //

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

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    public void setTitle(CharSequence title) {
        if ((title == null && this.title != null) || (title != null && !title.equals(this.title))) {
            this.title = title;
            notifyChanged();
        }
    }

    public void setTitle(@StringRes int resId) {
        setTitle(mContext.getString(resId));
    }

    public CharSequence getTitle() {
        return title;
    }

    private void notifyChanged() {

    }

    public boolean isVisible() {
        return mVisible;
    }

    public PreferenceManager getPreferenceManager() {
        return preferenceManager;
    }

    public long getId() {
        return mId;
    }

    public static class BaseSavedState extends AbsSavedState {
        public BaseSavedState(Parcel source) {
            super(source);
        }

        public BaseSavedState(Parcelable superState) {
            super(superState);
        }

        public static final Creator<BaseSavedState> CREATOR = new Creator<BaseSavedState>() {
            @Override
            public BaseSavedState createFromParcel(Parcel in) {
                return new BaseSavedState(in);
            }

            @Override
            public BaseSavedState[] newArray(int size) {
                return new BaseSavedState[size];
            }
        };
    }
}
