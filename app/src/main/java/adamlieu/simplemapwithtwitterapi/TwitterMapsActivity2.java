package adamlieu.simplemapwithtwitterapi;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Tile;
import com.google.android.gms.maps.model.TileOverlay;
import com.google.android.gms.maps.model.TileOverlayOptions;
import com.google.android.gms.maps.model.TileProvider;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class TwitterMapsActivity2 extends FragmentActivity {
    //RelativeLayout relative;

    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private TextView textView;

    //For drawer
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;

    List<String> sortedUnique;

    Map<String, ArrayList<LatLng>> tweetMap = new HashMap<String, ArrayList<LatLng>>();

    Set<String> uniqueDates = new HashSet<String>();

    TileOverlay tile;

    private GoogleMap mMap;
    LocationManager locManager;
    String provider;
    Location location;
    Criteria criteria = new Criteria();

    //List<LatLng> cachePos = new ArrayList<>();



    @Override
    protected void onCreate(Bundle savedInstanceState) {

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
        setContentView(R.layout.activity_twitter_maps2);
        setUpMapIfNeeded();

        mMap.setMyLocationEnabled(false);
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        //Drawer
        mDrawerList = (ListView) findViewById(R.id.left_drawer);
        mDrawerList.bringToFront();
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        addDrawer();
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        //relative = (RelativeLayout) findViewById(R.id.RelativeLayout1);
    }

    /**
     * AddDrawer():
     * Function for initializing the navigation drawer
     */
    private void addDrawer(){
        String[] testArray = { "6 hours", "12 hours", "1 day", "1 week", "All"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    /**
     * DrawerItemClickListener: class that runs whenever an option is selected on the drawer
     */
    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            //Toast.makeText(context, "Working!", Toast.LENGTH_LONG).show();
            switch(position) {
                case 0:
                    new RetrieveTweets().execute(1);
                    mDrawerLayout.closeDrawers();
                    break;
                case 1:
                    new RetrieveTweets().execute(2);
                    mDrawerLayout.closeDrawers();
                    break;
                case 2:
                    new RetrieveTweets().execute(3);
                    mDrawerLayout.closeDrawers();
                    break;
                case 3:
                    new RetrieveTweets().execute(4);
                    mDrawerLayout.closeDrawers();
                    break;
                case 4:
                    new RetrieveTweets().execute(5);
                    mDrawerLayout.closeDrawers();
                    break;
            }

        }
    }

    /**
     * initializeSeekBar():
     * Function for initializing the seekbars by setting the maximum values for both of them.
     * Serves as a pseudo-two way seekbar, seekbar1 is the lower limit and seekbar2 is the upper limit
     * Upon any kind of change in the seekbar value, it redraws the map circles
     */
    private void initializeSeekBar(){
        seekBar1 = (SeekBar) findViewById(R.id.seekBar);
        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        textView = (TextView) findViewById(R.id.textView);
        seekBar1.setMax(sortedUnique.size());
        seekBar2.setMax(sortedUnique.size());
        seekBar2.setProgress(seekBar2.getMax());
        loadInterval(seekBar1.getProgress(), seekBar2.getProgress() - 1);
        textView.setText("Range: " + sortedUnique.get(seekBar1.getProgress()) + " to " + sortedUnique.get(seekBar2.getProgress() - 1)
                + "   " + seekBar1.getProgress() + "---" + seekBar2.getProgress());


        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                if (seekBar2.getProgress() < seekBar1.getProgress()) {
                    seekBar1.setProgress(seekBar2.getProgress());
                    seekBar2.setProgress(seekBar2.getProgress() + 1);
                } else {
                    seekBar1.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                textView.setText("Range: " + sortedUnique.get(seekBar1.getProgress()) + " to " + sortedUnique.get(seekBar2.getProgress() - 1)
                        + "   " + seekBar1.getProgress() + "---" + seekBar2.getProgress());
                loadInterval(seekBar1.getProgress(), seekBar2.getProgress() - 1);
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                if (seekBar2.getProgress() < seekBar1.getProgress()) {
                    seekBar2.setProgress(seekBar1.getProgress() + 1);
                } else {
                    seekBar2.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

                textView.setText("Range: " + sortedUnique.get(seekBar1.getProgress()) + " to " + sortedUnique.get(seekBar2.getProgress() - 1)
                        + "   " + seekBar1.getProgress() + "---" + seekBar2.getProgress());
                loadInterval(seekBar1.getProgress(), seekBar2.getProgress() - 1);
            }
        });


    }

    /**
     * loadInterval: Function is used to draw the points on the map using an upper and lower limit
     * @param range1: lower limit of the time interval
     * @param range2: upper limit of the time interval
     */
    private void loadInterval(int range1, int range2) {
        final long t0 = System.currentTimeMillis(); //TIMER

        int intervalRange = range2 - range1;
        double perInterval = 100 / (double) intervalRange;
        double singleInterval = perInterval;

        CustomTileOverlay cto = new CustomTileOverlay();
        mMap.clear();
        for(int i=range1; i <= range2; i++){
            // Determines the colour for each time interval
            Paint paint = new Paint();
            double red = (255 * singleInterval) / 100;
            double green = (255 * (100 - singleInterval)) / 100;
            double blue = 0;
            double alpha = (255 * singleInterval) / 100;
            if(red > 255) red = 255;
            if(red < 0) red = 0;
            if(green > 255) green = 255;
            if(green < 0) green = 0;
            //if(alpha > 255) alpha = 255;
            if (alpha > 200) alpha = 200;
            if(alpha < 60) alpha = 60;
            paint.setARGB((int) alpha, (int) red, (int) green, (int) blue);
            String date = sortedUnique.get(i);
            ArrayList<LatLng> test = new ArrayList<LatLng>();

            test = tweetMap.get(date);

            for(LatLng l : test) {
                cto.addPoint(l, paint);
            }
            singleInterval += perInterval;
        }
        final long elapsedTimeMillis = System.currentTimeMillis() - t0; //TIMER
        Log.v("loadInterval Timer JSON", "" + elapsedTimeMillis);
        tile = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(cto));

    }

    /**
     * convertDate
     * @param tweetDate: The string from the JSON file
     * @param withHour: Whether or not the hour should be included, this is for the time intervals such as 6 hour and 12 hour
     * @return: A formatted string that can be used for later
     */

    private String convertDate(String tweetDate, boolean withHour) {
        String[] elements = tweetDate.split("\\s+");
        String month = null;
        switch (elements[1]) {
            case "Jan":
                month = "01";
                break;
            case "Feb":
                month = "02";
                break;
            case "Mar":
                month = "03";
                break;
            case "Apr":
                month = "04";
                break;
            case "May":
                month = "05";
                break;
            case "Jun":
                month = "06";
                break;
            case "Jul":
                month = "07";
                break;
            case "Aug":
                month = "08";
                break;
            case "Sep":
                month = "09";
                break;
            case "Oct":
                month = "10";
                break;
            case "Nov":
                month = "11";
                break;
            case "Dec":
                month = "12";
                break;
        }
        //Year, Month, Day
        String time = elements[3];
        String[] timeElements = time.split(":");
        //HH:MM:SS
        String date;
        if(withHour) {
            date = elements[5] + "-" + month + "-" + elements[2] + "-" +
                    timeElements[0];

        } else {
            date = elements[5] + "-" + month + "-" + elements[2];
        }

        return date;
    }

    /**
     * getNextDate
     * @param date: The current time interval
     * @param mode: Determines the time interval (6 hours, 12 hours, 1 day, 1 week)
     * @return: The next time interval in the series
     */
    private String getNextDate(String date, int mode){
        //Hourly, 3 hours, daily, weekly, all
        //yyyy-mm-dd-hh
        String[] elements = date.split("-");
        Calendar cal = Calendar.getInstance();
        Integer year = Integer.parseInt(elements[0]);
        Integer month = Integer.parseInt(elements[1]);
        Integer day = Integer.parseInt(elements[2]);
        Integer hour = 0;
        if(elements.length > 3) {
            hour = Integer.parseInt(elements[3]);
        }

        SimpleDateFormat format = null;
        switch(mode){
            case 1:
                format = new SimpleDateFormat("yyyy-MM-dd-HH");
                cal.set(year, month, day, hour, 0, 0);
                //Can't seem to actually increment a day, so increment a month and a day then decrement a month
                cal.add(Calendar.HOUR_OF_DAY, 6);
                cal.add(Calendar.MONTH, -1);
                break;
            case 2:
                format = new SimpleDateFormat("yyyy-MM-dd-HH");
                cal.set(year, month, day, hour, 0, 0);
                //Can't seem to actually increment a day, so increment a month and a day then decrement a month
                cal.add(Calendar.HOUR_OF_DAY, 12);
                cal.add(Calendar.MONTH, -1);
                break;
            case 3:
                format = new SimpleDateFormat("yyyy-MM-dd");
                cal.set(year, month, day);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.MONTH, -1);
                break;
            case 4:
                format = new SimpleDateFormat("yyyy-MM-dd");
                cal.set(year, month, day);
                cal.add(Calendar.WEEK_OF_MONTH, 1);
                cal.add(Calendar.MONTH, -1);
                break;
            case 5:
                format = new SimpleDateFormat("yyyy-MM-dd");
                cal.set(year, month, day);
                cal.add(Calendar.DAY_OF_MONTH, 1);
                cal.add(Calendar.MONTH, -1);
                break;

        }

        return format.format(cal.getTime());

    }


    public List<LatLng> loadJSON(int mode, String text) throws JSONException {
        List<LatLng> list = new ArrayList<>();
        boolean torontoCheck = false;
        boolean oshawaCheck = false;
        boolean hour;
        final DateFormat format;
        if(mode < 3){
            hour = true;
            format = new SimpleDateFormat("yyyy-MM-dd-HH");
        } else {
            hour = false;
            format = new SimpleDateFormat("yyyy-MM-dd");
        }

        String torontoNextDate = null;
        String oshawaNextDate = null;
        String torontoCurrentInterval = null;
        String oshawaCurrentInterval = null;

        try {
            InputStream isT = getResources().openRawResource(R.raw.toronto);
            InputStream isO = getResources().openRawResource(R.raw.oshawa);
            BufferedReader torontoReader = new BufferedReader(new InputStreamReader(isT, "UTF-8"));
            BufferedReader oshawaReader = new BufferedReader(new InputStreamReader(isO, "UTF-8"));
            String torLine;
            String oshLine;

            final long jsonRead_t0 = System.currentTimeMillis(); //TIMER
            while (true) {
                JSONObject obj = null;
                JSONObject oshObj = null;
                torLine = torontoReader.readLine();
                oshLine = oshawaReader.readLine();
                if (torLine == null) {
                    torontoCheck = true;
                } else {
                    obj = new JSONObject(torLine);
                }

                if (oshLine == null) {
                    oshawaCheck = true;
                } else {
                    oshObj = new JSONObject(oshLine);
                }

                if (oshawaCheck && torontoCheck) {
                    break;
                }

                final long torontoTimer = System.nanoTime();//TIMER
                //Toronto
                if (!torontoCheck) {
                    if (obj.get("text").toString().toLowerCase().contains(text.toLowerCase())) {
                        if (!obj.isNull("coordinates")) {


                            JSONObject coords = new JSONObject(obj.get("coordinates").toString());
                            JSONArray latlng = coords.getJSONArray("coordinates");


                            //************************
                            //TWITTER USES LONGITUDE THEN LATITUDE
                            //************************

                            Double lat = Double.parseDouble(latlng.get(1).toString());
                            double lng = Double.parseDouble(latlng.get(0).toString());
                            LatLng pos = new LatLng(lat, lng);
                            list.add(pos);

                            String convertedDate = convertDate(obj.get("created_at").toString(), hour);

                            //Initialize intervals at beginning, first part of if statement should only ever execute at the very beginning
                            if(torontoNextDate == null || torontoCurrentInterval == null){
                                torontoNextDate = getNextDate(convertedDate, mode);
                                torontoCurrentInterval = convertedDate;
                                uniqueDates.add(convertedDate);

                                ArrayList<LatLng> test = new ArrayList<LatLng>();
                                test.add(pos);
                                tweetMap.put(torontoCurrentInterval, test);
                            } else {
                                try {
                                    Date current = format.parse(convertedDate);
                                    Date nextInterval = format.parse(torontoNextDate);

                                    //If the current line has a date equal to the next interval, we set a new interval
                                    if(current.after(nextInterval)){
                                        torontoCurrentInterval = convertedDate;
                                        torontoNextDate = getNextDate(convertedDate, mode);
                                        uniqueDates.add(convertedDate);

                                        ArrayList<LatLng> test = new ArrayList<LatLng>();
                                        test.add(pos);
                                        tweetMap.put(torontoCurrentInterval, test);

                                    //If still in current time interval, place it in that category
                                    } else {
                                        ArrayList test = tweetMap.get(torontoCurrentInterval);
                                        test.add(pos);
                                        tweetMap.put(torontoCurrentInterval, test);
                                    }
                                } catch(ParseException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                    }
                }
                final long torontoTimerEnd = System.nanoTime() - torontoTimer; //System.currentTimeMillis() - torontoTimer; //TIMER

                final long oshawaTimer = System.nanoTime(); //TIMER
                //Oshawa
                if (!oshawaCheck) {
                    if (oshObj.get("text").toString().toLowerCase().contains(text.toLowerCase())) {
                        if (!oshObj.isNull("coordinates")) {

                            JSONObject coords = new JSONObject(oshObj.get("coordinates").toString());
                            JSONArray latlng = coords.getJSONArray("coordinates");

                            //************************
                            //TWITTER USES LONGITUDE THEN LATITUDE
                            //************************


                            Double lat = Double.parseDouble(latlng.get(1).toString());
                            double lng = Double.parseDouble(latlng.get(0).toString());
                            LatLng pos = new LatLng(lat, lng);
                            list.add(pos);


                            String convertedDate = convertDate(oshObj.get("created_at").toString(), hour);

                            //Initialize intervals at beginning, first part of if statement should only ever execute at the very beginning
                            if(oshawaNextDate == null || oshawaCurrentInterval == null){
                                oshawaNextDate = getNextDate(convertedDate, mode);
                                oshawaCurrentInterval = convertedDate;
                                uniqueDates.add(convertedDate);

                                ArrayList<LatLng> test = new ArrayList<LatLng>();
                                test.add(pos);
                                tweetMap.put(oshawaCurrentInterval, test);
                            } else {
                                try {
                                    Date current = format.parse(convertedDate);
                                    Date nextInterval = format.parse(oshawaNextDate);

                                    //If the current line has a date equal to the next interval, we set a new interval
                                    if(current.after(nextInterval)){
                                        oshawaCurrentInterval = convertedDate;
                                        oshawaNextDate = getNextDate(convertedDate, mode);
                                        uniqueDates.add(oshawaCurrentInterval);

                                        ArrayList<LatLng> test = new ArrayList<LatLng>();
                                        test.add(pos);
                                        tweetMap.put(oshawaCurrentInterval, test);
                                    } else {
                                        //If still in current time interval, place it in that category
                                        ArrayList test = tweetMap.get(oshawaCurrentInterval);
                                        test.add(pos);
                                        tweetMap.put(oshawaCurrentInterval, test);
                                    }
                                } catch(ParseException e){
                                    e.printStackTrace();
                                }
                            }
                        }
                        final long oshawaTimerEnd = System.nanoTime() - oshawaTimer; //System.currentTimeMillis() - oshawaTimer;
                    }
                }
            }
            final long jsonRead_t1 = System.currentTimeMillis() - jsonRead_t0; //TIMER
            Log.v("loadJSON Timer", jsonRead_t1 + ""); //TIMER
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        sortedUnique = new ArrayList<String>(uniqueDates);
        Collections.sort(sortedUnique);
        for (String s : sortedUnique) {
            Log.v("Unique Dates", s);
        }
        //Get all elements under a specified date
        Log.v("HashMap", tweetMap.get(sortedUnique.get(0)).toString());
        Log.v("HashMap", "" + tweetMap.keySet());

        return list;
    }


    /**
     * Class that performs background operations by reading the text files, parsing the JSONs and updating the UI thread
     */
    private class RetrieveTweets extends AsyncTask<Integer, Integer, List<LatLng>> {
        Context context = getApplicationContext();

        protected List<LatLng> doInBackground(Integer... test) {
            List<LatLng> cachePos = new ArrayList<>();
            TwitterMapsActivity2.this.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Retrieving tweets.", Toast.LENGTH_LONG).show();
                }
            });
            try {
                cachePos = loadJSON(test[0], "");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            return cachePos;
        }

        protected void onPostExecute(List<LatLng> test) {
            Log.v("Retrieval:", "Got " + test.size() + " tweets");
            Toast.makeText(context, "Retrieval complete, displaying " + test.size() + " Tweets.", Toast.LENGTH_SHORT).show();
            initializeSeekBar();
        }
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
            LatLng currentPos = new LatLng(43.945791, -78.894689);
            //Add a marker on the map with the current position
            //mMap.addMarker(new MarkerOptions().position(currentPos).title("Here"));

            //Controls the camera so it would zoom into current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, 13);
            mMap.animateCamera(cameraUpdate);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_twitter_maps_activity2, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
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
}
