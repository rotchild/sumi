package com.cx.sumicashersystem.myservice;


import com.cx.sumicashersystem.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class FailResultActivitySec extends Activity {
Button failBackbt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.failresultsec_layout);
		initView();
	}
	private void initView(){

failBackbt=(Button)findViewById(R.id.payfail_back_bt);
failBackbt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		FailResultActivitySec.this.finish();
	}
	
});
	}
}
