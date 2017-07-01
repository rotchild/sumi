package com.cx.sumicashersystem.ipset;


import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.R;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class IPsetActivity extends Activity {
SharedPreferences ipsetSP;
TextView exitTV;
//EditText ipsetET1,ipsetET2,ipsetET3,ipsetET4;
EditText ipsetET1;
EditText portET;
private static final String TAG="IPsetActivity";
Button sureToSetBT;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ipset_layout);
		initView();
	}
private void initView(){
	ipsetSP=getSharedPreferences("cashiervalues",0);
	String ipStr1=ipsetSP.getString("ippart1", HttpParams.DEFAULT_PAR1);
	String ipStr2=ipsetSP.getString("ippart2", HttpParams.DEFAULT_PAR2);
	String ipStr3=ipsetSP.getString("ippart3", HttpParams.DEFAULT_PAR3);
	//String ipStr4=ipsetSP.getString("ippart4", HttpParams.DEFAULT_PAR4);
	String currentPort=ipsetSP.getString("mPort",HttpParams.DEFAULT_PORT);
	
	ipsetET1=(EditText)findViewById(R.id.ipinput_et1);
	//ipsetET2=(EditText)findViewById(R.id.ipinput_et2);
	//ipsetET3=(EditText)findViewById(R.id.ipinput_et3);
	//ipsetET4=(EditText)findViewById(R.id.ipinput_et4);
	portET=(EditText)findViewById(R.id.portinput_et);
	
	ipsetET1.setText(ipStr1);
	//ipsetET2.setText(ipStr2);
	//ipsetET3.setText(ipStr3);
	//ipsetET4.setText(ipStr4);
	portET.setText(currentPort);
	
	sureToSetBT=(Button)findViewById(R.id.suretoset_bt);
    exitTV=(TextView)findViewById(R.id.exit_tv_setip);
    
    sureToSetBT.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			String ipStr1=ipsetET1.getText().toString();
			//String ipStr2=ipsetET2.getText().toString();
			//String ipStr3=ipsetET3.getText().toString();
			//String ipStr4=ipsetET4.getText().toString();
			//String ipUrl="http://"+ipStr1+"."+ipStr2+"."+ipStr3+"."+ipStr4;
			String ipUrl="http://"+ipStr1;
			Log.d(TAG, "ipUrl");
			String portStr=portET.getText().toString();
			if(portStr.equals("80")){
				portStr=portStr+"/xwps";
			}
			SharedPreferences.Editor ipportEditor=ipsetSP.edit();
			ipportEditor.putString("ippart1", ipStr1);
			//ipportEditor.putString("ippart2", ipStr2);
			//ipportEditor.putString("ippart3", ipStr3);
			//ipportEditor.putString("ippart4", ipStr4);
			ipportEditor.putString("mUrl", ipUrl);
			ipportEditor.putString("mPort",portStr);
			boolean result=ipportEditor.commit();
			if(result){
				Toast.makeText(IPsetActivity.this, "修改成功", Toast.LENGTH_SHORT).show();
			}else{
				Toast.makeText(IPsetActivity.this, "修改失败", Toast.LENGTH_SHORT).show();
			}
		}
    	
    });
    
	exitTV.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			
			SharedPreferences.Editor ipsetEditor=ipsetSP.edit();
			ipsetEditor.putInt("loginState", 0);
			ipsetEditor.commit();
			System.exit(0);
		}
		
	});
}
}
