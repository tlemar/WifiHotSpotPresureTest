package com.vivo.zhouchen.wifihotspotpresuretest.UIComponent;

import android.content.Context;
import android.support.design.widget.FloatingActionButton;
import android.util.AttributeSet;

/**
 * Created by zhouchen on 2016/1/2.
 */
public class StateFAB extends FloatingActionButton {
    private boolean mState  = true;

    public StateFAB(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public StateFAB(Context context) {
        super(context);
    }

    public StateFAB(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public synchronized void setActionState(boolean clickState){
        mState = clickState;
    }


    public boolean getActionState(){
        return mState;
    }


}
