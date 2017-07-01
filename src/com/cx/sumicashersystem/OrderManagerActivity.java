package com.cx.sumicashersystem;

import java.util.ArrayList;
import java.util.List;






import com.cx.sumicashersystem.R;
import com.cx.sumicashersystem.object.MonthRecord;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class OrderManagerActivity extends FragmentActivity {
	/*int start_0=0;//查询起点
	int start_1=0;//查询起点
	int start_2=0;//查询起点
	int limit_0=5;//查询起点
	int limit_1=5;//查询起点
	int limit_2=5;//查询起点
*/	private ImageView mTabLine;
	private int mScreen1_3;
	private ViewPager mViewPager;
	private List<Fragment> mDatas;
	private FragmentPagerAdapter mAdapter;
	private int mCurrentPageIndex;
	private TextView backtv;
	private List<MonthRecord> monthRecordList=new ArrayList<MonthRecord>();
	AllOrderTabFragment allOrderTab;
	FuelOrderTabFragment fuelOrderTab;
	CashOrderTabFragment cashOrderTab;
	
	LinearLayout all_orderLinear,fuel_orderLinear,shop_orderLinear;
	
	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.ordermanager_layout);
		initTabLine();
		initView();
	}
private void initTabLine(){
	mTabLine=(ImageView)findViewById(R.id.tableLine);
	Display display=getWindow().getWindowManager().getDefaultDisplay();
	DisplayMetrics outMetrics=new DisplayMetrics();
	display.getMetrics(outMetrics);
	mScreen1_3=outMetrics.widthPixels/3;
	LayoutParams lp=mTabLine.getLayoutParams();
	lp.width=mScreen1_3;
	mTabLine.setLayoutParams(lp);
}

private void initView(){
	all_orderLinear=(LinearLayout)findViewById(R.id.all_order);
	fuel_orderLinear=(LinearLayout)findViewById(R.id.fuel_order);
	shop_orderLinear=(LinearLayout)findViewById(R.id.shop_order);
	
	mViewPager=(ViewPager)findViewById(R.id.id_viewpager);
	mDatas=new ArrayList<Fragment>();
	allOrderTab=new AllOrderTabFragment();
	fuelOrderTab=new FuelOrderTabFragment();
	cashOrderTab=new CashOrderTabFragment();
	
	mDatas.add(allOrderTab);
	mDatas.add(fuelOrderTab);
	mDatas.add(cashOrderTab);
	
	mAdapter=new FragmentPagerAdapter(getSupportFragmentManager()){

		@Override
		public Fragment getItem(int arg0) {
			return mDatas.get(arg0);
		}

		@Override
		public int getCount() {
			return mDatas.size();
		}
		
	};
	mViewPager.setAdapter(mAdapter);
	
	all_orderLinear.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			mViewPager.setCurrentItem(0);
			LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabLine.getLayoutParams();
			lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(-1)*mScreen1_3);
			mTabLine.setLayoutParams(lp);
		}
		
	});
	
	fuel_orderLinear.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			mViewPager.setCurrentItem(1);
			LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabLine.getLayoutParams();
			lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(0)*mScreen1_3);
			mTabLine.setLayoutParams(lp);
		}
		
	});
	
	shop_orderLinear.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
			mViewPager.setCurrentItem(2);
			LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabLine.getLayoutParams();
			lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(1)*mScreen1_3);
			mTabLine.setLayoutParams(lp);
		}
		
	});
	
	
	mViewPager.setOnPageChangeListener(new OnPageChangeListener(){

		@Override
		public void onPageScrollStateChanged(int arg0) {
			// TODO 自动生成的方法存根
			
		}

		@Override
		public void onPageScrolled(int position, float positionOffset, int positionOffsetPx) {
			// TODO 自动生成的方法存根
			//Log.e("TAG", position + " , " + positionOffset + " , "
			//		+ positionOffsetPx);
			LinearLayout.LayoutParams lp=(android.widget.LinearLayout.LayoutParams)mTabLine.getLayoutParams();
			if(mCurrentPageIndex==0&&position==0)//0->1
			{
				lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+positionOffset*mScreen1_3);
				
			}else if(mCurrentPageIndex==1&&position==0)//1->0
			{
				lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(positionOffset-1)*mScreen1_3);
			}else if(mCurrentPageIndex==1&&position==1)//1->2
			{
				lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+positionOffset*mScreen1_3);
			}else if(mCurrentPageIndex==2&&position==1)//2->1
			{
				lp.leftMargin=(int)(mCurrentPageIndex*mScreen1_3+(positionOffset-1)*mScreen1_3);
			}
			mTabLine.setLayoutParams(lp);
			
			
		}

		@Override
		public void onPageSelected(int position) {
			// TODO 自动生成的方法存根
			mCurrentPageIndex=position;
			if(position==1){
				fuelOrderTab.showList();
			}
			
			
	
		}
		
	});
}
}
