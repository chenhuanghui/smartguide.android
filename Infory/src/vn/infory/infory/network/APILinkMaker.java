package vn.infory.infory.network;

public class APILinkMaker {
	private static final String mPrefix	= NetworkManager.mHostName + NetworkManager.mApiDomain;
	
	// v2
	public static final String mGetActivateCode 	= NetworkManager.mHostName + "/user/activation?phone=";
	public static final String mCheckActivateCode 	= NetworkManager.mHostName + "/user/check_v2";
	public static final String mEmergency 			= NetworkManager.mHostName + "/notification_v2";
	
	public static final String mUpdateProfile	 	= mPrefix + "user/updateProfile";
	public static final String mUploadSocialProfile	= mPrefix + "user/uploadSocialProfile";
	public static final String mUploadAva		 	= mPrefix + "user/uploadAvatar";
	public static final String mProfile	 			= mPrefix + "user/profile";
	public static final String mGetAvatarList		= mPrefix + "user/avatar/get";
	
	public static final String mSearch 				= mPrefix + "shop/search_v2_1";
	public static final String mPlaceListList		= mPrefix + "placelist/getList";
	public static final String mPlaceList			= mPrefix + "placelist/get";
	public static final String mPlaceListDetail		= mPrefix + "placelist/getDetail";
	public static final String mShopDetail			= mPrefix + "shop/user_v2";
	public static final String mShopDetailInfo		= mPrefix + "shop/detailinfo";
	public static final String mHome				= mPrefix + "user/home";
	public static final String mPromotion			= mPrefix + "user/promotion";
	public static final String mShopList			= mPrefix + "shop/getShopList";
	
	public static final String mUserGallery			= mPrefix + "images/getUserGallery";
	public static final String mShopGallery			= mPrefix + "images/getShopGallery";
	public static final String mComment				= mPrefix + "comment/getShopComment";
	public static final String mLikeComment			= mPrefix + "user/agreeComment";
	
	public static final String mScan				= mPrefix + "user/scanSGCode_v2";
	
	public static final String mAutoComplete		= NetworkManager.mHostName + ":9200/data/_search";
	
	// v1
//	public static final String mCheckEmergence 		= NetworkManager.mHostName + "notification";
//	
//	public static final String mDefaultAvatar		= mPrefix + "user/avatar/get";
//	public static final String mUserInfo			= mPrefix + "user/sginfo/update";
//	public static final String mInfoFacebook		= mPrefix + "user/info/update";
//	public static final String mShopCategory 		= mPrefix + "group";
//	public static final String mShopListInCategory 	= mPrefix + "shop/list";
//	public static final String mShopUser 			= mPrefix + "shop/user";
//	public static final String mShopSearch 			= mPrefix + "shop/search_v2";
//	public static final String mAds 				= mPrefix + "ads/get";
//	public static final String mUserCollection		= mPrefix + "user/collection";
//	public static final String mGetRewardList		= mPrefix + "reward/list";
//	public static final String mGetReward			= mPrefix + "reward/receive";
//	public static final String mFeedback			= mPrefix + "get_feedback";
//	public static final String mReview				= mPrefix + "feedback";
//	public static final String mLikeAction			= mPrefix + "user/like";
//	public static final String mCommentPost 		= mPrefix + "comment/post";
//	public static final String mFaceAccessToken		= mPrefix + "user/facebook/access_token";
//	public static final String mUpImage 			= mPrefix + "images/upload";
//	public static final String mSGP 				= mPrefix + "user/scan_sgcode";
//	public static final String mComment 			= mPrefix + "comment/get";
}
