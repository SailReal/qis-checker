package de.albsig.qischecker.network;

import android.content.Context;
import android.os.AsyncTask;

import de.albsig.qischecker.R;
import de.albsig.qischecker.view.DialogHost;

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
