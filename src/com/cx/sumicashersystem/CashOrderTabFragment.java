package com.cx.sumicashersystem;

import java.util.ArrayList;
import java.util.List;

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
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;

public class CashOrderTabFragment extends Fragment {
	SharedPreferences cashOrderTabSharedPreferences;
	List<MonthRecord> CashmonthRecordList;
	PullToRefreshListView cashlv;
	MonthRecordAdapter CashmonthRecordeAdapter;
	int start=0;//查询起点
	int limit=0;//所查数据量
	TextView allorderTv;
	TextView fuelorderTv;
	TextView cashorderTv;
	
/*	public CashOrderTabFragment(int start_1,int limit_1) {
		super();
		start=start_1;
		limit=limit_1;
	}*/
	
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
	
		return inflater.inflate(R.layout.cashorder_frag, container, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		Log.e("AllOrderTab", "CashorderonActivityCreated");
		super.onActivityCreated(savedInstanceState);
		CashmonthRecordList=new ArrayList<MonthRecord>();
		cashlv=(PullToRefreshListView)getView().findViewById(R.id.lv_cashorder);
		cashlv.setOnRefreshListener(new OnRefreshListener<ListView>(){

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO 自动生成的方法存根
				getCashOrdersByHttp();
				//start=start+limit;
			}
			
		});
		//getCashOrdersByHttp();
		if(FragmentOrderList.monthRecordList!=null){
			for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
				MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
				String orderType=mRecord.getOrdertype();
				if(orderType.equals("2")){
					CashmonthRecordList.add(mRecord);
				}
			}
		}
		CashmonthRecordeAdapter=new MonthRecordAdapter(this.getActivity(),R.layout.orderrecorder_layout,CashmonthRecordList);
		cashlv.setAdapter(CashmonthRecordeAdapter);
		cashlv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {//start posion is 1??
				Log.d("AllOrderTab", "position:"+position);
			MonthRecord monthRecordClicked=CashmonthRecordList.get(position-1);
			Bundle bundle=new Bundle();
			bundle.putSerializable("monthRecordClick", monthRecordClicked);
			Intent toOrderInfor=new Intent(getActivity(),OrderInforActivity.class);
			toOrderInfor.putExtras(bundle);
			startActivity(toOrderInfor);
			}
			
		});
	}

	@Override
	public void onDestroy() {
		// TODO 自动生成的方法存根
		Log.e("AllOrderTab", "CashorderonDestroy");
		CashmonthRecordList.clear();
		super.onDestroy();
	}
	
	public void showList(){
		if(FragmentOrderList.monthRecordList!=null){
			CashmonthRecordList.clear();
			for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
				MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
				String orderType=mRecord.getOrdertype();
				if(orderType.equals("2")){
					CashmonthRecordList.add(mRecord);
				}
			}
		}
		CashmonthRecordeAdapter.notifyDataSetChanged();
	}
	
	
	public void getCashOrdersByHttp(){
		AsyncHttpClient getOrdersHttpClient=new AsyncHttpClient();
		getOrdersHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
		getOrdersHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
		cashOrderTabSharedPreferences=getActivity().getSharedPreferences("cashiervalues",0);
		String mUrl=cashOrderTabSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
		String mPort=cashOrderTabSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
		String getOrderUrl=mUrl+":"+mPort+"/"+HttpParams.getOrdersUrl;
		//String getOrderUrl=mUrl+"/"+HttpParams.getOrdersUrl;
		RequestParams ordersparams=new RequestParams();
		int userid=cashOrderTabSharedPreferences.getInt("userId", -1);
		int stationid=cashOrderTabSharedPreferences.getInt("stationid", -1);
		String useridStr=String.valueOf(userid);
		String stationidStr=String.valueOf(stationid);
		String startStr=String.valueOf(start);
		String limitStr=String.valueOf(limit);
		ordersparams.put("userid",useridStr);
		ordersparams.put("stationid",stationidStr);
		ordersparams.put("ordertype","2");//0全部，1加油，2购物
		ordersparams.put("start",startStr);
		ordersparams.put("limit",limitStr);
		getOrdersHttpClient.post(getOrderUrl, ordersparams, new JsonHttpResponseHandler(){

			@Override
			public void onSuccess(JSONObject response) {
				// TODO 自动生成的方法存根
				super.onSuccess(response);
				boolean success=false;
				try{
					success=response.getBoolean("success");
					if(success){
						JSONArray data=response.getJSONArray("data");
						if(data.length()>0){
							start=start+data.length();
							ArrayList<MonthRecord> tempList=new ArrayList<MonthRecord>();
							for(int i=0;i<data.length();i++){
								JSONObject mData=(JSONObject) data.get(i);
								int orderType=mData.getInt("ordertype");
									int idint=mData.getInt("id");
									String idStr=String.valueOf(idint);
									Log.d("getOrderByHttp", "idStr:"+idStr);
									String ordernoStr=mData.getString("orderno");
									String orderdate=mData.getString("orderdate");
									orderType=mData.getInt("ordertype");
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
							CashmonthRecordList.clear();
							for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
								MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
								String orderType=mRecord.getOrdertype();
								if(orderType.equals("2")){
									CashmonthRecordList.add(mRecord);
								}
							}
							CashmonthRecordeAdapter.notifyDataSetChanged();
							cashlv.onRefreshComplete();
							allorderTv.setText("全部订单"+"("+FragmentOrderList.allOrderCount+")");
							fuelorderTv.setText("加油订单"+"("+FragmentOrderList.fuelOrderCount+")");
							cashorderTv.setText("购物订单"+"("+FragmentOrderList.cashOrderCount+")");
							
						}else{
							CashmonthRecordeAdapter.notifyDataSetChanged();
							cashlv.onRefreshComplete();
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
				cashlv.onRefreshComplete();
				super.onFailure(e, errorResponse);
			}
			
			@Override
			public void onFailure(Throwable e, String errorResponse) {
				// TODO 自动生成的方法存根
				cashlv.onRefreshComplete();
				super.onFailure(e, errorResponse);
			}
			
		});
	}

}
