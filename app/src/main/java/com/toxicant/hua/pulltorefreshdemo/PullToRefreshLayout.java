package com.toxicant.hua.pulltorefreshdemo;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.MotionEventCompat;
import android.support.v4.view.ViewCompat;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.FrameLayout;
import android.widget.ImageView;


/**
 * Created by hua on 2016/7/16.
 */
public class PullToRefreshLayout extends FrameLayout {
    private ImageView loadingView;//加载view
    private View mTarget;//下拉刷新的目标子view
    private float startY;
    private float d;//密度
    private float mRefreshMinDis;//刷新的最小距离
    private ObjectAnimator mScrollAnimator;//还原动画
    private ObjectAnimator mRotationAnimator;//滚动动画
    private float mLoadViewScrollDis;//加载视图的TranslationY
    private float mLoadViewRotation;//加载视图旋转角度
    private boolean isRefresh;//是否正在刷新
    private RefreshListener listener;

    public interface RefreshListener{
        void onRefresh();
    }

    public PullToRefreshLayout(Context context) {
        super(context);
        init();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }
    private void init(){
        startY=0;
        d=getResources().getDisplayMetrics().density;
        mRefreshMinDis=180*d;
        mLoadViewScrollDis=0;
        isRefresh=false;
    }
    private void ensureTarget(){
        if (mTarget==null){
            mTarget=this.getChildAt(0);
            //添加加载view
            loadingView=new ImageView(getContext());
            ViewGroup.LayoutParams p = new ViewGroup.LayoutParams(Math.round(40*d),Math.round(40*d));
            loadingView.setLayoutParams(p);
            loadingView.setX(40*d);
            loadingView.setY(-40*d);
            loadingView.setImageResource(R.drawable.loading);
            this.addView(loadingView);
        }
    }
    public boolean canChildScrollUp() {
        if (android.os.Build.VERSION.SDK_INT < 14) {
            if (mTarget instanceof AbsListView) {
                final AbsListView absListView = (AbsListView) mTarget;
                return absListView.getChildCount() > 0
                        && (absListView.getFirstVisiblePosition() > 0 || absListView.getChildAt(0)
                        .getTop() < absListView.getPaddingTop());
            } else {
                return ViewCompat.canScrollVertically(mTarget, -1) || mTarget.getScrollY() > 0;
            }
        } else {
            return ViewCompat.canScrollVertically(mTarget, -1);
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {

        ensureTarget();
        final int action = MotionEventCompat.getActionMasked(ev);

        float nowY=ev.getY();
        if (isRefresh){//刷新中，不拦截事件
            return false;
        }
        switch (action){
            case MotionEvent.ACTION_DOWN:
                //Log.i("dowm事件","startY="+startY);
                startY=nowY;
                break;
            case  MotionEvent.ACTION_MOVE:
               // Log.i("判断是否拦截","nowY=>"+nowY);
                if (canChildScrollUp()){//还能下拉，不拦截
                   // Log.i("判断是否拦截","还能下拉 nowY=>"+nowY);
                    return false;
                }
                final float yDiff=nowY-startY;
                if (yDiff>=0) {
                    startY = nowY;
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                break;
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        final int action = MotionEventCompat.getActionMasked(event);
        final float nowY=event.getY();
        final float yDiff=nowY-startY;
        if (isRefresh){//刷新中，不处理
            return false;
        }
        switch (action){
            case MotionEvent.ACTION_MOVE:
               // Log.i("拦截事件","ydiff=>"+yDiff);
                if (yDiff<mRefreshMinDis){//下拉出现加载视图
                    mLoadViewScrollDis=yDiff/2-40*d;
                    loadingView.setTranslationY(mLoadViewScrollDis);
                    mLoadViewRotation=(float) (1.5*yDiff);
                    loadingView.setRotation(mLoadViewRotation);
                }
                return true;
            case MotionEvent.ACTION_UP:
                //判断距离，小于最小距离不刷新，直接复原loadingview
                if (yDiff<mRefreshMinDis){
                   reLoadingView();
                }else {//开始转圈圈
                    mRotationAnimator=ObjectAnimator.ofFloat(loadingView,"Rotation",mLoadViewRotation%360,720).setDuration(1000);
                    mRotationAnimator.setRepeatCount(ValueAnimator.INFINITE);
                    mRotationAnimator.start();
                    //回调接口
                    if (listener!=null){
                        listener.onRefresh();
                    }
                    isRefresh=true;
                }
                //loadingView.setTranslationY(-40*d);
                return true;
            case MotionEvent.ACTION_DOWN:
                return true;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void requestDisallowInterceptTouchEvent(boolean disallowIntercept) {
        //Nope
    }

    public void stopRefresh(){
        if (isRefresh){
            mRotationAnimator.cancel();//停止旋转
            reLoadingView();
            isRefresh=false;
        }
    }
    private void reLoadingView(){
        mScrollAnimator= ObjectAnimator.ofFloat(loadingView,"TranslationY",mLoadViewScrollDis,-40*d);
        mScrollAnimator.start();
    }
    public void setListener(RefreshListener listener) {
        this.listener = listener;
    }
}
