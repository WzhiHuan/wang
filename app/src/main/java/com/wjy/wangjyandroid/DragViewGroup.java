package com.wjy.wangjyandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.support.annotation.AttrRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;

import com.wjy.wangjyandroid.Utils.ScreenUtil;

/**
 * Created by wjy on 2018/2/8.
 *
 * 用WindowManager实现应用内悬浮
 */

public class DragViewGroup extends FrameLayout implements View.OnTouchListener {

    private WindowManager manager;
    private WindowManager.LayoutParams params;
    private View floatView;
    private DisplayMetrics metrics;
    private Rect dragRect; // 可滑动区域 默认全屏
    private ValueAnimator animator;

    public DragViewGroup(@NonNull Context context) {
        this(context, null);
    }

    public DragViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DragViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, @AttrRes int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        manager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        metrics = ScreenUtil.getScreenMetrics(context);
        int statusBarHeight = ScreenUtil.getStatusBarHeight(context);
        dragRect = new Rect(0, 0, metrics.widthPixels, metrics.heightPixels - statusBarHeight);
        paramsConfigure(statusBarHeight);
    }

    private void paramsConfigure(int statusBarHeight) {
        params = new WindowManager.LayoutParams();
        params.format = PixelFormat.RGBA_8888;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
        params.windowAnimations = 0;
        params.gravity = Gravity.TOP | Gravity.START; // params.x 和 params.y 只有在 params.gravity = Gravity.TOP | Gravity.START 时能够表示floatView左上角坐标
    }

    public void setFloatView(View view, int viewWidth, int viewHeight) {
        floatView = view;
        params.width = viewWidth;
        params.height = viewHeight;
        params.x = dragRect.right - params.width;
        params.y = dragRect.bottom - params.height;
        floatView.setOnTouchListener(this);
        manager.addView(floatView, params);
    }

    public void setDragRectRegionLeft(int left) {
        dragRect.left = left;
    }

    public void setDragRectRegionTop(int top) {
        dragRect.top = top;
    }

    public void setDragRectRegionRight(int right) {
        dragRect.right = right;
    }

    public void setDragRectRegionBottom(int bottom) {
        dragRect.bottom = bottom;
    }

    public void setDragRectRegion(int left, int top, int right, int bottom) {
        dragRect.set(left, top, right, bottom);
    }

    private float downX, downY;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                downX = event.getRawX();
                downY = event.getRawY();
                cancelAnimator();
                break;
            case MotionEvent.ACTION_MOVE:
                float moveX = event.getRawX();
                float moveY = event.getRawY();
                float changeX = moveX - downX;
                float changeY = moveY - downY;
                params.x += (int) changeX;
                params.y += (int) changeY;
                int maxY = dragRect.bottom - params.height;
                if (params.y < dragRect.top) params.y = dragRect.top;
                else if (params.y > maxY) params.y = maxY;
                int maxX = dragRect.right - params.width;
                if (params.x < dragRect.left) params.x = dragRect.left;
                else if (params.x > maxX) params.x = maxX;
                manager.updateViewLayout(floatView, params);
                downX = moveX;
                downY = moveY;
                break;
            case MotionEvent.ACTION_UP:
                int maxDistance = metrics.widthPixels - params.width;
                if (params.x != 0 && params.x != maxDistance) {
                    int startX = params.x;
                    int endX = startX > (metrics.widthPixels - params.width) >> 1 ? maxDistance : 0;
                    animator = ValueAnimator.ofInt(startX, endX);
                    animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                        @Override
                        public void onAnimationUpdate(ValueAnimator animation) {
                            params.x = (int) animation.getAnimatedValue();
                            manager.updateViewLayout(floatView, params);
                        }
                    });
                    animator.addListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            animation.removeAllListeners();
                            animator = null;
                        }
                    });
                    animator.start();
                    return true;
                }
                break;
        }
        return false;
    }

    private void cancelAnimator() {
        if (null == animator) return;
        if (animator.isRunning()) animator.cancel();
    }

}
