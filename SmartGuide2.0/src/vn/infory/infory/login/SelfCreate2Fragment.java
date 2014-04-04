package vn.infory.infory.login;

import vn.infory.infory.CyUtils;
import vn.infory.infory.FontsCollection;
import vn.infory.infory.R;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AlphaAnimation;
import android.widget.DatePicker;
import android.widget.ImageButton;
import android.widget.TextView;

import com.cycrix.androidannotation.AndroidAnnotationParser;
import com.cycrix.androidannotation.Click;
import com.cycrix.androidannotation.ViewById;

public class SelfCreate2Fragment extends Fragment {
	
	// data
	private Listener mListener;
	private boolean mHasChoseDob;
	private boolean mSex;

	// GUI
	@ViewById(id = R.id.txtDay)		private TextView mTxtDay;
	@ViewById(id = R.id.txtMonth)	private TextView mTxtMonth;
	@ViewById(id = R.id.txtYear)	private TextView mTxtYear;
	@ViewById(id = R.id.txtDOBNoti)	private TextView mTxtDOBNoti;

	@ViewById(id = R.id.txtMale)	private TextView mTxtMale;
	@ViewById(id = R.id.txtFemale)	private TextView mTxtFemale;

	@ViewById(id = R.id.btnConfirm)	private ImageButton mBtnConfirm;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.login_self_create_2, container, false);
	}

	@Override
	public void onViewCreated(View view, Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		try {
			AndroidAnnotationParser.parse(this, view);
		} catch (Exception e) {
			e.printStackTrace();
			return;
		}

		FontsCollection.setFont(view);
		CyUtils.setHoverEffect(mBtnConfirm, false);
	}
	
	public void setListener(Listener listener) {
		mListener = listener;
	}
	
	/**
	 * 
	 * @return
	 * day : 1 - 31<br/>
	 * month : 1 - 12<br/>
	 * year<br/>
	 * sex : 0 - 1
	 */
	public int[] getResult() {
		int[] result = new int[4];
		result[0] = Integer.parseInt(mTxtDay.getText().toString());
		result[1] = Integer.parseInt(mTxtMonth.getText().toString());
		result[2] = Integer.parseInt(mTxtYear.getText().toString());
		result[3] = mSex ? 1 : 0;
		return result;
	}

	private DatePickerDialog dlg;
	@Click(id = R.id.layoutDOB)
	private void onDOBClick(View v) {

		mTxtDOBNoti.setVisibility(View.INVISIBLE);
		mHasChoseDob = true;
		int d = Integer.parseInt(mTxtDay.getText().toString());
		int m = Integer.parseInt(mTxtMonth.getText().toString()) - 1;
		int y = Integer.parseInt(mTxtYear.getText().toString()); 

		dlg = new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
			@Override
			public void onDateSet(DatePicker view, int year, int monthOfYear,
					int dayOfMonth) {
				mTxtDay.setText(Integer.toString(dayOfMonth));
				mTxtMonth.setText(Integer.toString(monthOfYear + 1));
				mTxtYear.setText(Integer.toString(year));
			}
		}, y, m, d) {
			@Override
			public void onDateChanged(DatePicker view, int year, int month,
					int day) {
				dlg.setTitle(makeVNDate(day, month, year));
			}
		};
		dlg.setTitle(makeVNDate(d, m, y));
		dlg.show();
	}

	@Click(id = R.id.txtMale)
	private void onMaleClick(View v) {
		setSex(true);
	}

	@Click(id = R.id.txtFemale)
	private void onFemaleClick(View v) {
		setSex(false);
	}

	@Click(id = R.id.btnConfirm)
	private void onConfirmClick(View v) {
		if (!mHasChoseDob) {
			toggleDobNoti(true);
		} else {
			mListener.onConfirmClick();
		}
	}
	
	private void toggleDobNoti(boolean show) {
		float startAlpha = show ? 0 : 1;
		AlphaAnimation animation = new AlphaAnimation(startAlpha, 1 - startAlpha);
		animation.setFillAfter(true);
		animation.setDuration(300);
		mTxtDOBNoti.startAnimation(animation);
	}

	private void setSex(boolean isMale) {
		mSex = isMale;
		TextView txtCheck = isMale ? mTxtMale : mTxtFemale;
		TextView txtUncheck = isMale ? mTxtFemale : mTxtMale;

		txtCheck.setCompoundDrawablesWithIntrinsicBounds(
				null, null, null, getResources().getDrawable(R.drawable.button_tickon));
		txtUncheck.setCompoundDrawablesWithIntrinsicBounds(
				null, null, null, getResources().getDrawable(R.drawable.button_tickoff));
	}

	private String makeVNDate(int day, int month, int year) {
		return new StringBuilder()
		.append("ngày ").append(day)
		.append(" tháng ").append(month+1)
		.append(" năm ").append(year)
		.toString();
	}

	///////////////////////////////////////////////////////////////////////////
	// Listener
	///////////////////////////////////////////////////////////////////////////

	public static class Listener {
		public void onConfirmClick() {}
	}
}