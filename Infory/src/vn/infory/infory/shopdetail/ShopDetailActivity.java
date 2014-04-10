package vn.infory.infory.shopdetail;

import it.sephiroth.android.library.widget.HListView;

import java.util.ArrayList;
import java.util.Arrays;
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
import vn.infory.infory.data.Shop;
import vn.infory.infory.data.UserGallery;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetComment;
import vn.infory.infory.network.GetShopDetail;
import vn.infory.infory.network.GetShopGallery;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.scancode.ScanCodeActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Point;
import android.graphics.drawable.AnimationDrawable;
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
import android.view.ViewGroup.LayoutParams;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.ViewById;

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

	private ShopDetailAdapter mAdapter;
	
	@ViewById(id = R.id.lst)				private ListView mLst;
	@ViewById(id = R.id.layoutCommentHeader)private View mLayoutCommentHeader;

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

		calcSize();

		// Set data layout
		mAdapter = new ShopDetailAdapter();
		mAdapter.mHideLoading = false;
		mLst.setAdapter(mAdapter);
		mLst.setOnScrollListener(mAdapter);

		// Get ShopDetail
		GetShopDetail task = new GetShopDetail(this, mShop, mShop.idShop) {
			@Override
			protected void onCompleted(Object result2) {
				mTaskList.remove(this);
				mAdapter.reset();
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

	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////

	private void setShopGalleryInfoBar(ViewGroup view) {
		// Download cover
		mShop.normalize();
		ViewPager pagerCover = (ViewPager) view.findViewById(R.id.pagerCover);
		GalleryFullAdapter shopGalleryadapter = new GalleryFullAdapter(this, 
				(List<PhotoGallery>) (Object) mShop.shopGallery, 
				new GetShopGallery(this, "" + mShop.idShop, 0), true);

		shopGalleryadapter.setPage(mShop.shopGallery.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (mShop.shopGallery.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0
				|| mShop.shopGallery.size() == 0)
			shopGalleryadapter.mIsMore = false;
		pagerCover.setAdapter(shopGalleryadapter);
		pagerCover.setOnPageChangeListener(shopGalleryadapter);

		// Download logo
		View layoutLogo = view.findViewById(R.id.layoutLogo); 
		if (!mLoadedLogo && mShop.logo != null) {
			mLoadedLogo = true;
			CyImageLoader.instance().showImageSmooth(mShop.logo, layoutLogo, mAvaSize, mTaskList);
		}

		// Info
		((TextView) view.findViewById(R.id.txtShopName)).setText(mShop.shopName);
		((TextView) view.findViewById(R.id.txtShopType)).setText(mShop.shopTypeDisplay);

		((TextView) view.findViewById(R.id.txtLoveNum)).setText(mShop.numOfLove);
		((TextView) view.findViewById(R.id.txtViewNum)).setText(mShop.numOfView);
	}

	private void setInfo(ViewGroup view) {
		TextView txtAddress = (TextView) view.findViewById(R.id.txtAddress);
		TextView txtTel = (TextView) view.findViewById(R.id.txtTel);
		txtAddress.setText(mShop.address);
		if (mShop.displayTel == null)
			txtTel.setText("");
		else
			txtTel.setText(" " + mShop.displayTel);

		if (mShop.shopLat != -1 && mShop.shopLng != -1 && !mLoadedMap) {
			mLoadedMap = true;
			String path = "http://maps.googleapis.com/maps/api/staticmap" +
					"?zoom=13&size=%dx%d&markers=%f,%f&sensor=false&scale=" +
					"1&visual_refresh=true&key=AIzaSyA4AYLIeG8R-i8BOQylnwMsShwqfapttP4";
			path = String.format(Locale.US, path, mMapSize.x, mMapSize.y, mShop.shopLat, mShop.shopLng);
			View layoutMap = view.findViewById(R.id.layoutMap);
			CyImageLoader.instance().showImageSmooth(path, layoutMap, mMapSize, mTaskList);
		}
		
		HListView mLstGallery = (HListView) view.findViewById(R.id.lstUserGallery);
		ArrayList<UserGallery> itemList = new ArrayList<UserGallery>();
		if (mShop.userGallery.size() > 0)
			itemList.add(new UserGallery());
		itemList.addAll(mShop.userGallery);
		if (mShop.userGallery.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
			itemList.add(new UserGallery());
		UserGalleryAdapter adapter = new UserGalleryAdapter(this, itemList, "" + mShop.idShop, mTaskList, view);
		adapter.setPage(mShop.userGallery.size() / LazyLoadAdapter.ITEM_PER_PAGE);
		if (mShop.userGallery.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0) {
			adapter.mIsMore = false;
		}
		mLstGallery.setAdapter(adapter);
		mLstGallery.setOnScrollListener(adapter);
		mLstGallery.setOnItemClickListener(adapter);
	}

	private void setDataPromo1(ViewGroup view) {
		Promotion promo = mShop.promotionDetail;

		if (!(promo instanceof PromotionTypeOne))
			return;

		PromotionTypeOne promo1 = (PromotionTypeOne) promo;
		Promo1Holder holder = new Promo1Holder();
		try {
			AndroidAnnotationParser.parse(holder, view);
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

	private void setDataPromo2(ViewGroup view) {
		Promotion promo = mShop.promotionDetail;

		if (!(promo instanceof PromotionTypeTwo))
			return;

		PromotionTypeTwo promo2 = (PromotionTypeTwo) promo;
		Promo2Holder holder = new Promo2Holder();
		try {
			AndroidAnnotationParser.parse(holder, view);
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
	
	private void setDataNews(ViewGroup view) {
		final NewsHolder holder = new NewsHolder();
		try {
			AndroidAnnotationParser.parse(holder, view);
		} catch (Exception e) {
			return;
		}
		
		News news = mShop.promotionNews;

		holder.mTxtTitle.setText(news.title);
		holder.mTxtContent.setText(news.content);
		
		if (news.imageHeight == 0)
			holder.mImgImage.setContentDescription("ratio:2.6");
		else
			holder.mImgImage.setContentDescription("ratio:" + (float) news.imageWidth / news.imageHeight);

		// Download news image
		if (!mLoadedNewsImage) {
			mLoadedNewsImage = true;
			CyImageLoader.instance().showImageSmooth(news.image, holder.mImgImage, mMapSize, mTaskList);
		}
	}

	private void calcSize() {
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
	// Inner class
	///////////////////////////////////////////////////////////////////////////

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

	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
	
	private class ShopDetailAdapter extends LazyLoadAdapter implements CommentLayout.MeasureListener {

		// 0: cover + info bar
		// 1: promotion
		// 2: news
		// 3: info
		// 4: comment header pad
		// 5: comment items

		private List<Integer> mTypeList = new ArrayList<Integer>();
		private ViewGroup mLayoutShopGallery;
		private ViewGroup mLayoutInfo;
		private ViewGroup mLayoutPromo;
		private ArrayList<Integer> mHeightList = new ArrayList<Integer>();
		private int mCommentLstHeight;

		public ShopDetailAdapter() {
			super(ShopDetailActivity.this, 
					new GetComment(ShopDetailActivity.this, "" + mShop.idShop, 0, 0), 
					R.layout.shop_detail_comment_loading, 6, (ArrayList) mShop.comments);

			mItemList.add(0, new Comment());
			mItemList.add(0, new Comment());
			mItemList.add(0, new Comment());

			mTypeList.addAll(Arrays.asList(0, 3, 4, 5));
			
			if (hasNews()) {
				mTypeList.add(1, 2);
				mItemList.add(0, new Comment());
			}

			if (hasPromo()) {
				mTypeList.add(1, 1);
				mItemList.add(0, new Comment());
			}

			setPage(mShop.comments.size() / LazyLoadAdapter.ITEM_PER_PAGE);

			if (mShop.comments.size() % LazyLoadAdapter.ITEM_PER_PAGE != 0)
				mIsMore = false;
		}

		public void reset() {
			if (!mTypeList.contains(2) && hasNews()) {
				mTypeList.add(1, 2);
				mItemList.add(0, new Comment());
				notifyDataSetChanged();
			}
			
			if (!mTypeList.contains(1) && hasPromo()) {
				mTypeList.add(1, 1);
				mItemList.add(0, new Comment());
				notifyDataSetChanged();
			}

			setShopGalleryInfoBar(mLayoutShopGallery);
			setInfo(mLayoutInfo);
		}

		private boolean hasPromo() {
			return mShop.promotionType == 1 || mShop.promotionType == 2;
		}
		
		private boolean hasNews() {
			return mShop.promotionNews != null;
		}

		@Override
		public int getItemViewType(int position) {

			int type = super.getItemViewType(position);
			if (type != -1) {
				return type;
			} else {
				return mTypeList.get(Math.min(position, mTypeList.size() - 1));
			}
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (mHeightList.size() < position + 1)
				mHeightList.add(0);

			convertView = super.getView(position, convertView, parent);

			if (position >= mItemList.size()) {
				View aniLayout = convertView.findViewById(R.id.layoutLoading);
				if (mIsMore)
					((AnimationDrawable) aniLayout.getBackground()).start();
				else
					aniLayout.setVisibility(View.GONE);
				LayoutParams param = convertView.getLayoutParams();
				param.height = getCommentTailHeight();
				convertView.setLayoutParams(param);
				return convertView;
			}

			int type = getItemViewType(position);
			if (convertView == null) {
				switch (type) {
				case 0: // shop gallery + info bar
					convertView = mInflater.inflate(R.layout.shop_detail_1, parent, false);
					mLayoutShopGallery = (ViewGroup) convertView;
					mLayoutShopGallery.findViewById(R.id.btnInfo)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							ShopDetailInfo.newInstance(ShopDetailActivity.this, mShop);
						}
					});
					setShopGalleryInfoBar(mLayoutShopGallery);

					// Calc comment list height
					{
						int screenHeight = parent.getHeight();
						int commentHeaderHeight = CyUtils.dpToPx(130, ShopDetailActivity.this);
						mCommentLstHeight = screenHeight - commentHeaderHeight;
					}
					break;
					
				case 1:	// promotion
					switch (mShop.promotionType) {
					case 1:
						convertView = mInflater.inflate(R.layout.shop_detail_promo1, parent, false);
						setDataPromo1((ViewGroup) convertView);
						break;
					case 2:
						convertView = mInflater.inflate(R.layout.shop_detail_promo2, parent, false);
						setDataPromo2((ViewGroup) convertView);
						break;
					}
					mLayoutPromo = (ViewGroup) convertView;
					break;
					
				case 2:	// news
					convertView = mInflater.inflate(R.layout.shop_detail_news, parent, false);
					setDataNews((ViewGroup) convertView);
					break;
					
				case 3:	// info + user gallery
					convertView = mInflater.inflate(R.layout.shop_detail_2, parent, false);
					mLayoutInfo = (ViewGroup) convertView;
					mLayoutInfo.findViewById(R.id.layoutMap)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							if (mShop.shopLat != -1 && mShop.shopLng != -1)
								MapActivity.newInstance(ShopDetailActivity.this, mShop);
						}
					});
					mLayoutInfo.findViewById(R.id.layoutTel)
					.setOnClickListener(new OnClickListener() {
						@Override
						public void onClick(View arg0) {
							Intent call = new Intent(Intent.ACTION_CALL);
							call.setData(Uri.parse("tel:" + mShop.tel));
							startActivity(call);
						}
					});
					setInfo(mLayoutInfo);
					break;
					
				case 4:
					convertView = mInflater.inflate(R.layout.shop_detail_comment_heade_pad, parent, false);
					break;
					
				case 5:	// comment
					convertView = mInflater.inflate(R.layout.shop_detail_comment_item, parent, false);
					CommentHolder holder = new CommentHolder();
					try {
						AndroidAnnotationParser.parse(holder, convertView);
					} catch (Exception e) {}
					convertView.setTag(holder);

					((CommentLayout) convertView).setListener(this);
					break;
				}
				FontsCollection.setFont(convertView);
			}

			if (type == 5) {
				CommentHolder holder = (CommentHolder) convertView.getTag();
				Comment item = (Comment) getItem(position);
				holder.mTxtName.setText(item.username);
				holder.mTxtContent.setText(item.comment);
				holder.mTxtTime.setText(item.time);
				holder.mTxtAgreeNum.setText(item.numOfAgree);
				CyImageLoader.instance().showImageListView(
						item.avatar, holder.mImgAva, mAvaSize, mTaskList);

				((CommentLayout) convertView).setPos(position);
			}

			return convertView;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			super.onScroll(view, firstVisibleItem, visibleItemCount, totalItemCount);

			// Find comment header pad position
			int commentHeaderPos = 0;
			for (int i = 0; i < mTypeList.size(); i++)
				if (mTypeList.get(i) == 4) {
					commentHeaderPos = i;
					break;
				}

			// if comment header pad is visible
			if (firstVisibleItem <= commentHeaderPos &&
					commentHeaderPos < firstVisibleItem + visibleItemCount) {
				mLayoutCommentHeader.setVisibility(View.VISIBLE);
				// move pad along
				int y = view.getChildAt(commentHeaderPos - firstVisibleItem).getTop();
				y = Math.max(y, 0);
				mLayoutCommentHeader.setTranslationY(y);
			} else if (firstVisibleItem >= commentHeaderPos) {
				mLayoutCommentHeader.setTranslationY(0);
			} else {
				// take it out off screen
				mLayoutCommentHeader.setTranslationY(mLst.getHeight());
			}
		}

		@Override
		public void measure(CommentLayout thiz) {
			mHeightList.set(thiz.mPos, thiz.getMeasuredHeight());
		}

		private int getCommentTailHeight() {
			int height = mCommentLstHeight;

			// calculate tail height
			for (int i = 0; i < mHeightList.size() && height > 0; i++)
				height -= mHeightList.get(i);

			height = Math.max(0, height);
			mLog.d("get height=" + height);
			return height;
		}
	}

	private static class CommentHolder {
		@ViewById(id = R.id.txtAgreeNum)	public TextView mTxtAgreeNum;
		@ViewById(id = R.id.txtName)		public TextView mTxtName;
		@ViewById(id = R.id.txtContent)		public TextView mTxtContent;
		@ViewById(id = R.id.imgAva)			public ImageView mImgAva;
		@ViewById(id = R.id.txtTime)		public TextView mTxtTime;
	}
}
