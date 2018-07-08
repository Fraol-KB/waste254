package com.example.fra.waste254sp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * A login screen that offers login via email/password.
 */
@RequiresApi(api = Build.VERSION_CODES.HONEYCOMB)
public class LoginActivity extends AppCompatActivity implements AsyncResponse {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */

    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    //private UserLoginTask mAuthTask = null;


    // UI references.
    private EditText mEmailView;
    private EditText mPasswordView;
    //private View mProgressView;
    private View mLoginFormView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        setContentView(R.layout.activity_login);


        // Set up the login form.
        mEmailView = (EditText) findViewById(R.id.email);
        // populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == R.id.login || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });


        //mProgressView = findViewById(R.id.login_progress);
    }




    /**
     * Callback received when a permissions request has been completed.
     */

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {

            EditText username = (EditText) findViewById(R.id.email);
            String uname = username.getText().toString();

            EditText pass = (EditText) findViewById(R.id.password);
            String passw = pass.getText().toString();

            PostResponseAsyncTask task;
            HashMap<String, String> post = new HashMap<String, String>();
            post.put("email", uname);
            post.put("password", password);
            task = new PostResponseAsyncTask(LoginActivity.this, post, LoginActivity.this);
            task.execute(getString(R.string.loginsp));

        }
    }


    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */



    @Override
    public void processFinish(String response) {

        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean("success");

            if (success) {
                //Toast.makeText(this,"success",Toast.LENGTH_LONG).show();

                int userid = jsonResponse.getInt("userid");
                //String name = jsonResponse.getString("name");
                String email = jsonResponse.getString("email");

                SharedPreferences prefs = getSharedPreferences("Waste254sp", Context.MODE_WORLD_WRITEABLE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("firstTime", false);
                editor.putInt("userid", userid);
                editor.putString("email", email);
                editor.commit();

                Intent i =new Intent();
                setResult(RESULT_OK,i);
                finish();

            } else {
                Toast.makeText(this, "Wrong email or password", Toast.LENGTH_SHORT).show();
                mEmailView.setError(getString(R.string.error_field_required));

            }

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}

