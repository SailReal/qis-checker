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

public class RefreshService {

    private final String URL_LOGIN = "https://qis.hs-albsig.de/qisserver/rds?state=user&type=1&category=auth.login&startpage=portal.vm";
    private final String URL_LOGOUT = "https://qis.hs-albsig.de/qisserver/rds?state=user&type=4&re=last&category=auth.logout&breadCrumbSource=portal&topitem=functions";
    private final String URL_FETCH_ASI = "https://qis.hs-albsig.de/qisserver/rds?state=change&type=1&moduleParameter=studyPOSMenu&nextdir=change&next=menu.vm&subdir=applications&xml=menu&purge=y&navigationPosition=functions%2CstudyPOSMenu&breadcrumb=studyPOSMenu&topitem=functions&subitem=studyPOSMenu";
    private final String URL_FETCH_NODE = "https://qis.hs-albsig.de/qisserver/rds?state=notenspiegelStudent&next=tree.vm&nextdir=qispos/notenspiegel/student&menuid=notenspiegelStudent&breadcrumb=notenspiegel&breadCrumbSource=menu&asi=%s";
    private final String URL_OBSERVE = "https://qis.hs-albsig.de/qisserver/rds?state=notenspiegelStudent&next=list.vm&nextdir=qispos/notenspiegel/student&createInfos=Y&struct=auswahlBaum&nodeID=%s&expand=0&asi=%s";
    private final String CHARSET = java.nio.charset.StandardCharsets.UTF_8.name();

    private final String USER_NAME;
    private final String PASSWORD;
    private final Context CONTEXT;

    private final Pattern pattern = Pattern.compile("<a name=\"(.*)\"></a>");

    RefreshService(Context c, String userName, String password) {
        this.CONTEXT = c;
        this.USER_NAME = userName;
        this.PASSWORD = password;

        initCookieHandler();
    }

    RefreshService(Context c) {
        this.CONTEXT = c;

        SharedPreferences sp = this.getSharedPreferences();
        this.USER_NAME = sp.getString(this.getStringResource(R.string.preference_user), "");
        this.PASSWORD = sp.getString(this.getStringResource(R.string.preference_password), "");

        initCookieHandler();
    }

    private void initCookieHandler() {
        CookieManager cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);
    }

    Exception refresh() {
        //Declare occuredException
        Exception occuredException = null;

        //Try 3 times
        for (int i = 0; i < 3; i++) {
            try {
                //Login
                this.showProgressDialog(this.getStringResource(R.string.text_logging_in));
                Log.i(this.getClass().getSimpleName(), this.getStringResource(R.string.text_logging_in));
                this.login();

                // Fetch asi
                String asi = this.getAsi();

                // Get nodeIDs of current and past degree
                List<String> nodeID = this.getNode(asi);

                //Check for change
                this.showProgressDialog(this.getStringResource(R.string.text_checking_update));
                Log.i(this.getClass().getSimpleName(), this.getStringResource(R.string.text_checking_update));
                boolean changed = this.checkDataChange(asi, nodeID);

                //If no change -> save a NoChnageException in occuredException
                if (!changed) {
                    occuredException = new NoChangeException();

                }

                //Update last_check
                getSharedPreferences().edit().putLong(getStringResource(R.string.preference_last_check), System.currentTimeMillis()).apply();

                //No error -> cancel (no further trys)
                break;

            } catch (Exception e) {
                //Something went wrong. Print stack trace and save
                e.printStackTrace();
                occuredException = e;

                //Try again, but show error
                this.showProgressDialog(getStringResource(R.string.exception_general));
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                }

            } finally {
                //try to Log out
                try {
                    this.showProgressDialog(this.getStringResource(R.string.text_logging_out));
                    Log.i(this.getClass().getSimpleName(), this.getStringResource(R.string.text_logging_out));
                    this.logout();

                } catch (Exception e) {
                    e.printStackTrace();

                }
            }
        }

        //Return exception (should be null or NoChangeException)
        return occuredException;
    }

    private void login() throws Exception {

        //Check Preference
        if (this.PASSWORD.length() == 0 || this.USER_NAME.length() == 0)
            throw new LoginException(this.getStringResource(R.string.exception_no_user_password));

        List<Pair<String, String>> nameValuePairs = new ArrayList<>(2);
        nameValuePairs.add(new Pair<>("asdf", this.USER_NAME));
        nameValuePairs.add(new Pair<>("fdsa", this.PASSWORD));
        nameValuePairs.add(new Pair<>("submit", "Anmelden"));

        //Load page (aka log in)
        String response = this.sendPost(this.URL_LOGIN, nameValuePairs);

        //Login failed
        if (response.contains("Anmeldung fehlgeschlagen")) {
            Log.i(this.getClass().getSimpleName(), "Login failed.");

            throw new LoginException(getStringResource(R.string.exception_wrong_user_password));

        }
    }

    private String getAsi() throws Exception {
        //Load page (aka log in)
        String response = this.sendPost(this.URL_FETCH_ASI, null);

        //Find asi
        int asiLength = ";asi=".length();
        int start = response.indexOf(";asi=") + asiLength;
        int end = response.indexOf("\"", start);

        if (start == asiLength) {
            throw new IOException("asi could not be extracted");
        }

        return response.substring(start, end);

    }

    private List<String> getNode(String asi) throws Exception {
        //Find Node IDs
        String response = this.sendPost(String.format(this.URL_FETCH_NODE, asi), null);
        int start = response.indexOf("<ul class=\"treelist\">");
        int end = response.indexOf("</ul>", start);
        String list = response.substring(start, end);

        List<String> nodes = new ArrayList<>();
        Matcher matcher = pattern.matcher(list);
        while (matcher.find()) {
            nodes.add(matcher.group(1));
        }

        return nodes;

    }


    private boolean checkDataChange(String asi, List<String> nodes) throws Exception {
        StringBuilder table = new StringBuilder();
        for (String node : nodes) {
            String response = this.sendGet(String.format(this.URL_OBSERVE, node, asi));
            int start = response.indexOf("<table border=\"0\">");
            int end = response.indexOf("</table>", start);
            table.append(response.substring(start, end));
        }

        //Create StudiportalData, Compare to saved one and save the new one
        StudiportalData sd = new StudiportalData(table.toString());
        List<Exam> changed = sd.findChangedExams(this.getSharedPreferences(), getStringResource(R.string.preference_last_studiportal_data));
        sd.save(this.getSharedPreferences(), getStringResource(R.string.preference_last_studiportal_data));

        //Compare
        boolean isChanged = changed.size() > 0;
        if (isChanged) {
            this.notifyAboutChange(changed);
        }

        return isChanged;
    }

    private void logout() throws Exception {
        this.sendGet(this.URL_LOGOUT);

    }

    private SharedPreferences getSharedPreferences() {
        return PreferenceManager.getDefaultSharedPreferences(this.getContext());
    }

    Context getContext() {
        return this.CONTEXT;

    }

    String getStringResource(int id) {
        return this.getContext().getResources().getString(id);

    }

    private String sendPost(String url, List<Pair<String, String>> params) throws Exception {
        // Open a new connection
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        // Trigger POST and set charset
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Accept-Charset", CHARSET);
        connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded;charset=" + CHARSET);

        if (params != null && !params.isEmpty()) {
            // Build URI with parameters
            Uri.Builder builder = new Uri.Builder();
            for (Pair<String, String> pair : params) {
                builder.appendQueryParameter(pair.first, pair.second);
            }
            String query = builder.build().getEncodedQuery();

            // Write query
            try (OutputStream output = connection.getOutputStream()) {
                output.write(query.getBytes(CHARSET));
                BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(output, CHARSET));
                writer.write(query);
                writer.flush();
                writer.close();
            }
        }

        // Get response
        String ret;
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response).useDelimiter("\\A")) {
            ret = scanner.hasNext() ? scanner.next() : "";
        }
        connection.disconnect();
        return ret;
    }

    private String sendGet(String url) throws Exception {
        // Open a new connection
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();

        connection.setRequestMethod("GET");

        // Set accepted charset
        connection.setRequestProperty("Accept-Charset", CHARSET);

        connection.connect();
        // Get response
        String ret;
        InputStream response = connection.getInputStream();
        try (Scanner scanner = new Scanner(response).useDelimiter("\\A")) {
            ret = scanner.hasNext() ? scanner.next() : "";
        }
        connection.disconnect();
        return ret;
    }

    private void showNotification(String title, String text, int id, Intent resultIntent) {
        NotificationCompat.Builder mBuilder =
                new NotificationCompat.Builder(this.getContext())
                        .setSmallIcon(R.drawable.albsig_logo_white)
                        .setContentTitle(title)
                        .setContentText(text);

        // The stack builder object will contain an artificial back stack for the
        // started Activity.
        // This ensures that navigating backward from the Activity leads out of
        // your application to the Home screen.

        PendingIntent pendingIntent = PendingIntent.getActivity(this.getContext(), id, resultIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        mBuilder.setContentIntent(pendingIntent);
        mBuilder.setAutoCancel(true);
        mBuilder.setVibrate(new long[]{1000, 1000, 1000, 1000, 1000});

        //LED
        mBuilder.setLights(Color.GREEN, 3000, 3000);

        //Ton
        mBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
        NotificationManager mNotificationManager =
                (NotificationManager) this.getContext().getSystemService(Context.NOTIFICATION_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = getStringResource(R.string.app_name);
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    getStringResource(R.string.app_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            mNotificationManager.createNotificationChannel(channel);
            mBuilder.setChannelId(channelId);
        }

        // mId allows you to update the notification later on.
        mNotificationManager.notify(id, mBuilder.build());

    }

    private void notifyAboutChange(List<Exam> changed) {
        boolean showGrade = this.getSharedPreferences().getBoolean(
                this.getStringResource(R.string.preference_show_grade_in_notification), true);
        boolean notifyAboutChange = this.getSharedPreferences().getBoolean(
                this.getStringResource(R.string.preference_notify_about_change), true);

        if (!notifyAboutChange)
            return;

        for (Exam e : changed) {
            Intent intent = new Intent(this.getContext(), ExamActivity.class);
            intent.putExtra(ExamActivity.ARG_EXAM, e);
            String subtitle = null;

            if (showGrade) {
                if (e.getGrade() != null && e.getGrade().length() > 0) {
                    subtitle = String.format("%s - %s", e.getGrade(), e.getState());

                } else {
                    subtitle = e.getState();

                }
            } else {
                subtitle = this.getStringResource(R.string.text_touch_to_show_grade);

            }

            this.showNotification(e.getName(), subtitle, e.getId(), intent);


        }
    }

    void notifyAboutError(Exception e) {
        this.showNotification(this.getStringResource(R.string.text_error), e.getMessage(), 1, new Intent(this.getContext(), MainActivity.class));

    }

    void showProgressDialog(String text) {
        if (this.CONTEXT instanceof DialogHost) {
            ((DialogHost) this.CONTEXT).showIndeterminateProgressDialog(this.getStringResource(R.string.text_refresh), text);

        }
    }
}
