package com.cx.sumicashersystem;



import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.object.MonthRecord;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

public class OrderInforActivity extends Activity{
TextView orderNoTv;
TextView createTimeTv;
TextView amountTv;
TextView payMentTv;
TextView memberPointTv;
TextView vouchersTv;
TextView realPayTv;
TextView cashierTv;
TextView orderTitleTv;
MonthRecord monthRecordClick;
SharedPreferences orderInfoSharedPreferences;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.orderinforlayout);
	    monthRecordClick=(MonthRecord) this.getIntent().getSerializableExtra("monthRecordClick");
		initView();
	}
private void initView(){
orderInfoSharedPreferences=getSharedPreferences("cashiervalues",0);
String orderNoStr=monthRecordClick.getOrderno();
String createTimeStr=monthRecordClick.getTimeStr();
String amountStr=monthRecordClick.getAmout();
String payMentStr=monthRecordClick.getPayment();
String memberPointStr=monthRecordClick.getPoints();
String vouchersStr=monthRecordClick.getVoucherValue();
String realPayStr=monthRecordClick.getPayamount();
String cashierStr=monthRecordClick.getUserid();//����Աid
String cashierName=orderInfoSharedPreferences.getString("realName", "");
String orderTyeStr=monthRecordClick.getOrdertype();

orderTitleTv=(TextView)findViewById(R.id.ordertype_tv);
if(orderTyeStr.equals("2")){
	orderTitleTv.setText("��������");
}
orderNoTv=(TextView)findViewById(R.id.ordernoid);//������
orderNoTv.setText(orderNoStr);
createTimeTv=(TextView)findViewById(R.id.createtimeid);//����ʱ��
createTimeTv.setText(createTimeStr);
amountTv=(TextView)findViewById(R.id.amountid);//���
amountTv.setText(amountStr);
payMentTv=(TextView)findViewById(R.id.paymentid);
payMentTv.setText(payMentStr);
memberPointTv=(TextView)findViewById(R.id.memberpointsid);
memberPointTv.setText(memberPointStr);
vouchersTv=(TextView)findViewById(R.id.vouchersvalueid);//����ȯ
vouchersTv.setText("0");//����
realPayTv=(TextView)findViewById(R.id.realpayid);
realPayTv.setText(realPayStr);
cashierTv=(TextView)findViewById(R.id.cashierid);//����Աid??ת����
cashierTv.setText(cashierName);
}
}
