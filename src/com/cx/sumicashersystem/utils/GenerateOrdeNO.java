package com.cx.sumicashersystem.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class GenerateOrdeNO {
	public static String orderIDGenarate(String orderTypeStr,String stationidStr){
		String orderId="";
		String mOrderType="";
		if(orderTypeStr.equals("1")){//加油订单
			mOrderType="J";
		}else if(orderTypeStr.equals("2")){//收银订单
			mOrderType="S";
		}
		int random1=(int)(Math.random()*(10));
		int random2=(int)(Math.random()*(10));
		long now=System.currentTimeMillis();
		SimpleDateFormat formatter=new SimpleDateFormat("yyyyMMddHHmmssSSS");
		String timestr=formatter.format(new Date());
		orderId=mOrderType+stationidStr+String.valueOf(random1)+String.valueOf(random2)+timestr;
		return orderId;
	}
}
