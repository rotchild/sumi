package com.cx.sumicashersystem.myservice;



import java.text.SimpleDateFormat;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cx.sumicashersystem.ipset.PasswordSetActivity;
import com.cx.sumicashersystem.mwidget.MyQueryResultDialog;
import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.R;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;

public class MyService extends Activity {
	SharedPreferences myServiceSP;
	Button queryAmountBtn;//查询余额
	TextView amountTv;
	ProgressDialog queryPd;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.myservice_layout);
		initView();
	}
private void initView(){
	myServiceSP=getSharedPreferences("cashiervalues",0);
	queryAmountBtn=(Button)findViewById(R.id.myservice_queryamount_btn);
	queryAmountBtn.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {//调用查询请求
			// TODO 自动生成的方法存根
			/*Date currentTime=new Date();
			SimpleDateFormat df=new SimpleDateFormat("yyyy-MM-dd HH:mm");
			String currentTimeStr=df.format(currentTime);
			queryAmountByHttp(currentTimeStr);*/
			SharedPreferences cardSP=getSharedPreferences("cashiervalues",0);
			String cardno=cardSP.getString("cardsNumber", "");
			queryByQueryAmount(cardno);
		}
		
	});
	
	final LinearLayout menuTv=(LinearLayout)findViewById(R.id.menuLinear);
	ImageView fuelImage=(ImageView)findViewById(R.id.fuelservice_iv);
	ImageView shopImage=(ImageView)findViewById(R.id.shopservice_iv);
	TextView cardNumberTv=(TextView)findViewById(R.id.cardnumber_tv);
	amountTv=(TextView)findViewById(R.id.amount_tv);
	TextView cashierNameTv=(TextView)findViewById(R.id.cashiername_tv);
	TextView backTv=(TextView)findViewById(R.id.service_back_tv);
	backTv.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			MyService.this.finish();
		}
		
	});
	
	String cardNumber0=myServiceSP.getString("cardsNumber", "");
	String amount=myServiceSP.getString("amount", "");
	String realNameStr=myServiceSP.getString("realName", "");
	cardNumberTv.setText(cardNumber0);
	amountTv.setText(amount);
	cashierNameTv.setText(realNameStr);
	menuTv.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			View popupView=MyService.this.getLayoutInflater().inflate(R.layout.menupopup_layout,null);
			final PopupWindow popupwindow=new PopupWindow(popupView,180,120);
			popupwindow.setAnimationStyle(R.style.popupwindowanim);
			popupwindow.setBackgroundDrawable(new ColorDrawable(Color.parseColor("#00F8F8F8")));
			popupwindow.setFocusable(true);
			popupwindow.setOutsideTouchable(true);
			popupwindow.update();
			popupwindow.showAsDropDown(menuTv, -2, -10);
			LinearLayout passwordSetLayout=(LinearLayout)popupView.findViewById(R.id.passwordset_linear_layout);
			LinearLayout logoutLayout=(LinearLayout)popupView.findViewById(R.id.logout_linear_layout);
			passwordSetLayout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO 自动生成的方法存根
				Intent toSetPassword=new Intent(MyService.this,PasswordSetActivity.class);
				startActivity(toSetPassword);
				popupwindow.dismiss();
				}
				
			});
			logoutLayout.setOnClickListener(new OnClickListener(){

				@Override
				public void onClick(View v) {
					// TODO 自动生成的方法存根
						SharedPreferences.Editor myServiceEditor=myServiceSP.edit();
						myServiceEditor.putInt("loginState", 0);
						myServiceEditor.commit();
						MyService.this.finish();
						System.exit(0);//breakout??
					}		
			});
		}
		
	});
	
	fuelImage.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			Intent fuelIntent=new Intent(MyService.this,MipcaActivityCapture.class);
			Bundle bundle = new Bundle();
			bundle.putString("scanType", "scanCustomerInfor");
			fuelIntent.putExtras(bundle);
			SharedPreferences.Editor myEditor=myServiceSP.edit();
			myEditor.putString("serivceType", "1");//加油为1,收银为2;
			myEditor.commit();
			startActivity(fuelIntent);
		}
		
	});
	
	shopImage.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			Intent shopIntent=new Intent(MyService.this,MipcaActivityCapture.class);
			Bundle bundle = new Bundle();
			bundle.putString("scanType", "scanCustomerInfor");
			shopIntent.putExtras(bundle);
			SharedPreferences.Editor myEditor=myServiceSP.edit();
			myEditor.putString("serivceType", "2");//加油为1,收银为2;
			myEditor.commit();
			startActivity(shopIntent);
		}
		
	});
	
}

public void showQueryResultDialog(String queryTimeStr,final String queryCardStr,final String queryAmountStr){
	final MyQueryResultDialog myQueryDialog=new MyQueryResultDialog(MyService.this,R.style.Dialog);
	myQueryDialog.setMyQueryDialog();
	myQueryDialog.setQueryTime(queryTimeStr);
	myQueryDialog.setQueryCard(queryCardStr);
	myQueryDialog.setQueryAmount(queryAmountStr);
	myQueryDialog.setCancelable(false);
	myQueryDialog.setCloseButtonListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
		if(myQueryDialog!=null&&myQueryDialog.isShowing()){
			myQueryDialog.dismiss();
			amountTv.setText(queryAmountStr);
			//amountTv.setText(queryAmountStr);
		}	
		}
		
	});
	myQueryDialog.show();
}

public void queryAmountByHttp(final String currentTimeStr){//通过getCards接口查询的
	AsyncHttpClient queyHttpClient=new AsyncHttpClient();
	queyHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	queyHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	if(myServiceSP==null){
		myServiceSP=getSharedPreferences("cashiervalues",0);
	}
	String mUrl=myServiceSP.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=myServiceSP.getString("mPort", HttpParams.DEFAULT_PORT);
	String queryUrl=mUrl+":"+mPort+"/"+HttpParams.getCardsUrl;//getCarUrl可以获得amount,通过stationid
	RequestParams queryParams=new RequestParams();
	int stationidGet=0;
	String stationidStr="";
	stationidGet=myServiceSP.getInt("stationid", 0);
	stationidStr=String.valueOf(stationidGet);
	queryParams.put("stationid", stationidStr);
	
	queryPd=ProgressDialog.show(MyService.this, "查询中", "请稍候...");
	queyHttpClient.post(queryUrl, queryParams,new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(statusCode, response);
			if(queryPd!=null&&queryPd.isShowing()){
				queryPd.dismiss();
				queryPd=null;
			}
			boolean success=false;
			try{
				success=response.getBoolean("success");
				if(success){//可能结构会修改
					JSONArray data=response.getJSONArray("data");
					if(data.length()>0){
						JSONObject data0=(JSONObject) data.get(0);
						String cardsNumber=data0.getString("cardno");
						String amount=data0.getString("amount");
						SharedPreferences.Editor myServiceEditor=myServiceSP.edit();
						myServiceEditor.putString("cardsNumber", cardsNumber);
						myServiceEditor.putString("amount", amount);
						myServiceEditor.commit();
						
						showQueryResultDialog(currentTimeStr,cardsNumber,amount);
						
						Log.d("loginActivity", "cardsNumber"+cardsNumber+"/"
						+"amount"+amount);
					}else{
						Toast.makeText(MyService.this, "数据为空", Toast.LENGTH_SHORT).show();
					}
				}else{
					Toast.makeText(MyService.this, "服务器返回错误", Toast.LENGTH_SHORT).show();	
				}
			}catch(JSONException e){
				Toast.makeText(MyService.this, "解析错误", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			if(queryPd!=null&&queryPd.isShowing()){
				queryPd.dismiss();
				queryPd=null;
			}
			Toast.makeText(MyService.this, "连接错误", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onFailure(Throwable e, String errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			if(queryPd!=null&&queryPd.isShowing()){
				queryPd.dismiss();
				queryPd=null;
			}
			Toast.makeText(MyService.this, "连接错误", Toast.LENGTH_SHORT).show();
		}
		
		
	});
}

public void queryByQueryAmount(final String cardNo){
	AsyncHttpClient queyHttpClient=new AsyncHttpClient();
	queyHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	queyHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	if(myServiceSP==null){
		myServiceSP=getSharedPreferences("cashiervalues",0);
	}
	String mUrl=myServiceSP.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=myServiceSP.getString("mPort", HttpParams.DEFAULT_PORT);
	String queryUrl=mUrl+":"+mPort+"/"+HttpParams.queryAmountUrl;//getCarUrl可以获得amount,通过stationid
	RequestParams queryParams=new RequestParams();
	queryParams.put("cardno", cardNo);
	queryPd=ProgressDialog.show(MyService.this, "查询中", "请稍候...");
	queyHttpClient.post(queryUrl, queryParams,new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			super.onSuccess(statusCode, response);
			if(queryPd!=null&&queryPd.isShowing()){
				queryPd.dismiss();
				queryPd=null;
			}
			boolean success=false;
			try{
				success=response.getBoolean("success");
				if(success){
					JSONObject data=response.getJSONObject("data");
					String cardNoGet=data.getString("cardno");
					String sysTime=data.getString("systime");
					String amount=data.getString("amount");
					if(!cardNo.equals(cardNoGet)){
						Toast.makeText(MyService.this, "卡号核对错误", Toast.LENGTH_SHORT).show();
					}else{
						SharedPreferences.Editor myServiceEditor=myServiceSP.edit();
						myServiceEditor.putString("cardsNumber", cardNoGet);
						myServiceEditor.putString("amount", amount);
						myServiceEditor.commit();
						showQueryResultDialog(sysTime,cardNoGet,amount);
					}
				}else{
					Toast.makeText(MyService.this, "服务器返回错误", Toast.LENGTH_SHORT).show();
				}
			}catch(JSONException e){
				Toast.makeText(MyService.this, "解析错误", Toast.LENGTH_SHORT).show();
			}
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			if(queryPd!=null&&queryPd.isShowing()){
				queryPd.dismiss();
				queryPd=null;
			}
			Toast.makeText(MyService.this, "连接错误", Toast.LENGTH_SHORT).show();
		}
		
		@Override
		public void onFailure(Throwable e, String errorResponse) {
			// TODO 自动生成的方法存根
			super.onFailure(e, errorResponse);
			if(queryPd!=null&&queryPd.isShowing()){
				queryPd.dismiss();
				queryPd=null;
			}
			Toast.makeText(MyService.this, "连接错误", Toast.LENGTH_SHORT).show();
		}
		
	} );
}

}
