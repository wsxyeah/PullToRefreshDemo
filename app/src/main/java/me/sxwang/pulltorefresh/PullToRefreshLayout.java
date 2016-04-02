package me.sxwang.pulltorefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.GridView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ScrollView;

/**
 * Created by wang on 3/18/16.
 */
public class PullToRefreshLayout extends LinearLayout {

    private static final String TAG = PullToRefreshLayout.class.getSimpleName();

    public static final int DEFAULT_SCROLL_THRESHOLD = 240;
    public static final int DEFAULT_MAX_SCROLL_HEIGHT = 256;
    public static final int DEFAULT_COLLAPSE_DURATION = 250;


    public static final int STATE_UNSTARTED = 0;
    public static final int STATE_REFRESHING = 1;
    public static final int STATE_RESULT_OK = 2;
    public static final int STATE_RESULT_FAIL = 3;

    private RefreshHeader mRefreshHeader;
    private View mScrollableView;

    private int mTouchSlop = 0;
    private float mActionDownY = 0;
    private int mState = STATE_UNSTARTED;

    public PullToRefreshLayout(Context context) {
        this(context, null);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PullToRefreshLayout(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(Context context, AttributeSet attrs, int defStyleAttr) {
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof RefreshHeader) {
                mRefreshHeader = (RefreshHeader) child;
            }
            if (child instanceof ListView || child instanceof GridView || child instanceof RecyclerView
                    || child instanceof ScrollView || child instanceof NestedScrollView) {
                mScrollableView = child;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        boolean canScrollUp = ViewCompat.canScrollVertically(mScrollableView, -1);
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (!canScrollUp) mActionDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (!canScrollUp && ev.getY() > mActionDownY + mTouchSlop) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (!canScrollUp && ev.getY() > mActionDownY + mTouchSlop) {
                    return true;
                }
        }
        return super.onInterceptTouchEvent(ev);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float scrollY = event.getY() - mActionDownY;
//        Log.i(TAG, "onTouchEvent: " + scrollY);

        int scrollThreshold = Utils.dpToPx(getContext(), DEFAULT_SCROLL_THRESHOLD);
        boolean shouldRefresh = scrollY >= scrollThreshold;
        setState(shouldRefresh ? STATE_REFRESHING : STATE_UNSTARTED);

        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_UP:

                if (!shouldRefresh) {
                    collapseHeader();
                } else {
                    if (mOnRefreshListener != null) {
                        mOnRefreshListener.onRefresh(this);
                    }
                }
                return true;
        }

        // animating progress
        int maxScrollHeight = Utils.dpToPx(getContext(), DEFAULT_MAX_SCROLL_HEIGHT);
        if (scrollY <= maxScrollHeight) {
            ViewGroup.LayoutParams lp = mRefreshHeader.getLayoutParams();
            lp.height = Math.round(scrollY * 0.55f);
            mRefreshHeader.setLayoutParams(lp);
            mRefreshHeader.setProgress(Math.round(scrollY * 100 / scrollThreshold));
        }

        return super.onTouchEvent(event);
    }

    private void collapseHeader() {
        final ViewGroup.LayoutParams lp = mRefreshHeader.getLayoutParams();
        ValueAnimator animator = ValueAnimator.ofInt(lp.height, 0);
        animator.setInterpolator(new DecelerateInterpolator());
        animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                lp.height = (int) animation.getAnimatedValue();
                mRefreshHeader.setLayoutParams(lp);
            }
        });
        animator.setDuration(DEFAULT_COLLAPSE_DURATION).start();
    }


    public int getState() {
        return mState;
    }

    public void setState(int state) {
        if (mState != state) {
            mState = state;
            mRefreshHeader.setState(mState);
            if (mState == STATE_RESULT_OK || mState == STATE_RESULT_FAIL) {
                postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        collapseHeader();
                    }
                }, 500);
            }
        }
    }

    private OnRefreshListener mOnRefreshListener;

    public OnRefreshListener getOnRefreshListener() {
        return mOnRefreshListener;
    }

    public void setOnRefreshListener(OnRefreshListener onRefreshListener) {
        mOnRefreshListener = onRefreshListener;
    }

    interface OnRefreshListener {
        void onRefresh(PullToRefreshLayout pullToRefreshLayout);
    }
}
