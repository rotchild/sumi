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
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;


public class ZBarScannerActivity extends Activity {

	private static final String TAG = "ZBarScannerActivity";
	public static Camera mCamera;
	public static boolean isScanOpen = false;
	//private CameraPreview mPreview;
	private Handler autoFocusHandler;
	ImageScanner scanner;
	private boolean previewing = true;
	boolean isStartScan = false;
	FrameLayout preview;
	
	MediaPlayer player;
	ProgressDialog pd;//等待�?
	Timer mTimer;//未连接成�?
	TimerTask tTask;
	private Handler dealerHandler=new Handler(){

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
					Toast.makeText(ZBarScannerActivity.this, "网络错误", Toast.LENGTH_SHORT).show();
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
	
	SharedPreferences ZBarScannerSharedPreferences;//mSP

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
		ZBarScannerSharedPreferences=getSharedPreferences("cashiervalues",0);//get SP	
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
/*		if (CameraCheck.hasBackFacingCamera()) {
			cameraInfoState = CameraInfo.CAMERA_FACING_BACK;
		} else if (CameraCheck.hasFrontFacingCamera()) {
			cameraInfoState = CameraInfo.CAMERA_FACING_FRONT;
		} else {
			Toast.makeText(getApplicationContext(),
				"无法连接到摄像头\r\nUnable to connect to the camera", 300).show();
			finish();
		}
		initCamera(cameraInfoState);*/
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
							pd=ProgressDialog.show(ZBarScannerActivity.this, "请等�?","加载�?...");
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
							getCustomerInfo(codeMessage);
							break;
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
	
	public void getCustomerInfo(String qrcode){
		AsyncHttpClient getCustomerClient=new AsyncHttpClient();
		getCustomerClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getCustomerClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=ZBarScannerSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=ZBarScannerSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String getCustomerInfourl=mUrl+":"+mPort+"/"+HttpParams.getCustomerUrl;
		Log.d("fUrl", getCustomerInfourl);
		RequestParams Customerparams=new RequestParams();
		Log.d("fUrl", getCustomerInfourl);
		Customerparams.put("qrcode", qrcode);//fortest
		getCustomerClient.post(getCustomerInfourl,Customerparams,new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存�?
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
							SharedPreferences.Editor qrInfoeditor=ZBarScannerSharedPreferences.edit();
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
							
				    		Intent toFuelPay=new Intent(ZBarScannerActivity.this,FuelPay.class);
				    		startActivity(toFuelPay);
				    		finish();
					}else{//客户二维码错�?
						//startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
						
						Toast.makeText(getApplicationContext(),"二维码信息错�?", Toast.LENGTH_SHORT).show();
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
			
				if(pd!=null){
					pd.dismiss();
					pd=null;
				}
				Log.d("loginActivity", "qrscanonFailure");
				Toast.makeText(getApplicationContext(), "网络连接错误", Toast.LENGTH_SHORT).show();
				
			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存�?
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
}
