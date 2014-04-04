package vn.infory.infory;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RatioImageView extends ImageView {
	
//	private double mRatio = 0;
//	private CyLogger mLog = new CyLogger("RatioImageView", true);

	public RatioImageView(Context context) {
		super(context);
	}
	
	public RatioImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
//		getRatio(attrs);
	}
	
	public RatioImageView(Context context, AttributeSet attrs, int defStyle) {
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
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		int w = getMeasuredWidth();
		setMeasuredDimension(w, (int) (w / getRatio()));
	}
}
