package nauta42.selfies;

import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.util.Log;

public class SelfieHelper {
	private static final String TAG = "DailySelfie_SelfieHelper"; 

	//decode a scaled image to reduce the amount of used memory
	public Bitmap getScaledBitmap(int width, int height, String filePath) {
	    // Get the dimensions of the View
		int targetW = width;
		int targetH = height;

	    // Get the dimensions of the bitmap
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(filePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;

	    // Determine how much to scale down the image
		int scaleFactor = 1;
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

	    // Decode the image file into a Bitmap sized to fill the View
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;
		Bitmap bitmap1 = BitmapFactory.decodeFile(filePath, bmOptions);
		
		//N42 gets the image rotation if the camera app returns the photo rotated
		int rotateDegrees = 0;  //future rotation for correction
		ExifInterface ei;
		int orientation;
		try {
			ei = new ExifInterface(filePath);
			orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Log.d(TAG, "Exif ORIENTATION: " + orientation);
			String logMsg = "Exif ORIENTATION";
			switch(orientation) {
		    case ExifInterface.ORIENTATION_NORMAL:
		    	rotateDegrees = 0; logMsg += "_NORMAL, rotate(degrees): ";
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_90:
		    	rotateDegrees = 90; logMsg += "_ROTATE_90, rotate(degrees): ";
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_180:
		    	rotateDegrees = 180; logMsg += "_ROTATE_180, rotate(degrees): ";
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_270:
		    	rotateDegrees = 270; logMsg += "_ROTATE_270, rotate(degrees): ";
		        break;
		    default:
		    	logMsg += "_UNDEFINED or OTHERS, rotate(degrees): ";
			}
			Log.d(TAG, logMsg + rotateDegrees);
		} catch (IOException e) {
			e.printStackTrace();
		};

		//N42 rotate, recycle and display bitmap
		bitmap1 = RotateBitmap(bitmap1, rotateDegrees);
		return bitmap1;
		//imageView.setImageBitmap(bitmap1);
		//imageView.setVisibility(View.VISIBLE);
		//
		//mBitmap1 = bitmap1.copy(bitmap1.getConfig(), false);
		//N42 thumbnail from bitmap already rotated
		//mImageView2.setMaxHeight(targetH/3);
		//mImageView2.setMaxWidth(targetW/3);
		//mImageView2.setImageBitmap(bitmap1);
		//mImageView2.setVisibility(View.VISIBLE);
	}

	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
	}
	

}
