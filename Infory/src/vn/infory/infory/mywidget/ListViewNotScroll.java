package vn.infory.infory.mywidget;

import vn.infory.infory.R;
import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.widget.ListView;

public class ListViewNotScroll extends ListView {
    private boolean swipeable;

    public ListViewNotScroll(Context context) {
        super(context);
    }

    public ListViewNotScroll(Context context, AttributeSet attrs) {
        super(context, attrs);
        TypedArray a = context.obtainStyledAttributes(attrs,
                R.styleable.ListViewScrollable);
        try {
            swipeable = a.getBoolean(R.styleable.ListViewScrollable_scrollable, true);
        } finally {
            a.recycle();
        }
    }

    @Override
    public boolean onInterceptTouchEvent(MotionEvent event) {
        return swipeable ? super.onInterceptTouchEvent(event) : false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return swipeable ? super.onTouchEvent(event) : false;
    }
}
