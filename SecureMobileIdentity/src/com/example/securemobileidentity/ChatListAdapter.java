package com.example.securemobileidentity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.text.Html;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

import org.apache.http.impl.cookie.DateUtils;


@SuppressLint("ResourceAsColor") public class ChatListAdapter  extends BaseAdapter 
{

	private Context context;


	public ChatListAdapter(Context context) 
	{
		this.context = context;

	}


	@Override
	public int getCount() {
		return Constants.allMsgs.size();
	}

	@Override
	public Message getItem(int position) {
		return Constants.allMsgs.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	private static class ViewHolder
	{
		TextView message;
		TextView date;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) 
	{
		Message message = (Message) this.getItem(position);

		ViewHolder holder; 
		if(convertView == null)
		{
			holder = new ViewHolder();
			convertView = LayoutInflater.from(context).inflate(R.layout.sms_row, parent, false);
			holder.message = (TextView) convertView.findViewById(R.id.message_text);
			holder.date = (TextView) convertView.findViewById(R.id.tv_date);
			convertView.setTag(holder);
		}
		else
		{
			holder = (ViewHolder) convertView.getTag();

		}
		
		if (position == 0)
		{
			holder.date.setVisibility(View.VISIBLE);
			holder.date.setText(Constants.DATE_FORMAT.format(message.getMessageTime()));
		}
		else
		{
			if (Constants.isSameDate(getItem(position - 1).getMessageTime(), message.getMessageTime()))
			{
				holder.date.setVisibility(View.GONE);
			}
			else
			{
				holder.date.setVisibility(View.VISIBLE);
				holder.date.setText(Constants.DATE_FORMAT.format(message.getMessageTime()));
			}
		}
		
		

		String msg = message.getMessageText() +
				"  <font color=\"#B0B0B0\"> <small>"+
				Constants.TIME_FORMAT.format(message.getMessageTime()) +
				" </small></font>";
		holder.message.setText(Html.fromHtml(msg));
		
		if (! message.isRead)
		{
			message.isRead = true;
			
			Constants.allMsgs.get(position).isRead = true;
			Constants.dbHandler.updateStatusOfMsg(Constants.allMsgs.get(position));
		}
		
		
		LayoutParams lp = (LayoutParams) holder.message.getLayoutParams();
		//check if it is a status message then remove background, and change text color.
		/*if(message.isStatusMessage())
		{
			holder.message.setBackgroundDrawable(null);
			lp.gravity = Gravity.LEFT;
			holder.message.setTextColor(R.color.textFieldColor);
		}
		else*/


		//Check whether message is mine to show green background and align to right
		if(message.getUserType() == Constants.UserType.SELF)
		{
			holder.message.setBackgroundResource(R.drawable.speech_bubble_green);
			lp.gravity = Gravity.RIGHT;
		}
		//If not mine then it is from sender to show orange background and align to left
		else
		{
			holder.message.setBackgroundResource(R.drawable.speech_bubble_orange);
			lp.gravity = Gravity.LEFT;
		}
		holder.message.setLayoutParams(lp);
		holder.message.setTextColor(R.color.textColor);	

		return convertView;    
	}

	@Override
	public int getViewTypeCount() {
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		Message message = Constants.allMsgs.get(position);
		return message.getUserType().ordinal();
	}

	private class ViewHolder1 {
		public TextView messageTextView;
		public TextView timeTextView;


	}

	private class ViewHolder2 {
		public ImageView messageStatus;
		public TextView messageTextView;
		public TextView timeTextView;

	}
}