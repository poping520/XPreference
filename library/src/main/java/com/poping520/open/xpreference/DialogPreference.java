package com.poping520.open.xpreference;


import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.CallSuper;
import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

public abstract class DialogPreference extends Preference {

    private CharSequence mDialogTitle;
    private CharSequence mDialogMessage;
    private Drawable mDialogIcon;
    private CharSequence mPositiveButtonText;
    private CharSequence mNegativeButtonText;
    private CharSequence mNeutralButtonText;
    private int mDialogLayoutResId;

    private AlertDialog mDialog;
    private AlertDialog.Builder mBuilder;
    private int mWhichButtonClicked;

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.DialogPreference, defStyleAttr, defStyleRes);
        mDialogTitle = a.getString(R.styleable.DialogPreference_android_dialogTitle);
        if (mDialogTitle == null) mDialogTitle = getTitle();
        mDialogMessage = a.getString(R.styleable.DialogPreference_android_dialogMessage);
        mDialogIcon = a.getDrawable(R.styleable.DialogPreference_android_dialogIcon);
        mPositiveButtonText = a.getString(R.styleable.DialogPreference_android_positiveButtonText);
        mNegativeButtonText = a.getString(R.styleable.DialogPreference_android_negativeButtonText);
        mDialogLayoutResId = a.getResourceId(R.styleable.DialogPreference_android_dialogLayout, 0);
        mNeutralButtonText = a.getString(R.styleable.DialogPreference_neutralButtonText);
        a.recycle();
    }

    public DialogPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public DialogPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.dialogPreferenceStyle);
    }

    public DialogPreference(Context context) {
        this(context, null);
    }

    public void setDialogTitle(CharSequence dialogTitle) {
        mDialogTitle = dialogTitle;
    }

    public void setDialogTitle(int dialogTitleResId) {
        setDialogTitle(mContext.getString(dialogTitleResId));
    }

    public CharSequence getDialogTitle() {
        return mDialogTitle;
    }

    public void setDialogMessage(CharSequence dialogMessage) {
        mDialogMessage = dialogMessage;
    }

    public void setDialogMessage(int dialogMessageResId) {
        setDialogMessage(mContext.getString(dialogMessageResId));
    }

    public CharSequence getDialogMessage() {
        return mDialogMessage;
    }

    public void setDialogIcon(Drawable dialogIcon) {
        mDialogIcon = dialogIcon;
    }

    public void setDialogIcon(@DrawableRes int dialogIconRes) {
        mDialogIcon = ContextCompat.getDrawable(mContext, dialogIconRes);
    }

    public Drawable getDialogIcon() {
        return mDialogIcon;
    }

    public void setPositiveButtonText(CharSequence positiveButtonText) {
        mPositiveButtonText = positiveButtonText;
    }

    public void setPositiveButtonText(@StringRes int positiveButtonTextResId) {
        setPositiveButtonText(mContext.getString(positiveButtonTextResId));
    }

    public CharSequence getPositiveButtonText() {
        return mPositiveButtonText;
    }

    public void setNegativeButtonText(CharSequence negativeButtonText) {
        mNegativeButtonText = negativeButtonText;
    }

    public void setNegativeButtonText(@StringRes int negativeButtonTextResId) {
        setNegativeButtonText(mContext.getString(negativeButtonTextResId));
    }

    public CharSequence getNegativeButtonText() {
        return mNegativeButtonText;
    }

    public void setNeutralButtonText(CharSequence neutralButtonText) {
        mNeutralButtonText = neutralButtonText;
    }

    public void setNeutralButtonText(@StringRes int neutralButtonTextResId) {
        setNeutralButtonText(mContext.getString(neutralButtonTextResId));
    }

    public CharSequence getNeutralButtonText() {
        return mNeutralButtonText;
    }

    public void setDialogLayoutResource(int dialogLayoutResId) {
        mDialogLayoutResId = dialogLayoutResId;
    }

    public int getDialogLayoutResource() {
        return mDialogLayoutResId;
    }

    protected void onPrepareDialogBuilder(AlertDialog.Builder builder) {
    }

    @Override
    protected void onClick() {
        if (mDialog != null && mDialog.isShowing()) return;

        showDialog(null);
    }

    protected void showDialog(Bundle state) {
        mWhichButtonClicked = DialogInterface.BUTTON_NEGATIVE;
        mBuilder = new AlertDialog.Builder(mContext)
                .setTitle(mDialogTitle)
                .setIcon(mDialogIcon)
                .setPositiveButton(mPositiveButtonText, this::onClick)
                .setNegativeButton(mNegativeButtonText, this::onClick);

        if (!TextUtils.isEmpty(mNeutralButtonText)) {
            mBuilder.setNeutralButton(mNeutralButtonText, this::onClick);
        }

        View contentView = onCreateDialogView();
        if (contentView != null) {
            onBindDialogView(contentView);
            mBuilder.setView(contentView);
        } else {
            mBuilder.setMessage(mDialogMessage);
        }

        onPrepareDialogBuilder(mBuilder);
        mPreferenceManager.registerOnActivityDestroyListener(this::onActivityDestroy);

        // Create the dialog
        final Dialog dialog = mDialog = mBuilder.create();

        if (state != null) {
            dialog.onRestoreInstanceState(state);
        }
        if (needInputMethod()) {
            requestInputMethod(dialog);
        }
        dialog.setOnDismissListener(this::onDismiss);
        dialog.show();

        onDialogShowed(mDialog);
    }

    protected void onDialogShowed(AlertDialog dialog) {

    }

    protected boolean needInputMethod() {
        return false;
    }

    private void requestInputMethod(Dialog dialog) {
        Window window = dialog.getWindow();
        if (window != null) {
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    protected View onCreateDialogView() {
        if (mDialogLayoutResId == 0) {
            return null;
        }

        LayoutInflater inflater = LayoutInflater.from(mContext);
        return inflater.inflate(mDialogLayoutResId, null);
    }

    @CallSuper
    protected void onBindDialogView(View view) {
        View dialogMessageView = view.findViewById(R.id.message);
        if (dialogMessageView != null) {
            final CharSequence message = getDialogMessage();
            int newVisibility = View.GONE;

            if (!TextUtils.isEmpty(message)) {
                if (dialogMessageView instanceof TextView) {
                    ((TextView) dialogMessageView).setText(message);
                }

                newVisibility = View.VISIBLE;
            }

            if (dialogMessageView.getVisibility() != newVisibility) {
                dialogMessageView.setVisibility(newVisibility);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        mWhichButtonClicked = which;
    }

    public void onDismiss(DialogInterface dialog) {

        mPreferenceManager.unregisterOnActivityDestroyListener(this::onActivityDestroy);
        mDialog = null;
        onDialogClosed(mWhichButtonClicked == DialogInterface.BUTTON_POSITIVE);
    }

    protected void onDialogClosed(boolean positiveResult) {
    }

    public Dialog getDialog() {
        return mDialog;
    }

    public void onActivityDestroy() {
        if (mDialog == null || !mDialog.isShowing()) {
            return;
        }
        mDialog.dismiss();
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        final Parcelable superState = super.onSaveInstanceState();
        if (mDialog == null || !mDialog.isShowing()) {
            return superState;
        }

        final SavedState myState = new SavedState(superState);
        myState.isDialogShowing = true;
        myState.dialogBundle = mDialog.onSaveInstanceState();
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
        if (myState.isDialogShowing) {
            showDialog(myState.dialogBundle);
        }
    }

    private static class SavedState extends BaseSavedState {
        boolean isDialogShowing;
        Bundle dialogBundle;

        public SavedState(Parcel source) {
            super(source);
            isDialogShowing = source.readInt() == 1;
            dialogBundle = source.readBundle();
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            super.writeToParcel(dest, flags);
            dest.writeInt(isDialogShowing ? 1 : 0);
            dest.writeBundle(dialogBundle);
        }

        public SavedState(Parcelable superState) {
            super(superState);
        }

        public static final Parcelable.Creator<SavedState> CREATOR =
                new Parcelable.Creator<SavedState>() {
                    public SavedState createFromParcel(Parcel in) {
                        return new SavedState(in);
                    }

                    public SavedState[] newArray(int size) {
                        return new SavedState[size];
                    }
                };
    }
}
