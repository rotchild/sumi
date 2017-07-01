package com.cx.sumicashersystem;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;






import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.ipset.IPsetActivity;
import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.utils.MD5Util;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends Activity {

SharedPreferences loginSharedPreferences;
EditText userNameET;
EditText passwordET;
ProgressDialog pd;
Timer mTimer;
TimerTask tTask;
private Handler dealerHandler=new Handler(){

	@Override
	public void handleMessage(Message msg) {
		// TODO 自动生成的方法存根
		super.handleMessage(msg);
		int msgId=msg.what;
		switch(msgId){
		case 1:
			if(pd!=null){
				pd.dismiss();
				pd=null;
				Toast.makeText(LoginActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
			}
			break;
		default:
			break;
		}
	}
	
};
public static final String APPTYPE="3";//收银apptype为3
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.activity_main);
		initView();
	}
private void initView(){
	loginSharedPreferences=getSharedPreferences("cashiervalues",0);
	userNameET=(EditText)findViewById(R.id.username_edit);
	passwordET=(EditText)findViewById(R.id.password_edit);
	Button loginBtn=(Button)findViewById(R.id.login_btn);
	loginBtn.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			String usernameStr=userNameET.getText().toString();
			String passwordStr=passwordET.getText().toString();
			if(isAdmin(usernameStr,passwordStr)){//是否为管理员
				if(loginSharedPreferences.getInt("loginState", 0)==0){
					SharedPreferences.Editor editor=loginSharedPreferences.edit();
					editor.putInt("loginState", 1);
					editor.commit();
				}
				Intent toIPSet=new Intent(LoginActivity.this,IPsetActivity.class);
				startActivity(toIPSet);
				LoginActivity.this.finish();
			}else{//非管理员
				if(isNetWorkAvailable(getApplicationContext())){
					
						//can't use getContext()
						pd=ProgressDialog.show(LoginActivity.this, "请等待", "加载中");
						mTimer=new Timer();
						tTask=new TimerTask(){

							@Override
							public void run() {
								// TODO 自动生成的方法存根
								Message message=new Message();
								message.what=1;
								dealerHandler.sendMessage(message);
							}
						};
						//mTimer.schedule(tTask, HttpParams.DEFAULT_TIME_OUT);
					
					cashierlogin(usernameStr,passwordStr);
				}else{
					Toast.makeText(getApplicationContext(), "当前网络不可用", Toast.LENGTH_SHORT).show();
				}
				
			}
		}
		
	});
}
private Boolean isAdmin(String username,String password){
	Boolean result=false;
	if(username.equals("admin")&&password.equals("1234")){
		result=true;
	}
	return result;
}

private void cashierlogin(String username,String password){
	AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
	asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	String mUrl=loginSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
	//String mUrl=HttpParams.DEFAULT_URL;
	String mPort=loginSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
	String loginUrl=mUrl+":"+mPort+"/"+HttpParams.LoginBUrl;
	//String loginUrl=mUrl+"/"+HttpParams.LoginBUrl;
	RequestParams params=new RequestParams();
	params.put("username",username);
	String passwordMd5=MD5Util.MD5(password);//password change to MD5
	params.put("password", passwordMd5);
	params.put("apptype", APPTYPE);

	asyncHttpClient.post(loginUrl, params,new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(statusCode, response);
			if(pd!=null){
				pd.dismiss();
				pd=null;
			}else{
				Log.e("LoginAcitivity", "pd=null");
			}
			boolean success=false;
			try{
				success=response.getBoolean("success");
				Log.d("loginActivity", "responseSuccess is"+success);
				if(success){//用户名密码正确
					Log.d("loginActivity", "enter"+success);
					JSONArray data=response.getJSONArray("data");
					Log.d("loginActivity", "dataLength"+data.length());
					if(data.length()>0){
						JSONObject data0=(JSONObject) data.get(0);
						int userId=data0.getInt("id");
						String userName=data0.getString("username");
						String passwordMd5=data0.getString("password");
						String realName=data0.getString("realname");
						String telePhone=data0.getString("telephone");
						int userClass=data0.getInt("class");
						int state=data0.getInt("state");
						int stationid=data0.getInt("stationid");
						String address=data0.getString("address");
						String stationName=data0.getString("stationname");
						Log.d("loginActivity", realName+"/"+telePhone+"/"+"userClass:"
						+userClass+"/"+"state:"+state+"/"+"stationid:"+stationid+"/"+address+
						"/"+stationName);
						
						SharedPreferences.Editor customerInfoEditor=loginSharedPreferences.edit();
						customerInfoEditor.putInt("userId", userId);
						customerInfoEditor.putString("userName", userName);
						customerInfoEditor.putString("passwordMd5", passwordMd5);
						customerInfoEditor.putString("realName", realName);
						customerInfoEditor.putString("telePhone", telePhone);
						customerInfoEditor.putInt("userClass",userClass);
						customerInfoEditor.putInt("state", state);
						customerInfoEditor.putInt("stationid", stationid);
						customerInfoEditor.putString("address", address);
						customerInfoEditor.putString("stationName", stationName);
						customerInfoEditor.commit();
						if(loginSharedPreferences.getInt("loginState", 0)==0){
							SharedPreferences.Editor editor=loginSharedPreferences.edit();
							editor.putInt("loginState", 1);
							editor.commit();
						}
						getCardsByasyncPost(String.valueOf(stationid));
						Intent intent=new Intent(LoginActivity.this,ChoosePage.class);
						startActivity(intent);
						LoginActivity.this.finish();
					}
					
				}else{//用户名密码错误
					Log.d("loginActivity", "usernamewrong");
					Toast.makeText(getApplicationContext(), "用户名或密码错误", Toast.LENGTH_SHORT).show();
				}
			}catch(JSONException e){
				e.printStackTrace();
				
			}
			
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			Log.d("loginActivity", "onFailure");
			if(pd!=null){
				pd.dismiss();
				pd=null;
			}
			Toast.makeText(getApplicationContext(), "连接异常", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onFailure(Throwable e, String errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			Log.d("loginActivity", "onFailure");
			if(pd!=null){
				pd.dismiss();
				pd=null;
			}
			Toast.makeText(getApplicationContext(), "连接异常", Toast.LENGTH_SHORT).show();
		}	
	});
	
}
public boolean isNetWorkAvailable(Context context){
	boolean result=false;
	ConnectivityManager connectivity=(ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
	if(connectivity!=null){
		NetworkInfo info=connectivity.getActiveNetworkInfo();
		if(info!=null&&info.isConnected()){
			if(info.getState()==NetworkInfo.State.CONNECTED){
				result=true;
			}
		}
	}
	return result;
	
}
public void  getCardsByasyncPost(String stationIdStr){
	AsyncHttpClient getCardHttpClient=new AsyncHttpClient();
	getCardHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	getCardHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	String mUrl=loginSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=loginSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
	String cardUrl=mUrl+":"+mPort+"/"+HttpParams.getCardsUrl;
	//String cardUrl=mUrl+"/"+HttpParams.getCardsUrl;

	RequestParams Cardparams=new RequestParams();
	Cardparams.put("stationid", stationIdStr);
	getCardHttpClient.post(cardUrl, Cardparams, new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(statusCode, response);
			boolean success=false;
			try{
				success=response.getBoolean("success");
				if(success){//what's the use of sucess
					JSONArray data=response.getJSONArray("data");
					if(data.length()>0){
						JSONObject data0=(JSONObject) data.get(0);
						String cardsNumber=data0.getString("cardno");
						String amount=data0.getString("amount");
						SharedPreferences.Editor cardInfoEditor=loginSharedPreferences.edit();
						cardInfoEditor.putString("cardsNumber", cardsNumber);
						cardInfoEditor.putString("amount", amount);
						cardInfoEditor.commit();
						Log.d("loginActivity", "cardsNumber"+cardsNumber+"/"
						+"amount"+amount);
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
			Log.d("loginActivity", "cardsonFailure");
		}
		
		@Override
		public void onFailure(Throwable e, String errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			Log.d("loginActivity", "onFailure");
			if(pd!=null){
				pd.dismiss();
				pd=null;
			}
		}	
		
		
		
	});
}
@Override
protected void onDestroy() {
	// TODO 自动生成的方法存根
	if(pd!=null){
		pd.dismiss();
		pd=null;
	}
	if(mTimer!=null){
		if(tTask!=null){
			tTask.cancel();
		    tTask=null;
		}
		mTimer.cancel();
		mTimer=null;
	}
	super.onDestroy();
}

}
