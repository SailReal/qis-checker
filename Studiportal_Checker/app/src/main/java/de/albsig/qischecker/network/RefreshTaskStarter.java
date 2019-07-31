package de.albsig.qischecker.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.work.Constraints;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

import de.albsig.qischecker.R;
import de.albsig.qischecker.view.LoginActivity;

public class RefreshTaskStarter {

    // Constraint to assure that worker is only called if network is connected
    private static Constraints constraints = new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build();
    private static PeriodicWorkRequest refreshWork = null;

    public static void cancelRefreshTask(Context context) {
        WorkManager workManager = WorkManager.getInstance(context);
        if (refreshWork != null) {
            workManager.cancelWorkById(refreshWork.getId());
        }
    }

    public static void startRefreshTask(Context context) {
        //Check if user and password is available, if not start Login
        SharedPreferences sp = getSharedPreferences(context);
        String user = sp.getString(context.getResources().getString(R.string.preference_user), "");
        String password = sp.getString(context.getResources().getString(R.string.preference_password), "");
        if ((user.length() == 0 || password.length() == 0) && context instanceof Activity) {
            Intent i = new Intent(context, LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(i);

            //Quit the old Activity to prevent going back
            ((Activity) context).finish();

            return;

        }

        //If the polling time less than the minimum, reset it to the minimum minutes
        String minValue = context.getResources().getStringArray(R.array.array_refresh_time_key)[0];
        if (TimeUnit.MINUTES.convert(getPauseTime(context), TimeUnit.MILLISECONDS) < Integer.parseInt(minValue)) {
            sp.edit().putString(context.getString(R.string.preference_refresh_rate), minValue).apply();
        }

        //Everything ok, check if worker was previously running and restart
        WorkManager workManager = WorkManager.getInstance(context);
        if (refreshWork != null) {
            workManager.cancelWorkById(refreshWork.getId());
        }
        refreshWork = new PeriodicWorkRequest.Builder(BackgroundWorker.class, getPauseTime(context), TimeUnit.MINUTES)
                .setConstraints(constraints)
                .build();
        workManager.enqueue(refreshWork);


    }

    private static long getPauseTime(Context con) {
        int minutes = Integer.valueOf(getSharedPreferences(con).getString(con.getResources().getString(R.string.preference_refresh_rate), "60"));

        return TimeUnit.MILLISECONDS.convert(minutes, TimeUnit.MINUTES);
    }

    private static SharedPreferences getSharedPreferences(Context con) {
        return PreferenceManager.getDefaultSharedPreferences(con);
    }

}
