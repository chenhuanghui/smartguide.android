package vn.smartguide;

/**
 * Created by ChauSang on 7/26/13.
 */
import java.util.ArrayList;
import java.util.List;



import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class ShopPromotionListAdapter extends BaseAdapter
{
	public static class PromotionStr {

		public PromotionStr(int c, int a, String b) {
			required = a;
			content = b;
			id = c;
		}

		public int required;
		public String content;
		public int id;
	}

	private Activity mActivity;
	public List<PromotionStr> mItemList = new ArrayList<PromotionStr>();
	private int mNowScore = 0;

	public ShopPromotionListAdapter(Activity activity)
	{
		mActivity = activity;
	}

	public void setData(List<PromotionStr> dataList, int score) {
		mItemList = dataList;
		mNowScore = score;
	}

	@Override
	public int getCount()
	{
		return mItemList.size();
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		View MyView = convertView;

		LayoutInflater li = mActivity.getLayoutInflater();
		MyView = li.inflate(R.layout.shop_promotion_item, null);
		// Xử trư�?ng hơp loại 2
		try{
			if (((PromotionTypeOne)GlobalVariable.mCurrentShop.mPromotion).mRequirement.get(position).mSGPRequire <= mNowScore){
				RelativeLayout head_bar = (RelativeLayout) MyView.findViewById(R.id.shop_bar_head_bg);
				LinearLayout tail_bar = (LinearLayout) MyView.findViewById(R.id.shop_bar_tail_bg);
				head_bar.setBackgroundResource(R.drawable.shop_head_bar_green);
				tail_bar.setBackgroundResource(R.drawable.shop_bar_highlight);
			}
		}catch(Exception ex){

		}

		TextView txtSGP = (TextView) MyView.findViewById(R.id.txtSGP);
		TextView txtAwardName = (TextView) MyView.findViewById(R.id.txtAwardName);

		txtSGP.setText("" + mItemList.get(position).required);
		txtAwardName.setText(mItemList.get(position).content);
		return MyView;
	}

	@Override
	public Object getItem(int pos) {
		return pos;
	}

	@Override
	public long getItemId(int pos) {
		return pos;
	}
}