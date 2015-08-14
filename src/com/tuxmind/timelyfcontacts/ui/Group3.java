package com.tuxmind.timelyfcontacts.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Contacts.Photo;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.AlphabetIndexer;
import android.widget.CursorAdapter;
import android.widget.ListView;
import android.widget.QuickContactBadge;
import android.widget.SectionIndexer;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileDescriptor;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.tuxmind.timelyfcontacts.util.ImageLoader;
import com.tuxmind.timelyfcontacts.util.Utils;
import com.tuxmind.timelyfcontacts.BuildConfig;
import com.tuxmind.timelyfcontacts.R;

public class Group3 extends Fragment implements
		AdapterView.OnItemClickListener, LoaderManager.LoaderCallbacks<Cursor> {

	private static final String TAG = "ContactsListFragment";
	ContactsAdapter2 mAdapter2;
	private ImageLoader mImageLoader;
	private OnContactsInteractionListener mOnContactSelectedListener2;
	private int mPreviouslySelectedSearchItem = 0;
	private static final String STATE_PREVIOUSLY_SELECTED_KEY = "com.tuxmind.timelygcontacts.ui.SELECTED_ITEM";
	private static final String OUT_CALL = "out_call";
	int groupId2, positionCheck, radioId, gridClick;
	long positionClick, positionClick1, radioTime;
	String groupContacts;
	ListView listView;
	boolean tabletSize;
	SharedPreferences prefs;
	static final int REQUEST_CODE = 1;
	boolean res = false;
	Bundle bundle, bundleresult, bundleclick;
	String phoneNumber;
	private String contactID;
	final static int RQS_1 = 1;
	TimePickerDialog timePickerDialog;
	PendingIntent pendingIntent, outCallLogger;
	AlarmManager alarmManager;
	int numChecked, numChecked2, mSelectedItem;
	boolean maPhone;
	boolean isActive = false;

	
	/*static Group3 newInstance(int num) {
		Group3 history = new Group3();

		Bundle args = new Bundle();
		args.putInt("num", num);

		history.setArguments(args);

		return history;
	}*/
	public static Group3 newInstance(int page, String title) {
		Group3 fragmentThird = new Group3();
		Bundle args = new Bundle();
		args.putInt("someInt", page);
		args.putString("someTitle", title);
		fragmentThird.setArguments(args);
		return fragmentThird;
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		if (savedInstanceState != null) {

		}
		setHasOptionsMenu(true);

		prefs = getActivity().getSharedPreferences(MainActivity.PREFS_NAME,
				Context.MODE_PRIVATE);
		groupId2 = prefs.getInt("myGroupId3", groupId2);
		System.out.println("myGroupId3: " + groupId2);
		mImageLoader = new ImageLoader(getActivity(),
				getListPreferredItemHeight()) {
			@Override
			protected Bitmap processBitmap(Object data) {

				return loadContactPhotoThumbnail((String) data, getImageSize());
			}
		};
		mImageLoader.setLoadingImage(R.drawable.ic_contact_picture_holo_light);
		mImageLoader.addImageCache(getActivity().getFragmentManager(), 0.1f);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		listView = new ListView(getActivity().getApplicationContext());

		mAdapter2 = new ContactsAdapter2(getActivity(),
				R.layout.contact_list_fragment1, container);
		System.out.println("viewcontainer: " + container);
		return listView;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		listView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);
		listView.setAdapter(mAdapter2);
		listView.setOnItemClickListener(this);
		listView.setOnScrollListener(new AbsListView.OnScrollListener() {
			@Override
			public void onScrollStateChanged(AbsListView absListView,
					int scrollState) {

				if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
					mImageLoader.setPauseWork(true);

				} else {
					mImageLoader.setPauseWork(false);
				}

			}

			@Override
			public void onScroll(AbsListView view, int firstVisibleItem,
					int visibleItemCount, int totalItemCount) {

			}
		});

		if (mPreviouslySelectedSearchItem == 0) {

			getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null, this);
		}
		System.out.println("LoaderActivity: "
				+ getLoaderManager().initLoader(ContactsQuery.QUERY_ID, null,
						this));

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			mOnContactSelectedListener2 = (OnContactsInteractionListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnContactsInteractionListener");
		}
	}

	@Override
	public void onPause() {
		super.onPause();
		mImageLoader.setPauseWork(false);
	}

	@SuppressLint("NewApi")
	@Override
	public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
		new QueryForPhoneNumberTask(position, v).execute();
		listView.setItemChecked(position, true);
	}

	private class QueryForPhoneNumberTask extends AsyncTask<Void, Void, Void> {

		int mPosition;
		View mView;

		private QueryForPhoneNumberTask(int intValue, View view) {
			mPosition = intValue;
			this.mView = view;
			System.out.println("mPosition: " + mPosition);
		}

		@SuppressWarnings("resource")
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			Cursor cursorID = null;
			Cursor cursorPhone = null;

			try {
				try {
					cursorID = (Cursor) listView.getItemAtPosition(mPosition);
					System.out.println("positiongrid: " + mPosition);

					final Uri uri = Contacts.getLookupUri(
							cursorID.getLong(ContactsQuery.ID),
							cursorID.getString(ContactsQuery.LOOKUP_KEY));
					System.out.println("uri: " + ContactsQuery.LOOKUP_KEY);
					cursorID = getActivity().getContentResolver().query(uri,
							new String[] { ContactsContract.Contacts._ID },
							null, null, null);
					mOnContactSelectedListener2.onContactSelected(uri);
					if (cursorID.moveToFirst()) {

						contactID = cursorID.getString(cursorID
								.getColumnIndex(ContactsContract.Contacts._ID));
					}

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					cursorID.close();
				}

				Log.d(TAG, "Contact ID: " + contactID);

				// Using the contact ID now we will get contact phone number
				cursorPhone = getActivity()
						.getContentResolver()
						.query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
								new String[] { ContactsContract.CommonDataKinds.Phone.NUMBER },

								ContactsContract.CommonDataKinds.Phone.CONTACT_ID
										+ " = ? AND "
										+ ContactsContract.CommonDataKinds.Phone.TYPE,

								new String[] { contactID }, null);

				System.out.println("cursorPhone: " + cursorPhone);

				if (cursorPhone.moveToFirst()) {
					phoneNumber = cursorPhone
							.getString(cursorPhone
									.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

				}

			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				cursorPhone.close();
			}
			return null;
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(Void params) {
			maPhone = ContactsListActivity.automaticPhone;
			mSelectedItem = mPosition;
			mAdapter2.notifyDataSetChanged();
			if (maPhone == true) {

				System.out.println("positionClick1: " + positionClick);
				System.out.println("AutoPhone: " + maPhone);
				if (gridClick == 0) {
					positionClick = listView.getItemIdAtPosition(mPosition);
					gridClick = 1;
					if (numChecked2 == 0) {
						numChecked2 = 1;
						isActive = true;
						mAdapter2.setSelectedIndex(mPosition);
						Intent data1 = new Intent(getActivity(),
								SelectDialogActivity.class);
						startActivityForResult(data1, REQUEST_CODE);
						bundleclick = new Bundle();
						bundleclick.putString("checkStatephoneNumber",
								phoneNumber);
						System.out.println("checkStatephoneNumber: "
								+ phoneNumber);

					}
				} else {

					positionClick1 = listView.getItemIdAtPosition(mPosition);
					if (positionClick == positionClick1) {
						mView.setBackground(getResources().getDrawable(
								R.drawable.list_selector));
						System.out.println("positionClick2: " + positionClick);
						gridClick = 0;
						numChecked2 = 0;
						isActive = false;
						cancelAlarm();
					} else {
						if (phoneNumber == null) {

							Toast t = Toast.makeText(getActivity()
									.getApplicationContext(),
									getString(R.string.pNumberNFound),
									Toast.LENGTH_SHORT);
							t.show();
						} else {
							Intent intent = new Intent(Intent.ACTION_CALL,
									Uri.parse("tel:" + phoneNumber));
							startActivity(intent);

						}
					}

				}
			} else {

				if (phoneNumber == null) {

					Toast t = Toast.makeText(getActivity()
							.getApplicationContext(), getString(R.string.pNumberNFound),
							Toast.LENGTH_SHORT);
					t.show();
				} else {
					Intent intent = new Intent(Intent.ACTION_CALL,
							Uri.parse("tel:" + phoneNumber));
					startActivity(intent);

				}

				System.out.println("cursorId: " + mPosition);
				System.out.println("phoneNumberclick: " + phoneNumber);
				listView.setItemChecked(mPosition, true);
			}
		}

	}
	
	private void onSelectionCleared() {
		mOnContactSelectedListener2.onSelectionCleared();
		listView.clearChoices();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt(STATE_PREVIOUSLY_SELECTED_KEY,
				listView.getCheckedItemPosition());
	}

	@Override
	public void onResume() {
		super.onResume();
		res = true;
		System.out.println("res: " + res);
		if (MainActivity.fin == true) {
			getLoaderManager()
					.restartLoader(ContactsQuery.QUERY_ID, null, this);
			System.out.println("groupIdresume: "
					+ ContactsListActivity.groupIdresult2);
			System.out.println("groupContactsresume: " + groupContacts);
		}
		if (ContactsListActivity.automaticPhone == false) {
			cancelAlarm();
			positionClick = positionClick1;
		}
		numChecked = prefs.getInt("GroupsCount", numChecked);

		if (numChecked == 3) {
			listView.setAdapter(mAdapter2);
		} else {
			System.out.println("myNumCheckedg2: " + numChecked);
			listView.setAdapter(null);
		}
	}

	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {

		if (id == ContactsQuery.QUERY_ID) {
			Uri contentUri;

			contentUri = ContactsQuery.CONTENT_URI;
			if (res == true) {
				groupContacts = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
						+ "="
						+ ContactsListActivity.groupIdresult2
						+ " AND "
						+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
						+ "='"
						+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
						+ "'"
						+ " AND "
						+ ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";
			} else {

				groupContacts = ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID
						+ "="
						+ groupId2
						+ " AND "
						+ ContactsContract.CommonDataKinds.GroupMembership.MIMETYPE
						+ "='"
						+ ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE
						+ "'"
						+ " AND "
						+ ContactsContract.Contacts.HAS_PHONE_NUMBER + "=1";
				;
			}
			System.out.println("groupContactsloader: " + groupContacts);

			return new CursorLoader(getActivity(), contentUri,
					ContactsQuery.PROJECTION, groupContacts, null,
					ContactsQuery.SORT_ORDER);
		}

		Log.e(TAG, "onCreateLoader - incorrect ID provided (" + id + ")");
		return null;
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
		// This swaps the new cursor into the adapter.
		if (loader.getId() == ContactsQuery.QUERY_ID) {
			mAdapter2.swapCursor(data);

			if (tabletSize) {

				if (data != null
						&& data.moveToPosition(mPreviouslySelectedSearchItem)) {

					final Uri uri = Uri.withAppendedPath(Contacts.CONTENT_URI,
							String.valueOf(data.getLong(ContactsQuery.ID)));
					mOnContactSelectedListener2.onContactSelected(uri);
					listView.setItemChecked(mPreviouslySelectedSearchItem, true);
				} else {
					onSelectionCleared();
				}
				mPreviouslySelectedSearchItem = 0;
			}
		}
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		if (loader.getId() == ContactsQuery.QUERY_ID) {

			mAdapter2.swapCursor(null);
		}
	}

	private int getListPreferredItemHeight() {
		final TypedValue typedValue = new TypedValue();
		getActivity().getTheme().resolveAttribute(
				android.R.attr.listPreferredItemHeight, typedValue, true);

		final DisplayMetrics metrics = new android.util.DisplayMetrics();

		getActivity().getWindowManager().getDefaultDisplay()
				.getMetrics(metrics);

		return (int) typedValue.getDimension(metrics);
	}

	private Bitmap loadContactPhotoThumbnail(String photoData, int imageSize) {

		if (!isAdded() || getActivity() == null) {
			return null;
		}

		AssetFileDescriptor afd = null;

		try {
			Uri thumbUri;

			if (Utils.hasHoneycomb()) {
				thumbUri = Uri.parse(photoData);
			} else {

				final Uri contactUri = Uri.withAppendedPath(
						Contacts.CONTENT_URI, photoData);

				thumbUri = Uri.withAppendedPath(contactUri,
						Photo.CONTENT_DIRECTORY);
			}

			afd = getActivity().getContentResolver().openAssetFileDescriptor(
					thumbUri, "r");

			FileDescriptor fileDescriptor = afd.getFileDescriptor();

			if (fileDescriptor != null) {

				return ImageLoader.decodeSampledBitmapFromDescriptor(
						fileDescriptor, imageSize, imageSize);
			}
		} catch (FileNotFoundException e) {

			if (BuildConfig.DEBUG) {
				Log.d(TAG, "Contact photo thumbnail not found for contact "
						+ photoData + ": " + e.toString());
			}
		} finally {
			if (afd != null) {
				try {
					afd.close();
				} catch (IOException e) {

				}
			}
		}

		return null;
	}

	private class ContactsAdapter2 extends CursorAdapter implements
			SectionIndexer {

		private LayoutInflater mInflater;
		private AlphabetIndexer mAlphabetIndexer;
		ViewHolder holder;
		private Drawable selectedBackground;
		private int selectedIndex;

		@SuppressWarnings("deprecation")
		public ContactsAdapter2(Context context, int contactListFragment,
				ViewGroup container) {
			super(context, null, 0);
			selectedIndex = -1;
			mInflater = LayoutInflater.from(context);
			final String alphabet = context.getString(R.string.alphabet);

			mAlphabetIndexer = new AlphabetIndexer(null,
					ContactsQuery.SORT_KEY, alphabet);
			selectedBackground = context.getResources().getDrawable(
					R.color.pressed_color);
		}

		public void setSelectedIndex(int ind) {
			selectedIndex = ind;
			notifyDataSetChanged();
		}

		public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {

			System.out.println("cursor.getCount(): " + cursor.getCount());
			final View itemLayout = mInflater.inflate(
					R.layout.contact_list_item, viewGroup, false);
			holder = new ViewHolder();
			holder.text1 = (TextView) itemLayout
					.findViewById(android.R.id.text1);
			holder.icon = (QuickContactBadge) itemLayout
					.findViewById(android.R.id.icon);
			itemLayout.setTag(holder);

			return itemLayout;
		}

		@SuppressWarnings("deprecation")
		@Override
		public void bindView(View view, Context context, final Cursor cursor) {
			// Gets handles to individual view resources

			final ViewHolder holder = (ViewHolder) view.getTag();

			final String photoUri = cursor
					.getString(ContactsQuery.PHOTO_THUMBNAIL_DATA);

			final String displayName = cursor
					.getString(ContactsQuery.DISPLAY_NAME);

			holder.text1.setText(displayName);

			final Uri contactUri = Contacts.getLookupUri(
					cursor.getLong(ContactsQuery.ID),
					cursor.getString(ContactsQuery.LOOKUP_KEY));

			holder.icon.assignContactUri(contactUri);

			mImageLoader.loadImage(photoUri, holder.icon);

			positionCheck = cursor.getPosition();
			if (maPhone == false) {
				view.setBackground(getResources().getDrawable(
						R.drawable.list_selector));
			} else {
				if (selectedIndex != -1 && positionCheck == selectedIndex
						&& isActive == true) {

					view.setBackground(selectedBackground);

				} else {

					view.setBackground(getResources().getDrawable(
							R.drawable.list_selector));
				}
				if (positionClick == positionClick1 && isActive == false) {
					view.setBackground(getResources().getDrawable(
							R.drawable.list_selector));

				}
			}
			System.out.println("SelectedPositionClick: " + positionClick + ": "
					+ positionClick1);
			System.out.println("SelectedBackground: " + positionCheck + ": "
					+ selectedIndex);
		}

		@Override
		public Cursor swapCursor(Cursor newCursor) {
			mAlphabetIndexer.setCursor(newCursor);
			return super.swapCursor(newCursor);
		}

		@Override
		public int getCount() {
			if (getCursor() == null) {
				return 0;
			}
			return super.getCount();
		}

		@Override
		public Object[] getSections() {
			return mAlphabetIndexer.getSections();
		}

		@Override
		public int getPositionForSection(int i) {
			if (getCursor() == null) {
				return 0;
			}
			return mAlphabetIndexer.getPositionForSection(i);
		}

		@Override
		public int getSectionForPosition(int i) {
			if (getCursor() == null) {
				return 0;
			}
			return mAlphabetIndexer.getSectionForPosition(i);
		}

		private class ViewHolder {
			TextView text1;
			QuickContactBadge icon;
		}
	}

	public interface OnContactsInteractionListener {

		public void onContactSelected(Uri contactUri);

		public void onSelectionCleared();
	}

	public interface ContactsQuery {

		final static int QUERY_ID = 1;

		final static Uri CONTENT_URI = ContactsContract.Data.CONTENT_URI;

		final static Uri FILTER_URI = Contacts.CONTENT_FILTER_URI;

		@SuppressLint("InlinedApi")
		final static String SELECTION = "("
				+ ContactsContract.Contacts.IN_VISIBLE_GROUP + " = '1' AND ("
				+ ContactsContract.Contacts.HAS_PHONE_NUMBER + " != 0 ))";

		@SuppressLint("InlinedApi")
		final static String SORT_ORDER = Utils.hasHoneycomb() ? Contacts.SORT_KEY_PRIMARY
				: Contacts.DISPLAY_NAME;

		@SuppressLint("InlinedApi")
		final static String[] PROJECTION = {

				Contacts._ID,

				Contacts.LOOKUP_KEY,

				Utils.hasHoneycomb() ? ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME
						: ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,

				Utils.hasHoneycomb() ? Contacts.PHOTO_THUMBNAIL_URI
						: Contacts._ID,

				SORT_ORDER, };

		final static int ID = 0;
		final static int LOOKUP_KEY = 1;
		final static int DISPLAY_NAME = 2;
		final static int PHOTO_THUMBNAIL_DATA = 3;
		final static int SORT_KEY = 4;
	}

	public void updateGridListView() {
		if (mAdapter2 != null) {
			mAdapter2.notifyDataSetChanged();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode,
			Intent dataDialog) {
		super.onActivityResult(requestCode, resultCode, dataDialog);

		getActivity();
		if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE) {
			if (dataDialog.hasExtra("returnTime")) {

				radioId = dataDialog.getExtras().getInt("returnTime");
				if (radioId == 0) {
					radioTime = 60 * 5000;
				}
				if (radioId == 1) {
					radioTime = 60 * 10000;
				}
				if (radioId == 2) {
					radioTime = 60 * 30000;
				}
				if (radioId == 3) {
					radioTime = 60 * 60000;
				}
				if (radioId == 4) {
					radioTime = 60 * 180000;
				}
				System.out.println("radioId: " + radioId);
				System.out.println("radioTime: " + radioTime);
				if (bundleclick != null) {
					phoneNumber = bundleclick
							.getString("checkStatephoneNumber");
				}
				setAlarm(radioTime, phoneNumber);
				System.out.println("phoneNumberResult: " + phoneNumber);

			}
		}
	}

	private void setAlarm(long radioTime, String phoneNumber2) {
		// TODO Auto-generated method stub

		Intent intent = new Intent(getActivity().getBaseContext(),
				AlarmReceiver.class).putExtra("mPhoneNumber", phoneNumber2);

		pendingIntent = PendingIntent.getBroadcast(getActivity()
				.getBaseContext(), RQS_1, intent,
				PendingIntent.FLAG_CANCEL_CURRENT);
		outCallLogger = PendingIntent.getBroadcast(getActivity()
				.getBaseContext(), 0, new Intent(OUT_CALL), 0);
		alarmManager = (AlarmManager) getActivity().getApplicationContext()
				.getSystemService(Context.ALARM_SERVICE);
		alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, radioTime,
				radioTime, pendingIntent);
		System.out.println("AllarmeSet:" + alarmManager);
		System.out.println("phoneNumberAlarm: " + phoneNumber2);
	}

	private void cancelAlarm() {
		if (alarmManager != null) {
			alarmManager = (AlarmManager) getActivity().getApplicationContext()
					.getSystemService(Context.ALARM_SERVICE);
			alarmManager.cancel(pendingIntent);
			alarmManager.cancel(outCallLogger);
			System.out.println("AllarmeCanc:" + alarmManager);
			Toast t = Toast.makeText(getActivity().getApplicationContext(),
					getString(R.string.autoPhoneDisabled), Toast.LENGTH_SHORT);
			t.show();
		}

	}

	/*
	 * private void save(final boolean isChecked) {
	 * 
	 * SharedPreferences.Editor editor = prefs.edit();
	 * editor.putBoolean("check", checkState[positionCheck]); editor.commit(); }
	 * 
	 * private boolean load() { return prefs.getBoolean("check",
	 * checkState[positionCheck]); }
	 */
}
