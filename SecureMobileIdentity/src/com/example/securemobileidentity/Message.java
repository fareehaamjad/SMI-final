package com.example.securemobileidentity;

import java.util.Date;

import com.example.securemobileidentity.Constants.UserType;

public class Message implements Comparable<Message>
{
	//if usertype is other: name and number will be of the sender
	//if usertype is self: name and number will be of the receiver 

	public String number;
	public String message;
	public Date time;
	public Constants.UserType type;
	public boolean isRead;


	public UserType getUserType() 
	{
		return type;
	}


	public Date getMessageTime() 
	{
		// TODO Auto-generated method stub
		return time;
	}


	public CharSequence getMessageText() 
	{

		return message;
	}


	public void setType(String t) 
	{
		if (t.equalsIgnoreCase("SELF"))
		{
			type = Constants.UserType.SELF;
		}
		else
		{
			type = Constants.UserType.OTHER;
		}
	}


	public String getUserTypeStr() 
	{
		if (type== Constants.UserType.SELF)
		{
			return "SELF";
		}
		else
		{
			return "OTHER";
		}
	}


	public String getReadStatus() 
	{
		if (isRead)
		{
			return "true";
		}
		else
		{
			return "false";
		}
	}


	public void setReadStatus(String s) 
	{
		if (s.equalsIgnoreCase("true"))
		{
			isRead = true;
		}
		else
		{
			isRead = false;
		}

	}


	@Override
	public int compareTo(Message m) 
	{
		if (getMessageTime() == null || m.getMessageTime() == null)
			return 0;
		
		return getMessageTime().compareTo(m.getMessageTime());
	}

}
