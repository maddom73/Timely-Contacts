package com.tuxmind.timelyfcontacts.ui;

import java.util.Locale;

import com.tuxmind.timelyfcontacts.R;
import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

@SuppressWarnings("deprecation")
public class MainActivity extends ActionBarActivity {


	private final static String GROUPSCOUNT = "GroupsCount";
	private final static String GROUPSID = "GroupsID";
	final static String PREFS_NAME = "WorkerContactsPrefs";
	LayoutInflater inflater;
	private final static String TAG = "SelectGroups";
	private CustomGroupAdapter adapter;
	private Cursor GroupCur;
	private Context context;
	int Id;
	String title;
	static boolean fin = false;
	int numChecked;
	ListView Groups;
	SharedPreferences mPrefs;
	SharedPreferences.Editor ed;
	Switch star, star1;
	boolean autoPhone, autoSms;
	boolean[] autoCheck;
	boolean checkCheck = false;
	Bundle pBundle;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		@SuppressWarnings("unused")
		int i, j, SavedGroupCount;
		context = this;
		inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		super.onCreate(savedInstanceState);
		getActionBar();
		// actionBar.setHomeButtonEnabled(true);
		setContentView(R.layout.activity_select_groups);
		Groups = (ListView) findViewById(R.id.groupsLv);
		star = (Switch) findViewById(R.id.star);
		star1 = (Switch) findViewById(R.id.star1);
		GroupCur = getAllGroups();
		mPrefs = getSharedPreferences(PREFS_NAME, 0);
		ed = mPrefs.edit();
		String[] fieldsT = new String[] { ContactsContract.Groups.TITLE };
		int[] to = new int[] { R.id.groupNameChk };
		adapter = new CustomGroupAdapter(this, R.layout.groupselectlayout,
				GroupCur, fieldsT, to);
		autoPhone = mPrefs.getBoolean("AutomaticPhoneCall", autoPhone);
		autoSms = mPrefs.getBoolean("AutomaticSms", autoSms);
		System.out.println("autoPhoneMain: " + autoPhone);
		System.out.println("autoSmsMain: " + autoSms);
		Groups.setAdapter(adapter);
		if (mPrefs.getBoolean("isFirstRun", true)) {
			Spanned sp = Html.fromHtml(getString(R.string.cookiesmsg));
			((TextView) new AlertDialog.Builder(this)
					.setTitle(getString(R.string.cookies))
					.setMessage(sp)
					.setNeutralButton(getString(R.string.cookiescl),
							new OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									mPrefs.edit()
											.putBoolean("isFirstRun", false)
											.commit();
									
								}
							}).show()
							.findViewById(android.R.id.message))
						    .setMovementMethod(LinkMovementMethod.getInstance());
		}
	}

	private Cursor getAllGroups() {
		Log.v(TAG, "Activity getAllGroups Started");
		Uri uri = ContactsContract.Groups.CONTENT_SUMMARY_URI;

		String[] projection = new String[] { ContactsContract.Groups._ID,
				ContactsContract.Groups.TITLE,
				ContactsContract.Groups.SUMMARY_COUNT,
				ContactsContract.Groups.SUMMARY_WITH_PHONES,
				ContactsContract.Groups.ACCOUNT_NAME,
				ContactsContract.Groups.ACCOUNT_TYPE, };

		String selection = ContactsContract.Groups.SUMMARY_WITH_PHONES + "> 0";
		String[] selectionArgs = null;
		String sortOrder = ContactsContract.Groups.TITLE
				+ " COLLATE LOCALIZED ASC";
		return getContentResolver().query(uri, projection, selection,
				selectionArgs, sortOrder);
	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.main, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.action_whatever:

			break;
		}

		return super.onOptionsItemSelected(item);
	}

	private class CustomGroupAdapter extends SimpleCursorAdapter {
		public boolean[] checkState;
		ViewHolder viewholder;

		public CustomGroupAdapter(Context context, int layout, Cursor c,
				String[] from, int[] to) {
			super(context, layout, c, from, to);
			checkState = new boolean[c.getCount()];
			autoCheck = new boolean[c.getCount()];
			// Initialize Checked State
			int i = 0;
			int j = 1;
			SharedPreferences mPrefs = getSharedPreferences(PREFS_NAME, 0);
			int SavedGroupCount = mPrefs.getInt(GROUPSCOUNT, 0);
			Log.v(TAG, "SavedGroupCount " + SavedGroupCount);
			if (SavedGroupCount > 0) {
				while (i < c.getCount()) {
					c.moveToPosition(i);
					j = 1;
					while (j <= SavedGroupCount) {
						if (c.getInt(0) == mPrefs.getInt(GROUPSID + j, -1)) {
							checkState[i] = true;
							Log.v(TAG,
									"Loading Saved Group "
											+ mPrefs.getInt(GROUPSID + j, -1));

						}
						j++;

					}
					i++;
				}
			}

		}

		private class ViewHolder {
			CheckBox checkBox;
		}

		@SuppressLint("InflateParams")
		@Override
		public View getView(final int position, View convertView,
				ViewGroup parent) {

			if (convertView == null) {
				convertView = inflater
						.inflate(R.layout.groupselectlayout, null);
				viewholder = new ViewHolder();

				viewholder.checkBox = (CheckBox) convertView
						.findViewById(R.id.groupNameChk);

				convertView.setTag(viewholder);

				if (checkState[position]) {
					viewholder.checkBox.setChecked(true);
				}
			} else
				viewholder = (ViewHolder) convertView.getTag();

			GroupCur.moveToPosition(position);
			viewholder.checkBox.setText(GroupCur.getString(1));
			if (Locale.getDefault().getLanguage().equals("it")) {
				if (GroupCur.getString(1).equals("My Contacts")) {
					viewholder.checkBox.setText("Contatti");
				}
				if (GroupCur.getString(1).equals("Starred in Android")) {
					viewholder.checkBox.setText("Preferiti");
				}
			}
			viewholder.checkBox.setChecked(checkState[position]);

			viewholder.checkBox.setOnClickListener(new View.OnClickListener() {

				public void onClick(View v) {
					if (((CheckBox) v).isChecked()) {
						checkState[position] = true;

					} else
						checkState[position] = false;

				}

			});
			viewholder.checkBox
					.setOnCheckedChangeListener(new OnCheckedChangeListener() {

						public void onCheckedChanged(CompoundButton buttonView,
								boolean isChecked) {
							// TODO Auto-generated method stub
							checkCheck = true;
						}
					});

			star.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub

					if (buttonView.isChecked()) {
						autoPhone = true;
						autoCheck[position] = true;
						setAutoPhone(position);

					} else {
						autoPhone = false;
						autoCheck[position] = false;
						star1.setChecked(false);
						Toast.makeText(getBaseContext(),
								getString(R.string.autoPhoneDisabled),
								Toast.LENGTH_SHORT).show();
					}

				}
			});
			star.setChecked(autoPhone);

			star1.setOnCheckedChangeListener(new OnCheckedChangeListener() {

				public void onCheckedChanged(CompoundButton buttonView,
						boolean isChecked) {
					// TODO Auto-generated method stub

					if (buttonView.isChecked()) {
						if (autoPhone == true) {
							autoSms = true;
							autoCheck[position] = true;
							setAutoSms(position);
						} else {

							buttonView.setChecked(false);
							autoCheck[position] = false;
							Toast.makeText(getBaseContext(),
									getString(R.string.autoSmsAdvice),
									Toast.LENGTH_SHORT).show();
						}
					} else {
						autoSms = false;

						Toast.makeText(getBaseContext(),
								getString(R.string.autoSmsDisabled),
								Toast.LENGTH_SHORT).show();
					}

				}
			});
			star1.setChecked(autoSms);
			return convertView;
		}

	}

	@SuppressLint("InflateParams")
	public void setAutoPhone(int position) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		LayoutInflater adbInflater = LayoutInflater.from(this);
		View eulaLayout = adbInflater.inflate(R.layout.dialogcheck, null);
		final CheckBox dontShowAgain = (CheckBox) eulaLayout
				.findViewById(R.id.skip);
		adb.setView(eulaLayout);
		adb.setTitle(getString(R.string.autoDialogTitle));
		adb.setMessage(Html.fromHtml(getString(R.string.autoPhone)));
		adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String checkBoxResult = "NOT checked";
				try {
					if (dontShowAgain.isChecked())
						checkBoxResult = "checked";
					ed.putString("skipMessage", checkBoxResult);
					// Commit the edits!

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					ed.commit();
					Toast.makeText(getBaseContext(),
							getString(R.string.autoPhoneEnabled),
							Toast.LENGTH_SHORT).show();
				}
				return;
			}
		});

		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String checkBoxResult = "NOT checked";
				try {
					if (dontShowAgain.isChecked())
						checkBoxResult = "checked";
					ed = mPrefs.edit();
					ed.putString("skipMessage", checkBoxResult);
					star.setChecked(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					ed.commit();
				}
				return;
			}

		});
		String skipMessage = mPrefs.getString("skipMessage", "NOT checked");
		if (!skipMessage.equals("checked") && autoCheck[position] == true) {
			adb.show();
			System.out.println("skipMessage: " + skipMessage);
		} else {
			Toast.makeText(getBaseContext(),
					getString(R.string.autoPhoneEnabled), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@SuppressLint("InflateParams")
	public void setAutoSms(int position) {
		AlertDialog.Builder adb = new AlertDialog.Builder(this);
		LayoutInflater adbInflater = LayoutInflater.from(this);
		View eulaLayout = adbInflater.inflate(R.layout.dialogcheck, null);
		final CheckBox dontShowAgain = (CheckBox) eulaLayout
				.findViewById(R.id.skip);
		adb.setView(eulaLayout);
		adb.setTitle(getString(R.string.autoDialogTitle));
		adb.setMessage(Html.fromHtml(getString(R.string.autoSms)));
		adb.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String checkBoxResult = "NOT checked";
				try {
					if (dontShowAgain.isChecked())
						checkBoxResult = "checked";
					ed = mPrefs.edit();
					ed.putString("skipMessageSms", checkBoxResult);

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					// Commit the edits!
					ed.commit();
					Toast.makeText(getBaseContext(),
							getString(R.string.autoSmsEnabled),
							Toast.LENGTH_SHORT).show();

				}
				return;
			}
		});

		adb.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				String checkBoxResult = "NOT checked";
				try {
					if (dontShowAgain.isChecked())
						checkBoxResult = "checked";
					ed = mPrefs.edit();
					ed.putString("skipMessageSms", checkBoxResult);
					star1.setChecked(false);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					// Commit the edits!
					ed.commit();

				}
				return;
			}
		});
		String skipMessageSms = mPrefs.getString("skipMessageSms",
				"NOT checked");
		if (!skipMessageSms.equals("checked") && autoCheck[position] == true) {
			adb.show();
			System.out.println("skipMessageSms: " + skipMessageSms);
		} else {
			Toast.makeText(getBaseContext(),
					getString(R.string.autoSmsEnabled), Toast.LENGTH_SHORT)
					.show();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
	}

	@Override
	public void onBackPressed() {
		new MainAsync().execute();
	}

	private int getCheckedCount() {
		int count, i;
		count = 0;
		i = 0;
		if (GroupCur.getCount() > 0) {
			while (i < adapter.getCount()) {
				if (adapter.checkState[i]) {

					count++;
				}
				i++;
			}
		}
		Log.v(TAG, "CheckedCount " + count);
		return count;

	}

	private class MainAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			numChecked = getCheckedCount();
			int i = 0;
			int j = 0;
			ed.putBoolean("AutomaticPhoneCall", autoPhone);
			ed.putBoolean("AutomaticSms", autoSms);

			try {
				if (numChecked > 0 && numChecked < 4) {
					ed.putInt(GROUPSCOUNT, numChecked);
					while (i < adapter.getCount()) {
						if (adapter.checkState[i]) {
							j++;
							GroupCur.moveToPosition(i);
							System.out.println("Cursore: " + i);
							int idcolumn = GroupCur
									.getColumnIndex(ContactsContract.Groups._ID);
							int titlecolumn = GroupCur
									.getColumnIndex(ContactsContract.Groups.TITLE);
							title = GroupCur.getString(titlecolumn);
							System.out.println("titolomio: " + title);
							Id = GroupCur.getInt(idcolumn);
							ed.putInt("myGroupId" + j, Id);
							ed.putString("titolo" + j, title);
							ed.putInt(GROUPSID + j, GroupCur.getInt(0));
						}
						i++;
					}

				}
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {

				ed.commit();
			}

			return null;

		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);

			if (numChecked == 0) {

				Log.v(TAG, "OnClick Group Save nothing Selected");
				Toast t = Toast.makeText(context,
						getString(R.string.select1group), Toast.LENGTH_SHORT);
				t.show();

			} else if (numChecked > 3) {
				Toast t = Toast.makeText(context,
						getString(R.string.select3groups), Toast.LENGTH_SHORT);
				t.show();
			} else {
				finish();
			}

		}

	}


	@Override
	public void finish() {
		// Prepare data intent

		Intent data = new Intent();
		data.putExtra("returnID1", mPrefs.getInt("myGroupId1", Id));
		data.putExtra("returnID2", mPrefs.getInt("myGroupId2", Id));
		data.putExtra("returnID3", mPrefs.getInt("myGroupId3", Id));
		data.putExtra("mTitolo1", mPrefs.getString("titolo1", title));
		data.putExtra("mTitolo2", mPrefs.getString("titolo2", title));
		data.putExtra("mTitolo3", mPrefs.getString("titolo3", title));
		data.putExtra("numeri", numChecked);
		data.putExtra("AutoPhone", autoPhone);
		data.putExtra("AutoSms", autoSms);
		data.putExtra("CheckCheck", checkCheck);
		setResult(RESULT_OK, data);
		fin = true;
		super.finish();
		overridePendingTransition(R.animator.push_right_in,
				R.animator.push_right_out);
		GroupCur.close();
	}
}
