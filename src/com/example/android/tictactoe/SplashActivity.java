package com.example.android.tictactoe;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;



public class SplashActivity extends Activity {
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_loading);
		Handler handler = new Handler(){
			@Override
			public void handleMessage(Message msg) {
				// TODO Auto-generated method stub
				finish();
			}
		};
		handler.sendEmptyMessageDelayed(0, 1500);

	}
}
