package nauta42.selfies;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import nauta42.selfies.provider.SelfiesContract;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class SelfieListAdapter extends CursorAdapter {

	private ArrayList<SelfieRecord> list = new ArrayList<SelfieRecord>();
	private static LayoutInflater inflater = null;
	private Context mContext;

	private static final String APP_DIR = "ContentProvider/Selfies";
	private String mBitmapStoragePath;
	private static final String TAG = "DailySelfie_ADAPTER";

	public SelfieListAdapter(Context context, Cursor cursor, int flags) {
		super(context, cursor, flags);
		mContext = context;
		inflater = LayoutInflater.from(mContext);
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				String root = mContext.getExternalFilesDir(null)
						.getCanonicalPath();
				if (null != root) {
					File bitmapStorageDir = new File(root, APP_DIR);
					bitmapStorageDir.mkdirs();
					mBitmapStoragePath = bitmapStorageDir.getCanonicalPath();
					Log.d(TAG, "mBitmapStoragePath:" + mBitmapStoragePath);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public Cursor swapCursor(Cursor newCursor) {
		super.swapCursor(newCursor);
		return newCursor;
	}

	// Returns a new SelfieRecord for the data at the cursor's
	// current position
	@SuppressWarnings("unused")
	private SelfieRecord getSelfieRecordFromCursor(Cursor cursor) {
		String col1 = cursor.getString(cursor
				.getColumnIndex(SelfiesContract.SELFIE_BITMAP_PATH));
		String col2 = cursor.getString(cursor
				.getColumnIndex(SelfiesContract.DATE));
		String col3 = cursor.getString(cursor
				.getColumnIndex(SelfiesContract.TIME));
		return new SelfieRecord(col1, col2, col3);
	}

	public int getCount() {
		return list.size();
	}

	public Object getItem(int position) {
		return list.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	static class ViewHolder {
		ImageView selfie;
		TextView date;
		TextView time;
		String selfieFile;
	}

	public void add(SelfieRecord listItem) {

		String selfieFilePath = listItem.getSelfieFilePath();
		String lastPathSegment = Uri.parse(listItem.getSelfieFilePath())
				.getLastPathSegment();
		Log.d(TAG, "lastPathSegment: " + lastPathSegment);
		String bitmapFilePath = mBitmapStoragePath + "/" + lastPathSegment;
		Log.d(TAG, "filePath: " + bitmapFilePath);
		
		if (storeBitmapToFile(listItem.getSelfieBitmap(), bitmapFilePath)) {

			listItem.setSelfieBitmapPath(bitmapFilePath);
			list.add(listItem);

			ContentValues values = new ContentValues();

			// DONE - Insert new record into the ContentProvider
			values.put(SelfiesContract.SELFIE_FILE_PATH, selfieFilePath);
			values.put(SelfiesContract.SELFIE_BITMAP_PATH, bitmapFilePath);
			values.put(SelfiesContract.DATE, listItem.getDate());
			values.put(SelfiesContract.TIME, listItem.getTime());
			//
			mContext.getContentResolver().insert(SelfiesContract.CONTENT_URI, values);
        }
	}

	public ArrayList<SelfieRecord> getList() {
		return list;
	}

	public void removeAllViews() {
		list.clear();
		// delete all records in the ContentProvider
		int nRecs = mContext.getContentResolver().delete(SelfiesContract.CONTENT_URI, null, null);
		Log.i(TAG, String.format("%d records deleted in ContentProvider", nRecs));
	}

	@Override
	public void bindView(View view, Context context, Cursor cursor) {
		ViewHolder holder = (ViewHolder) view.getTag();
		// Date and time
		holder.date.setText(context.getString(R.string.date_string)
				+ cursor.getString(cursor
						.getColumnIndex(SelfiesContract.DATE)));
		holder.time.setText(context.getString(R.string.time_string)
				+ cursor.getString(cursor
						.getColumnIndex(SelfiesContract.TIME)));
		// selfie file full pathname
		String selfieFile = cursor.getString(cursor.getColumnIndex(SelfiesContract.SELFIE_FILE_PATH));
		holder.selfieFile = selfieFile;
		// selfie thumbnail
		holder.selfie.setImageBitmap(getBitmapFromFile(cursor.getString(cursor
				.getColumnIndex(SelfiesContract.SELFIE_BITMAP_PATH))));

		holder.selfie.setTag(selfieFile);
		//TODO listener al click
		holder.selfie.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				String msg = (String )v.getTag();
				Log.d(TAG, "msg tag: " + msg);
				Intent intent = new Intent(mContext, SelfieDetailActivity.class);
				intent.putExtra(SelfieListActivity.EXTRA_MESSAGE, msg);
				intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				mContext.startActivity(intent);
			}
		});
	}

	@Override
	public View newView(Context context, Cursor cursor, ViewGroup parent) {
		View newView;
		ViewHolder holder = new ViewHolder();
		newView = inflater.inflate(R.layout.selfie_view, parent, false);
		holder.selfie = (ImageView) newView.findViewById(R.id.selfie1);
		holder.date = (TextView) newView.findViewById(R.id.date1);
		holder.time = (TextView) newView.findViewById(R.id.time1);
		holder.selfieFile = "";
		newView.setTag(holder);
		return newView;
	}

	private Bitmap getBitmapFromFile(String filePath) {
		return BitmapFactory.decodeFile(filePath);
	}

	private boolean storeBitmapToFile(Bitmap bitmap, String filePath) {
		if (Environment.getExternalStorageState().equals(
				Environment.MEDIA_MOUNTED)) {
			try {
				BufferedOutputStream bos = new BufferedOutputStream(
						new FileOutputStream(filePath));
				bitmap.compress(CompressFormat.PNG, 100, bos);
				bos.flush();
				bos.close();
			} catch (FileNotFoundException e) {
				return false;
			} catch (IOException e) {
				return false;
			}
			return true;
		}
		return false;
	}
	
}
