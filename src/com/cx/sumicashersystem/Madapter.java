package com.cx.sumicashersystem;

import java.util.List;

import com.cx.sumicashersystem.R;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class Madapter extends ArrayAdapter<String> {
	int resourceId;
	Context mContext;
	List<String> mObjects;

	public Madapter(Context context, int resource, List<String> objects) {
		super(context, resource, objects);
		resourceId=resource;
		mContext=context;
		mObjects=objects;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		String itemstr=mObjects.get(position);
		View view=LayoutInflater.from(mContext).inflate(resourceId, null);
		TextView mTV=(TextView)view.findViewById(R.id.tv);
		mTV.setText(itemstr);
		return view;
	}

}
