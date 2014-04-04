package vn.infory.infory;

import android.util.Log;

public class CyLogger {
	
	public static final boolean sEnable = true;
	
	public boolean 	mEnable;
	public String 	mHeader;
	public String 	mTag = "CycrixDebug";
	
	public CyLogger(String header, boolean enable) {
		mHeader = header;
		mEnable = enable;
	}
	
	public void d(String msg) {
		if (sEnable && mEnable)
			Log.d(mTag, mHeader + " " + msg);
	}
}
