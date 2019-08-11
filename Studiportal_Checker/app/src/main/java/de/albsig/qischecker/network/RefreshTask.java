package de.albsig.qischecker.network;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import android.util.Pair;

import androidx.core.app.NotificationCompat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import de.albsig.qischecker.R;
import de.albsig.qischecker.data.Exam;
import de.albsig.qischecker.data.StudiportalData;
import de.albsig.qischecker.view.DialogHost;
import de.albsig.qischecker.view.ExamActivity;
import de.albsig.qischecker.view.MainActivity;

public class RefreshTask extends AsyncTask<Void, Void, Exception> {

    private final RefreshService service;

    public RefreshTask(Context c, String userName, String password) {
        service = new RefreshService(c, userName, password);
    }

    public RefreshTask(Context c) {
        service = new RefreshService(c);
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

        service.showProgressDialog(service.getStringResource(R.string.text_connecting));

    }

    @Override
    protected Exception doInBackground(Void... params) {
        return service.refresh();
    }

    @Override
    protected void onPostExecute(Exception result) {
        super.onPostExecute(result);

        Context c = service.getContext();
        if (c instanceof DialogHost) {
            ((DialogHost) c).cancelProgressDialog();

            if (result != null) {
                ((DialogHost) c).showErrorDialog(result);

            }

        } else if (result != null && result instanceof LoginException) {
            service.notifyAboutError(result);

        }
    }
}
