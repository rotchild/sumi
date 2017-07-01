package com.cx.sumicashersystem;

import java.util.List;

import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.object.MonthRecord;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MonthRecordAdapter extends ArrayAdapter<MonthRecord> {
	private final String JIAYOU="1";
	private final String SHOUYIN="2";
	private int resourceId;
	private Context mContext;
	private List<MonthRecord> mList;

	public MonthRecordAdapter(Context context, int resource, List<MonthRecord> objects) {
		super(context, resource, objects);
		resourceId=resource;
		mContext=context;
		mList=objects;
	}
	

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		MonthRecord monthRecord=getItem(position);
		View view;
		ViewHolder viewHolder;
		if(convertView==null){
			view=LayoutInflater.from(mContext).inflate(resourceId, null);
			viewHolder=new ViewHolder();
			viewHolder.monthTitle=(TextView)view.findViewById(R.id.monthTitle_id);
			viewHolder.label=(TextView)view.findViewById(R.id.orderlabel_id);
			viewHolder.sum=(TextView)view.findViewById(R.id.sum_id);
			viewHolder.orderNum=(TextView)view.findViewById(R.id.ordernum_id);
			viewHolder.timeStr=(TextView)view.findViewById(R.id.timestr_id);
			view.setTag(viewHolder);
			Log.e("getView", "convertView is null");
		}else{
			view=convertView;
			viewHolder=(ViewHolder)view.getTag();
			Log.e("getView", "convertView is not null");
		}
		viewHolder.monthTitle.setText(monthRecord.getorderMonth());
		if(needTitle(position)){
			viewHolder.monthTitle.setVisibility(View.VISIBLE);
		}else{
			viewHolder.monthTitle.setVisibility(View.GONE);
		}
		
		
		if(monthRecord.getOrdertype().equals(JIAYOU)){
			viewHolder.label.setText("º””Õ");
			viewHolder.label.setTextColor(Color.rgb(255, 153, 51));
		}
		if(monthRecord.getOrdertype().equals(SHOUYIN)){
			viewHolder.label.setText(" ’“¯");
			viewHolder.label.setTextColor(Color.rgb(0, 102, 204));
		}
		viewHolder.sum.setText(monthRecord.getAmout());
		viewHolder.orderNum.setText(monthRecord.getOrderno());
		viewHolder.timeStr.setText(monthRecord.getTimeStr());
		return view;
	}
	@Override
	public int getCount() {
		if(null!=mList){
			return mList.size();
		}
		Log.e("getView", "mList is null");
		return 0;
	}
	@Override
	public MonthRecord getItem(int position) {
		if(null!=mList&&position<getCount()){
			return mList.get(position);
		}
		return null;
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	
	private boolean needTitle(int position){
		if(position==0){
			return true;
		}
		if(position<0){//????
			return false;
		}
		MonthRecord currentMR=getItem(position);
		MonthRecord previousMR=getItem(position-1);
		if(null==currentMR||null==previousMR){
			return false;
		}
		String currentTitle=currentMR.getorderMonth();
		String previousTitle=previousMR.getorderMonth();
		if(null==currentTitle||null==previousMR){
			return false;
		}
		if(currentTitle.equals(previousTitle)){
			return false;
		}
		return true;
	}
	
	class ViewHolder{
		TextView  monthTitle;
		TextView label;
		TextView sum;
		TextView orderNum;
		TextView timeStr;
	}
}
