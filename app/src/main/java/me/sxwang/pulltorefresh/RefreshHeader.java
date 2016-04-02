package me.sxwang.pulltorefresh;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.support.v4.widget.TextViewCompat;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Created by wang on 3/18/16.
 */
public class RefreshHeader extends RelativeLayout {

    private int mState = PullToRefreshLayout.STATE_UNSTARTED;
    private int mProgress;
    private TextView mTextView;
    private ValueAnimator mCircleAnimator;
    private CharSequence[] mPrompts = {"Pull to refresh", "Refreshing", "Success", "Failed"};

    private int mCircleCount = 5;
    private int mCircleRadius;
    private int mCircleBaseGap;
    private int mMaxExpandableGap;
    private int mCircleGap;
    private float mFirstTranslationY = 0;

    private RectF mCircleRectF = new RectF();
    private Paint mCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    public RefreshHeader(Context context) {
        this(context, null);
    }

    public RefreshHeader(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RefreshHeader(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs, defStyleAttr);
    }

    private void init(final Context context, AttributeSet attrs, int defStyleAttr) {
        mTextView = new TextView(context);
        TextViewCompat.setTextAppearance(mTextView, android.support.design.R.style.TextAppearance_AppCompat_Body2);
        addView(mTextView);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Utils.dpToPx(getContext(), 8);
        lp.addRule(CENTER_HORIZONTAL);
        lp.addRule(ALIGN_PARENT_BOTTOM);
        mTextView.setLayoutParams(lp);

        mCircleRadius = Utils.dpToPx(context, 5);
        mCircleBaseGap = Utils.dpToPx(context, 10);
        mCircleGap = mCircleBaseGap;
        mMaxExpandableGap = Utils.dpToPx(context, 30);

        mCirclePaint.setColor(Color.GRAY);
        setWillNotDraw(false);
        refresh();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cX = canvas.getWidth() / 2, cY = canvas.getHeight() / 2;
        int startX = (canvas.getWidth() - mCircleCount * mCircleRadius * 2 - (mCircleCount - 1) * mCircleGap) / 2;
        mCircleRectF.top = cY - mCircleRadius;
        mCircleRectF.bottom = cY + mCircleRadius;

        if (mState == PullToRefreshLayout.STATE_REFRESHING) {
            mCircleRectF.top += mFirstTranslationY;
            mCircleRectF.bottom += mFirstTranslationY;
        }

        for (int i = 0; i < mCircleCount; i++) {
            mCircleRectF.left = startX + i * 2 * mCircleRadius + i * mCircleGap;
            mCircleRectF.right = mCircleRectF.left + 2 * mCircleRadius;
            canvas.drawOval(mCircleRectF, mCirclePaint);
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        cancelCircleAnimation();
    }

    private void refresh() {
        float percent = mProgress / 100f;
        mTextView.setAlpha(percent / 3);

        mCircleGap = Math.round(mCircleBaseGap + mMaxExpandableGap * percent);
        mCirclePaint.setAlpha(Math.round(255 * Math.min(percent, 1f)));
        invalidate();

        if (mState == PullToRefreshLayout.STATE_REFRESHING) {
            startRefreshAnimation();
        } else {
            cancelCircleAnimation();
        }

        mTextView.setText(mPrompts[mState]);
    }

    private void cancelCircleAnimation() {
        if (mCircleAnimator != null) {
            mCircleAnimator.cancel();
        }
    }

    private void startRefreshAnimation() {
        mCircleAnimator = ValueAnimator.ofFloat(0, 30);
        mCircleAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator animation) {
                mFirstTranslationY = (float) animation.getAnimatedValue();
                invalidate();
            }
        });
        mCircleAnimator.setRepeatCount(ValueAnimator.INFINITE);
        mCircleAnimator.setRepeatMode(ValueAnimator.REVERSE);
        mCircleAnimator.start();
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        refresh();
    }

    public int getState() {
        return mState;
    }

    public void setState(int state) {
        mState = state;
        refresh();
    }
}
