package com.cx.sumicashersystem.utils;

import java.text.DecimalFormat;

public class ToAccuracy2 {

public static String getStringVal2(double valueD){
	String result="";
	DecimalFormat df=new DecimalFormat("0.00");
	result=df.format(valueD);
	return result;
}

}
