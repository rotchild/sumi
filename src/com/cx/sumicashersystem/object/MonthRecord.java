package com.cx.sumicashersystem.object;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class MonthRecord implements Serializable {
	private static final long serialVersionUID=1L;//???
	private String id,orderno,orderdate,ordertype,ordermonth,userid,stationid,
	wxplatform,wxsku,amout,points,pointsamount,voucherValue,payamount,payment;
    private String timeStr,orderMonth;//自动计算得到，构造时不传入
	public MonthRecord(String id, String orderno, String orderdate,
			String ordertype, String userid, String stationid,
			String wxplatform, String wxsku, String amout, String points,
			String pointsamount, String voucherValue, String payamount,
			String payment) {
		super();
		this.id = id;
		this.orderno = orderno;
		this.orderdate = orderdate;
		this.ordertype = ordertype;
		this.userid = userid;
		this.stationid = stationid;
		this.wxplatform = wxplatform;
		this.wxsku = wxsku;
		this.amout = amout;
		this.points = points;
		this.pointsamount = pointsamount;
		this.voucherValue = voucherValue;
		this.payamount = payamount;
		this.payment = payment;
		gerateOrderMonth(orderdate);
	}
	
	public  void gerateOrderMonth(String orderdate){
		Date mDate;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		long l = Long.valueOf(orderdate); 
		mDate=new Date(l);
		timeStr=sdf.format(mDate);
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(mDate);
		int month=calendar.get(Calendar.MONTH);
		orderMonth=String.valueOf(month+1)+"月";
		
	}
	
	public String getTimeStr(){
		return timeStr;
	}
	
	public String getorderMonth(){
		return orderMonth;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getOrderno() {
		return orderno;
	}

	public void setOrderno(String orderno) {
		this.orderno = orderno;
	}

	public String getOrderdate() {
		return orderdate;
	}

	public void setOrderdate(String orderdate) {
		this.orderdate = orderdate;
	}

	public String getOrdertype() {
		return ordertype;
	}

	public void setOrdertype(String ordertype) {
		this.ordertype = ordertype;
	}

	public String getUserid() {
		return userid;
	}

	public void setUserid(String userid) {
		this.userid = userid;
	}

	public String getStationid() {
		return stationid;
	}

	public void setStationid(String stationid) {
		this.stationid = stationid;
	}

	public String getWxplatform() {
		return wxplatform;
	}

	public void setWxplatform(String wxplatform) {
		this.wxplatform = wxplatform;
	}

	public String getWxsku() {
		return wxsku;
	}

	public void setWxsku(String wxsku) {
		this.wxsku = wxsku;
	}

	public String getAmout() {
		return amout;
	}

	public void setAmout(String amout) {
		this.amout = amout;
	}

	public String getPoints() {
		return points;
	}

	public void setPoints(String points) {
		this.points = points;
	}

	public String getPointsamount() {
		return pointsamount;
	}

	public void setPointsamount(String pointsamount) {
		this.pointsamount = pointsamount;
	}

	public String getVoucherValue() {
		return voucherValue;
	}

	public void setVoucherValue(String voucherValue) {
		this.voucherValue = voucherValue;
	}

	public String getPayamount() {
		return payamount;
	}

	public void setPayamount(String payamount) {
		this.payamount = payamount;
	}

	public String getPayment() {
		return payment;
	}

	public void setPayment(String payment) {
		this.payment = payment;
	}



}
