package com.cx.sumicashersystem.myservice;

import java.io.ByteArrayOutputStream;
import java.util.Hashtable;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;

public class BytesUtil {
	/**
	 * 生成间断性黑块数�?
	 * @param w : 打印纸宽�?, 单位�?
	 * @return
	 */
	public static byte[] initBlackBlock(int w){
		int ww = (w + 7)/8 ;
		int n = (ww + 11)/12;
		int hh = n * 24;
		byte[] data = new byte[ hh * ww + 10];
				
		data[0] = 0x0A;
		data[1] = 0x1D;
		data[2] = 0x76;
		data[3] = 0x30;
		data[4] = 0x00;
		
		data[5] = (byte)ww;//xL
		data[6] = (byte)(ww >> 8);//xH
		data[7] = (byte)hh;
		data[8] = (byte)(hh >> 8);
		
		int k = 9;
		for(int i=0; i < n; i++){
			for(int j=0; j<24; j++){
				for(int m =0; m<ww; m++){
					if(m/12 == i){
						data[k++] = (byte)0xFF;
					}else{
						data[k++] = 0;
					}
				}
			}
		}
		data[k++] = 0x0A;
		return data;
	}	
	/**
	 * 生成�?大块黑块数据
	 * @param h : 黑块高度, 单位�?
	 * @param w : 黑块宽度, 单位�?, 8的�?�数
	 * @return
	 */
	public static byte[] initBlackBlock(int h, int w){
		int hh = h;
		int ww = (w - 1)/8 + 1;
		byte[] data = new byte[ hh * ww + 10];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = (byte)hh;
		data[7] = (byte)(hh >> 8);
		
		int k = 8;
		for(int i=0; i<hh; i++){
			for(int j=0; j<ww; j++){
				data[k++] = (byte)0xFF;
			}
		}
		data[k++] = 0x00;data[k++] = 0x00;
		return data;
	}	
	/**
	 * 生成灰块数据
	 * @param h : 灰块高度, 单位�?
	 * @param w : 灰块宽度, 单位�?, 8的�?�数
	 * @return
	 */
	public static byte[] initGrayBlock(int h, int w){
		int hh = h;
		int ww = (w - 1)/8 + 1;
		byte[] data = new byte[ hh * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = (byte)hh;
		data[7] = (byte)(hh >> 8);
		
		int k = 8;
		byte m =(byte)0xAA;
		for(int i=0; i<hh; i++){
			m = (byte)~m;
			for(int j=0; j<ww; j++){
				data[k++] = m;
			}
		}
		//ata[k++] = 0x00;data[k++] = 0x00;
		return data;
	}
	
	/**
	 * 生成表格数据
	 * @param h : 每列格数, 每格32个点�?
	 * @param w : 每行格数, 每格32个点�?
	 * @return
	 */
	public static byte[] initTable(int h, int w){
		int hh = h * 32;
		int ww = w * 4;
		
		byte[] data = new byte[ hh * ww + 10];
		
		
		data[0] = 0x0A;
		data[1] = 0x1D;
		data[2] = 0x76;
		data[3] = 0x30;
		data[4] = 0x00;
		
		data[5] = (byte)ww;//xL
		data[6] = (byte)(ww >> 8);//xH
		data[7] = (byte)hh;
		data[8] = (byte)(hh >> 8);
		
		int k = 9;
		int m = 31;
		for(int i=0; i<h; i++){
			for(int j=0; j<w; j++){
				data[k++] = (byte)0xFF;
				data[k++] = (byte)0xFF;
				data[k++] = (byte)0xFF;
				data[k++] = (byte)0xFF;
			}
            if(i == h-1) m =30;
			for(int t=0; t< m; t++){
				for(int j=0; j<w-1; j++){
					data[k++] = (byte)0x80;
					data[k++] = (byte)0;
					data[k++] = (byte)0;
					data[k++] = (byte)0;	
				}
				data[k++] = (byte)0x80;
				data[k++] = (byte)0;
				data[k++] = (byte)0;
				data[k++] = (byte)0x01;				
			}
		}
		for(int j=0; j<w; j++){
			data[k++] = (byte)0xFF;
			data[k++] = (byte)0xFF;
			data[k++] = (byte)0xFF;
			data[k++] = (byte)0xFF;
		}		
		data[k++] = 0x0A;
		return data;
	}
	
    public static byte[] getGbk(String paramString)
    {
		byte[] arrayOfByte = null;
		try 
		{
			arrayOfByte = paramString.getBytes("GBK");  //必须放在try内才可以
		}
		catch (Exception   ex) {
				ex.printStackTrace();
		}
		return arrayOfByte;
    }
    
	public static byte[] setWH(int mode) {
		byte[] returnText = new byte[3]; // GS ! 11H 倍宽倍高
		returnText[0] = 0x1D;
		returnText[1] = 0x21;

		switch (mode) // 1-无；2-倍宽�?3-倍高�? 4-倍宽倍高
		{
		case 2:
			returnText[2] = 0x10;
			break;
		case 3:
			returnText[2] = 0x01;
			break;
		case 4:
			returnText[2] = 0x11;
			break;
		default:
			returnText[2] = 0x00;
			break;
		}

		return returnText;
	}
    //设置放大倍数 1�?8 �? (0-7)
    public static byte[] setZoom(int level){
    	byte[] rv = new byte[3];
    	rv[0] = 0x1D;
    	rv[1] = 0x21;
    	rv[2] = (byte)((level & 0x07)<<4 | (level & 0x07));   	
    	return rv;
    }
	public static byte[] setAlignCenter(int align) {
		byte[] returnText = new byte[5]; // 对齐 ESC a
		returnText[0] = 0x20;
		returnText[1] = 0x0A;
		returnText[2] = 0x1B;
		returnText[3] = 0x61;

		switch (align) // 0-左对齐；1-居中对齐�?2-右对�?
		{
		case 1:
			returnText[4] = 0x01;
			break;
		case 2:
			returnText[4] = 0x02;
			break;
		default:
			returnText[4] = 0x00;
			break;
		}
		return returnText;
	}

	public static byte[] setBold(boolean dist) {
		byte[] returnText = new byte[3]; // 加粗 ESC E
		returnText[0] = 0x1B;
		returnText[1] = 0x45;

		if (dist) {
			returnText[2] = 0x01; // 表示加粗
		} else {
			returnText[2] = 0x00;
		}
		return returnText;
	}

	/***************************************************************************
	 * add by yidie 2012-01-10 功能：设置打印绝对位�? 参数�? int 在当前行，定位光标位置，取�?�范�?0�?576�? 说明�?
	 * 在字体常规大小下，每汉字24点，英文字符12�? 如位于第n个汉字后，则position=24*n
	 * 如位于第n个半角字符后，则position=12*n
	 ****************************************************************************/

	public static byte[] setCusorPosition(int position) {
		byte[] returnText = new byte[4]; // 当前行，设置绝对打印位置 ESC $ bL bH
		returnText[0] = 0x1B;
		returnText[1] = 0x24;
		returnText[2] = (byte)position;
		returnText[3] = (byte)(position >> 8);
		return returnText;
	}
	
	public static byte[] PrintBarcode(String stBarcode) {
		int iLength = stBarcode.length() + 4;
		byte[] returnText = new byte[iLength];

		returnText[0] = 0x1D;
		returnText[1] = 'k';
		returnText[2] = 0x45;
		returnText[3] = (byte) stBarcode.length(); // 条码长度�?

		System.arraycopy(stBarcode.getBytes(), 0, returnText, 4,
				stBarcode.getBytes().length);

		return returnText;
	}

	public static byte[] CutPaper() {
		byte[] returnText = {0x20,0x0A, 0x1D, 0x56, 0x42, 0x00 }; // 切纸�? GS V
																	// 66D 0D
		return returnText;
	}
	
	public static byte[] selfCheck(){
		byte[] returnText = {0x1F, 0x1B, 0x1F, 0x53};
		return returnText;
	}
	
	public static byte[] getPrinterStatus(){
		byte[] data = {0x0A,0x10,0x04,0x01};
		return data;
	}
	
	
    public  static String getHexStringFromBytes(byte[] data){
    	if(data == null || data.length <= 0){
    		return null;
    	}    	
		String hexString = "0123456789ABCDEF";
		int size = data.length * 2;
		StringBuilder sb = new StringBuilder(size);
		for (int i = 0; i < data.length; i++) {
			sb.append(hexString.charAt((data[i] & 0xF0) >> 4));
			sb.append(hexString.charAt((data[i] & 0x0F) >> 0));
		}
		return sb.toString();    	
    }	

	//�?个二维码
	/**
	* 打印二维�?
	* @param code:			二维码数�?
	* @param modulesize:	二维码块大小(单位:�?, 取�?? 1 �? 16 )
	* @param errorlevel:	二维码纠错等�?(0 �? 3)
	*                0 -- 纠错级别L ( 7%)
	*                1 -- 纠错级别M (15%) 
	*                2 -- 纠错级别Q (25%) 
	*                3 -- 纠错级别H (30%) 
	*/
   
	public static byte[] getPrintQRCode(String code, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code));
			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();				
	}
	
	//两个二维�?
	/**
	* 打印二维�?
	* @param code1:			二维码数�?
	* @param code2:			二维码数�?
	* @param modulesize:	二维码块大小(单位:�?, 取�?? 1 �? 16 )
	* @param errorlevel:	二维码纠错等�?(0 �? 3)
	*                0 -- 纠错级别L ( 7%)
	*                1 -- 纠错级别M (15%) 
	*                2 -- 纠错级别Q (25%) 
	*                3 -- 纠错级别H (30%) 
	*/
	
	public static byte[] getPrintDoubleQRCode(String code1, String code2, int modulesize, int errorlevel){
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(setQRCodeSize(modulesize));
			buffer.write(setQRCodeErrorLevel(errorlevel));
			buffer.write(getQCodeBytes(code1));
			buffer.write(getBytesForPrintQRCode(false));
			buffer.write(getQCodeBytes(code2));
			
			//加入横向间隔
			buffer.write(new byte[]{0x1B, 0x5C, 0x30, 0x00});
			
			buffer.write(getBytesForPrintQRCode(true));
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();				
	}

	//�?维码
	public static byte[] getPrintBarCode(String data, int symbology, 
			int height,	int width, int textposition){

		if(symbology < 0 || symbology > 8){
			return new byte[]{0x0A};
		}
		if(width < 2 || width > 6){
			width = 2;
		}
		if(textposition <0 || textposition > 3){
			textposition = 0;
		}
		if(height < 1 || height>255){
			height = 162;
		}
		
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			buffer.write(new byte[]{0x1D,0x66,0x01,0x1D,0x48,(byte)textposition,
					0x1D,0x77,(byte)width,0x1D,0x68,(byte)height,0x0A});
			
			byte[] barcode = data.getBytes();
			
			if(symbology == 8){
				buffer.write(new byte[]{0x1D,0x6B,0x49,(byte)(barcode.length+2),0x7B,0x42});
			}else{
				buffer.write(new byte[]{0x1D,0x6B,(byte)(symbology + 0x41),(byte)barcode.length});
			}
			
			buffer.write(barcode);
			
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();				
	}	
	
////////////////////////////////////////////////////////////////////////////////////
//////////////////////////          private                /////////////////////////	
////////////////////////////////////////////////////////////////////////////////////
	

	private static byte[] setQRCodeSize(int modulesize){
		//二维码块大小设置指令
		byte[] dtmp = new byte[8];
		dtmp[0] = 0x1D;				
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x43;
		dtmp[7] = (byte)modulesize;		
		return dtmp;
	}
	private static byte[] setQRCodeErrorLevel(int errorlevel){
		//二维码纠错等级设置指�?
		byte[] dtmp = new byte[8];
		dtmp[0] = 0x1D;				
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x45;
		dtmp[7] = (byte)(48+errorlevel);
		return dtmp;		
	}
	private static byte[] getBytesForPrintQRCode(boolean single){
		//打印已存入数据的二维�?
		byte[] dtmp;
		if(single){		//同一行只打印�?个QRCode�? 后面加换�?
			dtmp = new byte[9];
			dtmp[8] = 0x0A;
		}else{
			dtmp = new byte[8];
		}
		dtmp[0] = 0x1D;				
		dtmp[1] = 0x28;
		dtmp[2] = 0x6B;
		dtmp[3] = 0x03;
		dtmp[4] = 0x00;
		dtmp[5] = 0x31;
		dtmp[6] = 0x51;
		dtmp[7] = 0x30;					
		return dtmp;
	}
	private static byte[] getQCodeBytes(String code){
		//二维码存入指�?
		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		try{
			byte[] d = getGbk(code);
			int len = d.length + 3;
			if(len > 7092)len = 7092;
			buffer.write((byte)0x1D);
			buffer.write((byte)0x28);
			buffer.write((byte)0x6B);
			buffer.write((byte)len);
			buffer.write((byte)(len >> 8));
			buffer.write((byte)0x31);
			buffer.write((byte)0x50);
			buffer.write((byte)0x30);
			for(int i=0; i<d.length && i<len; i++){
				buffer.write(d[i]);
			}
		}catch(Exception e){
			e.printStackTrace();
		}
		return buffer.toByteArray();		
	}    
	/**
	 * 取表格图�?
	 * @param rows: 表格的行�?
	 * @param cols: 表格的列�?
	 * @param size: 每个正方格的边长, 单位为点
	 * @return
	 */	
	public static Bitmap getTableBitmapFromData(int rows, int cols, int size){
		int[] pixels = createTableData(rows, cols, size);
		return getBitmapFromData(pixels, cols * size, rows * size);
	}

	protected static int[] createTableData(int rows, int cols, int size){
		int width = cols * size;
		int height = rows * size;
		int[] pixels = new int[width * height];
		int k = 0;
		for(int j=0; j<height; j++){
			for(int i=0; i<width; i++){
				if(i==width-1 || j==height-1 || (i % size)==0 || (j % size) == 0){
					pixels[k++] = 0xff000000;
				}else{
					pixels[k++] = 0xffffffff;
				}
			}
		}
		return pixels;
	}	
	protected static Bitmap getBitmapFromData(int[] pixels, int width, int height){
		Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height);
		return bitmap;		
	}
	
    /**
     * 单字符转字节
     * @param c
     * @return
     */
    private static byte charToByte(char c) {  
        return (byte) "0123456789ABCDEF".indexOf(c);  
    } 
    
    /**
     * 16进制字符串转字节数组
     * @param hexstring
     * @return
     */
    @SuppressLint("DefaultLocale") 
    public static byte[] getBytesFromHexString(String hexstring){
    	if(hexstring == null || hexstring.equals("")){
    		return null;
    	}
    	hexstring = hexstring.replace(" ", "");
    	hexstring = hexstring.toUpperCase();
    	int size = hexstring.length()/2;
    	char[] hexarray = hexstring.toCharArray();
    	byte[] rv = new byte[size];
    	for(int i=0; i<size; i++){
            int pos = i * 2;  
            rv[i] = (byte) (charToByte(hexarray[pos]) << 4 | charToByte(hexarray[pos + 1]));     		
    	}
    	return rv;
    } 
    
	protected static int[] createLineData(int size, int width){
		int[] pixels = new int[width * (size + 6)];
		int k = 0;
		for(int j=0; j<3; j++){
			for(int i=0; i<width; i++){
				pixels[k++] = 0xffffffff;
			}
		}
		
		for(int j = 0; j < size; j++){
			for(int i=0; i<width; i++){
				pixels[k++] = 0xff000000;
			}
		}
		
		for(int j=0; j<3; j++){
			for(int i=0; i<width; i++){
				pixels[k++] = 0xffffffff;
			}
		}
		return pixels;
	} 
	
	public static byte[] initLine1(int w, int type){
		byte[][] kk = new byte[][]{
				{0x00,0x00,0x7c,0x7c,0x7c,0x00,0x00},
				{0x00,0x00,(byte) 0xff,(byte) 0xff,(byte) 0xff,0x00,0x00},
				{0x00,0x44,0x44,(byte) 0xff,0x44,0x44,0x00},
				{0x00,0x22,0x55,(byte) 0x88,0x55,0x22,0x00},
				{0x08,0x08,0x1c,0x7f,0x1c,0x08,0x08},
				{0x08,0x14,0x22,0x41,0x22,0x14,0x08},
				{0x08,0x14,0x2a,0x55,0x2a,0x14,0x08},
				{0x08,0x1c,0x3e,0x7f,0x3e,0x1c,0x08},
				{0x49,0x22,0x14,0x49,0x14,0x22,0x49},
				{0x63,0x77,0x3e,0x1c,0x3e,0x77,0x63},
				{0x70,0x20,(byte) 0xaf,(byte) 0xaa,(byte) 0xfa,0x02,0x07},
				{(byte) 0xef,0x28,(byte) 0xee,(byte) 0xaa,(byte) 0xee,(byte) 0x82,(byte) 0xfe},
				};
		
		int ww = (w + 7)/8;

		byte[] data = new byte[ 13 * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = 13;  //高度13
		data[7] = 0;
		
		int k = 8;
		for(int i=0; i < 3 * ww; i++){
				data[k++] = 0;
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][0];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][1];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][2];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][3];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][4];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][5];
		}
		for(int i=0; i < ww; i++){
			data[k++] = kk[type][6];
		}		
		for(int i=0; i < 3 * ww; i++){
			data[k++] = 0;
		}
		return data;
	}
	public static byte[] initLine2(int w){
		int ww = (w + 7)/8;

		byte[] data = new byte[ 12 * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = 12;  //高度13
		data[7] = 0;
		
		int k = 8;
		for(int i=0; i < 5 * ww; i++){
				data[k++] = 0;
		}
		for(int i=0; i < ww; i++){
			data[k++] = 0x7f;
		}
		for(int i=0; i < ww; i++){
			data[k++] = 0x7f;
		}
		for(int i=0; i < 5 * ww; i++){
			data[k++] = 0;
		}
		return data;
	}	
	
	public static Bitmap getLineBitmapFromData(int size, int width){
		int[] pixels = createLineData(size, width);
		return getBitmapFromData(pixels, width, size + 6);
	}
	private static byte getBitMatrixColor(BitMatrix bits, int x, int y){
		int width = bits.getWidth();
		int height = bits.getHeight();
		if( x >= width || y >= height || x < 0 || y < 0)return 0;
		if(bits.get(x, y)){
			return 1;
		}else{
			return 0;
		}
	}
	
	public static byte[] getBytesFromBitMatrix(BitMatrix bits){
		if(bits == null)return null;
		
		int h = bits.getHeight();
		int w = (bits.getWidth()+7) / 8;
		byte[] rv = new byte[h * w + 8];
		
		rv[0] = 0x1D;
		rv[1] = 0x76;
		rv[2] = 0x30;
		rv[3] = 0x00;
		
		rv[4] = (byte)w;//xL
		rv[5] = (byte)(w >> 8);//xH
		rv[6] = (byte)h;
		rv[7] = (byte)(h >> 8);	
		
		int k = 8;
		for(int i=0; i<h; i++){
			for(int j=0; j<w; j++){
				for(int n=0; n<8; n++){
					byte b = getBitMatrixColor(bits, j * 8 + n, i);
					rv[k] += rv[k] + b;					
				}
				k++;
			}
		}		
		return rv;
	}
	
	public static byte[] getZXingQRCode(String data, int size){
        try {        
        	Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
        	hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
        	//图像数据转换，使用了矩阵转换
			BitMatrix bitMatrix = new QRCodeWriter().encode(data, BarcodeFormat.QR_CODE, size, size, hints);
			System.out.println("bitmatrix height:" + bitMatrix.getHeight() + " width:" + bitMatrix.getWidth());
			return getBytesFromBitMatrix(bitMatrix);
		} catch (WriterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
	
	public static byte[] getHorizontalLine(int w,  int size, int type){
		int ww = (w + 7)/8;
		int hh = size + 6;
		
		byte kk = (byte)0xff;	//实线
		
		if(type == 0){
			kk = 0x3f;			//虚线
		}
		
		byte[] data = new byte[ hh * ww + 8];
				
		data[0] = 0x1D;
		data[1] = 0x76;
		data[2] = 0x30;
		data[3] = 0x00;
		
		data[4] = (byte)ww;//xL
		data[5] = (byte)(ww >> 8);//xH
		data[6] = (byte)hh;  //高度
		data[7] = (byte)(hh >> 8);
		
		int k = 8;
		for(int i=0; i < 3 * ww; i++){
				data[k++] = 0;
		}
		for(int j=0; j < size; j++){
			for(int i=0; i < ww; i++){
				data[k++] = kk;
			}
		}
		for(int i=0; i < 3 * ww; i++){
			data[k++] = 0;
		}
		return data;
	}		
}
