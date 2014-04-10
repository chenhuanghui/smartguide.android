package vn.infory.infory.shopdetail;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

public class CommentLayout extends RelativeLayout {
	
	private MeasureListener mListener;
	public	int mPos;
	
	public CommentLayout(Context context) {
		super(context);
	}
	
	public CommentLayout(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public CommentLayout(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		
		if (mListener != null)
			mListener.measure(this);
	}
	
	public void setListener(MeasureListener listener) {
		mListener = listener;
	}
	
	public void setPos(int pos) {
		mPos = pos;
	}
	
	public interface MeasureListener {
		public void measure(CommentLayout thiz);
	}
}
