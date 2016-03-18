package me.sxwang.pulltorefresh;

import android.content.Context;
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
        lp.addRule(CENTER_IN_PARENT);
        mTextView.setLayoutParams(lp);
    }

    private void refresh() {
        float alpha = (float) (mProgress / 100.0);
        mTextView.setAlpha(alpha);

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
