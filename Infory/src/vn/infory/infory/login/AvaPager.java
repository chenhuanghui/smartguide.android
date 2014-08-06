package vn.infory.infory.login;

import android.content.Context;
import android.support.v4.view.ViewPager;
import android.util.AttributeSet;

public class AvaPager extends ViewPager {
	
	public AvaPager(Context context) {
		super(context);
	}

	public AvaPager(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {	
		widthMeasureSpec = MeasureSpec.makeMeasureSpec(
    			MeasureSpec.getSize(heightMeasureSpec) * 4 / 3,
    			MeasureSpec.getMode(heightMeasureSpec));
    	super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}
}
