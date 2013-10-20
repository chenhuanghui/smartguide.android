package vn.smartguide;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class DetailPromo1Fragment extends DetailPromoFragment {
	
	private final int FPS = 24;
	
	// GUI elements
	private ShopPromotionListAdapter mPromoListAdapter;
	private TextView mCostPerSGP;
	private GridView gridView;
	private TextView txtSGP;
	private TextView txtPromoDuration;
	private TextView txtSP;
	private TextView txtPperSGP;
	
	// Data
	private Shop mShop;
	private int mTotalScore = 0;
	private boolean isUpdatedScore = false;
	private Listener mListener = new Listener();
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_promo, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        
        mCostPerSGP = (TextView)getView().findViewById(R.id.txtKperSGP);
        txtPperSGP = (TextView)getView().findViewById(R.id.txtPperSGP);
        txtPromoDuration = (TextView) getView().findViewById(R.id.txtPromoDuration);
    	txtSGP = (TextView) getView().findViewById(R.id.txtSGP);
    	txtSP = (TextView) getView().findViewById(R.id.txtSP);
        // Instance of ImageAdapter Class
        mPromoListAdapter = new ShopPromotionListAdapter();
        gridView = (GridView) getView().findViewById(R.id.grid_view_shop_promotion_list);
        gridView.setAdapter(mPromoListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mTotalScore >= mPromoListAdapter.getItem(position).required)
//					mMainAcitivyListener.getAwardTypeOne(mPromoListAdapter.mItemList.get(position).id);
					mListener.onRewardClick(mPromoListAdapter.getItem(position));
			}
		});
    }
    
	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////
    
    public void setListener(Listener listener) {
    	if (listener == null)
    		listener = new Listener();
    	mListener = listener;
    }
    
    @Override
    public void setData(Shop s) {
    	
    	mShop = s;   
    	    	
    	mPromoListAdapter.clear();
    	
    	if (s == null) {
    		txtPromoDuration.setText("");
    		txtSP.setText("");
    		txtSGP.setText("");
    		txtPperSGP.setText("");
    		mCostPerSGP.setText("");
    		return;
    		
    	}
    	
    	txtPromoDuration.setText(s.mPromotion.mDuration);
    	
    	if (s.mPromotionStatus == false) {
    		txtSP.setText("");
    		txtSGP.setText("");
    		new GetPromotionDetail().execute();

    		return;
    	}

    	if (s.mPromotion.getType() == 1) {
    		PromotionTypeOne promo = (PromotionTypeOne) s.mPromotion;
    		//for test reason
    		mTotalScore = promo.mSGP;
    		//mTotalScore = 50;
    		isUpdatedScore = true;
    		
    		txtSGP.setText("" + promo.mSGP);
    		
    		SpannableString spanString = new SpannableString("" + promo.mSP + " SP tích lũy");
    		spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, ("" + promo.mSP).length(), 0);
    		txtSP.setText(spanString);
    		
    		spanString = new SpannableString("" + promo.mPperSGP + " P cho 1 SGP");
    		spanString.setSpan(new StyleSpan(Typeface.BOLD), 0, ("" + promo.mPperSGP).length(), 0);
    		txtPperSGP.setText(spanString);
    		
    		spanString = new SpannableString("Với mỗi " + promo.mCost + " trên hóa đơn bạn sẽ được 1 lượt quét thẻ");
    		spanString.setSpan(new ForegroundColorSpan(0xFFC95436),
    				"Với mỗi ".length(), ("Với mỗi " + promo.mCost).length(), 0);
    		spanString.setSpan(new StyleSpan(Typeface.BOLD),
    				"Với mỗi ".length(), ("Với mỗi " + promo.mCost).length(), 0);
    		mCostPerSGP.setText(spanString);
    		
    		new GetPromotionDetail().execute();
    	}
    	
//    	runAnimation();
    }
    
    private void runAnimation() {
    	txtSGP.setText("0");
    	
    	TimerTask runScore = new RunScore();
    	Timer timer = new Timer();
    	timer.scheduleAtFixedRate(runScore, 700, 1000/FPS);
    }	
    
    private void resetTextSGP() {
    	txtSGP.setText("0");
    }
    
    public void setSGP(int score) {
    	mTotalScore = score;
    	((PromotionTypeOne)mShop.mPromotion).mSGP = mTotalScore;
    	((PromotionTypeOne)GlobalVariable.mCurrentShop.mPromotion).mSGP = mTotalScore;
    	
    	txtSGP.setText(Integer.toString(mTotalScore));
    	
    	mPromoListAdapter.notifyDataSetChanged();
    }
    
	///////////////////////////////////////////////////////////////////////////
	// Network Asynctask
	///////////////////////////////////////////////////////////////////////////
    
    private class GetPromotionDetail extends AsyncTask<Void, Void, Boolean> {
    	
    	private Exception mEx;
    	private List<PromotionStr> dataList;
    	
		@Override
		protected void onPreExecute() { }

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			
				String json = NetworkManger.post(APILinkMaker.mGetPromotionDetail(), pairs);

				JSONObject jRoot = new JSONObject(json);
				parseJsonListPromotion(jRoot);
				
			} catch (Exception e) {
				mEx = e;
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k) {
			
			if (mEx == null) {
				mPromoListAdapter.clear();
				mPromoListAdapter.addAll(dataList);
			} else {
				
			}
		}
		
		private void parseJsonListPromotion(JSONObject jRoot) throws JSONException {
	    	
	    	JSONArray jPromoArr = jRoot.getJSONArray("array_required");
	    	
	    	dataList = new ArrayList<PromotionStr>();
	    	for (int i = 0; i < jPromoArr.length(); i++) {
	    		JSONObject jPromo = jPromoArr.getJSONObject(i);
	    		dataList.add(new PromotionStr(
	    				jPromo.getInt("id"), 
	    				jPromo.getInt("required"), 
	    				jPromo.getString("content"),
	    				jPromo.getString("numberVoucher")));
	    	}
	    }
	}
    
	///////////////////////////////////////////////////////////////////////////
	// Timer class for count up animation
	///////////////////////////////////////////////////////////////////////////
    
    private class RunScore extends TimerTask {
    	
    	private int mCurrentScore = 0;
    	
    	public void run() {
    		
    		getActivity().runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
		    		if (isUpdatedScore == false || mCurrentScore > mTotalScore) {
		    			RunScore.this.cancel();
		    			return;
		    		}
		    		txtSGP.setText(Integer.toString(mCurrentScore));
		    		mCurrentScore++;
				}
			});
    	}
    }
    
	///////////////////////////////////////////////////////////////////////////
	// Adapter
	///////////////////////////////////////////////////////////////////////////
    
    public static class PromotionStr {

		public PromotionStr(int c, int a, String b, String d) {
			required = a;
			content = b;
			id = c;
			voucherNumber = d;
		}

		public int required;
		public String content;
		public int id;
		public String voucherNumber;
	}
    
    private class ShopPromotionListAdapter extends ArrayAdapter<PromotionStr> {

    	public ShopPromotionListAdapter() {
    		super(getActivity(), R.layout.shop_promotion_item, R.id.txtAwardName, new ArrayList<PromotionStr>());
    	}

    	@Override
    	public View getView(int position, View convertView, ViewGroup parent) {
    		convertView = super.getView(position, convertView, parent);
    		
    		// Xử trường hợp loại 2
    		LinearLayout bar1 = (LinearLayout) convertView.findViewById(R.id.linearLayout1);
    		LinearLayout bar2 = (LinearLayout) convertView.findViewById(R.id.linearLayout2);
    		LinearLayout bar3 = (LinearLayout) convertView.findViewById(R.id.linearLayout3);
//    		HorizontalScrollView scrollView = (HorizontalScrollView) convertView.findViewById(R.id.scrollView);
//    		scrollView.fling(1);
    		
    		if (getItem(position).required <= mTotalScore) {
    			bar1.setBackgroundResource(R.drawable.shop_bar_1_highlight);
    			bar2.setBackgroundResource(R.drawable.shop_bar_2_highlight);
    			bar3.setBackgroundResource(R.drawable.shop_bar_3_highlight);
    		} else {
    			bar1.setBackgroundResource(R.drawable.shop_bar_1);
    			bar2.setBackgroundResource(R.drawable.shop_bar_2);
    			bar3.setBackgroundResource(R.drawable.shop_bar_3);
    		}
    		
    		TextView txtSGP = (TextView) convertView.findViewById(R.id.txtSGP);
    		TextView txtAwardName = (TextView) convertView.findViewById(R.id.txtAwardName);
    		txtAwardName.setSelected(true);
    		TextView txtType = (TextView) convertView.findViewById(R.id.txtType);

    		txtSGP.setText("" + getItem(position).required);
    		txtAwardName.setText(getItem(position).content);
    		txtType.setText(getItem(position).voucherNumber);
    		return convertView;
    	}
    }
    
    ///////////////////////////////////////////////////////////////////////////
    // Listener
    ///////////////////////////////////////////////////////////////////////////
    
    public static class Listener {
    	public void onRewardClick(PromotionStr reward) { }
    }
}