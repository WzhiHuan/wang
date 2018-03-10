package com.wjy.wangjyandroid;

import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.wjy.wangjyandroid.timerStyle.BaseCountDownTimer;
import com.wjy.wangjyandroid.timerStyle.JDCountDownTimer;

import java.util.Date;

public class FloatActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_float);
//        setContentView(R.layout.drag_helper);

//        DragViewGroup dragGroup = (DragViewGroup) findViewById(R.id.dragGroup);
//        dragGroup.setDragRectRegionTop(200);
//        final View floatView = getLayoutInflater().inflate(R.layout.float_view, dragGroup, false);
//        dragGroup.setFloatView(floatView, 200, 200);
//        floatView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Toast.makeText(FloatActivity.this, "111", Toast.LENGTH_SHORT).show();
//            }
//        });

        DragView iv = (DragView) findViewById(R.id.floatView);
        iv.setDragRectRegionTop(120);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(FloatActivity.this, "响应", Toast.LENGTH_SHORT).show();
            }
        });

        JDCountDownTimer jdTimer = new JDCountDownTimer(this, 1000000, "HH:mm:ss", R.drawable.border);
//        JDCountDownTimer jdTimer = new JDCountDownTimer(this, 100000000, "HH:mm:ss", R.drawable.circle);
        jdTimer.setTimerPadding(10,40,10,40)//设置内间距
                .setTimerTextColor(Color.BLACK)//设置字体颜色
                .setTimerTextSize(60)//设置字体大小
                .setTimerGapColor(Color.BLUE);//设置间隔的颜色
        FrameLayout fl = (FrameLayout) findViewById(R.id.fl);
        TextView view = jdTimer.getmDateTv();
        view.setBackgroundColor(getResources().getColor(android.R.color.black));
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, Gravity.CENTER);
        fl.addView(view, params);
        jdTimer.start();
    }
}
