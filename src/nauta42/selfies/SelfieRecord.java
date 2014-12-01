package nauta42.selfies;

import android.graphics.Bitmap;

public class SelfieRecord {

	// URL for retrieving the ORIGINAL selfie image
	private String mSelfieFilePath;

	// path to flag image in external memory
	private String mSelfieBitmapPath;
	
	private String mDate;
	private String mTime;
	private Bitmap mSelfieBitmap;  //bitmap


	public SelfieRecord(String selfieFilePath, String selfieBitmapPath,
			String date, String time) {
		mSelfieFilePath = selfieFilePath;
		mSelfieBitmapPath = selfieBitmapPath;
		mDate = date;
		mTime = time;
	}

	public SelfieRecord(String selfieUrl, String date, String time) {
		mSelfieFilePath = selfieUrl;
		mDate = date;
		mTime = time;
	}

	public SelfieRecord() {	
	}

	public String getSelfieFilePath() {
		return mSelfieFilePath;
	}
	
	public void setSelfieFilePath(String selfieFilePath) {
		this.mSelfieFilePath = selfieFilePath;
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
		return "Date: " + mDate + " Time: " + mTime +  " SelfiePath: " + mSelfieFilePath;

	}

	public String getSelfieBitmapPath() {
		return mSelfieBitmapPath;
	}
	
	public void setSelfieBitmapPath(String selfiePath) {
		this.mSelfieBitmapPath = selfiePath;
	}


}