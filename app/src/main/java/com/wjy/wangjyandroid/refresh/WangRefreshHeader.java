package com.wjy.wangjyandroid.refresh;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;

import com.scwang.smartrefresh.layout.api.RefreshHeader;
import com.scwang.smartrefresh.layout.api.RefreshKernel;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.constant.RefreshState;
import com.scwang.smartrefresh.layout.constant.SpinnerStyle;
import com.wjy.wangjyandroid.R;


/**
 * Created by wjy on 2018/1/12.
 *
 */
public class WangRefreshHeader extends LinearLayout implements RefreshHeader {

    private WangDetailRefreshView wangDetailRefreshView;

    public WangRefreshHeader(Context context) {
        super(context);
    }

    public WangRefreshHeader(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View view = layoutInflater.inflate(R.layout.wang_refresh_header, this);
        wangDetailRefreshView = view.findViewById(R.id.refreshView);
    }

    public WangRefreshHeader(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    public void onPullingDown(float percent, int offset, int headerHeight, int extendHeight) {
        wangDetailRefreshView.setDistance(offset);
    }

    @Override
    public void onReleasing(float percent, int offset, int headerHeight, int extendHeight) {
        wangDetailRefreshView.setDistance(offset);
    }

    @NonNull
    @Override
    public View getView() {
        return this;
    }

    @Override
    public SpinnerStyle getSpinnerStyle() {
        return SpinnerStyle.Translate;
    }

    @Override
    public void setPrimaryColors(int... colors) {

    }

    @Override
    public void onInitialized(RefreshKernel kernel, int height, int extendHeight) {

    }

    @Override
    public void onHorizontalDrag(float percentX, int offsetX, int offsetMax) {

    }

    @Override
    public void onStartAnimator(RefreshLayout layout, int height, int extendHeight) {

    }

    @Override
    public int onFinish(RefreshLayout layout, boolean success) {
        return 0;
    }

    @Override
    public boolean isSupportHorizontalDrag() {
        return false;
    }

    @Override
    public void onStateChanged(RefreshLayout refreshLayout, RefreshState oldState, RefreshState newState) {
        Log.d("wang", "onStateChanged() called with: refreshLayout = [" + refreshLayout + "], oldState = [" + oldState + "], newState = [" + newState + "]");
        switch (newState) {
            case Refreshing:
                wangDetailRefreshView.onRefreshing();
                break;
            case None:
                // if (oldState == RefreshState.RefreshFinish) {
                wangDetailRefreshView.restoreView();
                break;
        }
    }
}
