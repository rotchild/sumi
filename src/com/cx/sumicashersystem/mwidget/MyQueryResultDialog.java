package com.cx.sumicashersystem.mwidget;


import com.cx.sumicashersystem.R;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.TextView;

public class MyQueryResultDialog extends Dialog {
	 int screenWidth;
	 int screenHeight;
	 private TextView queryTimeTv,queryCarTv,queryAmountTv;
	 Button querydialogCloseBtn;
	public MyQueryResultDialog(Context context, int themeResId) {
		super(context, themeResId);
		// TODO 自动生成的构造函数存根
		WindowManager wm=(WindowManager)getContext().getSystemService(Context.WINDOW_SERVICE);
		screenWidth=wm.getDefaultDisplay().getWidth();
		screenHeight=wm.getDefaultDisplay().getHeight();
	}
	
	public void setMyQueryDialog(){
		View mView=LayoutInflater.from(getContext()).inflate(R.layout.qureydialog_layout, null);
		queryTimeTv=(TextView)mView.findViewById(R.id.querytime_tv);
		queryCarTv=(TextView)mView.findViewById(R.id.query_cardnumber_tv);
		queryAmountTv=(TextView)mView.findViewById(R.id.query_amount_tv);
		querydialogCloseBtn=(Button)mView.findViewById(R.id.querydialogClose_btn);
		int dialogWidth=(int)(screenWidth*3/4);
		int dialogHeight=(int)(dialogWidth*3/5);
		querydialogCloseBtn.getLayoutParams().width=(int)(dialogWidth*1/2);
		super.addContentView(mView, new LayoutParams(dialogWidth,dialogHeight));
	}
	
	public void setQueryTime(String timeStr){
		queryTimeTv.setText(timeStr);
	}
	public void setQueryCard(String cardStr){
		queryCarTv.setText(cardStr);
	}
	
	public void setQueryAmount(String amountStr){
		queryAmountTv.setText(amountStr);
	}
	
	public void setCloseButtonListener(View.OnClickListener listener){
		querydialogCloseBtn.setOnClickListener(listener);
	}

}
