package me.sxwang.pulltorefresh;

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

    private int mProgress;
    private TextView mTextView;

    private int mCircleCount = 5;
    private int mCircleRadius = 20;
    private int mCircleBaseGap = 30;
    private int mCircleGap = mCircleBaseGap;

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
        TextViewCompat.setTextAppearance(mTextView, android.support.design.R.style.TextAppearance_AppCompat_Body1);
        mTextView.setAlpha(0);
        mTextView.setText("Pull to Refresh");
        addView(mTextView);

        LayoutParams lp = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        lp.bottomMargin = Utils.dpToPx(getContext(), 16);
        lp.addRule(CENTER_HORIZONTAL);
        lp.addRule(ALIGN_PARENT_BOTTOM);
        mTextView.setLayoutParams(lp);

        mCirclePaint.setColor(Color.GRAY);
        setWillNotDraw(false);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        int cX = canvas.getWidth() / 2, cY = canvas.getHeight() / 2;
        int startX = (canvas.getWidth() - mCircleCount * mCircleRadius * 2 - (mCircleCount - 1) * mCircleGap) / 2;
        mCircleRectF.top = cY - mCircleRadius;
        mCircleRectF.bottom = cY + mCircleRadius;

        for (int i = 0; i < mCircleCount; i++) {
            mCircleRectF.left = startX + i * 2 * mCircleRadius + i * mCircleGap;
            mCircleRectF.right = mCircleRectF.left + 2 * mCircleRadius;
            canvas.drawOval(mCircleRectF, mCirclePaint);
        }
    }

    private void refresh() {
        float percent = mProgress / 100f;
        mTextView.setAlpha(percent / 3);

        mCircleGap = Math.round(mCircleBaseGap + 90 * percent);
        invalidate();

        if (mProgress == 0) {
            mTextView.setText("Pull to Refresh");
        } else if (mProgress == 100) {
            mTextView.setText("Refreshing");
        }
    }

    public int getProgress() {
        return mProgress;
    }

    public void setProgress(int progress) {
        mProgress = progress;
        refresh();
    }
}
