package com.poping520.open.xpreference;

import android.content.Context;
import android.util.AttributeSet;

public class PreferenceScreen extends PreferenceGroup {

    private boolean mShouldUseGeneratedIds = true;

    PreferenceScreen(Context context, AttributeSet attrs) {
        super(context, attrs, R.attr.preferenceScreenStyle);
    }

    protected void onClick() {
        if (getIntent() != null || getFragment() != null || getPreferenceCount() == 0)
            return;

        PreferenceManager.OnNavigateToScreenListener listener =
                getPreferenceManager().getOnNavigateToScreenListener();

        if (listener != null)
            listener.onNavigateToScreen(this);
    }

    @Override
    protected boolean isOnSameScreenAsChildren() {
        return false;
    }

    public boolean isShouldUseGeneratedIds() {
        return mShouldUseGeneratedIds;
    }

    public void setShouldUseGeneratedIds(boolean shouldUseGeneratedIds) {
        if (isAttached()) {
            throw new IllegalStateException(
                    "Cannot change the usage of generated IDs while attached to the preference hierarchy");
        }
        mShouldUseGeneratedIds = shouldUseGeneratedIds;
    }
}
