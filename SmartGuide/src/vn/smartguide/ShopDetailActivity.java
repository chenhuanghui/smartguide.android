package vn.smartguide;

import android.os.Bundle;

import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;

public class ShopDetailActivity extends FragmentActivity {
	
	// Static fields to pass parameters to this activity
	private static Shop sShop;
	
	// Data
	private Shop mShop;
	
	///////////////////////////////////////////////////////////////////////////
	// Override methods
	///////////////////////////////////////////////////////////////////////////

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_shop_detail);
		
		// Get parameters from static fields
		mShop = sShop;
		
		// Clear static fields to avoid leak me
		sShop = null;
	}

	///////////////////////////////////////////////////////////////////////////
	// Public methods
	///////////////////////////////////////////////////////////////////////////

	public static void newInstance(Activity act, Shop s) {
		sShop = s;
		
		Intent intent = new Intent(act, ShopDetailActivity.class);
		act.startActivity(intent);
	}
}