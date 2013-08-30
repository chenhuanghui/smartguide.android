package vn.smartguide;

import android.os.Bundle;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ViewSwitcher.ViewFactory;

public class TutorialActivity extends Activity {

	ImageSwitcher tutorialSwitcher;
	ImageButton nextButton;
	int currentPage = 0;
	RelativeLayout root;
	Activity mActivity;
	ImageButton mFirstView;
	ImageButton mEighthView;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_tutorial);
		
		mActivity = this;
		
		mFirstView = (ImageButton)findViewById(R.id.imageButton3);
		mFirstView.setOnClickListener(new View.OnClickListener() {	
			@Override
			public void onClick(View v) {
				currentPage = 0;
				nextButton.setVisibility(View.INVISIBLE);
				setContent(0);
			}
		});
		
		mEighthView = (ImageButton)findViewById(R.id.imageButton2);
		mEighthView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				currentPage = 7;
				nextButton.setVisibility(View.VISIBLE);
				setContent(7);
			}
		});
		
		root = (RelativeLayout)findViewById(R.id.root);
		tutorialSwitcher = (ImageSwitcher)findViewById(R.id.tutorial);
		tutorialSwitcher.setFactory(new ViewFactory() {
			@SuppressWarnings("deprecation")
			public View makeView() {
				ImageView imageView = new ImageView(getApplicationContext());
				imageView.setScaleType(ImageView.ScaleType.FIT_START);
				imageView.setLayoutParams(new ImageSwitcher.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.FILL_PARENT));
				return imageView;
			}
		});
		
		tutorialSwitcher.setInAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_in_right));
		tutorialSwitcher.setOutAnimation(AnimationUtils.loadAnimation(this, R.anim.slide_out_left));
		
		nextButton = (ImageButton)findViewById(R.id.imageButton1);
		nextButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				
				currentPage++;
				
				if (currentPage == 0 || currentPage == 3 || currentPage == 7 || currentPage == 12 
						|| currentPage == 9 || currentPage == 13 || currentPage == 14)
					nextButton.setVisibility(View.INVISIBLE);
				
				setContent(currentPage);
				
			}
		});
		
		tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android1));
		root.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (currentPage == 1 || currentPage == 2 || currentPage == 4 || currentPage == 5 || currentPage == 6 ||
						currentPage == 8 || currentPage == 10 || currentPage == 11)
					return;
				
				currentPage++;
				
				if (currentPage == 15){
					mFirstView.setVisibility(View.INVISIBLE);
					mEighthView.setVisibility(View.INVISIBLE);
					nextButton.setVisibility(View.INVISIBLE);
					setContent(currentPage);
					return;
				}
				

				if (currentPage == 16){
					finish();
					return;
				}
				
				setContent(currentPage);
				if (currentPage != 13 && currentPage != 14)
					nextButton.setVisibility(View.VISIBLE);
			}
		});
	}
	
	private void setContent(int page){
		
		switch(page){
		case 0:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android1));
			break;
		case 1:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android2));
			break;
		case 2:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android3));
			break;
		case 3:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android4));
			break;
		case 4:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android5));
			break;
		case 5:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android6));
			break;
		case 6:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android7));
			break;
		case 7:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android8));
			break;
		case 8:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android9));
			break;
		case 9:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android10));
			break;
		case 10:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android11));
			break;
		case 11:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android12));
			break;
		case 12:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android13));
			break;
		case 13:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android14));
			break;
		case 14:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_android15));
			break;
		case 15:
			tutorialSwitcher.setImageDrawable(getResources().getDrawable(R.drawable.tutorial_end));
			break;
		}
	}
}
