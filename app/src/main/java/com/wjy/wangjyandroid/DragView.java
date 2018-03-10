package com.wjy.wangjyandroid;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.content.Context;
import android.graphics.Rect;
import android.support.v7.widget.AppCompatImageView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.widget.FrameLayout;

import com.wjy.wangjyandroid.Utils.ScreenUtil;


/**
 * Created by wjy on 2018/2/6.
 *
 */
public class DragView extends AppCompatImageView {

    private int width;
    private int height;
    private int mTouchSlop;
    private ValueAnimator animator;
    private FrameLayout.LayoutParams params;
    private int screenWidth;

    private Rect dragRect; // 默认全屏

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

    //是否拖动
    private boolean isDrag = false;

    public boolean isDrag() {
        return isDrag;
    }

    public DragView(Context context, AttributeSet attrs) {
        super(context, attrs);
        ViewConfiguration configure = ViewConfiguration.get(context);
        mTouchSlop = configure.getScaledTouchSlop();
        Rect rect = ScreenUtil.getAppRect((Activity) getContext());
        screenWidth = rect.width();
        int appHeight = rect.height() - ScreenUtil.getStatusBarHeight(context);
        dragRect = new Rect();
        dragRect.set(0, 0, screenWidth, appHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        width = getMeasuredWidth();
        height = getMeasuredHeight();
        if (null == params) {
            params = new FrameLayout.LayoutParams(width, height);
        }
    }

    private float downX;
    private float downY;

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (this.isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    cancelAnimator();
                    isDrag = false;
                    downX = event.getRawX();
                    downY = event.getRawY();
                    break;
                case MotionEvent.ACTION_MOVE:
                    float moveX = event.getRawX();
                    float moveY = event.getRawY();
                    final float xDistance = moveX - downX;
                    final float yDistance = moveY - downY;
                    int l, r, t, b;
                    if (Math.abs(xDistance) > mTouchSlop || Math.abs(yDistance) > mTouchSlop) {
                        isDrag = true;
                        l = (int) (getLeft() + xDistance);
                        r = l + width;
                        t = (int) (getTop() + yDistance);
                        b = t + height;
                        //不划出边界判断,手机全屏写法
//                        if (l < 0) {
//                            l = 0;
//                            r = l + width;
//                        } else if (r > screenWidth) {
//                            r = screenWidth;
//                            l = r - width;
//                        }
//                        if (t < 0) {
//                            t = 0;
//                            b = t + height;
//                        } else if (b > screenHeight) {
//                            b = screenHeight;
//                            t = b - height;
//                        }
                        if (l < dragRect.left) {
                            l = dragRect.left;
                            r = l + width;
                        } else if (r > dragRect.right) {
                            r = dragRect.right;
                            l = r - width;
                        }
                        if (t < dragRect.top) {
                            t = dragRect.top;
                            b = t + height;
                        } else if (b > dragRect.bottom) {
                            b = dragRect.bottom;
                            t = b - height;
                        }
                        this.layout(l, t, r, b);
                        params.setMargins(l, t, screenWidth - r, dragRect.bottom - b);
                        setLayoutParams(params);
                        downX = moveX;
                        downY = moveY;
                    }
                    break;
                case MotionEvent.ACTION_UP:
                    int startX = getLeft();
                    if (startX != dragRect.left && startX != dragRect.right - width) { // 只考虑 dragRect.width() 和 dragRect.height() 同时 > width 的情况
                        int endX = getLeft() > (dragRect.width() - width) >> 1 ? dragRect.right - width : 0;
                        animator = ValueAnimator.ofInt(startX, endX);
                        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                            @Override
                            public void onAnimationUpdate(ValueAnimator animation) {
                                int leftX = (int) animation.getAnimatedValue();
                                layout(leftX, getTop(), leftX + width, getBottom());
                                params.setMargins(getLeft(), getTop(), screenWidth - getRight(), dragRect.bottom - getBottom());
                                setLayoutParams(params);
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
                    }
                    if (isDrag) {
                        return true;
                    }
                    break;
            }
            return super.onTouchEvent(event);
        }
        return false;
    }

    private void cancelAnimator() {
        if (null == animator) return;
        if (animator.isRunning()) animator.cancel();
    }

}
