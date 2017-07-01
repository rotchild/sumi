package com.cx.sumicashersystem.ipset;

import org.json.JSONException;
import org.json.JSONObject;

import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.utils.MD5Util;
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

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

public class PasswordSetActivity extends Activity {
EditText oldPasswordET;
EditText newPasswordET;
EditText repeatNewPasswordET;
Button sureToSetBt;
TextView backTv;
SharedPreferences passwordSetSP;
private String TAG="PasswordSetActivity";
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.passwordset);
		initView();
	}
private void initView(){
	passwordSetSP=getSharedPreferences("cashiervalues",0);
	backTv=(TextView)findViewById(R.id.back_tv_passwordset);
	
	oldPasswordET=(EditText)findViewById(R.id.original_password_et);
	newPasswordET=(EditText)findViewById(R.id.new_password_et);
	repeatNewPasswordET=(EditText)findViewById(R.id.repeatnew_password_et);
	sureToSetBt=(Button)findViewById(R.id.suretosetPW_bt);
	
	backTv.setOnClickListener(new View.OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			PasswordSetActivity.this.finish();
		}
		
	});
	sureToSetBt.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			String oldPasswordStr=oldPasswordET.getText().toString();
			String newPasswordStr=newPasswordET.getText().toString();
			String repeatNewPasswordStr=repeatNewPasswordET.getText().toString();
			if(newPasswordStr.equals(repeatNewPasswordStr)){
				int userid=passwordSetSP.getInt("userId", -1);
				String useridStr=String.valueOf(userid);
				changePasswordByAsyncHttp(useridStr,oldPasswordStr,newPasswordStr);
				
			}else{
				Toast.makeText(getApplicationContext(), "两次输入的新密码不一致", Toast.LENGTH_SHORT).show();
			}
		}
		
	});
}

public void changePasswordByAsyncHttp(String userid,String oldPWD,String newPWD){
	AsyncHttpClient changePWDClient=new AsyncHttpClient();
	changePWDClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	changePWDClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	String mUrl=passwordSetSP.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=passwordSetSP.getString("mPort", HttpParams.DEFAULT_PORT);
	String changePasswordUrl=mUrl+":"+mPort+"/"+HttpParams.changePassWordUrl;
	//String changePasswordUrl=mUrl+"/"+HttpParams.changePassWordUrl;
	Log.d(TAG, "changePasswordURL"+changePasswordUrl);
	RequestParams passwordParams=new RequestParams();
	passwordParams.put("userid", userid);
	Log.d(TAG, "userid"+userid);
	passwordParams.put("oldpwd",MD5Util.MD5(oldPWD));
	Log.d(TAG, "oldpwdMD"+MD5Util.MD5(oldPWD));
	passwordParams.put("newpwd",MD5Util.MD5(newPWD));
	Log.d(TAG, "newpwdMD"+MD5Util.MD5(newPWD));
	changePWDClient.post(changePasswordUrl, passwordParams, new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(statusCode, response);
			boolean success=false;
			try{
				success=response.getBoolean("success");
				if(success){
					Log.d(TAG, "success is true");
					/*JSONObject data=response.getJSONObject("data");
					int affectedRows=data.getInt("affectedRows");
					Log.d("loginActivity", "affectedRows"+affectedRows);
					if(affectedRows>0){
						Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
					}else{
						Toast.makeText(getApplicationContext(), "原密码错误", Toast.LENGTH_SHORT).show();
					}*/
					Toast.makeText(getApplicationContext(), "修改成功", Toast.LENGTH_SHORT).show();
				}else{
					Log.d(TAG, "success is false");
					String data=response.getString("data");
					if(!data.equals("")){
						Toast.makeText(PasswordSetActivity.this, data, Toast.LENGTH_SHORT).show();
					}
				}
			}catch(JSONException e){
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
		}
		
	});
}

}
