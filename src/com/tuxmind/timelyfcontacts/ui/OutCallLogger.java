package com.tuxmind.timelyfcontacts.ui;

import com.tuxmind.timelyfcontacts.util.PhoneCallService;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;

public class OutCallLogger extends BroadcastReceiver {
	private static boolean noCallListenerYet = true;
	static final String LOG_TAG = "OutboundCallReciever";
	String number;
	int count = 0;

	@Override
	public void onReceive(final Context context, Intent intent) {
		number = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
		if (noCallListenerYet) {
			final TelephonyManager tm = (TelephonyManager) context
					.getSystemService(Context.TELEPHONY_SERVICE);
			tm.listen(new PhoneStateListener() {
				@Override
				public void onCallStateChanged(int state, String incomingNumber) {
					switch (state) {
					case TelephonyManager.CALL_STATE_RINGING:
						Log.d(LOG_TAG, "RINGING");
						break;
					case TelephonyManager.CALL_STATE_OFFHOOK:
						Log.d(LOG_TAG, "OFFHOOK");
						break;
					case TelephonyManager.CALL_STATE_IDLE:
						Log.d(LOG_TAG, "IDLE");

						break;
					default:
						Log.d(LOG_TAG, "Default: " + state);
						break;
					}
					if (ContactsListActivity.automaticPhone == true
							&& TelephonyManager.CALL_STATE_OFFHOOK == state) {
						count++;
						if (count > 5) {
							count = 1;
						}
						context.startService(new Intent(context,
								PhoneCallService.class).putExtra("contatore",
								count));
						System.out.println("Number logger " + count);
					}
				}

			}, PhoneStateListener.LISTEN_CALL_STATE);

			noCallListenerYet = false;
		}
	}

}