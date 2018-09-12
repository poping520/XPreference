package com.poping520.open.xpreference;


import android.content.Context;
import android.content.res.TypedArray;
import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import com.poping520.open.xpreference.PreferenceGroupAdapter.PreferenceViewHolder;

public abstract class TwoStatePreference extends Preference {

    private CharSequence mSummaryOn;
    private CharSequence mSummaryOff;
    protected boolean mChecked;
    private boolean mCheckedSet;
    private boolean mDisableDependentsState;

    public TwoStatePreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public TwoStatePreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public TwoStatePreference(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public TwoStatePreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onClick() {
        super.onClick();

        final boolean newValue = !isChecked();
        if (callChangeListener(newValue)) {
            setChecked(newValue);
        }
    }

    public void setChecked(boolean checked) {
        final boolean changed = mChecked != checked;
        if (changed || !mCheckedSet) {
            mChecked = checked;
            mCheckedSet = true;
            persistBoolean(checked);
            if (changed) {
                notifyDependencyChange(shouldDisableDependents());
                notifyChanged();
            }
        }
    }

    public boolean isChecked() {
        return mChecked;
    }

    @Override
    public boolean shouldDisableDependents() {
        boolean shouldDisable = mDisableDependentsState == mChecked;
        return shouldDisable || super.shouldDisableDependents();
    }

    public void setSummaryOn(CharSequence summary) {
        mSummaryOn = summary;
        if (isChecked()) {
            notifyChanged();
        }
    }

    public void setSummaryOn(int summaryResId) {
        setSummaryOn(mContext.getString(summaryResId));
    }

    public CharSequence getSummaryOn() {
        return mSummaryOn;
    }

    public void setSummaryOff(CharSequence summary) {
        mSummaryOff = summary;
        if (!isChecked()) {
            notifyChanged();
        }
    }

    public void setSummaryOff(int summaryResId) {
        setSummaryOff(mContext.getString(summaryResId));
    }

    public CharSequence getSummaryOff() {
        return mSummaryOff;
    }

    public boolean getDisableDependentsState() {
        return mDisableDependentsState;
    }

    public void setDisableDependentsState(boolean disableDependentsState) {
        mDisableDependentsState = disableDependentsState;
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        return a.getBoolean(index, false);
    }

    @Override
    protected void onSetInitialValue(boolean restoreValue, Object defaultValue) {
        setChecked(restoreValue ? getPersistedBoolean(mChecked)
                : (Boolean) defaultValue);
    }

    protected void syncSummaryView(PreferenceViewHolder holder) {
        View view = holder.findViewById(android.R.id.summary);
        syncSummaryView(view);
    }

    protected void syncSummaryView(View view) {
        if (!(view instanceof TextView)) {
            return;
        }
        TextView tvSummary = (TextView) view;
        boolean useDefaultSummary = true;
        if (mChecked && !TextUtils.isEmpty(mSummaryOn)) {
            tvSummary.setText(mSummaryOn);
            useDefaultSummary = false;
        } else if (!mChecked && !TextUtils.isEmpty(mSummaryOff)) {
            tvSummary.setText(mSummaryOff);
            useDefaultSummary = false;
        }
        if (useDefaultSummary) {
            if (!TextUtils.isEmpty(mSummary)) {
                tvSummary.setText(mSummary);
                useDefaultSummary = false;
            }
        }
        int newVisibility = View.GONE;
        if (!useDefaultSummary) {
            newVisibility = View.VISIBLE;
        }
        if (newVisibility != tvSummary.getVisibility()) {
            tvSummary.setVisibility(newVisibility);
        }
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mPersistent) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.checked = isChecked();
        return myState;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state == null || !state.getClass().equals(SavedState.class)) {
            super.onRestoreInstanceState(state);
            return;
        }

        SavedState myState = (SavedState) state;
        super.onRestoreInstanceState(myState.getSuperState());
        setChecked(myState.checked);
    }

    static class SavedState extends BaseSavedState {
        boolean checked;

        SavedState(Parcelable superState) {
            super(superState);
        }

        SavedState(Parcel source) {
            super(source);
            checked = source.readInt() == 1;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(checked ? 1 : 0);
        }

        public static final Creator<SavedState> CREATOR = new Creator<SavedState>() {
            @Override
            public SavedState createFromParcel(Parcel in) {
                return new SavedState(in);
            }

            @Override
            public SavedState[] newArray(int size) {
                return new SavedState[size];
            }
        };
    }
}
