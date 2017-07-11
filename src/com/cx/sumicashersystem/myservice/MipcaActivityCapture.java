package com.cx.sumicashersystem.myservice;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.graphics.Bitmap;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.view.SurfaceView;
import android.widget.Toast;

import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.utils.GenerateOrdeNO;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.Result;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.mining.app.zxing.camera.CameraManager;
import com.mining.app.zxing.decoding.CaptureActivityHandler;
import com.mining.app.zxing.decoding.InactivityTimer;
import com.mining.app.zxing.view.ViewfinderView;
/**
 * Initial the camera
 * @author Ryan.Tang
 */
public class MipcaActivityCapture extends Activity implements Callback {

	private CaptureActivityHandler handler;
	private ViewfinderView viewfinderView;
	private boolean hasSurface;
	private Vector<BarcodeFormat> decodeFormats;
	private String characterSet;
	private InactivityTimer inactivityTimer;
	private MediaPlayer mediaPlayer;
	private boolean playBeep;
	private static final float BEEP_VOLUME = 0.10f;
	private boolean vibrate;
	private String scanType;
	private static final String TAG = "MipcaActivity";
	SharedPreferences MipSharedPreferences;
	ProgressDialog pd;//等待条
	SurfaceHolder mHolder;
	Timer mTimer;
	TimerTask tTask;
	String amountStr="";
	String pointsStr="";
	String pointsamountStr="";
	//"realPayStr"
	String realPayStr="";
	String orderno="";
	
	Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			super.handleMessage(msg);
			int msgId=msg.what;
			switch(msgId){
			case 1:
				handler=null;
				handler = new CaptureActivityHandler(MipcaActivityCapture.this, decodeFormats,
						characterSet);
				if(mTimer!=null){
					if(tTask!=null){
						tTask.cancel();
						tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
				}
				break;
			default:break;
			}
		}
		
	};
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Log.d("spc", "MiponCreate");
		setContentView(R.layout.activity_capture);
		MipSharedPreferences=getSharedPreferences("cashiervalues",0);
		Bundle bundle=this.getIntent().getExtras();
		scanType=bundle.getString("scanType");
		if(scanType.equals("scanPayCode")){
			 bundle=this.getIntent().getExtras();
			amountStr=bundle.getString("amountStr");
			pointsStr=bundle.getString("pointsStr");
			pointsamountStr=bundle.getString("pointsamountStr");
			realPayStr=bundle.getString("realPayStr");
		}else if(scanType.equals("repayScan")){
			amountStr=MipSharedPreferences.getString("amountStr", "-1");
			pointsStr=MipSharedPreferences.getString("pointsStr", "-1");
			pointsamountStr=MipSharedPreferences.getString("pointsamountStr", "-1");
			realPayStr=MipSharedPreferences.getString("realPayStr", "-1");
			//4个value置为0
			/*SharedPreferences.Editor clearEditor=MipSharedPreferences.edit();
			clearEditor.putString("amountStr", "0");
			clearEditor.putString("pointsStr", "0");
			clearEditor.putString("pointsamountStr", "0");
			clearEditor.putString("realPayStr", "0");
			clearEditor.commit();*/
		}
		//ViewUtil.addTopView(getApplicationContext(), this, R.string.scan_card);
		CameraManager.init(getApplication());
		viewfinderView = (ViewfinderView) findViewById(R.id.viewfinder_view);
	
		//Button mButtonBack = (Button) findViewById(R.id.button_back);
/*		mButtonBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				MipcaActivityCapture.this.finish();
				
			}
		});*/
		hasSurface = false;
		Log.e("SPC", "hasSurface:"+hasSurface);
		inactivityTimer = new InactivityTimer(this);
	}


	
	@Override
	protected void onResume() {
		super.onResume();
		SurfaceView surfaceView = (SurfaceView) findViewById(R.id.preview_view);
		SurfaceHolder surfaceHolder = surfaceView.getHolder();
		if (hasSurface) {
			initCamera(surfaceHolder);
		} else {
			surfaceHolder.addCallback(this);
			surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
		}
		decodeFormats = null;
		characterSet = "ISO-8859-1";

		playBeep = true;
		AudioManager audioService = (AudioManager) getSystemService(AUDIO_SERVICE);
		if (audioService.getRingerMode() != AudioManager.RINGER_MODE_NORMAL) {
			playBeep = false;
		}
		initBeepSound();
		vibrate = true;
		
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (handler != null) {
			handler.quitSynchronously();
			handler = null;
		}
		CameraManager.get().closeDriver();
	}

	@Override
	protected void onDestroy() {
		inactivityTimer.shutdown();
		super.onDestroy();
	}
	
	/**
	 * 处理扫描结果
	 * @param result
	 * @param barcode
	 */
	public void handleDecode(Result result, Bitmap barcode) {
		inactivityTimer.onActivity();
		playBeepSoundAndVibrate();
		String resultString = result.getText();
		if (resultString.equals("")) {
			Toast.makeText(MipcaActivityCapture.this, "Scan failed!", Toast.LENGTH_SHORT).show();
		}else {
			//Toast.makeText(MipcaActivityCapture.this, resultString, Toast.LENGTH_SHORT).show();
			pd=ProgressDialog.show(MipcaActivityCapture.this, "请等待","加载中...");
			if(scanType.equals("scanCustomerInfor")){
				getCustomerInfo(resultString);
			}else if(scanType.equals("scanPayCode")){
				String reg="\\d{18}";
				boolean matchResult=resultString.matches(reg);
				if(matchResult){
					submitOrderByHttp(resultString);
				}else{
					if(pd!=null){
						pd.cancel();
						pd=null;
					}
					Toast.makeText(MipcaActivityCapture.this,"二维码信息错误", Toast.LENGTH_SHORT).show();
					mTimer=new Timer();//延迟3s
					tTask=new TimerTask(){

						@Override
						public void run() {
							// TODO 自动生成的方法存根
							Message message=Message.obtain();
							message.what=1;
							mHandler.sendMessage(message);
						}
					};
					mTimer.schedule(tTask, 3*1000);
				}
				
			}else if(scanType.equals("repayScan")){
				submitOrderByHttp(resultString);
			}
			
			/*Intent resultIntent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("result", resultString);
			bundle.putParcelable("bitmap", barcode);
			resultIntent.putExtras(bundle);
			this.setResult(RESULT_OK, resultIntent);*/
		}
		//MipcaActivityCapture.this.finish();
	}
	
	private void initCamera(SurfaceHolder surfaceHolder) {
		try {
			CameraManager.get().openDriver(surfaceHolder);
		} catch (IOException ioe) {
			return;
		} catch (RuntimeException e) {
			return;
		}
		if (handler == null) {
			handler = new CaptureActivityHandler(this, decodeFormats,
					characterSet);
		}
	}

	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width,
			int height) {

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
	Log.d("spc", "surfaceCreated");
		mHolder=holder;//cx
		if (!hasSurface) {
			hasSurface = true;
			initCamera(holder);
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		hasSurface = false;

	}

	public ViewfinderView getViewfinderView() {
		return viewfinderView;
	}

	public Handler getHandler() {
		return handler;
	}

	public void drawViewfinder() {
		viewfinderView.drawViewfinder();

	}

	private void initBeepSound() {
		if (playBeep && mediaPlayer == null) {
			// The volume on STREAM_SYSTEM is not adjustable, and users found it
			// too loud,
			// so we now play on the music stream.
			setVolumeControlStream(AudioManager.STREAM_MUSIC);
			mediaPlayer = new MediaPlayer();
			mediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
			mediaPlayer.setOnCompletionListener(beepListener);

			AssetFileDescriptor file = getResources().openRawResourceFd(
					R.raw.beep);
			try {
				mediaPlayer.setDataSource(file.getFileDescriptor(),
						file.getStartOffset(), file.getLength());
				file.close();
				mediaPlayer.setVolume(BEEP_VOLUME, BEEP_VOLUME);
				mediaPlayer.prepare();
			} catch (IOException e) {
				mediaPlayer = null;
			}
		}
	}

	private static final long VIBRATE_DURATION = 200L;

	private void playBeepSoundAndVibrate() {
		if (playBeep && mediaPlayer != null) {
			mediaPlayer.start();
		}
		if (vibrate) {
			Vibrator vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
			vibrator.vibrate(VIBRATE_DURATION);
		}
	}

	/**
	 * When the beep has finished playing, rewind to queue up another one.
	 */
	private final OnCompletionListener beepListener = new OnCompletionListener() {
		@Override
		public void onCompletion(MediaPlayer mediaPlayer) {
			mediaPlayer.seekTo(0);
		}
	};

	public void getCustomerInfo(String qrcode){
		AsyncHttpClient getCustomerClient=new AsyncHttpClient();
		getCustomerClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getCustomerClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=MipSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=MipSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String getCustomerInfourl=mUrl+":"+mPort+"/"+HttpParams.getCustomerUrl;
		//String getCustomerInfourl=mUrl+"/"+HttpParams.getCustomerUrl;
		Log.d("fUrl", getCustomerInfourl);
		RequestParams Customerparams=new RequestParams();
		Log.d("fUrl", getCustomerInfourl);
		Customerparams.put("qrcode", qrcode);//fortest
		getCustomerClient.post(getCustomerInfourl,Customerparams,new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存根
				super.onSuccess(statusCode, response);
				if(pd!=null){
					pd.dismiss();
					pd=null;
				}
				boolean success=false;
				try{
					success=response.getBoolean("success");
					if(success){
						Log.d("fUrl","success is true");
						JSONObject data=response.getJSONObject("data");
						
							String wxplatform=data.getString("wxplatform");
							String wxsku=data.getString("wxsku");//wxsku???
							int points=data.getInt("points");
							JSONArray vouches=data.getJSONArray("vouchers");
							int vouchesAmount=vouches.length();
							String wxtag=data.getString("wxtag");
							int wxclass=data.getInt("wxclass");
							SharedPreferences.Editor qrInfoeditor=MipSharedPreferences.edit();
							qrInfoeditor.putString("wxplatform",wxplatform);//eg:hbtb
							qrInfoeditor.putString("wxsku",wxsku);//eg:balabala123
							qrInfoeditor.putInt("points",points);//45678
							qrInfoeditor.putInt("vouchesAmount",vouchesAmount);//0
							qrInfoeditor.putString("wxtag",wxtag);//eg:太保VIP客户
							qrInfoeditor.putInt("wxclass",wxclass);//1
							qrInfoeditor.commit();
							Log.d("loginActivity",wxplatform+"/"+wxsku+"/"+"points:"
									+points+"/"+"vouchesAmount:"+vouchesAmount+"/"+"wxtag:"+wxtag+"/"+"wxclass:"+
									wxclass);
							
				    		Intent toFuelPay=new Intent(MipcaActivityCapture.this,FuelPay.class);
				    		startActivity(toFuelPay);
				    		finish();
					}else{//客户二维码错误
						//startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
					
						Toast.makeText(getApplicationContext(),"二维码信息错误", Toast.LENGTH_SHORT).show();
						//initCamera(cameraInfoState);
						mTimer=new Timer();//延迟3s
						tTask=new TimerTask(){

							@Override
							public void run() {
								// TODO 自动生成的方法存根
								Message message=Message.obtain();
								message.what=1;
								mHandler.sendMessage(message);
							}
						};
						mTimer.schedule(tTask, 3*1000);
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO 自动生成的方法存根
				super.onFailure(e, errorResponse);
			
				if(pd!=null){
					pd.dismiss();
					pd=null;
				}
				Log.d("loginActivity", "qrscanonFailure");
				Toast.makeText(getApplicationContext(), "网络连接错误", Toast.LENGTH_SHORT).show();
				
			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存根
				super.onFailure(e, errorResponse);
			
				if(pd!=null){
					pd.dismiss();
					pd=null;
				}
				Log.d("loginActivity", "qrscanonFailure");
				Toast.makeText(getApplicationContext(), "网络连接错误", Toast.LENGTH_SHORT).show();
				
			}
			
			
			
		});
		
	}
	
	public void submitOrderByHttp(String payqrcode){
		//Toast.makeText(getApplicationContext(), "submitOrder", Toast.LENGTH_SHORT).show();
		AsyncHttpClient getCustomerClient=new AsyncHttpClient();
		getCustomerClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getCustomerClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=MipSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=MipSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String submiturl=mUrl+":"+mPort+"/"+HttpParams.submitOrderUrl;
		//String submiturl=mUrl+"/"+HttpParams.submitOrderUrl;
		Log.d("fUrl", submiturl);
		RequestParams submitparams=new RequestParams();
		Log.d("fUrl", submiturl);
		String ordertyeStr=MipSharedPreferences.getString("serivceType", "1");
		String ordertype=ordertyeStr;
		int stationId=MipSharedPreferences.getInt("stationid", -1);
		int userId=MipSharedPreferences.getInt("userId", -1);
		String userid=String.valueOf(userId);
		String stationid=String.valueOf(stationId);
		orderno=GenerateOrdeNO.orderIDGenarate(ordertyeStr,stationid);

		String wxplatform=MipSharedPreferences.getString("wxplatform", "");
		String wxsku=MipSharedPreferences.getString("wxsku", "");
		String voucher="";
		String voucheramount="0";
		String payamount=realPayStr;//bundle
		MipSharedPreferences=getSharedPreferences("cashiervalues",0);
		String cardNumber=MipSharedPreferences.getString("cardsNumber", "");
		
		
		submitparams.put("orderno", orderno);
		submitparams.put("ordertype", ordertype);
		submitparams.put("userid", userid);
		submitparams.put("stationid", stationid);
		submitparams.put("wxplatform", wxplatform);
		submitparams.put("wxsku", wxsku);
		submitparams.put("cardno", cardNumber);
		//amountStr="100";
		//pointsStr="600";
		//pointsamountStr="60";
		
		submitparams.put("amount", amountStr);//bundle
		submitparams.put("points", pointsStr);//bundle
		submitparams.put("pointsamount", pointsamountStr);//bundle
		Log.d("MipcaActivity", "realPay"+realPayStr+"/"+"amount"+amountStr+"/"+"points"+pointsStr+"/"+"pointsamount"+pointsamountStr);
		submitparams.put("voucher", voucher);
		submitparams.put("voucheramount", voucheramount);
		submitparams.put("payamount", payamount);
		submitparams.put("qrcode", payqrcode);//fortest
		getCustomerClient.post(submiturl,submitparams,new JsonHttpResponseHandler(){
 
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存�?
				super.onSuccess(statusCode, response);
				Log.d(TAG, "onSuccess");
				if(pd!=null){
					pd.dismiss();
					pd=null;
				}

				boolean success=false;
				try{
					success=response.getBoolean("success");
					if(success){
						Log.d("fUrl","success is true");
						
						JSONObject data=response.getJSONObject("data");
						String paystate=data.getString("paystate");
						if(paystate.equals("USERPAYING")){
							Long orderId=data.getLong("orderid");
							String orderid="0";//默认
							if(orderId!=null){
							 orderid=String.valueOf(orderId);
							}
							SharedPreferences.Editor mscanpayeditor=MipSharedPreferences.edit();
							mscanpayeditor.putString("orderid", orderid);
							mscanpayeditor.commit();
							Bundle bundle=new Bundle();
							bundle.putString("activitytype", "1");
							bundle.putString("orderno", orderno);
							Intent toSuccessPaying=new Intent(MipcaActivityCapture.this,SuccessResultActivity.class);
							toSuccessPaying.putExtras(bundle);
							startActivity(toSuccessPaying);
							FuelPay.mFuelPay.finish();
							finish();
							//Toast.makeText(MipcaActivityCapture.this, "userpaying", Toast.LENGTH_SHORT).show();
						}else{
							Long orderId=data.getLong("orderid");
							if(orderId==null){
								Log.e(TAG, "orderid is null");
							}
							String orderid=String.valueOf(orderId);
							SharedPreferences.Editor scanpayeditor=MipSharedPreferences.edit();
							scanpayeditor.putString("orderid", orderid);
							scanpayeditor.commit();
							getOrderDetail(orderno);
						}
							
							
							//printByHttp();
				    		//Intent toPrint=new Intent(ScannerPayCodeRealActivity.this,PrtintTest.class);
							
					}else{//
						//startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
						JSONObject err=(JSONObject) response.get("err");
						String name=err.getString("name");
						Log.e(TAG, "name:"+name);
						Toast.makeText(getApplicationContext(),"提交失败"+name, Toast.LENGTH_SHORT).show();
						//initCamera(cameraInfoState);
						mTimer=new Timer();//延迟3s
						tTask=new TimerTask(){

							@Override
							public void run() {
								// TODO 自动生成的方法存根
								Message message=Message.obtain();
								message.what=1;
								mHandler.sendMessage(message);
							}
						};
						mTimer.schedule(tTask, 3*1000);
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO 自动生成的方法存�?
				super.onFailure(e, errorResponse);
				Log.d("loginActivity", "qrscanonFailure");
				if(pd!=null){
					pd.dismiss();
				}

			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存�?
				super.onFailure(e, errorResponse);
				Log.d("loginActivity", "qrscanonFailure");
				if(pd!=null){
					pd.dismiss();
				}

			}
			
			
			
		});
		
	}
	
	public void getOrderDetail(String orderno){
		Log.d(TAG, "getOrderDetail");
		AsyncHttpClient getOrderHttpClient=new AsyncHttpClient();
		getOrderHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getOrderHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=MipSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=MipSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String orderDetailUrl=mUrl+":"+mPort+"/"+HttpParams.getOrderDetailUrl;
		//String orderDetailUrl=mUrl+"/"+HttpParams.getOrderDetailUrl;
		RequestParams getOrderparams=new RequestParams();
		getOrderparams.put("orderno", orderno);
		getOrderHttpClient.post(orderDetailUrl, getOrderparams, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存�?
				super.onSuccess(statusCode, response);
				boolean success=false;
					try{
						success=response.getBoolean("success");
						if(success){
							Log.e(TAG, "SPC onSuccess is true");
							JSONArray data=response.getJSONArray("data");
							if(data.length()>0){
								Log.d(TAG, "datalength>0");
								JSONObject mData=(JSONObject) data.get(0);
								int id=mData.getInt("id");
								String ordernoStr=mData.getString("orderno");
								String orderdate=mData.getString("orderdate");
								int orderType=mData.getInt("ordertype");
								String orderTypeStr=String.valueOf(orderType);
								int userid=mData.getInt("userid");
								String useridStr=String.valueOf(userid);
								int stationid=mData.getInt("stationid");
								String stationidStr=String.valueOf(stationid);
								String wxplatform=mData.getString("wxplatform");
								String wxsku=mData.getString("wxsku");
								double amountInt=mData.getDouble("amount");
								String amountStr=String.valueOf(amountInt);
								double points=mData.getDouble("points");
								String pointsStr=String.valueOf(points);
								double pointsamount=mData.getDouble("pointsamount");
								String pointsamountStr=String.valueOf(pointsamount);
								String voucherStr=mData.getString("voucher");
								
								int voucheramount=mData.getInt("voucheramount");
								String voucheramountStr=String.valueOf(voucheramount);
								double payamount=mData.getDouble("payamount");
								String payamountStr=String.valueOf(payamount);
								String payment=mData.getString("payment");
								int state=mData.getInt("state");
								String stateStr=String.valueOf(state);
								String qrcode=mData.getString("qrcode");
								
								Bundle bundle=new Bundle();
								bundle.putString("orderno", ordernoStr);
								bundle.putString("orderdate", orderdate);
								bundle.putString("stationid", stationidStr);//change to name!!!!
								bundle.putString("amount", amountStr);
								bundle.putString("payment", payment);
								bundle.putString("points", pointsStr);
								bundle.putString("pointsamount", pointsamountStr);
								bundle.putString("voucheramount", voucheramountStr);//null
								bundle.putString("payamount", payamountStr);
								bundle.putString("activitytype", "2");
						
								Intent toSuccess=new Intent(MipcaActivityCapture.this,SuccessResultActivity.class);
								FuelPay.mFuelPay.finish();//finish fuelPay
								toSuccess.putExtras(bundle);
					    		startActivity(toSuccess);
					    		
					    		MipcaActivityCapture.this.finish();
							}else{
								Toast.makeText(MipcaActivityCapture.this, "数据异常", Toast.LENGTH_SHORT).show();
								Log.d(TAG, "data's length is0");
							}
						}else{
							Log.d(TAG, "success is false");
							Toast.makeText(MipcaActivityCapture.this, "网络异常", Toast.LENGTH_SHORT).show();
						}
						
					}catch(JSONException e){
						e.printStackTrace();
					}

			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO 自动生成的方法存�?
				Log.e(TAG, "SPC onFailure json");
				super.onFailure(e, errorResponse);
				if(pd!=null){
					pd.dismiss();
					pd=null;
					Toast.makeText(MipcaActivityCapture.this, "网络错误", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存�?
				Log.e(TAG, "SPC onFailure string");
				super.onFailure(e, errorResponse);
				
				if(pd!=null){
					pd.dismiss();
					pd=null;
					Toast.makeText(MipcaActivityCapture.this, "网络错误", Toast.LENGTH_SHORT).show();
				}
				
			}
			
			
		});
	}
	
}