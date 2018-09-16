package com.poping520.open.xpreference;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.AppCompatImageView;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.AbsSavedState;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.poping520.open.xpreference.PreferenceGroupAdapter.PreferenceViewHolder;
import com.poping520.open.xpreference.storage.Storage;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;


public class Preference implements Comparable<Preference> {

    private static final String TAG = "Preference";

    public static final int DEFAULT_ORDER = Integer.MAX_VALUE;

    protected Context mContext;
    protected PreferenceManager mPreferenceManager;

    private Storage mStorage;

    private boolean mDependencyMet = true;
    private boolean mParentDependencyMet = true;

    private boolean mVisible = true;
    private boolean mWasDetached;
    private boolean mBaseMethodCalled;

    private int mViewId;

    private long mId;
    private boolean mHasId;

    protected int iconResId;
    protected Drawable icon;
    protected String mKey;
    protected CharSequence title;
    protected CharSequence mSummary;
    protected int mLayoutResId;
    protected int mWidgetLayoutResId;
    protected boolean mPersistent;
    private String mDependencyKey;
    private String mFragment;
    private boolean mEnabled;
    private boolean mSelectable;

    private boolean mIconSpaceReserved;
    private boolean mShouldDisableView;

    private Object defaultValue;
    private int mOrder;

    private boolean mHasSingleLineTitleAttr;

    private boolean mSingleLineTitle = true;
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;

    private PreferenceGroup mParentGroup;

    private Intent mIntent;
    private Bundle mExtras;

    private List<Preference> mDependents;

    private OnPreferenceChangeListener mOnChangeListener;
    private OnPreferenceClickListener mOnClickListener;
    private OnPreferenceChangeInternalListener mListener;

    public interface OnPreferenceChangeListener {
        boolean onPreferenceChange(Preference preference, Object newValue);
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        this.mContext = context;

        final TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);

        iconResId = a.getResourceId(R.styleable.Preference_android_icon, 0);
        mKey = a.getString(R.styleable.Preference_android_key);
        title = a.getText(R.styleable.Preference_android_title);
        mSummary = a.getString(R.styleable.Preference_android_summary);
        mPersistent = a.getBoolean(R.styleable.Preference_android_persistent, true);
        mOrder = a.getInt(R.styleable.Preference_android_order, DEFAULT_ORDER);
        mFragment = a.getString(R.styleable.Preference_android_fragment);
        mLayoutResId = a.getResourceId(R.styleable.Preference_android_layout, R.layout.xpreference_material);
        mWidgetLayoutResId = a.getResourceId(R.styleable.Preference_android_widgetLayout, 0);
        mEnabled = a.getBoolean(R.styleable.Preference_android_enabled, true);
        mSelectable = a.getBoolean(R.styleable.Preference_android_selectable, true);
        mDependencyKey = a.getString(R.styleable.Preference_android_dependency);


        if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            defaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }

        mHasSingleLineTitleAttr = a.hasValue(R.styleable.Preference_singleLineTitle);
        if (mHasSingleLineTitleAttr) {
            mSingleLineTitle = a.getBoolean(R.styleable.Preference_singleLineTitle, true);
        }
        mAllowDividerAbove = a.getBoolean(R.styleable.Preference_allowDividerAbove, mSelectable);
        mAllowDividerBelow = a.getBoolean(R.styleable.Preference_allowDividerBelow, mSelectable);

        mShouldDisableView = a.getBoolean(R.styleable.Preference_android_shouldDisableView, true);
        mIconSpaceReserved = a.getBoolean(R.styleable.Preference_android_iconSpaceReserved, false);

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
        if (TextUtils.isEmpty(key) || mPreferenceManager == null)
            return null;
        return mPreferenceManager.getPreferenceScreen().findPreference(key);
    }

    private void registerDependent(Preference dependent) {
        if (mDependents == null) {
            mDependents = new ArrayList<Preference>();
        }
        mDependents.add(dependent);
        dependent.onDependencyChanged(this, shouldDisableDependents());
    }

    private void unregisterDependent(Preference dependent) {
        if (mDependents != null) {
            mDependents.remove(dependent);
        }
    }

    public void notifyDependencyChange(boolean disableDependents) {
        final List<Preference> dependents = mDependents;

        if (dependents == null) return;

        final int count = dependents.size();
        for (int i = 0; i < count; i++) {
            dependents.get(i).onDependencyChanged(this, disableDependents);
        }
    }

    public void onDependencyChanged(Preference dependency, boolean disableDependent) {
        if (mDependencyMet == disableDependent) {
            mDependencyMet = !disableDependent;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean shouldDisableDependents() {
        return !isEnabled();
    }

    public void onParentChanged(PreferenceGroup parent, boolean disableChild) {
        if (mParentDependencyMet == disableChild) {
            mParentDependencyMet = !disableChild;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public void notifyHierarchyChanged(boolean disableDependents) {

    }

    void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                container.putParcelable(mKey, state);
            }
        }
    }

    protected Parcelable onSaveInstanceState() {
        mBaseMethodCalled = true;
        return BaseSavedState.EMPTY_STATE;
    }

    public void restoreHierarchyState(Bundle container) {
        dispatchRestoreInstanceState(container);
    }

    void dispatchRestoreInstanceState(Bundle bundle) {
        if (!hasKey()) return;
        Parcelable state = bundle.getParcelable(mKey);
        if (state != null) {
            mBaseMethodCalled = false;
            onRestoreInstanceState(state);
            if (!mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onRestoreInstanceState()");
            }
        }
    }

    protected void onRestoreInstanceState(Parcelable state) {
        mBaseMethodCalled = true;
        if (state != BaseSavedState.EMPTY_STATE && state != null) {
            throw new IllegalArgumentException("Wrong state class -- expecting Preference State");
        }
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

    /**
     * different Preference maybe has different value type.
     * so, if your sub Preference has default value, you should
     * overwrite this method to confirm the value type which it hasEntries.
     */
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return null;
    }

    public void onBindViewHolder(PreferenceViewHolder holder) {
        holder.itemView.setOnClickListener(v -> performClick(v));
        holder.itemView.setId(mViewId);

        TextView tvTitle = holder.findViewById(R.id.title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(title)) {
                tvTitle.setText(title);
                tvTitle.setVisibility(View.VISIBLE);

                if (mHasSingleLineTitleAttr)
                    tvTitle.setSingleLine(mSingleLineTitle);

            } else {
                tvTitle.setVisibility(View.GONE);
            }
        }

        TextView tvSummary = holder.findViewById(R.id.summary);
        if (tvSummary != null) {
            if (!TextUtils.isEmpty(mSummary)) {
                tvSummary.setText(mSummary);
                tvSummary.setVisibility(View.VISIBLE);
            } else {
                tvSummary.setVisibility(View.GONE);
            }
        }

        AppCompatImageView ivIcon = holder.findViewById(R.id.icon);
        if (ivIcon != null) {
            if (iconResId != 0 || icon != null) {
                if (icon == null)
                    icon = ContextCompat.getDrawable(mContext, iconResId);
                if (icon != null)
                    ivIcon.setImageDrawable(icon);
            }
            if (icon != null)
                ivIcon.setVisibility(View.VISIBLE);
            else
                ivIcon.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
        }

        View imgFrame = holder.findViewById(R.id.icon_frame);
        if (imgFrame != null) {
            if (icon != null)
                imgFrame.setVisibility(View.VISIBLE);
            else
                imgFrame.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
        }
        if (mShouldDisableView)
            setEnabledStateOnViews(holder.itemView, isEnabled());
        else
            setEnabledStateOnViews(holder.itemView, true);

        holder.itemView.setFocusable(mSelectable);
        holder.itemView.setClickable(mSelectable);

        holder.setDividerAllowedAbove(mAllowDividerAbove);
        holder.setDividerAllowedBelow(mAllowDividerBelow);
    }

    private void setEnabledStateOnViews(View v, boolean enabled) {
        v.setEnabled(enabled);
        if (v instanceof ViewGroup) {
            ViewGroup viewGroup = (ViewGroup) v;
            int childCount = viewGroup.getChildCount();
            for (int i = childCount - 1; i >= 0; i--) {
                setEnabledStateOnViews(viewGroup.getChildAt(i), enabled);
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
        return !TextUtils.isEmpty(mKey);
    }

    public void onAttachedToHierarchy(PreferenceManager preferenceManager) {
        this.mPreferenceManager = preferenceManager;
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
        if (!shouldPersist() || !mStorage.contains(mKey)) {
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

    protected void performClick(View view) {
        if (!isEnabled()) {
            return;
        }

        onClick();

        if (mOnClickListener != null && mOnClickListener.onPreferenceClick(this)) {
            return;
        }

        if (mPreferenceManager != null) {
            PreferenceManager.OnPreferenceTreeClickListener listener = mPreferenceManager
                    .getOnPreferenceTreeClickListener();
            if (listener != null && listener.onPreferenceTreeClick(this)) {
                return;
            }
        }

        if (mIntent != null) {
            mContext.startActivity(mIntent);
        }
    }

    protected void onClick() {
    }

    public boolean callChangeListener(Object newValue) {
        return mOnChangeListener == null || mOnChangeListener.onPreferenceChange(this, newValue);
    }

    public void setPersistent(boolean persistent) {
        mPersistent = persistent;
    }

    public boolean isPersistent() {
        return mPersistent;
    }

    private boolean shouldPersist() {
        return mPersistent && hasKey();
    }

    protected boolean persistInt(int value) {
        return shouldPersist() && (value == getPersistedInt(~value)
                || mStorage.saveInt(mKey, value));
    }

    protected int getPersistedInt(int defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getInt(mKey, defaultValue);
    }

    protected boolean persistLong(long value) {
        return shouldPersist() && (value == getPersistedLong(~value)
                || mStorage.saveLong(mKey, value));
    }

    protected long getPersistedLong(long defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getLong(mKey, defaultValue);
    }

    protected boolean persistFloat(float value) {
        return shouldPersist() && (value == getPersistedFloat(Float.NaN)
                || mStorage.saveFloat(mKey, value));
    }

    protected float getPersistedFloat(float defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getFloat(mKey, defaultValue);
    }

    protected boolean persistBoolean(boolean value) {
        return shouldPersist() && (value == getPersistedBoolean(!value)
                || mStorage.saveBoolean(mKey, value));
    }

    protected boolean getPersistedBoolean(boolean defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getBoolean(mKey, defaultValue);
    }

    protected boolean persistString(String value) {
        return shouldPersist() && (TextUtils.equals(value, getPersistedString(null))
                || mStorage.saveString(mKey, value));
    }

    protected String getPersistedString(String defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getString(mKey, defaultValue);
    }

    protected boolean persistStringSet(Set<String> values) {
        return shouldPersist() && (values.equals(getPersistedStringSet(null))
                || mStorage.saveStringSet(mKey, values));

    }

    protected Set<String> getPersistedStringSet(Set<String> defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getStringSet(mKey, defaultValue);
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
        this.mLayoutResId = layoutResId;
    }

    public int getLayoutResource() {
        return mLayoutResId;
    }

    public void setWidgetLayoutResource(int widgetLayoutResId) {
        mWidgetLayoutResId = widgetLayoutResId;
    }

    public int getWidgetLayoutResource() {
        return mWidgetLayoutResId;
    }


    public String getKey() {
        return mKey;
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

    public void setSummary(CharSequence summary) {
        if ((summary == null && mSummary != null)
                || (summary != null && !summary.equals(mSummary))) {
            mSummary = summary;
            notifyChanged();
        }
    }

    public CharSequence getSummary() {
        return mSummary;
    }

    protected void notifyChanged() {
        if (mListener != null) {
            mListener.onPreferenceChange(this);
        }
    }

    public PreferenceManager getPreferenceManager() {
        return mPreferenceManager;
    }

    public long getId() {
        return mId;
    }

    public void setVisible(boolean visible) {
        if (mVisible != visible) {
            mVisible = visible;
            if (mListener != null) {
                mListener.onPreferenceVisibilityChange(this);
            }
        }
    }

    public boolean isVisible() {
        return mVisible;
    }

    public void setEnabled(boolean enabled) {
        if (mEnabled != enabled) {
            mEnabled = enabled;
            notifyDependencyChange(shouldDisableDependents());
            notifyChanged();
        }
    }

    public boolean isEnabled() {
        return mEnabled && mDependencyMet && mParentDependencyMet;
    }

    public void setSelectable(boolean selectable) {
        if (mSelectable != selectable) {
            mSelectable = selectable;
            notifyChanged();
        }
    }

    public boolean isSelectable() {
        return mSelectable;
    }

    public void setShouldDisableView(boolean shouldDisableView) {
        mShouldDisableView = shouldDisableView;
        notifyChanged();
    }

    public boolean isShouldDisableView() {
        return mShouldDisableView;
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
