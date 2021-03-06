package de.albsig.qischecker.view;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;

import de.albsig.qischecker.R;
import de.albsig.qischecker.data.ExamCategory;
import de.albsig.qischecker.network.NoChangeException;
import de.albsig.qischecker.network.RefreshTask;
import de.albsig.qischecker.network.RefreshTaskStarter;

public class MainActivity extends DialogHostActivity implements Refreshable, AdapterView.OnItemClickListener, View.OnClickListener {

    public static final String QIS_URL = "https://qis.hs-albsig.de/";

    private ExamCategoryArrayAdapter examCategoryAdapter;
    private ActionBarDrawerToggle drawerToggle;
    private DrawerLayout drawerLayout;
    private boolean isDestroyed = false;
    private ListView examCategoryList;
    private Integer selectedCategory = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //If the user is empty, forward to LoginActivity
        if (!this.isLoggedIn()) {
            Intent i = new Intent(this, LoginActivity.class);
            this.startActivity(i);
            this.finish();
            return;
        }

        //Build View
        this.setContentView(R.layout.activity_main);

        //Load user name and password
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String user = prefs.getString(this.getString(R.string.preference_user), "");

        selectedCategory = Integer.parseInt(prefs.getString(getResources().getString(R.string.preference_default_category), "0"));

        //Set username
        TextView userView = this.findViewById(R.id.textViewUser);
        userView.setText(user);

        //Set up Toolbar
        Toolbar bar = findViewById(R.id.toolbar);
        this.setSupportActionBar(bar);
        this.getSupportActionBar().setTitle(this.getString(R.string.app_name));

        //Set Up Navigation Drawer
        this.drawerLayout = findViewById(R.id.drawer_layout);

        //Set up drawer toggle which will be put in the left side of the ActionBar
        this.drawerToggle = new ActionBarDrawerToggle(
                this,              /* host Activity */
                drawerLayout,      /* DrawerLayout object */
                R.string.app_name, /* "open drawer" description */
                R.string.app_name  /* "close drawer" description */
        ) {

            /** Called when a drawer has settled in a completely closed state. */
            public void onDrawerClosed(View view) {
                super.onDrawerClosed(view);
            }

            /** Called when a drawer has settled in a completely open state. */
            public void onDrawerOpened(View view) {
                super.onDrawerClosed(view);
            }
        };

        // Set the drawer toggle as the DrawerListener
        this.drawerLayout.setDrawerListener(this.drawerToggle);

        //Setup the ActionBar for the DrawerToggle
        this.getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        this.getSupportActionBar().setHomeButtonEnabled(true);

        //enable pull to refresh
        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pullToRefresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                startRefreshTask();
                pullToRefresh.setRefreshing(false);
            }
        });

        //Find exam category list
        this.examCategoryList = this.findViewById(R.id.examCategoryList);

        //Add item click listener
        this.examCategoryList.setOnItemClickListener(this);

        //Add click listener to search button (fab)
        this.findViewById(R.id.buttonSearch).setOnClickListener(this);

        //Set Up View
        this.onRefresh();

        //Start Background Service
        RefreshTaskStarter.startRefreshTask(this, false);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        // Sync the toggle state after onRestoreInstanceState has occurred.
        this.drawerToggle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.drawerToggle.onConfigurationChanged(newConfig);
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

        if (this.drawerToggle.onOptionsItemSelected(item)) {
            return true;
        }

        if (item.getItemId() == R.id.action_refresh) {
            startRefreshTask();
            return true;
        }

        if (item.getItemId() == R.id.action_preferences) {
            Intent i = new Intent(this, PreferencesActivity.class);
            this.startActivity(i);

            return true;
        }

        if (item.getItemId() == R.id.action_open_online) {
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(QIS_URL));
            this.startActivity(i);

            return true;
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void showErrorDialog(final Exception e) {

        if (e instanceof NoChangeException) {
            //No change
            Snackbar.make(this.findViewById(R.id.coordinatorLayout), getResources().getString(R.string.text_no_change), Snackbar.LENGTH_LONG).show();

        } else {
            super.showErrorDialog(e);

        }
    }

    @Override
    protected void onDestroy() {
        synchronized (this) {
            this.isDestroyed = true;
        }

        this.dismiss();

        super.onDestroy();

    }

    @Override
    public synchronized void onRefresh() {
        if (this.isDestroyed)
            return;

        //Create ExamCategoryAdapter
        this.examCategoryAdapter = new ExamCategoryArrayAdapter(this, this);

        //Set adapter
        this.examCategoryList.setAdapter(this.examCategoryAdapter);

        //Update fragment
        this.showCategory(this.selectedCategory);

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        //Save the selected Category
        this.selectedCategory = position;

        //Update fragment
        this.showCategory(this.selectedCategory);

    }


    private boolean isLoggedIn() {
        //Load user and password
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
        String user = prefs.getString(this.getString(R.string.preference_user), "");
        String password = prefs.getString(this.getString(R.string.preference_password), "");

        //If the user is empty, forward to LoginActivity
        return user.length() != 0 && password.length() != 0;
    }

    private void showCategory(int categoryIndex) {
        //Check if at least one category is available or the activity was destroyed, cancel if not
        if (this.examCategoryAdapter.getCount() == 0 || this.isDestroyed || !this.isLoggedIn()) {
            return;
        }

        //Create Fragment
        Fragment fragment = new ExamCategoryFragment();
        Bundle args = new Bundle();

        //Fetch Category
        ExamCategory category = this.examCategoryAdapter.getCategory(categoryIndex);

        // Our object is just an integer :-P
        args.putSerializable(ExamCategoryFragment.ARG_CATEGORY, category);
        fragment.setArguments(args);

        //Set Fragment
        this.getSupportFragmentManager().beginTransaction().replace(R.id.contentPanel, fragment).commitAllowingStateLoss();

        //Set title
        this.getSupportActionBar().setTitle(category.getCategoryName());

        //Hide drawer
        this.drawerLayout.closeDrawers();

    }

    private void startRefreshTask() {
        new RefreshTask(this).execute();
        RefreshTaskStarter.startRefreshTask(this, true);
    }

    @Override
    public void onClick(View v) {
        if (v == this.findViewById(R.id.buttonSearch)) {
            Intent i = new Intent(this, ExamSearchActivity.class);
            this.startActivity(i);

        }
    }
}
