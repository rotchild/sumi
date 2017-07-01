package com.cx.sumicashersystem.object;

public class Vouches {
private String vouchesName;
private String vouchesValue;
private String validPeriod;
private int state;//Ñ¡ÖÐ×´Ì¬

public Vouches(String vouchesName, String vouchesValue, String validPeriod,int state) {
	super();
	this.vouchesName = vouchesName;
	this.vouchesValue = vouchesValue;
	this.validPeriod = validPeriod;
	this.state=state=state;
}
public String getVouchesName() {
	return vouchesName;
}
public void setVouchesName(String vouchesName) {
	this.vouchesName = vouchesName;
}
public String getVouchesValue() {
	return vouchesValue;
}
public int getState(){
	return state;
}

public void setVouchesValue(String vouchesValue) {
	this.vouchesValue = vouchesValue;
}
public String getValidPeriod() {
	return validPeriod;
}
public void setValidPeriod(String validPeriod) {
	this.validPeriod = validPeriod;
}
public void setState(int state){
	this.state=state;
}
}
