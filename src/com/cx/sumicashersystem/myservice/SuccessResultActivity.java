package com.cx.sumicashersystem.myservice;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.utils.TimeStampToTime;
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class SuccessResultActivity extends Activity {
	/*bundle.putString("orderno", ordernoStr);
	bundle.putString("orderdate", orderdate);
	bundle.putString("stationid", stationidStr);//change to name!!!!
	bundle.putString("amount", amountStr);
	bundle.putString("payment", payment);
	bundle.putString("points", pointsStr);
	bundle.putString("voucheramount", voucheramountStr);//null
	bundle.putString("payamount", payamountStr);*/
	private IWoyouService woyouService;
	private ICallback callback = null;
	private ICallback mcallback = null;
    CountDownTimer cdtimer=null;
     
   // long currentTime=0;
    int count=0;
	String orderno="";
	String orderdate="";
	String orderdateShow="";
	String stationidStr="";
	String amount="";
	String payment="";
	String points="";
	String pointsAmount="";
	String voucheramount="";
	String payamount="";
	String stationName="";
	String activitytype="";
	TextView orderNoTv,orderDateTv,stationidTv,amountTv,paymentTv,pointsTv,
	voucheramountTv,payamountTv,successLabelTv;
	Button successFinishBt;
	SharedPreferences successResultSharedPreferences;
	ProgressDialog printPd,printWaitPd,payingPd;
	private static final String TAG = "PrintTest";
	private static final int PRINTEFINISH=1;
	private static final int SHOWDIALOG=2;
	private static final int PAYSUCCESS=3;
	private static final int PRINTFAIL=4;
	//private String device = "/dev/ttyMT0";
	//private int baudrate = 115200;
	TimerTask mTimerTask;
	Timer timer;
	Handler mHandler=new Handler(){

		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			super.handleMessage(msg);
			int msgId=msg.what;
			switch(msgId){
			case SHOWDIALOG:
				//printWaitPd=ProgressDialog.show(SuccessResultActivity.this, "打印中", "请稍候...");
				break;
			case PRINTEFINISH:
				if(printWaitPd!=null){
					printWaitPd.dismiss();
					printWaitPd=null;
				}
				break;
				
			case PAYSUCCESS:
				if(payingPd!=null){
					payingPd.dismiss();
					payingPd=null;
				}
				//cdtimer
				if(cdtimer!=null){
					cdtimer.cancel();
					cdtimer=null;
				}
				printByHttp();
				initView();
				break;
			case PRINTFAIL:
				if(printWaitPd!=null){
					printWaitPd.dismiss();
					printWaitPd=null;
				}
			Toast.makeText(SuccessResultActivity.this, "打印失败,请再次打印", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			
			}
			
		}
		
	};
	
	private ServiceConnection connService = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {

			woyouService = null;
		}

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			woyouService = IWoyouService.Stub.asInterface(service);
			//setButtonEnable(true);
		}
	};
	
	
	//PrinterClassSerialPort3505 printerClass = null;
/*	Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {
			Log.e(TAG, "enter handleMessage what"+msg.what);
			switch (msg.what) {
			case PrinterCommand.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				if(readBuf[0] == 0x08){
					Toast.makeText(getApplicationContext(),
							"打印缺纸", Toast.LENGTH_SHORT).show();
				}else if(readBuf[0] == 0x01){
					printPd=ProgressDialog.show(SuccessResultActivity.this, "请等候", "打印中...");
				}
				if(printPd!=null){
					printPd.dismiss();
					printPd=null;
				}
				Log.i(TAG, "readBuf:" + readBuf[0]);
				break;
			case PrinterCommand.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case PrinterCommand.STATE_CONNECTED:
					break;
				case PrinterCommand.STATE_CONNECTING:
					Toast.makeText(getApplicationContext(),
							"STATE_CONNECTING", Toast.LENGTH_SHORT).show();
					break;
				case PrinterCommand.STATE_LISTEN:
				case PrinterCommand.STATE_NONE:
					break;
				case PrinterCommand.SUCCESS_CONNECT:
					//printerClass.write(new byte[] { 0x1b, 0x2b });
					Toast.makeText(getApplicationContext(),
							"SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
					break;
				case PrinterCommand.FAILED_CONNECT:
					Toast.makeText(getApplicationContext(),
							"FAILED_CONNECT", Toast.LENGTH_SHORT).show();

					break;
				case PrinterCommand.LOSE_CONNECT:
					Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case PrinterCommand.MESSAGE_WRITE:

				break;
			case PrinterCommand.PERMIT_PRINTER:
				String result = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"允许打印", Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.FORBID_PRINTER:
				String forbid_print = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"禁止打印", Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.TIMEOUT_PRINTER:
				String print_timeout = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"开启打印功能", Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}
	};*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.successresult_layout);
		Bundle bundle=this.getIntent().getExtras();
		activitytype=bundle.getString("activitytype");
		if(activitytype.equals("1")){
			orderno=bundle.getString("orderno");
			successResultSharedPreferences=getSharedPreferences("cashiervalues",0);
			//Toast.makeText(SuccessResultActivity.this, "userpaying", Toast.LENGTH_SHORT).show();
			payingPd=ProgressDialog.show(SuccessResultActivity.this, "查询中", "请稍候...");
			cdtimer=new CountDownTimer(50*1000,5*1000){

				@Override
				public void onTick(long millisUntilFinished) {
					// TODO 自动生成的方法存根
					SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
					String timeStr=formatter.format(new Date());
					Log.d(TAG, "currentTime"+timeStr+"/"+"count"+count);
					
					if(count>0){
						//Log.d(TAG, "currentTime"+(System.currentTimeMillis()-currentTime)+"/"+"count"+count);
					//	System.out.println("currentTime"+(System.currentTimeMillis()-currentTime)+"/"+"count"+count);
					//	System.out.println("1");
						//currentTime=System.currentTimeMillis();
						Log.d(TAG, "getOrderFinishByHttp");
						getOrderFinishByHttp(orderno);
					}
					count++;

				}

				@Override
				public void onFinish() {
					// TODO 自动生成的方法存根
					 SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
						String timeStr=formatter.format(new Date());
					  Log.d(TAG, "onFinish"+"currentTime"+timeStr);
					Intent toFail=new Intent(SuccessResultActivity.this,FailResultActivity.class);
					startActivity(toFail);
					SuccessResultActivity.this.finish();
				}
				
			};
			
  cdtimer.start();
  SimpleDateFormat formatter=new SimpleDateFormat("yyyy-MM-dd-HH:mm:ss");
	String timeStr=formatter.format(new Date());
  Log.d(TAG, "onStart"+"currentTime"+timeStr);
			
		}else if(activitytype.equals("2")){
			orderno=bundle.getString("orderno");
			orderdate=bundle.getString("orderdate");
			orderdateShow=TimeStampToTime.getTime(orderdate);
			stationidStr=bundle.getString("stationid");//staionid
			
			amount=bundle.getString("amount");
			payment=bundle.getString("payment");
			points=bundle.getString("points");
			pointsAmount=bundle.getString("pointsamount");
			voucheramount=bundle.getString("voucheramount");
			payamount=bundle.getString("payamount");
			
			successResultSharedPreferences=getSharedPreferences("cashiervalues",0);
			//printerClass = new PrinterClassSerialPort3505(device, baudrate, mhandler);
			printByHttp();
			stationName=successResultSharedPreferences.getString("stationName", "");//stationName
			initView();
		}
		
	}
	private void initView(){
		successLabelTv=(TextView)findViewById(R.id.success_label);
		orderNoTv=(TextView)findViewById(R.id.ordernumVal);
		orderDateTv=(TextView)findViewById(R.id.createTimeVal);
		stationidTv=(TextView)findViewById(R.id.sellerVal);
		amountTv=(TextView)findViewById(R.id.fuelMoneyVal);
		paymentTv=(TextView)findViewById(R.id.payMethodVal);
		pointsTv=(TextView)findViewById(R.id.memberPointVal);
		voucheramountTv=(TextView)findViewById(R.id.vouchesVal);
		payamountTv=(TextView)findViewById(R.id.realPayVal);
		
		successLabelTv.setText("交易成功");
		orderNoTv.setText(orderno);
		orderDateTv.setText(orderdateShow);
		stationidTv.setText(stationName);
		amountTv.setText(amount);
		if(payment==null||payment.length()<=0||payment.equals("null")){
			paymentTv.setText("");
		}else{
			paymentTv.setText(payment);
		}
		
		pointsTv.setText(points+"分"+"(-"+pointsAmount+")");
		voucheramountTv.setText(voucheramount);
		payamountTv.setText(payamount);
		
		successFinishBt=(Button) findViewById(R.id.success_finish);
		successFinishBt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				SuccessResultActivity.this.finish();
			}
			
		});
Button rePrintbt=(Button)findViewById(R.id.reprint);
rePrintbt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		Bundle bundle=new Bundle();
		bundle.putString("orderno", orderno);
		bundle.putString("orderdateShow", orderdateShow);
		bundle.putString("stationName", stationName);
		bundle.putString("amount",amount);
		bundle.putString("payment",payment);
		bundle.putString("points",points);
		bundle.putString("pointsAmount",pointsAmount);
		bundle.putString("voucheramount",voucheramount);
		bundle.putString("payamount", payamount);
		Intent toApplyReprint=new Intent(SuccessResultActivity.this,ApplyRePrintActivity.class);
		toApplyReprint.putExtras(bundle);
		startActivity(toApplyReprint);
	}
	
});

callback = new ICallback.Stub() {
	
	@Override
	public void onRunResult(final boolean success) throws RemoteException {
		if(success){
			
		}else{
			runOnUiThread(new Runnable(){
				@Override
				public void run() {
				//	info.append("onRaiseException = " + msg + "\n");
					if(printWaitPd!=null){
						printWaitPd.dismiss();
						printWaitPd=null;
					}
					Toast.makeText(SuccessResultActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
				}});
			
			/*Message message=Message.obtain();
			message.what=PRINTFAIL;
			mHandler.sendMessage(message);*/
			Log.d("successresult", "fail");
	/*		if(printWaitPd!=null){
				printWaitPd.dismiss();
				printWaitPd=null;
			}
		Toast.makeText(SuccessResultActivity.this, "打印失败,请再次打印", Toast.LENGTH_SHORT).show();*/
		}
	}
	
	@Override
	public void onReturnString(final String value) throws RemoteException {
		Log.i(TAG,"printlength:" + value + "\n");						
	}
	
	@Override
	public void onRaiseException(int code, final String msg) throws RemoteException {
		Log.i(TAG,"onRaiseException: " + msg);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
			//	info.append("onRaiseException = " + msg + "\n");
				if(printWaitPd!=null){
					printWaitPd.dismiss();
					printWaitPd=null;
				}
				Toast.makeText(SuccessResultActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
			}});
		
	}
};

mcallback = new ICallback.Stub() {
	
	@Override
	public void onRunResult(final boolean success) throws RemoteException {
		Log.d(TAG,"onRunResult" + success + "\n");
		if(printWaitPd!=null){
			printWaitPd.dismiss();
			printWaitPd=null;
		}
		//Toast.makeText(SuccessResultActivity.this, "finish", Toast.LENGTH_SHORT).show();//在callback中无效
		if(success){
			Message message=Message.obtain();
			message.what=PRINTEFINISH;
			mHandler.sendMessage(message);	
		}else{
			Message message=Message.obtain();
			message.what=PRINTFAIL;
			mHandler.sendMessage(message);	
		}

	}
	
	@Override
	public void onReturnString(final String value) throws RemoteException {
		Log.i(TAG,"printlength:" + value + "\n");						
	}
	
	@Override
	public void onRaiseException(int code, final String msg) throws RemoteException {
		Log.i(TAG,"onRaiseException: " + msg);
		runOnUiThread(new Runnable(){
			@Override
			public void run() {
			//	info.append("onRaiseException = " + msg + "\n");
				if(printWaitPd!=null){
					printWaitPd.dismiss();
					printWaitPd=null;
				}
				Toast.makeText(SuccessResultActivity.this, "打印失败", Toast.LENGTH_SHORT).show();
			}});
		
	}
};

Intent intent=new Intent();
intent.setPackage("woyou.aidlservice.jiuiv5");
intent.setAction("woyou.aidlservice.jiuiv5.IWoyouService");
startService(intent);
bindService(intent, connService, Context.BIND_AUTO_CREATE);
	}
	
	@Override
	protected void onDestroy() {
		// TODO 自动生成的方法存根
		try{
			unbindService(connService);					
		}catch(Exception e){
			e.printStackTrace();
		}
		super.onDestroy();
	}
	
	public void getOrderFinishByHttp(String morderno){
		AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
		asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=successResultSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=successResultSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String orderFinishUrl=mUrl+":"+mPort+"/"+HttpParams.getOrderFinish;
		
		RequestParams params=new RequestParams();
		params.put("orderno", morderno);
		asyncHttpClient.post(orderFinishUrl, params, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(JSONObject response) {
				// TODO 自动生成的方法存根
				super.onSuccess(response);
				boolean success=false;
				try{
					success=response.getBoolean("success");
					if(success){
						JSONArray data=response.getJSONArray("data");
						if(data==null||data.length()<=0){
							
						}else if(data!=null&&data.length()>0){
							//mHandler
							JSONObject data0=(JSONObject) data.get(0);
							orderno=data0.getString("orderno");
							orderdate=data0.getString("orderdate");
							orderdateShow=TimeStampToTime.getTime(orderdate);
							int stationidint=data0.getInt("stationid");
							stationidStr=String.valueOf(stationidint);
							stationName=successResultSharedPreferences.getString("stationName", "");
							double mamount=data0.getDouble("amount");
							amount=String.valueOf(mamount);
							double mpoints=data0.getDouble("points");
							points=String.valueOf(mpoints);
							double mpointsamount=data0.getDouble("pointsamount");
							pointsAmount=String.valueOf(mpointsamount);
							double mvouchersamount=data0.getDouble("voucheramount");
							voucheramount=String.valueOf(mvouchersamount);
	
							double mPayamount=data0.getDouble("payamount");
							payamount=String.valueOf(mPayamount);
							payment=data0.getString("payment");
							
							
							Message message=Message.obtain();
							message.what=PAYSUCCESS;
							mHandler.sendMessage(message);
						}

					}else{
						Toast.makeText(SuccessResultActivity.this, "success is false", Toast.LENGTH_SHORT).show();
					}
					
				}catch(JSONException e){
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存根
				super.onFailure(e, errorResponse);
			}

			
			
		});
	}
	
	
	public void printByHttp(){
		AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
		asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=successResultSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=successResultSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String printUrl=mUrl+":"+mPort+"/"+HttpParams.printReceiptUrl;
		int userid=successResultSharedPreferences.getInt("userId", -1);
		String orderid=successResultSharedPreferences.getString("orderid", "");
		String printtype="1";//第一次打印
		RequestParams params=new RequestParams();
		params.put("orderid", orderid);
		Log.d(TAG, "orderid:"+orderid);
		params.put("printtype", printtype);
		Log.d(TAG, "printtype:"+printtype);
		params.put("userid", String.valueOf(userid));
		params.put("remark", "");
		asyncHttpClient.post(printUrl, params, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(JSONObject response) {
				// TODO 自动生成的方法存根
				super.onSuccess(response);
				boolean success=false;
				String serialnoStr="";
				try{
					success=response.getBoolean("success");
					if(success){
						JSONObject data=(JSONObject) response.get("data");
						if(data!=null){
							serialnoStr=data.getString("serialno");
							/*Message showDialogM=Message.obtain();
							showDialogM.what=SHOWDIALOG;
							mHandler.sendMessage(showDialogM);*/
							print(serialnoStr);
						}
					}else{
						Toast.makeText(getApplicationContext(), "已打印",Toast.LENGTH_SHORT).show();
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
	
	public void print(String serialnoStr){
		printText(serialnoStr);
	//	openDevice();
		//Bitmap blankBit=BitmapFactory.decodeResource(getResources(), R.drawable.printpic);
		//printerClass.printImage(blankBit);
		//blankBit.recycle();
		String printSerialappend=GetPrintStr(serialnoStr);
		//printUTF8Text(printSerialappend);
	}
	
/*	public void openDevice(){
		if(printerClass.mSerialPort.isOpen){
			
		}else{
			printerClass.device = device;
			printerClass.baudrate = baudrate;
			printerClass.open();
			//printerClass.write(new byte[] { 0x1b, 0x76 });
		}
	}*/
	
/*	private void printUTF8Text(String printSerical) {
		
		try {
			printerClass.printText(printSerical);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}*/
	
	public void printText(final String serialnoStr){
		printWaitPd=ProgressDialog.show(SuccessResultActivity.this, "打印中", "请稍候...");
		mTimerTask=new TimerTask(){

			@Override
			public void run() {
				// TODO 自动生成的方法存根
				if(printWaitPd!=null&&printWaitPd.isShowing()){
					printWaitPd.dismiss();
				}
			}
			
		};
		timer=new Timer();
		timer.schedule(mTimerTask, 6*1000);
		
		successResultSharedPreferences=getSharedPreferences("cashiervalues",0);
		final String serviceTypeStr=successResultSharedPreferences.getString("serivceType", "1");
		ThreadPoolManager.getInstance().executeTask(new Runnable(){

			@Override
			public void run() {
				
				try {
					int maxLength=30;
					int spaceFontSize=38;
					int contentFontSize=24;
					String line="---------------------";
					String blank="                     ";
					woyouService.setFontSize(50, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printText("交易凭证\n", callback);
					
					woyouService.setFontSize(36, callback);
					woyouService.setAlignment(1, callback);
					woyouService.lineWrap(1, callback);
					if(serviceTypeStr.equals("2")){
				    woyouService.printText("购物金额："+spaceNumber(4)+amount, callback);
					}else{
				    woyouService.printText("加油金额："+spaceNumber(4)+amount, callback);	
					}

					
				
					woyouService.lineWrap(1, callback);

					woyouService.printText(line, callback);
					
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("订单号：", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(spaceNumber(1),callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(orderno, callback);
					woyouService.lineWrap(1, callback);
					
					
//					woyouService.setFontSize(8, callback);
//					woyouService.setAlignment(0, callback);
//					woyouService.printText("123456789012345678", callback);
//					woyouService.lineWrap(1, callback);
//					woyouService.setFontSize(24, callback);
//					woyouService.setAlignment(0, callback);
					
					
					woyouService.printText("创建时间：", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(spaceNumber(3), callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(orderdateShow, callback);
					woyouService.lineWrap(1, callback);
					
					/*woyouService.setFontSize(18, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("\n", callback);
					woyouService.lineWrap(1, callback);
					woyouService.setFontSize(24, callback);
					woyouService.setAlignment(0, callback);*/
					
					woyouService.printText("交易商户：", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					String jysh="交易商户：";
					int jyshLength=jysh.length()*2;
					int jyshCount=(maxLength-jyshLength-stationName.length()*2)/2;
					woyouService.printText(spaceNumber(jyshCount+2), callback);//????
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(stationName, callback);
					woyouService.lineWrap(1, callback);
					
					/*woyouService.setFontSize(18, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(blank, callback);
					woyouService.lineWrap(1, callback);
					woyouService.setFontSize(24, callback);
					woyouService.setAlignment(0, callback);*/

					//woyouService.lineWrap(2, callback);
					woyouService.printText("积分抵扣金额：", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(spaceNumber(1), callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("-"+pointsAmount+"("+points+"积分"+")", callback);
					woyouService.lineWrap(1, callback);

/*					woyouService.setFontSize(18, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("\n", callback);
					woyouService.lineWrap(1, callback);
					woyouService.setFontSize(24, callback);
					woyouService.setAlignment(0, callback);*/
					/*String daijinquan="代金券：";
					String voucheramountStr=voucheramount;
					String[] stringArray={daijinquan,voucheramountStr};
					int daijinLength=daijinquan.length()*2;
					int voucherLength=voucheramountStr.length();
					int[] intArray={daijinLength,30-daijinLength};
					int[] alignArray={0,2};
					woyouService.printColumnsText(stringArray,intArray,alignArray,callback);*/
					woyouService.printText("代金券：", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(spaceNumber(12), callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(voucheramount, callback);

					woyouService.lineWrap(1, callback);

					
					woyouService.setFontSize(36, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printText(line, callback);
					
					woyouService.lineWrap(1, callback);

					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					if(payment==null||payment.length()<=0||payment.equals("null")){
						woyouService.printText("支付方式："+spaceNumber(17), callback);
					}else{
						String paymethodName="支付方式：";
						int paymethodNameLength=paymethodName.length();
						int paymentLength=payment.length();
						int count=maxLength-paymethodNameLength*2-paymentLength*2;
						woyouService.printText("支付方式："+spaceNumber(count)+payment, callback);
						
					}
					
				/*	woyouService.lineWrap(2, callback);
					woyouService.printText("会员积分："+spaceNumber(15)+points, callback);*/

					woyouService.lineWrap(1, callback);

					int spaceCount=30;
					String realPayStr="实付金额：";
					int realPayStrLength=realPayStr.length();
					String payamountStr=String.valueOf(payamount);
					int payamountlength=payamountStr.length();
					int count=(spaceCount-realPayStrLength*2-payamountlength)-6;//????待处理
					woyouService.printText("实付金额：", callback);
					
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(spaceNumber(count), callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(payamount, callback);

					woyouService.lineWrap(1, callback);

					
					woyouService.setFontSize(36, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printText(line, callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.lineWrap(1, callback);
					woyouService.printText("序列号："+spaceNumber(5)+ serialnoStr , callback);
					//woyouService.lineWrap(4, callback);
					woyouService.lineWrap(5, mcallback);
					
					/*woyouService.printText("您前面还有",callback);
					woyouService.printTextWithFont("28", "", 36, callback);
					woyouService.printText("位在等待\n",callback);
					
					woyouService.lineWrap(1, callback);							

					woyouService.lineWrap(2, callback);
					woyouService.setAlignment(1, callback);*/
					//woyouService.printBitmap(mBitmap, callback);
/*					woyouService.setFontSize(24, callback);
					woyouService.printTextWithFont("歡迎開啟打印機測試\n", "", 35, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printTextWithFont("*****************************\n", "", 24, callback);
					woyouService.printTextWithFont("这是一行正常字体\n", "", 24, callback);
					woyouService.printTextWithFont("這是一行30號字體\n", "", 30, callback);
					woyouService.printTextWithFont("这是一行36号字体\n", "", 36, callback);
					woyouService.printTextWithFont("这是一行42号字体\n", "", 42, callback);
					woyouService.printTextWithFont("*****************************\n", "", 24, callback);
					woyouService.sendRAWData(BytesUtil.initLine2(384), callback);
					woyouService.setAlignment(1, callback);
					woyouService.printBarCode("2015112910", 8, 100, 2, 2, callback);
					woyouService.printTextWithFont("\n\n", "", 24, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printTextWithFont("***Completed***\n", "", 35, callback);
					for(int i=0; i<12; i++){
						woyouService.sendRAWData(BytesUtil.initLine1(384,i), callback);
					}*/
					/*Message message=Message.obtain();
					message.what=1;
					mHandler.sendMessage(message);*/
					
					/*String[] s = woyouService.getServiceVersion().split("\\.");
					int ver = 10000 * Integer.parseInt(s[0]) 
							+ 100 * Integer.parseInt(s[1])  
							+ Integer.parseInt(s[2]) ;*/
					
					//printOriginalText方法是1.7.6版本新加的，用于字符原宽度输出，即不作等宽处理
					/*if(ver >= 10706){
						woyouService.setFontSize(36, callback);
						woyouService.printOriginalText("κρχκμνκλρκνκνμρτυφ\n", callback);
						woyouService.printOriginalText("http://www.sunmi.com\n", callback);
						woyouService.printOriginalText("这是一行36号字体\n這是一行36號字體\n", callback);
						woyouService.setFontSize(24, callback);
						woyouService.printOriginalText("κρχκμνκλρκνκνμρτυφ\n", callback);
						woyouService.printOriginalText("http://www.sunmi.com\n", callback);
						woyouService.printOriginalText("这是一行正常字体\n這是一行正常字體\n", callback);
					}*/
					
					/*Log.i("PrinterTestDemo","version = " + woyouService.getServiceVersion()+ "\nlength="+s.length);
						
					woyouService.lineWrap(4, callback);*/
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					Message message=Message.obtain();
					message.what=PRINTFAIL;
					mHandler.sendMessage(message);
				}
				
			}});
	}
	
	private String spaceNumber(int num){
		String result="";
		for(int i=0;i<num;i++){
			result+=" ";
		}
		return result;
	}
	
	public String GetPrintStr(String serialnoStr) {
		SimpleDateFormat sDateFormat = new SimpleDateFormat(
				"yyyy-MM-dd hh:mm:ss");
		String date = sDateFormat.format(new java.util.Date());
		StringBuilder sb = new StringBuilder();

		String tou = "交易凭证";
		//String orderno="1111111";
		//String createtime="1122222";
		//String staionid="11113333";
		//String amount="1111444";
		//String payment="1115555";
		//String memberpoints="1111666";
		//String vouchers="1111777";
		//String realPay="1111888";
		/*String address = "南山区深南大道9028号益田假日广场3楼东侧（1号线世界之窗站A出口）";
		String saleID = "2016930233330";
		String item = "项目";
		Double price = 25.00;
		int count = 10;
		Double total = 0.00;
		Double fukuan = 500.00;*/
		

		sb.append("\t\t\t" + tou + "     \n");
		//sb.append("日期:" + date + "  " + "\n单号:" + saleID + "\n");
		//sb.append("******************************\n");
		sb.append("订单号:" + "\t" + orderno + "\n");
		sb.append("创建时间:" + "\t" + orderdateShow + "\n");
		sb.append("交易商户:" + "\t\t\t" + stationName + "\n");
		sb.append("加油金额:" + "\t\t\t\t\t" + amount + "\n");
		if(payment!=""||payment!=null){
			sb.append("支付方式:" + "\t\t\t\t" + payment + "\n");
		}else{
			sb.append("支付方式:" + "\t\t\t\t" + " " + "\n");
		}
		sb.append("会员积分:" + "\t\t\t\t\t" + points+"("+pointsAmount+")"+ "\n");
		sb.append("代金券:" + "\t\t\t\t\t" + voucheramount+ "\n");
		sb.append("实付金额:" + "\t\t\t\t\t" + payamount + "\n");
		sb.append("序列号:" + "\t" + serialnoStr + "\n");
		sb.append(""+"\t\t\t\t"+ "\n");
		sb.append(""+"\t\t\t\t"+ "\n");
		sb.append(""+"\t\t\t\t"+ "\r\n\n");
		/*for (int i = 0; i < count; i++) {
			Double xiaoji = (i + 1) * price;
			sb.append(item + (i + 1) + "\t\t" + (i + 1) + "\t" + price + "\t"
					+ xiaoji);
			total += xiaoji;

			if (i != (count))
				sb.append("\n");
		}*/

		/*sb.append("******************************\n");
		sb.append("数量: " + count + " 合计:   " + total + "\n");
		sb.append("付款: 现金" + "    " + fukuan + "\n");
		sb.append("现金找零:" + "   " + (fukuan - total) + "\n");
		sb.append("******************************\n");*/
		//sb.append("地址：" + address + "\n");
		/*sb.append("电话：0755-89829988\n");

		sb.append("******谢谢惠顾欢迎下次光临******\r\n\n");*/
		return sb.toString();
	}
}
