package com.cx.sumicashersystem.myservice;


import com.cx.sumicashersystem.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

public class ScannPayCodeActivity extends Activity {
EditText scanResult;
Button scanButton;
Button cleanresultButton;
Button toResultButton;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.scannpay_layout);
		initView();
	}
	private void initView(){
	scanResult=(EditText)findViewById(R.id.scan_result);
	scanButton=(Button)findViewById(R.id.scan_btn);
	cleanresultButton=(Button)findViewById(R.id.cleanresult_btn);
	toResultButton=(Button)findViewById(R.id.toresult_btn);
	scanButton.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO �Զ����ɵķ������
			
		}
		
	});
	cleanresultButton.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO �Զ����ɵķ������
			
		}
		
	});
	toResultButton.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO �Զ����ɵķ������
			Intent toPrint=new Intent(ScannPayCodeActivity.this,PrtintTest.class);
			startActivity(toPrint);
		}
		
	});
	}
}
