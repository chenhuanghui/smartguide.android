package vn.smartguide;

public final class APILinkMaker {
	private static String mHostName = "https://devapi.smartguide.vn/api/";

	private static String mShopListInCategory 	= 	"shop/list"; 
	private static String mGroupByCity 			= 	"group";
	private static String mVersion 				= 	"version";
	private static String mInfoFacebook			= 	"user/info/update";
	private static String mCityList 			= 	"city/list1";
	private static String mPromotionDetail 		= 	"shop/promotion/get";
	private static String mShopUser 			= 	"shop/user";
	private static String mShopItem 			= 	"item/get";
	private static String mUserImage 			= 	"images/user/get";
	private static String mShopImage 			= 	"images/gallery/get";
	private static String mUpImage 				= 	"images/upload";
	private static String mSGP 					= 	"user/get/promotion1/point";
	private static String mAds 					= 	"ads/get";
	private static String mTotalSGP 			= 	"score/get";
	private static String mCommentGet 			= 	"comment/get";
	private static String mCommentPost 			= 	"comment/post";
	private static String mLikeAction			= 	"user/like";
	private static String mUnlikeAction			= 	"user/unlike";
	private static String mAwardType1			= 	"user/get/promotion1/award";
	private static String mAwardType2			= 	"user/get/promotion2";
	private static String mCollection			= 	"user/collection";
	
	public static String ShopListInCategory(){
		return mHostName + mShopListInCategory;
	}
	
	public static String mGroupByCity(){
		return mHostName + mGroupByCity;
	}
	
	public static String mGetVersion(){
		return mHostName + mVersion;
	}
	
	public static String mPushInforFacebook(){
		return mHostName + mInfoFacebook;
	}
	
	public static String mGetCityList(){
		return mHostName + mCityList;
	}
	
	public static String mGetPromotionDetail() {
		return mHostName + mPromotionDetail;
	}
	
	public static String mGetShopUser() {
		return mHostName + mShopUser;
	}
	
	public static String mGetShopItem() {
		return mHostName + mShopItem;
	}
	
	public static String mGetUserImage() {
		return mHostName + mUserImage;
	}
	
	public static String mGetShopImage() {
		return mHostName + mShopImage;
	}
	
	public static String mUploadImage(){
		return mHostName + mUpImage;
	}
	
	public static String mGetTotalSGP(){
		return mHostName + mTotalSGP;
	}
	
	public static String mGetSGP(){
		return mHostName + mSGP;
	}
	
	public static String mGetAds(){
		return mHostName + mAds;
	}
	
	public static String mGetCommentGet() {
		return mHostName + mCommentGet;
	}
	
	public static String mGetCommentPost() {
		return mHostName + mCommentPost;
	}
	
	public static String mPushLikeAction(){
		return mHostName + mLikeAction;
	}
	
	public static String mPushUnlikeAction(){
		return mHostName + mUnlikeAction;
	}
	
	public static String mGetAwardType1(){
		return mHostName + mAwardType1;
	}
	
	public static String mGetAwardType2(){
		return mHostName + mAwardType2;
	}
	
	public static String mGetUserCollection(){
		return mHostName + mCollection;
	}
}
