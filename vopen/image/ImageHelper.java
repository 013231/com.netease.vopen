package vopen.image;

import java.io.File;

import vopen.tools.FileUtils;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.text.TextUtils;

import com.netease.vopen.app.VopenApp;
import com.netease.vopen.pal.Constants;

public class ImageHelper {
	 /**
     * <br/>从路径中取得文件名.
     * <br/>比如用于取得图片保存名称
     * @param url
     * @return
     */
    /*package*/ 
	static String getNameFromPath(String path) {
        if (!TextUtils.isEmpty(path)) {
            return path.substring(path.lastIndexOf("/") + 1);
        }
        return "";
    }
	/*package*/ 
	static String getLocalImageName(String fileName, int width, int height){
		String name = getNameFromPath(fileName);
		return width + "x" + height + "_" + name;
	}
	static String getLocalImagePath(String fileName, int width, int height) {
		/**
		 * 修改图片缓存的路径
		 */
		String fn = getLocalImageName(fileName, width, height);
		File f = new File(VopenApp.getAppInstance().getExternalCacheDir(), fn);
		return f.getAbsolutePath();
    }
	
	
	static Bitmap resizeImage(Bitmap src, int width, int height) {
        Bitmap bitmap = null;
        
        if (src == null || width == 0 || height == 0 
        		|| (width == src.getWidth() && height == src.getHeight())) {
            return src;
        }
        
        try {
            Matrix matrix = new Matrix();
            
            float sw = (float)width/src.getWidth();
            float sh = (float)height/src.getHeight();
            matrix.postScale(sw, sh);
            
            bitmap = Bitmap.createBitmap(src, 0, 0, src.getWidth(), src.getHeight(), matrix, true);
            
//            int x = 0;
//            int y = 0;
//            if(width < src.getWidth()){
//            	x = (src.getWidth() - width)/2;
//            }
//            if(height < src.getHeight()){
//            	y = (src.getHeight() - height)/2;
//            }
//            bitmap = Bitmap.createBitmap(src, x, y, width, height);
            
        } catch (Exception e) {
            if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        catch (OutOfMemoryError e){
        	if (bitmap != null) {
                bitmap.recycle();
                bitmap = null;
            }
        }
        finally {
        	if(bitmap == null)
        		bitmap = src;
        	
            if (src != null && src != bitmap) {
                src.recycle();
            }
        }
        
        return bitmap;
    }
	
	
	/**
	 * 解大图内存不足时尝试5此, samplesize增大
	 * @param file
	 * @param max 宽或高的最大值, <= 0 , 能解多大解多大, > 0, 最大max, 内存不足解更小
	 * @return
	 */
	/*package*/ 
	static Bitmap getBitmapFromFileLimitSize(String file, int max) {
		if (file == null)
			return null;
		Bitmap bm = null;
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inSampleSize = 1;

		if(max > 0){
			options.inJustDecodeBounds = true;
			// 获取这个图片的宽和高
			bm = BitmapFactory.decodeFile(file, options);
			options.inJustDecodeBounds = false;
	
			float blW = (float) options.outWidth / max;
			float blH = (float) options.outHeight / max;
	
			if (blW > 1 || blH > 1) {
				if(blW > blH )
					options.inSampleSize = (int) (blW + 0.9f);
				else
					options.inSampleSize = (int) (blH + 0.9f);
			}
		}
		
		int i = 0;
		while (i <= 5) {
			i++;
			try {
				bm = BitmapFactory.decodeFile(file, options);
				if(bm == null){
					//解不出来图片，认为图片文件损坏，删掉该文件
					FileUtils.delete(file);
				}
				break;
			} catch (OutOfMemoryError e) {
				options.inSampleSize++;
				e.printStackTrace();
			}
		}
		return bm;
	}
	/**
	 * dip转pixel
	 * @param context
	 * @param dipValue
	 * @return
	 */
	public static float dip2px(Context context, float dipValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (dipValue * scale);
	}

	/**
	 * pixel转dip
	 * @param context
	 * @param pxValue
	 * @return
	 */
	public static int px2dip(Context context, float pxValue) {
		final float scale = context.getResources().getDisplayMetrics().density;
		return (int) (pxValue / scale + 0.5f);
	}
	
	public static Bitmap getThumImage(Bitmap source, int width, int height) {
        if (source == null)
            return null;
        
        if (width <= 0 || height <= 0) {
            return source;
        }
        
        // TODO: 对于获取的图片大小期望大小，则进行拉伸裁剪 
//      if (source.getWidth() <= width && source.getHeight() <= height) {
//          return source;
//      }

        // create the matrix to scale it
        Matrix matrix = new Matrix();
        
        float tmp_w = ((float) source.getWidth()) / width;
        float tmp_h = ((float) source.getHeight()) / height;
        float tmp = tmp_w < tmp_h ? tmp_w : tmp_h;
        
        int clipWidth = (int) (width * tmp);
        int clipHeight = (int) (height * tmp);
        
        clipWidth = clipWidth > source.getWidth() ? source.getWidth() : clipWidth;
        clipHeight = clipHeight > source.getHeight() ? source.getHeight() : clipHeight;

        matrix.setScale(1 / tmp, 1 / tmp);

        int pading_x = (source.getWidth() - clipWidth) >> 1;
        int pading_y = source.getHeight() / 3 - clipHeight / 2;
        pading_y = pading_y < 0 ? 0 : pading_y;

        Bitmap thumb = null;
        try {
            long t = System.currentTimeMillis();
            
            thumb = Bitmap.createBitmap(source, pading_x, pading_y,
                    clipWidth, clipHeight, matrix, true);

//          Log.i("Image Scale", " getThumImage time " + (System.currentTimeMillis() - t));
//          Log.i("Image Scale", " getThumImage setScale " + tmp);
//          Log.i("Image Scale", " getThumImage w=" + width + " H=" + height);
//          Log.i("Image Scale", " getThumImage sw=" + source.getWidth() + " sH=" + source.getHeight());
//          Log.i("Image Scale", " getThumImage dw=" + thumb.getWidth() + " dH=" + thumb.getHeight());
            
        } catch (java.lang.OutOfMemoryError e) {
            e.printStackTrace();
            if (thumb != null) {
                thumb.recycle();
            }
            thumb = null;
            System.gc();
        }
        
        return thumb;
    }
}
