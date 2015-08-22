package com.example.securemobileidentity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage;
import android.util.Log;
import android.widget.Toast;

public class SMSReceiver extends BroadcastReceiver
{
	static String className = "SMSReceiver";
	static final String ACTION = "android.intent.action.DATA_SMS_RECEIVED"; 
	//static final String ACTION = "android.provider.Telephony.SMS_RECEIVED";(tried this too, but failed) 


	@Override
	public void onReceive(Context context, Intent intent)
	{
		//---get the SMS message passed in---
		Bundle bundle = intent.getExtras();
		SmsMessage[] msgs = null;
		String msg = "";
		String number = "";

		if (bundle != null)
		{
			//---retrieve the SMS message received---
			Object[] pdus = (Object[]) bundle.get("pdus");
			msgs = new SmsMessage[pdus.length];

			for (int i=0; i<msgs.length; i++){
				msgs[i] = SmsMessage.createFromPdu((byte[])pdus[i]);

				msg += msgs[i].getMessageBody().toString();
			}
			number =  msgs[0].getOriginatingAddress();

			Log.i(className, "message received: "+ msg);

			SharedPreferences prefs = Constants.getDefaultSharedPreferences(context);
			final String SERVER_NUMBER = prefs.getString(Constants.pref_server_no, "00");

			Log.i(className, "SERVER_NUMBER: "+ SERVER_NUMBER);
			if (PhoneNumberUtils.compare(number, SERVER_NUMBER))
			{
				Constants.allServerMsgs.add(msg);


				abortBroadcast();
			}
			else if (msg.contains(Constants.separator))
			{
				Message mesg = new Message();
				mesg.number = number;
				mesg.message = msg;

				try {
					ProcessMessage.addMsg(mesg);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Toast.makeText(context, "message received", Toast.LENGTH_SHORT).show();
				abortBroadcast();
			}


		}



	}



}

