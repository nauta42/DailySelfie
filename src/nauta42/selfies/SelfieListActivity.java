package nauta42.selfies;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import nauta42.selfies.provider.SelfiesContract;
import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.Context;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ShareActionProvider;
import android.widget.Toast;

public class SelfieListActivity extends ListActivity implements LoaderCallbacks<Cursor> {
	private static final String TAG = "DailySelfie_ListActivity"; 
	private ImageView mImageView1;
	private static final int REQUEST_TAKE_SELFIE = 1;
	private String mCurrentSelfiePath;
	private static final String SELFIE_FILE_PREFIX = "SELFIE_";
	private static final String SELFIE_FILE_SUFFIX = ".jpg";
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private Bitmap mImageBitmap;
	private ShareActionProvider mShareActionProvider;
	private PhotoTaking mPhotoTaking;
	private Display mDisplay;
	private SelfieListAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		ListView selfiesListView = getListView();
		//footer
		selfiesListView.setFooterDividersEnabled(true);
		View footerView = getLayoutInflater().inflate(R.layout.footer_view, selfiesListView, false);
		footerView.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				Log.d(TAG, "Entered footerView.OnClickListener.onClick()");
				takeSelfie();
			}
		});
		selfiesListView.addFooterView(footerView);
		//
		mAdapter = new SelfieListAdapter(getApplicationContext(), null, 0);
		setListAdapter(mAdapter);
		// Prepare the loader.  Either re-connect with an existing one,
		// or start a new one, relates to CursorLoader
		getLoaderManager().initLoader(0, null, this);

		//verify if is posible photo taking
		mPhotoTaking = new PhotoTaking();
		if (!mPhotoTaking.isEnabled()) {
			new AlertDialog.Builder(this).
			setMessage(R.string.no_camera)
			.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// nothing to do
				}
			}).show();
			footerView.setEnabled(false);
			invalidateOptionsMenu();
		}
		//N42 get display rotation
		mDisplay = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int rot = mDisplay.getRotation();Log.d(TAG, "Display rotation: " + rot);
		//??
		mImageBitmap = null;
	}

	private File getSelfiesDir() {
		File selfiesDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.i(TAG, "DIRECTORY_PICTURES: " + Environment.DIRECTORY_PICTURES);
			selfiesDir = (new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES), getString(R.string.selfies_dirname)));
			Log.i(TAG, "storageDir: " + selfiesDir.getAbsolutePath());
			//uses (or creates) a dir for the selfies
			if (selfiesDir != null && !selfiesDir.mkdirs() && !selfiesDir.exists()) {
				Log.e(TAG, "failed to create directory");
				return null;
			}
		} else {
			Log.w(TAG, "External storage is not mounted READ/WRITE.");
		}
		return selfiesDir;
	}

	private File createImageFile() throws IOException {
	    // Create an image file name
		File selfieFile = null;
		String timeStamp = (String) DateFormat.format("yyyyMMdd_HHmmss", new Date());
		String imageFileName = SELFIE_FILE_PREFIX + timeStamp + "_";
	    selfieFile = File.createTempFile(imageFileName, SELFIE_FILE_SUFFIX, getSelfiesDir());
		Log.d(TAG, "selfieFile: " + selfieFile.getName());
	    // Save a file: path for use with ACTION_VIEW intents
		mCurrentSelfiePath = selfieFile.getAbsolutePath();
	    //mCurrentSelfiePath = "file:" + selfieFile.getAbsolutePath();
		Log.d(TAG, "mCurrentSelfiePath: " + mCurrentSelfiePath);
	    return selfieFile;
	}
	
	//N42 Dispatch an intent for other app take the selfie
	private void takeSelfie() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File selfieFile = null;
		try {
			selfieFile = createImageFile();  // Create a temporal file
		} catch (IOException e) {
			e.printStackTrace();
			selfieFile = null;
			mCurrentSelfiePath = null;
		}
		if(selfieFile != null) {
			//completes intent with the file
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(selfieFile));
			startActivityForResult(takePictureIntent, REQUEST_TAKE_SELFIE);
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_SELFIE && resultCode == RESULT_OK) {
			if (mCurrentSelfiePath != null) {
				// SelfieRecord
				int w = (int)getResources().getDimensionPixelSize(R.dimen.thumbnail_selfie_width);
				int h = (int)getResources().getDimensionPixelSize(R.dimen.thumbnail_selfie_height);
				Bitmap thumbnail = getScaledBitmap(w, h, mCurrentSelfiePath);
				Date d = new Date();
				String date = (String) DateFormat.format("yyyyMMdd", d);
				String time = (String) DateFormat.format("HHmmss", d);
				SelfieRecord selfie = new SelfieRecord(mCurrentSelfiePath, date, time);
				selfie.setSelfieBitmap(thumbnail);
				Log.d(TAG, selfie.toString());
				//
				mAdapter.add(selfie);
				//
				galleryAddPic();
				mCurrentSelfiePath = null;
			}
		}
	}

	//add the selfie to the Media Provider's database, making it available to other apps
	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentSelfiePath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
	}

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
	

	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    // Checks the orientation of the screen
	    if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
	    	Log.d(TAG, "LANSCAPE");
	        Toast.makeText(this, "landscape", Toast.LENGTH_SHORT).show();
	    } else if (newConfig.orientation == Configuration.ORIENTATION_PORTRAIT){
	    	Log.d(TAG, "PORTRAIT");
	        Toast.makeText(this, "portrait", Toast.LENGTH_SHORT).show();
	    }
	}
	
	// Some lifecycle callbacks so that the image can survive orientation change
	@Override
	protected void onSaveInstanceState(Bundle outState) {
		outState.putParcelable(BITMAP_STORAGE_KEY, mImageBitmap);
		outState.putBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY, (mImageBitmap != null) );
		super.onSaveInstanceState(outState);
	}

	@Override
	protected void onRestoreInstanceState(Bundle savedInstanceState) {
		super.onRestoreInstanceState(savedInstanceState);
		mImageBitmap = savedInstanceState.getParcelable(BITMAP_STORAGE_KEY);
		mImageView1.setImageBitmap(mImageBitmap);
		mImageView1.setVisibility(
				savedInstanceState.getBoolean(IMAGEVIEW_VISIBILITY_STORAGE_KEY) ? 
						ImageView.VISIBLE : ImageView.INVISIBLE
		);
	}
	
	//  ********** OPTIONS MENU ********** 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selfie_menu, menu);
		//N42 Set up ShareActionProvider
		MenuItem menuItem = menu.findItem(R.id.menu_action_share);
		mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
		mShareActionProvider.setShareIntent(new Intent(Intent.ACTION_SEND).setType("image/*"));
		return super.onCreateOptionsMenu(menu);
	}
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		//N42 disable photo taking
		menu.findItem(R.id.menu_take_selfie).setEnabled(mPhotoTaking.isEnabled());
		return super.onPrepareOptionsMenu(menu);
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here.
		switch (item.getItemId()) {
		case R.id.menu_take_selfie:
			takeSelfie();
			return true;
		case R.id.menu_help_guide:
			Toast.makeText(getApplicationContext(),
					getText(R.string.help_guide),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_action_settings:
			Toast.makeText(getApplicationContext(),
					getText(R.string.action_settings),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_delete_selfies:
			Toast.makeText(getApplicationContext(),
					getText(R.string.delete_selfies),
					Toast.LENGTH_LONG).show();
			mAdapter.removeAllViews();
			return true;
		case R.id.menu_about:
			Toast.makeText(getApplicationContext(),
					getText(R.string.about),
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//  ********** Context Menu **********
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//N42 switch each context menu 
		if(v.getId() == R.id.imageView1){
			getMenuInflater().inflate(R.menu.selfie_context_menu, menu);
			//N42 disable photo taking
			menu.findItem(R.id.context_menu_take_selfie).setEnabled(mPhotoTaking.isEnabled());
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//N42 ignore logcat ERROR for SAMSUNG GALAXY S3
		switch (item.getItemId()) {
		case R.id.context_menu_details:
			Toast.makeText(getApplicationContext(),
					getText(R.string.TODO),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.context_menu_take_selfie:
			takeSelfie();
			return true;
		default:
			return false;
		}
	}
	
	//  ********** Helper class **********
	class PhotoTaking {
		//states
		private boolean enabled = false;
		private boolean abled = false;
		//usually there are camera(s) and camera apps presents but..
		private boolean hasCamera = false;
		private int nCameraApps = 0;
		private boolean hasCameraApp = false;
		//N42 by default enable
		public PhotoTaking() {
			this.enabled = isAbled();
		}
		//N42 verify if can be enabled
		private boolean isAbled() {
			hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
			//num of apps that can respond this intent
			nCameraApps = getApplicationContext().getPackageManager().queryIntentActivities(
					new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
					PackageManager.MATCH_DEFAULT_ONLY).size();
			Log.d(TAG, "# Camera Apps Founded: " + nCameraApps);
			hasCameraApp = (nCameraApps > 0);
			if (hasCamera && hasCameraApp) {
				Log.i(TAG, getString(R.string.photo_taking_ok));
				this.abled = true;
			} else {
				Log.w(TAG, getString(R.string.photo_taking_no));
				this.abled = false;
			}
			return this.abled;
		}
		public boolean isEnabled() {
			Log.d(TAG, "Photo taking isEnabled? " + this.enabled);
			return this.enabled;
		}
		public PhotoTaking enable() {
			Log.d(TAG, "Trying to enable Photo taking..");
			this.enabled = isAbled();
			return this;
		}
		public PhotoTaking disable() {
			//disable taking photos
			Log.d(TAG, "Trying to disable Photo taking..");
			//reinforcer disable?
			this.enabled = false;
			Log.w(TAG, getString(R.string.photo_taking_no));
			return this;
		}
	}

	// LoaderCallback methods
	@Override
	public Loader<Cursor> onCreateLoader(int id, Bundle args) {
		//Create a new CursorLoader and return it
		return new CursorLoader(this, SelfiesContract.CONTENT_URI, null, null, null, null);
	}

	@Override
	public void onLoadFinished(Loader<Cursor> loader, Cursor newCursor) {
		// Swap in the newCursor
		mAdapter.swapCursor(newCursor);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> loader) {
		// swap in a null Cursor
		mAdapter.swapCursor(null);
	}

}
