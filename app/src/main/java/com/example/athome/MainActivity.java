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

//public class MainActivity extends AppCompatActivity {
    public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<String>
{
    // Debug logging Tag
    public static final String LOG_TAG = MainActivity.class.getName();

    public static final int LOADER_OPERATION_HTTP_REQUEST = 22;

    // Preference Constants
    public static final String PREFERENCES_FILE_NAME = "ATHomePreferences";

    // Preference Names and default Values
    public static final String PREFERENCES_TitleBarName_DEFAULT_VALUE = "AtHome";
    public static final String PREFERENCES_TitleBarName_ID = "TitleBarName";

    // Preference Values
    private String mTitleBarName = PREFERENCES_TitleBarName_DEFAULT_VALUE;

    // Global Variable
    Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(LOG_TAG, ":OnCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Restore Application Preferences from File
        restorePreferences();

        // Set the Toolbar Title
        Toolbar mActionBarToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mActionBarToolbar);
        getSupportActionBar().setTitle(mTitleBarName);

        // Initialize Background loader
        getLoaderManager().initLoader(LOADER_OPERATION_HTTP_REQUEST,null,this);

        // display a request in progress message
        Context context = getApplicationContext();
        mToast = Toast.makeText(context, "URL Request in progress", Toast.LENGTH_LONG);
        mToast.show();

        }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        Log.i(LOG_TAG, ":onCreateOptionsMenu");

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(LOG_TAG, ":onOptionsItemSelected");

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // restorePreferences loads saved application preferences from a file
    private void restorePreferences() {
        Log.i(LOG_TAG, ":restorePreferences");

        // Restore preferences
        SharedPreferences sharedPrefs = getSharedPreferences(PREFERENCES_FILE_NAME, MODE_PRIVATE);
        mTitleBarName = sharedPrefs.getString(PREFERENCES_TitleBarName_ID, PREFERENCES_TitleBarName_DEFAULT_VALUE);
    }

    protected void onStop() {
        Log.i(LOG_TAG, ":restorePreferences");

        // Execute the supper class tidy up
        super.onStop();

        // Save preferences
        savePreferences();
    }


    // savePreferences stores application preferences into a preference file
    private void savePreferences() {
        Log.i(LOG_TAG, ":savePreferences");

        // Save preferences
        // We need an Editor object to make preference changes.
        SharedPreferences sharedPrefs = getSharedPreferences(PREFERENCES_FILE_NAME, MODE_PRIVATE);
        SharedPreferences.Editor sharedPrefsEditor = sharedPrefs.edit();

        sharedPrefsEditor.putString(PREFERENCES_TitleBarName_ID, mTitleBarName);

        // commit the changes
        sharedPrefsEditor.commit();

    }

        @Override
        public Loader<String> onCreateLoader( int id, Bundle args ) {
            Log.i(LOG_TAG, ":onCreateLoader");

            // Kick off an AsyncTask to perform the network request
            return new HTTPRequestBackgroundLoaderTask(this);
        }

        @Override
        public void onLoadFinished(Loader<String> loader, String data) {
            Log.i(LOG_TAG, ":onLoadFinished");

            // Process HTTP Response Data
            ProcessHTTPResponseData(data);

        }

        @Override
        public void onLoaderReset(Loader<String> loader) {
            Log.i(LOG_TAG, ":onLoaderReset");

        }
    // the ProcessHTTPResponseData metthod is executed by the background thread once the
    // thread has completed its processing
    private void ProcessHTTPResponseData(String sHTTPResponseData) {
        Log.i(LOG_TAG, ":ProcessHTTPResponseData");

        Context context = getApplicationContext();

        if (sHTTPResponseData.isEmpty()) {
            // HTTP Request was not successful

            // display a request ERROR
            mToast = mToast.makeText(context, "Earthquake Request Failed to retrieve data", Toast.LENGTH_LONG);
            mToast.show();
        }
        else {
            // Clear Toast in progress
            mToast.cancel();
        }
    }

}
