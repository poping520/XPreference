package com.poping520.open.xpreference;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.v4.util.SimpleArrayMap;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class PreferenceGroup extends Preference {

    private List<Preference> mList;

    private boolean mOrderingAsAdded;

    private int mCurrentOrder = -1;

    private boolean mAttachedToHierarchy = false;

    private final SimpleArrayMap<String, Long> mIdRecycleCache = new SimpleArrayMap<>();

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

    public void addItemFromInflater(Preference preference) {
        addPreference(preference);
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

        prepareAddPreference(preference);

        synchronized (this) {
            mList.add(index, preference);
        }

        String key = preference.getKey();
        long id;
        if (key != null && mIdRecycleCache.containsKey(key)) {
            id = mIdRecycleCache.get(key);
            mIdRecycleCache.remove(key);
        } else {
            id = preferenceManager.getNextId();
        }
        preference.onAttachedToHierarchy(preferenceManager, id);
        preference.assignParent(this);

        if (mAttachedToHierarchy) preference.onAttached();

        notifyHierarchyChanged();
        return true;
    }

    @Override
    public void onAttached() {
        super.onAttached();
        mAttachedToHierarchy = true;

        for (Preference p : mList) {
            p.onAttached();
        }
    }

    protected void prepareAddPreference(Preference preference) {

    }

    protected boolean isOnSameScreenAsChildren() {
        return true;
    }

    public void setOrderingAsAdded(boolean orderingAsAdded) {
        mOrderingAsAdded = orderingAsAdded;
    }

    void sortPreferences() {
        synchronized (this) {
            Collections.sort(mList);
        }
    }

    public int getPreferenceCount() {
        return mList.size();
    }

    List<Preference> getPreferenceList() {
        return mList;
    }
}
