package vn.redbase.smartguide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.TextView;

public class DetailShopMenuFragment extends Fragment {
	
	private Shop mShop;
	private ShopMenuListAdapter mAdapter;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_shopmenu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mAdapter = new ShopMenuListAdapter();
        ((GridView) getView().findViewById(R.id.lstShopMenu)).setAdapter(mAdapter);
    }
    
    public void setData(Shop s) {
    	
    	mShop = s;
    	
    	// Get item list
    	mAdapter.setData(mShop.mItemList);
    	mAdapter.notifyDataSetChanged();
    }
    
    public class ShopMenuListAdapter extends BaseAdapter
    {
    	private LayoutInflater inflater;
    	private List<Item> mItemList = new ArrayList<Item>();

        public ShopMenuListAdapter() {
        	inflater = DetailShopMenuFragment.this.getActivity().getLayoutInflater();
        }

        @Override
        public int getCount() {
        	return mItemList.size();
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
        	
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.shopmenu_item, null);
            }
            
            ((TextView) convertView.findViewById(R.id.txtItemName)).setText(mItemList.get(position).mName);
            ((TextView) convertView.findViewById(R.id.txtItemPrice)).setText(mItemList.get(position).mPrice);
            
            return convertView;
        }

        @Override
        public Object getItem(int pos) {
            return pos;
        }

        @Override
        public long getItemId(int pos) {
            return pos;
        }
        
        public void setData(List<Item> dataList) {
        	mItemList = dataList;
        }
    }
    
    public static String int2VND(int price) {
    	NumberFormat formatter = NumberFormat.getInstance(Locale.US);
    	return formatter.format(price) + " vnd";
    }
}
