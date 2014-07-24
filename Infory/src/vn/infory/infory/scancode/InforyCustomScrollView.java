package vn.infory.infory.scancode;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ScrollView;

public class InforyCustomScrollView extends ScrollView{

	//http://stackoverflow.com/questions/6210895/listview-inside-scrollview-is-not-scrolling-on-android/11554684#11554684
	
	public InforyCustomScrollView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public InforyCustomScrollView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InforyCustomScrollView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }
    
    @Override
    public boolean onInterceptTouchEvent(MotionEvent ev) {
        final int action = ev.getAction();
        switch (action)
        {        	
            case MotionEvent.ACTION_DOWN:
//                Log.i("VerticalScrollview", "onInterceptTouchEvent: DOWN super false" );
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_MOVE:
                return false; // redirect MotionEvents to ourself

            case MotionEvent.ACTION_CANCEL:
//                Log.i("VerticalScrollview", "onInterceptTouchEvent: CANCEL super false" );
                super.onTouchEvent(ev);
                break;

            case MotionEvent.ACTION_UP:
//                Log.i("VerticalScrollview", "onInterceptTouchEvent: UP super false" );
                return false;

            default: Log.i("VerticalScrollview", "onInterceptTouchEvent: " + action ); break;
        }

        return false;
    }

    @Override
    public boolean onTouchEvent(MotionEvent ev) {
        super.onTouchEvent(ev);
//        Log.i("VerticalScrollview", "onTouchEvent. action: " + ev.getAction() );
         return true;
    }
}
