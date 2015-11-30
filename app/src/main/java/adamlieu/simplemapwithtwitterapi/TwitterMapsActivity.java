package adamlieu.simplemapwithtwitterapi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.location.*;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.List;

import twitter4j.*;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterMapsActivity extends FragmentActivity {

    //static List<twitter4j.Status> tweets;

    Circle circle;

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    //static ConfigurationBuilder cb = new ConfigurationBuilder();
    LatLng UOIT = new LatLng(43.945791, -78.894689);

    LocationManager locManager;
    Location location;
    String provider;
    Criteria criteria = new Criteria();

    //For drawer
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*cb.setDebugEnabled(true)
                .setOAuthConsumerKey("3L9ScMzKYZSy8lmwEJqT7mIS5")
                .setOAuthConsumerSecret("OOd5DnGNagJ4PpkcIZxbNa6pXQEQKTPLGfyW86K2nbVkxwR2UB")
                .setOAuthAccessToken("4244372086-mXU7CPfRpbBGGyI14JjQYgI4YICm4lIB0oc75hV")
                .setOAuthAccessTokenSecret("7NTHv3zE1i5LyWqpFUEXRj0czi7H0qkGzc5HvlI5UJibh");*/
        //Context context = getApplicationContext(); //For Toast
        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String gpsProvider = LocationManager.GPS_PROVIDER;

        //Prompts user to enable location services if it is not already enabled
        if (!locManager.isProviderEnabled(gpsProvider)) {

            //Alert Dialog
            AlertDialog alertDialog = new AlertDialog.Builder(this).create();
            alertDialog.setTitle("Notice");
            alertDialog.setMessage("Location GPS must be enabled!");
            alertDialog.setButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    String locConfig = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
                    Intent enableGPS = new Intent(locConfig);
                    startActivity(enableGPS);
                }
            });
            alertDialog.show();
        }


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_twitter_maps);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(true);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(true);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        //Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.bringToFront();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawer();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

        new RetrieveTimeline().execute("Test");
        /*
        try {
            List<Status> statuses = twit.getHomeTimeline();
            for(Status status : statuses){
                //System.out.println(status.getUser().getName() + ":" + status.getText());
                Log.v("TEST TWIT", status.getUser().getName() + ":" + status.getText());
            }
        } catch (TwitterException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }*/

    }

    private class RetrieveTimeline extends AsyncTask<String, String, String> {
        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey("3L9ScMzKYZSy8lmwEJqT7mIS5")
                .setOAuthConsumerSecret("OOd5DnGNagJ4PpkcIZxbNa6pXQEQKTPLGfyW86K2nbVkxwR2UB")
                .setOAuthAccessToken("4244372086-mXU7CPfRpbBGGyI14JjQYgI4YICm4lIB0oc75hV")
                .setOAuthAccessTokenSecret("7NTHv3zE1i5LyWqpFUEXRj0czi7H0qkGzc5HvlI5UJibh");;


        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twit = tf.getInstance();
        List<twitter4j.Status> tweets;
        //List<twitter4j.Status> statuses;

        double lat = 43.6532;
        double lon = -79.3832;
        int radius = 100;
        String mesUnit = "km";

        Query query = new Query("movie%20:(").geoCode(new GeoLocation(lat, lon), radius, mesUnit);

        protected String doInBackground(String... test){
            /*
            try {
                statuses = twit.getHomeTimeline();
                for(twitter4j.Status status : statuses){
                    //System.out.println(status.getUser().getName() + ":" + status.getText());
                    Log.v("TEST TWIT", status.getUser().getName() + ":" + status.getText());
                }*/
            try{
                query.count(250);
                QueryResult result = twit.search(query);
                tweets = result.getTweets();
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            return test[0];
        }

        protected void onPostExecute(String string){
            Log.v("TESTTWIT ", "" + tweets.size());
            for(twitter4j.Status tweet : tweets) {
                /*
                Log.v("TESTTWIT ", tweet.getUser().getScreenName()
                        + " --- " + tweet.getText()
                        + " --- " + tweet.getCreatedAt()
                        + " --- " + tweet.getGeoLocation());
                        */
                if(tweet.getGeoLocation() != null) {
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(tweet.getGeoLocation().getLatitude(),
                                    tweet.getGeoLocation().getLongitude()))
                            .radius(25)
                            .strokeWidth(0)
                            //.fillColor(0x7F96B0FF));
                            .fillColor(Color.BLUE));

                    mMap.addMarker(new MarkerOptions().position(new LatLng(tweet.getGeoLocation().getLatitude(),
                            tweet.getGeoLocation().getLongitude()))
                        .title(tweet.getUser().getScreenName())
                        .snippet(tweet.getText()));
                }
            }
        }
    }

    private void addDrawer(){
        String[] testArray = { "Normal", "Satellite", "Hybrid", "Terrain"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Toast.makeText(context, "Working!", Toast.LENGTH_LONG).show();

            switch(position) {
                case 0:
                    mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL);
                    break;
                case 1:
                    mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                    break;
                case 2:
                    mMap.setMapType(GoogleMap.MAP_TYPE_HYBRID);
                    break;
                case 3:
                    mMap.setMapType(GoogleMap.MAP_TYPE_TERRAIN);
                    break;
            }

        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p/>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p/>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p/>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //mMap.addMarker(new MarkerOptions().position(new LatLng(0, 0)).title("Marker"));
        provider = locManager.getBestProvider(criteria, true); // Name for best provider
        //Check for permissions if they are granted
        if((ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) &&
                (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED)){
            return;
        }
        location = locManager.getLastKnownLocation(provider); // Get last known location, basically current location
        if(location != null){
            //Get current long and lat positions
            //LatLng currentPos = new LatLng(location.getLatitude(), location.getLongitude());

            //LatLng currentPos = new LatLng(43.945791, -78.894689); //UOIT
            LatLng currentPos = new LatLng(43.6532, -79.3832); //Toronto
            //Add a marker on the map with the current position
            mMap.addMarker(new MarkerOptions().position(currentPos).title("Toronto"));

            //Controls the camera so it would zoom into current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, 15);
            mMap.animateCamera(cameraUpdate);
            //randGridToMap(UOIT, 200);
        }
    }
}
