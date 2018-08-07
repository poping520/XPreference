package com.poping520.open.xpreference;


import android.support.v4.view.AccessibilityDelegateCompat;
import android.support.v4.view.accessibility.AccessibilityNodeInfoCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.RecyclerViewAccessibilityDelegate;
import android.view.View;

class PreferenceRecyclerViewAccessibilityDelegate extends RecyclerViewAccessibilityDelegate {

    private RecyclerView mRecyclerView;
    private AccessibilityDelegateCompat mDefaultItemDelegate = getItemDelegate();

    private AccessibilityDelegateCompat mItemDelegate = new AccessibilityDelegateCompat() {
        @Override
        public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfoCompat info) {
            mDefaultItemDelegate.onInitializeAccessibilityNodeInfo(host, info);
            int position = mRecyclerView.getChildAdapterPosition(host);

            RecyclerView.Adapter a = mRecyclerView.getAdapter();
            if (!(a instanceof PreferenceGroupAdapter)) {
                return;
            }

            PreferenceGroupAdapter adapter = (PreferenceGroupAdapter) a;
            Preference preference = adapter.getItem(position);

            if (preference != null) {
                preference.onInitializeAccessibilityNodeInfo(info);
            }
        }
    };

    PreferenceRecyclerViewAccessibilityDelegate(RecyclerView recyclerView) {
        super(recyclerView);
        mRecyclerView = recyclerView;
    }

    @Override
    public AccessibilityDelegateCompat getItemDelegate() {
        return mItemDelegate;
    }
}
