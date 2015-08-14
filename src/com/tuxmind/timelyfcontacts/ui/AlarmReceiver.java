package com.tuxmind.timelyfcontacts.ui;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;

public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        // TODO Auto-generated method stub
    	System.out.println("AlarmReceiver");

    	Intent mIntent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:"
				+ intent.getExtras().getString("mPhoneNumber")));
    	mIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(mIntent);
       
		System.out.println("phoneNumberReceiver: " + intent.getStringExtra("mPhoneNumber"));
		

    }

}
