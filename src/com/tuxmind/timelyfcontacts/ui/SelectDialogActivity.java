package com.tuxmind.timelyfcontacts.ui;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.tuxmind.timelyfcontacts.R;

@SuppressWarnings("deprecation")
public class SelectDialogActivity extends ActionBarActivity {

	RadioGroup radioGroup;
	final String KEY_SAVED_RADIO_BUTTON_INDEX = "SAVED_RADIO_BUTTON_INDEX";
	static int checkedIndex;
	int savedRadioIndex;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		getActionBar();
		setContentView(R.layout.time_preferences);
		radioGroup = (RadioGroup) findViewById(R.id.radiogroup);
		radioGroup
				.setOnCheckedChangeListener(radioGroupOnCheckedChangeListener);
		LoadPreferences();
	}

	OnCheckedChangeListener radioGroupOnCheckedChangeListener = new OnCheckedChangeListener() {

		@Override
		public void onCheckedChanged(RadioGroup group, int checkedId) {

			RadioButton checkedRadioButton = (RadioButton) radioGroup
					.findViewById(checkedId);
			checkedIndex = radioGroup.indexOfChild(checkedRadioButton);

			SavePreferences(KEY_SAVED_RADIO_BUTTON_INDEX, checkedIndex);
		}
	};

	private void SavePreferences(String key, int value) {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"WorkerContactsPrefs", MODE_PRIVATE);
		SharedPreferences.Editor editor = sharedPreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}

	private void LoadPreferences() {
		SharedPreferences sharedPreferences = getSharedPreferences(
				"WorkerContactsPrefs", MODE_PRIVATE);
		savedRadioIndex = sharedPreferences.getInt(
				KEY_SAVED_RADIO_BUTTON_INDEX, 0);
		RadioButton savedCheckedRadioButton = (RadioButton) radioGroup
				.getChildAt(savedRadioIndex);
		savedCheckedRadioButton.setChecked(true);
	}
	
	@Override
	public void onBackPressed() {

		finish();

	}
	@Override
	public void finish() {
		// Prepare data intent
		Intent dataDialog = new Intent();
		dataDialog.putExtra("returnTime", savedRadioIndex);
		setResult(RESULT_OK, dataDialog);
		Toast t = Toast.makeText(this,
				getString(R.string.autoPhoneEnabled),
				Toast.LENGTH_SHORT);
		t.show();
		super.finish();
	}
}