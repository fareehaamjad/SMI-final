package com.example.securemobileidentity;

public class Contact 
{

	public String number;
	public String name;
	public boolean isSMI;

	public Contact(String _name, String _number, boolean _isSMI)
	{
		name = _name;
		number = _number;
		isSMI = _isSMI;
	}

	public String getName() 
	{
		// TODO Auto-generated method stub
		return name;
	}

	@Override
	public boolean equals(Object c)
	{
		if (number.equals(((Contact)c).number) && name.equals(((Contact)c).name))
		{
			return true;
		}
		else
		{
			return false;
		}

	}

}
