package me.sxwang.pulltorefresh;

import android.content.Context;

/**
 * Created by wang on 3/18/16.
 */
public class Utils {
    public static int dpToPx(Context context, float dps) {
        return Math.round(dps * context.getResources().getDisplayMetrics().density);
    }
}
