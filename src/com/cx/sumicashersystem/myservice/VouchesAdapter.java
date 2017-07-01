package com.cx.sumicashersystem.myservice;

import java.util.ArrayList;




import com.cx.sumicashersystem.object.Vouches;
import com.cx.sumicashersystem.R;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;

public class VouchesAdapter extends BaseAdapter {
private ArrayList<Vouches> vouchesList;
private Context context;
private int resourceId;
private int selectID;
private OnMyClickChangedListener mChangeListener;

	public VouchesAdapter( Context context,int resourceId,ArrayList<Vouches> vouchesList) {
	super();
	this.vouchesList = vouchesList;
	this.context = context;
	this.resourceId = resourceId;
}
	public void setSelectID(int position){
		selectID=position;
	}
	
	public void setOncheckChanged(OnMyClickChangedListener l){
		mChangeListener=l;
	}
	
	public interface OnMyClickChangedListener{
		void setSelectID(int selectID);
	}

	@Override
	public int getCount() {
		return vouchesList.size();
	}

	@Override
	public Object getItem(int position) {
		return vouchesList.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) {
		Log.e("getView", "getView"+position);
		ViewHolder viewHolder;
		if(convertView==null){
			viewHolder=new ViewHolder();
			convertView=LayoutInflater.from(context).inflate(R.layout.vouches_item_layout, null);
			viewHolder.vouchesLabel=(TextView)convertView.findViewById(R.id.vouches_label);//how to connect convertview??
			viewHolder.vouchesVal=(TextView)convertView.findViewById(R.id.vouchesVal);
			viewHolder.vouchesPeriod=(TextView)convertView.findViewById(R.id.vouchesPeriod);
			viewHolder.vouchesCheck=(CheckBox)convertView.findViewById(R.id.vouche_rb);
			convertView.setTag(viewHolder);
		}else{
			viewHolder=(ViewHolder)convertView.getTag();
		}
		viewHolder.vouchesLabel.setText(vouchesList.get(position).getVouchesName());
		viewHolder.vouchesVal.setText(vouchesList.get(position).getVouchesValue());
		viewHolder.vouchesPeriod.setText(vouchesList.get(position).getValidPeriod());
		if(vouchesList.get(position).getState()==0){
			Log.e("vouchesCheck", "vouchesCheck"+position+"isFalse");
			viewHolder.vouchesCheck.setChecked(false);
		}else{
			Log.e("vouchesCheck", "vouchesCheck"+position+"true");
			viewHolder.vouchesCheck.setChecked(true);
		}
		if(selectID==position){
			if(viewHolder.vouchesCheck.isChecked()){
				Log.e("checkbox", "isChecked true");
				viewHolder.vouchesCheck.setChecked(false);
			}else{
				Log.e("checkbox", "isChecked false");
				viewHolder.vouchesCheck.setChecked(true);
			}
					
		}else{
			viewHolder.vouchesCheck.setChecked(false);
		}
		viewHolder.vouchesCheck.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				Log.e("voucherAdapter", "onClick");
				selectID=position;
				if(mChangeListener!=null){
					mChangeListener.setSelectID(selectID);
				}
			}
			
		});
		return convertView;
	}
class ViewHolder{
	TextView vouchesLabel;
	TextView vouchesVal;
	TextView vouchesPeriod;
	CheckBox vouchesCheck;
}
}
