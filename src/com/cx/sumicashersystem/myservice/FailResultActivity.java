package com.cx.sumicashersystem.myservice;


import com.cx.sumicashersystem.R;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;

public class FailResultActivity extends Activity {
Button rescanbt,failBackbt;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.failresult_layout);
		initView();
	}
	private void initView(){
rescanbt=(Button)findViewById(R.id.rescan_bt);
failBackbt=(Button)findViewById(R.id.payfail_back_bt);

rescanbt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		Bundle bundle=new Bundle();
		bundle.putString("scanType", "repayScan");
		Intent intent=new Intent(FailResultActivity.this,MipcaActivityCapture.class);
		intent.putExtras(bundle);
		startActivity(intent);
		FailResultActivity.this.finish();
	}
	
});
failBackbt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		FailResultActivity.this.finish();
	}
	
});
	}
}
