package com.example.fra.waste254;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;

/**
 * Created by KidusMT on 12/29/2016.
 */

public class SignupActivity extends AppCompatActivity implements AsyncResponse {

    String binID="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "cancelled", Toast.LENGTH_SHORT).show();
            } else {

                binID=result.getContents();
                Toast.makeText(this, "Scanned: " + result.getContents(), Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }

    public void onClick(View view) {

        //Toast.makeText(this, "" + isConnected(), Toast.LENGTH_SHORT).show();
        IntentIntegrator scanIntegrator = new IntentIntegrator(this);
        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
        scanIntegrator.setPrompt("scan");
        scanIntegrator.setCameraId(0);
        scanIntegrator.setBeepEnabled(false);
        scanIntegrator.setBarcodeImageEnabled(false);
        scanIntegrator.initiateScan();

    }

    public void onSignUpClick(View v){


            EditText pass1 = (EditText) findViewById(R.id.signup_pass1);//first password
            EditText pass2 = (EditText) findViewById(R.id.signup_pass2);//second password


            String Fname = ((EditText) findViewById(R.id.signup_fname)).getText().toString();
            String Lname= ((EditText) findViewById(R.id.signup_lname)).getText().toString();
            String email = ((EditText) findViewById(R.id.signup_email)).getText().toString();
            String password= ((EditText) findViewById(R.id.signup_pass1)).getText().toString();
            String phoneNo = ((EditText) findViewById(R.id.signup_tele)).getText().toString();



            String password1 = pass1.getText().toString();
            String password2 = pass2.getText().toString();


            //If there is a mistake in confirming the password
            if(!password1.equals(password2))
            {
                //popup messge
                Toast.makeText(SignupActivity.this, "Password don't match", Toast.LENGTH_SHORT).show();
                System.out.println(password1);
                System.out.println(password2);
            }
            else
            {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
                if(!binID.isEmpty()) {
                    if (networkInfo != null && networkInfo.isConnected()) {
                    String bin=binID;
                    String name = Fname + " " + Lname;
                    PostResponseAsyncTask task;
                    HashMap<String, String> post = new HashMap<String, String>();
                    post.put("name", name);
                    post.put("binID", bin);
                    post.put("email", email);
                    post.put("password", password1);
                    post.put("phoneNo", phoneNo);

                    task = new PostResponseAsyncTask(SignupActivity.this, post, SignupActivity.this);
                   task.execute(getString(R.string.register));
                }else{
                    Toast.makeText(SignupActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
                }
                }else{
                    Toast.makeText(this,"please Scan ur BIN",Toast.LENGTH_LONG).show();
                }
            }
        }


    @Override
    public void processFinish(String response) {



        try {
            JSONObject jsonResponse = new JSONObject(response);
            boolean success = jsonResponse.getBoolean("success");

            if (success) {
                finish();
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(SignupActivity.this);
                builder.setMessage("Sign up Failed")
                        .setNegativeButton("Retry", null)
                        .create()
                        .show();
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }
}
