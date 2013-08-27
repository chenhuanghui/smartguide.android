package vn.smartguide;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.view.Menu;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

public class TutorialActivity extends Activity {

	ImageSwitcher tutorialSwitcher;
	ImageButton nextButton;
	int currentPage = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		tutorialSwitcher = (ImageSwitcher)findViewById(R.id.tutorial);
		tutorialSwitcher.setFactory(new ViewFactory() {
			@SuppressWarnings("deprecation")
			public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				return imageView;
			}
		});
		
		nextButton = (ImageButton)findViewById(R.id.imageButton1);
		tutorialSwitcher.setBackgroundResource(R.drawable.tutorial_1);
	}
}
