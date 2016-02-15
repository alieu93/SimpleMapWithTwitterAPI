package adamlieu.simplemapwithtwitterapi;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import twitter4j.*;
import twitter4j.Query;
import twitter4j.QueryResult;
import twitter4j.conf.ConfigurationBuilder;

public class TwitterMapsActivity extends FragmentActivity {

    EditText edit;
    Button searchButton;
    RelativeLayout relative;
    boolean hasSearched = false;


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

    //Tweets
    List<twitter4j.Status> tweets;
    List<twitter4j.Status> tweetsWithGeo = new ArrayList<>();
    //Twitter profile pictures as bitmaps
    private List<Bitmap> image = new ArrayList<>();
    //Twitter profile pictures as overlays
    private List<GroundOverlayOptions> overlaysOptions = new ArrayList<>();
    //Twitter embed URLs
    private List<String> mediaEmbeds = new ArrayList<>();


    private List<LatLng> cachePos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        /*relative = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        edit = (EditText) relative.findViewById(R.id.EditText1);
        searchButton = (Button) relative.findViewById(R.id.button1);*/
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



        //new RetrieveTimeline().execute("Test");

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            final Context context = getApplicationContext();

            @Override
            public void onMapClick(LatLng position) {
                /*
                LatLng center = circle.getCenter();
                double radius = circle.getRadius();
                float[] distance = new float[1];
                Location.distanceBetween(position.latitude, position.longitude, center.latitude, center.longitude, distance);
                boolean clicked = distance[0] < radius;*/

                //for(GroundOverlayOptions i : overlaysOptions){
                for (int i = 0; i < overlaysOptions.size(); i++) {

                    //LatLng center = i.getLocation();
                    //double radius = i.getWidth() / 2;
                    LatLng center = overlaysOptions.get(i).getLocation();
                    double radius = overlaysOptions.get(i).getWidth() / 2;
                    float[] distance = new float[1];
                    Location.distanceBetween(position.latitude, position.longitude,
                            center.latitude, center.longitude, distance);
                    boolean clicked = distance[0] < radius;


                    if (clicked) {
                        //Toast.makeText(context, "CLICKED!", Toast.LENGTH_SHORT).show();
                        /*
                        Toast.makeText(context, "\t" + tweetsWithGeo.get(i).getUser().getName() + "\n\n" +
                                tweetsWithGeo.get(i).getText(),
                                Toast.LENGTH_SHORT).show();*/
                        /*
                        AlertDialog.Builder builder = new AlertDialog.Builder(TwitterMapsActivity.this);
                        builder.setMessage(tweetsWithGeo.get(i).getText())
                                .setTitle(tweetsWithGeo.get(i).getUser().getName());
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int id) {

                            }
                        });
                        AlertDialog dialog = builder.create();
                        dialog.show();*/
                        FragmentManager fm = getFragmentManager();
                        DialogFragment newFragment = TweetFragment.newInstance(
                                tweetsWithGeo.get(i).getUser().getName(),
                                tweetsWithGeo.get(i).getText(),
                                mediaEmbeds.get(i));
                        newFragment.show(fm, tweetsWithGeo.get(i).getUser().getName());

                    }

                }
            }
        });

        relative = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        edit = (EditText) relative.findViewById(R.id.EditText1);
        searchButton = (Button) relative.findViewById(R.id.button1);

        searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view){
                if(edit.getText().toString() != null){
                    if(!hasSearched) {
                        String test = edit.getText().toString();
                        Toast.makeText(getApplicationContext(), "Searching for: " + test, Toast.LENGTH_SHORT).show();
                        new RetrieveTimeline().execute(test);
                        hasSearched = true;
                    }
                }
            }
        });

    }


    private class RetrieveTimeline extends AsyncTask<String, String, String> {
        Context context = getApplicationContext();
        ConfigurationBuilder cb = new ConfigurationBuilder()
                .setDebugEnabled(true)
                .setOAuthConsumerKey("3L9ScMzKYZSy8lmwEJqT7mIS5")
                .setOAuthConsumerSecret("OOd5DnGNagJ4PpkcIZxbNa6pXQEQKTPLGfyW86K2nbVkxwR2UB")
                .setOAuthAccessToken("4244372086-mXU7CPfRpbBGGyI14JjQYgI4YICm4lIB0oc75hV")
                .setOAuthAccessTokenSecret("7NTHv3zE1i5LyWqpFUEXRj0czi7H0qkGzc5HvlI5UJibh");;


        TwitterFactory tf = new TwitterFactory(cb.build());
        Twitter twit = tf.getInstance();

        double lat = 43.6532;
        double lon = -79.3832;
        int radius = 100;
        String mesUnit = "km";

        protected String doInBackground(String... test){
            Query query = new Query(test[0]).geoCode(new GeoLocation(lat, lon), radius, mesUnit);
            Log.v("Query", test[0]);
            /*
            try {
                statuses = twit.getHomeTimeline();
                for(twitter4j.Status status : statuses){
                    //System.out.println(status.getUser().getName() + ":" + status.getText());
                    Log.v("TEST TWIT", status.getUser().getName() + ":" + status.getText());
                }*/
            try{
                query.count(500);
                QueryResult result = twit.search(query);
                tweets = result.getTweets();
            } catch (TwitterException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

            for(twitter4j.Status tweet : tweets) {
                //String url = tweet.getUser().getProfileImageURL();
                String url = tweet.getUser().getBiggerProfileImageURL();
                try {
                    InputStream in = new BufferedInputStream(new URL(url).openStream(), 4096);
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    image.add(bitmap);
                } catch (IOException e) {
                    Log.e("URL IO", "Error reading URL");
                }

                /*
                //Test: Just for getting the embedded URL for tweets
                String mediaURL = null;
                for(MediaEntity mediaEnt : tweet.getMediaEntities()) {
                    mediaURL = mediaEnt.getMediaURL();
                    //Log.v("MEDIAENTITY: ", mediaEnt.getMediaURL());
                    counter++;
                }
                if(mediaURL != null){
                    mediaEmbeds.add(mediaURL);
                } else {
                    mediaEmbeds.add(null);
                }
                Log.v("MediaEntity Counter: ", "" + counter);
                */
            }
            return test[0];
        }

        protected void onPostExecute(String string){
            Toast.makeText(context, "Search complete, displaying " + tweets.size() + " results." , Toast.LENGTH_SHORT).show();
            Log.v("TESTTWIT ", "" + tweets.size());
            int count = 0;
            //Toast.makeText(context, "image array: " + image.size(), Toast.LENGTH_LONG).show();
            //Toast.makeText(context, "tweets array: " + tweets.size(), Toast.LENGTH_LONG).show();
            Log.v("ARRAY TEST", "image array: " + image.size() + "tweets array: " + tweets.size());
            /*
            for(String x : mediaEmbeds){
                Log.v("MediaEntity: ", "" + x);
            }*/
            int counter = 0;
            for(twitter4j.Status tweet : tweets) {

                Log.v("TESTTWIT ", tweet.getUser().getScreenName()
                        + " --- " + tweet.getText()
                        + " --- " + tweet.getCreatedAt()
                        + " --- " + tweet.getGeoLocation()
                        + " --- " + tweet.getUser().getBiggerProfileImageURL());

                if(tweet.getGeoLocation() != null) {
                    tweetsWithGeo.add(tweet);

                    //Adding the embed URLs to a separate list
                    String mediaURL = null;
                    for(MediaEntity mediaEnt : tweet.getMediaEntities()) {
                        mediaURL = mediaEnt.getMediaURL();
                        //Log.v("MEDIAENTITY: ", mediaEnt.getMediaURL());
                        counter++;
                    }
                    if(mediaURL != null){
                        mediaEmbeds.add(mediaURL);
                    } else {
                        mediaEmbeds.add(null);
                    }
                    Log.v("MediaEntity Counter: ", "" + counter);

                    /*
                    mMap.addCircle(new CircleOptions()
                            .center(new LatLng(tweet.getGeoLocation().getLatitude(),
                                    tweet.getGeoLocation().getLongitude()))
                            .radius(25)
                            .strokeWidth(0)
                                    //.fillColor(0x7F96B0FF));
                            .fillColor(Color.BLUE));*/

                    //String url = tweet.getUser().getProfileImageURL();
                    LatLng loc = new LatLng(tweet.getGeoLocation().getLatitude(),
                            tweet.getGeoLocation().getLongitude());

                    //for(Bitmap bit : image) {
                    if(count < tweets.size()) {
                        Log.v("Array", "" + count);
                        if(count == image.size()){
                            break;
                        }
                        if(image.get(count) != null) {
                            GroundOverlayOptions overlay = new GroundOverlayOptions()
                                    .image(BitmapDescriptorFactory.fromBitmap(image.get(count)))
                                    .position(loc, 80f);
                            overlaysOptions.add(overlay);
                        }
                    }
                    for(GroundOverlayOptions i : overlaysOptions){
                        mMap.addGroundOverlay(i);
                    }

                }
                count++;
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
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, 1);
            mMap.animateCamera(cameraUpdate);
            //randGridToMap(UOIT, 200);
        }
    }
}
