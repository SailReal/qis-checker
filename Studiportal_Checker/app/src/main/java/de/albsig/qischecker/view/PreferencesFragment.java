package de.albsig.qischecker.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;

import de.albsig.qischecker.R;
import de.albsig.qischecker.data.StudiportalData;
import de.albsig.qischecker.network.RefreshTaskStarter;

/**
 * Fragment to display the settings.xml
 *
 * @author preussjan
 * @version 1.0
 * @since 1.0
 */
public class PreferencesFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener {

    private static final String TAG = "PreferencesFragment";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        updateSummaries();
        PreferenceManager.setDefaultValues(this.getActivity(), R.xml.preferences, false);
        PreferenceManager.getDefaultSharedPreferences(this.getActivity()).registerOnSharedPreferenceChangeListener(this);

        Preference logout = this.findPreference(getResources().getString(R.string.preference_logout));
        logout.setOnPreferenceClickListener(new OnPreferenceClickListener() {

            @Override
            public boolean onPreferenceClick(Preference preference) {

                // Use the Builder class for convenient dialog construction
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage(R.string.text_logout_dialog)
                        .setNegativeButton(R.string.text_cancel, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                            }
                        })
                        .setPositiveButton(R.string.preferences_logout_title, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {
                                //Stop update task
                                RefreshTaskStarter.cancelRefreshTask(getActivity());

                                //Delete login-info
                                Editor sp = PreferenceManager.getDefaultSharedPreferences(getActivity()).edit();
                                sp.putString(getResources().getString(R.string.preference_last_studiportal_data), "");
                                sp.putString(getResources().getString(R.string.preference_password), "");
                                sp.putString(getResources().getString(R.string.preference_last_studiportal_data), "");
                                sp.apply();

                                //Go to Login Activity
                                Intent i = new Intent(PreferencesFragment.this.getActivity(), LoginActivity.class);
                                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                PreferencesFragment.this.getActivity().startActivity(i);

                                //Finish Activity
                                PreferencesFragment.this.getActivity().finish();

                            }
                        });

                // Create the AlertDialog object and return it
                builder.create().show();
                return true;

            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = super.onCreateView(inflater, container, savedInstanceState);
        view.setBackgroundColor(getResources().getColor(R.color.color_activity_background));

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        PreferenceManager.getDefaultSharedPreferences(this.getActivity()).unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        updateSummaries();

    }

    private void updateSummaries() {
        //Get Timestamp of last check
        String key = getResources().getString(R.string.preference_last_check);
        long lastCheck = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getLong(key, 0);

        //inti date string
        String dateString = "";

        //If it was refreshed
        if (lastCheck > 0) {
            Date d = new Date(lastCheck);
            dateString = getResources().getString(R.string.text_last_updated);
            dateString += new SimpleDateFormat("dd.MM.yyyy, HH:mm:ss").format(d);
            Log.i(this.getClass().getSimpleName(), dateString);

        }

        //Set the summary or an empty string if never refreshed
        Preference p = this.findPreference(getResources().getString(R.string.preference_refresh_rate));
        p.setSummary(dateString);

        //Display username
        key = getResources().getString(R.string.preference_user);
        String username = PreferenceManager.getDefaultSharedPreferences(this.getActivity()).getString(key, "");
        p = this.findPreference(getResources().getString(R.string.preference_logout));
        p.setSummary(username);

        ListPreference defaultCategory = (ListPreference) this.findPreference(getResources().getString(R.string.preference_default_category));
        try {
            StudiportalData data = StudiportalData.loadFromSharedPreferences(PreferenceManager.getDefaultSharedPreferences(this.getActivity()), getResources().getString(R.string.preference_last_studiportal_data));
            CharSequence[] entries = new CharSequence[data.getCategoryCount()];
            for (int i = 0; i < entries.length; i++) {
                entries[i] = data.getCategory(i).getCategoryName();
            }
            CharSequence[] entryValues = new CharSequence[data.getCategoryCount()];
            for (int i = 0; i < entryValues.length; i++) {
                entryValues[i] = String.valueOf(i);
            }

            defaultCategory.setEntries(entries);
            defaultCategory.setDefaultValue("0");
            defaultCategory.setEntryValues(entryValues);
            defaultCategory.setSummary(defaultCategory.getEntry());
        } catch (Exception e) {
            Log.e(TAG, "Can't load StudiportalData", e);
        }
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
                                          String key) {

        updateSummaries();

        if (key.equals(getResources().getString(R.string.preference_refresh_rate))) {
            RefreshTaskStarter.startRefreshTask(this.getActivity(), true);

        }

    }
}
