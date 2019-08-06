package de.albsig.qischecker.network;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.Observer;
import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkInfo;
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

    public static void startRefreshTask(final Context context, boolean update) {
        SharedPreferences sp = getSharedPreferences(context);

        // If the polling time less than the minimum, reset it to the minimum minutes
        long minutes = Long.valueOf(getSharedPreferences(context).getString(context.getResources().getString(R.string.preference_refresh_rate), "60"));
        String minValue = context.getResources().getStringArray(R.array.array_refresh_time_key)[0];
        if (minutes < Integer.parseInt(minValue)) {
            sp.edit().putString(context.getString(R.string.preference_refresh_rate), minValue).apply();
        }

        // Everything ok, start unique worker with flex interval of 10 minutes
        WorkManager workManager = WorkManager.getInstance(context);
        refreshWork = new PeriodicWorkRequest.Builder(BackgroundWorker.class, minutes, TimeUnit.MINUTES, 10, TimeUnit.MINUTES)
                //.setConstraints(constraints)
                .build();
        // Set ExistingPeriodicWorkPolicy
        ExistingPeriodicWorkPolicy policy = ExistingPeriodicWorkPolicy.KEEP;
        if (update)
            policy = ExistingPeriodicWorkPolicy.REPLACE;
        workManager.enqueueUniquePeriodicWork("BackgroundWorker", policy, refreshWork);
    }

    private static SharedPreferences getSharedPreferences(Context con) {
        return PreferenceManager.getDefaultSharedPreferences(con);
    }

}
