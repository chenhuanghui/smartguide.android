package vn.infory.infory.scancode;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import vn.infory.infory.WebActivity;
import vn.infory.infory.data.PlaceList;
import vn.infory.infory.data.Shop;
import vn.infory.infory.home.LoadingActivity;
import vn.infory.infory.login.InforyLoginActivity.BackListener;
import vn.infory.infory.login.RegisterTypeFragment.Listener;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetPlaceList;
import vn.infory.infory.network.GetPlaceListDetail;
import vn.infory.infory.network.GetShopDetail;
import vn.infory.infory.network.GetShopDetail2;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.network.ScanCode;
import vn.infory.infory.network.ScanCodeRelated;
import vn.infory.infory.shopdetail.ShopDetailActivity;
import vn.infory.infory.shoplist.ShopListActivity;

import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.facebook.FacebookException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Session;
import com.facebook.SessionLoginBehavior;
import com.facebook.SessionState;
import com.facebook.Session.StatusCallback;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.FacebookDialog;
import com.facebook.widget.LoginButton;
import com.facebook.widget.WebDialog;
import com.facebook.widget.WebDialog.OnCompleteListener;
import com.google.android.gms.plus.PlusShare;

import android.R.array;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.Resources.NotFoundException;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.graphics.drawable.AnimationDrawable;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.opengl.Visibility;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.Display;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.MediaController;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.LinearLayout.LayoutParams;
import android.widget.VideoView;

public class ScanCodeResultActivity extends FragmentActivity{
	
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	
	private Listener mListener;
	
	public Activity mAct;
	
	private static Object mScanCodeResult;
	public static Object mScanCodeRelated;
	public static String mQRCode;
	
	private UiLifecycleHelper uiHelper;
	
//	ScanCodeRelatedPagerAdapter mScanCodeRelatedPagerAdapter;	
//	ViewPager mViewPager;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.scan_dlg_2);
		
		uiHelper = new UiLifecycleHelper(this, null);
	    uiHelper.onCreate(savedInstanceState);
        
		mAct = this;	
		
		JSONArray jArr = (JSONArray) mScanCodeResult;	
		
		final LinearLayout linearLayout = (LinearLayout) findViewById(R.id.linearLayoutScanDLG2);
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
							
							FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER);													
							frameLoading.setLayoutParams(loadingParams);
							
							frameLoading.setBackgroundResource(R.drawable.button_loading_big);	
							final AnimationDrawable frameAnimation = (AnimationDrawable) 
									frameLoading.getBackground();
							frameAnimation.start();
							frameImg.addView(frameLoading);
										
							int[] scaled_width_height = getScaledSize(jImage.optInt("width"), jImage.optInt("height"));
										
							LayoutParams params = new LayoutParams(
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
						}
					}
					
					if(jItem.has("video"))
					{
						if(jItem.optJSONObject("video") instanceof JSONObject)
						{
							
							final JSONObject jVideo = jItem.optJSONObject("video");
							
							final FrameLayout frameVideo = new FrameLayout(getApplicationContext());
							
							final FrameLayout frameLoading = new FrameLayout(getApplicationContext());
							
							FrameLayout.LayoutParams loadingParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER);													
							frameLoading.setLayoutParams(loadingParams);
							
							frameLoading.setBackgroundResource(R.drawable.button_loading_big);	
							final AnimationDrawable frameAnimation = (AnimationDrawable) 
									frameLoading.getBackground();
							frameAnimation.start();
							frameVideo.addView(frameLoading);
							
							int[] scaled_width_height = getScaledSize(jVideo.optInt("width"), jVideo.optInt("height"));
							
							LayoutParams params = new LayoutParams(
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
									
									FrameLayout.LayoutParams playButtonParams = new FrameLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT,Gravity.CENTER);
									
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
						wv_small_text.loadData(html_small_text, "text/html; charset=UTF-8", null);
						*/
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
								
								LayoutParams params = new LayoutParams(
								        LayoutParams.WRAP_CONTENT,      
								        LayoutParams.WRAP_CONTENT
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
												GetShopDetail2 task = new GetShopDetail2(getApplicationContext(), jItemButton.optInt("idShop")) {
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
														
														showAlertDialog();
													}
												};
												task.setTaskList(mTaskList);
												task.executeOnExecutor(NetworkManager.THREAD_POOL);
												break;
												
											case 2: //Shop list
												if(jItemButton.has("idPlacelist"))
												{
													LoadingActivity.newInstance(mAct, jItemButton.optInt("idPlacelist"));													
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
												WebActivity.newInstance(mAct, jItemButton.optString("url"));
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
						
						RelativeLayout rlShare = (RelativeLayout) findViewById(R.id.relativeLayoutShare);
						rlShare.setVisibility(View.VISIBLE);
						
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
		
		/*mScanCodeRelatedPagerAdapter = new ScanCodeRelatedPagerAdapter(getSupportFragmentManager());
		mViewPager = (ViewPager) findViewById(R.id.testpager1);
        mViewPager.setAdapter(mScanCodeRelatedPagerAdapter);*/
		
        
        /*mViewPager.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                mViewPager.getParent().requestDisallowInterceptTouchEvent(true);
                return false;
            }
        });*/
		
		ImageButton mBtnBack = (ImageButton) findViewById(R.id.btnBack);
		mBtnBack.setOnClickListener(new View.OnClickListener() {			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				finish();
			}
		});
	}
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);

	    uiHelper.onActivityResult(requestCode, resultCode, data, new FacebookDialog.Callback() {
	        @Override
	        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
	            Log.e("Activity", String.format("Error: %s", error.toString()));
	        }

	        @Override
	        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
	            Log.i("Activity", "Success!");
	        }
	    });
	}
	
	@Override
	protected void onResume() {
	    super.onResume();
	    uiHelper.onResume();
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    uiHelper.onSaveInstanceState(outState);
	}

	@Override
	public void onPause() {
	    super.onPause();
	    uiHelper.onPause();
	}

	@Override
	public void onDestroy() {
	    super.onDestroy();
	    uiHelper.onDestroy();
	}
	
	/*@Override
    public boolean onOptionsItemSelected(MenuItem item) {        
        return super.onOptionsItemSelected(item);
    }
	
    public static class ScanCodeRelatedPagerAdapter extends FragmentStatePagerAdapter {

        public ScanCodeRelatedPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {            
            Bundle args = new Bundle();
            
            setListData();
            Fragment fragment = null;
            
            if(position == 1)
            {            	
                fragment = new ScanCodeRelatedPromotionsFragment();
    			args.putInt(ScanCodeRelatedPromotionsFragment.ARG_OBJECT, position);	
    			fragment.setArguments(args);
            }            
            else
            {
            	fragment = new ScanCodeRelatedShopsFragment();
    			args.putInt(ScanCodeRelatedShopsFragment.ARG_OBJECT, position);	
    			fragment.setArguments(args);
            }
			
//            switch (position) {
//			case 0:
//				fragment = new ScanCodeRelatedShopsFragment();
//				args.putInt(ScanCodeRelatedShopsFragment.ARG_OBJECT, position);	
//				fragment.setArguments(args);
//				break;
//
//			case 1:
//				fragment = new ScanCodeRelatedShopsFragment();
//				args.putInt(ScanCodeRelatedShopsFragment.ARG_OBJECT, position);	
//				fragment.setArguments(args);
//				break;
//			case 2:
//				fragment = new ScanCodeRelatedShopsFragment();
//				args.putInt(ScanCodeRelatedShopsFragment.ARG_OBJECT, position);
//				fragment.setArguments(args);
//				break;
//			}            
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
        	String title = "";
        	switch (position) {
			case 0:
				title = "Địa điểm";
				break;

			case 1:
				title = "Ưu đãi";
				break;
				
			case 2:
				title = "Danh sách";
				break;
			}
            return title;
        }
    }
    
    private static ArrayList<ListModelRelatedShops> arrListModelRelatedShops = new ArrayList<ListModelRelatedShops>();
    private static ArrayList<ListModelRelatedPromotions> arrListModelRelatedPromotions = new ArrayList<ListModelRelatedPromotions>();
	
    public static void setListData()
    {
		JSONArray jArr = (JSONArray) mScanCodeRelated;
		for (int i = 0; i < jArr.length(); i++) 
		{						
			try {
				if(jArr.getJSONObject(i) instanceof JSONObject)
				{
					JSONObject jItem = jArr.getJSONObject(i);							
					
					if(jItem.has("relatedShops"))
					{
						JSONArray jArrRelatedShops = jItem.getJSONArray("relatedShops");
						for(int j = 0; j < jArrRelatedShops.length(); j++){
							JSONObject related_shop_obj = jArrRelatedShops.getJSONObject(j);
							final ListModelRelatedShops related_shop_model = new ListModelRelatedShops();
		                     
			                related_shop_model.setName(related_shop_obj.optString("shopName"));
			                related_shop_model.setDescription(related_shop_obj.optString("description"));
			                related_shop_model.setLogo(related_shop_obj.optString("logo"));
			                    
			                arrListModelRelatedShops.add( related_shop_model );
						}
					}
					
					if(jItem.has("relatedPromotions")){
						JSONArray jArrRelatedPromotions = jItem.getJSONArray("relatedPromotions");
						for(int j = 0; j < jArrRelatedPromotions.length(); j++){
							JSONObject related_promotion_obj = jArrRelatedPromotions.getJSONObject(j);
							final ListModelRelatedPromotions related_promotion_model = new ListModelRelatedPromotions();
		                     
							related_promotion_model.setName(related_promotion_obj.optString("promotionName"));
							related_promotion_model.setDescription(related_promotion_obj.optString("description"));
							related_promotion_model.setLogo(related_promotion_obj.optString("logo"));
			                    
							arrListModelRelatedPromotions.add( related_promotion_model );
						}
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
    }
    
    
    public static class ScanCodeRelatedShopsFragment extends Fragment {

        public static final String ARG_OBJECT = "object";        
            	
    	public ScanCodeResultActivity CustomListView = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {            
            Bundle args = getArguments();
            
            View rootView = inflater.inflate(R.layout.scan_code_related_shop_fragment, container, false);
            
            Resources res =getResources();            
            CustomListView = (ScanCodeResultActivity) getActivity();  
            
            if(args.getInt(ARG_OBJECT) == 0)
            {
            	ScanCodeRelatedListViewAdapter adapter_related_shops = new ScanCodeRelatedListViewAdapter(CustomListView, arrListModelRelatedShops,res);
    			ListView list_related_shop = (ListView)rootView.findViewById(R.id.lstRelatedShops);
    			
    			list_related_shop.setAdapter(adapter_related_shops);
            }
            
            return rootView;
        }
    }
    
    public static class ScanCodeRelatedPromotionsFragment extends Fragment {

        public static final String ARG_OBJECT = "object";        
            	
    	public ScanCodeResultActivity CustomListView = null;

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {            
            Bundle args = getArguments();
            
            View rootView = inflater.inflate(R.layout.scan_code_related_promotion_fragment, container, false);
            
            Resources res = getResources();            
            CustomListView = (ScanCodeResultActivity) getActivity(); 
            
            if(args.getInt(ARG_OBJECT) == 1)
            {
            	ScanCodeRelatedListViewAdapter adapter_related_promotions = new ScanCodeRelatedListViewAdapter(CustomListView, arrListModelRelatedPromotions,res);
    			ListView list_related_promotions = (ListView)rootView.findViewById(R.id.lstRelatedPromotions);
    			
    			list_related_promotions.setAdapter(adapter_related_promotions); 
            }
                       
            return rootView;
        }
    }*/
	
	public static void newInstance(Activity act, Object objScanCode, String code) {
		mScanCodeResult = objScanCode;
		mQRCode = code;
		
		Intent intent = new Intent(act, ScanCodeResultActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
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
}
