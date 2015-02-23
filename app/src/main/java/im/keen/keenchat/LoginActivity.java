package im.keen.keenchat;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends Activity {

    //Keys for retrieving objects from intents by other activities
    public final static String event_id_hash = "im.keen.keenchat.chat.event_id.key"; //Currently the channels are the events
    public final static String user_id_hash = "im.keen.keenchat.chat.user_id_hash.key";
    public final static String auth_hash= "im.keen.keenchat.chat.auth_hash.key";
    public final static String user_name_hash = "im.keen.keenchat.chat.user_name.key";
    //We're going to cheat a little and let get the name of the user
    //from the login, before the user list is actually loaded from the web.

    public static final String TAG = "Login Screen";

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;
    Gson mGson = new GsonBuilder().create();


    // UI references.
    private AutoCompleteTextView mEmailView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.sign_in_from_pw || id == EditorInfo.IME_NULL) {
                    attemptLogin();

                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    public void attemptLogin() {
        Log.d(TAG,"Login attempted");
        if (mAuthTask != null) {
            Log.d(TAG,"Login already in progress. aborted additional attempt");
            return;
        }

        // Reset errors.
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;


        // Check for a valid password, if the user entered one.
        if (TextUtils.isEmpty(password) || !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid username
        if (TextUtils.isEmpty(username)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            //The params for the login are associated
            //With the instance of the task, not the execution.
            mAuthTask.execute();
        }
    }

    private boolean isPasswordValid(String password) {
        return (password.length() > 4);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    public void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }



    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, String> {

        private final String mUsername;
        private final String mPassword;
        Map<String,String> logon_map = new HashMap<String,String>();

        //This is a constructor for the task..? To avoid using the async task argument params.
        UserLoginTask(String username, String password) {
            mUsername = username;
            mPassword = password;

        }

        @Override
        protected String doInBackground(Void... params) {

            logon_map.put("username",mUsername);
            logon_map.put("password",mPassword);

            String logon_json = mGson.toJson(logon_map);
            try {
                Log.d(TAG,"Attempt logon as" + logon_json);
                return new DataFetch().getStringFromUrl("http://chat.server.keen.im/auth","POST",logon_json);

            } catch (IOException ioe) {
                Log.d(TAG,"Logon attempt failed");
            }

            return null;
        }

        @Override
        protected void onPostExecute(final String s) {
            mAuthTask = null;
            showProgress(false);
            Log.d(TAG,"Server login response:" + s);
            try {
                JSONObject login_result = new JSONObject(s);//Read server response as JSON object
                String auth_token = login_result.getString("token_value");
                Log.d(TAG,"Token received: " + auth_token);
                int user_id = login_result.getInt("user_id");
                //Todo: Data loading should start here, to successfully load things before the next screen
                //Todo: Fix intents after done testing chats
                //Todo: Hide IME on login success.
                Intent i = new Intent(LoginActivity.this,EventActivity.class);
                //Intent i = new Intent(LoginActivity.this,ChatActivity.class);
                i.putExtra(auth_hash,auth_token);
                i.putExtra(user_id_hash,user_id);
                i.putExtra(user_name_hash,mUsername);

                startActivity(i);
            } catch (Exception e) {
                mPasswordView.setError(getString(R.string.error_invalid_details));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
            Toast.makeText(getBaseContext(),"Could not reach server. Check internet connectivity.",Toast.LENGTH_SHORT).show();
        }
    }


}



