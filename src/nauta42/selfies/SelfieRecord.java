package nauta42.selfies;

import android.graphics.Bitmap;

public class SelfieRecord {

	// URL for retrieving the ORIGINAL selfie image
	private String mSelfieUrl;

	// path to flag image in external memory
	private String mSelfieBitmapPath;
	
	private String mDate;
	private String mTime;
	private Bitmap mSelfieBitmap;  //bitmap


	public SelfieRecord(String selfieUrl, String selfieBitmapPath,
			String date, String time) {
		mSelfieUrl = selfieUrl;
		mSelfieBitmapPath = selfieBitmapPath;
		mDate = date;
		mTime = time;
	}

	public SelfieRecord(String selfieUrl, String date, String time) {
		mSelfieUrl = selfieUrl;
		mDate = date;
		mTime = time;
	}

	public SelfieRecord() {	
	}

	public String getSelfieUrl() {
		return mSelfieUrl;
	}
	
	public void setSelfieUrl(String selfieUrl) {
		this.mSelfieUrl = selfieUrl;
	}

	public String getDate() {
		return mDate;
	}

	public void setDate(String date) {
		this.mDate = date;
	}

	public String getTime() {
		return mTime;
	}
	
	public void setTime(String time) {
		this.mTime = time;
	}

	public Bitmap getSelfieBitmap() {
		return mSelfieBitmap;
	}
	
	public void setSelfieBitmap(Bitmap selfieBitmap) {
		this.mSelfieBitmap = selfieBitmap;
	}

	@Override
	public String toString() {
		return "Date: " + mDate + " Time: " + mTime +  " SelfiePath: " + mSelfieUrl;

	}

	public String getSelfieBitmapPath() {
		return mSelfieBitmapPath;
	}
	
	public void setSelfieBitmapPath(String selfiePath) {
		this.mSelfieBitmapPath = selfiePath;
	}


}