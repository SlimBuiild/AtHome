package com.example.athome;

import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;


import android.content.AsyncTaskLoader;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

// Background Task that performs HTTP requests without hanging up the UI
public class HTTPRequestBackgroundLoaderTask extends AsyncTaskLoader<String> {

    private static final String ALPHA_ESS_LOGON_REQUEST_URL = "https://www.alphaess.com/Account/Logon";

    private String mHTTPResponseData = "";
    private PackageManager mPackageManager = null;

    public static final String LOG_TAG = HTTPRequestBackgroundLoaderTask.class.getName();


    public HTTPRequestBackgroundLoaderTask(Context context) {

        super(context);

        // Retrieve the package manager for later use; note we don't
        // use 'context' directly but instead the save global application
        // context returned by getContext().
        mPackageManager = getContext().getPackageManager();
    }

    @Override
    public void onStartLoading() {
        Log.i(LOG_TAG, ":onStartLoading");
        forceLoad();
    }

    @Override
    public String loadInBackground() {
        Log.i(LOG_TAG, ":loadInBackground");

        // initialize response buffer empty
        mHTTPResponseData = "";

        // Create URL object
        URL url = createUrl(ALPHA_ESS_LOGON_REQUEST_URL);
        if (url != null) {
            // Perform HTTP request to the URL and receive a JSON response back
            try {
                mHTTPResponseData = makeHttpRequest(url);
            } catch (IOException e) {
                // TODO Handle the IOException
            }
        }
        // Note the response to the HttpRequest will be handled in updateUi method
        // which will be called by the bacground thread that initiates the request
        return mHTTPResponseData;
    }

    private void onLoadFinished() {
        Log.i(LOG_TAG, ":onLoadFinished");
        // Finalize
    }

    private URL createUrl(String stringUrl) {
        URL url = null;
        try {
            url = new URL(stringUrl);
        } catch (MalformedURLException exception) {
            Log.e(LOG_TAG, "Error with creating URL - Malformed URL", exception);
            return null;
        }
        return url;
    }

    /**
     * Make an HTTP request to the given URL and return a String as the response.
     */
    private String makeHttpRequest(URL url) throws IOException {
        String HTTPResponseData = "";
        HttpURLConnection urlConnection = null;
        InputStream inputStream = null;
        Log.i(LOG_TAG, ":makeHttpRequest ");
        try {
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setReadTimeout(10000 /* milliseconds */);
            urlConnection.setConnectTimeout(15000 /* milliseconds */);
            urlConnection.connect();
            // check Respomse code
            if (urlConnection.getResponseCode() == 200) {
                inputStream = urlConnection.getInputStream();
                HTTPResponseData = readFromStream(inputStream);
                Log.i(LOG_TAG, "makeHttpRequest: HTTPResponseData size: " + String.format("%d", HTTPResponseData.length()));
            } else {
                Log.e(LOG_TAG, "makeHttpRequest: HTTP Error response code: " + String.format("%d", urlConnection.getResponseCode()));
            }
        } catch (IOException e) {
            // TODO: Handle the exception
            Log.e(LOG_TAG, "makeHttpRequest IOException", e);

        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (inputStream != null) {
                // function must handle java.io.IOException here
                inputStream.close();
            }
        }
        return HTTPResponseData;
    }

    /**
     * Convert the {@link InputStream} into a String which contains the
     * whole JSON response from the server.
     */
    private String readFromStream(InputStream inputStream) throws IOException {
        StringBuilder output = new StringBuilder();
        if (inputStream != null) {
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(inputStreamReader);
            String line = reader.readLine();
            while (line != null) {
                output.append(line);
                line = reader.readLine();
            }
        }
        return output.toString();
    }

}
