package com.wjy.wangjyandroid;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Toast;

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
    }
}
