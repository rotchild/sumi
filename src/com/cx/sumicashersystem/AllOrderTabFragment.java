package com.cx.sumicashersystem;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.net.HttpParams;
import com.cx.sumicashersystem.object.FragmentOrderList;
import com.cx.sumicashersystem.object.MonthRecord;
import com.handmark.pulltorefresh.library.PullToRefreshBase;
import com.handmark.pulltorefresh.library.PullToRefreshListView;
import com.handmark.pulltorefresh.library.PullToRefreshBase.OnRefreshListener;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


public class AllOrderTabFragment extends Fragment {
	SharedPreferences allOrderTabSharedPreferences;
	//List<MonthRecord> monthRecordList;
	MonthRecordAdapter monthRecordeAdapter;
	
	PullToRefreshListView lv;
	//int start=0;//查询起点
	//int limit=0;//所查数据量
	TextView allorderTv;
	TextView fuelorderTv;
	TextView cashorderTv;

	@Override
	public void onAttach(Activity activity) {
		// TODO 自动生成的方法存根
		super.onAttach(activity);
		if(activity instanceof OrderManagerActivity){
			OrderManagerActivity omActivity=(OrderManagerActivity)activity;
			allorderTv=(TextView) omActivity.findViewById(R.id.all_order_tv);
			fuelorderTv=(TextView) omActivity.findViewById(R.id.fuel_order_tv);
			cashorderTv=(TextView) omActivity.findViewById(R.id.shop_order_tv);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.allorder_frag, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {//why is onActivityCreated use the listview adapter??
		super.onActivityCreated(savedInstanceState);
		Log.e("AllOrderTab", "onActivityCreated");
		if(FragmentOrderList.monthRecordList==null){
			FragmentOrderList.monthRecordList=new ArrayList<MonthRecord>();//can't be global,or it will add repeat/use ondestroy to resolve
		}
		if(FragmentOrderList.monthRecordList.size()==0){
			 getOrdersByHttp();
		}
		 lv=(PullToRefreshListView)getView().findViewById(R.id.lv);
		
		lv.setOnRefreshListener(new OnRefreshListener<ListView>(){

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO 自动生成的方法存根
				getOrdersByHttp();
				//start=start+limit;
			}
			
		});
		//id,orderno,orderdate,ordertype,userid,stationid,
		//wxplatform,wxsku,amout,points,pointsamount,voucherValue,payamount,payment;
		
			/*MonthRecord monthRecord1=new MonthRecord("id","201710100001","1489025086682","1","2","stationid","wxplatform",
					"wxsku","100","60","6","","94","");
			monthRecordList.add(monthRecord1);
			MonthRecord monthRecord2=new MonthRecord("id","201710100001","1489025086682","1","2","stationid","wxplatform",
					"wxsku","100","60","6","","94","");
			monthRecordList.add(monthRecord2);
			MonthRecord monthRecord3=new MonthRecord("id","201710100001","1489025086682","1","2","stationid","wxplatform",
					"wxsku","100","60","6","","94","");
			monthRecordList.add(monthRecord3);*/
		   
		    monthRecordeAdapter=new MonthRecordAdapter(this.getActivity(),R.layout.orderrecorder_layout,FragmentOrderList.monthRecordList);
			lv.setAdapter(monthRecordeAdapter);
			lv.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?> parent, View view,
						int position, long id) {//start posion is 1??
					Log.d("AllOrderTab", "position:"+position);
				MonthRecord monthRecordClicked=FragmentOrderList.monthRecordList.get(position-1);
				Bundle bundle=new Bundle();
				bundle.putSerializable("monthRecordClick", monthRecordClicked);
				Intent toOrderInfor=new Intent(getActivity(),OrderInforActivity.class);
				toOrderInfor.putExtras(bundle);
				startActivity(toOrderInfor);
				}
				
			});
	}
public void getOrdersByHttp(){
	Log.d("getOrderByHttp", "in getOrders");
	AsyncHttpClient getOrdersHttpClient=new AsyncHttpClient();
	getOrdersHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	getOrdersHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	allOrderTabSharedPreferences=getActivity().getSharedPreferences("cashiervalues",0);
	String mUrl=allOrderTabSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=allOrderTabSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
	String getOrderUrl=mUrl+":"+mPort+"/"+HttpParams.getOrdersUrl;
//	String getOrderUrl=mUrl+"/"+HttpParams.getOrdersUrl;
	RequestParams ordersparams=new RequestParams();
	int userid=allOrderTabSharedPreferences.getInt("userId", -1);
	int stationid=allOrderTabSharedPreferences.getInt("stationid", -1);
	String useridStr=String.valueOf(userid);
	String stationidStr=String.valueOf(stationid);
	String startStr=String.valueOf(FragmentOrderList.static_start);
	String limitStr=String.valueOf(FragmentOrderList.static_limit);
	ordersparams.put("ordertype","0");//0全部，1加油，2购物
	ordersparams.put("userid",useridStr);
	ordersparams.put("stationid",stationidStr);
	ordersparams.put("start",startStr);
	ordersparams.put("limit",limitStr);
	Log.d("getOrderUrl", getOrderUrl);
	Log.d("getOrderUrl", "userid:"+useridStr);
	Log.d("getOrderUrl", "stationid"+stationidStr);
	Log.d("getOrderUrl", "start"+startStr);
	Log.d("getOrderUrl", "limit"+limitStr);
	
	getOrdersHttpClient.post(getOrderUrl, ordersparams,new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO 自动生成的方法存根
			Log.d("getOrderByHttp", "onSuccess");
			super.onSuccess(statusCode, response);
			boolean success=false;
			try{
				success=response.getBoolean("success");
				
				if(success){
				JSONArray data=response.getJSONArray("data");
				
				if(data.length()>0){
					FragmentOrderList.static_start+=data.length();
					/*MonthRecord monthRecord1=new MonthRecord("本月","加油","200.00","4202016122222","2016-12-23 12:33");
					monthRecordList.add(monthRecord1);*/
					ArrayList<MonthRecord> tempList=new ArrayList<MonthRecord>();
					for(int i=0;i<data.length();i++){
						JSONObject mData=(JSONObject) data.get(i);
						int idint=mData.getInt("id");
						String idStr=String.valueOf(idint);
						Log.d("getOrderByHttp", "idStr:"+idStr);
						String ordernoStr=mData.getString("orderno");
						String orderdate=mData.getString("orderdate");
						int orderType=mData.getInt("ordertype");
						if(orderType==1){
							FragmentOrderList.fuelOrderCount++;
						}else if(orderType==2){
							FragmentOrderList.cashOrderCount++;
						}
						FragmentOrderList.allOrderCount++;
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
						Log.d("getOrderByHttp", "payment"+payment);
						MonthRecord monthRecorder=new MonthRecord(idStr,ordernoStr,orderdate
								,orderTypeStr,useridStr,stationidStr,wxplatform,
								wxsku,amountStr,pointsStr,pointsamountStr,voucherStr,
								payamountStr,payment);
						tempList.add(monthRecorder);
					}
					for(int i=0;i<tempList.size();i++){
						Log.d("getOrderByHttp", "templist"+i);
						FragmentOrderList.monthRecordList.add(tempList.get(i));
					}
					monthRecordeAdapter.notifyDataSetChanged();
					lv.onRefreshComplete();
					allorderTv.setText("全部订单"+"("+FragmentOrderList.allOrderCount+")");
					fuelorderTv.setText("加油订单"+"("+FragmentOrderList.fuelOrderCount+")");
					cashorderTv.setText("购物订单"+"("+FragmentOrderList.cashOrderCount+")");
				}else{//data.length=0
					monthRecordeAdapter.notifyDataSetChanged();
					lv.onRefreshComplete();
					Toast.makeText(getContext(), "暂时没有新记录", Toast.LENGTH_SHORT).show();
				}
				}
			}catch(JSONException e){
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			// TODO 自动生成的方法存根
			Log.d("getOrderByHttp", "onFailure");
			lv.onRefreshComplete();
			super.onFailure(e, errorResponse);
		}
		
		@Override
		public void onFailure(Throwable e, String errorResponse) {
			// TODO 自动生成的方法存根
			Log.d("getOrderByHttp", "onFailure");
			lv.onRefreshComplete();
			super.onFailure(e, errorResponse);
		}

		@Override
		public void onFinish() {
			// TODO 自动生成的方法存根
			Log.d("getOrderByHttp", "onFinish");
			//lv.onRefreshComplete();
			super.onFinish();
		}
		
		
		
	});
}



@Override
public void setUserVisibleHint(boolean isVisibleToUser) {
	// TODO 自动生成的方法存根
	super.setUserVisibleHint(isVisibleToUser);
	if(isVisibleToUser){
		if(monthRecordeAdapter!=null){
			monthRecordeAdapter.notifyDataSetChanged();
		}
	}
}

@Override
public void onDestroy() {
	// TODO 自动生成的方法存根
	FragmentOrderList.monthRecordList=null;
	FragmentOrderList.allOrderCount=0;
	FragmentOrderList.fuelOrderCount=0;
	FragmentOrderList.cashOrderCount=0;
	Log.e("AllOrderTab", "onDestroy");
	super.onDestroy();
}


}
