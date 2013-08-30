package vn.smartguide;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;

public class DetailShopMenuFragment extends Fragment {
	
	private Shop mShop;
	private ExpandableListView mLst;
	private ShopMenuListAdapter mAdapter;

	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.detail_shopmenu, container, false);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        mLst = (ExpandableListView) getView().findViewById(R.id.lstShopMenu);
        mAdapter = new ShopMenuListAdapter((MainActivity) getActivity(), mLst);
        mLst.setAdapter(mAdapter);
    }
    
    public void setData(Shop s) {
    	
    	mShop = s;
    	
    	// Get item list
    	mAdapter.setData(mShop);
    	mAdapter.notifyDataSetChanged();
    	
    	// Expand all group
        for (int i = 0; i < mAdapter.getGroupCount(); i++)
        	mLst.expandGroup(i);
    }
    
//    public class ShopMenuListAdapter extends BaseAdapter
//    {
//    	private LayoutInflater inflater;
//    	private List<Item> mItemList = new ArrayList<Item>();
//
//        public ShopMenuListAdapter() {
//        	inflater = DetailShopMenuFragment.this.getActivity().getLayoutInflater();
//        }
//
//        @Override
//        public int getCount() {
//        	return mItemList.size();
//        }
//
//        @Override
//        public View getView(int position, View convertView, ViewGroup parent) {
//        	
//            if (convertView == null) {
//                convertView = inflater.inflate(R.layout.shopmenu_item, null);
//            }
//            
//            ((TextView) convertView.findViewById(R.id.txtItemName)).setText(mItemList.get(position).mName);
//            ((TextView) convertView.findViewById(R.id.txtItemPrice)).setText(mItemList.get(position).mPrice);
//            
//            return convertView;
//        }
//
//        @Override
//        public Object getItem(int pos) {
//            return pos;
//        }
//
//        @Override
//        public long getItemId(int pos) {
//            return pos;
//        }
//        
//        public void setData(List<Item> dataList) {
//        	mItemList = dataList;
//        }
//    }
    
    public class ShopMenuListAdapter extends BaseExpandableListAdapter {
    	 
        private MainActivity mContext;
        private Map<String, List<Item>> menuCollections = new LinkedHashMap<String, List<Item>>();
        private List<String> groupMenuList = new ArrayList<String>();
     
        public ShopMenuListAdapter(MainActivity context, ExpandableListView explst) {
        	
            mContext = context;
        }
        
        public void setData(Shop s) {
        	
        	groupMenuList = s.mGroupItemList;
        	menuCollections = s.mItemCollections;
        }

        public Object getChild(int groupPosition, int childPosition) {
        	
            return menuCollections.get(groupMenuList.get(groupPosition)).get(childPosition);
        }
     
        public long getChildId(int groupPosition, int childPosition) {
        	
            return childPosition;
        }
     
        public View getChildView(final int groupPosition, final int childPosition,
                boolean isLastChild, View convertView, ViewGroup parent) {
        	
            LayoutInflater inflater = mContext.getLayoutInflater();
     
            if (convertView == null) {
                convertView = inflater.inflate(R.layout.shopmenu_item, null);
            }
            
            Item item = (Item) getChild(groupPosition, childPosition);
            ((TextView) convertView.findViewById(R.id.txtItemName)).setText(item.mName);
            ((TextView) convertView.findViewById(R.id.txtItemPrice)).setText(item.mPrice);
           
            return convertView;
        }
     
        public int getChildrenCount(int groupPosition) {
        	
            return menuCollections.get(groupMenuList.get(groupPosition)).size();
        }
     
        public Object getGroup(int groupPosition) {
        	
            return groupMenuList.get(groupPosition);
        }
     
        public int getGroupCount() {
        	
            return groupMenuList.size();
        }
     
        public long getGroupId(int groupPosition) {
        	
            return groupPosition;
        }
     
        public View getGroupView(int groupPosition, boolean isExpanded,
                View convertView, ViewGroup parent) {
            if (convertView == null) {
            	LayoutInflater inflater = mContext.getLayoutInflater();
                convertView = inflater.inflate(R.layout.shopmenu_group_item, null);
            }
            
            ((TextView) convertView.findViewById(R.id.txtGroupName)).setText((String) getGroup(groupPosition));
            
            return convertView;
        }
     
        public boolean hasStableIds() {
        	
            return true;
        }
     
        public boolean isChildSelectable(int groupPosition, int childPosition) {
        	
            return true;
        }
    }
    
    public static String int2VND(int price) {
    	NumberFormat formatter = NumberFormat.getInstance(Locale.US);
    	return formatter.format(price) + " vnd";
    }
}
