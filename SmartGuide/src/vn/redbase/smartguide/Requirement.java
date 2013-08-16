package vn.redbase.smartguide;

public class Requirement {
	int mID;
	int mSGPRequire;
	String mContent;
	
	public Requirement(int id, int sgprequire, String content){
		mID = id;
		mSGPRequire = sgprequire;
		mContent = content;
	}
}
