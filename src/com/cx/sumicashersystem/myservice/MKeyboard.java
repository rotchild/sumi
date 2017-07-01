package com.cx.sumicashersystem.myservice;

import java.lang.reflect.Method;

import android.app.Activity;
import android.graphics.Paint;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.inputmethodservice.KeyboardView.OnKeyboardActionListener;
import android.text.Editable;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.View.OnTouchListener;
import android.view.inputmethod.InputMethodManager;
import android.view.WindowManager;
import android.widget.EditText;

public class MKeyboard {
Paint paint;
private KeyboardView keyboardView;
private Activity mHostActivity;
private OnKeyboardActionListener mOnKeyboardActionListener=new OnKeyboardActionListener(){
	  public final static int CodeDelete = -5; // Keyboard.KEYCODE_DELETE
      public final static int CodeCancel = -3; // Keyboard.KEYCODE_CANCEL
      public final static int Codedot=46;
      public final static int Code0=48;
      public final static int Code1=49;
      public final static int Code2=50;
      public final static int Code3=51;
      public final static int Code4=52;
      public final static int Code5=53;
      public final static int Code6=54;
      public final static int Code7=55;
      public final static int Code8=56;
      public final static int Code9=57;
      

	@Override
	public void onPress(int primaryCode) {
		// TODO 自动生成的方法存根
		switch(primaryCode){
		case Code0:
			
			break;
		default:break;
		}

	}
	
	  public void hideCustomKeyboard()
	    {
		  keyboardView.setVisibility(View.GONE);
		  keyboardView.setEnabled(false);
	    }

	@Override
	public void onRelease(int primaryCode) {
		// TODO 自动生成的方法存根
		
	}
	
	

	@Override
	public void onKey(int primaryCode, int[] keyCodes) {
		// TODO 自动生成的方法存根
		View focusCurrent=mHostActivity.getWindow().getCurrentFocus();
		if(focusCurrent==null||focusCurrent.getClass()!=EditText.class)
			return;
		EditText editText=(EditText)focusCurrent;
		Editable editable=editText.getText();
		int start=editText.getSelectionStart();
		if(primaryCode==CodeCancel){
			hideCustomKeyboard();
		}else if(primaryCode==CodeDelete){
			if(editable!=null&&start>0){
				editable.delete(start-1, start);
			}
		}else{
			editable.insert(start, Character.toString((char)primaryCode));
		}
	}

	@Override
	public void onText(CharSequence text) {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void swipeLeft() {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void swipeRight() {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void swipeDown() {
		// TODO 自动生成的方法存根
		
	}

	@Override
	public void swipeUp() {
		// TODO 自动生成的方法存根
		
	}
	
};

public MKeyboard(Activity hostActivity,int viewid,int layoutid){
	mHostActivity=hostActivity;
	keyboardView=(KeyboardView)hostActivity.findViewById(viewid);
	Keyboard keyBoard=new Keyboard(mHostActivity,layoutid);
	keyboardView.setKeyboard(keyBoard);
	keyboardView.setPreviewEnabled(false);
	keyboardView.setOnKeyboardActionListener(mOnKeyboardActionListener);
	mHostActivity.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
}

public void registerEditText(int resid){
	EditText editText=(EditText)mHostActivity.findViewById(resid);
	//paint=editText.getPaint();
	editText.setOnFocusChangeListener(new OnFocusChangeListener(){

		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			// TODO 自动生成的方法存根
			Log.d("MKeyboard", "onFocusChange");
			if(hasFocus){
				 showMKeyboard(v);
			}else{
				 showMKeyboard(v);
			}
		}
		
	});
	
	editText.setOnClickListener(new OnClickListener(){

		@Override
		public void onClick(View v) {
			// TODO 自动生成的方法存根
		showMKeyboard(v);	
		}});
	
	editText.setOnTouchListener(new OnTouchListener(){

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			// TODO 自动生成的方法存根
			EditText editText=(EditText)v;
			int inType=editText.getInputType();
			//editText.setInputType(InputType.TYPE_NULL);
			if (android.os.Build.VERSION.SDK_INT <= 10) {//4.0以下 danielinbiti  
	               editText.setInputType(InputType.TYPE_NULL);  
	            } else {  
	               mHostActivity.getWindow().setSoftInputMode(  
	                        WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);  
	                try {  
	                    Class<EditText> cls = EditText.class;  
	                    Method setShowSoftInputOnFocus;  
	                    setShowSoftInputOnFocus = cls.getMethod("setShowSoftInputOnFocus",  
	                            boolean.class);  
	                    setShowSoftInputOnFocus.setAccessible(true);  
	                    setShowSoftInputOnFocus.invoke(editText, false);  
	                } catch (Exception e) {  
	                    e.printStackTrace();  
	                }   
	            }  
			return false;
		}
		
	});
	
}

public void showMKeyboard(View v){
	keyboardView.setVisibility(View.VISIBLE);
	keyboardView.setEnabled(true);
	if(v!=null){
		((InputMethodManager)mHostActivity.getSystemService(Activity.INPUT_METHOD_SERVICE)).hideSoftInputFromWindow(v.getWindowToken(), 0);
	}
}

public void hideMKeyboard(){
	keyboardView.setVisibility(View.GONE);
	keyboardView.setEnabled(false);
	
}


}
