package com.example.securemobileidentity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.FileHandler;

import android.os.Environment;
/**
 * 
 * 
 * Definition - Logger file use to keep Log info to external SD with the simple method
 */

public class Logger 
{

	public static  FileHandler logger = null;
	private static String filename = "locationdata";
	public static String file = "/SMI/";

	static boolean isExternalStorageAvailable = false;
	static boolean isExternalStorageWriteable = false; 
	static String state = Environment.getExternalStorageState();
	static String root;// = Environment.getExternalStorageDirectory().getAbsolutePath();

	public static void i(String tag, String strWrite)
	{
		android.util.Log.i(tag, strWrite);
		root = Environment.getExternalStorageDirectory().getAbsolutePath();

		try 
		{
			if (isSdReadable())   // isSdReadable()e method is define at bottom of the post
			{
				File dir = new File(root + file);      
				if(!dir.exists() && !dir.isDirectory()) 
				{
					//Log.d("Dir created ", "Dir created ");
					dir.mkdirs();
				}

				File logFile = new File( root + file +filename+".txt");


				//String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				//File myFile = new File(fullPath + File.separator + "/"+filename);

				FileOutputStream fOut = new FileOutputStream(logFile, true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
				String currentDateandTime = sdf.format(new Date());
				
				
				myOutWriter.append(currentDateandTime+ "\t" + tag + "\t"+strWrite + "\n");
				myOutWriter.close();
				fOut.close();
				
				
			}
		}
		catch (Exception e)
		{
			//do your stuff here
		}
	}
	
	public static void e(String tag, String strWrite)
	{
		android.util.Log.e(tag, strWrite);
		root = Environment.getExternalStorageDirectory().getAbsolutePath();

		try 
		{
			if (isSdReadable())   // isSdReadable()e method is define at bottom of the post
			{
				File dir = new File(root + file);      
				if(!dir.exists() && !dir.isDirectory()) 
				{
					//Log.d("Dir created ", "Dir created ");
					dir.mkdirs();
				}

				File logFile = new File( root + file +filename+".txt");


				//String fullPath = Environment.getExternalStorageDirectory().getAbsolutePath();
				//File myFile = new File(fullPath + File.separator + "/"+filename);

				FileOutputStream fOut = new FileOutputStream(logFile, true);
				OutputStreamWriter myOutWriter = new OutputStreamWriter(fOut);
				
				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss ");
				String currentDateandTime = sdf.format(new Date());
				
				
				myOutWriter.append(currentDateandTime+ "\t" + tag + "\t"+strWrite + "\n");
				myOutWriter.close();
				fOut.close();
				
				
			}
		}
		catch (Exception e)
		{
			//do your stuff here
		}
	}
	public static boolean isSdReadable() 
	{

		boolean mExternalStorageAvailable = false;
		try 
		{
			String state = Environment.getExternalStorageState();

			if (Environment.MEDIA_MOUNTED.equals(state))
			{
				// We can read and write the media
				mExternalStorageAvailable = true;
				//Log.i("isSdReadable", "External storage card is readable.");
			}
			else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) 
			{
				// We can only read the media
				//Log.i("isSdReadable", "External storage card is readable.");
				mExternalStorageAvailable = true;
			} 
			else
			{
				// Something else is wrong. It may be one of many other
				// states, but all we need to know is we can neither read nor
				// write
				mExternalStorageAvailable = false;
			}
		} catch (Exception ex) 
		{

		}
		return mExternalStorageAvailable;
	}


}