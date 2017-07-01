package com.cx.sumicashersystem.myservice;

import org.json.JSONException;
import org.json.JSONObject;

import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class PrtintTest extends Activity {
private static final String TAG = "PrintTest";
EditText printEt;
Button printBt;
Button toSuccessBt;
SharedPreferences prtintTestSharedPreferences;

private String device = "/dev/ttyMT0";
private int baudrate = 115200;
//PrinterClassSerialPort3505 printerClass = null;
/*Handler mhandler = new Handler() {
	public void handleMessage(Message msg) {
		switch (msg.what) {
		case PrinterCommand.MESSAGE_READ:
			byte[] readBuf = (byte[]) msg.obj;
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
				printerClass.write(new byte[] { 0x1b, 0x2b });
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
		setContentView(R.layout.test_print_layout);
		initView();
	}
private void initView(){
//printerClass = new PrinterClassSerialPort3505(device, baudrate, mhandler);
prtintTestSharedPreferences=getSharedPreferences("cashiervalues",0);
printEt=(EditText)findViewById(R.id.print_et);
printBt=(Button)findViewById(R.id.print_bt);
toSuccessBt=(Button)findViewById(R.id.tosuccessresult_bt);
printBt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根	
		printReceiptByAsyncHttp();
	}
	
});
toSuccessBt.setOnClickListener(new OnClickListener(){

	@Override
	public void onClick(View v) {
		// TODO 自动生成的方法存根
		Intent toSuccessResult=new Intent(PrtintTest.this,SuccessResultActivity.class);
		startActivity(toSuccessResult);
	}
	
});


}


@Override
protected void onDestroy() {
	// TODO 自动生成的方法存根
	/*if(printerClass.mSerialPort.isOpen){
		printerClass.close();
		}else{
		}*/
	super.onDestroy();
}
public void print(String serialnoStr){
	/*openDevice();
	printUTF8Text();*/
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

/*private void printUTF8Text() {
	String str = printEt.getText().toString();
	try {
		printerClass.printText(str);

	} catch (Exception e) {
		Log.e(TAG, e.getMessage());
	}
}*/

public void printReceiptByAsyncHttp(){
	
	AsyncHttpClient asyncHttpClient=new AsyncHttpClient();
	asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	String mUrl=prtintTestSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=prtintTestSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
	String printUrl=mUrl+":"+mPort+"/"+HttpParams.printReceiptUrl;
	//String printUrl=mUrl+"/"+HttpParams.printReceiptUrl;
	int userid=prtintTestSharedPreferences.getInt("userId", -1);
	
	RequestParams params=new RequestParams();
	params.put("orderid", "100010");
	params.put("printtype", "1");
	params.put("userid", String.valueOf(userid));
	params.put("remark", "");
	asyncHttpClient.post(printUrl, params, new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(statusCode, response);
			boolean success=false;
			String serialnoStr="";
			try{
				success=response.getBoolean("success");
				if(success){
					JSONObject data=(JSONObject) response.get("data");
					if(data!=null){
						serialnoStr=data.getString("serialno");
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



}
