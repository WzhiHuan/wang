package com.wjy.wangjyandroid.refresh;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.wjy.wangjyandroid.R;
import com.wjy.wangjyandroid.Utils.ScreenUtil;

/**
 *
 *  Created by wjy on 2018/1/12.
 *
 *  根据角度计算圆上某点的坐标
 *  圆点坐标：(x0,y0)
 *  半径：r
 *  角度：angle
 *  则圆上任一点为：（x1,y1）
 *  x1   =   x0   +   r   *   cos(angle   *   Math.PI   /180   )
 *  y1   =   y0   +   r   *   sin(angle   *   Math.PI   /180   )
 *
 *  2018/1/13 如果每一次invalidate()方法调用后都执行onDraw则可以这样写,但当动画执行时快速让动画移出屏幕则并不是每一次都执行,
 *  所以后续动画绘制前面已经执行完成的动画时需要直接绘制计算后的最终位置,以防上述不绘制的情况出现导致部分动画位置坐标不正确
 *  (文字最终位置未计算,因为懒,如果之后因为文字紊乱出现频繁再计算)
 */

public class WangDetailRefreshView extends View {
    Paint paint;
    //盒子上方的角的路径
    Path lidPath = new Path();

    //盒子的两个盖子的上方的角坐标
    Point pointLeftOut;
    Point pointLeftInner;
    Point pointRightOut;
    Point pointRightInner;

    //盒子上方的四个角相当于四个圆心  盖子相当于围绕圆心做运动
    Point leftPointOut;
    Point leftPointInner;
    Point rightPointOut;
    Point rightPointInner;

    Path path = new Path();
    private int viewSizeHeight;
//    private int viewSizeWidth;

    //标语字的集合
    private Bitmap[] slogan;
    //吉祥物
    private Bitmap mascot;
    private Bitmap mascotHand;
    private Bitmap mascotNoEyes;
    private Bitmap mascotEye;
    //字坐标的集合
    private Point[] points;
    //吉祥物body坐标
    private Point point;
    //吉祥物hand坐标
    private Point pointHand;
    //吉祥物eye坐标
    private Point pointLeftEye;
    private Point pointRightEye;
    //吉祥物眼睛缩放控制
    private Matrix matrix;
    //严选logo
    private Bitmap alaLogo;
    //阴影的矩形
    private RectF rectShadow;

    private int jumpInt;
    private float scaleFloat;
    private float originX, originY, sloganOriginX;
    private int sloganIndex;
    private boolean isFirst = true; // 每个字的第一次出现
    private boolean isReverse; // 绘制文字的时候判断已缩放完成的文字的抖动动画方向
    private ViewStatus viewStatus;

    //当前的下拉距离
    int distance = 0;
    int leftAngle = 0;
    int rightAngle = Math.abs(leftAngle) - 180;

//    private int outCircleR = 80; // 原100
//    private int innerCircleR = 45; // 原70
    private int outCircleR = ScreenUtil.dp2px(26.67f, getContext()); // 原100
    private int innerCircleR = ScreenUtil.dp2px(15, getContext()); // 原70

    private float boxHeight, handStart;
    private int eyeWidthHalf, eyeHeightHalf;
    private int handMaxOffset, bodyMaxOffset;
    private Point pointLogo;

    private boolean isPlay; // 控制动画播放与否

    public WangDetailRefreshView(Context context) {
        this(context, null);
    }

    public WangDetailRefreshView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WangDetailRefreshView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        matrix = new Matrix();
        //初始所有的点
        pointLeftOut = new Point(0, 0);
        pointLeftInner = new Point(0, 0);
        pointRightOut = new Point(0, 0);
        pointRightInner = new Point(0, 0);
        leftPointOut = new Point(0, 0);
        leftPointInner = new Point(0, 0);
        rightPointOut = new Point(0, 0);
        rightPointInner = new Point(0, 0);

        //获取所有的字
        int[] arrayOfInt = {R.mipmap.wu, R.mipmap.yi, R.mipmap.fan, R.mipmap.bei};
        slogan = new Bitmap[arrayOfInt.length];
        points = new Point[arrayOfInt.length];
        for (int i = 0; i < arrayOfInt.length; i++) {
            slogan[i] = BitmapFactory.decodeResource(getResources(), arrayOfInt[i]);
            points[i] = new Point();
        }
        mascot = BitmapFactory.decodeResource(getResources(), R.mipmap.mascot);
        mascotHand = BitmapFactory.decodeResource(getResources(), R.mipmap.mascot_hand);
        mascotNoEyes = BitmapFactory.decodeResource(getResources(), R.mipmap.mascot_noeye);
        mascotEye = BitmapFactory.decodeResource(getResources(), R.mipmap.mascot_eye);
        eyeWidthHalf = mascotEye.getWidth() >> 1;
        eyeHeightHalf = mascotEye.getHeight() >> 1;
        point = new Point();
        pointHand = new Point();
        pointLeftEye = new Point();
        pointRightEye = new Point();

        alaLogo = BitmapFactory.decodeResource(getResources(), R.mipmap.refresh_icon);
        pointLogo = new Point();

        rectShadow = new RectF();
        viewStatus = ViewStatus.START;
    }

    private ValueAnimator handAnimator, bodyAnimator, eyesAnimator, sloganAnimator, shakeAnimator;

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        initAnimatior();
    }

    private void initAnimatior() {
        handMaxOffset = mascot.getHeight() * 3 / 4 + mascotHand.getHeight() * 2 / 3;
        handAnimator = ValueAnimator.ofInt(0, handMaxOffset);
        handAnimator.setDuration(200);
        handAnimator.setInterpolator(new DecelerateInterpolator());
        handAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                jumpInt = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        handAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                jumpAnimBody();
            }
        });

        bodyMaxOffset = mascot.getHeight() * 4 / 5;
        bodyAnimator = ValueAnimator.ofInt(0, mascot.getHeight() - ScreenUtil.dp2px(3.33f, getContext()), bodyMaxOffset); // dp2px(3.33f)是调节头部以下露出的高度,是被箱体遮挡的高度,但要注意小于mascot.getHeight()的五分之一
        bodyAnimator.setDuration(300);
        bodyAnimator.setInterpolator(new AccelerateInterpolator(1.3f));
        bodyAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                jumpInt = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
        bodyAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                blinkAnimEyes();
            }
        });

        eyesAnimator = ValueAnimator.ofFloat(1f, 1f, 1f, 0.8f, 0.5f, 0.2f, 0.1f);
        eyesAnimator.setRepeatCount(3);
        eyesAnimator.setRepeatMode(ValueAnimator.REVERSE);
        eyesAnimator.setDuration(200);
        eyesAnimator.setInterpolator(new AccelerateInterpolator());
        eyesAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scaleFloat = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        eyesAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                drawAnimslogan();
            }
        });

        sloganAnimator = ValueAnimator.ofFloat(0.2f, 0.4f, 0.6f, 0.8f, 1);
        sloganAnimator.setDuration(100);
        sloganAnimator.setRepeatCount(3); // 四个字就重复三次
        sloganAnimator.setRepeatMode(ValueAnimator.RESTART);
        sloganAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                scaleFloat = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        sloganAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                super.onAnimationEnd(animation);
                paint.setAlpha(255);
                shakeAnimSlogan();
            }

            @Override
            public void onAnimationRepeat(Animator animation) {
                super.onAnimationRepeat(animation);
                isReverse = !isReverse;
                sloganIndex++;
                isFirst = true;
            }
        });

        //简单实现的抖动动画 想要实现更好的效果可以查看https://github.com/hujiaweibujidao/wava
        shakeAnimator = ValueAnimator.ofInt(1, -1);
        shakeAnimator.setRepeatCount(ValueAnimator.INFINITE);
        shakeAnimator.setDuration(300);
        shakeAnimator.setRepeatMode(ValueAnimator.REVERSE);
        shakeAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                jumpInt = (int) animation.getAnimatedValue();
                invalidate();
            }
        });
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        removeAnimatior();
    }

    private void removeAnimatior() {
        handAnimator.removeAllUpdateListeners();
        handAnimator.removeAllListeners();
        handAnimator = null;

        bodyAnimator.removeAllUpdateListeners();
        bodyAnimator.removeAllListeners();
        bodyAnimator = null;

        eyesAnimator.removeAllUpdateListeners();
        eyesAnimator.removeAllListeners();
        eyesAnimator = null;

        sloganAnimator.removeAllUpdateListeners();
        sloganAnimator.removeAllListeners();
        sloganAnimator = null;

        shakeAnimator.removeAllUpdateListeners();
        shakeAnimator.removeAllListeners();
        shakeAnimator = null;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int viewSizeWidth = getMeasuredWidth();
        viewSizeHeight = getMeasuredHeight();

        leftPointOut.pointX = (viewSizeWidth >> 1) - outCircleR;
        leftPointOut.pointY = (viewSizeHeight >> 1);
        leftPointInner.pointX = (viewSizeWidth >> 1) - innerCircleR;
        leftPointInner.pointY = (viewSizeHeight >> 1) - ScreenUtil.dp2px(10, getContext());
        rightPointOut.pointX = (viewSizeWidth >> 1) + outCircleR;
        rightPointOut.pointY = (viewSizeHeight >> 1);
        rightPointInner.pointX = (viewSizeWidth >> 1) + innerCircleR;
        rightPointInner.pointY = (viewSizeHeight >> 1) - ScreenUtil.dp2px(10, getContext());

        rectShadow.left = leftPointOut.pointX - ScreenUtil.dp2px(16.67f, getContext());
        rectShadow.right = rightPointOut.pointX + ScreenUtil.dp2px(16.67f, getContext());
        rectShadow.top = leftPointOut.pointY + ScreenUtil.dp2px(26.67f, getContext());
        rectShadow.bottom = leftPointOut.pointY + ScreenUtil.dp2px(40, getContext());
        
        boxHeight = rightPointOut.pointY + ScreenUtil.dp2px(33.33f, getContext()); // dp2px(33.33f)为盒子高度,也可写成 leftPointOut.pointY + dp2px(33.33f)

        pointLogo.pointX = ((int)(leftPointOut.pointX + rightPointOut.pointX - alaLogo.getWidth())) >> 1;
        pointLogo.pointY = ((int)(leftPointOut.pointY + leftPointOut.pointY + ScreenUtil.dp2px(33.33f, getContext()) - alaLogo.getHeight())) >> 1;

        // ViewStatus.START_SHAND
        pointHand.pointX = (((int)((leftPointOut.pointX + rightPointOut.pointX) - mascotHand.getWidth())) >> 1) + 8;
        handStart = leftPointOut.pointY + mascot.getHeight() * 3 / 4;
        // ViewStatus.START_SBODY
        point.pointX = ((int)(leftPointOut.pointX + rightPointOut.pointX) - mascot.getWidth()) >> 1;
        // ViewStatus.START_SEYES
        originX = ((int)(leftPointOut.pointX + rightPointOut.pointX) - mascot.getWidth()) >> 1; // originX = (leftPointOut.pointX + rightPointOut.pointX) / 2 - mascot.getWidth() / 2;
        originY = leftPointOut.pointY - mascot.getHeight() * 4 / 5; // 之所以是五分之四是因为身体动画最终停止在箱体遮挡身体五分之一处
        // 左眼
        pointLeftEye.pointX = originX + (mascotNoEyes.getWidth() >> 1) - ScreenUtil.dp2px(6.4f, getContext());
        pointLeftEye.pointY = originY + (mascotNoEyes.getHeight() >> 1) + ScreenUtil.dp2px(1.8f, getContext());
        // 右眼
        pointRightEye.pointX = originX + (mascotNoEyes.getWidth() >> 1) + ScreenUtil.dp2px(7.5f, getContext());
        pointRightEye.pointY = originY + (mascotNoEyes.getHeight() >> 1) - ScreenUtil.dp2px(1.33f, getContext());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // 无眼吉祥物和有眼吉祥物对比
//        canvas.drawBitmap(mascot, mascot.getWidth(), 0, paint);
//        canvas.drawBitmap(mascotNoEyes, 0, 0, paint);
//        canvas.drawBitmap(mascotEye, (mascotNoEyes.getWidth() >> 1) - 19, (mascotNoEyes.getHeight() >> 1) + 6, paint); // 左眼
//        canvas.drawBitmap(mascotEye, (mascotNoEyes.getWidth() >> 1) + 22, (mascotNoEyes.getHeight() >> 1) - 2, paint); // 右眼

//        leftPointOut.pointX = viewSizeWidth / 2 - outCircleR;
//        leftPointOut.pointY = viewSizeHeight / 2;
//        leftPointInner.pointX = viewSizeWidth / 2 - innerCircleR;
//        leftPointInner.pointY = viewSizeHeight / 2 - dp2px(10);
//        rightPointOut.pointX = viewSizeWidth / 2 + outCircleR;
//        rightPointOut.pointY = viewSizeHeight / 2;
//        rightPointInner.pointX = viewSizeWidth / 2 + innerCircleR;
//        rightPointInner.pointY = viewSizeHeight / 2 - dp2px(10);
        // 优化计算位置
//        leftPointOut.pointX = (viewSizeWidth >> 1) - outCircleR;
//        leftPointOut.pointY = (viewSizeHeight >> 1);
//        leftPointInner.pointX = (viewSizeWidth >> 1) - innerCircleR;
//        leftPointInner.pointY = (viewSizeHeight >> 1) - dp2px(10);
//        rightPointOut.pointX = (viewSizeWidth >> 1) + outCircleR;
//        rightPointOut.pointY = (viewSizeHeight >> 1);
//        rightPointInner.pointX = (viewSizeWidth >> 1) + innerCircleR;
//        rightPointInner.pointY = (viewSizeHeight >> 1) - dp2px(10);

        pointLeftOut.pointX = (float) (leftPointOut.pointX + outCircleR * Math.cos(leftAngle * Math.PI / 180));
        pointLeftOut.pointY = (float) (leftPointOut.pointY + outCircleR * Math.sin(leftAngle * Math.PI / 180));

//        pointLeftInner.pointX = (float) (leftPointInner.pointX + (innerCircleR + dp2px(4.33f)) * Math.cos(leftAngle * Math.PI / 180));
        pointLeftInner.pointX = (float) (leftPointInner.pointX + (innerCircleR) * Math.cos(leftAngle * Math.PI / 180));
        pointLeftInner.pointY = (float) (leftPointInner.pointY + innerCircleR * Math.sin(leftAngle * Math.PI / 180));

        pointRightOut.pointX = (float) (rightPointOut.pointX + outCircleR * Math.cos(rightAngle * Math.PI / 180));
        pointRightOut.pointY = (float) (rightPointOut.pointY + outCircleR * Math.sin(rightAngle * Math.PI / 180));

//        pointRightInner.pointX = (float) (rightPointInner.pointX + (innerCircleR + dp2px(4.33f)) * Math.cos(rightAngle * Math.PI / 180));
        pointRightInner.pointX = (float) (rightPointInner.pointX + (innerCircleR) * Math.cos(rightAngle * Math.PI / 180));
        pointRightInner.pointY = (float) (rightPointInner.pointY + innerCircleR * Math.sin(rightAngle * Math.PI / 180));

        paint.setAntiAlias(true);
        paint.setColor(0xffE7B79A);
        //绘制第一个梯形
        path.moveTo(leftPointInner.pointX, leftPointInner.pointY);
        path.lineTo(rightPointInner.pointX, rightPointInner.pointY);
        path.lineTo(rightPointOut.pointX, rightPointOut.pointY);
        path.lineTo(leftPointOut.pointX, leftPointOut.pointY);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();

        //绘制内侧矩形
        paint.setColor(0xFFD59F7F);
        path.moveTo(leftPointInner.pointX, leftPointInner.pointY);
        path.lineTo(rightPointInner.pointX, rightPointInner.pointY);
        path.lineTo(rightPointInner.pointX, rightPointOut.pointY);
        path.lineTo(leftPointInner.pointX, leftPointOut.pointY);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();

        drawCover(canvas);
        draw51mascot(canvas);

        //绘制下方阴影
        paint.setColor(0xffE6E6E6);
//        paint.setColor(getResources().getColor(android.R.color.transparent));
        canvas.drawOval(rectShadow, paint);

        //绘制正面的矩形
        paint.setColor(0xFFFBD9C5);
//        paint.setColor(getResources().getColor(android.R.color.transparent));
        path.moveTo(leftPointOut.pointX, leftPointOut.pointY);
        path.lineTo(rightPointOut.pointX, rightPointOut.pointY);
        path.lineTo(rightPointOut.pointX, boxHeight); // 盒子高度
        path.lineTo(leftPointOut.pointX, boxHeight);
        path.close();
        canvas.drawPath(path, paint);
        path.reset();
//        paint.setColor(getResources().getColor(android.R.color.white)); // 为了看动画实现效果之前把画笔设置成透明,在下面绘制手的时候如果是透明,则手绘制效果不符合预期,所以要设置一个颜色
        if (viewStatus == ViewStatus.START_SBODY || viewStatus == ViewStatus.START_SEYES || viewStatus == ViewStatus.START_SLOGAN || viewStatus == ViewStatus.START_SHAKE)
//            canvas.drawBitmap(mascotHand, pointHand.pointX, pointHand.pointY, paint); // @see 41行
            canvas.drawBitmap(mascotHand, pointHand.pointX, handStart - handMaxOffset, paint);
        paint.reset();

        // 绘制51文字logo
        canvas.drawBitmap(alaLogo, pointLogo.pointX, pointLogo.pointY, paint);

        // 看各个point所用
//        paint.setStrokeWidth(10);
//        paint.setColor(Color.parseColor("#FF0000FF")); // 蓝色 leftPointOut
//        canvas.drawPoint(leftPointOut.pointX, leftPointOut.pointY, paint);
//        paint.setColor(Color.parseColor("#FF008000")); // 绿色 rightPointOut
//        canvas.drawPoint(rightPointOut.pointX, rightPointOut.pointY, paint);
//        paint.setColor(Color.parseColor("#FFFFC0CB")); // 粉色 leftPointInner
//        canvas.drawPoint(leftPointInner.pointX, leftPointInner.pointY, paint);
//        paint.setColor(Color.parseColor("#FFF8F8FF")); // 白色 rightPointInner
//        canvas.drawPoint(rightPointInner.pointX, rightPointInner.pointY, paint);

//        paint.setColor(Color.parseColor("#FF000000")); // 黑色 pointLeftOut
//        canvas.drawPoint(pointLeftOut.pointX, pointLeftOut.pointY, paint);
//        paint.setColor(Color.parseColor("#FF8B0000")); // 红色 pointRightOut
//        canvas.drawPoint(pointRightOut.pointX, pointRightOut.pointY, paint);
//        paint.setColor(Color.parseColor("#FFFFFF00")); // 黄色 pointLeftInner
//        canvas.drawPoint(pointLeftInner.pointX, pointLeftInner.pointY, paint);
//        paint.setColor(Color.parseColor("#FFA9A9A9")); // 灰色 pointRightInner
//        canvas.drawPoint(pointRightInner.pointX, pointRightInner.pointY, paint);
//        paint.setColor(0xff88ada6); // 水色 盖子 pointLeftInner
//        canvas.drawPoint(pointLeftInner.pointX, pointLeftInner.pointY, paint);

//        float originX = (leftPointOut.pointX + rightPointOut.pointX) / 2 - mascot.getWidth() / 2;
//        float originY = leftPointOut.pointY;
//        paint.setColor(Color.parseColor("#FF622a1d")); // 玄色
//        canvas.drawPoint(originX, originY, paint);
    }

    //绘制盒子的盖子
    private void drawCover(Canvas canvas) {
        paint.setColor(0xFFFCE1D1);
        //绘制左边的盖子
        lidPath.moveTo(leftPointInner.pointX, leftPointInner.pointY);
        lidPath.lineTo(leftPointOut.pointX, leftPointOut.pointY);
        lidPath.lineTo(pointLeftOut.pointX, pointLeftOut.pointY);
        lidPath.lineTo(pointLeftInner.pointX, pointLeftInner.pointY);
        lidPath.close();
        canvas.drawPath(lidPath, paint);
        lidPath.reset();
        //绘制右边的盖子
        lidPath.moveTo(rightPointInner.pointX, rightPointInner.pointY);
        lidPath.lineTo(rightPointOut.pointX, rightPointOut.pointY);
        lidPath.lineTo(pointRightOut.pointX, pointRightOut.pointY);
        lidPath.lineTo(pointRightInner.pointX, pointRightInner.pointY);
        lidPath.close();
        canvas.drawPath(lidPath, paint);
        //path需要重置  不然会有之前的绘制图像
        lidPath.reset();
    }

    private void draw51mascot(Canvas canvas) {
        if (viewStatus != ViewStatus.START && viewStatus != ViewStatus.REFRESHING) {
            if (viewStatus == ViewStatus.START_SHAND) {
                pointHand.pointY = handStart - jumpInt;
                canvas.drawBitmap(mascotHand, pointHand.pointX, pointHand.pointY, paint);
            } else if (viewStatus == ViewStatus.START_SBODY) {
//                canvas.drawBitmap(mascotHand, pointHand.pointX, pointHand.pointY, paint);
                point.pointY = leftPointOut.pointY - jumpInt;
                canvas.drawBitmap(mascot, point.pointX, point.pointY, paint);
            } else if (viewStatus == ViewStatus.START_SEYES) {
//                canvas.drawBitmap(mascot, point.pointX, point.pointY, paint);
//                canvas.drawBitmap(mascotNoEyes, point.pointX, point.pointY, paint); // @see 41行
                canvas.drawBitmap(mascotNoEyes, point.pointX, leftPointOut.pointY - bodyMaxOffset, paint);

                matrix.setScale(1, scaleFloat, eyeWidthHalf, eyeHeightHalf);

                canvas.save();
                canvas.translate(pointLeftEye.pointX, pointLeftEye.pointY);
//                canvas.rotate(-3);
                canvas.drawBitmap(mascotEye, matrix, paint);
                canvas.restore();

                canvas.save();
                canvas.translate(pointRightEye.pointX, pointRightEye.pointY);
//                canvas.rotate(-3);
                canvas.drawBitmap(mascotEye, matrix, paint);
                canvas.restore();
            } else if (viewStatus == ViewStatus.START_SLOGAN){
                drawMascotBodyAndEye(canvas);
                if (sloganIndex < slogan.length) {
                    for (int i = 0; i <= sloganIndex; i++) {
                        if (i == sloganIndex) {
                            Bitmap bitmap = slogan[sloganIndex];
                            Point point = points[sloganIndex];
//                            Log.d("wang", "sloganIndex: " + sloganIndex + "   scaleFloat: " + scaleFloat);
                            matrix.setScale(scaleFloat, scaleFloat, 0, bitmap.getHeight());
                            paint.setAlpha((int) (255 * scaleFloat));
                            if (isFirst) {
                                paint.setAlpha(0);
                                sloganOriginX = point.pointX = sloganIndex == 0 ? initSloganOriginX() : sloganOriginX + slogan[sloganIndex - 1].getWidth() + (sloganIndex == slogan.length - 1 ? ScreenUtil.dp2px(1f, getContext()) : ScreenUtil.dp2px(3, getContext()));
                                point.pointY = originY - ScreenUtil.dp2px(10, getContext()) - sloganIndex * ScreenUtil.dp2px(1, getContext());
                                isFirst = false;
                            }
//                            Log.d("wang", "alpha: " + paint.getAlpha());
                            matrix.postTranslate(point.pointX, point.pointY);
                            canvas.drawBitmap(bitmap, matrix, paint);
                        } else { // 缩放结束的字进行上下抖动动画
                            canvas.drawBitmap(slogan[i], points[i].pointX, points[i].pointY + 3 * (isReverse ? 1 - scaleFloat : scaleFloat), paint);
//                            canvas.drawBitmap(slogan[i], points[i].pointX, points[i].pointY, paint);
                        }
                    }
                }
            } else { // 这里是 viewStatus == ViewStatus.START_SHAKE 文字抖动
                drawMascotBodyAndEye(canvas);
                for (int i = 0; i < slogan.length; i++) {
                    canvas.drawBitmap(slogan[i], points[i].pointX, points[i].pointY + (i % 2 == 0 ? jumpInt : -jumpInt), paint);
                }
            }
        }
    }

    private void drawMascotBodyAndEye(Canvas canvas) {
//        canvas.drawBitmap(mascot, point.pointX, point.pointY, paint);
        canvas.drawBitmap(mascotNoEyes, point.pointX, leftPointOut.pointY - bodyMaxOffset, paint);// @see 41行
        canvas.save();
        canvas.translate(pointLeftEye.pointX, pointLeftEye.pointY);
        // canvas.rotate(-3);
        canvas.drawBitmap(mascotEye, 0, 0, paint);
        // canvas.drawBitmap(mascotEye, pointLeftEye.pointX, pointLeftEye.pointY, paint);
        canvas.restore();
        canvas.save();
        canvas.translate(pointRightEye.pointX, pointRightEye.pointY);
        // canvas.rotate(-3);
        canvas.drawBitmap(mascotEye, 0, 0, paint);
        // canvas.drawBitmap(mascotEye, pointRightEye.pointX, pointRightEye.pointY, paint);
        canvas.restore();
    }

    //51手手弹起的动画
    public void startJumpAnimHand() {
        viewStatus = ViewStatus.START_SHAND;
//        final int maxDistance = mascot.getHeight() * 3 / 4 + mascotHand.getHeight() * 2 / 3;
//        ValueAnimator animator = ValueAnimator.ofInt(0, maxDistance);
//        animator.setDuration(200);
//        animator.setInterpolator(new DecelerateInterpolator());
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                jumpInt = (int) animation.getAnimatedValue();
//                invalidate();
//            }
//        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                jumpAnimBody();
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
        if (isPlay)
            handAnimator.start();
        else
            restoreView();
    }

    //51身体弹起的动画
    public void jumpAnimBody() {
        viewStatus = ViewStatus.START_SBODY;
//        ValueAnimator animator = ValueAnimator.ofInt(0, mascot.getHeight() - dp2px(3.33f), mascot.getHeight() * 4 / 5); // dp2px(3.33f)是调节头部以下露出的高度,是被箱体遮挡的高度,但要注意小于mascot.getHeight()的五分之一
//        animator.setDuration(300);
//        animator.setInterpolator(new AccelerateInterpolator(1.3f));
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                jumpInt = (int) animation.getAnimatedValue();
//                invalidate();
//            }
//        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                blinkAnimEyes();
//            }
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
        if (isPlay)
            bodyAnimator.start();
        else
            restoreView();
    }

    //51眨眼的动画
    public void blinkAnimEyes() {
        viewStatus = ViewStatus.START_SEYES;
//        ValueAnimator animator = ValueAnimator.ofFloat(1f, 1f, 1f, 0.8f, 0.5f, 0.2f, 0.1f);
//        animator.setRepeatCount(3);
//        animator.setRepeatMode(ValueAnimator.REVERSE);
//        animator.setDuration(200);
//        animator.setInterpolator(new AccelerateInterpolator());
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                scaleFloat = (float) animation.getAnimatedValue();
//                invalidate();
//            }
//        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                drawAnimslogan();
//            }
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//
//            }
//        });
        if (isPlay)
            eyesAnimator.start();
        else
            restoreView();
    }

    //51标语绘制动画
    public void drawAnimslogan() {
        viewStatus = ViewStatus.START_SLOGAN;
        sloganIndex = 0;
//        ValueAnimator animator = ValueAnimator.ofFloat(0.2f, 0.4f, 0.6f, 0.8f, 1);
//        animator.setDuration(100);
//        animator.setRepeatCount(3); // 四个字就重复三次
//        animator.setRepeatMode(ValueAnimator.RESTART);
//        animator.setInterpolator(null);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                scaleFloat = (float) animation.getAnimatedValue();
//                invalidate();
//            }
//        });
//        animator.addListener(new Animator.AnimatorListener() {
//            @Override
//            public void onAnimationEnd(Animator animation) {
//                paint.setAlpha(255);
//                shakeAnimSlogan();
//            }
//
//            @Override
//            public void onAnimationStart(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationCancel(Animator animation) {
//
//            }
//
//            @Override
//            public void onAnimationRepeat(Animator animation) {
//                isReverse = !isReverse;
//                sloganIndex++;
//                isFirst = true;
//            }
//        });
        if (isPlay)
            sloganAnimator.start();
        else
            restoreView();
    }


    //51标语抖动动画
    public void shakeAnimSlogan() {
        viewStatus = ViewStatus.START_SHAKE;
        Log.d("wang", "onStateChanged: " + viewStatus.getName());
//        //简单实现的抖动动画 想要实现更好的效果可以查看https://github.com/hujiaweibujidao/wava
//        ValueAnimator animator = ValueAnimator.ofInt(1, -1);
//        animator.setRepeatCount(ValueAnimator.INFINITE);
//        animator.setDuration(300);
//        animator.setRepeatMode(ValueAnimator.REVERSE);
//        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
//            @Override
//            public void onAnimationUpdate(ValueAnimator animation) {
//                jumpInt = (int) animation.getAnimatedValue();
//                invalidate();
//            }
//        });
        if (isPlay)
            shakeAnimator.start();
        else
            restoreView();
    }

    //设置偏移 这里为了方便计算 设置210偏移对应盖子打开的最大角度 ＋－210
    public void setDistance(int distance) {
        this.distance = distance;
        float offset = (viewSizeHeight >> 1) - ScreenUtil.dp2px(17, getContext());
        float animOffset = distance - (viewSizeHeight >> 1) - ScreenUtil.dp2px(17, getContext());
        Log.d("wang", "setDistance: " + viewStatus.getName());
        Log.d("wang", "onDraw: >>> " + leftAngle + "  ================");
        if (viewStatus == ViewStatus.START && animOffset > 0) {
            if (leftAngle >= -210) {
                //规避原则 下拉距离超过viewSizeHeight后,手指仍然不离开屏幕的情况下移动回来时如果没以下判断条件,盖子不会跟随翻转
                if (animOffset >= offset) {
                    animOffset = offset;
                }
                leftAngle = -(int) (animOffset * 210 / offset); // animOffset * 210 / offset = viewSizeHeight / offset * animOffset * 210 / viewSizeHeight
                rightAngle = Math.abs(leftAngle) - 180;
                invalidate();
                Log.d("wang", "onDraw: >>> " + leftAngle + "  ===================================");
            }
        } else if (viewStatus == ViewStatus.START && animOffset <= 0) {
            restoreView();
        }
    }

    public void onRefreshing() {
        this.viewStatus = ViewStatus.REFRESHING;
        leftAngle = -210;
        rightAngle = 30;
        invalidate();
        isPlay = true;
        startJumpAnimHand();
    }

    //刷新完毕 重置view的状态
    public void restoreView() {
        viewStatus = ViewStatus.START;
        leftAngle = 0;
        rightAngle = -180;
        distance = 0;
        sloganOriginX = initSloganOriginX();
        isFirst = true;
        isPlay = false;
        invalidate();
    }

    private float initSloganOriginX() {
        return originX + ScreenUtil.dp2px(2, getContext());
    }
}
