package vn.redbase.smartguide;

import android.graphics.Bitmap;
import android.view.View;

/**
 * Created by ChauSang on 7/23/13.
 */
public class Comment {
	public String mUser;
	public String mComment;
	public String mAvaUrl;
	public String mTime;
	public Bitmap mAva;
	public boolean mLoading;	

	public Comment(String user, String comment, String url, String time){
		mUser = user;
		mComment = comment;
		mAvaUrl = url;
		mTime = time;
	}
}
