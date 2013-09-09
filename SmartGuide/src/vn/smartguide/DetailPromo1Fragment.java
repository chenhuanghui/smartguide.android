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

import vn.smartguide.ShopPromotionListAdapter.PromotionStr;


import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.TextView;

public class DetailPromo1Fragment extends DetailPromoFragment {
	
	private ShopPromotionListAdapter mPromoListAdapter;
	private Shop mShop;
	int mTotalScore = 0;
	private boolean isUpdatedScore = false;
	private Activity mActivity;
	private MainAcitivyListener mMainAcitivyListener;
	private TextView mCostPerSGP;
	GridView gridView;
	List<PromotionStr> dataList;
	
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_promo, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mMainAcitivyListener = (MainAcitivyListener) getActivity();
        
        mCostPerSGP = (TextView)getView().findViewById(R.id.textView1);
        // Instance of ImageAdapter Class
        mPromoListAdapter = new ShopPromotionListAdapter(getActivity());
        gridView = (GridView) getView().findViewById(R.id.grid_view_shop_promotion_list);
        gridView.setAdapter(mPromoListAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				if (mTotalScore >= mPromoListAdapter.mItemList.get(position).required)
					mMainAcitivyListener.getAwardTypeOne(mPromoListAdapter.mItemList.get(position).id);
			}
		});
        mActivity = getActivity();
    }
    
    TextView txtSGP = null;
    
    public void setData(Shop s) {
    	
    	mShop = s;
    	
    	TextView txtPromoDuration = (TextView) getView().findViewById(R.id.txtPromoDuration);
    	txtSGP = (TextView) getView().findViewById(R.id.txtSGP);
    	TextView txtSP = (TextView) getView().findViewById(R.id.txtSP);
    	
    	txtPromoDuration.setText(s.mPromotion.mDuration);
    	
    	if (s.mPromotionStatus == false){
    		txtSP.setText("");
    		txtSGP.setText("");
    		new GetPromotionDetail().execute();

    		return;
    	}

    	switch (s.mPromotion.getType()) {
    	case 1:    		
    	{   
    		PromotionTypeOne promo = (PromotionTypeOne) s.mPromotion;
    		//for test resean
    		mTotalScore = promo.mSGP;
    		//mTotalScore = 50;
    		isUpdatedScore = true;
    		txtSP.setText("" + promo.mSP);
    		txtSGP.setText("" + promo.mSGP);
    		mCostPerSGP.setText(Integer.toString(promo.mCost / 1000) + "K VNĐ/1 SGP");
    		new GetPromotionDetail().execute();
    	}
    	break;
    	}
    	
    }
    
    private void parseJsonListPromotion(JSONObject jRoot) throws JSONException {
    	
    	JSONArray jPromoArr = jRoot.getJSONArray("array_required");
    	
    	dataList = new ArrayList<PromotionStr>();
    	for (int i = 0; i < jPromoArr.length(); i++) {
    		JSONObject jPromo = jPromoArr.getJSONObject(i);
    		dataList.add(new PromotionStr(jPromo.getInt("id"), jPromo.getInt("required"), jPromo.getString("content")));
    	}
    	
    	mPromoListAdapter.setData(dataList, mTotalScore);
    }
    
    public class GetPromotionDetail extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			
			try {
				List<NameValuePair> pairs = new ArrayList<NameValuePair>();
				pairs.add(new BasicNameValuePair("shop_id", Integer.toString(mShop.mID)));
				pairs.add(new BasicNameValuePair("user_id", GlobalVariable.userID));
			
				String json = NetworkManger.post(APILinkMaker.mGetPromotionDetail(), pairs);

				JSONObject jRoot = new JSONObject(json);
				parseJsonListPromotion(jRoot);
				
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			return true;
		}

		@Override
		protected void onPostExecute(Boolean k){
			mPromoListAdapter.notifyDataSetChanged();
		}

		@Override
		protected void onPreExecute(){
			
		}
	}
    
    class RunScore extends TimerTask {
    	int mCurrentScore = 0;
    	TimerTask mthiz = this;
    	
    	public void run() {
    		
    		mActivity.runOnUiThread(new Runnable() {
				
				@Override
				public void run() {
		    		if (isUpdatedScore == false || mCurrentScore > mTotalScore){
		    			mthiz.cancel();
		    			return;
		    		}
		    		txtSGP.setText(Integer.toString(mCurrentScore));
		    		mCurrentScore++;
				}
			});
    	}
    }
    
    void runAnimation(){
    	txtSGP.setText("0");
    	final int FPS = 24;
    	TimerTask runScore = new RunScore();
    	Timer timer = new Timer();
    	timer.scheduleAtFixedRate(runScore, 700, 1000/FPS);
    }
    
    public void resetTextSGP(){
    	txtSGP.setText("0");
    }
    
    public void setSGP(int score){
    	mTotalScore = score;
    	((PromotionTypeOne)mShop.mPromotion).mSGP = mTotalScore;
    	((PromotionTypeOne)GlobalVariable.mCurrentShop.mPromotion).mSGP = mTotalScore;
    	
    	txtSGP.setText(Integer.toString(mTotalScore));
    	
    	mPromoListAdapter.setData(dataList, mTotalScore);
    	mPromoListAdapter.notifyDataSetChanged();
    }
}