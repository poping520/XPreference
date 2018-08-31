package com.poping520.open.xpreference;


import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.Checkable;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;

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

        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.SwitchPreference, defStyleAttr, defStyleRes);
        setSummaryOn(a.getString(R.styleable.SwitchPreference_android_summaryOn));
        setSummaryOff(a.getString(R.styleable.SwitchPreference_android_summaryOff));

        a.recycle();
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

    @Override
    public void onBindViewHolder(PreferenceGroupAdapter.PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);
        View view = holder.findViewById(R.id.switch_widget);
        syncSwitchView(view);
        syncSummaryView(holder);
    }

    public void setSwitchTextOn(CharSequence onText) {
        mSwitchOn = onText;
        notifyChanged();
    }

    public void setSwitchTextOff(CharSequence offText) {
        mSwitchOff = offText;
        notifyChanged();
    }

    public void setSwitchTextOn(int resId) {
        setSwitchTextOn(mContext.getString(resId));
    }

    public void setSwitchTextOff(int resId) {
        setSwitchTextOff(mContext.getString(resId));
    }

    public CharSequence getSwitchTextOn() {
        return mSwitchOn;
    }

    public CharSequence getSwitchTextOff() {
        return mSwitchOff;
    }

    @Override
    protected void performClick(View view) {
        super.performClick(view);
        syncViewIfAccessibilityEnabled(view);
    }

    private void syncViewIfAccessibilityEnabled(View view) {
        AccessibilityManager mgr =
                (AccessibilityManager) mContext.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (mgr == null || !mgr.isEnabled()) return;

        View switchView = view.findViewById(R.id.switch_widget);
        syncSwitchView(switchView);

        View summaryView = view.findViewById(R.id.summary);
        syncSummaryView(summaryView);
    }

    private void syncSwitchView(View view) {
        if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
            switchView.setOnCheckedChangeListener(null);
        }
        if (view instanceof Checkable) {
            ((Checkable) view).setChecked(mChecked);
        }
        if (view instanceof Switch) {
            final Switch switchView = (Switch) view;
            switchView.setTextOn(mSwitchOn);
            switchView.setTextOff(mSwitchOff);
            switchView.setOnCheckedChangeListener(mListener);
        }
    }
}
