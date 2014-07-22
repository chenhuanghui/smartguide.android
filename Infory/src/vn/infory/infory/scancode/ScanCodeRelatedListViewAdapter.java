package vn.infory.infory.scancode;

import java.util.ArrayList;

import vn.infory.infory.R;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ScanCodeRelatedListViewAdapter extends BaseAdapter implements OnClickListener{
	
	private Activity activity;
	private ArrayList data;
	private static LayoutInflater inflater = null;
	public Resources res;
	ListModelRelatedShops tempValues = null;
	
	public ScanCodeRelatedListViewAdapter(Activity a, ArrayList d, Resources resLocal){
		activity = a;
		data = d;
		res = resLocal;
		
		inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public int getCount() {
		// TODO Auto-generated method stub
		if(data.size() <= 0)
			return 1;
		return data.size();
	}

	@Override
	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public static class ViewHolder {
		public TextView name;
		public TextView content;
		public ImageView image;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		View vi = convertView;
		ViewHolder holder;
		
		if(convertView == null){
			/****** Inflate tabitem.xml file for each row ( Defined below ) *******/
			vi = inflater.inflate(R.layout.scan_related_listview_item, null);
			
			/****** View Holder Object to contain tabitem.xml file elements ******/
			holder = new ViewHolder();
			holder.name = (TextView) vi.findViewById(R.id.txtName);
			holder.content = (TextView) vi.findViewById(R.id.txtContent);
			holder.image = (ImageView) vi.findViewById(R.id.imgListView);
			
			/************  Set holder with LayoutInflater ************/
            vi.setTag( holder );
		}
		else {
			holder = (ViewHolder)vi.getTag();
		}
		
		if(data.size() > 0){
			/***** Get each Model object from Arraylist ********/
			tempValues = null;
			tempValues = (ListModelRelatedShops)data.get(position);
			
			/************  Set Model values in Holder elements ***********/
			holder.name.setText(tempValues.getName());
			holder.content.setText(tempValues.getDescription());
//			holder.image.setImageResource(
//					res.getIdentifier(name, defType, defPackage));
			
//			vi.setOnClickListener(new OnIte)
		}
		return vi;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		Log.v("CustomAdapter","Click");
	}

	private class OnItemClickListener implements OnClickListener {

		private int mPosition;
		
		public OnItemClickListener(int position) {
			// TODO Auto-generated constructor stub
			mPosition = position;
		}
		
		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			
		}
		
	}
	

}
