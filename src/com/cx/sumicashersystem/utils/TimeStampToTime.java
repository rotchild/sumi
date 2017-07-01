package com.cx.sumicashersystem.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

public class TimeStampToTime {
public static String getTime(String timeStamp){
	String timeStr="";
	Date mDate;
	SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	long l = Long.valueOf(timeStamp); 
	mDate=new Date(l);
	timeStr=sdf.format(mDate);
	return timeStr;
}
}
