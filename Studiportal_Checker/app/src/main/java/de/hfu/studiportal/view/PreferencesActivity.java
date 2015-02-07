package de.hfu.studiportal.view;

import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import de.hfu.funfpunktnull.R;

/**
 * Empty Activity to hold the {@link PreferencesFragment}
 * @author preussjan
 * @since 1.0
 * @version 1.0
 */
public class PreferencesActivity extends DialogHostActivity {


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        //Build View
        setContentView(R.layout.activity_preferences);

        //Set up Toolbar
        Toolbar bar = (Toolbar) findViewById(R.id.toolbar);
        this.setSupportActionBar(bar);
        this.getSupportActionBar().setTitle(this.getString(R.string.text_activity_preferences_title));
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);

		// Load the preferences from an XML resource
		getFragmentManager().beginTransaction().replace(R.id.preferences_fragment,
				new PreferencesFragment()).commit();

	}

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home) {
            this.finish();
            return true;

        }

        return super.onOptionsItemSelected(item);

    }
}