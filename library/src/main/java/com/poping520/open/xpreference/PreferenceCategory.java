package com.poping520.open.xpreference;

import android.content.Context;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat.CollectionItemInfoCompat;
import android.util.AttributeSet;

//completed
public class PreferenceCategory extends PreferenceGroup {

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceCategory(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceCategory(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.preferenceCategoryStyle);
    }

    public PreferenceCategory(Context context) {
        this(context, null);
    }

    @Override
    protected boolean onPrepareAddPreference(Preference preference) {
        if (preference instanceof PreferenceCategory) {
            throw new IllegalArgumentException("Cannot add a PreferenceCategory directly to a PreferenceCategory");
        }
        return super.onPrepareAddPreference(preference);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }

    @Override
    public boolean shouldDisableDependents() {
        return !super.isEnabled();
    }

    @Override
    public void onInitializeAccessibilityNodeInfo(AccessibilityNodeInfoCompat info) {
        super.onInitializeAccessibilityNodeInfo(info);

        CollectionItemInfoCompat existingItemInfo = info.getCollectionItemInfo();
        if (existingItemInfo == null) {
            return;
        }

        final CollectionItemInfoCompat newItemInfo = CollectionItemInfoCompat.obtain(
                existingItemInfo.getRowIndex(),
                existingItemInfo.getRowSpan(),
                existingItemInfo.getColumnIndex(),
                existingItemInfo.getColumnSpan(),
                true,
                existingItemInfo.isSelected());
        info.setCollectionItemInfo(newItemInfo);
    }
}
