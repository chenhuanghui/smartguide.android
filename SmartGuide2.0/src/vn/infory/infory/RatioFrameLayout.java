package vn.infory.infory;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

public class RatioFrameLayout extends FrameLayout {
	
//	private double mRatio = 0;

	public RatioFrameLayout(Context context) {
		super(context);
	}
	
	public RatioFrameLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
//		getRatio(attrs);
	}
	
	public RatioFrameLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
//		getRatio(attrs);
	}
	
	private double getRatio() {
		String prefix = "ratio:";
//		String ns = "http://schemas.android.com/apk/res/android";
//		String dsc = attrs.getAttributeValue(ns, "contentDescription");
		String dsc = getContentDescription().toString();
		if (!dsc.startsWith(prefix))
			return 1;
		
		String ratioStr = dsc.substring(prefix.length());
		try {
			return Double.parseDouble(ratioStr);
		} catch(Exception e) {
			return 1;
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int mode = MeasureSpec.getMode(widthMeasureSpec);
		int size = MeasureSpec.getSize(widthMeasureSpec);
		int spec = MeasureSpec.makeMeasureSpec((int) (size / getRatio()), mode);
		super.onMeasure(widthMeasureSpec, spec);
	}
}
