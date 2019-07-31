package de.albsig.qischecker.network;

import android.content.Context;

import android.content.SharedPreferences;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import de.albsig.qischecker.R;

public class BackgroundWorker extends Worker {

    private Context context;
    public BackgroundWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);

    }

    @Override
    public Result doWork() {
        context = getApplicationContext();
        SharedPreferences sp = getSharedPreferences(context);
        String user = sp.getString(context.getResources().getString(R.string.preference_user), "");
        String password = sp.getString(context.getResources().getString(R.string.preference_password), "");
        // Check if user is logged in
        if (user.length() == 0 && password.length() == 0) {
            // Return failure if not logged in
            return Result.failure();
        }
        //Only check if wifi is enabled or we are allowed to check over cellular
        if (getWifiManager(context).isWifiEnabled() ||
                getSharedPreferences(context).getBoolean(context.getResources().getString(R.string.preference_use_mobile), true)) {
            new RefreshService(context).refresh();
            return Result.success();

        } else {
            //Wifi is off or we are not allowed to use cellular. Set the overdue flag to signalised the upate is delayed and retry later
            getSharedPreferences(context).edit().putBoolean(context.getString(R.string.preference_refresh_is_overdue), true).commit();
            return Result.retry();
        }
    }

    private static SharedPreferences getSharedPreferences(Context con) {
        return PreferenceManager.getDefaultSharedPreferences(con);
    }

    private WifiManager getWifiManager(Context con) {
        return (WifiManager) con.getSystemService(Context.WIFI_SERVICE);
    }
}
