package dailyselfie.app;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
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
import android.view.WindowManager;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Gallery;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SelfieActivity extends Activity {
	private static final String TAG = "Daily-Selfie-App"; 
	private ImageView mImageView1;
	private ImageView mImageView2;
	private Gallery mGallery1;
	private ToggleButton mToggleButton1;
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

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.selfie_main_layout);
		//
		mImageView1 = (ImageView) findViewById(R.id.imageView1);
		mImageView2 = (ImageView) findViewById(R.id.imageView2);
		mGallery1 = (Gallery) findViewById(R.id.gallery1);
		registerForContextMenu(mImageView1);  //N42 Long presses invoke Context Menu
		//
		mToggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
		mToggleButton1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//
				Log.d(TAG, "ToogleButton state: " + isChecked);
				if(isChecked){
					if(!mPhotoTaking.enable().isEnabled()) {
						buttonView.toggle();
					}
				} else {
					mPhotoTaking.disable();  //N42 disable photo taking
				}
				invalidateOptionsMenu(); //
			}
		});
		//verify if is posible photo taking
		mPhotoTaking = new PhotoTaking();
		if (!mPhotoTaking.isEnabled()) {
			mToggleButton1.setChecked(false);
			invalidateOptionsMenu();
		}
		//N42 get display rotation
		mDisplay = ((WindowManager) getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay(); 
		int rot = mDisplay.getRotation();Log.d(TAG, "Display rotation: " + rot);
		//
		mImageBitmap = null;
		//

	}

	//N42 Dispatch an intent for other app take the selfie
	private void takeSelfie() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File selfieFile = null;
		try {
			// Create a temporal file
			String timeStamp = (String) DateFormat.format("yyyyMMdd_HHmmss", new Date());
			String imageFileName = SELFIE_FILE_PREFIX + timeStamp + "_";
			selfieFile = File.createTempFile(imageFileName, SELFIE_FILE_SUFFIX, getSelfiesDir());
			Log.d(TAG, "selfieFile: " + selfieFile.getName());
			mCurrentSelfiePath = selfieFile.getAbsolutePath();
			Log.d(TAG, "mCurrentSelfiePath: " + mCurrentSelfiePath);
			//completes intent
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(selfieFile));
		} catch (IOException e) {
			e.printStackTrace();
			selfieFile = null;
			mCurrentSelfiePath = null;
		}
		startActivityForResult(takePictureIntent, REQUEST_TAKE_SELFIE);
	}

	private File getSelfiesDir() {
		File selfiesDir = null;
		if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {
			Log.i(TAG, "DIRECTORY_PICTURES: " + Environment.DIRECTORY_PICTURES);
			selfiesDir = (new File(Environment.getExternalStoragePublicDirectory(
					Environment.DIRECTORY_PICTURES), getString(R.string.selfies_dirname)));
			Log.i(TAG, "storageDir: " + selfiesDir.getAbsolutePath());
			if (selfiesDir != null && !selfiesDir.mkdirs() && !selfiesDir.exists()) {
				Log.e(TAG, "failed to create directory");
				return null;
			}
		} else {
			Log.w(TAG, "External storage is not mounted READ/WRITE.");
		}
		return selfiesDir;
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == REQUEST_TAKE_SELFIE && resultCode == RESULT_OK) {
			//get the thumbnail
			//Bundle extras = data.getExtras();
			//Bitmap thumbnailBitmap = (Bitmap) extras.get("data");
	        //mImageView2.setImageBitmap(thumbnailBitmap);
			if (mCurrentSelfiePath != null) {
				setPic();
				galleryAddPic();
				mCurrentSelfiePath = null;
			}
		}
	}

	private void setPic() {
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */
		/* Get the size of the ImageView */
		int targetW = mImageView1.getWidth();
		int targetH = mImageView1.getHeight();

		/* Get the size of the image */
		BitmapFactory.Options bmOptions = new BitmapFactory.Options();
		bmOptions.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(mCurrentSelfiePath, bmOptions);
		int photoW = bmOptions.outWidth;
		int photoH = bmOptions.outHeight;
		
		/* Figure out which way needs to be reduced less */
		int scaleFactor = 1;
		//N42 TODO would be instead: (targetW > 0) && (targetH > 0) ??
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		//N42 problema camara rotada
		int rotateDegrees = 0;
		ExifInterface ei;
		int orientation;
		try {
			ei = new ExifInterface(mCurrentSelfiePath);
			orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);
			Log.d(TAG, "Exif ORIENTATION: " + orientation);
			switch(orientation) {
		    case ExifInterface.ORIENTATION_NORMAL:
		    	rotateDegrees = 0;
				Log.d(TAG, "Exif ORIENTATION_NORMAL, rotate(degrees): " + rotateDegrees);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_90:
		    	rotateDegrees = 90;
				Log.d(TAG, "Exif ORIENTATION_ROTATE_90, rotate(degrees): " + rotateDegrees);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_180:
		    	rotateDegrees = 180;
				Log.d(TAG, "Exif ORIENTATION_ROTATE_180, rotate(degrees): " + rotateDegrees);
		        break;
		    case ExifInterface.ORIENTATION_ROTATE_270:
		    	rotateDegrees = 270;
				Log.d(TAG, "Exif ORIENTATION_ROTATE_270, rotate(degrees): " + rotateDegrees);
		        break;
		    default:
				Log.d(TAG, "Exif ORIENTATION_UNDEFINED or OTHERS, rotate(degrees): " + rotateDegrees);
			}
		} catch (IOException e) {
			e.printStackTrace();
		};
		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap1 = BitmapFactory.decodeFile(mCurrentSelfiePath, bmOptions);
		//N42 rotate the bitmap, recycles bitmap
		bitmap1 = RotateBitmap(bitmap1, rotateDegrees);
		/* Associate the Bitmap to the ImageView */
		mImageView1.setImageBitmap(bitmap1);
		mImageView1.setVisibility(View.VISIBLE);

		//thumbnail from bitmap already rotated
		mImageView2.setMaxHeight(targetH/3);
		mImageView2.setMaxWidth(targetW/3);
		//Bitmap bitmap2 = Bitmap.createScaledBitmap(bitmap1, mImageView2.getWidth(), mImageView2.getHeight(), false);
		mImageView2.setImageBitmap(bitmap1);
		mImageView2.setVisibility(View.VISIBLE);
	}

	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentSelfiePath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
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
			Log.d(TAG, "menu_take_selfie");
			takeSelfie();
			return true;
		case R.id.menu_action_settings:
			Toast.makeText(getApplicationContext(),
					getText(R.string.action_settings),
					Toast.LENGTH_LONG).show();
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
		}
	}
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		//N42 ignore logcat ERROR for SAMSUNG GALAXY S3
		switch (item.getItemId()) {
		case R.id.context_menu_help_guide:
			Toast.makeText(getApplicationContext(),
					getText(R.string.help_guide),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.context_menu_take_selfie:
			Toast.makeText(getApplicationContext(),
					getText(R.string.take_selfie),
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return false;
		}
	}
	
	//  ********** Helper class **********
	public static Bitmap RotateBitmap(Bitmap source, float angle)
	{
	      Matrix matrix = new Matrix();
	      matrix.postRotate(angle);
	      return Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, true);
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

}
