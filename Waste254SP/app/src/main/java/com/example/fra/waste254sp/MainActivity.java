package com.example.fra.waste254sp;

import android.Manifest;
import android.app.Activity;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;
import com.kosalgeek.asynctask.AsyncResponse;
import com.kosalgeek.asynctask.PostResponseAsyncTask;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, AsyncResponse {
    ListView listView;
    ArrayList previousRequests;
    private LocationListener listener;
    private LocationManager locationManager;
    String clientid,lat="1",lon="1",clientBin, jobId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences prefs = getSharedPreferences("Waste254sp", Context.MODE_PRIVATE);
        Boolean firstTime = prefs.getBoolean("firstTime",true);
        Boolean die = prefs.getBoolean("die",false);
        if(die) {
            finish();
        }
        if (firstTime){
            Intent intent = new Intent(this,LoginActivity.class);
            startActivityForResult(intent,5);
        }else{
            //finish();
            int userid= prefs.getInt("userid",0);
            String email= prefs.getString("email",null);


            //populate(userid);
        }
        ((Button) findViewById(R.id.jobsp)).setText("There is no new request");
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ConnectivityManager connMgr = (ConnectivityManager) getSystemService(Activity.CONNECTIVITY_SERVICE);
                NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();


                if (networkInfo != null && networkInfo.isConnected()){
                    if(jobId!=null) {
                        IntentIntegrator scanIntegrator = new IntentIntegrator(MainActivity.this);
                        scanIntegrator.setDesiredBarcodeFormats(IntentIntegrator.ALL_CODE_TYPES);
                        scanIntegrator.setPrompt("Place the barcode in the box");
                        scanIntegrator.setCameraId(0);
                        scanIntegrator.setBeepEnabled(false);
                        scanIntegrator.setBarcodeImageEnabled(false);
                        scanIntegrator.initiateScan();
                    }
                }
                else{
                    Toast.makeText(MainActivity.this,"No Internet Connection",Toast.LENGTH_LONG).show();
                }
                //Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    //    .setAction("Action", null).show();
                //scanBtn();
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        findJob();
        locationServices();
    }
    public void findJob(){
        PostResponseAsyncTask task;
        task = new PostResponseAsyncTask(MainActivity.this,  MainActivity.this);
        task.execute(getString(R.string.cleanbin));
    }

    public void locationServices(){
        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        listener = new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {

                SharedPreferences prefs = getSharedPreferences("Waste254", Context.MODE_WORLD_WRITEABLE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("longitude", Double.toString(location.getLongitude()));
                editor.putString("latitude", Double.toString(location.getLatitude()));
                editor.commit();

                //((TextView) findViewById(R.id.welcome)).append("\nlong: " + location.getLongitude() + " lat: " + location.getLatitude());
            }

            @Override
            public void onStatusChanged(String s, int i, Bundle bundle) {

            }

            @Override
            public void onProviderEnabled(String s) {

            }

            @Override
            public void onProviderDisabled(String s) {

                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(i);
            }
        };

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
        }


    }

    public void getLocations(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.INTERNET}
                        , 10);
            }
            return;
        }
        //populate(1);
        Toast.makeText(MainActivity.this,"waiting for location",Toast.LENGTH_LONG).show();
        locationManager.requestLocationUpdates("gps", 5000, 0, listener);
        // ((TextView) findViewById(R.id.welcome)).append("waiting for location");
    }


    public void sendRequest(String binId){
        SharedPreferences prefs = getSharedPreferences("Waste254sp", Context.MODE_PRIVATE);

        int userid= prefs.getInt("userid",0);
        PostResponseAsyncTask task;
        HashMap<String, String> post = new HashMap<String, String>();
        post.put("jobId", jobId);
        post.put("userId", Integer.toString(userid));
        task = new PostResponseAsyncTask(MainActivity.this, post, MainActivity.this);
        task.execute(getString(R.string.bincleaned));
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
        if(requestCode==5) {
            if (resultCode == RESULT_OK) {
                SharedPreferences prefs = getSharedPreferences("Waste254", Context.MODE_PRIVATE);

                int userid = prefs.getInt("userid", 0);
                String name = prefs.getString("name", null);
                String email = prefs.getString("email", null);
                //((TextView) findViewById(R.id.welcome)).setText("Welcome Back, mr : " + name + "\n email: " + email + "\n user id: " + userid);

                return;
            } else {
                finish();
            }
        }

        if (result != null) {
            if (result.getContents() == null) {
                Toast.makeText(this, "Scan cancelled", Toast.LENGTH_SHORT).show();
            } else {


                if(clientBin.equals(result.getContents())){
                    Toast.makeText(this, "Bin Scanned", Toast.LENGTH_SHORT).show();
                    sendRequest(result.getContents());}
                else
                    Toast.makeText(this, "You scanned the Wrong Bin!", Toast.LENGTH_LONG).show();
            }
        } else {
            super.onActivityResult(requestCode, resultCode, intent);
        }
    }
    public void maps(View v){
        Intent intent = new Intent(this,MapsActivity.class);
        intent.putExtra("lat",lat);
        intent.putExtra("long",lon);
        startActivity(intent);
       // Toast.makeText(this,"u have a job",Toast.LENGTH_LONG).show();

    }
    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    void showDialog(String dialog){
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Fragment pre = getFragmentManager().findFragmentByTag("dialog");
        if (pre != null){
            ft.remove(pre);
        }
        ft.addToBackStack(null);
        switch (dialog){
            case "help":
                DialogFragment newFragment = new Help();
                newFragment.show(ft,"dialog");
                break;
            case "about":
                DialogFragment about = new About();
                about.show(ft, "dialog");
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button_login, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_profile) {
            // Handle the camera action
        } else if (id == R.id.nav_previous) {

        } else if (id == R.id.nav_help) {
            showDialog("help");
        } else if (id == R.id.nav_contact) {

        } else if (id == R.id.nav_about) {
            //showDialog("about");
        } else if (id == R.id.nav_settings) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void goodBye(){


    }

    @Override
    public void processFinish(String response) {
        Toast.makeText(this,response,Toast.LENGTH_LONG).show();
        try {
            JSONObject jsonResponse = new JSONObject(response);

            boolean success = jsonResponse.getBoolean("success");
            boolean die = jsonResponse.getBoolean("die");
            boolean from = jsonResponse.getBoolean("show");


            if(die){

                SharedPreferences prefs = getSharedPreferences("Waste254sp", Context.MODE_WORLD_WRITEABLE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putBoolean("die", true);
                editor.commit();
            }

            if (success) {

                if(from){
                    Toast.makeText(this, "Got A new Job!", Toast.LENGTH_LONG).show();
                    ((TextView) findViewById(R.id.jobsp)).setText("you got a BIN to Clean");
                    jobId=jsonResponse.getString("id");
                    clientid=jsonResponse.getString("userid");
                    lat=jsonResponse.getString("lat");
                    lon=jsonResponse.getString("long");
                    clientBin=jsonResponse.getString("binId");

                    ((Button) findViewById(R.id.jobsp)).setText("you got a BIN to Clean ::bin id: "+clientBin);

                }else {
                    Toast.makeText(this, "No new Request!", Toast.LENGTH_LONG).show();
                }
            }else{
                Toast.makeText(this,"Try again",Toast.LENGTH_LONG).show();
            }

        } catch (JSONException e) {
            Toast.makeText(this,"Connection failed",Toast.LENGTH_LONG).show();
            e.printStackTrace();

        }

    }
}
