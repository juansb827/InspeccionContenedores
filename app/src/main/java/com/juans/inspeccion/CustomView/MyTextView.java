package com.juans.inspeccion.CustomView;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;

/**
 * Created by Juan on 12/03/2015.
 */
public class MyTextView extends TextView {

    int mViewWidth;
    int mViewHeight;
    int mTextBaseline;

    public MyTextView(Context context) {
        super(context);
    }

    public MyTextView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public MyTextView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }




}
