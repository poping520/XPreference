package com.poping520.open.xpreference;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class SwitchPreference extends TwoStatePreference {

    private CharSequence mSwitchOn;
    private CharSequence mSwitchOff;

    private OnCheckedChangeListener mListener = (buttonView, isChecked) -> {
        if (!callChangeListener(isChecked)) {
            buttonView.setChecked(!isChecked);
            return;
        }
        setChecked(isChecked);
    };

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public SwitchPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public SwitchPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.switchPreferenceStyle);
    }

    public SwitchPreference(Context context) {
        this(context, null);
    }
}
