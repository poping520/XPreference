package com.poping520.open.xpreference;

import android.content.Context;
import android.util.AttributeSet;


public class PreferenceGroup extends Preference {

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public PreferenceGroup(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public PreferenceGroup(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public void addItemFromInflater(Preference item) {

    }
}
