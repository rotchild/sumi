package com.cx.sumicashersystem.mwidget;


import com.cx.sumicashersystem.R;

import android.app.Activity;
import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MOrderTitle extends LinearLayout{

	public MOrderTitle(Context context, AttributeSet attrs) {
		super(context, attrs);
		LayoutInflater.from(context).inflate(R.layout.order_top0,MOrderTitle.this);
		TextView titleback=(TextView)findViewById(R.id.back_tv);
		titleback.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				// TODO 自动生成的方法存根
				((Activity)getContext()).finish();
			}
			
		});
	}

}
