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
 * A login screen that offers login via username/password and user registration.
 * After login, the screen will start the MainActivity
 * @version 1.0
 * @author Philipp Bahnmueller
 * @author Felix Rosa
 * @author Kevin Schroetter
 */
public class LoginActivity extends AppCompatActivity {
    /**
     * Global Server address used for all API calls within the whole project
     * Change the value is you want to use another server where the API is running
     */
    public static String URL = "https://friendstrackerworldwide-api.mybluemix.net";

    /** Used for sending the username to the MainActivity */
    public static final String EXTRA_MESSAGE = "com.osmap.pbfrks.friendstrackerworldwide.MESSAGE";
    /** Used for doing the RESTFUL API requests */
    private ApiCaller apiCaller;
    /** Required for performing the user login */
    private UserLoginTask myAuthTask = null;
    /** Reqired for performing the user registration */
    private UserRegTask myRegTask = null;

    /** UI references */
    private AutoCompleteTextView myUsernameView;
    private EditText myPasswordView;
    private View myProgressView;
    private View myLoginFormView;
    private Button closeButton;
    private Button loginButton;
    private Button registerButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        super.onCreate(savedInstanceState);
        // Set view to activity_login.xml
        setContentView(R.layout.activity_login);
        apiCaller = new ApiCaller();
        myUsernameView = (AutoCompleteTextView) findViewById(R.id.login_username);

        myPasswordView = (EditText) findViewById(R.id.login_password);
        // ActionListener used for reacting on a press on "enter"
        myPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login_button || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
        // Allocating of buttons to related view items
        loginButton = (Button) findViewById(R.id.login_button);
        closeButton = (Button) findViewById(R.id.closeButton);
        registerButton = (Button) findViewById(R.id.register_button);
        // Performing login on click of the login button
        loginButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });
        // Closing the app when clicked on close button
        closeButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                System.exit(0);
            }
        });
        // Performing registration when registration button is clicked
        registerButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptRegistration();
            }
        });
        // Allocating login form and login progress information from activity_login.xml to corresponding ui elements
        myLoginFormView = findViewById(R.id.login_form);
        myProgressView = findViewById(R.id.login_progress);
    }
    /**
     * Attempts to login to the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (myAuthTask != null) {
            return;
        }
        // Reset errors
        myUsernameView.setError(null);
        myPasswordView.setError(null);

        // Store values from the username and password field at the time of the login attempt
        String username = myUsernameView.getText().toString();
        String password = myPasswordView.getText().toString();
        boolean cancel = false;
        View focusView = null;
        // Check for a valid password
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            myPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = myPasswordView;
            cancel = true;
        }
        // Check for a valid username
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
            // There was an error; don't attempt login and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner and start a background task when attempting to login
            showProgress(true);
            myAuthTask = new UserLoginTask(username, password);
            myAuthTask.execute((Void) null);
        }
    }
    /**
     * Attempts to register the account specified by the login form.
     * If there are form errors (invalid username, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptRegistration() {
        if (myRegTask != null) {
            return;
        }
        // Reset errors
        myUsernameView.setError(null);
        myPasswordView.setError(null);

        // Store values at the time of the registration attempt
        String username = myUsernameView.getText().toString();
        String password = myPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            myPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = myPasswordView;
            cancel = true;
        }

        // Check for a valid username
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
            // There was an error; don't attempt registration and focus the first form field with an error
            focusView.requestFocus();
        } else {
            // Show a progress spinner for registration and start the background process
            showProgress(true);
            myRegTask = new UserRegTask(username, password);
            myRegTask.execute((Void) null);
        }
    }

    /**
     * Method used for validating a username
     * @param username - The username that must be checked
     * @return true or false depending in whether or not the validation is okay
     */
    private boolean isUsernameValid(String username) {
        return username.length()>=3;
    }

    /**
     * Method used for validating a username
     * @param password - The password that must be checked
     * @return true or false depending in whether or not the validation is okay
     */
    private boolean isPasswordValid(String password) {
        return password.length() >= 3;
    }

    /**
     * Shows the progress UI and hides the login form.
     * Requires build version honeycomb for showing the progess correctly
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        /*
         * On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
         * for very easy animations. If available, use these APIs to fade-in
         * the progress spinner.
         */
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
            /**
             * Hiding the form when build version is not honeycomb_mr2
             */
            myProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            myLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    /**
     * Represents an asynchronous task used to authenticate the user in the login process calling the FTW API
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the variables used for the login
        private final String myUsername;
        private final String myPassword;

        UserLoginTask(String username, String password) {
            myUsername = username;
            myPassword = password;
            myUsernameView.setText(myUsername);
        }

        /**
         * Method used for performing a background task that sends information from the Application to the API for login
         * Performing a user login
         * @param params - a username and password that will be sent to the API
         * @return true or false depending on reaching the API through the internet
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject loginParams = new JSONObject();
            // Adding required variables to the JSONObject array
            try {
                loginParams.put("username", myUsername);
                loginParams.put("password", myPassword);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for login
            String apiUrl = LoginActivity.URL+"/api/user/loginUser";
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a POST request to the apiUrl destination
            resultObj = apiCaller.executePost(apiUrl,loginParams.toString());
            // Handling the results returned by the FTW API - either "success" or "err"
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

        /**
         * Method called after the background task for further execution of the application
         * Starting the MainActivity when successfull and displaying an error when not
         * @param success - return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myAuthTask = null;
            showProgress(false);

            if (success) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra(EXTRA_MESSAGE,myUsername);
                startActivity(intent);
            } else {
                myUsernameView.setError(getString(R.string.error_false_login)+"Username: "+(myUsername.equals("Kevin")));
                myUsernameView.requestFocus();
            }
        }

        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            myAuthTask = null;
            showProgress(false);
        }
    }

    /**
     * Represents an asynchronous task for user registration process calling the FTW API
     */
    public class UserRegTask extends AsyncTask<Void, Void, Boolean> {
        // Defining the variables used for the registration
        private final String myUsername;
        private final String myPassword;

        UserRegTask(String username, String password) {
            myUsername = username;
            myPassword = password;
            myUsernameView.setText(myUsername);
        }

        /**
         * Method used for performing a background task that sends information from the Application to the API for registration
         * @param params
         * @return
         */
        @Override
        protected Boolean doInBackground(Void... params) {
            // Defining a JSONObject array used for the API call, since the api uses JSONObjects for handling requests
            JSONObject loginParams = new JSONObject();
            // Adding required variables to the JSONObject array
            try {
                loginParams.put("username", myUsername);
                loginParams.put("password", myPassword);
                loginParams.put("latitude", 48.482918);
                loginParams.put("longitude", 9.188356);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            // Defining the API route called for registration
            String apiUrl = LoginActivity.URL+"/api/user/addUser";
            JSONObject resultObj = new JSONObject();
            // Using an ApiCaller for executing a POST request to the apiUrl destination
            resultObj = apiCaller.executePost(apiUrl,loginParams.toString());
            // Handling the results returned by the FTW API - either "success" or "err"
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
            return false;
        }

        /**
         * Method called after the background task for further execution of the application
         * Confirming registration when successfull and displaying an error when not
         * @param success - return statement from doInBackground()
         */
        @Override
        protected void onPostExecute(final Boolean success) {
            myRegTask = null;
            showProgress(false);

            if (success) {
                Toast.makeText(LoginActivity.this, "Registration complete for user: "+myUsername, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(LoginActivity.this, "Registration Failed", Toast.LENGTH_SHORT).show();
                myUsernameView.setError(getString(R.string.error_false_registration)+": "+(myUsername));
                myUsernameView.requestFocus();
            }
        }
        /**
         * Handling of cancelling the Task
         */
        @Override
        protected void onCancelled() {
            myRegTask = null;
            showProgress(false);
        }
    }
}

