package com.poping520.open.xpreference;

import android.content.Context;
import android.util.AttributeSet;


public class PreferenceScreen extends PreferenceGroup {

    private boolean mShouldUseGeneratedIds = true;

    public PreferenceScreen(Context context, AttributeSet attrs) {
        super(context, attrs, 0);
    }

    protected void onClick() {

    }

    public boolean isShouldUseGeneratedIds() {
        return mShouldUseGeneratedIds;
    }
}
