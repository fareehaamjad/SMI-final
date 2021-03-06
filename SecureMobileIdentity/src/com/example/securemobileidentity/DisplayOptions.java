package com.example.securemobileidentity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("ResourceAsColor") public class DisplayOptions extends Activity 
{

	String className = "DisplayOptions";

	TextView name, number;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.display_options);

		name = (TextView) findViewById(R.id.tv_name);
		number = (TextView) findViewById(R.id.tv_number);

		name.setText(Constants.contactSelected.getName());
		number.setText(Constants.contactSelected.number);
	}
	
	@Override
	public void onResume()
	{
		Constants.currentContext = this;
		super.onResume();
	}


	public void goBack(View v)
	{
		onBackPressed();
	}

	public void btnClick(View v)
	{
		if (v.getId() == R.id.button_sms)
		{
			final TextView t = (TextView) findViewById(R.id.button_sms);
			t.setBackgroundColor(R.color.blue);

			new Thread(){
				@Override
				public void run(){
					try {
						synchronized(this){
							wait(100);

							DisplayOptions.this.runOnUiThread(new Runnable()
							{
								public void run()
								{
									t.setBackgroundColor(0);
								}
							});
						}
					}
					catch(InterruptedException ex){                    
					}

					// TODO              
				}
			}.start();

			Intent intent = new Intent(DisplayOptions.this, SMSActivity.class);
			intent.putExtra("method","sms");
			startActivity(intent);

		}
		else if (v.getId() == R.id.button_data)
		{
			final TextView t = (TextView) findViewById(R.id.button_data);
			t.setBackgroundColor(R.color.blue);

			new Thread(){
				@Override
				public void run(){
					try {
						synchronized(this){
							wait(100);

							DisplayOptions.this.runOnUiThread(new Runnable()
							{
								public void run()
								{
									t.setBackgroundColor(0);
								}
							});
						}
					}
					catch(InterruptedException ex){                    
					}

					// TODO              
				}
			}.start();


			Toast.makeText(this, "This option is not yet available. Please try again later!", Toast.LENGTH_LONG).show();

			
		}
		else if (v.getId() == R.id.button_bluetooth)
		{
			final TextView t = (TextView) DisplayOptions.this.findViewById(R.id.button_bluetooth);
			t.setBackgroundColor(R.color.blue);

			new Thread(){
				@Override
				public void run(){
					try {
						synchronized(this){
							wait(100);

							DisplayOptions.this.runOnUiThread(new Runnable()
							{
								public void run()
								{
									t.setBackgroundColor(0);
								}
							});
						}
					}
					catch(InterruptedException ex){                    
					}

					// TODO              
				}
			}.start();


			Toast.makeText(this, "This option is not yet available. Please try again later!", Toast.LENGTH_LONG).show();
			
		}
	}

}
