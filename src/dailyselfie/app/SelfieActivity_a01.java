package dailyselfie.app;

import java.io.File;
import java.io.IOException;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.ToggleButton;

public class SelfieActivity_a01 extends Activity {
	private static final String TAG = "Daily-Selfie-App"; 
	// Usually there are camera(s) and camera apps presents
	private boolean hasCamera = false;
	private boolean hasCameraApp = false;
	private int nCameraApps = 0;
	private ImageView mImageView1;
	private ToggleButton mToggleButton1;
	private Button mButton1;
	private static final int CODE_TAKE_SELFIE = 1;
	private String mCurrentSelfiePath;
	private static final String SELFIE_FILE_PREFIX = "SELFIE_";
	private static final String SELFIE_FILE_SUFFIX = ".jpg";
	private static final String BITMAP_STORAGE_KEY = "viewbitmap";
	private static final String IMAGEVIEW_VISIBILITY_STORAGE_KEY = "imageviewvisibility";
	private Bitmap mImageBitmap;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.test_foto_simple_a01);
		mImageView1 = (ImageView) findViewById(R.id.imageView1);
		registerForContextMenu(mImageView1);  //N42 Long presses invoke Context Menu
		mImageBitmap = null;
		//
		mToggleButton1 = (ToggleButton) findViewById(R.id.toggleButton1);
		mToggleButton1.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				//
				Log.i(TAG, "ToogleButton state: " + isChecked);
				if(isChecked){
					if(verifyCamera()) {
						enableCamera();
					} else {
						disableCamera();
						buttonView.toggle();
					}
				} else {
					disableCamera();
				}
			}
		});
		//to take the photo
		mButton1 = (Button) findViewById(R.id.button1);
		mButton1.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				//
				Toast.makeText(getApplicationContext(),
						getText(R.string.take_selfie),
						Toast.LENGTH_LONG).show();
				takeSelfie();
			}
		});
		//
		//
		//verify if camera present
		if(!verifyCamera()) {
			mToggleButton1.setChecked(false);
		}
	}

	//N42 Dispatch an intent for other app take the selfie
	private void takeSelfie() {
		Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		File selfieFile = null;
		try {
			// Create an image file name
			String timeStamp = (String) DateFormat.format("yyyyMMdd_HHmmss", new Date());
			String imageFileName = SELFIE_FILE_PREFIX + timeStamp + "_";
			selfieFile = File.createTempFile(imageFileName, SELFIE_FILE_SUFFIX, getSelfiesDir());
			Log.i(TAG, "selfieFile: " + selfieFile.getName());
			mCurrentSelfiePath = selfieFile.getAbsolutePath();
			Log.i(TAG, "mCurrentSelfiePath: " + mCurrentSelfiePath);
			takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(selfieFile));
		} catch (IOException e) {
			e.printStackTrace();
			selfieFile = null;
			mCurrentSelfiePath = null;
		}
		startActivityForResult(takePictureIntent, CODE_TAKE_SELFIE);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == CODE_TAKE_SELFIE && resultCode == RESULT_OK) {
			handleBigCameraPhoto();
		}
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
		if ((targetW > 0) || (targetH > 0)) {
			scaleFactor = Math.min(photoW/targetW, photoH/targetH);	
		}

		/* Set bitmap options to scale the image decode target */
		bmOptions.inJustDecodeBounds = false;
		bmOptions.inSampleSize = scaleFactor;
		bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
		Bitmap bitmap = BitmapFactory.decodeFile(mCurrentSelfiePath, bmOptions);
		
		/* Associate the Bitmap to the ImageView */
		mImageView1.setImageBitmap(bitmap);
		mImageView1.setVisibility(View.VISIBLE);
	}

	private void galleryAddPic() {
		    Intent mediaScanIntent = new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE");
			File f = new File(mCurrentSelfiePath);
		    Uri contentUri = Uri.fromFile(f);
		    mediaScanIntent.setData(contentUri);
		    this.sendBroadcast(mediaScanIntent);
	}

	private void handleBigCameraPhoto() {
		if (mCurrentSelfiePath != null) {
			setPic();
			galleryAddPic();
			mCurrentSelfiePath = null;
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

	//verify camera present, disable app behaviours if not
	public boolean verifyCamera() {
		hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		//num of apps that can respond this intent
		nCameraApps = getApplicationContext().getPackageManager().queryIntentActivities(
				new Intent(MediaStore.ACTION_IMAGE_CAPTURE),
				PackageManager.MATCH_DEFAULT_ONLY).size();
		Log.i(TAG, "# Camera Apps Founded: " + nCameraApps);
		hasCameraApp = (nCameraApps > 0);
		if (hasCamera && hasCameraApp) {
			return true;
		} else {
			Toast.makeText(getApplicationContext(),
					getText(R.string.no_camera),
					Toast.LENGTH_LONG).show();
			return false;
		}
	}

	public void enableCamera() {
		Log.i(TAG, "enableCamera()");
		mButton1.setEnabled(true);
	}
	
	public void disableCamera() {
		Log.i(TAG, "disableCamera()");
		//disable button to take photos
		mButton1.setEnabled(false);
	}

	
	//N42 ** **** **** **** **** **** **MENUS** **** **** **** **** **** ** 
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selfie_menu_a01, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here.
		switch (item.getItemId()) {
		case R.id.menu_take_selfie:
			takeSelfie();
			Toast.makeText(getApplicationContext(),
					getText(R.string.take_selfie),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_action_settings:
			Toast.makeText(getApplicationContext(),
					getText(R.string.action_settings),
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	//N42 Create Context Menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		//N42 switch each context menu 
		if(v.getId() == R.id.imageView1){
			getMenuInflater().inflate(R.menu.selfie_context_menu_a01, menu);
		}
	}

	//N42 Process clicks on Context Menu Items
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

}
