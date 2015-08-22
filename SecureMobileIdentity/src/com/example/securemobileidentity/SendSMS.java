package com.example.securemobileidentity;

import java.util.ArrayList;

import android.content.Context;
import android.telephony.SmsManager;
import android.util.Log;
import android.widget.Toast;

public class SendSMS 
{

	static String className = "SendSMS";

	public static void sendSMSMessage(String msg, Context context, final String phoneNo) 
	{
		Toast.makeText(context, "sending msg", Toast.LENGTH_SHORT).show();
		Log.i(className , "sendSMSMessage: Message: "+ msg + ": "+phoneNo);
		//Log.i(className , tempPhoneNo);

		SmsManager smsManager = SmsManager.getDefault();
		ArrayList<String> parts = smsManager.divideMessage(msg); 
		smsManager.sendMultipartTextMessage(phoneNo, null, parts, null, null);
	}


	

}