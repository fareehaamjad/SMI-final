package com.example.securemobileidentity;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SectionIndexer;
import android.widget.TextView;

public class CustomListViewAdapter extends ArrayAdapter<Contact> implements SectionIndexer 
{

	private HashMap<String, Integer> alphaIndexer;
	private String[] sections;

	Context context;
	
	DatabaseHandler dbHandler;

	public CustomListViewAdapter(Context _context, int contactRow, ArrayList<Contact> items, DatabaseHandler db) 
	{
		super(_context, contactRow, items);

		context = _context;
		dbHandler = db;
		
		alphaIndexer = new HashMap<String, Integer>();
		for (int i = 0; i < items.size(); i++)
		{
			String s = items.get(i).getName().substring(0, 1).toUpperCase();
			if (!alphaIndexer.containsKey(s))
				alphaIndexer.put(s, i);
		}

		Set<String> sectionLetters = alphaIndexer.keySet();
		ArrayList<String> sectionList = new ArrayList<String>(sectionLetters);
		Collections.sort(sectionList);
		sections = new String[sectionList.size()];
		for (int i = 0; i < sectionList.size(); i++)
			sections[i] = sectionList.get(i); 

	}

	/*private view holder class*/
	private class ViewHolder 
	{
		ImageView imageView;
		TextView name;
		ImageView showUnread;
	}

	public View getView(int position, View convertView, ViewGroup parent) 
	{
		ViewHolder holder = null;
		Contact rowItem = getItem(position);

		LayoutInflater mInflater = (LayoutInflater) context
				.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);

		if (convertView == null) 
		{
			convertView = mInflater.inflate(R.layout.contact_row, null);
			holder = new ViewHolder();
			holder.name = (TextView) convertView.findViewById(R.id.tv_name);
			holder.imageView = (ImageView) convertView.findViewById(R.id.iv_issmi);
			holder.showUnread = (ImageView) convertView.findViewById(R.id.iv_showunread);

			convertView.setTag(holder);
		}
		else
			holder = (ViewHolder) convertView.getTag();

		holder.name.setText(rowItem.getName());
		
		if (rowItem.isSMI)
		{
			holder.imageView.setVisibility(View.VISIBLE);
		}
		else
		{
			holder.imageView.setVisibility(View.INVISIBLE);
		}
		
		try 
		{
			if (Constants.isAnyUnreadMsg(rowItem, dbHandler))
			{
				holder.showUnread.setVisibility(View.VISIBLE);
			}
			else
			{
				holder.showUnread.setVisibility(View.GONE);
			}
		} catch (ParseException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//holder.imageView.setImageResource(R.);

		return convertView;
	}

	public void doFilter(ListView mainListView, String charSeq, List<Contact> rowItems) 
	{
		ArrayList<Contact> newRow = new ArrayList<Contact>();

		for(int i = 0 ; i < rowItems.size(); i++)
		{
			if (rowItems.get(i).getName().toLowerCase().contains(charSeq.toLowerCase()))
			{
				newRow.add(rowItems.get(i));
			}
		}


		mainListView.setAdapter( new CustomListViewAdapter(context, R.layout.contact_row, newRow, dbHandler) );     

	}

	@Override
	public int getPositionForSection(int section) {
		// TODO Auto-generated method stub
		return alphaIndexer.get(sections[section]);
	}

	@Override
	public int getSectionForPosition(int arg0) {
		// TODO Auto-generated method stub
		return 1;
	}

	@Override
	public Object[] getSections() {
		// TODO Auto-generated method stub
		return sections;
	}

}
