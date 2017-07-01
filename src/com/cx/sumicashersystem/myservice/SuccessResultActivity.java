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
			// TODO �Զ����ɵķ������
			super.handleMessage(msg);
			int msgId=msg.what;
			switch(msgId){
			case SHOWDIALOG:
				//printWaitPd=ProgressDialog.show(SuccessResultActivity.this, "��ӡ��", "���Ժ�...");
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
			Toast.makeText(SuccessResultActivity.this, "��ӡʧ��,���ٴδ�ӡ", Toast.LENGTH_SHORT).show();
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
							"��ӡȱֽ", Toast.LENGTH_SHORT).show();
				}else if(readBuf[0] == 0x01){
					printPd=ProgressDialog.show(SuccessResultActivity.this, "��Ⱥ�", "��ӡ��...");
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
				Toast.makeText(getApplicationContext(),"�����ӡ", Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.FORBID_PRINTER:
				String forbid_print = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"��ֹ��ӡ", Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.TIMEOUT_PRINTER:
				String print_timeout = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"������ӡ����", Toast.LENGTH_SHORT).show();
				break;
			}
			super.handleMessage(msg);
		}
	};*/
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.successresult_layout);
		Bundle bundle=this.getIntent().getExtras();
		activitytype=bundle.getString("activitytype");
		if(activitytype.equals("1")){
			orderno=bundle.getString("orderno");
			successResultSharedPreferences=getSharedPreferences("cashiervalues",0);
			//Toast.makeText(SuccessResultActivity.this, "userpaying", Toast.LENGTH_SHORT).show();
			payingPd=ProgressDialog.show(SuccessResultActivity.this, "��ѯ��", "���Ժ�...");
			cdtimer=new CountDownTimer(50*1000,5*1000){

				@Override
				public void onTick(long millisUntilFinished) {
					// TODO �Զ����ɵķ������
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
					// TODO �Զ����ɵķ������
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
		
		successLabelTv.setText("���׳ɹ�");
		orderNoTv.setText(orderno);
		orderDateTv.setText(orderdateShow);
		stationidTv.setText(stationName);
		amountTv.setText(amount);
		if(payment==null||payment.length()<=0||payment.equals("null")){
			paymentTv.setText("");
		}else{
			paymentTv.setText(payment);
		}
		
		pointsTv.setText(points+"��"+"(-"+pointsAmount+")");
		voucheramountTv.setText(voucheramount);
		payamountTv.setText(payamount);
		
		successFinishBt=(Button) findViewById(R.id.success_finish);
		successFinishBt.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO �Զ����ɵķ������
				SuccessResultActivity.this.finish();
			}
			
		});
Button rePrintbt=(Button)findViewById(R.id.reprint);
rePrintbt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO �Զ����ɵķ������
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
					Toast.makeText(SuccessResultActivity.this, "��ӡʧ��", Toast.LENGTH_SHORT).show();
				}});
			
			/*Message message=Message.obtain();
			message.what=PRINTFAIL;
			mHandler.sendMessage(message);*/
			Log.d("successresult", "fail");
	/*		if(printWaitPd!=null){
				printWaitPd.dismiss();
				printWaitPd=null;
			}
		Toast.makeText(SuccessResultActivity.this, "��ӡʧ��,���ٴδ�ӡ", Toast.LENGTH_SHORT).show();*/
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
				Toast.makeText(SuccessResultActivity.this, "��ӡʧ��", Toast.LENGTH_SHORT).show();
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
		//Toast.makeText(SuccessResultActivity.this, "finish", Toast.LENGTH_SHORT).show();//��callback����Ч
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
				Toast.makeText(SuccessResultActivity.this, "��ӡʧ��", Toast.LENGTH_SHORT).show();
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
		// TODO �Զ����ɵķ������
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
				// TODO �Զ����ɵķ������
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
				// TODO �Զ����ɵķ������
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
		String printtype="1";//��һ�δ�ӡ
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
				// TODO �Զ����ɵķ������
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
						Toast.makeText(getApplicationContext(), "�Ѵ�ӡ",Toast.LENGTH_SHORT).show();
					}
				}catch(JSONException e){
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO �Զ����ɵķ������
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
		printWaitPd=ProgressDialog.show(SuccessResultActivity.this, "��ӡ��", "���Ժ�...");
		mTimerTask=new TimerTask(){

			@Override
			public void run() {
				// TODO �Զ����ɵķ������
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
					woyouService.printText("����ƾ֤\n", callback);
					
					woyouService.setFontSize(36, callback);
					woyouService.setAlignment(1, callback);
					woyouService.lineWrap(1, callback);
					if(serviceTypeStr.equals("2")){
				    woyouService.printText("�����"+spaceNumber(4)+amount, callback);
					}else{
				    woyouService.printText("���ͽ�"+spaceNumber(4)+amount, callback);	
					}

					
				
					woyouService.lineWrap(1, callback);

					woyouService.printText(line, callback);
					
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("�����ţ�", callback);
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
					
					
					woyouService.printText("����ʱ�䣺", callback);
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
					
					woyouService.printText("�����̻���", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					String jysh="�����̻���";
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
					woyouService.printText("���ֵֿ۽�", callback);
					woyouService.setFontSize(spaceFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText(spaceNumber(1), callback);
					woyouService.setFontSize(contentFontSize, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("-"+pointsAmount+"("+points+"����"+")", callback);
					woyouService.lineWrap(1, callback);

/*					woyouService.setFontSize(18, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printText("\n", callback);
					woyouService.lineWrap(1, callback);
					woyouService.setFontSize(24, callback);
					woyouService.setAlignment(0, callback);*/
					/*String daijinquan="����ȯ��";
					String voucheramountStr=voucheramount;
					String[] stringArray={daijinquan,voucheramountStr};
					int daijinLength=daijinquan.length()*2;
					int voucherLength=voucheramountStr.length();
					int[] intArray={daijinLength,30-daijinLength};
					int[] alignArray={0,2};
					woyouService.printColumnsText(stringArray,intArray,alignArray,callback);*/
					woyouService.printText("����ȯ��", callback);
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
						woyouService.printText("֧����ʽ��"+spaceNumber(17), callback);
					}else{
						String paymethodName="֧����ʽ��";
						int paymethodNameLength=paymethodName.length();
						int paymentLength=payment.length();
						int count=maxLength-paymethodNameLength*2-paymentLength*2;
						woyouService.printText("֧����ʽ��"+spaceNumber(count)+payment, callback);
						
					}
					
				/*	woyouService.lineWrap(2, callback);
					woyouService.printText("��Ա���֣�"+spaceNumber(15)+points, callback);*/

					woyouService.lineWrap(1, callback);

					int spaceCount=30;
					String realPayStr="ʵ����";
					int realPayStrLength=realPayStr.length();
					String payamountStr=String.valueOf(payamount);
					int payamountlength=payamountStr.length();
					int count=(spaceCount-realPayStrLength*2-payamountlength)-6;//????������
					woyouService.printText("ʵ����", callback);
					
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
					woyouService.printText("���кţ�"+spaceNumber(5)+ serialnoStr , callback);
					//woyouService.lineWrap(4, callback);
					woyouService.lineWrap(5, mcallback);
					
					/*woyouService.printText("��ǰ�滹��",callback);
					woyouService.printTextWithFont("28", "", 36, callback);
					woyouService.printText("λ�ڵȴ�\n",callback);
					
					woyouService.lineWrap(1, callback);							

					woyouService.lineWrap(2, callback);
					woyouService.setAlignment(1, callback);*/
					//woyouService.printBitmap(mBitmap, callback);
/*					woyouService.setFontSize(24, callback);
					woyouService.printTextWithFont("�gӭ�_����ӡ�C�yԇ\n", "", 35, callback);
					woyouService.setAlignment(0, callback);
					woyouService.printTextWithFont("*****************************\n", "", 24, callback);
					woyouService.printTextWithFont("����һ����������\n", "", 24, callback);
					woyouService.printTextWithFont("�@��һ��30̖���w\n", "", 30, callback);
					woyouService.printTextWithFont("����һ��36������\n", "", 36, callback);
					woyouService.printTextWithFont("����һ��42������\n", "", 42, callback);
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
					
					//printOriginalText������1.7.6�汾�¼ӵģ������ַ�ԭ���������������ȿ���
					/*if(ver >= 10706){
						woyouService.setFontSize(36, callback);
						woyouService.printOriginalText("�ʦѦ֦ʦ̦ͦʦ˦Ѧʦͦʦ̦ͦѦӦԦ�\n", callback);
						woyouService.printOriginalText("http://www.sunmi.com\n", callback);
						woyouService.printOriginalText("����һ��36������\n�@��һ��36̖���w\n", callback);
						woyouService.setFontSize(24, callback);
						woyouService.printOriginalText("�ʦѦ֦ʦ̦ͦʦ˦Ѧʦͦʦ̦ͦѦӦԦ�\n", callback);
						woyouService.printOriginalText("http://www.sunmi.com\n", callback);
						woyouService.printOriginalText("����һ����������\n�@��һ���������w\n", callback);
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

		String tou = "����ƾ֤";
		//String orderno="1111111";
		//String createtime="1122222";
		//String staionid="11113333";
		//String amount="1111444";
		//String payment="1115555";
		//String memberpoints="1111666";
		//String vouchers="1111777";
		//String realPay="1111888";
		/*String address = "��ɽ�����ϴ��9028��������չ㳡3¥���ࣨ1��������֮��վA���ڣ�";
		String saleID = "2016930233330";
		String item = "��Ŀ";
		Double price = 25.00;
		int count = 10;
		Double total = 0.00;
		Double fukuan = 500.00;*/
		

		sb.append("\t\t\t" + tou + "     \n");
		//sb.append("����:" + date + "  " + "\n����:" + saleID + "\n");
		//sb.append("******************************\n");
		sb.append("������:" + "\t" + orderno + "\n");
		sb.append("����ʱ��:" + "\t" + orderdateShow + "\n");
		sb.append("�����̻�:" + "\t\t\t" + stationName + "\n");
		sb.append("���ͽ��:" + "\t\t\t\t\t" + amount + "\n");
		if(payment!=""||payment!=null){
			sb.append("֧����ʽ:" + "\t\t\t\t" + payment + "\n");
		}else{
			sb.append("֧����ʽ:" + "\t\t\t\t" + " " + "\n");
		}
		sb.append("��Ա����:" + "\t\t\t\t\t" + points+"("+pointsAmount+")"+ "\n");
		sb.append("����ȯ:" + "\t\t\t\t\t" + voucheramount+ "\n");
		sb.append("ʵ�����:" + "\t\t\t\t\t" + payamount + "\n");
		sb.append("���к�:" + "\t" + serialnoStr + "\n");
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
		sb.append("����: " + count + " �ϼ�:   " + total + "\n");
		sb.append("����: �ֽ�" + "    " + fukuan + "\n");
		sb.append("�ֽ�����:" + "   " + (fukuan - total) + "\n");
		sb.append("******************************\n");*/
		//sb.append("��ַ��" + address + "\n");
		/*sb.append("�绰��0755-89829988\n");

		sb.append("******лл�ݹ˻�ӭ�´ι���******\r\n\n");*/
		return sb.toString();
	}
}
