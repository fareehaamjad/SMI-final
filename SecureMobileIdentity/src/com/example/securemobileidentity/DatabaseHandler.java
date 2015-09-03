package com.example.securemobileidentity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.telephony.PhoneNumberUtils;
import android.util.Log;

public class DatabaseHandler extends SQLiteOpenHelper
{

	// Database Version
	private static final int DATABASE_VERSION = 1;

	// Database Name
	private static final String DATABASE_NAME = "androidCrypto";

	// Table Names
	private static final String TABLE_msg = "msgs";
	private static final String TABLE_challange = "challange";
	private static final String TABLE_temp_key = "tempKeys";
	//private static final String TABLE_bt_challange = "btChallange";
	//private static final String TABLE_bt_temp_key = "btKeysTemp";
	private static final String TABLE_keys = "Keys";
	private static final String TABLE_temp_msgs = "tempMsgs";


	// Contacts Table Columns names
	private static final String KEY_ID = "id";
	private static final String KEY_PH_NO = "phone_number";
	private static final String KEY_message = "message";
	private static final String KEY_verify = "verify_metadata_nonce";
	private static final String KEY_public_key = "public_key";
	private static final String KEY_TIME = "time";
	private static final String KEY_USERTYPE = "usertype";
	private static final String KEY_READ = "isread";
	private static final String KEY_last_update = "last_updated";
	private static final String KEY_score = "score";
	//should update: if you started the key exchange
	//then this is set to 1 else 0
	private static final String KEY_shouldUpdate = "should_update";
	private static final String KEY_exchangeKeysTrying = "exchangeKeysTrying";

	public DatabaseHandler(Context context) 
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) 
	{
		String CREATE_STRORE_MESSAGES_TABLE = "CREATE TABLE " + TABLE_msg + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_PH_NO + " TEXT,"
				+ KEY_message + " TEXT," 
				+ KEY_TIME + " TEXT,"
				+ KEY_USERTYPE + " TEXT,"
				+ KEY_READ + " TEXT" +")";
		db.execSQL(CREATE_STRORE_MESSAGES_TABLE);

		String CREATE_STORE_CHALLANGE_TABLE = "CREATE TABLE " + TABLE_temp_key + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_PH_NO + " TEXT,"
				+ KEY_public_key + " TEXT" + ")";
		db.execSQL(CREATE_STORE_CHALLANGE_TABLE);

		String CREATE_STORE_KEY_TABLE = "CREATE TABLE " + TABLE_challange + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_PH_NO + " TEXT,"
				+ KEY_verify + " TEXT" + ")";
		db.execSQL(CREATE_STORE_KEY_TABLE);

		/*String CREATE_STORE_BT_CHALLANGE_TABLE = "CREATE TABLE " + TABLE_bt_challange + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_bt_device_name + " TEXT,"
				+ KEY_verify + " TEXT" + ")";
		db.execSQL(CREATE_STORE_BT_CHALLANGE_TABLE);*/

		/*String CREATE_STORE_BT_KEY_TABLE = "CREATE TABLE " + TABLE_bt_temp_key + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_bt_device_name + " TEXT,"
				+ KEY_public_key + " TEXT" + ")";
		db.execSQL(CREATE_STORE_BT_KEY_TABLE);*/

		String CREATE_STORE_KEYS_TABLE = "CREATE TABLE " + TABLE_keys + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_PH_NO + " TEXT,"
				+ KEY_public_key + " TEXT,"
				+ KEY_last_update + " TEXT,"
				+ KEY_score + " TEXT,"
				+ KEY_shouldUpdate +" TEXT,"
				+ KEY_exchangeKeysTrying +" TEXT"+ ")";
		db.execSQL(CREATE_STORE_KEYS_TABLE);


		String CREATE_TEMP_MSG_TABLE = "CREATE TABLE " + TABLE_temp_msgs + "("
				+ KEY_ID + " INTEGER PRIMARY KEY," 
				+ KEY_PH_NO + " TEXT,"
				+ KEY_message +" TEXT" + ")";
		db.execSQL(CREATE_TEMP_MSG_TABLE);


	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) 
	{
		// Drop older table if existed
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_msg);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_challange);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_temp_key);
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_bt_challange);
		//db.execSQL("DROP TABLE IF EXISTS " + TABLE_bt_temp_key);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_keys);
		db.execSQL("DROP TABLE IF EXISTS " + TABLE_temp_msgs);

		// Create tables again
		onCreate(db);
	}

	// Adding new message
	public void addNewMsg(Message msg) 
	{
		Log.i("db", "db inserting: " + msg.number + "  " + msg.message + "  " + msg.time.toString() + "   " + msg.getUserTypeStr() + "  " + msg.isRead);


		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_PH_NO, msg.number); // Contact Phone
		values.put(KEY_message, msg.message); // message
		values.put(KEY_TIME, Constants.SIMPLE_DATE_FORMAT.format(msg.time)); //time of the message
		values.put(KEY_USERTYPE, msg.getUserTypeStr()); //type
		values.put(KEY_READ, msg.getReadStatus());

		// Inserting Row
		if (db.insert(TABLE_msg, null, values) == -1)
		{
			Log.i("db", "db didnt insert the message");
		}
		db.close(); // Closing database connection
	}

	// Adding new temp message for our service
	public void addNewTempMsg(String phNo, String msg) 
	{
		Log.i("db", "db inserting: " +phNo + "  " + msg);


		SQLiteDatabase db = this.getWritableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_PH_NO, phNo); // Contact Phone
		values.put(KEY_message, msg); // message


		// Inserting Row
		if (db.insert(TABLE_temp_msgs, null, values) == -1)
		{
			Log.i("db", "db didnt insert the message");
		}
		db.close(); // Closing database connection
	}




	//update read status of a message
	public void updateStatusOfMsg(Message msg)
	{
		Log.i("db", "updating message status");

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_READ, msg.getReadStatus());

		int ret = db.update(TABLE_msg, values, 
				KEY_PH_NO + " = ? AND " + KEY_message + " = ? AND " + KEY_TIME + " = ? AND " + KEY_USERTYPE +" = ?" ,
				new String[]{msg.number, msg.message , Constants.SIMPLE_DATE_FORMAT.format(msg.time), msg.getUserTypeStr()});

		Log.i("db", "no of rows updated are: " + ret);

	}

	//update keys
	public void updateTableKeys(TableKeys key)
	{
		Log.i("db", "updating table keys");

		SQLiteDatabase db = this.getReadableDatabase();

		ContentValues values = new ContentValues();
		values.put(KEY_last_update, key.getlastupdate());
		values.put(KEY_score, key.getscore());
		values.put(KEY_exchangeKeysTrying, key.getexchangeKeysTrying());

		int ret = db.update(TABLE_keys, values, 
				KEY_ID + " = ? AND " + KEY_PH_NO + " = ?",
				new String[]{Integer.toString(key.getID()), key.getPhNo()});

		Log.i("db", "no of rows updated are: " + ret); 
	}

	// Adding new keys[permanent]
	public void addNewKey(TableKeys key) 
	{
		long id = -1;
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_keys, new String[] { KEY_public_key, KEY_PH_NO }, null, null, null, null, null, null);

		boolean present = false;
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), key.getPhNo()))
				{
					Log.i("db", "Public key found");

					if (cursor.getString(cursor.getColumnIndex(KEY_public_key)).equalsIgnoreCase(key.getPublicKey()))
					{
						//do nothing
					}
					else
					{
						Log.i("db", "Public key found but is different. Updating records");


						ContentValues values = new ContentValues();
						values.put(KEY_public_key, key.getPublicKey() ); // Key
						values.put(KEY_last_update, key.getlastupdate()); //last updated
						values.put(KEY_score, key.getscore());//score
						values.put(KEY_shouldUpdate, key.getShouldUpdate());
						values.put(KEY_exchangeKeysTrying, key.getexchangeKeysTrying());

						// updating row
						db.update(TABLE_keys, values, KEY_PH_NO + " = ?",
								new String[] { key.getPhNo() });


						db.close(); // Closing database connection

						for (TableKeys tk: Constants.tableKeys)
						{
							if (PhoneNumberUtils.compare(tk.getPhNo(), key.getPhNo() ))
							{
								Log.i("db", "Public key found but is different. Updating array");
								tk.updatePublicKey(key.getPublicKey());
							}
						}

					}
					present = true;
					break;

				}
			} while(cursor.moveToNext());


			if (!present)
			{
				Log.i("db", "No public key found; inserting the values in the database");
				ContentValues values = new ContentValues();
				values.put(KEY_PH_NO, key.getPhNo()); // Contact Phone
				values.put(KEY_public_key, key.getPublicKey() ); // Key
				values.put(KEY_last_update, key.getlastupdate()); //last updated
				values.put(KEY_score, key.getscore());//score
				values.put(KEY_shouldUpdate, key.getShouldUpdate());
				values.put(KEY_exchangeKeysTrying, key.getexchangeKeysTrying());


				// Inserting Row
				id = db.insert(TABLE_keys, null, values);
				db.close(); // Closing database connection


				if (key.getID() != -1)
				{
					key.updateID(id);

					Constants.tableKeys.add(key);
				}
			}

		}
		else
		{
			Log.i("db", "No public key found; inserting the values in the database");
			ContentValues values = new ContentValues();
			values.put(KEY_PH_NO, key.getPhNo()); // Contact Phone
			values.put(KEY_public_key, key.getPublicKey()); // Key
			values.put(KEY_last_update, key.getlastupdate()); //last updated
			values.put(KEY_score, key.getscore());//score
			values.put(KEY_shouldUpdate, key.getShouldUpdate());
			values.put(KEY_exchangeKeysTrying, key.getexchangeKeysTrying());

			// Inserting Row
			id = db.insert(TABLE_keys, null, values);
			db.close(); // Closing database connection

			if (key.getID() != -1)
			{
				key.updateID(id);

				Constants.tableKeys.add(key);
			}

		}



	}


	/*//get public Key[permanent]
	public String getKey(String no)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_keys, new String[] { KEY_public_key, KEY_PH_NO },null, null, null, null, null, null);

		String ret = "null";
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), no))
				{
					Log.i("db", "public key found");
					// return challenge
					Log.i("db", cursor.getString(cursor.getColumnIndex(KEY_public_key)));
					db.close(); // Closing database connection
					ret = cursor.getString(cursor.getColumnIndex(KEY_public_key));

					break;
				}


			}while (cursor.moveToNext());

		}
		if (ret.equals("null"))
		{
			Log.i("db", "No public key found");
		}

		db.close(); // Closing database connection
		return ret;


	}*/

	// Getting All Contacts
	public void getAllContacts() 
	{
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_msg;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				String number = cursor.getString(1);

				if (!Constants.isContactRead(number))
				{
					Contact c = new Contact(number, number, true);
					Constants.allContacts.add(c);
				}

			} while (cursor.moveToNext());
		}

		db.close(); // Closing database connection

	}

	// Getting All Keys
	public void getAllKeys() throws ParseException 
	{
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_keys;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				String number = cursor.getString(1);

				if (!Constants.isContactRead(number))
				{
					TableKeys k = new TableKeys(cursor.getString(0), cursor.getString(1),
							cursor.getString(2), cursor.getString(3), cursor.getString(4), 
							cursor.getString(5), cursor.getString(6));
					Constants.tableKeys.add(k);
				}

			} while (cursor.moveToNext());
		}

		db.close(); // Closing database connection

	}

	public void getAllKeysService() throws ParseException 
	{
		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_keys;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) {
			do {
				String number = cursor.getString(1);

				if (!Constants.isContactRead(number))
				{
					TableKeys k = new TableKeys(cursor.getString(0), cursor.getString(1), 
							cursor.getString(2), cursor.getString(3), cursor.getString(4),
							cursor.getString(5), cursor.getString(6));
					
					KeyExchangeService.participants.add( k );
				}

			} while (cursor.moveToNext());
		}

		db.close(); // Closing database connection

	}


	// Getting All Messages
	public void getAllMessages(String number) throws ParseException 
	{

		Log.i("db", "Getting all messages for: " + number);

		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_msg;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		int i = 0;
		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do 
			{
				Log.i("db", "Number db is: " + (cursor.getString(cursor.getColumnIndex(KEY_PH_NO))) + " number is: " + number);


				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), number))
				{
					Message msg = new Message();
					msg.number = cursor.getString(1);
					msg.message = cursor.getString(2);
					msg.time = Constants.SIMPLE_DATE_FORMAT.parse(cursor.getString(3));
					msg.setType(cursor.getString(4));
					msg.setReadStatus(cursor.getString(5));

					Constants.allMsgs.add(msg);

					i++;
				}
				// Adding contact to list

			} while (cursor.moveToNext());
		}

		Log.i("db", "the count is " + i);

		Collections.sort(Constants.allMsgs);
		db.close(); // Closing database connection

	}


	// Getting All temp Messages and delete them
	public void getAllTempMessages(ArrayList<Message> allMsgs) throws ParseException 
	{
		Log.i("db", "Getting all messages for service ");

		// Select All Query
		String selectQuery = "SELECT  * FROM " + TABLE_temp_msgs;

		SQLiteDatabase db = this.getWritableDatabase();
		Cursor cursor = db.rawQuery(selectQuery, null);

		// looping through all rows and adding to list
		if (cursor.moveToFirst()) 
		{
			do 
			{
				Message msg = new Message();
				msg.number = cursor.getString(1);
				msg.message = cursor.getString(2);
				allMsgs.add(msg);
			} while (cursor.moveToNext());
		}

		db.delete(TABLE_temp_msgs, null, null);
		Log.i("db", "deleted all rows");
		
		db.close(); // Closing database connection

	}





	//Adding new challenge
	public void addNewChallange(String number, String challenge)
	{
		Log.i("db", "storing challenge for: " + number);

		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_challange, new String[] { KEY_verify, KEY_PH_NO }, null, null, null, null, null, null);

		boolean present = false;
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), number))
				{
					Log.i("db", "found challenge--updating");

					ContentValues values = new ContentValues();
					values.put(KEY_verify, challenge);

					// updating row
					db.update(TABLE_challange, values, KEY_PH_NO + " = ?",
							new String[] { cursor.getString(cursor.getColumnIndex(KEY_PH_NO)) });


					db.close(); // Closing database connection

					present = true;

					break;
				}
			}while(cursor.moveToNext());

			if (! present)
			{
				Log.i("db", "didn't find challenge--adding");

				ContentValues values = new ContentValues();
				values.put(KEY_PH_NO, number); // Contact Phone
				values.put(KEY_verify, challenge); // Challenge

				// Inserting Row
				db.insert(TABLE_challange, null, values);
				db.close(); // Closing database connection
			}

		}
		else
		{
			ContentValues values = new ContentValues();
			values.put(KEY_PH_NO, number); // Contact Phone
			values.put(KEY_verify, challenge); // Challenge

			// Inserting Row
			db.insert(TABLE_challange, null, values);
			db.close(); // Closing database connection
		}


	}

	public void deleteChallange(String number) 
	{
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.query(TABLE_challange, new String[] { KEY_PH_NO }, null, null, null, null, null, null);

		boolean present = false;
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), number))
				{
					db.delete(TABLE_challange, KEY_PH_NO + " = ?",
							new String[] { cursor.getString(cursor.getColumnIndex(KEY_PH_NO)) });

					db.close();

					break;
				}
			}while (cursor.moveToNext());
		}

	}

	//getting the challenge associated with the number
	public String getChallenge(String no)
	{
		SQLiteDatabase db = this.getReadableDatabase();

		Log.i("db", "finding challenge for: " + no);

		Cursor cursor = db.query(TABLE_challange, new String[] { KEY_verify, KEY_PH_NO }, null, null, null, null, null, null);

		String ret = "null";
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), no))
				{
					Log.i("db", "number found");

					// return challenge
					Log.i("db", cursor.getString(cursor.getColumnIndex(KEY_verify)));
					ret = cursor.getString(0);

					db.close(); // Closing database connection
					break;
				}
			}while (cursor.moveToNext());



		}
		//Log.i("db", "No number found");
		db.close(); // Closing database connection
		return ret;




	}

	public void AddNewTempKeys(String number, String publicKey)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.query(TABLE_temp_key, new String[] { KEY_public_key, KEY_PH_NO }, null, null, null, null, null, null);

		boolean present = false;
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), number))
				{

					Log.i("db", "Public key found but is different. Updating records");


					ContentValues values = new ContentValues();
					values.put(KEY_public_key, publicKey);

					// updating row
					db.update(TABLE_temp_key, values, KEY_PH_NO + " = ?",
							new String[] { number });


					db.close(); // Closing database connection


					present = true;
					break;

				}
			} while(cursor.moveToNext());


			if (!present)
			{
				ContentValues values = new ContentValues();
				values.put(KEY_PH_NO, number); // Contact Phone
				values.put(KEY_public_key, publicKey); // pub key

				// Inserting Row
				db.insert(TABLE_temp_key, null, values);
				db.close(); // Closing database connection
			}

		}
		else
		{
			ContentValues values = new ContentValues();
			values.put(KEY_PH_NO, number); // Contact Phone
			values.put(KEY_public_key, publicKey); // pub key

			// Inserting Row
			db.insert(TABLE_temp_key, null, values);
			db.close(); // Closing database connection
		}


	}



	public String getTempKey(String no)
	{
		Log.i("db", "finding temp public key found");
		SQLiteDatabase db = this.getReadableDatabase();

		Cursor cursor = db.query(TABLE_temp_key, new String[] { KEY_public_key, KEY_PH_NO },null, null, null, null, null, null);
		String ret = "null";
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), no))
				{
					Log.i("db", "public key found");
					// return challenge
					Log.i("db", cursor.getString(cursor.getColumnIndex(KEY_public_key)));
					db.close(); // Closing database connection
					ret =  cursor.getString(cursor.getColumnIndex(KEY_public_key));
					break;
				}
			}while (cursor.moveToNext());
		}

		//Log.i("db", "No public key found");
		db.close(); // Closing database connection
		return ret;

	}

	public void deleteTempKey(String number)
	{
		SQLiteDatabase db = this.getWritableDatabase();

		Cursor cursor = db.query(TABLE_temp_key, new String[] { KEY_PH_NO }, null, null, null, null, null, null);

		boolean present = false;
		if (cursor.moveToFirst())
		{
			do
			{
				if (PhoneNumberUtils.compare(cursor.getString(cursor.getColumnIndex(KEY_PH_NO)), number))
				{
					db.delete(TABLE_temp_key, KEY_PH_NO + " = ?",
							new String[] { cursor.getString(cursor.getColumnIndex(KEY_PH_NO)) });

					db.close();

					break;
				}
			}while (cursor.moveToNext());
		}

	}




}
