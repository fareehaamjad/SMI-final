package com.example.securemobileidentity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.http.ParseException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;


public class MainActivity extends Activity 
{

	String className = "MainActivity";
	// List view
	private ListView lv;

	// Search EditText
	EditText inputSearch;

	public static CustomListViewAdapter listAdapter = null;

	public static SharedPreferences s;

	static Context context;

	LinearLayout ll_main, ll_progressbar;

	WaitForSMS mTask = null;
	LoadContacts mTask1 = null;

	private Handler mHandler;
	
	private DatabaseHandler dbHandler;
	
	private EncryptionManager encryptionManager;

	String what = "";

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		Constants.isAppRunning = true;

		Constants.root 			= Environment.getExternalStorageDirectory().getAbsolutePath();

		Constants.allServerMsgs = new ArrayList<String>();
		mHandler = new Handler();

		context = this;
		
		dbHandler = new DatabaseHandler(context);
		try
		{
			what = getIntent().getStringExtra("what");
			Log.i("test", "pre post what is " + what);
		}
		catch(Exception e)
		{
			//ignore
		}
		
		//LocationTracker.startLocUpdates();
		
		Constants.viberate = (Vibrator) this.context.getSystemService(Context.VIBRATOR_SERVICE);


		s= Constants.getDefaultSharedPreferences(context);
		
		try 
		{
			encryptionManager = new EncryptionManager( MainActivity.s , "testpassword");

		} catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		if (s.getString(Constants.pref_phone_no, null) == null)
		{
			Log.i("phone number", "getting phone number from user: should only happen once!!");
			getPhoneNo(this);
		}
		else
		{
			prepareList();
		}

		Constants.createDirectories();

		// Crash log
		CrashReport errReporter = new CrashReport();
		errReporter.Init(this);
		errReporter.CheckErrorAndSendMail(this);


	}

	@Override
	public void onResume()
	{
		Constants.currentContext = this;
		Constants.isContactListOpen = true;

		if (listAdapter != null)
		{
			listAdapter.notifyDataSetChanged();
		}


		super.onResume();
	}

	@Override
	public void onPause()
	{
		Constants.isContactListOpen = false;

		super.onPause();
	}

	@Override
	public void onDestroy()
	{
		Constants.isAppRunning = false;

		super.onDestroy();
	}

	private Runnable smsFailed = new Runnable() 
	{
		public void run() 
		{
			/*Intent i = new Intent(context, SMSFail.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			getBaseContext().startActivity(i);
			((Activity) context).finish();*/

			((Activity) context).setContentView(R.layout.sms_fail);


		}
	};

	private Runnable smsReceived = new Runnable() 
	{
		public void run() 
		{
			/*Intent i = new Intent(context, SMSFail.class);
			i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
			getBaseContext().startActivity(i);
			((Activity) context).finish();*/

			((Activity) context).setContentView(R.layout.activity_main);
			prepareList();


		}
	};
	


	void getPhoneNo(final Context context) 
	{

		if (s.getString(Constants.pref_server_no, null) == null)
		{
			final EditText input = new EditText(this);
			new AlertDialog.Builder(context)
			.setTitle("Server Number")
			.setMessage("Enter server number:")
			.setView(input)
			.setPositiveButton("Ok", new DialogInterface.OnClickListener()
			{
				public void onClick(DialogInterface dialog, int whichButton)
				{
					Editable value = input.getText(); 
					if (value.toString().equals("") || value.toString() == null)
					{
						Toast.makeText(context, "Please enter a server number", Toast.LENGTH_SHORT).show();
						getPhoneNo(context);
					}
					else
					{
						String server_number = value.toString();

						SharedPreferences.Editor e = MainActivity.s.edit();
						e.putString(Constants.pref_server_no, server_number);
						e.apply();
						while (!e.commit());

						//SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
						Log.i(className, s.getString(Constants.pref_server_no, "00"));
						SendSMS.sendSMSMessage(Constants.get_phone_no, context, server_number);
						((Activity) context).setContentView(R.layout.get_phone_number);
						getServerNoWait();

						dialog.cancel();

					}
				}
			}).show();


		}
		else
		{
			String server_number = s.getString(Constants.pref_server_no, null);

			SendSMS.sendSMSMessage(Constants.get_phone_no, context, server_number);
			getServerNoWait();
			((Activity) context).setContentView(R.layout.get_phone_number);
		}


	}

	protected void prepareList() 
	{

		if ( mTask1 != null ) 
		{
			mTask1.cancel(true);
		}

		mTask1 = (LoadContacts) new LoadContacts().execute();

		/*lv = (ListView) findViewById(R.id.lv_list_view);
		inputSearch = (EditText) findViewById(R.id.et_inputSearch);

		yfyf
		allContacts = new ArrayList<Contact>();
		readContacts();

		// Adding items to listview
		listAdapter = new CustomListViewAdapter(this, R.layout.contact_row, allContacts);
		lv.setAdapter(listAdapter); 


		lv.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> adapter, View v, int position,
					long arg3) 
			{
				Constants.contactSelected = (Contact)adapter.getItemAtPosition(position); 
				Log.d("selected item", Constants.contactSelected.getName());

				Intent intent = new Intent(getBaseContext(), DisplayOptions.class);
				startActivity(intent);
			}
		});


		inputSearch.addTextChangedListener(new TextWatcher() {

			@Override
			public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) 
			{
				// When user changed the Text
				//MainActivity.this.listAdapter.getFilter().filter(cs);   
				Log.d("*** Search value changed: " , cs.toString());
				listAdapter.doFilter(lv, cs.toString(), allContacts);
			}

			@Override
			public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
					int arg3) {
				// TODO Auto-generated method stub

			}

			@Override
			public void afterTextChanged(Editable s) 
			{
				Log.d("*** Search value changedjfjfgjfgjfgjfgj: " , s.toString());
				listAdapter.doFilter(lv, s.toString(), allContacts);                  
			}


		});*/
	}

	private void getServerNoWait() 
	{
		if ( mTask != null ) 
		{
			mTask.cancel(true);
		}

		mTask = (WaitForSMS) new WaitForSMS().execute();



	}

	private void readContacts() 
	{
		ContentResolver cr = getContentResolver();
		Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,null, null, null, ContactsContract.Contacts.DISPLAY_NAME + " ASC");

		if (cur.getCount() > 0)
		{
			while (cur.moveToNext())
			{
				String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
				String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

				// Using the contact ID now we will get contact phone number
				Cursor cursorPhone = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
						new String[]{ContactsContract.CommonDataKinds.Phone.NUMBER},

						ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ? AND " +
								ContactsContract.CommonDataKinds.Phone.TYPE + " = " +
								ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE,

								new String[]{id},
								null);

				String contactNumber = "000";
				if (cursorPhone.moveToFirst()) {
					contactNumber = cursorPhone.getString(cursorPhone.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
				}
				//Log.i("number", name +" : " + contactNumber);
				cursorPhone.close();


				Contact contact = new Contact(name, contactNumber, true);
				Constants.allContacts.add(contact);                



			}
		}
	}


	class WaitForSMS extends AsyncTask<String, String, String>
	{
		long timeStart;
		boolean smsFail = false;

		String msg;
		@Override
		protected void onPreExecute() 
		{    
			timeStart = System.nanoTime();

			super.onPreExecute();

		}


		@Override
		protected String doInBackground(String... arg0) 
		{
			try 
			{

				//while (!(Constants.userDetails && Constants.systemDetails) )
				while (!(Constants.phone_no_response) )
				{
					//android.util.Log.i("time", System.nanoTime() + "");
					//android.util.Log.i("time", timeStart + "");

					long time = System.nanoTime() - timeStart;
					//time = Math.abs(time);
					long t = (2 * 60 * 1000000000L);
					//android.util.Log.i("t", t + "");
					//android.util.Log.i("time", time + "");
					if (time >= t)
					{
						//android.util.Log.i("time", "difference is greater");
						smsFail = true;
						break;
					}

					for (String msg : Constants.allServerMsgs)
					{
						String _msg = msg;
						String sp[] = msg.split(Constants.keywordsSeparator, 2);

						if (sp[0].equalsIgnoreCase(Constants.get_phone_no))
						{
							Log.i(className,"phone number retrieved from doInBackground Function...");

							Constants.allServerMsgs.remove(_msg);
							Log.i(className,"Message removed");

							TelephonyManager mngr = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE); 
							String imei = mngr.getDeviceId();


							SharedPreferences.Editor e = MainActivity.s.edit();
							e.putString(Constants.pref_phone_no, sp[1]);
							e.putString(Constants.pref_KEY_imei, imei);

							Log.i("Phone No",  sp[1]);
							Log.i("IMEI number",  imei);
							while (!e.commit());
							//finish();

							Constants.phone_no_response = true;

							//break;
							//return null;

						}
						/*else if (sp[0].equalsIgnoreCase(Constants.getInfoUserKeyword)) {
							Constants.allServerMsgs.remove(_msg);
							Log.i(className, "Message removed");

							Utils.getUserDetails(sp[1]);

						}*/

					}
					Thread.sleep(1000);
				}

			} catch (InterruptedException e) 
			{
				e.printStackTrace();
			}



			// TODO Auto-generated method stub
			return null;
		}



		@Override
		protected void onPostExecute(String result) 
		{     
			String funcName = "onPostExecute";
			Log.i(className, funcName);

			if (smsFail)
			{
				mHandler.post(smsFailed);
			}
			else
			{
				mHandler.post(smsReceived);
			}




			/*Constants.myExistingOffer = msg;
			MainActivity.myOffers.setText(Constants.myExistingOffer);*/



			// HIDE THE SPINNER AFTER LOADING FEEDS
			//headerProgress.setVisibility(View.GONE);
		}


	}



	class LoadContacts extends AsyncTask<String, String, String>
	{
		@Override
		protected void onPreExecute() 
		{    
			ll_main = (LinearLayout) findViewById(R.id.ll_main);
			ll_progressbar = (LinearLayout) findViewById(R.id.linlaHeaderProgress);

			ll_progressbar.setVisibility(View.VISIBLE);
			ll_main.setVisibility(View.GONE);

			lv = (ListView) findViewById(R.id.lv_list_view);
			inputSearch = (EditText) findViewById(R.id.et_inputSearch);

			
			super.onPreExecute();

		}


		@Override
		protected String doInBackground(String... arg0) 
		{
			try 
			{

				readContacts();
				dbHandler.getAllContacts();
				dbHandler.getAllKeys();
				
			} catch (Exception e) 
			{
				e.printStackTrace();
			}



			// TODO Auto-generated method stub
			return null;
		}



		@Override
		protected void onPostExecute(String result) 
		{     
			String funcName = "onPostExecute";
			Log.i(className, funcName);

			ll_progressbar.setVisibility(View.GONE);
			ll_main.setVisibility(View.VISIBLE);

			// Adding items to listview
			listAdapter = new CustomListViewAdapter(context, R.layout.contact_row, Constants.allContacts, dbHandler);
			lv.setAdapter(listAdapter); 

			lv.setOnItemClickListener(new OnItemClickListener()
			{
				@Override
				public void onItemClick(AdapterView<?> adapter, View v, int position,
						long arg3) 
				{
					Constants.contactSelected = (Contact)adapter.getItemAtPosition(position); 
					Log.d("selected item", Constants.contactSelected.getName());

					Intent intent = new Intent(getBaseContext(), DisplayOptions.class);
					startActivity(intent);
				}
			});


			inputSearch.addTextChangedListener(new TextWatcher() {

				@Override
				public void onTextChanged(CharSequence cs, int arg1, int arg2, int arg3) 
				{
					// When user changed the Text
					//MainActivity.this.listAdapter.getFilter().filter(cs);   
					Log.d("*** Search value changed: " , cs.toString());
					listAdapter.doFilter(lv, cs.toString(), Constants.allContacts);
				}

				@Override
				public void beforeTextChanged(CharSequence arg0, int arg1, int arg2,
						int arg3) {
					// TODO Auto-generated method stub

				}

				@Override
				public void afterTextChanged(Editable s) 
				{
					Log.d("*** Search value changedjfjfgjfgjfgjfgj: " , s.toString());
					listAdapter.doFilter(lv, s.toString(), Constants.allContacts);                  
				}


			});

			try
			{


				Log.i("test", "what is " + what);

				if (what.equals("handshake"))
				{

					Contact c = Constants.findContact(Constants.msgRec.number);
					Constants.contactsVisited.remove(c);

					new AlertDialog.Builder(Constants.currentContext)
					.setTitle("Handshake Request")
					.setMessage((c.name == null || c.name.equals("") ? c.number : c.name) + " would like to exchange keys with you. Would you like to continue?")
					.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{ 
							Constants.exchangeKeysTrying = true;
							Log.i("metadata", "Got meta-data    " +Constants.msgRec.message);

							//send a reply: new metadata, new nonce, public key, public modulus, signed metadata + nonce of sender
							try 
							{
								ProcessMessage.replyMetadata(Constants.msgRec.message, context, encryptionManager, dbHandler);
							} catch (Exception e) 
							{
								// TODO Auto-generated catch block
								e.printStackTrace();
							}

							Constants.timeStart = System.nanoTime();
							Constants.contactSelected = Constants.findContact(Constants.msgRec.number);
							Intent intent = new Intent(MainActivity.context, SMSActivity.class);
							intent.putExtra("method","sms");
							MainActivity.context.startActivity(intent);
						}
					})
					.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() 
					{
						public void onClick(DialogInterface dialog, int which) 
						{ 
							// do nothing
						}
					})
					.setIcon(android.R.drawable.ic_dialog_alert)
					.show()
					.setCancelable(false);

				}
				else if (what.equals("message"))
				{
					String[] split = Constants.msgRec.message.split(Constants.separator);
					Constants.msgRec.message = split[1];
					Log.i("got encrypted message", Constants.msgRec.message);

					// Vibrate for 500 milliseconds
					Constants.viberate.vibrate(500);

					String publicKey = Constants.getKey(Constants.msgRec.number);

					if (publicKey.equalsIgnoreCase("null"))
					{
						/*1- save message
					2- send handshake request
					3- retrieve message*/
						final Contact c = Constants.findContact(Constants.msgRec.number);
						Constants.contactsVisited.remove(c);

						Constants.unread_unknown.add(Constants.msgRec);

						new AlertDialog.Builder(Constants.currentContext)
						.setTitle("Handshake Request")
						.setMessage((c.name == null || c.name.equals("") ? c.number : c.name) + " has sent you a message. Would you like to start key exchange?")
						.setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{ 
								Constants.exchangeKeysTrying = true;
								Log.i("metadata", "Got meta-data    " +Constants.msgRec.message);

								//send a reply: new metadata, new nonce, public key, public modulus, signed metadata + nonce of sender
								try 
								{
									Constants.contactSelected = c;
									SMSActivity.exchangeKeys(mHandler, context);
								} catch (Exception e) 
								{
									// TODO Auto-generated catch block
									e.printStackTrace();
								}

								Constants.timeStart = System.nanoTime();
								Constants.contactSelected = Constants.findContact(Constants.msgRec.number);
								Intent intent = new Intent(MainActivity.context, SMSActivity.class);
								intent.putExtra("method","sms");
								MainActivity.context.startActivity(intent);
							}
						})
						.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() 
						{
							public void onClick(DialogInterface dialog, int which) 
							{ 
								// do nothing
							}
						})
						.setIcon(android.R.drawable.ic_dialog_alert)
						.show()
						.setCancelable(false);


					}
					else
					{
						Log.i("test", "the key is found. we are in else statement");
						Log.i("test", "decrypting: " + Constants.msgRec.message);
						String message =  encryptionManager.decrypt(Constants.msgRec.message, publicKey);

						Log.i("test", "The message is: " + message);

						Message msgRec = new Message();
						msgRec.message = message;
						msgRec.number = Constants.msgRec.number;
						msgRec.time = new Date();
						msgRec.type = Constants.UserType.OTHER;
						msgRec.isRead = false;

						dbHandler.addNewMsg(msgRec);

						if (Constants.isChatOpen)
						{
							Log.i("test", "chat is open");
							if (PhoneNumberUtils.compare(Constants.contactSelected.number, msgRec.number))
							{
								Log.i("test", "chat is open");
								Constants.allMsgs.add(msgRec);
								Log.i("test", "message is added");
								SMSActivity.adapter.notifyDataSetChanged();

								msgRec.isRead = true;
								dbHandler.updateStatusOfMsg(msgRec);
							}
						}

						if (Constants.isContactListOpen)
						{
							MainActivity.listAdapter.notifyDataSetChanged();
						}
					}
				}
				
				
			}
			catch(Exception e)
			{
				//do nothing!
			}

		}


	}
}
