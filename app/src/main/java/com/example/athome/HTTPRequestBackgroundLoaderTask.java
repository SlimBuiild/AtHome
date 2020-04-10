package com.example.athome;

import android.app.LoaderManager;
import android.content.Context;
import android.content.Loader;
import android.content.SharedPreferences;
import android.os.Bundle;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.util.Log;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.Charset;
import android.content.AsyncTaskLoader;
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

import javax.net.ssl.HttpsURLConnection;

import static android.content.Context.MODE_PRIVATE;

// Background Task that performs HTTP requests without hanging up the UI
public class HTTPRequestBackgroundLoaderTask extends AsyncTaskLoader<String> {
    // Debug logging Tag
    public static final String LOG_TAG = HTTPRequestBackgroundLoaderTask.class.getName();

    private static final String ALPHA_ESS_LOGON_REQUEST_URL = "https://www.alphaess.com/Account/Logon";
    private static final String ALPHA_ESS_POWER_DETAILS_REQUEST_URL = "https://www.alphaess.com/Monitoring/VtSystem/VtSystemIndexForCustomer";

    private String mHTTPResponseData = "";
    private PackageManager mPackageManager = null;
    private Context mContext;



    public HTTPRequestBackgroundLoaderTask(Context context) {

        super(context);

        // Save Context
        mContext = context;

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

        String loginData = "ReturnUrl=&Username=AndyRutherford&Userpwd=746f646f%40Slimbuild416c";
        mHTTPResponseData = POST_req(ALPHA_ESS_LOGON_REQUEST_URL, loginData, 10000); /*last parameter is a limit of page content length*/
        mHTTPResponseData = POST_req(ALPHA_ESS_LOGON_REQUEST_URL, loginData, 10000); /*last parameter is a limit of page content length*/

        // And after succcess login you can send second request:
        mHTTPResponseData = POST_req(ALPHA_ESS_POWER_DETAILS_REQUEST_URL, "", 10000);

        // Create URL object
        URL url = null;
        // url = createUrl(ALPHA_ESS_LOGON_REQUEST_URL);

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

    //Methods for sending requests and saving cookie:
    //(this no needs for changing, can only past to you project)
    public String POST_req(String url, String post_data, int maxRespLen) {
        URL addr = null;
        try {
            addr = new URL(url);
        } catch (MalformedURLException e) {
            return "Некорректный URL";
        }
        StringBuffer data = new StringBuffer();
        HttpsURLConnection conn = null;
        try {
            conn = (HttpsURLConnection) addr.openConnection();
        } catch (IOException e) {
            return "Open connection error";
        }
        conn.setRequestProperty("Connection", "keep-alive");
        conn.setRequestProperty("Accept-Language", "ru,en-GB;q=0.8,en;q=0.6");
        conn.setRequestProperty("Accept-Charset", "utf-8");
        conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
        conn.setRequestProperty("Cookie", "");
        conn.setDoOutput(true);
        conn.setDoInput(true);
        //conn.setInstanceFollowRedirects(true);
        set_cookie(conn);

        //POST data:
        String post_str = post_data;
        data.append(post_str);
        try {
            conn.connect();
        } catch (IOException e) {
            return "Connecting error";
        }
        DataOutputStream dataOS = null;
        try {
            dataOS = new DataOutputStream(conn.getOutputStream());
        } catch (IOException e2) {
            return "Out stream error";
        }
        try {
            ((DataOutputStream) dataOS).writeBytes(data.toString());
        } catch (IOException e) {
            return "Out stream error 1";
        }

        /*If redirect: */
        int status;
        try {
            status = conn.getResponseCode();
        } catch (IOException e2) {
            return "Response error";
        }
        if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM || status == HttpURLConnection.HTTP_SEE_OTHER) {
            String new_url = conn.getHeaderField("Location");
            String cookies = conn.getHeaderField("Set-Cookie");
            URL red_url;
            try {
                red_url = new URL(new_url);
            } catch (MalformedURLException e) {
                return "Redirect error";
            }
            try {
                conn = (HttpsURLConnection) red_url.openConnection();
            } catch (IOException e) {
                return "Redirect connection error";
            }
            //conn.setRequestProperty("Content-type", "text/html");
            conn.setRequestProperty("Connection", "keep-alive");
            conn.setRequestProperty("Accept-Language", "ru,en-GB;q=0.8,en;q=0.6");
            conn.setRequestProperty("Accept-Charset", "utf-8");
            conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
            conn.setRequestProperty("Cookie", cookies);
            conn.setDoOutput(true);
            conn.setDoInput(true);
            //conn.setInstanceFollowRedirects(true);
        }

        java.io.InputStream in = null;
        try {
            in = (java.io.InputStream) conn.getInputStream();
        } catch (IOException e) {
            return "In stream error";
        }
        InputStreamReader reader = null;
        try {
            reader = new InputStreamReader(in, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return "In stream error";
        }
        char[] buf = new char[maxRespLen];
        try {
            reader.read(buf);
        } catch (IOException e) {
            return "In stream error";
        }
        get_cookie(conn);
        httpViewHeader(conn);
        return (new String(buf));
    }

    private void httpViewHeader(HttpURLConnection conn) {
        Log.i(LOG_TAG, ":httpViewHeader");

        String HeaderFieldValue;
        String HeaderFieldKey;
        int x = 0;

        HeaderFieldKey  = conn.getHeaderFieldKey(x);
        HeaderFieldValue = conn.getHeaderField(x);

        while ( HeaderFieldValue != null  ) {
            if ( HeaderFieldValue != null & HeaderFieldKey != null ) {
                Log.i(LOG_TAG, ":httpViewHeader - Key "+ HeaderFieldKey + " " +  HeaderFieldValue );
            }
            x++;
            HeaderFieldKey  = conn.getHeaderFieldKey(x);
            HeaderFieldValue = conn.getHeaderField(x);

        }
    }


    private void get_cookie(HttpURLConnection conn) {

        // Preference Constants
        final String COOKIES_FILE_NAME = "cookies";

        SharedPreferences sh_pref_cookie = mContext.getSharedPreferences(COOKIES_FILE_NAME, MODE_PRIVATE);
        String cook_new;
        String COOKIES_HEADER;
        if (conn.getHeaderField("Set-Cookie") != null) {
            COOKIES_HEADER = "Set-Cookie";
        }
        else {
            COOKIES_HEADER = "Cookie";
        }
        cook_new = conn.getHeaderField(COOKIES_HEADER);
        if (cook_new.indexOf("sid", 0) >= 0) {
            SharedPreferences.Editor editor = sh_pref_cookie.edit();
            editor.putString("Cookie", cook_new);
            editor.commit();
        }
    }
    public void set_cookie(HttpURLConnection conn) {
        SharedPreferences sh_pref_cookie = mContext.getSharedPreferences("cookies", MODE_PRIVATE);
        String COOKIES_HEADER = "Cookie";
        String cook = sh_pref_cookie.getString(COOKIES_HEADER, "no_cookie");
        if (!cook.equals("no_cookie")) {
            conn.setRequestProperty(COOKIES_HEADER, cook);
        }
    }
}
