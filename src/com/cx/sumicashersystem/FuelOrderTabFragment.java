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

public class FuelOrderTabFragment extends Fragment {
	SharedPreferences fuelOrderTabSharedPreferences;
	List<MonthRecord> FuelmonthRecordList;
	PullToRefreshListView fuellv;
	MonthRecordAdapter FuelmonthRecordeAdapter;
	//int start=0;//��ѯ���
	//int limit=0;//����������
	TextView allorderTv;
	TextView fuelorderTv;
	TextView cashorderTv;
	
/*	public FuelOrderTabFragment(int start_2,int limit_2) {
		super();
		start=start_2;
		limit=limit_2;
	}*/
	
	@Override
	public void onAttach(Activity activity) {
		// TODO �Զ����ɵķ������
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
		Log.e("AllOrderTab", "FuelonCreatView");
		return inflater.inflate(R.layout.fuelorder_frag, container, false);
	}
	
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		// TODO �Զ����ɵķ������
		Log.e("AllOrderTab", "FuelOrderonActivityCreated");
		super.onActivityCreated(savedInstanceState);
		FuelmonthRecordList=new ArrayList<MonthRecord>();
		fuellv=(PullToRefreshListView)getView().findViewById(R.id.lv_fuelorder);
		fuellv.setOnRefreshListener(new OnRefreshListener<ListView>(){

			@Override
			public void onRefresh(PullToRefreshBase<ListView> refreshView) {
				// TODO �Զ����ɵķ������
				getFuelOrdersByHttp();
				//start=start+limit;
			}
			
		});
		//getFuelOrdersByHttp();
		if(FragmentOrderList.monthRecordList!=null){
			for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
				MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
				String orderType=mRecord.getOrdertype();
				if(orderType.equals("1")){
					FuelmonthRecordList.add(mRecord);
				}
			}
		}
		FuelmonthRecordeAdapter=new MonthRecordAdapter(this.getActivity(),R.layout.orderrecorder_layout,FuelmonthRecordList);
		fuellv.setAdapter(FuelmonthRecordeAdapter);
		fuellv.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {//start posion is 1??
				Log.d("AllOrderTab", "position:"+position);
			MonthRecord monthRecordClicked=FuelmonthRecordList.get(position-1);
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
		// TODO �Զ����ɵķ������
		Log.e("AllOrderTab", "FuelOrderonDestroy");
		FuelmonthRecordList.clear();
		super.onDestroy();
	}
	
public void showList(){
	if(FragmentOrderList.monthRecordList!=null){
		FuelmonthRecordList.clear();
		for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
			MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
			String orderType=mRecord.getOrdertype();
			if(orderType.equals("1")){
				FuelmonthRecordList.add(mRecord);
			}
		}
	}
	FuelmonthRecordeAdapter.notifyDataSetChanged();
}	

public void getFuelOrdersByHttp(){
	AsyncHttpClient getOrdersHttpClient=new AsyncHttpClient();
	getOrdersHttpClient.addHeader("Charset", HttpParams.DEFAULT_CHARSET);
	getOrdersHttpClient.setTimeout(HttpParams.DEFAULT_TIME_OUT);
	fuelOrderTabSharedPreferences=getActivity().getSharedPreferences("cashiervalues",0);
	String mUrl=fuelOrderTabSharedPreferences.getString("mUrl", HttpParams.DEFAULT_URL);
	String mPort=fuelOrderTabSharedPreferences.getString("mPort", HttpParams.DEFAULT_PORT);
	String getOrderUrl=mUrl+":"+mPort+"/"+HttpParams.getOrdersUrl;
	//String getOrderUrl=mUrl+"/"+HttpParams.getOrdersUrl;
	RequestParams ordersparams=new RequestParams();
	int userid=fuelOrderTabSharedPreferences.getInt("userId", -1);
	int stationid=fuelOrderTabSharedPreferences.getInt("stationid", -1);
	String useridStr=String.valueOf(userid);
	String stationidStr=String.valueOf(stationid);
	String startStr=String.valueOf(FragmentOrderList.static_start);
	String limitStr=String.valueOf(FragmentOrderList.static_limit);
	ordersparams.put("userid",useridStr);
	ordersparams.put("stationid",stationidStr);
	ordersparams.put("ordertype","0");//0ȫ����1���ͣ�2����
	ordersparams.put("start",startStr);
	ordersparams.put("limit",limitStr);
	getOrdersHttpClient.post(getOrderUrl, ordersparams, new JsonHttpResponseHandler(){

		@Override
		public void onSuccess(int statusCode, JSONObject response) {
			// TODO �Զ����ɵķ������
			super.onSuccess(statusCode, response);
			boolean success=false;
			try{
				success=response.getBoolean("success");
				if(success){
					JSONArray data=response.getJSONArray("data");
					if(data.length()>0){
						FragmentOrderList.static_start+=data.length();
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
						   FuelmonthRecordList.clear();
							for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
								MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
								String orderType=mRecord.getOrdertype();
								if(orderType.equals("1")){
									FuelmonthRecordList.add(mRecord);
								}
							}
						
						FuelmonthRecordeAdapter.notifyDataSetChanged();
						fuellv.onRefreshComplete();
						allorderTv.setText("ȫ������"+"("+FragmentOrderList.allOrderCount+")");
						fuelorderTv.setText("���Ͷ���"+"("+FragmentOrderList.fuelOrderCount+")");
						cashorderTv.setText("���ﶩ��"+"("+FragmentOrderList.cashOrderCount+")");
						
					}else{
						FuelmonthRecordeAdapter.notifyDataSetChanged();
						fuellv.onRefreshComplete();
						Toast.makeText(getContext(), "��ʱû���¼�¼", Toast.LENGTH_SHORT).show();
					}
				}
			}catch(JSONException e){
				e.printStackTrace();
			}
		}

		@Override
		public void onFailure(Throwable e, JSONObject errorResponse) {
			// TODO �Զ����ɵķ������
			fuellv.onRefreshComplete();
			super.onFailure(e, errorResponse);
		}
		
		@Override
		public void onFailure(Throwable e, String errorResponse) {
			// TODO �Զ����ɵķ������
			fuellv.onRefreshComplete();
			super.onFailure(e, errorResponse);
		}
		
	});
}

@Override
public void setUserVisibleHint(boolean isVisibleToUser) {
	// TODO �Զ����ɵķ������
	super.setUserVisibleHint(isVisibleToUser);
	if(isVisibleToUser){
		if(FuelmonthRecordeAdapter!=null){
			FuelmonthRecordeAdapter.notifyDataSetChanged();
			if(FuelmonthRecordList!=null){
				   FuelmonthRecordList.clear();
					for(int i=0;i<FragmentOrderList.monthRecordList.size();i++){
						MonthRecord mRecord=FragmentOrderList.monthRecordList.get(i);
						String orderType=mRecord.getOrdertype();
						if(orderType.equals("1")){
							FuelmonthRecordList.add(mRecord);
						}
					}
			}
		}
	}
}
}
