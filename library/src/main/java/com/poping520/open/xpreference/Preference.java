package com.poping520.open.xpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
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
import com.poping520.open.xpreference.datastore.Storage;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Set;

public class Preference extends android.preference.Preference {

    protected Context mContext;
    protected PreferenceManager mPreferenceManager;

    private Storage mStorage;

    private boolean mVisible = true;
    private boolean mWasDetached;
    private boolean mBaseMethodCalled;

    private int mViewId;

    private long mId;
    private boolean mHasId;

    private int mIconResId;
    private Drawable mIcon;
    private boolean mIconSpaceReserved;
    private Object mDefaultValue;
    private boolean mHasSingleLineTitleAttr;
    private boolean mSingleLineTitle = true;
    private boolean mAllowDividerAbove;
    private boolean mAllowDividerBelow;

    private PreferenceGroup mParentGroup;

    private XPreferenceChangeInternalListener mListener;

    interface XPreferenceChangeInternalListener {

        void onPreferenceChange(Preference preference);

        void onPreferenceHierarchyChange(Preference preference);

        void onPreferenceVisibilityChange(Preference preference);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mContext = context;

        final TypedArray a = this.mContext.obtainStyledAttributes(attrs, R.styleable.Preference, defStyleAttr, defStyleRes);

        mIconResId = a.getResourceId(R.styleable.Preference_android_icon, 0);
        if (a.hasValue(R.styleable.Preference_android_defaultValue)) {
            mDefaultValue = onGetDefaultValue(a, R.styleable.Preference_android_defaultValue);
        }

        mIconSpaceReserved = a.getBoolean(R.styleable.Preference_android_iconSpaceReserved, false);

        mHasSingleLineTitleAttr = a.hasValue(R.styleable.Preference_singleLineTitle);
        if (mHasSingleLineTitleAttr) {
            mSingleLineTitle = a.getBoolean(R.styleable.Preference_singleLineTitle, true);
        }
        mAllowDividerAbove = a.getBoolean(R.styleable.Preference_allowDividerAbove, isSelectable());
        mAllowDividerBelow = a.getBoolean(R.styleable.Preference_allowDividerBelow, isSelectable());

        a.recycle();
    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Preference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceStyle);
    }

    public Preference(Context context) {
        this(context, null);
    }

    protected Preference findPreferenceInHierarchy(String key) {
        if (TextUtils.isEmpty(key) || mPreferenceManager == null)
            return null;
        return mPreferenceManager.getPreferenceScreen().findPreference(key);
    }

    @Override
    public void onParentChanged(android.preference.Preference parent, boolean disableChild) {
        super.onParentChanged(parent, disableChild);
    }

    public void notifyHierarchyChanged(boolean disableDependents) {
        if (mListener != null) {
            mListener.onPreferenceHierarchyChange(this);
        }
    }

    @Nullable
    public PreferenceGroup getXParent() {
        return mParentGroup;
    }


    void setOnPreferenceChangeInternalListener(PreferenceGroupAdapter listener) {
        mListener = listener;
    }

    protected void notifyChanged() {
        if (mListener != null) {
            mListener.onPreferenceChange(this);
        }
    }

    protected void notifyHierarchyChanged() {
        if (mListener != null) {
            mListener.onPreferenceHierarchyChange(this);
        }
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
        holder.itemView.setOnClickListener(this::performClick);
        holder.itemView.setId(mViewId);

        TextView tvTitle = holder.findViewById(R.id.title);
        if (tvTitle != null) {
            if (!TextUtils.isEmpty(getTitle())) {
                tvTitle.setText(getTitle());
                tvTitle.setVisibility(View.VISIBLE);

                if (mHasSingleLineTitleAttr)
                    tvTitle.setSingleLine(mSingleLineTitle);

            } else {
                tvTitle.setVisibility(View.GONE);
            }
        }

        TextView tvSummary = holder.findViewById(R.id.summary);
        if (tvSummary != null) {
            if (!TextUtils.isEmpty(getSummary())) {
                tvSummary.setText(getSummary());
                tvSummary.setVisibility(View.VISIBLE);
            } else {
                tvSummary.setVisibility(View.GONE);
            }
        }

        AppCompatImageView ivIcon = holder.findViewById(R.id.icon);

        if (ivIcon != null) {
            if (mIconResId != 0 || mIcon != null) {
                if (mIcon == null) {
                    mIcon = ContextCompat.getDrawable(getContext(), mIconResId);
                }
                if (mIcon != null) {
                    ivIcon.setImageDrawable(mIcon);
                }
            }
            if (mIcon != null) {
                ivIcon.setVisibility(View.VISIBLE);
            } else {
                ivIcon.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
            }
        }

        View imgFrame = holder.findViewById(R.id.icon_frame);
        if (imgFrame != null) {
            if (getIcon() != null)
                imgFrame.setVisibility(View.VISIBLE);
            else
                imgFrame.setVisibility(mIconSpaceReserved ? View.INVISIBLE : View.GONE);
        }
        if (getShouldDisableView())
            setEnabledStateOnViews(holder.itemView, isEnabled());
        else
            setEnabledStateOnViews(holder.itemView, true);

        holder.itemView.setFocusable(isSelectable());
        holder.itemView.setClickable(isSelectable());

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

    @Override
    protected void onPrepareForRemoval() {
        super.onPrepareForRemoval();
    }

    protected void onAttached() {
        super.onAttachedToActivity();
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
        if (!shouldPersist() || !mStorage.contains(getKey())) {
            if (mDefaultValue != null) {
                onSetInitialValue(false, mDefaultValue);
            }
        } else {
            onSetInitialValue(true, null);
        }
    }

    public void onDetached() {
        try {
            Method method = getClass().getDeclaredMethod("unregisterDependency");
            method.setAccessible(true);
            method.invoke(this);
            mWasDetached = true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    public final boolean wasDetached() {
        return mWasDetached;
    }

    void clearWasDetached() {
        mWasDetached = true;
    }

    protected void performClick(View view) {
        if (!isEnabled()) {
            return;
        }

        onClick();

        OnPreferenceClickListener clickListener = getOnPreferenceClickListener();
        if (clickListener != null && clickListener.onPreferenceClick(this)) {
            return;
        }

        if (mPreferenceManager != null) {
            PreferenceManager.OnPreferenceTreeClickListener listener = mPreferenceManager
                    .getOnPreferenceTreeClickListener();
            if (listener != null && listener.onPreferenceTreeClick(this)) {
                return;
            }
        }

        if (getIntent() != null) {
            mContext.startActivity(getIntent());
        }
    }

    protected boolean shouldPersist() {
        return isPersistent() && hasKey();
    }

    protected boolean persistInt(int value) {
        return shouldPersist() && (value == getPersistedInt(~value)
                || mStorage.saveInt(getKey(), value));
    }

    protected int getPersistedInt(int defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getInt(getKey(), defaultValue);
    }

    protected boolean persistLong(long value) {
        return shouldPersist() && (value == getPersistedLong(~value)
                || mStorage.saveLong(getKey(), value));
    }

    protected long getPersistedLong(long defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getLong(getKey(), defaultValue);
    }

    protected boolean persistFloat(float value) {
        return shouldPersist() && (value == getPersistedFloat(Float.NaN)
                || mStorage.saveFloat(getKey(), value));
    }

    protected float getPersistedFloat(float defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getFloat(getKey(), defaultValue);
    }

    protected boolean persistBoolean(boolean value) {
        return shouldPersist() && (value == getPersistedBoolean(!value)
                || mStorage.saveBoolean(getKey(), value));
    }

    protected boolean getPersistedBoolean(boolean defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getBoolean(getKey(), defaultValue);
    }

    protected boolean persistString(String value) {
        return shouldPersist() && (TextUtils.equals(value, getPersistedString(null))
                || mStorage.saveString(getKey(), value));
    }

    protected String getPersistedString(String defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getString(getKey(), defaultValue);
    }

    public boolean persistStringSet(Set<String> values) {
        return shouldPersist() && (values.equals(getPersistedStringSet(null))
                || mStorage.saveStringSet(getKey(), values));
    }

    public Set<String> getPersistedStringSet(Set<String> defaultValue) {
        if (!shouldPersist()) return defaultValue;
        return mStorage.getStringSet(getKey(), defaultValue);
    }

    public void setViewId(int viewId) {
        mViewId = viewId;
    }

    public void setIcon(Drawable icon) {
        if ((icon == null && mIcon != null) || (icon != null && mIcon != icon)) {
            mIcon = icon;
            mIconResId = 0;
            notifyChanged();
        }
    }

    public void setIcon(int iconResId) {
        setIcon(ContextCompat.getDrawable(mContext, iconResId));
        mIconResId = iconResId;
    }

    public Drawable getIcon() {
        if (mIcon == null && mIconResId != 0) {
            mIcon = ContextCompat.getDrawable(mContext, mIconResId);
        }
        return mIcon;
    }

    public PreferenceManager getXPreferenceManager() {
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

    public void saveHierarchyState(Bundle container) {
        dispatchSaveInstanceState(container);
    }

    void dispatchSaveInstanceState(Bundle container) {
        if (hasKey()) {
            mBaseMethodCalled = false;
            Parcelable state = onSaveInstanceState();
            if (!mBaseMethodCalled) {
                throw new IllegalStateException("Derived class did not call super.onSaveInstanceState()");
            }
            if (state != null) {
                container.putParcelable(getKey(), state);
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
        Parcelable state = bundle.getParcelable(getKey());
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

    @CallSuper
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {

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
