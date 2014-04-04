package vn.infory.infory.shopdetail;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ScrollView;

public class NonChildFocusScrollView extends ScrollView {
	
	public NonChildFocusScrollView(Context context) {
		super(context);
	}
	
	public NonChildFocusScrollView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NonChildFocusScrollView(Context context, AttributeSet attrs,
			int defStyle) {
		super(context, attrs, defStyle);
	}
	
	public void requestChildFocus(android.view.View child, android.view.View focused) {}
	public boolean requestChildRectangleOnScreen(android.view.View child, android.graphics.Rect rectangle, boolean immediate) {
		return false;
	};
}
