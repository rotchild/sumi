package com.cx.sumicashersystem.myservice;

import java.text.SimpleDateFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;

import woyou.aidlservice.jiuiv5.ICallback;
import woyou.aidlservice.jiuiv5.IWoyouService;

import com.cx.sumicashersystem.net.HttpParams;
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
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

public class ApplyRePrintActivity extends Activity {
	private static final String TAG = "ApplyRePrint";
	RadioButton radio_lackPaper;
	RadioButton radio_equiptrouble;
	RadioButton radio_otherreason;
	EditText editText;
	
	Button suretoPrint;
	Button cancelPrint;
	
	TextView backTv;
	SharedPreferences applyRePrintSharedPreferences;
	String remarkStr="打印缺纸";
	ProgressDialog pd;
	Timer mTimer;
	TimerTask tTask;
	private IWoyouService woyouService;
	private ICallback callback = null;
	private ICallback mcallback = null;
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
	
	
	Handler dealerHandler=new Handler(){

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
					Toast.makeText(ApplyRePrintActivity.this, "网络错误",Toast.LENGTH_SHORT).show();
				}
				break;
				
			case 3:
				Toast.makeText(ApplyRePrintActivity.this, "打印完成", Toast.LENGTH_SHORT).show();
				break;
			default:
				break;
			}
		}
		
	};
	//private String device = "/dev/ttyMT0";
	//private int baudrate = 115200;
	//PrinterClassSerialPort3505 printerClass = null;
	
	String orderno="";
	String orderdateShow="";
	String amount="";
	String payment="";
	String points="";
	String pointsAmount="";
	String voucheramount="";
	String payamount="";
	String stationName="";
	ProgressDialog printPd;
/*	Handler mhandler = new Handler() {
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case PrinterCommand.MESSAGE_READ:
				byte[] readBuf = (byte[]) msg.obj;
				Log.d(TAG, "readBufSize:"+readBuf.length+"readBuf:" + readBuf[0]+","+readBuf[1]);
				if(readBuf[0] == 0x08){
					Toast.makeText(getApplicationContext(),
							"打印缺纸", Toast.LENGTH_SHORT).show();
				}else if(readBuf[0] == 0x01){
					printPd=ProgressDialog.show(ApplyRePrintActivity.this, "请等候", "打印中...");
				}
				if(printPd!=null){
					printPd.dismiss();
					printPd=null;
				}
				break;
			case PrinterCommand.MESSAGE_STATE_CHANGE:
				switch (msg.arg1) {
				case PrinterCommand.STATE_CONNECTED:
					Log.d(TAG, "state_connected");
					break;
				case PrinterCommand.STATE_CONNECTING:
					Log.d(TAG, "state_connecting");
					Toast.makeText(getApplicationContext(),
							"STATE_CONNECTING", Toast.LENGTH_SHORT).show();
					break;
				case PrinterCommand.STATE_LISTEN:
				case PrinterCommand.STATE_NONE:
					Log.d(TAG, "state_listen&none");
					break;
				case PrinterCommand.SUCCESS_CONNECT:
					Log.d(TAG, "success_connect");
					printerClass.write(new byte[] { 0x1b, 0x2b });
					Toast.makeText(getApplicationContext(),
							"SUCCESS_CONNECT", Toast.LENGTH_SHORT).show();
					
					break;
				case PrinterCommand.FAILED_CONNECT:
					Log.d(TAG, "failed_connect");
					Toast.makeText(getApplicationContext(),
							"FAILED_CONNECT", Toast.LENGTH_SHORT).show();

					break;
				case PrinterCommand.LOSE_CONNECT:
					Log.d(TAG, "lose_connect");
					Toast.makeText(getApplicationContext(), "LOSE_CONNECT",
							Toast.LENGTH_SHORT).show();
				}
				break;
			case PrinterCommand.MESSAGE_WRITE:
				Log.d(TAG, "message_write");

				break;
			case PrinterCommand.PERMIT_PRINTER:
				Log.d(TAG, "permit_printer");
				String result = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"允许打印", Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.FORBID_PRINTER:
				Log.d(TAG, "forbid_printer");
				String forbid_print = (String) msg.obj;
				Toast.makeText(getApplicationContext(),"禁止打印", Toast.LENGTH_SHORT).show();
				break;
			case PrinterCommand.TIMEOUT_PRINTER:
				Log.d(TAG, "timeout");
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
		setContentView(R.layout.reprint_layout);
		initView();
		
		/*bundle.putString("orderno", orderno);
		bundle.putString("orderdateShow", orderdateShow);
		bundle.putString("stationName", stationName);
		bundle.putString("amount",amount);
		bundle.putString("payment",payment);
		bundle.putString("points",points);
		bundle.putString("pointsAmount",pointsAmount);
		bundle.putString("voucheramount",voucheramount);*/
		Bundle bundle=this.getIntent().getExtras();
		orderno=bundle.getString("orderno");
		orderdateShow=bundle.getString("orderdateShow");
		amount=bundle.getString("amount");
		payment=bundle.getString("payment");
		points=bundle.getString("points");
		pointsAmount=bundle.getString("pointsAmount");
		voucheramount=bundle.getString("voucheramount");
		payamount=bundle.getString("payamount");
		stationName=bundle.getString("stationName");
	}
	private void initView(){
		
	//printerClass = new PrinterClassSerialPort3505(device, baudrate, mhandler);
	applyRePrintSharedPreferences=getSharedPreferences("cashiervalues",0);
	 suretoPrint=(Button)findViewById(R.id.suretoreprint_bt);
	 cancelPrint=(Button)findViewById(R.id.canceltoreprint_bt);
	 
	 RelativeLayout lackPaperlayout=(RelativeLayout)findViewById(R.id.rl_paperlack);
	 RelativeLayout equiptroublelayout=(RelativeLayout)findViewById(R.id.rl_equipmenttrouble);
	 RelativeLayout otherreasonlayout=(RelativeLayout)findViewById(R.id.rl_otherreason);
	  radio_lackPaper=(RadioButton)findViewById(R.id.radio_paperlack);
	  radio_equiptrouble=(RadioButton)findViewById(R.id.radio_equipmenttrouble);
	  radio_otherreason=(RadioButton)findViewById(R.id.radio_otherreason);
	  editText=(EditText)findViewById(R.id.reasondescribe_et);
	  
	  radio_lackPaper.setChecked(true);
	  radio_equiptrouble.setChecked(false);
	  radio_otherreason.setChecked(false);
	  editText.setText("");
	  editText.setFocusableInTouchMode(false);;
	  editText.setEnabled(false);
	  
	  backTv=(TextView) findViewById(R.id.back_tv_reprint);
	  backTv.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			ApplyRePrintActivity.this.finish();
		}
		  
	  });
	  
	suretoPrint.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			String editTextS=editText.getText().toString();
			if(!editTextS.equals("")){
				remarkStr=editTextS;
			}
			pd=ProgressDialog.show(ApplyRePrintActivity.this, "请稍候", "提交中...");
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
			ReprintReceiptByAsyncHttp();
		}
		
	});
	cancelPrint.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			ApplyRePrintActivity.this.finish();
		}
		
	});
	

lackPaperlayout.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
	Log.e("lackPaperLayout", "you click lackPaperlayout");
	radio_lackPaper.setChecked(true);
	remarkStr="打印纸缺失";
	radio_equiptrouble.setChecked(false);
	radio_otherreason.setChecked(false);
	editText.setText("");
	editText.setFocusableInTouchMode(false);;
	editText.setEnabled(false);
	}
	
});

equiptroublelayout.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		Log.e("lackPaperLayout", "you click equiptroublelayout");
		radio_lackPaper.setChecked(false);
		radio_equiptrouble.setChecked(true);
		remarkStr="设备故障";
		radio_otherreason.setChecked(false);
		editText.setText("");
		editText.setFocusableInTouchMode(false);;
		editText.setEnabled(false);
	}
	
});
otherreasonlayout.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		Log.e("lackPaperLayout", "you click otherreasonlayout");
		radio_lackPaper.setChecked(false);
		radio_equiptrouble.setChecked(false);
		radio_otherreason.setChecked(true);
		editText.setFocusableInTouchMode(true);;
		editText.setEnabled(true);
	}
	
});

callback = new ICallback.Stub() {
	
	@Override
	public void onRunResult(final boolean success) throws RemoteException {
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
			}});
		
	}
};

mcallback = new ICallback.Stub() {
	
	@Override
	public void onRunResult(final boolean success) throws RemoteException {
		Log.d(TAG,"onRunResult:" + success + "\n");	
		//Toast.makeText(ApplyRePrintActivity.this, "finish", Toast.LENGTH_SHORT).show();
		Message testmessage=Message.obtain();
		testmessage.what=3;
		dealerHandler.sendMessage(testmessage);
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
		/*if(printerClass.mSerialPort.isOpen){
			printerClass.close();
			}else{
			}*/
		try{
			unbindService(connService);					
		}catch(Exception e){
			e.printStackTrace();
		}
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
	public void rePrint(String serialnoStr){
		//String editTextContent=editText.getText().toString();
		//String printSerialappend=editTextContent+serialnoStr;
		String printSerialappend=GetPrintStr(serialnoStr);
		//openDevice();
		//Bitmap blankBit=BitmapFactory.decodeResource(getResources(), R.drawable.printpic);
		//printerClass.printImage(blankBit);
		//blankBit.recycle();
		//printUTF8Text(printSerialappend);
		printText(serialnoStr);
		
		
	}
	
	public void printText(final String serialnoStr){
		applyRePrintSharedPreferences=getSharedPreferences("cashiervalues",0);
		final String serviceTypeStr=applyRePrintSharedPreferences.getString("serivceType", "1");
		ThreadPoolManager.getInstance().executeTask(new Runnable(){

			@Override
			public void run() {
				
				try {
					int maxLength=30;
					int spaceFontSize=38;
					int contentFontSize=24;
					String line="---------------------";
					String blank="                     ";
					woyouService.setFontSize(36, callback);
					woyouService.setAlignment(1, callback);
					woyouService.printText("交易凭证(再次打印)\n", callback);
					
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
				} catch (RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}});
	}
	
	/*public void openDevice(){
		if(printerClass.mSerialPort.isOpen){
			
		}else{
			printerClass.device = device;
			printerClass.baudrate = baudrate;
			printerClass.open();
			printerClass.write(new byte[] { 0x1b, 0x76 });
		}
	}*/
	
/*	private void printUTF8Text(String printSerial) {
		try {
			printerClass.printText(printSerial);

		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
	}*/
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

		String tou = "交易凭证(再次打印)";
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
		

		sb.append("\t\t" + tou + "     \n");
		//sb.append("日期:" + date + "  " + "\n单号:" + saleID + "\n");
		//sb.append("******************************\n");
		sb.append("订单号:" + "\t" + orderno + "\n");
		sb.append("创建时间:" + "\t" + orderdateShow + "\n");
		sb.append("交易商户:" + "\t\t\t" + stationName + "\n");
		sb.append("加油金额:" + "\t\t\t\t\t" + amount + "\n");
		//sb.append("支付方式:" + "\t\t\t\t" + payment + "\n");
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
	
	public void ReprintReceiptByAsyncHttp(){
		AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
		asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl=applyRePrintSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=applyRePrintSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String reprintUrl=mUrl+":"+mPort+"/"+HttpParams.printReceiptUrl;
		//String reprintUrl=mUrl+"/"+HttpParams.printReceiptUrl;
		int userid=applyRePrintSharedPreferences.getInt("userId", -1);
		RequestParams params=new RequestParams();
		String orderid=applyRePrintSharedPreferences.getString("orderid", "");
		params.put("orderid", orderid);
		params.put("printtype", "2");
		params.put("userid", String.valueOf(userid));
		params.put("remark", remarkStr);
		Log.d(TAG, "remarkStr"+remarkStr);
		asyncHttpClient.post(reprintUrl, params, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存根
				super.onSuccess(statusCode, response);
				if(pd!=null){
					pd.dismiss();
				}
				if(mTimer!=null){
					if(tTask!=null){
						tTask.cancel();
						tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
				}
				boolean success=false;
				String serialnoStr="";
				try{
				success=response.getBoolean("success");
				if(success){
					JSONObject data=(JSONObject) response.get("data");
					if(data!=null){
						serialnoStr=data.getString("serialno");
						rePrint(serialnoStr);
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
				if(pd!=null){
					pd.dismiss();
				}
				if(mTimer!=null){
					if(tTask!=null){
						tTask.cancel();
						tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
				}
			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存根
				super.onFailure(e, errorResponse);
				if(pd!=null){
					pd.dismiss();
				}
				if(mTimer!=null){
					if(tTask!=null){
						tTask.cancel();
						tTask=null;
					}
					mTimer.cancel();
					mTimer=null;
				}
			}
			
			
		});
	}
}
