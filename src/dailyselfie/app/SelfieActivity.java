package dailyselfie.app;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class SelfieActivity extends Activity {
	// Usually there is at least a camera present, but not always
	private boolean hasCamera = true;
	private ImageView mImageView1;
	private Button mButton1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.main);
		setContentView(R.layout.test_foto_simple);
		mImageView1 = (ImageView) findViewById(R.id.imageView1);
		//take the photo
		mButton1 = (Button) findViewById(R.id.button1);
		mButton1.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});

		//verify if camera present
		verifyCamera();
		
	}

	//verify camera present, disable app behaviours if not
	public void verifyCamera() {
		hasCamera = getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
		if (!hasCamera) {
			Toast.makeText(getApplicationContext(),
					getResources().getText(R.string.no_camera),
					Toast.LENGTH_LONG).show();
			//disable button to take photos
			if(mButton1 != null) mButton1.setEnabled(false);
		}
		
	}
	
	/* MENUS */
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selfie, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
