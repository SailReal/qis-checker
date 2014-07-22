package de.hfu.studiportal.view;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import de.hfu.funfpunktnull.R;
import de.hfu.studiportal.network.NoChangeException;
import de.hfu.studiportal.network.RefreshTask;
import de.hfu.studiportal.network.RefreshTaskStarter;

public class MainActivity extends DialogHostActivity {
	
	private ExamCategoryPagerAdapter pagerAdapter;
	private ViewPager viewPager;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		//Start Background Service
		RefreshTaskStarter.startRefreshTask(this);
		
		//Build View
		setContentView(R.layout.activity_main);

		//Set Up ViewPager
		viewPager = (ViewPager) findViewById(R.id.pager);
		pagerAdapter = new ExamCategoryPagerAdapter(getSupportFragmentManager(), this);
		viewPager.setAdapter(pagerAdapter);
		
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {

		getMenuInflater().inflate(R.menu.activity_main, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	protected void onResume() {
		super.onResume();
		this.cancelProgressDialog();

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if(item.getItemId() == R.id.action_refresh) {
			new RefreshTask(this).execute();

			return true;
		}
		
		if(item.getItemId() == R.id.action_preferences) {
			Intent i = new Intent(this, PreferencesActivity.class);
			this.startActivity(i);

			return true;
		}

		if(item.getItemId() == R.id.action_open_online) {
			Intent i = new Intent(Intent.ACTION_VIEW);
			i.setData(Uri.parse("https://studi-portal.hs-furtwangen.de/"));
			this.startActivity(i);

			return true;
		}

		
		return super.onOptionsItemSelected(item);

	}

	@Override
	public void showErrorDialog(final Exception e) {

		if(e instanceof NoChangeException) {
			//No change
			Toast.makeText(MainActivity.this, getResources().getString(R.string.text_no_change), Toast.LENGTH_SHORT).show();

		}else {
			super.showErrorDialog(e);

		}
	}

}
