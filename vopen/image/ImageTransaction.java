package vopen.image;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import vopen.protocol.VopenServiceCode;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.os.StatFs;
import android.text.TextUtils;

import com.netease.vopen.pal.Constants;
import common.framework.http.HttpRequest;
import common.framework.task.AsyncTransaction;
import common.framework.task.TransactionEngine;
import common.pal.PalLog;
import common.pal.PalPlatform;
import common.util.NameValuePair;

public class ImageTransaction extends AsyncTransaction {
	int mWidth;
	int mHeight;
	String mUrl;
	public static final int Error_Catch = 5*1024*1024;  //当sd卡剩余空间小于该值时，将不往缓存中保存数据
	
	protected ImageTransaction(TransactionEngine transMgr, String url, int width, int height) {
		super(transMgr, 0);
		mWidth = width;
		mHeight = height;
		mUrl = url;
	}

	@Override
	public void onResponseError(int errCode, Object err) {
		notifyError(errCode, mType, getId(), err);
	}

	@Override
	public void onTransactException(Exception e) {
		onResponseError(VopenServiceCode.TRANSACTION_FAIL, null);
	}
	
	@Override
	public void onResponseSuccess(String response, NameValuePair[] pairs) {
		throw new IllegalAccessError("Download image must responsed as stream");
	}
	
	@Override
	public void onResponseSuccess(InputStream in, NameValuePair[] pairs) {
		boolean b = false;
		if(in != null){
			b = saveFile(in, pairs);
		}
		
		if(b){
			String path = ImageHelper.getLocalImagePath(mUrl, mWidth, mHeight);
	       	notifyMessage(0, mType, getId(), path);
		} else{
			
            try {
	            Bitmap bitmap = BitmapFactory.decodeStream(in); 
	            PalLog.i("ImageTransaction","onResponseSuccess notcached,bitmap is" + bitmap);
	            if(bitmap != null) {
	            	notifyMessage(0, mType, getId(), bitmap);	
	            } else {
	            	notifyError(VopenServiceCode.TRANSACTION_FAIL, mType, getId(), null);
	            }
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				notifyError(VopenServiceCode.TRANSACTION_FAIL, mType, getId(), null);
			} 	
		}
	}
	@Override
	public void onTransact() {
//		PalLog.i("ImageTransaction", "[url]" + mUrl + "|" + mWidth + "x" + mHeight);
		if (!isCancel()) {
			String path = ImageHelper.getLocalImagePath(mUrl, mWidth, mHeight);
	        File file = new File(path);
			if(file.exists()){
				notifyMessage(0, mType, getId(), path);
				getTransactionEngine().endTransaction(this);
			}
			else{
				HttpRequest request = createImageRequest();
				sendRequest(request);
			}
			
		} else {
			getTransactionEngine().endTransaction(this);
		}
	}

    
	private HttpRequest createImageRequest(){
		StringBuilder url = new StringBuilder(Constants.IMG_URL_PREFEX);
		url.append(mUrl);
		if(mWidth > 0)
			url.append("&w=" + mWidth);
		if(mHeight > 0)
			url.append("&h=" + mHeight);
		url.append("&limit=0");
		url.append("&fill=0");
		url.append("&gif=0");
          
        HttpRequest request = new HttpRequest(url.toString());
		request.setStreamCallBack(true);
		
		return request;
	}
	
	private boolean saveFile(InputStream in, NameValuePair[] pairs) {
		boolean bDecompress = true;
		int len = 0;
		if (pairs != null) {
			try {
				for (int i = 0; i < pairs.length; i++) {
					String name = pairs[i].getName();
					String value = pairs[i].getValue();

					if (name != null && value != null){
						if(name.equalsIgnoreCase("Content-Encoding")
							&& value.equalsIgnoreCase("gzip")) {
							 PalLog.i("ImageTransaction","saveFile is gzip" );
							in = PalPlatform.gzipDecompress(in);
						}
						
//						if(name.equalsIgnoreCase("content-length")){
//							try{
//								len = Integer.parseInt(value);
//							}catch (NumberFormatException e) {
//								e.printStackTrace();
//							}
//							PalLog.i("ImageTransaction", "content length:" + len);
//						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				bDecompress = false;
			}
		}

		if (bDecompress) {
			return savetoFile(in, len);
		}

		return false;
	}
	
	private boolean savetoFile(InputStream in, int len){
		if(!enoughSpaceBySize(Environment.getExternalStorageDirectory().toString(), Error_Catch)){
			return false;
		}		
		FileOutputStream fos;
		String path = ImageHelper.getLocalImagePath(mUrl, mWidth, mHeight);
		File file = new File(path);
        if(file != null){
			File parent = file.getParentFile();
	        if (!parent.exists()) {
	            parent.mkdirs();
	        }
	        
			try {
				byte[] buffer = new byte[1024];
				fos = new FileOutputStream(file);
	
				while (true) {
					int size = in.read(buffer);				
					if (size < 0) {
						break;
					}
					if(size > 0)
						fos.write(buffer, 0, size);
				}
				fos.close();
				
				return true;			
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
        if(file != null){
			file.delete();
		}
		return false;
	}
	/**
     * 判断当前sd卡的大小是否满足要求的size
     * @param path
     * @param size
     * @return
     */
    static public boolean enoughSpaceBySize(String path,long size)
	{
		if(TextUtils.isEmpty(path))
			return true;
		
		StatFs statFS = new StatFs(path);
		if(!path.endsWith(File.separator) )
			path +=File.separator;

		return (long)statFS.getAvailableBlocks()*statFS.getBlockSize() > size;
	}
}
