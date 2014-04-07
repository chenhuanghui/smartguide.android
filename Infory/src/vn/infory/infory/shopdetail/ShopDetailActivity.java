package vn.infory.infory.shopdetail;

import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyLogger;
import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.LazyLoadAdapter;
import vn.infory.infory.MapActivity;
import vn.infory.infory.R;
import vn.infory.infory.data.Comment;
import vn.infory.infory.data.News;
import vn.infory.infory.data.PhotoGallery;
import vn.infory.infory.data.Promotion;
import vn.infory.infory.data.PromotionTypeOne;
import vn.infory.infory.data.PromotionTypeTwo;
import vn.infory.infory.data.Settings;
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.UserGallery;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopDetail;
import vn.infory.infory.network.GetShopGallery;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.scancode.ScanCodeActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;
import com.cycrix.jsonparser.JsonObject;

public class ShopDetailActivity extends FragmentActivity {

	private static Shop sShop;
	
	private CyLogger mLog = new CyLogger("ShopDetailActivity", true);

	// Data
	private Shop mShop;
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();

	public static Point mCoverSize;
	private Point mAvaSize;
	private Point mMapSize;
	
	private boolean mLoadedLogo 		= false;
	private boolean mLoadedNewsImage 	= false;
	private boolean mLoadedMap 			= false;

	boolean mShowUserGalleryLeft 	= false;
	boolean mShowUserGalleryRight 	= false;

	@ViewById(id = R.id.layoutShopDetail2)	private ViewGroup mLayoutShopDetail2;

//	@ViewById(id = R.id.layoutUserImage)	private UserImageLayout mLayoutUserImage;
//	@ViewById(id = R.id.imgCover)			private ImageView mImgCover;
	@ViewById(id = R.id.pagerCover)			private ViewPager mPagerCover;
	@ViewById(id = R.id.layoutLogo)			private View mLayoutLogo;

	@ViewById(id = R.id.txtShopName) 		private TextView mTxtShopName;
	@ViewById(id = R.id.txtShopType) 		private TextView mTxtShopType;
	@ViewById(id = R.id.txtLoveNum)			private TextView mTxtLoveNum;
	@ViewById(id = R.id.txtViewNum)			private TextView mTxtViewNum;
	@ViewById(id = R.id.txtCommentNum)		private TextView mTxtCommentNum;
	@ViewById(id = R.id.txtDuration)		private TextView mTxtDuration;
	@ViewById(id = R.id.txtAddress)			private TextView mTxtAddress;
	@ViewById(id = R.id.txtTel)				private TextView mTxtTel;
	@ViewById(id = R.id.layoutPage1)		private ViewGroup mLayoutPage1;

	@ViewById(id = R.id.layoutPromo)		private FrameLayout mLayoutPromo;
	@ViewById(id = R.id.layoutNews)			private FrameLayout mLayoutNews;

	@ViewById(id = R.id.layoutMap)			private FrameLayout mLayoutMap;

	//	@ViewById(id = R.id.layoutUserImage) 	private UserImageLayout mlayoutUserImage;
	@ViewById(id = R.id.lstUserGallery)		HListView mLstGallery;
	@ViewById(id = R.id.imgUserGalleryLeft) View mImgUserGalleryLeft;
	@ViewById(id = R.id.imgUserGalleryRight)View mImgUserGalleryRight;
	@ViewById(id = R.id.imgGallaryOverlay)	private ImageView mImgGalleryOverlay;

	@ViewById(id = R.id.imgAva)				private ImageView mImgAva;

	private View mViewPromo;
	private View mViewNews;

	@ViewById(id = R.id.lstComment)			private CommentListView mLstComment;

	private CommentAdapter mCommentAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_detail);

		mShop = sShop;
		sShop = null;

		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			e.printStackTrace();
			finish();
			return;
		}

		calcMapSize();

		// Set data layout
		setData(mShop);

		// Get ShopDetail
		GetShopDetail task = new GetShopDetail(this, mShop, mShop.idShop) {
			@Override
			protected void onCompleted(Object result2) {
				mTaskList.remove(this);
				Shop s = (Shop) result2;

				// Inflate promotion layout
				switch (s.promotionType) {
				case 1:
					mLayoutPromo.removeAllViews();
					mViewPromo = getLayoutInflater().inflate(R.layout.shop_detail_promo1, mLayoutPromo, true);
					setDataPromo1(s.promotionDetail);
					FontsCollection.setFont(mViewPromo);
					break;
				case 2:
					mLayoutPromo.removeAllViews();
					mViewPromo = getLayoutInflater().inflate(R.layout.shop_detail_promo2, mLayoutPromo, true);
					setDataPromo2(s.promotionDetail);
					FontsCollection.setFont(mViewPromo);
					break;
				}

				// Inflate news layout
				if (s.promotionNews != null) {
					mLayoutNews.removeAllViews();
					mViewNews = getLayoutInflater().inflate(R.layout.shop_detail_news, mLayoutNews, true);
					setDataNews(s.promotionNews);
					FontsCollection.setFont(mViewNews);
				}

				setData(s);
			}

			@Override
			protected void onFail(Exception e) {
				mTaskList.remove(this);
				CyUtils.showError("Không thể lấy chi tiết cửa hàng", e, ShopDetailActivity.this);
			}
		};
		mTaskList.add(task);
		task.executeOnExecutor(NetworkManager.THREAD_POOL);
		
		FontsCollection.setFont(findViewById(android.R.id.content));
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mTaskList.size() > 0)
			mLog.d("onDestroy with " + mTaskList.size() + " remaining tasks.");
		
		for (CyAsyncTask task : mTaskList)
			task.cancel(true);
	}

	public static void newInstance(Activity act, Shop s) {
		sShop = s;
		Intent intent = new Intent(act, ShopDetailActivity.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_down_detail, R.anim.alpha_out);
	}

	@Override
	public void onBackPressed() {
		finish();
	}

	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(R.anim.alpha_in, R.anim.slide_out_up_detail);
	}

	@Click(id = R.id.btnInfo)
	private void onShopDetailInfoClick(View v) {
		ShopDetailInfo.newInstance(this, mShop);
	}

	@Click(id = R.id.layoutMap)
	private void onMapPreviewClick(View v) {
		if (mShop.shopLat != -1 && mShop.shopLng != -1)
			MapActivity.newInstance(this, mShop);
	}

	@Click(id = R.id.layoutTel)
	private void onTelClick(View v) {
		Intent call = new Intent(Intent.ACTION_CALL);
		call.setData(Uri.parse("tel:" + mShop.tel));
		startActivity(call);	
	}

	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////

	private void setData(Shop s) {

		// Download cover
		s.normalize();
		GalleryFullAdapter shopGalleryadapter = new GalleryFullAdapter(this, 
				(List<PhotoGallery>) (Object) s.shopGallery, 
				new GetShopGallery(this, "" + s.idShop, 0), true);
		
		shopGalleryadapter.setPage(mShop.shopGallery.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (mShop.shopGallery.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0
				|| mShop.shopGallery.size() == 0)
			shopGalleryadapter.mIsMore = false;
		mPagerCover.setAdapter(shopGalleryadapter);
		mPagerCover.setOnPageChangeListener(shopGalleryadapter);

		// Download logo
		if (!mLoadedLogo && s.logo != null) {
			mLoadedLogo = true;
			
			CyAsyncTask task = CyImageLoader.instance().loadImage(s.logo, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					mTaskList.remove(task);
					switch (from) {
					case CyImageLoader.FROM_MEMORY:
					case CyImageLoader.FROM_DISK:
						mLayoutLogo.setBackgroundDrawable(new BitmapDrawable(image));
						break;

					case CyImageLoader.FROM_NETWORK:
						TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
								new ColorDrawable(0xFFEBEBEB),
								new BitmapDrawable(image)
						});

						mLayoutLogo.setBackgroundDrawable(trans);
						trans.startTransition(300);
						break;
					}
				}

				@Override
				public void loadFail(Exception e, CyAsyncTask task) {
					mTaskList.remove(task);
					mLoadedLogo = false;
//					CyUtils.showError("Không thể tải logo", e, ShopDetailActivity.this);
				}
			}
			, mAvaSize, this);
			if (task != null)
				mTaskList.add(task);
		}

		// Download map image
		if (mShop.shopLat != -1 && mShop.shopLng != -1 && !mLoadedMap) {
			mLoadedMap = true;
			String path = "http://maps.googleapis.com/maps/api/staticmap" +
					"?zoom=13&size=%dx%d&markers=%f,%f&sensor=false&scale=" +
					"1&visual_refresh=true&key=AIzaSyA4AYLIeG8R-i8BOQylnwMsShwqfapttP4";
			path = String.format(Locale.US, path, mMapSize.x, mMapSize.y, mShop.shopLat, mShop.shopLng);
			
			CyImageLoader.instance().showImageSmooth(path, mLayoutMap, mMapSize, mTaskList);
			CyAsyncTask task = CyImageLoader.instance().loadImage(path, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					mTaskList.remove(task);
					switch (from) {
					case CyImageLoader.FROM_MEMORY:
					case CyImageLoader.FROM_DISK:
						mLayoutMap.setBackgroundDrawable(new BitmapDrawable(image));
						break;

					case CyImageLoader.FROM_NETWORK:
						TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
								new ColorDrawable(0xFFEBEBEB),
								new BitmapDrawable(image)
						});

						mLayoutMap.setBackgroundDrawable(trans);
						trans.startTransition(300);
						break;
					}
				}

				@Override
				public void loadFail(Exception e, CyAsyncTask task) {
					mTaskList.remove(task);
					mLoadedMap = false;
//					CyUtils.showError("Không thể tải logo", e, ShopDetailActivity.this);
				}
			}, mMapSize, this);
			
			if (task != null)
				mTaskList.add(task);
		}

		// Set up UserGallery
		if (mShop.userGallery.size() == 0) {
			mImgUserGalleryLeft.setAlpha(0);
			mImgUserGalleryRight.setAlpha(0);
			mShowUserGalleryLeft = false;
			mShowUserGalleryRight = false;

			mImgGalleryOverlay.setImageResource(R.drawable.photo_firsttime);
			mLstGallery.setVisibility(View.GONE);
		} else {
			mLstGallery.setVisibility(View.VISIBLE);
			mShowUserGalleryLeft = true;
			if (mShop.userGallery.size() > 1) {
				mImgUserGalleryRight.setAlpha(0);
				mShowUserGalleryRight = false;
			} else {
				mImgUserGalleryRight.setAlpha(1);
				mShowUserGalleryRight = true;
			}

			mImgGalleryOverlay.setImageResource(R.drawable.frame_photo);

			ArrayList<UserGallery> itemList = new ArrayList<UserGallery>();
			if (mShop.userGallery.size() > 0)
				itemList.add(new UserGallery());
			itemList.addAll(mShop.userGallery);
			if (mShop.userGallery.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
				itemList.add(new UserGallery());
			UserGalleryAdapter adapter = new UserGalleryAdapter(this, itemList, "" + mShop.idShop, mTaskList);
			adapter.setPage(mShop.userGallery.size() / LazyLoadAdapter.ITEM_PER_PAGE);
			if (mShop.userGallery.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
				adapter.mIsMore = false;
			
			mLstGallery.setAdapter(adapter);
			mLstGallery.setOnScrollListener(adapter);
			mLstGallery.setOnItemClickListener(adapter);
		}

		// Set up comments
		int avaSize = CyUtils.dpToPx(37, this);
		CyImageLoader.instance().showImageSmooth(Settings.instance().avatar, mImgAva, 
				new Point(avaSize, avaSize), mTaskList);

		if (s.comments != null) {
			mCommentAdapter= new CommentAdapter(this, (ArrayList<Comment>) s.comments, "" + s.idShop, mTaskList);
			mCommentAdapter.setPage(s.comments.size() / LazyLoadAdapter.ITEM_PER_PAGE);
			if (s.comments.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
				mCommentAdapter.mIsMore = false;
			mLstComment.setAdapter(mCommentAdapter);
			mLstComment.setOnScrollListener(mCommentAdapter);
			mCommentAdapter.setOnScrollListener(mLstComment);
		}

		mTxtShopName.setText(mShop.shopName);
		mTxtShopType.setText(mShop.shopTypeDisplay);

		mTxtLoveNum.setText(mShop.numOfLove);
		mTxtViewNum.setText(mShop.numOfView);
		mTxtCommentNum.setText(mShop.numOfComment);

		mTxtAddress.setText(mShop.address);
		if (mShop.displayTel == null)
			mTxtTel.setText("");
		else
			mTxtTel.setText(" " + mShop.displayTel);
	}

	private void setDataPromo1(Promotion promo) {
		if (!(promo instanceof PromotionTypeOne))
			return;

		PromotionTypeOne promo1 = (PromotionTypeOne) promo;
		Promo1Holder holder = new Promo1Holder();
		try {
			AndroidAnnotationParser.parse(holder, mViewPromo);
		} catch (Exception e) {
			return;
		}

		holder.mTxtDuration.setText(promo1.duration);

		String str1 = "Với ";
		String str2 = " trên hóa đơn bạn sẽ được một lượt quét thẻ";
		SpannableString span = new SpannableString(str1 + promo1.money + str2);
		span.setSpan(new ForegroundColorSpan(0xFFFF4E25), 
				str1.length(), str1.length() + promo1.money.length(), 0);
		holder.mTxtCost.setText(span);
		holder.mLayoutHasSGP.setVisibility(promo1.hasSGP != 0 ? View.VISIBLE : View.INVISIBLE);
		holder.mBtnScan2.setVisibility(promo1.hasSGP == 0 ? View.VISIBLE : View.INVISIBLE);
		holder.mTxtSGP.setText(promo1.sgp);
		holder.mTxtText.setText(promo1.text);

		OnClickListener scanClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScanCodeActivity.newInstance(ShopDetailActivity.this);
			}
		};

		holder.mBtnScan.setOnClickListener(scanClick);
		holder.mBtnScan2.setOnClickListener(scanClick);

		for (PromotionTypeOne.Voucher voucher : promo1.voucherList) {
			View viewVoucher = getLayoutInflater().inflate(R.layout.promo1_item, holder.mLayoutVoucher, false);
			((TextView) viewVoucher.findViewById(R.id.txtType)).setText(voucher.type);
			((TextView) viewVoucher.findViewById(R.id.txtName)).setText(voucher.name);
			((TextView) viewVoucher.findViewById(R.id.txtSGP)).setText(voucher.sgp);
			((View) viewVoucher.findViewById(R.id.layoutFlag)).setBackgroundResource(
					voucher.isAfford != 0 ? R.drawable.icon_flag : R.drawable.icon_flaggray);
			holder.mLayoutVoucher.addView(viewVoucher);
		}
	}

	private void setDataPromo2(Promotion promo) {
		if (!(promo instanceof PromotionTypeTwo))
			return;

		PromotionTypeTwo promo2 = (PromotionTypeTwo) promo;
		Promo2Holder holder = new Promo2Holder();
		try {
			AndroidAnnotationParser.parse(holder, mViewPromo);
		} catch (Exception e) {
			return;
		}

		holder.mTxtDuration.setText(promo2.duration);
		holder.mTxtNote.setText(promo2.note);
		holder.mTxtText.setText(promo2.text);

		OnClickListener scanClick = new OnClickListener() {
			@Override
			public void onClick(View v) {
				ScanCodeActivity.newInstance(ShopDetailActivity.this);
			}
		};

		holder.mBtnScan.setOnClickListener(scanClick);

		for (PromotionTypeTwo.Voucher voucher : promo2.voucherList) {
			View viewVoucher = getLayoutInflater().inflate(R.layout.promo2_item, holder.mLayoutVoucher, false);
			TextView txtName = (TextView) viewVoucher.findViewById(R.id.txtName);
			TextView txtType = (TextView) viewVoucher.findViewById(R.id.txtType);
			TextView txtCondition = (TextView) viewVoucher.findViewById(R.id.txtCondition);
			txtName.setText(voucher.name);
			txtName.setTextColor(voucher.isAfford != 0 ? 0xFF476CD7 : 0xFF909090);
			txtType.setText(voucher.type);
			txtCondition.setVisibility(voucher.condition.length() != 0 ? View.VISIBLE : View.GONE);
			txtCondition.setText(voucher.condition);
			txtCondition.setTextColor(voucher.isAfford != 0 ? 0xFF7C7C7C : 0xFFC2C2C2);
			((View) viewVoucher.findViewById(R.id.layoutFlag)).setBackgroundResource(
					voucher.isAfford != 0 ? R.drawable.icon_flag_gift : R.drawable.icon_flaggray_clock);

			holder.mLayoutVoucher.addView(viewVoucher);
		}
	}

	private void setDataNews(News news) {
		final NewsHolder holder = new NewsHolder();
		try {
			AndroidAnnotationParser.parse(holder, mViewNews);
		} catch (Exception e) {
			return;
		}

		holder.mTxtTitle.setText(news.title);
		holder.mTxtContent.setText(news.content);

		// Download news image
		if (!mLoadedNewsImage) {
			mLoadedNewsImage = true;
			CyImageLoader.instance().loadImage(news.image, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {

					switch (from) {
					case CyImageLoader.FROM_MEMORY:
					case CyImageLoader.FROM_DISK:
						holder.mImgImage.setBackgroundDrawable(new BitmapDrawable(image));
						break;

					case CyImageLoader.FROM_NETWORK:
						TransitionDrawable trans = new TransitionDrawable(new Drawable[] {
								new ColorDrawable(0),
								new BitmapDrawable(image)
						});

						holder.mImgImage.setBackgroundDrawable(trans);
						trans.startTransition(300);
						break;
					}
				}

				@Override
				public void loadFail(Exception e, CyAsyncTask task) {
					mLoadedNewsImage = false;
					CyUtils.showError("Không thể tải ảnh khuyến mãi", e, ShopDetailActivity.this);
				}
			}
			, new Point(), this);
		}
	}

	private SpannableString makeHighlight(String content, String highlightPos) {
		SpannableString span = new SpannableString(content);

		try {
			String[] numStrArr = highlightPos.split("[,;]");

			for (int i = 0; i < numStrArr.length; i += 2) {
				int begin = Integer.parseInt(numStrArr[i]);
				int len = Integer.parseInt(numStrArr[i + 1]);
				Object spanObj = new ForegroundColorSpan(0xFFFF4E25);
				span.setSpan(spanObj, begin, begin + len, 0);
			}
		} catch (Exception e) {
		}

		return span;
	}

	private void calcMapSize() {
		View v = getLayoutInflater().inflate(R.layout.shop_detail_map_preview_measure, 
				(ViewGroup) findViewById(android.R.id.content), false);
		int w = getResources().getDisplayMetrics().widthPixels;
		int measureWidth = MeasureSpec.makeMeasureSpec(w, MeasureSpec.EXACTLY);
		int measureHeight = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED);
		v.measure(measureWidth, measureHeight);
		View childView = v.findViewById(R.id.layoutMap);
		mMapSize = new Point(childView.getMeasuredWidth(), childView.getMeasuredHeight());
		mCoverSize = new Point(w, (int) (w / 1.6));
		int avaEdge = CyUtils.dpToPx(74, this);
		mAvaSize = new Point(avaEdge, avaEdge);
	}

	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////

	///////////////////////////////////////////////////////////////////////////
	// Inner class
	///////////////////////////////////////////////////////////////////////////

	@JsonObject
	private static class Promo1Holder {
		@ViewById(id = R.id.layoutHasSGP)	public View mLayoutHasSGP;
		@ViewById(id = R.id.txtDuration)	public TextView mTxtDuration;
		@ViewById(id = R.id.txtCost)		public TextView mTxtCost;
		@ViewById(id = R.id.txtSGP)			public TextView mTxtSGP;
		@ViewById(id = R.id.btnScan)		public ImageButton mBtnScan;
		@ViewById(id = R.id.btnScan2)		public ImageButton mBtnScan2;
		@ViewById(id = R.id.txtText)		public TextView mTxtText;
		@ViewById(id = R.id.layoutVoucher)	public LinearLayout mLayoutVoucher;
	}

	private static class Promo2Holder {
		@ViewById(id = R.id.txtDuration)	public TextView mTxtDuration;
		@ViewById(id = R.id.txtNote)		public TextView mTxtNote;
		@ViewById(id = R.id.txtText)		public TextView mTxtText;
		@ViewById(id = R.id.btnScan)		public ImageButton mBtnScan;
		@ViewById(id = R.id.layoutVoucher)	public LinearLayout mLayoutVoucher;
	}

	private static class NewsHolder {
		@ViewById(id = R.id.txtTitle)		public TextView mTxtTitle;
		@ViewById(id = R.id.txtContent)		public TextView mTxtContent;
		@ViewById(id = R.id.imgImage)		public ImageView mImgImage;
	}
}
