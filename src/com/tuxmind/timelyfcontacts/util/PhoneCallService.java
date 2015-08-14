package com.tuxmind.timelyfcontacts.util;

import java.util.Date;

import com.tuxmind.timelyfcontacts.ui.ContactsListActivity;
import com.tuxmind.timelyfcontacts.R;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.os.Vibrator;
import android.provider.BaseColumns;
import android.provider.CallLog;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class PhoneCallService extends Service {

	int mynumber;

	@Override
	public void onStart(Intent intent, int startId) {
		if (intent != null) {
			mynumber = intent.getExtras().getInt("contatore");
			System.out.println("Number service " + mynumber);
		}
	}

	private Handler mHandler = new Handler();
	private ContentObserver mLogContentObserver = new ContentObserver(mHandler) {

		@Override
		public boolean deliverSelfNotifications() {
			return true;
		}

		@Override
		public void onChange(boolean selfChange) {
			Log.d("Call Log", "MyContentObserver.onChange(" + selfChange + ")");
			super.onChange(selfChange);
			new PhoneCallAsync().execute();
		}
	};

	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	public void onCreate() {
		getContentResolver().registerContentObserver(CallLog.Calls.CONTENT_URI,
				true, this.mLogContentObserver);
		super.onCreate();
	}

	public void onDestroy() {
		getContentResolver()
				.unregisterContentObserver(this.mLogContentObserver);
		super.onDestroy();
	}

	private class PhoneCallAsync extends AsyncTask<Void, Void, Void> {
		String mnumber;
		String duration;
		Date callDayTime;
		Cursor cur = null;
		int numberColumn;
		int durationcolumn;
		int date;
		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
			
			StringBuffer sb = null;
			try {
				sb = new StringBuffer();
				String[] projection = new String[] { BaseColumns._ID,
						CallLog.Calls.NUMBER, CallLog.Calls.TYPE,
						CallLog.Calls.DATE, CallLog.Calls.DURATION };
				sb.append("Call Details :");
				ContentResolver resolver = getContentResolver();
				cur = resolver.query(CallLog.Calls.CONTENT_URI, projection,
						null, null, CallLog.Calls.DEFAULT_SORT_ORDER);
				numberColumn = cur.getColumnIndex(CallLog.Calls.NUMBER);
				durationcolumn = cur.getColumnIndex(CallLog.Calls.DURATION);
				date = cur.getColumnIndex(CallLog.Calls.DATE);
			} catch (NumberFormatException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} finally {
				if (!cur.moveToNext()) {
					cur.close();

				}

				mnumber = cur.getString(numberColumn);
				duration = cur.getString(durationcolumn);
				String callDate = cur.getString(date);
				callDayTime = new Date(Long.valueOf(callDate));
				sb.append("\nPhone Number:--- " + mnumber + " \nCall Type:--- "
						+ " \nCall Date:--- " + callDayTime
						+ " \nCall duration in sec :--- " + duration);
				sb.append("\n----------------------------------");
				System.out.println("PhoneAnswered: " + sb);
			}
			

			return null;

		}

		@Override
		protected void onPostExecute(Void params) {
			super.onPostExecute(params);
			int myDur = 0;
			boolean maSms = ContactsListActivity.automaticSms;
			try {
				myDur = Integer.parseInt(duration.toString());
				if (myDur > 0) {
					Vibrator v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
					// Vibrate for 500 milliseconds
					v.vibrate(500);
					Toast t = Toast.makeText(getApplicationContext(),
							"Answered", Toast.LENGTH_SHORT);
					t.show();
				}
				System.out.println("Sms Condition before: " + maSms + ": "
						+ mynumber + ": " + myDur);
				if (maSms == true && mynumber > 4 && myDur == 0) {
					System.out.println("Sms Condition after : " + maSms + ": "
							+ mynumber + ": " + myDur);
					SmsManager sms = SmsManager.getDefault();
					sms.sendTextMessage(mnumber, null, getString(R.string.smsmessage)
							+ callDayTime, null, null);
					Toast t = Toast.makeText(getApplicationContext(),
							"Sms sent", Toast.LENGTH_SHORT);
					t.show();
				}
			} catch (NumberFormatException nfe) {
				System.out.println("Could not parse " + nfe);
			}
			stopSelf();

		}
	}
}
