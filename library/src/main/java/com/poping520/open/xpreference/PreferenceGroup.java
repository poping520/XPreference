package com.poping520.open.xpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.util.SimpleArrayMap;
import android.text.TextUtils;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

//Completed
public class PreferenceGroup extends Preference {

    private List<Preference> mList;

    private boolean mOrderingAsAdded = true;

    private int mCurrentOrder = -1;

    private boolean mAttachedToHierarchy = false;

    private final SimpleArrayMap<String, Long> mIdRecycleCache = new SimpleArrayMap<>();

    private final Handler mHandler = new Handler();

    private final Runnable mClearRecycleCacheRunnable = () -> {
        synchronized (this) {
            mIdRecycleCache.clear();
        }
    };

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        mList = new ArrayList<>();
        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.PreferenceGroup, defStyleAttr, defStyleRes);
        mOrderingAsAdded = a.getBoolean(R.styleable.PreferenceGroup_android_orderingFromXml, true);
        a.recycle();
    }

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void setOrderingAsAdded(boolean orderingAsAdded) {
        mOrderingAsAdded = orderingAsAdded;
    }

    public boolean isOrderingAsAdded() {
        return mOrderingAsAdded;
    }

    public void addItemFromInflater(Preference preference) {
        addPreference(preference);
    }

    public int getPreferenceCount() {
        return mList.size();
    }

    public Preference getPreference(int index) {
        return mList.get(index);
    }

    public boolean addPreference(Preference preference) {
        if (mList.contains(preference)) return true;

        if (preference.getOrder() == DEFAULT_ORDER) {
            if (mOrderingAsAdded) preference.setOrder(++mCurrentOrder);

            if (preference instanceof PreferenceGroup)
                ((PreferenceGroup) preference).setOrderingAsAdded(mOrderingAsAdded);
        }

        int index = Collections.binarySearch(mList, preference);
        if (index < 0) index = -index - 1;

        if (!onPrepareAddPreference(preference)) return false;

        synchronized (this) {
            mList.add(index, preference);
        }

        String key = preference.getKey();
        long id;
        if (key != null && mIdRecycleCache.containsKey(key)) {
            id = mIdRecycleCache.get(key);
            mIdRecycleCache.remove(key);
        } else {
            id = mPreferenceManager.getNextId();
        }
        preference.onAttachedToHierarchy(mPreferenceManager, id);
        preference.assignParent(this);

        if (mAttachedToHierarchy) preference.onAttached();

        notifyHierarchyChanged();
        return true;
    }

    public boolean removePreference(Preference preference) {
        final boolean returnValue = removePreferenceInt(preference);
        notifyHierarchyChanged();
        return returnValue;
    }

    private boolean removePreferenceInt(Preference preference) {
        synchronized (this) {
            preference.onPrepareForRemoval();
            if (preference.getParent() == this) {
                preference.assignParent(null);
            }
            boolean success = mList.remove(preference);
            if (success) {
                // If this preference, or another preference with the same mKey, gets re-added
                // immediately, we want it to have the same id so that it can be correctly tracked
                // in the adapter by RecyclerView, to make it appear as if it has only been
                // seamlessly updated. If the preference is not re-added by the time the handler
                // runs, we take that as a signal that the preference will not be re-added soon
                // in which case it does not need to retain the same id.

                // If two (or more) preferences have the same (or null) mKey and both are removed
                // and then re-added, only one id will be recycled and the second (and later)
                // preferences will receive a newly generated id. This use pattern of the preference
                // API is strongly discouraged.
                final String key = preference.getKey();
                if (key != null) {
                    mIdRecycleCache.put(key, preference.getId());
                    mHandler.removeCallbacks(mClearRecycleCacheRunnable);
                    mHandler.post(mClearRecycleCacheRunnable);
                }
                if (mAttachedToHierarchy) {
                    preference.onDetached();
                }
            }

            return success;
        }
    }

    public void removeAll() {
        synchronized (this) {
            List<Preference> preferenceList = mList;
            for (int i = preferenceList.size() - 1; i >= 0; i--) {
                removePreferenceInt(preferenceList.get(0));
            }
        }
        notifyHierarchyChanged();
    }

    protected boolean onPrepareAddPreference(Preference preference) {
        preference.onParentChanged(this, shouldDisableDependents());
        return true;
    }

    public Preference findPreference(CharSequence key) {
        if (TextUtils.equals(getKey(), key)) {
            return this;
        }

        for (Preference p : mList) {
            if (p == null) continue;
            String curKey = p.getKey();
            if (curKey != null && TextUtils.equals(curKey, key))
                return p;
            if (p instanceof PreferenceGroup)
                return ((PreferenceGroup) p).findPreference(key);
        }
        return null;
    }

    protected boolean isOnSameScreenAsChildren() {
        return true;
    }

    boolean isAttached() {
        return mAttachedToHierarchy;
    }

    @Override
    public void onAttached() {
        super.onAttached();
        mAttachedToHierarchy = true;

        for (Preference p : mList)
            p.onAttached();
    }

    @Override
    public void onDetached() {
        super.onDetached();
        mAttachedToHierarchy = false;

        for (Preference p : mList)
            p.onDetached();
    }

    @Override
    public void notifyHierarchyChanged(boolean disableDependents) {
        super.notifyHierarchyChanged(disableDependents);

        for (Preference p : mList) {
            p.onParentChanged(this, disableDependents);
        }
    }

    void sortPreferences() {
        synchronized (this) {
            Collections.sort(mList);
        }
    }

    @Override
    protected void dispatchSaveInstanceState(Bundle container) {
        super.dispatchSaveInstanceState(container);

        // Dispatch to all contained preferences
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchSaveInstanceState(container);
        }
    }

    @Override
    protected void dispatchRestoreInstanceState(Bundle container) {
        super.dispatchRestoreInstanceState(container);

        // Dispatch to all contained preferences
        final int preferenceCount = getPreferenceCount();
        for (int i = 0; i < preferenceCount; i++) {
            getPreference(i).dispatchRestoreInstanceState(container);
        }
    }

    List<Preference> getPreferenceList() {
        return mList;
    }

    public interface PreferencePositionCallback {

        int getPreferenceAdapterPosition(String key);

        int getPreferenceAdapterPosition(Preference preference);
    }
}
