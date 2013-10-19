package vn.smartguide;

import android.graphics.Bitmap;

public class Comment {
	public String mUser;
	public String mComment;
	public String mAvaUrl;
	public String mTime;
	public String mFullTime;

	public Comment(String user, String comment, String url, String time, String fullTime){
		mUser = user;
		mComment = comment;
		mAvaUrl = url;
		mTime = time;
		mFullTime = fullTime;
	}
}
