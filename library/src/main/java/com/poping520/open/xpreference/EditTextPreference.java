package com.poping520.open.xpreference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

/**
 * @author WangKZ
 * @version 1.0.0
 * create on 2018/9/12 14:16
 */
public class EditTextPreference extends DialogPreference {

    private EditText mEditText;
    private String mText;
    private boolean mTextSet;

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

    }

    public EditTextPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public EditTextPreference(Context context, AttributeSet attrs) {
        this(context, attrs, R.attr.editTextPreferenceStyle);
    }

    public EditTextPreference(Context context) {
        this(context, null);
    }
}
