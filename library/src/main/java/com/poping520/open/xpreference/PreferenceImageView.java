package com.poping520.open.xpreference;

import android.content.Context;
import android.content.res.TypedArray;

import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;

public class PreferenceImageView extends AppCompatImageView {

    private int mMaxWidth = Integer.MAX_VALUE;
    private int mMaxHeight = Integer.MAX_VALUE;

    public PreferenceImageView(Context context) {
        this(context, null);
    }

    public PreferenceImageView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PreferenceImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        final TypedArray a = context.obtainStyledAttributes(
                attrs, R.styleable.PreferenceImageView, defStyleAttr, 0);

        setMaxWidth(a.getDimensionPixelSize(
                R.styleable.PreferenceImageView_maxWidth, Integer.MAX_VALUE));

        setMaxHeight(a.getDimensionPixelSize(
                R.styleable.PreferenceImageView_maxHeight, Integer.MAX_VALUE));

        a.recycle();
    }

//    public PreferenceImageView(Context mContext, AttributeSet attrs, int defStyleAttr,
//            int defStyleRes) {
//        super(mContext, attrs, defStyleAttr, defStyleRes);
//    }

    @Override
    public void setMaxWidth(int maxWidth) {
        mMaxWidth = maxWidth;
        super.setMaxWidth(maxWidth);
    }

    @Override
    public int getMaxWidth() {
        return mMaxWidth;
    }

    @Override
    public void setMaxHeight(int maxHeight) {
        mMaxHeight = maxHeight;
        super.setMaxHeight(maxHeight);
    }

    @Override
    public int getMaxHeight() {
        return mMaxHeight;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int widthMode = MeasureSpec.getMode(widthMeasureSpec);
        if (widthMode == MeasureSpec.AT_MOST || widthMode == MeasureSpec.UNSPECIFIED) {
            final int widthSize = MeasureSpec.getSize(widthMeasureSpec);
            final int maxWidth = getMaxWidth();
            if (maxWidth != Integer.MAX_VALUE
                    && (maxWidth < widthSize || widthMode == MeasureSpec.UNSPECIFIED)) {
                widthMeasureSpec = MeasureSpec.makeMeasureSpec(maxWidth, MeasureSpec.AT_MOST);
            }
        }

        final int heightMode = MeasureSpec.getMode(heightMeasureSpec);
        if (heightMode == MeasureSpec.AT_MOST || heightMode == MeasureSpec.UNSPECIFIED) {
            final int heightSize = MeasureSpec.getSize(heightMeasureSpec);
            final int maxHeight = getMaxHeight();
            if (maxHeight != Integer.MAX_VALUE
                    && (maxHeight < heightSize || heightMode == MeasureSpec.UNSPECIFIED)) {
                heightMeasureSpec = MeasureSpec.makeMeasureSpec(maxHeight, MeasureSpec.AT_MOST);
            }
        }

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }
}
