package vn.infory.infory.scancode;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONObject;

import vn.infory.infory.CyImageLoader;
import vn.infory.infory.R;
import vn.infory.infory.SGSideMenu;
import vn.infory.infory.data.Shop;
import vn.infory.infory.home.HomeAdapter;
import vn.infory.infory.network.CyAsyncTask;
import vn.infory.infory.network.GetShopDetail2;
import vn.infory.infory.network.NetworkManager;
import vn.infory.infory.shopdetail.ShopDetailActivity;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class ScanCodeRelatedListViewAdapter extends BaseAdapter{
	
	private Activity activity;
	private ArrayList data;
	private static LayoutInflater inflater = null;
	public Resources res;	
	private int type;
	
	private List<CyAsyncTask> mTaskList = new ArrayList<CyAsyncTask>();
	
	public ScanCodeRelatedListViewAdapter(Activity a, ArrayList d, Resources resLocal, int t){
		activity = a;
		data = d;
		res = resLocal;
		type = t;
		
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
		return data.get(position);
	}

	@Override
	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}
	
	public int getType() {
		return type;
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
		final ViewHolder holder;
		
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
			String name, description, logo;
			if(type == 0)
			{
				ListModelRelatedShops tempValues = null;
				tempValues = (ListModelRelatedShops)data.get(position);
				
				name = tempValues.getName();
				description = tempValues.getDescription();
				logo = tempValues.getLogo();
			}
			else if(type == 1)
			{
				ListModelRelatedPromotions tempValues = null;
				tempValues = (ListModelRelatedPromotions)data.get(position);
				
				name = tempValues.getName();
				description = tempValues.getDescription();
				logo = tempValues.getLogo();				
			}
			else
			{
				ListModelRelatedPlacelists tempValues = null;
				tempValues = (ListModelRelatedPlacelists)data.get(position);
				
				name = tempValues.getName();
				description = tempValues.getDescription();
				logo = tempValues.getAuthorAvatar();
			}
			holder.name.setText(name);
			holder.content.setText(description);
			
//			CyImageLoader.instance().showImage(tempValues.getLogo(), holder.image);
			/*CyImageLoader.instance().loadImage(tempValues.getLogo(), new CyImageLoader.Listener() {
				@Override
				public void loadFinish(int from, Bitmap image, String url, CyAsyncTask task) {
					holder.image.setImageBitmap(SGSideMenu.getCroppedBitmap(image));
				}
			}, new Point(), new Activity());*/
			
			CyImageLoader.instance().showImageListView(logo, holder.image, new Point(), mTaskList);
		}
		return vi;
	}
}
