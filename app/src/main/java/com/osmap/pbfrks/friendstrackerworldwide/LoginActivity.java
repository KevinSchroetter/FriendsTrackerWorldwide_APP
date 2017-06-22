package com.osmap.pbfrks.friendstrackerworldwide;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.StrictMode;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.TextUtils;
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

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A login screen that offers login via username/password.
 */
public class LoginActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    private ApiCaller apiCaller;
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask myAuthTask = null;
    private UserRegTask myRegTask = null;

    // UI references.
    private AutoCompleteTextView myUsernameView;
    private EditText myPasswordView;
    private View myProgressView;
    private View myLoginFormView;
    private TextView myApiResultView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        apiCaller = new ApiCaller();
        // Set up the login form.
        myApiResultView = (TextView) findViewById(R.id.apiResult);
        myUsernameView = (AutoCompleteTextView) findViewById(R.id.login_username);

        myPasswordView = (EditText) findViewById(R.id.login_password);
        myPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button loginButton = (Button) findViewById(R.id.login_button);
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        Button registerButton = (Button) findViewById(R.id.register_button);
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });

        myLoginFormView = findViewById(R.id.login_form);
        myProgressView = findViewById(R.id.login_progress);
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (myAuthTask != null) {
            return;
        }

        // Reset errors.
        myUsernameView.setError(null);
        myPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = myUsernameView.getText().toString();
        String password = myPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            myPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = myPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            myUsernameView.setError(getString(R.string.error_field_required));
            focusView = myUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            myUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = myUsernameView;
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
            myAuthTask = new UserLoginTask(username, password);
            myAuthTask.execute((Void) null);

        }
    }
    private void attemptRegistration() {
        if (myRegTask != null) {
            return;
        }

        // Reset errors.
        myUsernameView.setError(null);
        myPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String username = myUsernameView.getText().toString();
        String password = myPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            myPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = myPasswordView;
            cancel = true;
        }

        // Check for a valid username.
        if (TextUtils.isEmpty(username)) {
            myUsernameView.setError(getString(R.string.error_field_required));
            focusView = myUsernameView;
            cancel = true;
        } else if (!isUsernameValid(username)) {
            myUsernameView.setError(getString(R.string.error_invalid_username));
            focusView = myUsernameView;
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
            myRegTask = new UserRegTask(username, password);
            myRegTask.execute((Void) null);

        }
    }

    private boolean isUsernameValid(String username) {
        //TODO: Replace this with your own logic
        return username.length()>=3;
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() >= 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            myLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            myProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }


    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final String myPassword;

        UserLoginTask(String username, String password) {
            myUsername = username;
            myPassword = password;
            myUsernameView.setText(myUsername);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            JSONObject loginParams = new JSONObject();
            try {
                loginParams.put("username", myUsername);
                loginParams.put("password", myPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/loginUser";
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executePost(apiUrl,loginParams.toString());


            try {
                if(resultObj.get("success")!=null){
                    return true;
                }
                else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myAuthTask = null;
            showProgress(false);

            if (success) {
               Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_MESSAGE,myUsername);
                startActivity(intent);
                //finish();
            } else {
                myUsernameView.setError(getString(R.string.error_false_login)+"Username: "+(myUsername.equals("Kevin")));
                myUsernameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            myAuthTask = null;
            showProgress(false);
        }
    }
    public class UserRegTask extends AsyncTask<Void, Void, Boolean> {

        private final String myUsername;
        private final String myPassword;

        UserRegTask(String username, String password) {
            myUsername = username;
            myPassword = password;
            myUsernameView.setText(myUsername);
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            JSONObject loginParams = new JSONObject();
            try {
                loginParams.put("username", myUsername);
                loginParams.put("password", myPassword);
                loginParams.put("latitude", 48.482918);
                loginParams.put("longitude", 9.188356);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            String apiUrl = "https://friendstrackerworldwide-api.mybluemix.net/api/user/addUser";
            JSONObject resultObj = new JSONObject();
            resultObj = apiCaller.executePost(apiUrl,loginParams.toString());


            try {
                if(resultObj.get("message")!=null){
                    return true;
                }
                else{
                    return false;
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }

            //API CALL HERE
            return false;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            myRegTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(LoginActivity.this, "Registration complete for user: "+myUsername, Toast.LENGTH_SHORT).show();


                //finish();
            } else {
                Toast.makeText(LoginActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                myUsernameView.setError(getString(R.string.error_false_registration)+": "+(myUsername));
                myUsernameView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            myRegTask = null;
            showProgress(false);
        }
    }
}

