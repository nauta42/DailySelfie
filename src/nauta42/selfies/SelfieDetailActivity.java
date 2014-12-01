package nauta42.selfies;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

public class SelfieDetailActivity extends Activity {
	private static final String TAG = "DailySelfie_SelfieDetailActivity"; 

	private ImageView imageView1;
	//private ImageView imageView2;
	private Bitmap bitmap1;
	SelfieHelper sf = new SelfieHelper();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_selfie_detail);
		imageView1 = (ImageView) findViewById(R.id.imageView1);
		//imageView2 = (ImageView) findViewById(R.id.imageView2);
		
		Intent intent = getIntent();
		String message = intent.getStringExtra(SelfieListActivity.EXTRA_MESSAGE);
		Log.d(TAG, message);
		bitmap1 = sf.getScaledBitmap(imageView1.getWidth(), imageView1.getHeight(), message);
		imageView1.setImageBitmap(bitmap1);
		imageView1.setVisibility(View.VISIBLE);

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.selfie_detail, menu);
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
