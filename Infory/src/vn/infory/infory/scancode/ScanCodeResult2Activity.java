package vn.infory.infory.scancode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.WebActivity;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopDetail2;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.ScanCodeRelated;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;
import com.facebook.FacebookException;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.plus.PlusShare;
import com.nineoldandroids.view.ViewHelper;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Bitmap.Config;
import android.graphics.PorterDuff.Mode;
import android.graphics.drawable.AnimationDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.util.SparseArrayCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBarActivity;
import android.text.Spannable;
import android.text.SpannableString;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.AbsListView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;


public class ScanCodeResult2Activity extends FragmentActivity implements ScrollTabHolder, ViewPager.OnPageChangeListener{
	private static AccelerateDecelerateInterpolator sSmoothInterpolator = new AccelerateDecelerateInterpolator();

//	private KenBurnsSupportView mHeaderPicture;
	private View mHeader;

	private PagerSlidingTabStrip mPagerSlidingTabStrip;
	private ViewPager mViewPager;
	private PagerAdapter mPagerAdapter;

	private int mActionBarHeight;
	private int mMinHeaderHeight;
	private int mHeaderHeight;
	private int mMinHeaderTranslation;
	private ImageView mHeaderLogo;

	private RectF mRect1 = new RectF();
	private RectF mRect2 = new RectF();

	private TypedValue mTypedValue = new TypedValue();
	private SpannableString mSpannableString;
	private AlphaForegroundColorSpan mAlphaForegroundColorSpan;
	
	private static Object mScanCodeResult;
	private static Object mScanCodeRelated;
	private static String mQRCode;
	
	private static List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	public Activity mAct;
	private UiLifecycleHelper uiHelper;
	private static int mLayoutScanDLGHeight; 
	
	@ViewById(id = R.id.linearLayoutScanDLG2)		private LinearLayout linearLayout;
	@ViewById(id = R.id.linearLayoutContent)		private LinearLayout linearLayoutContent;
	@ViewById(id = R.id.scrScanDLG2)				private ScrollView mScrScanDLG2;
	@ViewById(id = R.id.linearLayoutCoTheBanThich)	private LinearLayout mLinearLayoutCoTheBanThich;
	@ViewById(id = R.id.btnBack)	private ImageButton mBtnBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);	
		
		/*int scanLayoutHeight = 0;
    	for(int i = 0; i < mLinearLayoutScan.getChildCount(); i++)
    	{
    		View childView = mLinearLayoutScan.getChildAt(i);
    		scanLayoutHeight += childView.getHeight();
    	}
		Toast.makeText(getApplicationContext(), scanLayoutHeight+"", Toast.LENGTH_SHORT).show();*/		

		setContentView(R.layout.scan_dlg_2_2);
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
		mAct = this;	
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
		}
		
		mBtnBack.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
		
		JSONArray jArr = (JSONArray) mScanCodeResult;
		for (int i = 0; i < jArr.length(); i++) 
		{						
			try {
				if(jArr.getJSONObject(i) instanceof JSONObject)
				{
					final JSONObject jItem = jArr.getJSONObject(i);							
					
					if(jItem.has("header"))
					{
						TextView txtHeader = new TextView(getApplicationContext());
						txtHeader.setText(jItem.optString("header"));
						txtHeader.setTextSize(28);
						txtHeader.setTextColor(Color.BLACK);
						txtHeader.setGravity(Gravity.CENTER);
						txtHeader.setPadding(50, 0, 50, 20);
						
						FontsCollection.setFontForTextView(txtHeader, "sfufuturabook");
						   
						linearLayout.addView(txtHeader);
						
						/*linearLayout.measure(0, 0);
						mLayoutScanDLGHeight += linearLayout.getMeasuredHeight();
						Log.i("header", linearLayout.getMeasuredHeight()+"");*/
					}
					
					if(jItem.has("bigText"))
					{
						TextView txtBigText = new TextView(getApplicationContext());
						txtBigText.setText(jItem.optString("bigText"));

//								txtBigText.setTextAppearance(getActivity(), R.style.BigText);
						
						txtBigText.setTextSize(16);
						txtBigText.setTextColor(Color.BLACK);
						txtBigText.setPadding(50, 0, 50, 20);	
						
						FontsCollection.setFontForTextView(txtBigText, "sfufuturabook");
						
						linearLayout.addView(txtBigText);
						
						/*linearLayout.measure(0, 0);
						mLayoutScanDLGHeight += linearLayout.getMeasuredHeight();
						Log.i("bigText", linearLayout.getMeasuredHeight()+"");*/
						
						/*String html_text = "<html><head></head><body style=\"text-align:justify;background-color:#EBEBEB;padding-left: 20px;padding-right: 20px;\">"+ jItem.optString("bigText") +"</body></html>";
						WebView wv = new WebView(getApplicationContext());
						wv.setVerticalScrollBarEnabled(false);
						wv.loadData(html_text, "text/html; charset=UTF-8", null);
						linearLayout.addView(wv);*/
					}
					
					if(jItem.has("image"))
					{
						if(jItem.optJSONObject("image") instanceof JSONObject)
						{									
							JSONObject jImage = jItem.optJSONObject("image");
														
							final FrameLayout frameImg = new FrameLayout(getApplicationContext());
							
							final FrameLayout frameLoading = new FrameLayout(getApplicationContext());
							
							FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);													
							frameLoading.setLayoutParams(loadingParams);
							
							frameLoading.setBackgroundResource(R.drawable.button_loading_big);	
							final AnimationDrawable frameAnimation = (AnimationDrawable) 
									frameLoading.getBackground();
							frameAnimation.start();
							frameImg.addView(frameLoading);
										
							int[] scaled_width_height = getScaledSize(jImage.optInt("width"), jImage.optInt("height"));
										
							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
									scaled_width_height[0],
									scaled_width_height[1]
							);
							params.setMargins(0, 0, 0, 20);
							frameImg.setLayoutParams(params);
							
							final ImageView imgView = new ImageView(getApplicationContext());	
							
							CyImageLoader.instance().loadImage(jImage.optString("url"), new CyImageLoader.Listener(){
								@Override
								public void loadFinish(int from,
										Bitmap image, String url,
										CyAsyncTask task) {
									// TODO Auto-generated method stub
									frameLoading.setVisibility(View.GONE);
									imgView.setImageBitmap(image);
								}
								
							}, new Point(), getApplicationContext());
							
							frameImg.addView(imgView);
							linearLayout.addView(frameImg);
							
							linearLayout.measure(0, 0);
							mLayoutScanDLGHeight += linearLayout.getMeasuredHeight();
						}
					}
					
					if(jItem.has("video"))
					{
						if(jItem.optJSONObject("video") instanceof JSONObject)
						{
							
							final JSONObject jVideo = jItem.optJSONObject("video");
							
							final FrameLayout frameVideo = new FrameLayout(getApplicationContext());
							
							final FrameLayout frameLoading = new FrameLayout(getApplicationContext());
							
							FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);													
							frameLoading.setLayoutParams(loadingParams);
							
							frameLoading.setBackgroundResource(R.drawable.button_loading_big);	
							final AnimationDrawable frameAnimation = (AnimationDrawable) 
									frameLoading.getBackground();
							frameAnimation.start();
							frameVideo.addView(frameLoading);
							
							int[] scaled_width_height = getScaledSize(jVideo.optInt("width"), jVideo.optInt("height"));
							
							FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
									scaled_width_height[0],
									scaled_width_height[1]
							);
							params.setMargins(0, 0, 0, 20);
//							FrameLayout.LayoutParams frameLayoutParams = new FrameLayout.LayoutParams(scaled_width_height[0],scaled_width_height[1],Gravity.CENTER);							
							frameVideo.setLayoutParams(params);
							
							final ImageView thumb = new ImageView(getApplicationContext());		
							final ImageView playButton = new ImageView(getApplicationContext());
							
							CyImageLoader.instance().loadImage(jVideo.optString("thumbnail"), new CyImageLoader.Listener(){								
								@Override
								public void loadFinish(int from,
										Bitmap image, String url,
										CyAsyncTask task) {
									// TODO Auto-generated method stub
									
									frameLoading.setVisibility(View.GONE);								
									thumb.setImageBitmap(image);	
									frameVideo.addView(thumb);
									
									FrameLayout.LayoutParams playButtonParams = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.WRAP_CONTENT,FrameLayout.LayoutParams.WRAP_CONTENT,Gravity.CENTER);
									
									playButton.setImageResource(R.drawable.play_icon);	
									playButton.setLayoutParams(playButtonParams);
									
									frameVideo.addView(playButton);
									playButton.bringToFront();
									frameVideo.requestLayout();									
								}								
							}, new Point(), getApplicationContext());	
							
							linearLayout.addView(frameVideo);
							
							/*final VideoView video = new VideoView(this);
							MediaController mediaController = new MediaController(this);
							mediaController.setAnchorView(video);
							video.setMediaController(mediaController);

							video.setVideoURI(Uri.parse(jVideo.optString("url")));
							
							video.setLayoutParams(params);
							video.setVisibility(View.GONE);							
							linearLayout.addView(video);*/
							
							playButton.setOnClickListener(new View.OnClickListener() {								
								@Override
								public void onClick(View v) {
									// TODO Auto-generated method stub
									/*linearLayout.removeView(frameVideo);
									
									video.setVisibility(View.VISIBLE);
									video.start();*/
									
									Intent intent = new Intent(android.content.Intent.ACTION_VIEW);
									Uri data = Uri.parse(jVideo.optString("url"));
									intent.setData(data);
									startActivity(intent);
								}
							});				
							
						}
					}

					if(jItem.has("smallText"))
					{
						TextView txtSmallText = new TextView(getApplicationContext());
						txtSmallText.setText(jItem.optString("smallText"));
						txtSmallText.setTextSize(14);
						txtSmallText.setTextColor(Color.GRAY);
						txtSmallText.setPadding(50, 0, 50, 20);
						
						FontsCollection.setFontForTextView(txtSmallText, "sfufuturabook");
						
						linearLayout.addView(txtSmallText);
												
						/*String html_small_text = "<html><head></head><body style=\"text-align:justify; font-size:14px; color:gray;background-color:#EBEBEB;padding-left: 20px;padding-right: 20px;\">"+ jItem.optString("smallText") +"</body></html>";
						WebView wv_small_text = new WebView(getApplicationContext());
						wv_small_text.setVerticalScrollBarEnabled(false);
						wv_small_text.loadData(html_small_text, "text/html; charset=UTF-8", null);*/
						
					}		
					
					if(jItem.has("buttons"))
					{						
						if(jItem.optJSONArray("buttons") instanceof JSONArray)
						{
							JSONArray jArrButton = jItem.optJSONArray("buttons");
							
							if(jArrButton.length() > 0)
							{
								LinearLayout btnLayout = new LinearLayout(getApplicationContext());
								btnLayout.setOrientation(LinearLayout.HORIZONTAL);
								btnLayout.setGravity(Gravity.CENTER);
								
								LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
										LinearLayout.LayoutParams.WRAP_CONTENT,      
										LinearLayout.LayoutParams.WRAP_CONTENT
								);
								params.setMargins(0, 0, 0, 20);
								btnLayout.setLayoutParams(params);
								
								for (int j = 0; j < jArrButton.length(); j++) 
								{
									final JSONObject jItemButton = jArrButton.getJSONObject(j);
									
									final Button btn = new Button(getApplicationContext());
									btn.setText(jItemButton.optString("actionTitle"));
									
									FontsCollection.setFontForButton(btn, "sfufuturabook");
																				
									switch (jItemButton.optInt("color")) 
									{
										case 1: //grey
											btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_viewer_buttongrey));
											break;

										default:
											btn.setBackgroundDrawable(getResources().getDrawable(R.drawable.bg_viewer_button));
											break;
									}						
									
									btn.setPadding(50, 0, 50, 0);									
									btnLayout.addView(btn);																
									
									btn.setOnClickListener(new View.OnClickListener() {
										@Override
										public void onClick(View v) {
											// TODO Auto-generated method stub
											switch (jItemButton.optInt("actionType")) {
											case 1: //Shop user									
												try
												{													
													GetShopDetail2 task = new GetShopDetail2(mAct, jItemButton.optInt("idShop")) {
														@Override
														protected void onCompleted(Object result2) {
															mTaskList.remove(this);
															
															JSONObject jShop = (JSONObject) result2;
															
															Shop shop = new Shop();
															shop.idShop	= jShop.optInt("idShop");
															shop.shopName	= jShop.optString("shopName");
															shop.numOfView = jShop.optString("numOfView");
															shop.logo		= jShop.optString("logo");
															
															ShopDetailActivity.newInstance(mAct, shop);
														}

														@Override
														protected void onFail(Exception e) {
															mTaskList.remove(this);
															ShopDetailActivity.newInstanceNoReload(mAct, new Shop());
														}
													};
													task.setTaskList(mTaskList);
													task.executeOnExecutor(NetworkManager.THREAD_POOL);							
												}
												catch(Exception e)
												{
													ShopDetailActivity.newInstanceNoReload(mAct, new Shop());
												}
												break;
												
											case 2: //Shop list
												if(jItemButton.has("idPlacelist"))
												{
//													LoadingActivity.newInstance(mAct, jItemButton.optInt("idPlacelist"));
													ShopListActivity.newInstanceWithPlacelistId(mAct, jItemButton.optInt("idPlacelist")+"", new ArrayList<Shop>());
												}												
												else if(jItemButton.has("keywords"))
												{
													ShopListActivity.newInstance(mAct, jItemButton.optString("keywords"), new ArrayList<Shop>());
												}
												else if(jItemButton.has("idShops"))
												{
													ShopListActivity.newInstance(mAct, jItemButton.optString("idShops"), new ArrayList<Shop>(),0);
												}												
												break;

											case 3: //popup url (tutorial,...)
												String url = jItemButton.optString("url");
												if(url.startsWith("www.")) {
													url = "http://" + url;
												}
												WebActivity.newInstance(mAct, url);
												break;
											}
										}
									});
								}
								linearLayout.addView(btnLayout);	
							}
						}
						else if (jItem.optJSONObject("buttons") instanceof JSONObject) 
						{
							
						}
					}
					
					if(jItem.has("linkToShare"))
					{
						LinearLayout llLineComment = (LinearLayout) findViewById(R.id.linearLayoutLineComment);
						llLineComment.setVisibility(View.VISIBLE);
						
						LinearLayout llShare = (LinearLayout) findViewById(R.id.linearLayoutShare);
						llShare.setVisibility(View.VISIBLE);
						
						TextView txtShare = (TextView)findViewById(R.id.txtShare);
						FontsCollection.setFontForTextView(txtShare, "sfufuturabook");
						
						ImageButton imgBtnShareFB = (ImageButton) findViewById(R.id.btnShareFb);
						imgBtnShareFB.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								/*try{
								    ApplicationInfo info = getPackageManager().
								            getApplicationInfo("com.facebook.katana", 0 );
								} catch( PackageManager.NameNotFoundException e ){
									Toast.makeText(getApplicationContext(), "Vui lòng cài đặt Facebook trước khi chia sẻ", Toast.LENGTH_LONG).show();
								    return;
								}*/
								
								if(FacebookDialog.canPresentShareDialog(getApplicationContext(), 
							            FacebookDialog.ShareDialogFeature.SHARE_DIALOG)) {
									FacebookDialog shareDialog = new FacebookDialog.ShareDialogBuilder(mAct)
																			        .setLink(jItem.optString("linkToShare"))
																			        .build();
									uiHelper.trackPendingDialogCall(shareDialog.present());
								}
								else
								{			
									Session session = new Session(mAct);
									Session.OpenRequest request = new Session.OpenRequest(mAct);
									request.setCallback(new Session.StatusCallback() {

										@Override
										public void call(Session session,
												SessionState state,
												Exception exception) {
											// TODO Auto-generated method stub	
											if(session.isOpened())
												publishFeedDialog(session,jItem.optString("linkToShare"));
										}});
									request.setLoginBehavior(SessionLoginBehavior.SUPPRESS_SSO); // <-- this is the important line
									session.openForRead(request);
									Session.setActiveSession(session);
									
										
								}
							}
						});
						
						ImageButton imgBtnShareGG = (ImageButton) findViewById(R.id.btnShareGoogle);
						imgBtnShareGG.setOnClickListener(new View.OnClickListener() {
							
							@Override
							public void onClick(View v) {
								// TODO Auto-generated method stub
								Intent shareIntent = new PlusShare.Builder(mAct)
														          .setType("text/plain")
														          .setText("")
														          .setContentUrl(Uri.parse(jItem.optString("linkToShare")))
														          .getIntent();

						      startActivityForResult(shareIntent, 0);
							}
						});
					}
				}
				else if (jArr.getJSONArray(i) instanceof JSONArray) 
				{
					JSONArray jChildArr = jArr.getJSONArray(i);
					if(jChildArr.length() > 0)
					{
						
					}
				}
			} catch (NotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		
		ViewTreeObserver vto = linearLayoutContent.getViewTreeObserver();
		vto.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {

		    @Override
		    public void onGlobalLayout() {
		        ViewTreeObserver obs = linearLayoutContent.getViewTreeObserver();

		        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
		            obs.removeGlobalOnLayoutListener(this);
		        } else {
		            obs.removeGlobalOnLayoutListener(this);
		        }
		        
		        mLayoutScanDLGHeight = linearLayoutContent.getHeight();
		        Log.i("test", mLayoutScanDLGHeight+"");
		        
		        Display display = getWindowManager().getDefaultDisplay();
				int maxHeight = display.getHeight();
				int visibleHeight = maxHeight - ((int)(maxHeight/3)) - 48; //Trừ 48 là chiều cao của btnBack
					
				Log.i("height", mLayoutScanDLGHeight+"");
				if(mLayoutScanDLGHeight >= visibleHeight)
				{
					LinearLayout.LayoutParams lp = (LinearLayout.LayoutParams) mScrScanDLG2.getLayoutParams();
					lp.width = LinearLayout.LayoutParams.MATCH_PARENT;
					lp.height = visibleHeight;
					mScrScanDLG2.requestLayout();
					
					mLayoutScanDLGHeight = visibleHeight;
				}
				/*else
				{
					mLayoutScanDLGHeight -= 100;
//					Log.i("sau", mLayoutScanDLGHeight+"");
				}*/

				
//				mMinHeaderHeight = 250;
				mHeaderHeight = mLayoutScanDLGHeight;
				mMinHeaderTranslation = -mLayoutScanDLGHeight;

				mHeader = findViewById(R.id.header);

				mPagerSlidingTabStrip = (PagerSlidingTabStrip) findViewById(R.id.tabs);
				mViewPager = (ViewPager) findViewById(R.id.pager);
				mViewPager.setOffscreenPageLimit(4);

				mPagerAdapter = new PagerAdapter(getSupportFragmentManager());
				mPagerAdapter.setTabHolderScrollingContent((ScrollTabHolder)mAct);

				mViewPager.setAdapter(mPagerAdapter);

				mPagerSlidingTabStrip.setViewPager(mViewPager);
				mPagerSlidingTabStrip.setOnPageChangeListener((ViewPager.OnPageChangeListener)mAct);
				mAlphaForegroundColorSpan = new AlphaForegroundColorSpan(0xffffffff);
		    }

		});			
	}


	@Override
	public void onPageScrollStateChanged(int arg0) {
		// nothing
	}

	@Override
	public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
		// nothing
	}

	@Override
	public void onPageSelected(int position) {
		SparseArrayCompat<ScrollTabHolder> scrollTabHolders = mPagerAdapter.getScrollTabHolders();
		ScrollTabHolder currentHolder = scrollTabHolders.valueAt(position);

		currentHolder.adjustScroll((int) (mHeader.getHeight() + ViewHelper.getTranslationY(mHeader)));
	}

	@Override
	public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount, int pagePosition) {
		if (mViewPager.getCurrentItem() == pagePosition) {
			int scrollY = getScrollY(view);
			ViewHelper.setTranslationY(mHeader, Math.max(-scrollY, mMinHeaderTranslation));
//			float ratio = clamp(ViewHelper.getTranslationY(mHeader) / mMinHeaderTranslation, 0.0f, 1.0f);
//			interpolate(mHeaderLogo, getActionBarIconView(), sSmoothInterpolator.getInterpolation(ratio));
//			setTitleAlpha(clamp(5.0F * ratio - 4.0F, 0.0F, 1.0F));
		}
	}

	@Override
	public void adjustScroll(int scrollHeight) {
		// nothing
	}

	public int getScrollY(AbsListView view) {
		View c = view.getChildAt(0);
		if (c == null) {
			return 0;
		}

		int firstVisiblePosition = view.getFirstVisiblePosition();
		int top = c.getTop();

		int headerHeight = 0;
		if (firstVisiblePosition >= 1) {
			headerHeight = mHeaderHeight;
		}

		return -top + firstVisiblePosition * c.getHeight() + headerHeight;
	}

	public static float clamp(float value, float max, float min) {
		return Math.max(Math.min(value, min), max);
	}

	private void interpolate(View view1, View view2, float interpolation) {
		getOnScreenRect(mRect1, view1);
		getOnScreenRect(mRect2, view2);

		float scaleX = 1.0F + interpolation * (mRect2.width() / mRect1.width() - 1.0F);
		float scaleY = 1.0F + interpolation * (mRect2.height() / mRect1.height() - 1.0F);
		float translationX = 0.5F * (interpolation * (mRect2.left + mRect2.right - mRect1.left - mRect1.right));
		float translationY = 0.5F * (interpolation * (mRect2.top + mRect2.bottom - mRect1.top - mRect1.bottom));

		ViewHelper.setTranslationX(view1, translationX);
		ViewHelper.setTranslationY(view1, translationY - ViewHelper.getTranslationY(mHeader));
		ViewHelper.setScaleX(view1, scaleX);
		ViewHelper.setScaleY(view1, scaleY);
	}

	private RectF getOnScreenRect(RectF rect, View view) {
		rect.set(view.getLeft(), view.getTop(), view.getRight(), view.getBottom());
		return rect;
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public int getActionBarHeight() {
		if (mActionBarHeight != 0) {
			return mActionBarHeight;
		}
		
		if(Build.VERSION.SDK_INT > Build.VERSION_CODES.HONEYCOMB){
			getTheme().resolveAttribute(android.R.attr.actionBarSize, mTypedValue, true);
		}else{
			getTheme().resolveAttribute(R.attr.actionBarSize, mTypedValue, true);
		}
		
		mActionBarHeight = TypedValue.complexToDimensionPixelSize(mTypedValue.data, getResources().getDisplayMetrics());
		
		return mActionBarHeight;
	}

	private void setTitleAlpha(float alpha) {
		mAlphaForegroundColorSpan.setAlpha(alpha);
		mSpannableString.setSpan(mAlphaForegroundColorSpan, 0, mSpannableString.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//		getSupportActionBar().setTitle(mSpannableString);
	}

	@TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private ImageView getActionBarIconView() {
		
		if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB){
			return (ImageView)findViewById(android.R.id.home);
		}

		return (ImageView)findViewById(android.support.v7.appcompat.R.id.home);
	}

	public class PagerAdapter extends FragmentPagerAdapter {

		private SparseArrayCompat<ScrollTabHolder> mScrollTabHolders;
		private final String[] TITLES = { "Page 1", "Page 2", "Page 3", "Page 4"};
		private ScrollTabHolder mListener;

		public PagerAdapter(FragmentManager fm) {
			super(fm);
			mScrollTabHolders = new SparseArrayCompat<ScrollTabHolder>();
		}

		public void setTabHolderScrollingContent(ScrollTabHolder listener) {
			mListener = listener;
		}

		@Override
		public CharSequence getPageTitle(int position) {
			return TITLES[position];
		}

		@Override
		public int getCount() {
			return TITLES.length;
		}

		@Override
		public Fragment getItem(int position) {
			ScrollTabHolderFragment fragment = (ScrollTabHolderFragment) SampleListFragment.newInstance(mAct,position,mLayoutScanDLGHeight+48+30+3+48+30);
																																		//48: btnBack
																																		//30: có thể bạn thích
																																		//3: line ngăn cách
																																		//48: tabs
																																		//29: ko biết (+ vào thì khi chuyển tab ko bị giật)
//			Log.i("h", mLayoutScanDLGHeight+"");
			mScrollTabHolders.put(position, fragment);
			if (mListener != null) {
				fragment.setScrollTabHolder(mListener);
			}

			return fragment;
		}

		public SparseArrayCompat<ScrollTabHolder> getScrollTabHolders() {
			return mScrollTabHolders;
		}

	}
	
	public static void newInstance(final Activity act, Object objScanCode, String code) {
		mScanCodeResult = objScanCode;
		mQRCode = code;
		
		ScanCodeRelated scanCodeRelatedTask = new ScanCodeRelated(act, mQRCode, 0, 0)
		{
			@Override
			protected void onCompleted(Object result3) throws Exception {
				mTaskList.remove(this);		
								
				mScanCodeRelated = result3;
				
				Intent intent = new Intent(act, ScanCodeResult2Activity.class);
				act.startActivity(intent);
				act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);
			}
		};    
		
		mTaskList.add(scanCodeRelatedTask);
		scanCodeRelatedTask.executeOnExecutor(NetworkManager.THREAD_POOL);		
	}	
	
	public int[] getScaledSize(int server_width, int server_height) {
		
		//Set image width and height
		WindowManager wm = (WindowManager) getApplicationContext().getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int height = size.y;
									
		if(server_width > width) //Nếu hình trên server > màn hình thì scale lại
		{
			height = (int) width*server_height/server_width;
		}
		else
		{
			width = server_width;
			height = server_height;
		}
		int[] data = {width,height};
		return data;
	}
	
	public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }
	
	public void showAlertDialog() {
		AlertDialog.Builder builder = new Builder(mAct);
		builder.setCancelable(false);
		builder.setMessage("Không có dữ liệu!");
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface arg0, int arg1) {
				mAct.finish();
			}
		});
		builder.create().show();
	}
	
	private void publishFeedDialog(Session session,String link) {
		if (Session.getActiveSession() != null || Session.getActiveSession().isOpened()) {
			Bundle params = new Bundle();
		    params.putString("link", link);

		    WebDialog feedDialog = (
		        new WebDialog.FeedDialogBuilder(mAct,
		            session,
		            params))
		        .setOnCompleteListener(new OnCompleteListener() {

		            @Override
		            public void onComplete(Bundle values,
		                FacebookException error) {
		                if (error == null) {
		                    // When the story is posted, echo the success
		                    // and the post Id.
		                    /*final String postId = values.getString("post_id");
		                    if (postId != null) {
		                        Toast.makeText(mAct,"Chia sẻ thành công!",Toast.LENGTH_SHORT).show();
		                    }*/ 
		                    /*else {
		                        // User clicked the Cancel button
		                        Toast.makeText(getApplicationContext(), 
		                            "Publish cancelled", 
		                            Toast.LENGTH_SHORT).show();
		                    }*/
		                } 
		                /*else if (error instanceof FacebookOperationCanceledException) {
		                    // User clicked the "x" button
		                    Toast.makeText(mAct, 
		                        "Publish cancelled", 
		                        Toast.LENGTH_SHORT).show();
		                } */
		                else {
		                    // Generic, ex: network error
		                    Toast.makeText(mAct, 
		                        "Chia sẻ thất bại! Vui lòng thử lại", 
		                        Toast.LENGTH_SHORT).show();
		                }
		            }
		        })
		        .build();
		    feedDialog.show();			
		}	    
	}
}
