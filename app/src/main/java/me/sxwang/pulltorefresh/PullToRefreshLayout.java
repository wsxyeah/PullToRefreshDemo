package me.sxwang.pulltorefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.ListView;

/**
 * Created by wang on 3/18/16.
 */
public class PullToRefreshLayout extends LinearLayout {

    private static final String TAG = PullToRefreshLayout.class.getSimpleName();

    public static final int DEFAULT_SCROLL_THRESHOLD = 240;
    public static final int DEFAULT_MAX_SCROLL_HEIGHT = 256;
    public static final int DEFAULT_COLLAPSE_DURATION = 250;

    private RefreshHeader mRefreshHeader;
    private ListView mListView;

    private float mActionDownY = 0;
    private boolean isRefreshing = false;

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
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        for (int i = 0, count = getChildCount(); i < count; i++) {
            View child = getChildAt(i);
            if (child instanceof RefreshHeader) {
                mRefreshHeader = (RefreshHeader) child;
            }
            if (child instanceof ListView) {
                mListView = (ListView) child;
            }
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        int listScrollY = mListView.getChildCount() == 0 ? 0 : mListView.getChildAt(0).getTop();
        switch (ev.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                if (listScrollY == 0) mActionDownY = ev.getY();
                break;
            case MotionEvent.ACTION_MOVE:
                if (listScrollY == 0 && ev.getY() > mActionDownY) {
                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                if (listScrollY == 0 && ev.getY() > mActionDownY) {
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
        if (event.getActionMasked() == MotionEvent.ACTION_UP) {
            if (scrollY < scrollThreshold) {
                collapseHeader();
            } else {
                isRefreshing = true;
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
            lp.height = (int) (scrollY * 0.55);
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

    public boolean isRefreshing() {
        return isRefreshing;
    }

    public void setRefreshing(boolean isRefreshing) {
        this.isRefreshing = isRefreshing;
        if (!isRefreshing) {
            collapseHeader();
            mRefreshHeader.setProgress(0);
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
