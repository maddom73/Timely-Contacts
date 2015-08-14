package com.tuxmind.timelyfcontacts.ui;

import java.util.Locale;

import com.tuxmind.timelyfcontacts.util.SmartFragmentStatePagerAdapter;
import com.tuxmind.timelyfcontacts.util.SupportFragmentTabListener;
import com.tuxmind.timelyfcontacts.R;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBar.Tab;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

@SuppressWarnings("deprecation")
public class ContactsListActivity extends ActionBarActivity implements
		ContactsListFragment.OnContactsInteractionListener,
		Group2.OnContactsInteractionListener,
		Group3.OnContactsInteractionListener {

	String error;
	private SectionsPagerAdapter mSectionsPagerAdapter;
	SharedPreferences prefs = null;
	SharedPreferences.Editor ed;
	public static ViewPager mViewPager;
	ContactsListFragment cListFragment;
	Group2 group2;
	Group3 group3;
	FragmentTransaction ft;
	int groupId, groupId1, groupId2;
	int myNumChecked;
	private static final int REQUEST_CODE = 1337;
	boolean myactive = false;
	static int groupIdresult, groupIdresult1, groupIdresult2;
	static boolean automaticPhone;
	public static boolean automaticSms;
	ActionBar actionBar;
	boolean increasing;
	int NUM_ITEMS, ind;
	Tab tab1, tab2, tab3;
	String mtitle1, mtitle2, mtitle3;
	boolean checkMyTab;
	int nindv;
	Tab mTab;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		/*if (BuildConfig.DEBUG) {
			Utils.enableStrictMode();
		}*/
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		prefs = getSharedPreferences(MainActivity.PREFS_NAME, MODE_PRIVATE);
		ed = prefs.edit();
		NUM_ITEMS = prefs.getInt("NUM_ITEMS", NUM_ITEMS);

		mSectionsPagerAdapter = new SectionsPagerAdapter(
				getSupportFragmentManager());

		mViewPager = (ViewPager) findViewById(R.id.pager);
		mViewPager.setAdapter(mSectionsPagerAdapter);
		mViewPager.setOffscreenPageLimit(3);
		// mSectionsPagerAdapter.getRegisteredFragment(mViewPager.getCurrentItem());
		actionBar = getSupportActionBar();
		actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
		actionBar.setDisplayShowTitleEnabled(true);

		mtitle1 = prefs.getString("titolo1", mtitle1);
		mtitle2 = prefs.getString("titolo2", mtitle2);
		mtitle3 = prefs.getString("titolo3", mtitle3);

		mViewPager
				.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
					@Override
					public void onPageSelected(int position) {
						actionBar.setSelectedNavigationItem(position);
						ind = actionBar.getSelectedNavigationIndex();
						System.out.println("Tab position: "
								+ actionBar.getSelectedNavigationIndex());
					}
				});

		if (prefs.getBoolean("firstrun", true)) {
			Intent data = new Intent(this, MainActivity.class);
			startActivityForResult(data, REQUEST_CODE);
		}
	
		setupTabs();
	}

	@Override
	public void onStart() {
		super.onStart();
		
	}

	@Override
	public void onStop() {
		super.onStop();

	}

	

	private void setupTabs() {
		if (Locale.getDefault().getLanguage().equals("it")) {
			if (mtitle1 != null && mtitle1.equals("My Contacts")) {
				mtitle1 = "Contatti";
			} else if (mtitle2 != null && mtitle2.equals("My Contacts")) {
				mtitle2 = "Contatti";
			} else if (mtitle3 != null && mtitle3.equals("My Contacts")) {
				mtitle2 = "Contatti";
			}
			if (mtitle1 != null && mtitle1.equals("Starred in Android")) {
				mtitle1 = "Preferiti";
			} else if (mtitle2 != null && mtitle2.equals("Starred in Android")) {
				mtitle2 = "Preferiti";
			} else if (mtitle3 != null && mtitle3.equals("Starred in Android")) {
				mtitle3 = "Preferiti";
			}
		}
		tab1 = actionBar
				.newTab()
				.setText(mtitle1)
				.setTabListener(
						new SupportFragmentTabListener<ContactsListFragment>(
								R.id.pager, this, "first",
								ContactsListFragment.class));

		tab2 = actionBar
				.newTab()
				.setText(mtitle2)
				.setTabListener(
						new SupportFragmentTabListener<Group2>(R.id.pager,
								this, "second", Group2.class));

		tab3 = actionBar
				.newTab()
				.setText(mtitle3)
				.setTabListener(
						new SupportFragmentTabListener<Group3>(R.id.pager,
								this, "third", Group3.class));

		if (NUM_ITEMS == 1) {
			actionBar.addTab(tab1);

		}
		if (NUM_ITEMS == 2) {
			actionBar.addTab(tab1);
			actionBar.addTab(tab2);

		}
		if (NUM_ITEMS == 3) {
			actionBar.addTab(tab1);
			actionBar.addTab(tab2);
			actionBar.addTab(tab3);
		}
		if (checkMyTab == true) {
			actionBar.selectTab(mTab);
		}
		mSectionsPagerAdapter.notifyDataSetChanged();
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (prefs.getBoolean("firstrun", true)) {

			prefs.edit().putBoolean("firstrun", false).commit();
		}
		if (checkMyTab == true) {
			if (myNumChecked == 1) {
				NUM_ITEMS = 1;

			}
			if (myNumChecked == 2) {
				NUM_ITEMS = 2;

			}
			if (myNumChecked == 3) {
				NUM_ITEMS = 3;

			}
			new CAsync().execute();
		}

		mSectionsPagerAdapter.notifyDataSetChanged();

	}

	private class CAsync extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			try {

				ed.putInt("NUM_ITEMS", NUM_ITEMS);

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
			mTab = actionBar.getSelectedTab();
			actionBar.removeAllTabs();
			setupTabs();

		}

	}

	@Override
	public boolean onCreateOptionsMenu(final Menu menu) {
		final MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.contact_list_menu, menu);
		return true;

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {

		case R.id.menu_setting:
			Intent data = new Intent(this, MainActivity.class);
			startActivityForResult(data, REQUEST_CODE);
			overridePendingTransition(R.animator.push_right_in,
					R.animator.push_right_out);
			
	
			break;

		case R.id.menu_about:
			AboutDialog about = new AboutDialog(this);
			about.setTitle("Timely Contacts");
			about.show();
			break;
		}

		return super.onOptionsItemSelected(item);
	}




	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode == RESULT_OK && requestCode == REQUEST_CODE) {
			if (data.hasExtra("returnID1") || data.hasExtra("returnID2")
					|| data.hasExtra("returnID3")) {

				groupId = data.getExtras().getInt("returnID1");
				groupId1 = data.getExtras().getInt("returnID2");
				groupId2 = data.getExtras().getInt("returnID3");
				mtitle1 = data.getExtras().getString("mTitolo1");
				mtitle2 = data.getExtras().getString("mTitolo2");
				mtitle3 = data.getExtras().getString("mTitolo3");
				System.out.println("title: " + mtitle1);
				myNumChecked = data.getExtras().getInt("numeri");
				checkMyTab = data.getExtras().getBoolean("CheckCheck");
				automaticPhone = data.getExtras().getBoolean("AutoPhone");
				System.out.println("AutoPhoneActivity: " + automaticPhone);
				automaticSms = data.getExtras().getBoolean("AutoSms");
				System.out.println("AutoSmsActivity: " + automaticSms);
				groupIdresult = groupId;
				groupIdresult1 = groupId1;
				groupIdresult2 = groupId2;
				System.out.println("groupIdresult: " + groupId);
				System.out.println("groupIdresult1: " + groupId1);
				System.out.println("groupIdresult2: " + groupId2);
				myactive = true;
				System.out.println("myactive: " + myactive);

			}
		}
	}

	@Override
	public void onContactSelected(Uri contactUri) {

	}

	@Override
	public void onSelectionCleared() {

	}

	public class SectionsPagerAdapter extends SmartFragmentStatePagerAdapter {

		public SectionsPagerAdapter(FragmentManager fm) {
			super(fm);
			// TODO Auto-generated constructor stub
		}

		@Override
		public int getCount() {
			return NUM_ITEMS;
		}

		// Returns the fragment to display for that page
		@Override
		public Fragment getItem(int position) {
			switch (position) {
			case 0:
				return ContactsListFragment.newInstance(0, "Page # 1");
			case 1:
				return Group2.newInstance(1, "Page # 2");
			case 2:
				return Group3.newInstance(2, "Page # 3");
			default:
				return null;
			}
		}

		@Override
		public CharSequence getPageTitle(int position) {
			System.out.println("actiontitle: " + mtitle1 + ": " + position);
			return "" + position;

		}
	}

}
