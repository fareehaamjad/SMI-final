package com.example.securemobileidentity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.NotificationCompat;
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

				if (Constants.isAppRunning)
				{
					try 
					{
						ProcessMessage.addMsg(mesg, null, null);
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}

					Toast.makeText(context, "message received", Toast.LENGTH_SHORT).show();
				}
				else
				{
					//notify using notification thing
					
					//check what the message is about
					String[] split = mesg.message.split(Constants.separator);
					int index = 0;

					String header = split[index++];
					
					Constants.msgRec = mesg;
					if (header.equals(Constants.HEADER_METADATA))
					{
						//showNotificationHandshake(context);
						saveInDB(context, mesg.number, mesg.message);
					}
					else if (header.equals(Constants.HEADER_REPLY_METADATA))
					{
						saveInDB(context, mesg.number, mesg.message);
					}
					else if (header.equals(Constants.HEADER_VERIFY_META))
					{
						saveInDB(context, mesg.number, mesg.message);
					}
					else
					{
						showNotificationMessage(context); 
					}
					
					
					
				}

				Log.i("sms r" , "msg received");
				Log.i("sms r" , "is app running " + Constants.isAppRunning);

				abortBroadcast();
			}


		}



	}

	private void saveInDB(Context con, String phNo, String msg) 
	{
		DatabaseHandler db = new DatabaseHandler(con);
		
		Log.i("sms r", "Message saved in the temp db");
		db.addNewTempMsg(phNo, msg);
	}

	private void showNotificationHandshake(Context context) 
	{
		Intent mainIntent = new Intent(context, MainActivity.class); 
		mainIntent.putExtra("what", "handshake");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		
		
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("New handshake request")
		.setContentText("You have a new handshake request. Click to find out more.");
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());

	} 
	
	private void showNotificationMessage(Context context) 
	{
		Intent mainIntent = new Intent(context, MainActivity.class); 
		mainIntent.putExtra("what", "message");
		PendingIntent contentIntent = PendingIntent.getActivity(context, 0,
				mainIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		
		
		NotificationCompat.Builder mBuilder =
				new NotificationCompat.Builder(context)
		.setSmallIcon(R.drawable.ic_launcher)
		.setContentTitle("New message")
		.setContentText("You have a new message from your friend.");
		mBuilder.setContentIntent(contentIntent);
		mBuilder.setDefaults(Notification.DEFAULT_SOUND);
		mBuilder.setAutoCancel(true);
		NotificationManager mNotificationManager =
				(NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.notify(1, mBuilder.build());

	}



}

