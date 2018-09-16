package com.poping520.open.xpreference.sample;

import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;

/**
 * Created by WangKZ on 18/09/16.
 *
 * @author poping520
 * @version 1.0.0
 */
public class Preference extends android.preference.Preference {



    public Preference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr);

    }

    public Preference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public Preference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Preference(Context context) {
        super(context);
    }


}
