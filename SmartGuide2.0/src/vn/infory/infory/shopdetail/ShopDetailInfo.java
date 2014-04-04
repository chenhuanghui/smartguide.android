package vn.infory.infory.shopdetail;

import java.util.ArrayList;
import java.util.List;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.CyUtils;
import vn.infory.infory.R;
import vn.infory.infory.CyImageLoader.Listener;
import vn.infory.infory.R.anim;
import vn.infory.infory.R.drawable;
import vn.infory.infory.R.id;
import vn.infory.infory.R.layout;
import vn.infory.infory.data.DetailInfoBlock;
import vn.infory.infory.data.DetailInfoItem1;
import vn.infory.infory.data.DetailInfoItem2;
import vn.infory.infory.data.DetailInfoItem3;
import vn.infory.infory.data.DetailInfoItem4;
import vn.infory.infory.data.Shop;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopDetailInfo;
import vn.infory.infory.network.NetworkManager;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.text.Html;
import android.text.SpannableString;
import android.text.TextUtils.TruncateAt;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.View;
import android.view.View.OnLayoutChangeListener;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ShopDetailInfo extends Activity {
	
	private static Shop sShop;
	
	private Shop mShop;
	private CyAsyncTask mGetInfoTask;
	private List<CyAsyncTask> mGetImageList = new ArrayList<CyAsyncTask>(); 
	
	// GUI elements
	@ViewById(id = R.id.layoutRootContent)private ViewGroup mLayoutRootContent;
	@ViewById(id = R.id.txtShopName)	private TextView mTxtShopName;
	@ViewById(id = R.id.txtShopType)	private TextView mTxtShopType;
	@ViewById(id = R.id.txtAddress)		private TextView mTxtAddress;
	@ViewById(id = R.id.txtIntroHeader)	private TextView mTxtIntroHeader;
	@ViewById(id = R.id.txtIntroContent)private TextView mTxtIntroContent;
	@ViewById(id = R.id.txtReadmore)	private TextView mTxtReadmore;
	@ViewById(id = R.id.prgLoading)		private ProgressBar mPrgLoading;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_detail_info);
		
		try {
			AndroidAnnotationParser.parse(this, findViewById(android.R.id.content));
		} catch (Exception e) {
			finish();
			return;
		}
		
		mShop = sShop;
		sShop = null;
		
		// Request detail info
		new GetShopDetailInfo(this, mShop.idShop) {			
			@Override
			protected void onCompleted(Object result2) {
				List<DetailInfoBlock> result = (List<DetailInfoBlock>) result2;
				mLayoutRootContent.removeView(mPrgLoading);				
				setShopInfoData(result);
			};
			
			@Override
			protected void onFail(Exception e) {
				CyUtils.showError("Không thể lấy thông tin chi tiết", e, ShopDetailInfo.this);
				mLayoutRootContent.removeView(mPrgLoading);
			}
		}.executeOnExecutor(NetworkManager.THREAD_POOL);
		
		// Set layout
		setShopDetailData(mShop);
		
		mTxtIntroContent.addOnLayoutChangeListener(new OnLayoutChangeListener() {
			@Override
			public void onLayoutChange(View v, int left, int top, int right,
					int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
				mTxtIntroContent.removeOnLayoutChangeListener(this);
//				if (mTxtIntroContent.getLineCount() > 3)
				if (mTxtIntroContent.getLineCount() > 3) {
					mTxtIntroContent.setMaxLines(3);
					mTxtReadmore.setVisibility(View.VISIBLE);
				} else
					mTxtReadmore.setVisibility(View.GONE);
			}
		});
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		
		if (mGetInfoTask != null)
			mGetInfoTask.cancel(true);
		
		for (CyAsyncTask task : mGetImageList)
			task.cancel(true);
	}
	
	@Click(id = R.id.txtReadmore)
	private void onReadmoreClick(View v) {
		if (mTxtReadmore.getText().toString().equals("Xem thêm")) {
			mTxtReadmore.setText("Ẩn");
			mTxtIntroContent.setMaxLines(Integer.MAX_VALUE);
		} else {
			mTxtReadmore.setText("Xem thêm");
			mTxtIntroContent.setMaxLines(3);
		}
	}

	public static void newInstance(Activity act, Shop s) {
		sShop = s;
		
		Intent intent = new Intent(act, ShopDetailInfo.class);
		act.startActivity(intent);
		act.overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
	}
	
	@Override
	public void finish() {
		super.finish();
		overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Private methods
	///////////////////////////////////////////////////////////////////////////
	
	private void setShopDetailData(Shop s) {
		mTxtShopName.setText(s.shopName);
		mTxtShopType.setText(" " + s.shopTypeDisplay);
		mTxtAddress.setText(s.address);
//		mTxtIntroContent.setText("jabsdkasbd");
		mTxtIntroContent.setText(s.description);
	}
	
	private void setShopInfoData(List<DetailInfoBlock> blockList) {
		
		for (DetailInfoBlock block : blockList) {
			switch (block.type) {
			case 1:
				inFlateBlock1(block);
				break;
			case 2:
				inFlateBlock2(block);
				break;
			case 3:
				inFlateBlock3(block);
				break;
			case 4:
				inFlateBlock4(block);
				break;
			}
		}
	}
	
	private void inFlateBlock1(DetailInfoBlock block) {
		ViewGroup layoutBlock = (ViewGroup) getLayoutInflater().inflate(
				R.layout.detail_info_block1, mLayoutRootContent, false);
		mLayoutRootContent.addView(layoutBlock);
		
		((TextView) layoutBlock.findViewById(R.id.txtHeader)).setText(block.header);
		
		for (int i = 0; i < block.items.size(); i++) {
			int layoutId;
			if (i != block.items.size() - 1)
				layoutId = R.layout.detail_info_item1;
			else
				layoutId = R.layout.detail_info_item1_end;
			ViewGroup layoutItem = (ViewGroup) getLayoutInflater().inflate(
					layoutId, layoutBlock, false);
			layoutBlock.addView(layoutItem);
			
			if (!(block.items.get(i) instanceof DetailInfoItem1))
				return;
			
			DetailInfoItem1 item = (DetailInfoItem1) block.items.get(i);
			((TextView) layoutItem.findViewById(R.id.txtContent)).setText(item.content);
			((ImageView) layoutItem.findViewById(R.id.imgTick)).setImageResource(
					item.isTicked != 0 ? R.drawable.button_tickon : R.drawable.button_tickoff);
		}
	}

	private void inFlateBlock2(DetailInfoBlock block) {
		ViewGroup layoutBlock = (ViewGroup) getLayoutInflater().inflate(
				R.layout.detail_info_block2, mLayoutRootContent, false);
		mLayoutRootContent.addView(layoutBlock);
		
		((TextView) layoutBlock.findViewById(R.id.txtHeader)).setText(block.header);
		
		for (int i = 0; i < block.items.size(); i++) {
			int layoutId;
			if (i != block.items.size() - 1)
				layoutId = R.layout.detail_info_item2;
			else
				layoutId = R.layout.detail_info_item2_end;
			ViewGroup layoutItem = (ViewGroup) getLayoutInflater().inflate(
					layoutId, layoutBlock, false);
			layoutBlock.addView(layoutItem);
			
			if (!(block.items.get(i) instanceof DetailInfoItem2))
				return;
			
			DetailInfoItem2 item = (DetailInfoItem2) block.items.get(i);
			((TextView) layoutItem.findViewById(R.id.txtTitle)).setText(item.title);
			setHyperLink(layoutItem, item.content);
		}
	}

	private void inFlateBlock3(DetailInfoBlock block) {
		ViewGroup layoutBlock = (ViewGroup) getLayoutInflater().inflate(
				R.layout.detail_info_block3, mLayoutRootContent, false);
		mLayoutRootContent.addView(layoutBlock);
		
		((TextView) layoutBlock.findViewById(R.id.txtHeader)).setText(block.header);
		
		for (int i = 0; i < block.items.size(); i++) {
			int layoutId;
			if (i != block.items.size() - 1)
				layoutId = R.layout.detail_info_item3;
			else
				layoutId = R.layout.detail_info_item3_end;
			ViewGroup layoutItem = (ViewGroup) getLayoutInflater().inflate(
					layoutId, layoutBlock, false);
			layoutBlock.addView(layoutItem);
			
			if (!(block.items.get(i) instanceof DetailInfoItem3))
				return;
			
			DetailInfoItem3 item = (DetailInfoItem3) block.items.get(i);
			((TextView) layoutItem.findViewById(R.id.txtTitle)).setText(item.title);
			((TextView) layoutItem.findViewById(R.id.txtContent)).setText(item.content);
			final ImageView img = (ImageView) layoutItem.findViewById(R.id.imgImage);
			CyAsyncTask task = CyImageLoader.instance().loadImage(item.image, new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					img.setImageBitmap(image);
				}
			}, new Point(), this);
			if (task != null)
				mGetImageList.add(task);
		}
	}

	private void inFlateBlock4(DetailInfoBlock block) {
		ViewGroup layoutBlock = (ViewGroup) getLayoutInflater().inflate(
				R.layout.detail_info_block4, mLayoutRootContent, false);
		mLayoutRootContent.addView(layoutBlock);
		
		((TextView) layoutBlock.findViewById(R.id.txtHeader)).setText(block.header);
		
		for (int i = 0; i < block.items.size(); i++) {
			int layoutId;
			if (i != block.items.size() - 1)
				layoutId = R.layout.detail_info_item4;
			else
				layoutId = R.layout.detail_info_item4_end;
			ViewGroup layoutItem = (ViewGroup) getLayoutInflater().inflate(
					layoutId, layoutBlock, false);
			layoutBlock.addView(layoutItem);
			
			if (!(block.items.get(i) instanceof DetailInfoItem4))
				return;
			
			DetailInfoItem4 item = (DetailInfoItem4) block.items.get(i);
			((TextView) layoutItem.findViewById(R.id.txtTitle)).setText(item.title);
			((TextView) layoutItem.findViewById(R.id.txtDate)).setText(item.date);
			((TextView) layoutItem.findViewById(R.id.txtContent)).setText(item.content);
		}
	}
	
	private void setHyperLink(ViewGroup parent, String content) {
		String message = "<a href=\"" + content + "\">" + content + "</a>";
		TextView txtView = (TextView) parent.findViewById(R.id.txtContent);
		txtView.setText(Html.fromHtml(message));
		txtView.setMovementMethod(LinkMovementMethod.getInstance());
	}
}