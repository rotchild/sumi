package com.cx.sumicashersystem.myservice;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.utils.GenerateOrdeNO;
import com.cx.sumicashersystem.utils.ToAccuracy2;
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class FuelPay extends Activity {
	
	public static FuelPay mFuelPay = null;
	private double totalDiscount = 0.00;// 合计优惠金额
	private double pointsAmountUse = 0.00;// 可用600积分
	private double inputMoneyD = 0.00;
	private double realPayD = 0.00;
	private static final String TAG = "FuelPay";
	private String pointsStr, pointAmountStr, realPayStr;
	Timer mTimer;
	TimerTask tTask;
	String inputMoneyStr = "";
	TextView totalDiscountTv;
	TextView vouchesValue;
	CheckBox usePointsCB;
	EditText inputMoneyET;
	MKeyboard mKeyboard;
	
	TextView realPayTv;
	TextView wexinTagTv, memberPointsTv, vouchersAmountTv;
	TextView pointsValableTv, pointsAmountTv;
	TextView serviceTitleTv;
	TextView backTv;
	TextView checkPayTv;
	SharedPreferences fuelPaySharedPreferences;
	String orderno="";
	ProgressDialog pd;//等待条
	SharedPreferences FuelSP;

	private Handler dealerHandler = new Handler() {

		@Override
		public void handleMessage(Message msg) {
			// TODO 自动生成的方法存根
			super.handleMessage(msg);
			int msgId = msg.what;
			switch (msgId) {
			case 1:
				Bundle bundle = msg.getData();
				String myinput = bundle.getString("myInput");
				checkPointsByHttp(myinput);
				break;
				
			case 2:
				checkPayTv.setClickable(true);
				
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
		//getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
		setContentView(R.layout.fuelpay_layout);
		initView();
		mFuelPay = this;
	}

	private void initView() {
		fuelPaySharedPreferences = getSharedPreferences("cashiervalues", 0);
		serviceTitleTv = (TextView) findViewById(R.id.fuelservice_tv);
		backTv = (TextView) findViewById(R.id.back_tv);
		checkPayTv =(TextView) findViewById(R.id.checkandpay_tv);
		
		backTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				FuelPay.this.finish();
			}

		});
		String serviceTypeStr = fuelPaySharedPreferences.getString(
				"serivceType", "1");
		if (serviceTypeStr.equals("2")) {
			serviceTitleTv.setText("收银服务");
		}
		usePointsCB = (CheckBox) findViewById(R.id.fuelpay_usepoints_cb);
		usePointsCB.setChecked(true);//默认为勾选状态
		totalDiscountTv = (TextView) findViewById(R.id.total_discount_tv);
		vouchesValue = (TextView) findViewById(R.id.voucheformoney);
		inputMoneyET = (EditText) findViewById(R.id.input_money_et);
		realPayTv = (TextView) findViewById(R.id.realpay_tv);

		wexinTagTv = (TextView) findViewById(R.id.wxtag_tv);
		memberPointsTv = (TextView) findViewById(R.id.member_points_tv);
		vouchersAmountTv = (TextView) findViewById(R.id.voucheramount_tv);

		pointsValableTv = (TextView) findViewById(R.id.pointsValable_show);
		pointsAmountTv = (TextView) findViewById(R.id.pointsformoney);

		String wxtagStr = fuelPaySharedPreferences.getString("wxtag", "");
		int memberPointsInt = fuelPaySharedPreferences.getInt("points", -1);
		String memberPointsStr = String.valueOf(memberPointsInt);
		int voucherAmountInt = fuelPaySharedPreferences.getInt("vouchesAmount",
				-1);
		String voucherAmountStr = String.valueOf(voucherAmountInt);

		wexinTagTv.setText(wxtagStr);
		memberPointsTv.setText(memberPointsStr);
		vouchersAmountTv.setText(voucherAmountStr);
		
		mKeyboard=new MKeyboard(this,R.id.keyboardview,R.xml.hexkbd);
		mKeyboard.registerEditText(R.id.input_money_et);

		inputMoneyET.addTextChangedListener(new TextWatcher() {

			@Override
			public void beforeTextChanged(CharSequence s, int start, int count,
					int after) {
				// TODO 自动生成的方法存根
				Log.e("inputMoneyET", "beforTC" + "start" + start + "/count"
						+ count + "/after" + after);
			}

			@Override
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				// TODO 自动生成的方法存根
				Log.e("inputMoneyET", "onTC" + "start" + start + "/before"
						+ before + "/count" + count);
			}

			@Override
			public void afterTextChanged(Editable s) {
				// TODO 自动生成的方法存根

				Log.e("inputMoneyET", "afterTC");
				inputMoneyStr = inputMoneyET.getText().toString();
				if(inputMoneyStr.startsWith(".")){
					inputMoneyStr="0"+inputMoneyStr;
					inputMoneyET.setText(inputMoneyStr);
					inputMoneyET.setSelection(inputMoneyStr.length());
				}
				if (!inputMoneyStr.equals("")) {
					// inputMoneyD=Double.valueOf(inputMoneyStr).doubleValue();
					inputMoneyD = Double.parseDouble(inputMoneyStr);
					BigDecimal inputMB=new BigDecimal(Double.toString(inputMoneyD));
					BigDecimal totalDB=new BigDecimal(Double.toString(totalDiscount));
					double mRealPay=inputMB.add(totalDB).doubleValue();
					realPayD = mRealPay;
				
					//realPayD = inputMoneyD + totalDiscount;
				//	realPayStr = String.valueOf(realPayD);
					realPayStr = String.valueOf(mRealPay);
					Log.d("Fuel realPayStr", realPayStr);
					// checkPointsByHttp(inputMoneyStr);
					checkPayTv.setClickable(false);
					//checkPayTv.setBackground(background);
					delayCheckPoints(inputMoneyStr);
				} else {
					inputMoneyD = 0.00;
					//delayCheckPoints("0");
					realPayD = 0.00;
					pointsValableTv.setText("可用0积分");
					pointsAmountUse=0;
					totalDiscount=0;
					pointsAmountTv.setText("0");
					totalDiscountTv.setText("0");
					
				}

				if (realPayD > 0) {
					Log.d("FuelPay", "inputMoneyStr" + inputMoneyStr + "/"
							+ "inputMoneyD" + inputMoneyD + "/"
							+ "totalDiscount" + totalDiscount);
					//realPayTv.setText(ToAccuracy2.getStringVal2(realPayD));
					realPayTv.setText(String.valueOf(realPayD));
					//realPayStr = String.valueOf(realPayD);
					realPayStr = String.valueOf(realPayD);
				} else {

					realPayTv.setText("0.00");
					realPayStr = "0";
				}

			}

		});
		usePointsCB.setOnCheckedChangeListener(new OnCheckedChangeListener() {

			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				// TODO 是否使用600积分
				if (isChecked) {
					totalDiscount = 0.00;
					String vouchesforMoneyStr1 = vouchesValue.getText()
							.toString();
					if (vouchesforMoneyStr1 != "") {
						double vouchesDouble = Double
								.valueOf(vouchesforMoneyStr1);
						totalDiscount = totalDiscount+vouchesDouble;
					}
					totalDiscount = totalDiscount - pointsAmountUse;
					totalDiscountTv.setText(ToAccuracy2
							.getStringVal2(totalDiscount));
					Log.d("mFuelPay", "inputMoneyD" + inputMoneyD + "/"
							+ "totalDiscount" + totalDiscount);
					BigDecimal inputMB=new BigDecimal(Double.toString(inputMoneyD));
					BigDecimal totalDB=new BigDecimal(Double.toString(totalDiscount));
					double mRealPay=inputMB.add(totalDB).doubleValue();
					realPayD = mRealPay;
					//realPayD = inputMoneyD + totalDiscount;
					if (realPayD > 0) {

						//realPayTv.setText(ToAccuracy2.getStringVal2(realPayD));
						realPayTv.setText(String.valueOf(realPayD));
						realPayStr = String.valueOf(realPayD);
					} else {

						realPayTv.setText("0.00");
						realPayStr = "0";
					}
				} else {
					totalDiscount = 0.00;
					String vouchesforMoneyStr2 = vouchesValue.getText()
							.toString();
					Log.e("onCheckedChanged", "vouchesforMoney"
							+ vouchesforMoneyStr2);
					if (vouchesforMoneyStr2 != "") {
						double vouchesDouble = Double
								.valueOf(vouchesforMoneyStr2);
						totalDiscount = totalDiscount + vouchesDouble;
					} else {
						totalDiscount = 0.00;
					}
					totalDiscountTv.setText(ToAccuracy2
							.getStringVal2(totalDiscount));
					BigDecimal inputMB=new BigDecimal(Double.toString(inputMoneyD));
					BigDecimal totalDB=new BigDecimal(Double.toString(totalDiscount));
					double mRealPay=inputMB.add(totalDB).doubleValue();//????
					//realPayD = mRealPay;
					realPayD = inputMoneyD + totalDiscount;
					if (realPayD > 0) {

						//realPayTv.setText(ToAccuracy2.getStringVal2(realPayD));
						realPayTv.setText(String.valueOf(mRealPay));
						realPayStr = String.valueOf(realPayD);
					} else {

						realPayTv.setText("0.00");
						realPayStr = "0";
					}
				}
			}

		});
		View vouchesLayout = findViewById(R.id.vouche_layout);
		vouchesLayout.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent chooseVouchesIntent = new Intent(FuelPay.this,
						ChooseVouches.class);
				startActivityForResult(chooseVouchesIntent, 1000);// 去选代金券

			}
		});

		checkPayTv.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				
				// TODO 自动生成的方法存根
				if(inputMoneyStr.endsWith(".")){
					Toast.makeText(FuelPay.this, "请检查实付金额是否错误", Toast.LENGTH_SHORT).show();
				}else{
					if (realPayD > 0) {
						SharedPreferences.Editor repayEditor=fuelPaySharedPreferences.edit();
						Bundle bundle = new Bundle();
						bundle.putString("amountStr", inputMoneyStr);
						repayEditor.putString("amountStr", inputMoneyStr);
						if (!usePointsCB.isChecked()) {
							Log.d("FuelPay", "cbcheckedfalse");
							bundle.putString("pointsStr", "0");
							bundle.putString("pointsamountStr", "0");
							repayEditor.putString("pointsStr", "0");
							repayEditor.putString("pointsamountStr", "0");
							
						} else {
							//?????
							bundle.putString("pointsStr", pointsStr);
							bundle.putString("pointsamountStr", pointAmountStr);
							repayEditor.putString("pointsStr", pointsStr);
							repayEditor.putString("pointsamountStr",pointAmountStr);
							
						}
						bundle.putString("realPayStr", realPayStr);
						repayEditor.putString("realPayStr", realPayStr);
						repayEditor.commit();	
						bundle.putString("scanType", "scanPayCode");
						
						Log.d(TAG, "realPayStr"+realPayStr+"/"+"pointsamountStr"+pointAmountStr);
						Intent toScannPayCodeIntent = new Intent(FuelPay.this,
								MipcaActivityCapture.class);
						toScannPayCodeIntent.putExtras(bundle);
						startActivity(toScannPayCodeIntent);
						//FuelPay.this.finish();
					} else if(realPayD==0){//实付金额为0调用submitOrderByHttp
						long TimeStamp=System.currentTimeMillis()/1000;//获取10位时间戳
						String timeStampStr=String.valueOf(TimeStamp);
						orderno="JF"+timeStampStr;
						submitOrderByHttp(orderno);
					}else {
						Toast.makeText(getApplicationContext(), "请检查实付金额是否错误",
								Toast.LENGTH_SHORT).show();
					}
				}


			}

		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		// TODO 自动生成的方法存根
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == 1000 && resultCode == 1001) {// 所选代金券
			String result_vaulue = data.getStringExtra("vouchesVal");
			Double result_Double = Double.parseDouble(result_vaulue);
			DecimalFormat df = new DecimalFormat("0.00");
			String result_2Str = df.format(result_Double);
			vouchesValue.setText(result_2Str);
			if (usePointsCB.isChecked()) {
				totalDiscount = 0.00;
				totalDiscount = totalDiscount - pointsAmountUse- result_Double.doubleValue();
			} else {
				totalDiscount = 0.00;
				totalDiscount = totalDiscount - result_Double.doubleValue();
			}
			totalDiscountTv.setText(ToAccuracy2.getStringVal2(totalDiscount));
			BigDecimal inputMB=new BigDecimal(Double.toString(inputMoneyD));
			BigDecimal totalDB=new BigDecimal(Double.toString(totalDiscount));
			double mRealPay=inputMB.add(totalDB).doubleValue();
			//realPayD = mRealPay;
			realPayD = inputMoneyD + totalDiscount;
			if (realPayD > 0) {
				Log.d("FuelPay", "inputMoneyD" + inputMoneyD + "/"
						+ "totalDiscount" + totalDiscount);
				//realPayTv.setText(ToAccuracy2.getStringVal2(realPayD));
				realPayTv.setText(String.valueOf(mRealPay));//eg.123.3 mrealpay=98.7 realPayD=98.69999999...,but post need realPayD
				//realPayStr = String.valueOf(realPayD);
				realPayStr = String.valueOf(mRealPay);
			} else {

				realPayTv.setText("0.00");
				realPayStr = "0";
			}

		}
	}

	public void checkPointsByHttp(String currentMountStr) {
		AsyncHttpClient asyncHttpClient = new AsyncHttpClient();
		asyncHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		asyncHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		String mUrl = fuelPaySharedPreferences.getString("mUrl",
				HttpParams.DEFAULT_URL);
		String mPort = fuelPaySharedPreferences.getString("mPort",
				HttpParams.DEFAULT_PORT);
		String wxplatform = fuelPaySharedPreferences
				.getString("wxplatform", "");
		String wxsku = fuelPaySharedPreferences.getString("wxsku", "");
		String chekcPointsUrl = mUrl + ":" + mPort + "/"+ HttpParams.checkPointsUrl;
		//String chekcPointsUrl = mUrl+"/"+ HttpParams.checkPointsUrl;
		RequestParams params = new RequestParams();
		params.put("wxplatform", wxplatform);
		params.put("wxsku", wxsku);
		params.put("amount", currentMountStr);
		JsonHttpResponseHandler responseHandler=new JsonHttpResponseHandler(){
			@Override
			public void onSuccess(int statusCode, JSONObject response) {
				// TODO 自动生成的方法存根
				super.onSuccess(statusCode, response);
				boolean success = false;
				try {
					success = response.getBoolean("success");
					if (success) {
						JSONObject data = (JSONObject) response
								.get("data");
						String amountCheck=data.getString("amount");
						String currentString=inputMoneyET.getText().toString();
						if(amountCheck.equals(currentString)){
							String points = data.getString("points");
							String pointsamount = data
									.getString("pointsamount");
						/*	BigDecimal b = new BigDecimal(pointsamount);
							double pointsamount2 = b.setScale(2,
									BigDecimal.ROUND_HALF_UP).doubleValue();*/
							double pointsamount2=Double.parseDouble(pointsamount);
							if(!pointsValableTv.getText().equals("")){
							pointsAmountUse = pointsamount2;
							pointsStr = String.valueOf(points);
							pointAmountStr = String.valueOf(pointsamount2);
							pointsValableTv
									.setText("可用" + pointsStr + "积分");
							pointsAmountTv.setText(pointAmountStr);
							if(usePointsCB.isChecked()){
								totalDiscount = 0.00;
								String vouchesforMoneyStr1 = vouchesValue.getText()
										.toString();
								if (vouchesforMoneyStr1 != "") {
									double vouchesDouble = Double
											.valueOf(vouchesforMoneyStr1);
									totalDiscount = totalDiscount-vouchesDouble;
								}
								totalDiscount = totalDiscount - pointsAmountUse;
								totalDiscountTv.setText(ToAccuracy2
										.getStringVal2(totalDiscount));
								inputMoneyStr=inputMoneyET.getText().toString();
								inputMoneyD=Double.parseDouble(inputMoneyStr);
								
								BigDecimal inputMB=new BigDecimal(Double.toString(inputMoneyD));
								BigDecimal totalDB=new BigDecimal(Double.toString(totalDiscount));
								double mRealPay=inputMB.add(totalDB).doubleValue();
								//realPayD = mRealPay;
								realPayD=inputMoneyD+totalDiscount;
							    //realPayStr=String.valueOf(realPayD);
								realPayStr=String.valueOf(mRealPay);
								realPayTv.setText(String.valueOf(mRealPay));
							}
						
							} else {
								//全部为0
								pointsAmountUse = 0.00;
								totalDiscount=0.00;
								pointsStr = "";
								pointAmountStr = "";
								pointsValableTv
										.setText("可用" + "0" + "积分");
								pointsAmountTv.setText(pointAmountStr);
								totalDiscountTv.setText("0");
							}
							
							Log.d(TAG, "success is true");
							Message canClick=Message.obtain();
							canClick.what=2;
							dealerHandler.sendMessage(canClick);
							
						}else{
							currentString=inputMoneyET.getText().toString();
							checkPointsByHttp(currentString);
							
						}
                       
						
						
					} else {
						Log.d(TAG, "success is false");
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}

			@Override
			public void onFailure(Throwable e, JSONObject errorResponse) {
				// TODO 自动生成的方法存根
				super.onFailure(e, errorResponse);
				Log.d(TAG, "onFailure");
			}	
			
		};
		asyncHttpClient.post(chekcPointsUrl, params,responseHandler);
	}

	public void delayCheckPoints(final String myinputMoneyStr) {
		int taskTime = 1000;

		// 先取消定时器中的任务
		if (tTask != null) {
			tTask.cancel();
		}
		tTask = null;

		// 再取消定时器
		if (mTimer != null) {
			mTimer.cancel();
		}
		mTimer = null;

		// 如果传入的值不是0
		if (myinputMoneyStr != "0") {
			// 再新建定时任务。
			tTask = new TimerTask() {
				@Override
				public void run() {
					// TODO 自动生成的方法存根
					Message message = new Message();
					message.what = 1;
					Bundle bundle = new Bundle();
					bundle.putString("myInput", myinputMoneyStr);
					message.setData(bundle);
					dealerHandler.sendMessage(message);
				}
			};
			// 最后新建定时器
			mTimer = new Timer();
			mTimer.schedule(tTask, taskTime);
		}
	}
	
	public void submitOrderByHttp(String payqrcode){
		//Toast.makeText(getApplicationContext(), "submitOrder", Toast.LENGTH_SHORT).show();
		AsyncHttpClient getCustomerClient=new AsyncHttpClient();
		getCustomerClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getCustomerClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		
		FuelSP=getSharedPreferences("cashiervalues", 0);
		String mUrl=FuelSP.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=FuelSP.getString("mPort", HttpParams.DEFAULT_PORT);
		String submiturl=mUrl+":"+mPort+"/"+HttpParams.submitOrderUrl;
		//String submiturl=mUrl+"/"+HttpParams.submitOrderUrl;
		Log.d("fUrl", submiturl);
		RequestParams submitparams=new RequestParams();
		Log.d("fUrl", submiturl);
		String ordertyeStr=FuelSP.getString("serivceType", "1");
		String ordertype=ordertyeStr;
		int stationId=FuelSP.getInt("stationid", -1);
		int userId=FuelSP.getInt("userId", -1);
		String userid=String.valueOf(userId);
		String stationid=String.valueOf(stationId);
		orderno=GenerateOrdeNO.orderIDGenarate(ordertyeStr,stationid);

		String wxplatform=FuelSP.getString("wxplatform", "");
		String wxsku=FuelSP.getString("wxsku", "");
		String voucher="";
		String voucheramount="0";
		String payamount=realPayStr;//bundle
		FuelSP=getSharedPreferences("cashiervalues",0);
		String cardNumber=FuelSP.getString("cardsNumber", "");
		
		
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
		
		submitparams.put("amount", inputMoneyStr);//bundle
		submitparams.put("points", pointsStr);//bundle
		submitparams.put("pointsamount", pointAmountStr);//bundle
		Log.d("MipcaActivity", "realPay"+realPayStr+"/"+"amount"+inputMoneyStr+"/"+"points"+pointsStr+"/"+"pointsamount"+pointAmountStr);
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
							SharedPreferences.Editor mscanpayeditor=FuelSP.edit();
							mscanpayeditor.putString("orderid", orderid);
							mscanpayeditor.commit();
							Bundle bundle=new Bundle();
							bundle.putString("activitytype", "1");
							bundle.putString("orderno", orderno);
							Intent toSuccessPaying=new Intent(FuelPay.this,SuccessResultActivity.class);
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
							SharedPreferences.Editor scanpayeditor=FuelSP.edit();
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
/*						mTimer=new Timer();//延迟3s
						tTask=new TimerTask(){

							@Override
							public void run() {
								// TODO 自动生成的方法存根
								Message message=Message.obtain();
								message.what=1;
								mHandler.sendMessage(message);
							}
						};
						mTimer.schedule(tTask, 3*1000);*/
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
		FuelSP=getSharedPreferences("cashiervalues", 0);
		String mUrl=FuelSP.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=FuelSP.getString("mPort", HttpParams.DEFAULT_PORT);
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
								//String payamount=mData.getString("payamount");
							    String payamountStr=String.valueOf(payamount);
								//String payamountStr=payamount;
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
						
								Intent toSuccess=new Intent(FuelPay.this,SuccessResultActivity.class);
								FuelPay.mFuelPay.finish();//finish fuelPay
								toSuccess.putExtras(bundle);
					    		startActivity(toSuccess);
					    		
					    		FuelPay.this.finish();
							}else{
								Toast.makeText(FuelPay.this, "数据异常", Toast.LENGTH_SHORT).show();
								Log.d(TAG, "data's length is0");
							}
						}else{
							Log.d(TAG, "success is false");
							Toast.makeText(FuelPay.this, "网络异常", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(FuelPay.this, "网络错误", Toast.LENGTH_SHORT).show();
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
					Toast.makeText(FuelPay.this, "网络错误", Toast.LENGTH_SHORT).show();
				}
				
			}
			
			
		});
	}

}
