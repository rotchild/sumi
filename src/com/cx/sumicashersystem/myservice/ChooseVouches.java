package com.cx.sumicashersystem.myservice;

import java.util.ArrayList;

import com.cx.sumicashersystem.myservice.VouchesAdapter.OnMyClickChangedListener;
import com.cx.sumicashersystem.object.Vouches;
import com.cx.sumicashersystem.R;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

public class ChooseVouches extends Activity {
	SharedPreferences chooseVouchesSharedPreferences;
	LinearLayout invis;
	ListView mlist;
	VouchesAdapter mAdapter;
	int vouchesSelectPosition;
	int vouchesAmount;
	TextView goBackTv;
	private ArrayList<Vouches> mVouchesList=new ArrayList<Vouches>();
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO 自动生成的方法存根
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.choosevouches_layout);
		initView();
	}

private void initView(){
	goBackTv=(TextView)findViewById(R.id.vouchesback_tv);
	goBackTv.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			ChooseVouches.this.finish();
		}
		
	});
	chooseVouchesSharedPreferences=getSharedPreferences("cashiervalues",0);
	initData();
	invis=(LinearLayout)findViewById(R.id.invis);
    mlist=(ListView)findViewById(R.id.vouches_list);
	mlist.addFooterView(View.inflate(this, R.layout.bottombutton_layout, null));
     Log.e("mVouchesListSize", "mVouchesSize"+mVouchesList.size());
     mAdapter=new VouchesAdapter(this,R.layout.vouches_item_layout,mVouchesList);
     mAdapter.setOncheckChanged(new OnMyClickChangedListener(){

		@Override
		public void setSelectID(int selectID) {
			// TODO 自动生成的方法存根
			mAdapter.setSelectID(selectID);
			mAdapter.notifyDataSetChanged();
		}
    	 
     });
     mlist.setAdapter(mAdapter);
     mlist.setOnItemClickListener(new OnItemClickListener(){

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			Log.e("mList", "mList"+position);
			vouchesSelectPosition=position;
			mAdapter.setSelectID(position);
			mAdapter.notifyDataSetChanged();
		}
    	 
     });
	mlist.setOnScrollListener(new OnScrollListener(){

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {
			
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem,
				int visibleItemCount, int totalItemCount) {
			Log.e("onScroll", "firstVisibleItem"+firstVisibleItem);
			Log.e("onScroll", "visibleItemCount"+visibleItemCount);
			Log.e("onScroll", "totalItemCount"+totalItemCount);
			if(firstVisibleItem+visibleItemCount<totalItemCount){
				invis.setVisibility(View.VISIBLE);
			}else{
				invis.setVisibility(View.GONE);
			}
		}
		
	});
	Button vouchesCheckButton=(Button)findViewById(R.id.vouches_checkbt);
	vouchesCheckButton.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			Intent intentBack=new Intent();
			if(mlist.getCount()>1){//button is count1
				Log.d("chooseVouches", "mListCount"+mlist.getCount());
				Vouches selectVouches=(Vouches)mlist.getItemAtPosition(vouchesSelectPosition);
		           String vouchesValue=selectVouches.getVouchesValue();
		           intentBack.putExtra("vouchesVal", vouchesValue);	
			}else{
				
		           String vouchesValue="0";
		           intentBack.putExtra("vouchesVal", vouchesValue);
			}
			
	           setResult(1001,intentBack);
	           ChooseVouches.this.finish();	
		}
		
	});
	Button vouchesCheckButtonInvs=(Button)findViewById(R.id.vouches_checkbt_invis);
	vouchesCheckButtonInvs.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			Intent intentBack=new Intent();
		/*	if(vouchesAmount>0){
				Vouches selectVouches=(Vouches)mlist.getItemAtPosition(vouchesSelectPosition);
		           String vouchesValue=selectVouches.getVouchesValue();
		           intentBack.putExtra("vouchesVal", vouchesValue);	
			}else{
				String vouchesValue="0";
		           intentBack.putExtra("vouchesVal", vouchesValue);
			}*/
			if(mlist.getCount()>1){
				Vouches selectVouches=(Vouches)mlist.getItemAtPosition(vouchesSelectPosition);
		           String vouchesValue=selectVouches.getVouchesValue();
		           intentBack.putExtra("vouchesVal", vouchesValue);	
			}else{
				
		           String vouchesValue="0";
		           intentBack.putExtra("vouchesVal", vouchesValue);
			}
			
	           setResult(1001,intentBack);
	           ChooseVouches.this.finish();	
		}
		
	});
}
private void initData(){
	vouchesAmount=chooseVouchesSharedPreferences.getInt("vouchesAmount", -1);
	if(vouchesAmount>0){
		//examples
		Vouches vouches1=new Vouches("加油代金券","2","2017.02.1-2017.02.15",0);
		mVouchesList.add(vouches1);
		Vouches vouches2=new Vouches("加油代金券","2","2017.02.1-2017.02.15",0);
		mVouchesList.add(vouches2);
		Vouches vouches3=new Vouches("加油代金券","5","2017.02.1-2017.02.15",0);
		mVouchesList.add(vouches3);
		Vouches vouches4=new Vouches("加油代金券","5","2017.02.1-2017.02.15",0);
		mVouchesList.add(vouches4);
		Vouches vouches5=new Vouches("加油代金券","5","2017.02.1-2017.02.15",0);
		mVouchesList.add(vouches5);
	}else{
		
	}
	
}

}
