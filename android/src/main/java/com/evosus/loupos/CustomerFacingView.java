package com.evosus.loupos;

import android.content.Context;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.facebook.react.bridge.Promise;

public class CustomerFacingView extends LinearLayout {

    private TextView tv_name;
    private Button btn_play;
    private Button btn_delete;

    public CustomerFacingView(Context context, AttributeSet attrs){
        super(context, attrs);

        setOrientation(LinearLayout.HORIZONTAL);
        setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));

        tv_name = new TextView(context);
        addView(tv_name);

        btn_play = new Button(context);
        addView(btn_play);

        btn_delete = new Button(context);
        addView(btn_delete);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_main, container, false);
    }

    public void mySuperDuperFunction(Promise promise) {
        Log.i("CustomerFacingView", "I'm here!!!"); // <-- Magic
    }
}
