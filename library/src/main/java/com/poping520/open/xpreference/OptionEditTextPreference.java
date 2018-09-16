package com.poping520.open.xpreference;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListAdapter;
import android.widget.ListPopupWindow;

import java.util.Random;

/**
 * Created by WangKZ on 18/09/15.
 *
 * @author poping520
 * @version 1.0.0
 */
public class OptionEditTextPreference extends EditTextPreference {

    private String[] mEntries;
    private String[] mEntryValues;
    private String mRandomButtonText;

    public OptionEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.OptionEditTextPreference, defStyleAttr, defStyleRes);

        int keysResId = a.getResourceId(R.styleable.OptionEditTextPreference_entries, 0);
        if (keysResId != 0)
            mEntries = a.getResources().getStringArray(keysResId);

        int valuesResId = a.getResourceId(R.styleable.OptionEditTextPreference_entryValues, 0);
        if (valuesResId != 0)
            mEntryValues = a.getResources().getStringArray(valuesResId);

        mRandomButtonText = a.getString(R.styleable.OptionEditTextPreference_randomButtonText);
        if (!TextUtils.isEmpty(mRandomButtonText))
            setNeutralButtonText(mRandomButtonText);

        a.recycle();

        if (mEntries != null) {
            if (mEntryValues == null)
                throw new IllegalStateException("the entryValues object hasEntries NULL");
            if (mEntries.length != mEntryValues.length)
                throw new IllegalStateException("entries length not same as entryValues length");
        }
    }

    public OptionEditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public OptionEditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.optionEditTextPreferenceStyle);
    }

    public OptionEditTextPreference(Context context) {
        this(context, null);
    }

    @Override
    protected void onAddEditTextToDialogView(View dialogView, EditText editText) {
        super.onAddEditTextToDialogView(dialogView, editText);

        ImageButton iBtn = dialogView.findViewById(R.id.imageButton);

        // set Ripple
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            int id = android.R.attr.selectableItemBackgroundBorderless;
            TypedValue v = new TypedValue();
            mContext.getTheme().resolveAttribute(id, v, true);
            TypedArray a = mContext.obtainStyledAttributes(v.resourceId, new int[]{id});
            iBtn.setBackground(a.getDrawable(0));
            a.recycle();
        }

        if (hasEntries()) {

            ListPopupWindow listPopup = new ListPopupWindow(mContext);

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    mContext, R.layout.xpreference_simple_list_item_1, mEntries);
            listPopup.setAdapter(adapter);
            listPopup.setAnchorView(iBtn);
            //设置为true响应物理键(点返回键popupWindow消失)
            listPopup.setModal(true);
            listPopup.setHeight(ListPopupWindow.WRAP_CONTENT);
            listPopup.setWidth(ListPopupWindow.WRAP_CONTENT);

            DisplayMetrics dm = mContext.getResources().getDisplayMetrics();
            int dp_100 = (int) (dm.density * 100);
            listPopup.setContentWidth(measureAdapterContentWidth(adapter, dp_100));

            iBtn.setOnClickListener(v -> listPopup.show());

            listPopup.setOnItemClickListener((parent, view, position, id) -> {
                getEditText().setText(mEntryValues[position]);
                listPopup.dismiss();
            });
        }
    }

    @Override
    protected void onDialogShowed(AlertDialog dialog) {
        super.onDialogShowed(dialog);

        if (!hasEntries()) return;

        if (!TextUtils.isEmpty(mRandomButtonText)) {
            dialog.getButton(DialogInterface.BUTTON_NEUTRAL).setOnClickListener(v -> {
                int next = RandomInt.next(mEntries.length);
                getEditText().setText(mEntryValues[next]);
            });
        }
    }

    public boolean hasEntries() {
        return mEntries != null && mEntries.length > 0;
    }

    //根据适配器的内容来计算 View 所需的宽度(取最长的一个)
    private int measureAdapterContentWidth(ListAdapter adapter, int minWidth) {
        ViewGroup mMeasureParent = null;
        int maxWidth = 0;
        View itemView = null;
        int itemType = 0;
        int widthMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int heightMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        int count = adapter.getCount();
        for (int i = 0; i < count; i++) {
            int positionType = adapter.getItemViewType(i);
            if (positionType != itemType) {
                itemType = positionType;
                itemView = null;
            }
            if (mMeasureParent == null) {
                mMeasureParent = new FrameLayout(mContext);
            }
            itemView = adapter.getView(i, itemView, mMeasureParent);
            itemView.measure(widthMeasureSpec, heightMeasureSpec);
            int itemWidth = itemView.getMeasuredWidth();
            if (itemWidth > maxWidth) {
                maxWidth = itemWidth;
            }
        }
        if (minWidth > maxWidth) return minWidth;
        else return maxWidth;
    }

    private static class RandomInt {

        private static final Random sRandom = new Random();
        private static Integer sLastRandom;

        private static int next(int max) {
            if (max <= 1) return 0;

            int random;
            if (sLastRandom == null) sLastRandom = sRandom.nextInt(max);

            do random = sRandom.nextInt(max);
            while (random == sLastRandom);

            sLastRandom = random;
            return random;
        }
    }
}
