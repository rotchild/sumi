package com.cx.sumicashersystem.myservice;

import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import net.sourceforge.zbar.Config;
import net.sourceforge.zbar.Image;
import net.sourceforge.zbar.ImageScanner;
import net.sourceforge.zbar.Symbol;
import net.sourceforge.zbar.SymbolSet;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.CameraInfo;
import android.hardware.Camera.PreviewCallback;
import android.hardware.Camera.Size;
import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.utils.GenerateOrdeNO;
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class ScannerPayCodeRealActivity extends Activity {
   
	private static final String TAG = "ScannerPayCodeRealActivity";
	public static Camera mCamera;
	public static boolean isScanOpen = false;
	//private CameraPreview mPreview;
	private Handler autoFocusHandler;
	ImageScanner scanner;
	private boolean previewing = true;
	boolean isStartScan = false;
	FrameLayout preview;
	String orderno="";
	String amountStr="";
	String pointsStr="";
	String pointsamountStr="";
	//"realPayStr"
	String realPayStr="";
	MediaPlayer player;
	ProgressDialog pd;
	Timer mTimer;
	TimerTask tTask;
	Handler dealerHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存�?
			super.handleMessage(msg);
			int msgId=msg.what;
			switch(msgId){
			case 1:
				if(pd!=null){
					pd.dismiss();
					pd=null;
					Toast.makeText(ScannerPayCodeRealActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
				}
				break;
			default:
				break;
			}
		}
		
	};
	/**
	 * 摄像头位�??
	 */
	int cameraInfoState = 0;
	/**
	 * 条码信息
	 */
	private String codeMessage = null;
	
	SharedPreferences ScannerPayCodeSharedPreferences;//mSP

	/**
	 * 解码�??
	 */
	static {
		System.loadLibrary("iconv");
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		player = MediaPlayer.create(getApplicationContext(), R.raw.beep);
		
		// 去掉标题�??
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		// 全屏
		Bundle bundle=this.getIntent().getExtras();
		amountStr=bundle.getString("amountStr");
		pointsStr=bundle.getString("pointsStr");
		pointsamountStr=bundle.getString("pointsamountStr");
		realPayStr=bundle.getString("realPayStr");
		ScannerPayCodeSharedPreferences=getSharedPreferences("cashiervalues",0);//get SP	
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		Window window = getWindow();
		window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.zbar_capture);

		preview = (FrameLayout) findViewById(R.id.cameraPreview);

		isScanOpen = true;

		ImageView imageView_change = (ImageView) findViewById(R.id.imageView_change);
		imageView_change.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (cameraInfoState == CameraInfo.CAMERA_FACING_BACK) {
					cameraInfoState = CameraInfo.CAMERA_FACING_FRONT;
				} else {
					cameraInfoState = CameraInfo.CAMERA_FACING_BACK;
				}
				initCamera(cameraInfoState);
			}
		});

		if (android.os.Build.VERSION.SDK_INT > Build.VERSION_CODES.GINGERBREAD) {// SDK版本必须大于2.3
			int cameraCount = Camera.getNumberOfCameras();
			if (cameraCount < 2) {// 多余两个摄像头，显示切换按钮
				imageView_change.setVisibility(View.GONE);
			}
		}
	}

	@Override
	protected void onResume() {
		/*if (CameraCheck.hasBackFacingCamera()) {
			cameraInfoState = CameraInfo.CAMERA_FACING_BACK;
		} else if (CameraCheck.hasFrontFacingCamera()) {
			cameraInfoState = CameraInfo.CAMERA_FACING_FRONT;
		} else {
			Toast.makeText(getApplicationContext(),
				"无法连接到摄像头\r\nUnable to connect to the camera", 300).show();
			finish();
		}*/
		initCamera(cameraInfoState);
		super.onResume();
	}

	private void initCamera(int cameraInfo) {
		isStartScan = true;
		try {
			setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

			autoFocusHandler = new Handler();

			/* Instance barcode scanner */
			scanner = new ImageScanner();
			scanner.setConfig(0, Config.X_DENSITY, 3);
			scanner.setConfig(0, Config.Y_DENSITY, 3);
			preview.removeAllViews();

			mCamera = Camera.open(cameraInfo);

			//mPreview = new CameraPreview(this, mCamera, previewCb, autoFocusCB);

			//preview.addView(mPreview);

			mCamera.setPreviewCallback(previewCb);

			mCamera.startPreview();
			previewing = true;
			mCamera.autoFocus(autoFocusCB);
			
			mCamera.setDisplayOrientation(90);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onPause() {
		releaseCamera();
		isScanOpen = false;
		super.onPause();
	}

	@Override
	protected void onDestroy() {
		isScanOpen = false;
		if(pd!=null){
			pd.dismiss();
			pd=null;
		}
		if(mTimer!=null){
			if(tTask!=null){//relate to mTimer???
			tTask.cancel();
			tTask=null;
			}
			mTimer.cancel();
			mTimer=null;
		}
		super.onDestroy();
	}

	private void releaseCamera() {
		if (mCamera != null) {
			isStartScan = false;
			previewing = false;
			mCamera.stopPreview();
		}
	}

	// 自动对焦
	private Runnable doAutoFocus = new Runnable() {
		@Override
		public void run() {
			if (previewing)
				mCamera.autoFocus(autoFocusCB);
		}
	};

	PreviewCallback previewCb = new PreviewCallback() {
		@Override
		public void onPreviewFrame(byte[] data, Camera camera) {
			Camera.Parameters parameters = camera.getParameters();
			Size size = parameters.getPreviewSize();
			if (isStartScan) {
				Image barcode = new Image(size.width, size.height, "Y800");
				barcode.setData(data);

				int result = scanner.scanImage(barcode);

				if (result != 0) {

					previewing = false;
					// 获取扫描数据
					SymbolSet syms = scanner.getResults();
					for (Symbol sym : syms) {
						codeMessage = sym.getData();
						if (codeMessage != "") {
							isStartScan = false;
							/*Toast.makeText(getApplicationContext(),
									codeMessage, 300).show();*/
							/*Intent dataIntent = new Intent();
							dataIntent.putExtra(ZBarConstants.SCAN_RESULT,
									codeMessage);
							dataIntent.putExtra(ZBarConstants.SCAN_RESULT_TYPE,
									sym.getType());
							setResult(Activity.RESULT_OK, dataIntent);*/
							player.start();
							//finish();
							String reg="\\d{18}";
							boolean matchResult=codeMessage.matches(reg);
							if(matchResult){
								pd=ProgressDialog.show(ScannerPayCodeRealActivity.this, "请稍�?", "数据提交�?");
								mTimer=new Timer();
								tTask=new TimerTask(){

									@Override
									public void run() {
										// TODO 自动生成的方法存�?
										Message message=new Message();
										message.what=1;
										dealerHandler.sendMessage(message);
									}
									
								};
								//mTimer.schedule(tTask, HttpParams.DEFAULT_TIME_OUT);
								
								submitOrderByHttp(codeMessage);
								break;
							}else{
								Toast.makeText(ScannerPayCodeRealActivity.this,"二维码信息错�?", Toast.LENGTH_SHORT).show();
								initCamera(cameraInfoState);
								break;
								
							}

						}
					}
				}
			}
		}
	};

	/**
	 * 自动对焦回调
	 */
	AutoFocusCallback autoFocusCB = new AutoFocusCallback() {
		@Override
		public void onAutoFocus(boolean success, Camera camera) {
			autoFocusHandler.postDelayed(doAutoFocus, 1000);
		}
	};
	
	public void submitOrderByHttp(String payqrcode){
		//Toast.makeText(getApplicationContext(), "submitOrder", Toast.LENGTH_SHORT).show();
		AsyncHttpClient getCustomerClient=new AsyncHttpClient();
		getCustomerClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getCustomerClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=ScannerPayCodeSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=ScannerPayCodeSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String submiturl=mUrl+":"+mPort+"/"+HttpParams.submitOrderUrl;
		//String submiturl=mUrl+"/"+HttpParams.submitOrderUrl;
		Log.d("fUrl", submiturl);
		RequestParams submitparams=new RequestParams();
		Log.d("fUrl", submiturl);
		
		String ordertyeStr=ScannerPayCodeSharedPreferences.getString("serivceType", "1");
		String ordertype=ordertyeStr;
		int userId=ScannerPayCodeSharedPreferences.getInt("userId", -1);
		int stationId=ScannerPayCodeSharedPreferences.getInt("stationid", -1);
		String userid=String.valueOf(userId);
		String stationid=String.valueOf(stationId);
		orderno=GenerateOrdeNO.orderIDGenarate(ordertyeStr,stationid);
		String wxplatform=ScannerPayCodeSharedPreferences.getString("wxplatform", "");
		String wxsku=ScannerPayCodeSharedPreferences.getString("wxsku", "");
		String voucher="";
		String voucheramount="0";
		String payamount=realPayStr;//bundle
		submitparams.put("orderno", orderno);
		submitparams.put("ordertype", ordertype);
		submitparams.put("userid", userid);
		submitparams.put("stationid", stationid);
		submitparams.put("wxplatform", wxplatform);
		submitparams.put("wxsku", wxsku);
		
		//amountStr="100";
		//pointsStr="600";
		//pointsamountStr="60";
		
		submitparams.put("amount", amountStr);//bundle
		submitparams.put("points", pointsStr);//bundle
		submitparams.put("pointsamount", pointsamountStr);//bundle
		Log.d(TAG, "realPay"+realPayStr+"/"+"amount"+amountStr+"/"+"points"+pointsStr+"/"+"pointsamount"+pointsamountStr);
		submitparams.put("voucher", voucher);
		submitparams.put("voucheramount", voucheramount);
		submitparams.put("payamount", payamount);
		submitparams.put("qrcode", payqrcode);//fortest
		getCustomerClient.post(submiturl,submitparams,new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存�?
				super.onSuccess(statusCode, response);
				if(pd!=null){
					pd.dismiss();
					pd=null;
				}
				if(mTimer!=null){
					if(tTask!=null){//relate to mTimer???
					tTask.cancel();
					tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
				}
				boolean success=false;
				try{
					success=response.getBoolean("success");
					if(success){
						Log.d("fUrl","success is true");
						JSONObject data=response.getJSONObject("data");
						
							Long orderId=data.getLong("orderid");
							if(orderId==null){
								Log.e(TAG, "orderid is null");
							}
							String orderid=String.valueOf(orderId);
							SharedPreferences.Editor scanpayeditor=ScannerPayCodeSharedPreferences.edit();
							scanpayeditor.putString("orderid", orderid);
							scanpayeditor.commit();
							getOrderDetail(orderno);
							
							//printByHttp();
				    		//Intent toPrint=new Intent(ScannerPayCodeRealActivity.this,PrtintTest.class);
							
					}else{//客户二维码错�?
						//startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
						JSONObject err=(JSONObject) response.get("err");
						String name=err.getString("name");
						Log.e(TAG, "name:"+name);
						Toast.makeText(getApplicationContext(),"提交失败", Toast.LENGTH_SHORT).show();
						initCamera(cameraInfoState);
						
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
				if(mTimer!=null){
					if(tTask!=null){//relate to mTimer???
					tTask.cancel();
					tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
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
				if(mTimer!=null){
					if(tTask!=null){//relate to mTimer???
					tTask.cancel();
					tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
				}
			}
			
			
			
		});
		
	}
	
	/*public void printByHttp(){
		
	}
	*/
	public void getOrderDetail(String orderno){
		Log.d(TAG, "getOrderDetail");
		AsyncHttpClient getOrderHttpClient=new AsyncHttpClient();
		getOrderHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getOrderHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=ScannerPayCodeSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=ScannerPayCodeSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
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
								int amountInt=mData.getInt("amount");
								String amountStr=String.valueOf(amountInt);
								int points=mData.getInt("points");
								String pointsStr=String.valueOf(points);
								int pointsamount=mData.getInt("pointsamount");
								String pointsamountStr=String.valueOf(pointsamount);
								String voucherStr=mData.getString("voucher");
								
								int voucheramount=mData.getInt("voucheramount");
								String voucheramountStr=String.valueOf(voucheramount);
								int payamount=mData.getInt("payamount");
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
						
								Intent toSuccess=new Intent(ScannerPayCodeRealActivity.this,SuccessResultActivity.class);
								FuelPay.mFuelPay.finish();//finish fuelPay
								toSuccess.putExtras(bundle);
					    		startActivity(toSuccess);
					    		finish();
							}else{
								Log.d(TAG, "data's length is0");
							}
						}else{
							Log.d(TAG, "success is false");
						}
						
					}catch(JSONException e){
						e.printStackTrace();
					}

			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO 自动生成的方法存�?
				super.onFailure(e, errorResponse);
				if(pd!=null){
					pd.dismiss();
					pd=null;
					Toast.makeText(ScannerPayCodeRealActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
				}
			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存�?
				super.onFailure(e, errorResponse);
				if(pd!=null){
					pd.dismiss();
					pd=null;
					Toast.makeText(ScannerPayCodeRealActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
				}
				
			}
			
			
		});
	}
	
}
