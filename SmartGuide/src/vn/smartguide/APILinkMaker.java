package vn.smartguide;

public final class APILinkMaker {
	public static String mHostName = "https://devapi.smartguide.vn/";
//2	public static String mHostName = "https://api.smartguide.vn/";
	private static String mApiDomain = "api/";
	
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
	private static String mReview				= 	"feedback";
	private static String mContact				=	"contact/import";
	private static String mSearch				=	"shop/search";
	private static String mFeedback				=	"get_feedback";
	private static String mGetRewardList		=	"reward/list";
	private static String mGetReward			=	"reward/receive";
	private static String mFaceAccessToken		=	"user/facebook/access_token";
	private static String mRegistration 		= 	"user/createRegistrationCode";
	private static String mEmergence			=   "notification";
	
	public static String ShopListInCategory(){
		return mHostName + mApiDomain + mShopListInCategory;
	}
	
	public static String mGroupByCity(){
		return mHostName + mApiDomain + mGroupByCity;
	}
	
	public static String mGetVersion(){
		return mHostName + mApiDomain + mVersion;
	}
	
	public static String mPushInforFacebook(){
		return mHostName + mApiDomain + mInfoFacebook;
	}
	
	public static String mGetCityList(){
		return mHostName + mApiDomain + mCityList;
	}
	
	public static String mGetPromotionDetail() {
		return mHostName + mApiDomain + mPromotionDetail;
	}
	
	public static String mGetShopUser() {
		return mHostName + mApiDomain + mShopUser;
	}
	
	public static String mGetShopItem() {
		return mHostName + mApiDomain + mShopItem;
	}
	
	public static String mGetUserImage() {
		return mHostName + mApiDomain + mUserImage;
	}
	
	public static String mGetShopImage() {
		return mHostName + mApiDomain + mShopImage;
	}
	
	public static String mUploadImage(){
		return mHostName + mApiDomain + mUpImage;
	}
	
	public static String mGetTotalSGP(){
		return mHostName + mApiDomain + mTotalSGP;
	}
	
	public static String mGetSGP(){
		return mHostName + mApiDomain + mSGP;
	}
	
	public static String mGetAds(){
		return mHostName + mApiDomain + mAds;
	}
	
	public static String mGetCommentGet() {
		return mHostName + mApiDomain + mCommentGet;
	}
	
	public static String mGetCommentPost() {
		return mHostName + mApiDomain + mCommentPost;
	}
	
	public static String mPushLikeAction(){
		return mHostName + mApiDomain + mLikeAction;
	}
	
	public static String mPushUnlikeAction(){
		return mHostName + mApiDomain + mUnlikeAction;
	}
	
	public static String mGetAwardType1(){
		return mHostName + mApiDomain + mAwardType1;
	}
	
	public static String mGetAwardType2(){
		return mHostName + mApiDomain + mAwardType2;
	}
	
	public static String mGetUserCollection(){
		return mHostName + mApiDomain + mCollection;
	}
	
	public static String mPostReview(){
		return mHostName + mApiDomain + mReview;
	}
	
	public static String mPostContact(){
		return mHostName + mApiDomain + mContact;
	}
	
	public static String mSearch(){
		return mHostName + mApiDomain + mSearch;
	}
	
	public static String mGetFeedback(){
		return mHostName + mApiDomain + mFeedback;
	}

	public static String mGetRewardList(){
		return mHostName + mApiDomain + mGetRewardList;
	}
	
	public static String mGetReward(){
		return mHostName + mApiDomain + mGetReward;
	}
	
	public static String mUpFaceAccessToken(){
		return mHostName + mApiDomain + mFaceAccessToken;
	}
	
	public static String mUpRegistration(){
		return mHostName + mApiDomain + mRegistration;
	}
	
	public static String mCheckEmergence(){
		return mHostName + mEmergence;
	}
	
}
