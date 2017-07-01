package com.cx.sumicashersystem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.json.JSONException;
import org.json.JSONObject;

import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.myservice.MyService;
import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.object.UpdateObject;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ChoosePage extends Activity{
	SharedPreferences sharedPreferences;
	private static String TAG="ChoosePage";
	private AlertDialog mAlertDialog;
	private ProgressDialog progressbar;
	private String fileName;
	private final int percentStep=5;
	private final int SHOWUPDATEDIAG=1;
	private final int UPDATEPROGRESS=2;
	private final int UPDATEWRONG=3;
	boolean stopThread=false;
	TextView VersionTv;
	Handler dealerHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			super.handleMessage(msg);
			int msgId=msg.what;
			switch(msgId){
			case SHOWUPDATEDIAG://需要更新
				showUpdateDialog();
				break;
			case UPDATEPROGRESS:
				Bundle mBundle=msg.getData();
				int percentCount=mBundle.getInt("percentCount");
				if(percentCount>0){
					if(progressbar!=null){
						progressbar.setProgress(percentCount);
					}
				}
				
				if(percentCount==100){
					if(progressbar!=null){
						if(progressbar.isShowing()){
							progressbar.dismiss();
							progressbar=null;
						}
						inStallAPK();
					}
					
				}
				break;
			case UPDATEWRONG:
				Toast.makeText(ChoosePage.this, "下载错误", Toast.LENGTH_SHORT).show();;
				if(progressbar!=null){
					if(progressbar.isShowing()){
						progressbar.dismiss();
						progressbar=null;
					}
				}
				Log.d("ChoosePage", "UPDATEWRONG");
				try{
					File apkFile=new File(Environment.getExternalStorageDirectory(),fileName);
					if(apkFile.exists()){
						apkFile.delete();
					}
				}catch(Exception e){
					e.printStackTrace();
				}
				break;
			default:
				break;
			}
		}
		
	};
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choosepage);
		checkLogin();
		checkUpdateByHttp();
		initView();
	}
	private void checkLogin(){
		sharedPreferences=getSharedPreferences("cashiervalues",0);
		if(sharedPreferences.getInt("loginState", 0)==0){//未登录，跳loginPage
			Intent toLogin=new Intent(ChoosePage.this,LoginActivity.class);
			startActivity(toLogin);
			ChoosePage.this.finish();
		}
	}
private void initView(){
	String localVersionStr=getLocalVersion();
	VersionTv=(TextView)findViewById(R.id.version_message);
	if(!localVersionStr.equals("")){
		VersionTv.setText("当前版本号:"+localVersionStr);
	}
	Button orderMBtn=(Button)findViewById(R.id.order_manage);
	orderMBtn.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			Intent intent_orderM=new Intent(ChoosePage.this,OrderManagerActivity.class);
			startActivity(intent_orderM);
		}
		
	});
	Button cashBtn=(Button)findViewById(R.id.cash);
	cashBtn.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			Intent intent_cash=new Intent(ChoosePage.this,MyService.class);
			startActivity(intent_cash);
		}
		
	});
}

public void checkUpdateByHttp(){
	AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
	asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	sharedPreferences=getSharedPreferences("cashiervalues",0);
	String mUrl=sharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=sharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
	String checkUpdateUrl=mUrl+":"+mPort+"/"+HttpParams.checkUpdateUrl;
//	String checkUpdateUrl=mUrl+"/"+HttpParams.checkUpdateUrl;
/*	String mUrl="192.168.1.63";
	String mPort="3003";
	String checkUpdateUrl="http://"+mUrl+":"+mPort+"/"+HttpParams.checkUpdateUrl;*/
	Log.d(TAG, "checkUpdateUrl"+checkUpdateUrl);
	RequestParams params=new RequestParams();
	params.put("SystemOS", HttpParams.SystemOS);
	params.put("ProjectName",HttpParams.ProjectName);
	asyncHttpClient.post(checkUpdateUrl, params, new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(response);
			boolean success=false;
			try{
				success=response.getBoolean("success");
				if(success){
					Log.d(TAG, "success is true");
					JSONObject value=response.getJSONObject("value");
					if(value!=null){
						Log.d(TAG, "value is not null");
						JSONObject latestVersion=value.getJSONObject("latest_version");
						if(latestVersion!=null){
							Log.d(TAG, "lastVersion is not null");
							String versionCode=latestVersion.getString("version_code");
							String updateHintMsg=latestVersion.getString("update_hint_msg");
							String downLoadUrl=latestVersion.getString("update_url");
							String releaseDate=latestVersion.getString("release_date");
							UpdateObject.versionCode=versionCode;
							UpdateObject.hintMessage=updateHintMsg;
							UpdateObject.downLoadUrl=downLoadUrl;
							UpdateObject.releaseDate=releaseDate;
							Log.d(TAG, "servierversionCode"+UpdateObject.versionCode);
							if(isNeedUpdate(UpdateObject.versionCode)){
								Log.d(TAG, "needUpdate");
							 Message message=new Message();
							 message.what=SHOWUPDATEDIAG;
							 dealerHandler.sendMessage(message);
							}
							
						}
					}else{
						Log.d(TAG, "value is  null");
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
			Toast.makeText(ChoosePage.this, "自动更新检查失败", Toast.LENGTH_SHORT).show();
		}
		
	});
}

public boolean isNeedUpdate(String serverVersionCode){
	String localVersion=getLocalVersion();
	Log.d(TAG, "localVersion"+localVersion);
	if(localVersion.equals("-1")){
		Log.d(TAG, "本地版本namenofound");
		return false;
	}else if(localVersion.compareTo(UpdateObject.versionCode)==0){
		Log.d(TAG, "本地版本不需要更新");
		return false;
	}else if(localVersion.compareTo(UpdateObject.versionCode)<0){
		return true;
	}else{
		Log.d(TAG, "本地版大于新版本");
		return false;
	}
		
	}

public String getLocalVersion(){
	String localVersionStr="";
	PackageManager mPackagemanager=getPackageManager();
	try{
		PackageInfo packageInfo=mPackagemanager.getPackageInfo(getPackageName(), 0);
		localVersionStr=packageInfo.versionName;
	}catch(NameNotFoundException e){
		e.printStackTrace();
		localVersionStr= "-1";
	}
	return localVersionStr;
}

public void showUpdateDialog(){
	mAlertDialog=new AlertDialog.Builder(ChoosePage.this).create();
	mAlertDialog.setCancelable(false);
	LayoutInflater myInflater=LayoutInflater.from(ChoosePage.this);
	View mView=myInflater.inflate(R.layout.alertdialog_layout, null);
	Button sureToUpdate=(Button)mView.findViewById(R.id.suretoUpdate_id);
	TextView versionMsg=(TextView)mView.findViewById(R.id.versionmsg_id);
	if(UpdateObject.versionCode!=null){
		versionMsg.setText("请更新到版本"+UpdateObject.versionCode);
	}
	sureToUpdate.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			if(UpdateObject.downLoadUrl!=null){
				downLoadAPK(UpdateObject.downLoadUrl);
			}else{
				Log.d(TAG, "downLoadUrl is null");
			}
		}
		
	});
	mAlertDialog.show();
	mAlertDialog.getWindow().setContentView(mView,new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT));
}

public void downLoadAPK(final String url){
	if(mAlertDialog!=null){
		if(mAlertDialog.isShowing()){
			mAlertDialog.dismiss();
		}
		mAlertDialog=null;
	}
	if(!(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))){
		Toast.makeText(ChoosePage.this, "SD卡不可用", Toast.LENGTH_SHORT).show();
	}else{//原生http请求...
		progressbar=new ProgressDialog(ChoosePage.this);
		progressbar.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
		progressbar.setTitle("正在下载...");
		progressbar.setMessage("请稍候...");
		progressbar.setProgress(0);
		progressbar.show();
		progressbar.setCancelable(false);
		
		new Thread(){

			@Override
			public void run() {
				if(!stopThread){
					// TODO 自动生成的方法存根
					int updateTotalSize=0;
					int totalSize=0;
					int percentCount=0;
					InputStream is=null;
					FileOutputStream fileOutputStream=null;
					HttpClient httpClient=new DefaultHttpClient();
					httpClient.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT, HttpParams.DEFAULT_TIME_OUT);
					httpClient.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT, HttpParams.DEFAULT_TIME_OUT);
					HttpGet get=new HttpGet(url);
					HttpResponse response;
					try{
						response=httpClient.execute(get);
						HttpEntity entity=response.getEntity();
						updateTotalSize=(int)entity.getContentLength();
						 is=entity.getContent();
						
						if(is!=null){
							fileName=HttpParams.APKName+UpdateObject.versionCode+".apk";
							File apkFile=new File(Environment.getExternalStorageDirectory(),fileName);
							/*if(!apkFile.exists()){ 
								apkFile.createNewFile();//cause to apkFile download 0B???
							}*/
							fileOutputStream=new FileOutputStream(apkFile);
							byte[] buf=new byte[32];
							int readSize=-1;
							int progress=0;//????
							while((readSize=is.read(buf))!=-1){
								fileOutputStream.write(buf,0,readSize);
								Log.d("ChoosePage", "apkFileLength"+apkFile.length());
								totalSize+=readSize;
								if(percentCount==0||((totalSize*100)/updateTotalSize-percentStep)>=percentCount){
									percentCount+=percentStep;
									Message message=Message.obtain();//减少开销???
									message.what=UPDATEPROGRESS;
									Bundle mBundle=new Bundle();
									mBundle.putInt("percentCount", percentCount);
									message.setData(mBundle);
									dealerHandler.sendMessage(message);
								}
							}
							
						}else{
							Message message=Message.obtain();
							message.what=UPDATEWRONG;
							dealerHandler.sendMessage(message);
						}
					}catch(ClientProtocolException e){
						Message message=Message.obtain();
						message.what=UPDATEWRONG;
						dealerHandler.sendMessage(message);
						e.printStackTrace();
					}catch(IOException e){
						Message message=Message.obtain();
						message.what=UPDATEWRONG;
						dealerHandler.sendMessage(message);
						e.printStackTrace();
					}finally{
					try{
						if(is!=null){
							is.close();
						}
						if(fileOutputStream!=null){
							fileOutputStream.close();
						}
						
					}catch(IOException e){
						e.printStackTrace();
					}
					}
				}
			}
			
		}.start();
		
	}	
}
public void inStallAPK(){
	Intent intent=new Intent(Intent.ACTION_VIEW);
	intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
	File apkFile=new File(Environment.getExternalStorageDirectory(),fileName);
	if(apkFile.length()>0){
		intent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
	}else{
		Toast.makeText(ChoosePage.this, "apk文件为0B", Toast.LENGTH_SHORT).show();
	}
	
	startActivity(intent);
	android.os.Process.killProcess(android.os.Process.myPid());
}
@Override
protected void onDestroy() {
	// TODO 自动生成的方法存根
	stopThread=true;
	super.onDestroy();
}



}
