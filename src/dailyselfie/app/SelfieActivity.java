package dailyselfie.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
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

public class SelfieActivity extends Activity {
	private static final String TAG = "Daily-Selfie-App"; 
	// Usually there is at least a camera present, but not always
	private boolean hasCamera = true;
	private ImageView mImageView1;
	private ToggleButton mToggleButton1;
	private Button mButton1;
	private static final int ACTION_TAKE_SELFIE = 1;


	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		setContentView(R.layout.test_foto_simple);
		mImageView1 = (ImageView) findViewById(R.id.imageView1);
		// Long presses invoke Context Menu
		registerForContextMenu(mImageView1);
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
						getResources().getText(R.string.take_selfie),
						Toast.LENGTH_LONG).show();
			}
		});
		//verify if camera present
		if(!verifyCamera()) {
			mToggleButton1.setChecked(false);
		}
	}

	//verify camera present, disable app behaviours if not
	public boolean verifyCamera() {
		hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		if (!hasCamera) {
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.no_camera),
					Toast.LENGTH_LONG).show();
		}
		return hasCamera;
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

	
	/* ********* ********* MENUS ********* ********* */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selfie_menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		switch (item.getItemId()) {
		case R.id.menu_action_settings:
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.action_settings),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.menu_take_selfie:
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.take_selfie),
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Create Context Menu
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		super.onCreateContextMenu(menu, v, menuInfo);
		getMenuInflater().inflate(R.menu.selfie_context_menu, menu);
	}

	// Process clicks on Context Menu Items
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		// SALE UN ERROR PERO ES CULPA DEL SAMSUNG S3
		switch (item.getItemId()) {
		case R.id.context_menu_help_guide:
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.help_guide),
					Toast.LENGTH_LONG).show();
			return true;
		case R.id.context_menu_take_selfie:
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.take_selfie),
					Toast.LENGTH_LONG).show();
			return true;
		default:
			return false;
		}
	}

}
