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
import android.graphics.Point;
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
import com.google.android.gms.maps.model.LatLngBounds;
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
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TwitterMapsActivity2 extends FragmentActivity {
    RelativeLayout relative;

    private SeekBar seekBar1;
    private SeekBar seekBar2;
    private TextView textView;
    int upperRange;


    //For drawer
    private ListView mDrawerList;
    private DrawerLayout mDrawerLayout;
    private ArrayAdapter<String> mAdapter;


    LatLng UOIT = new LatLng(43.945791, -78.894689);

    List<String> listTrends = new ArrayList<String>();
    List<String> listDates = new ArrayList<String>();

    List<String> sortedUnique;

    Map<String, ArrayList<LatLng>> tweetMap = new HashMap<String, ArrayList<LatLng>>();



    Set<String> unique;
    Set<String> uniqueDates = new HashSet<String>();
    Set<String> uniqueTrends;// = new HashSet<String>();

    //JSON created to store the coordinates under dates
    JSONObject tweets = new JSONObject();
    TileOverlay tile;


    private GoogleMap mMap;
    LocationManager locManager;// = (LocationManager)getSystemService(LOCATION_SERVICE);
    String provider;
    Location location;
    Criteria criteria = new Criteria();
    List<LatLng> cachePos = new ArrayList<>();
    LatLng currentPos = new LatLng(44.333304, -94.419696);

    ArrayList<LatLng> newOne = new ArrayList<LatLng>();

    LatLngBounds bounds;


    @Override
    protected void onCreate(Bundle savedInstanceState) {

        locManager = (LocationManager) getSystemService(LOCATION_SERVICE);
        String gpsProvider = LocationManager.GPS_PROVIDER;

        //Prompts user to enable location services if it is not already enabled
        if (!locManager.isProviderEnabled(gpsProvider)) {
            /*Toast toast =  Toast.makeText(context, "Location GPS must be enabled!", Toast.LENGTH_LONG);
            toast.show();*/

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
        //mMap = ((SupportMapFragment)getSupportFragmentManager().findFragmentById(R.id.map)).getMap();
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


        relative = (RelativeLayout) findViewById(R.id.RelativeLayout1);
        //new RetrieveTweets().execute(5);
        //edit = (EditText) relative.findViewById(R.id.EditText1);
        //searchButton = (Button) relative.findViewById(R.id.button1);
        /*searchButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (!hasSearched) {
                    String test = edit.getText().toString();
                    Toast.makeText(getApplicationContext(), "Searching for: " + test, Toast.LENGTH_SHORT).show();
                    new RetrieveTweets().execute(test);
                    hasSearched = true;
                } else {
                    if(show) {
                        tile.setVisible(false);
                        show = false;
                    } else {
                        tile.setVisible(true);
                        show = true;
                    }

                }

            }
        });*/
    }

    private void initializeSeekBar(){
        seekBar1 = (SeekBar) findViewById(R.id.seekBar);
        seekBar2 = (SeekBar) findViewById(R.id.seekBar2);
        textView = (TextView) findViewById(R.id.textView);

        //upperRange = seekBar2.getProgress() + seekBar1.getMax();
        // = seekBar2.getProgress() - seekBar1.getProgress();

        /*
        seekBar1.setMax(sortedUnique.size()/2);
        seekBar2.setMax(sortedUnique.size()/2);*/
        seekBar1.setMax(sortedUnique.size());
        seekBar2.setMax(sortedUnique.size());
        seekBar2.setProgress(seekBar2.getMax());

        //final int absoluteTotal = seekBar1.getMax() + seekBar2.getMax();

        //CustomTileOverlay cto = new CustomTileOverlay();
        //cto.add

        seekBar1.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;


            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                if(seekBar2.getProgress() < seekBar1.getProgress()){
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
                try {
                    loadInterval(seekBar1.getProgress(), seekBar2.getProgress() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });

        seekBar2.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int progress = 0;

            @Override
            public void onProgressChanged(SeekBar seekBar, int progressValue, boolean fromUser) {
                progress = progressValue;
                //upperRange = seekBar2.getProgress() + seekBar1.getMax();
                //upperRange = seekBar2.getProgress() - seekBar1.getMax();
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
                try {
                    loadInterval(seekBar1.getProgress(), seekBar2.getProgress() - 1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });


    }
    private void addDrawer(){
        String[] testArray = { "6 hours", "12 hours", "1 day", "1 week", "All"};
        mAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, testArray);
        mDrawerList.setAdapter(mAdapter);
        mDrawerLayout.openDrawer(Gravity.LEFT);
    }

    private void loadInterval(int range1, int range2) throws JSONException{
        final long t0 = System.currentTimeMillis(); //TIMER

        CustomTileOverlay cto = new CustomTileOverlay();
        mMap.clear();
        //tile.clearTileCache();
        for(int i=range1; i <= range2; i++){
            String date = sortedUnique.get(i);
            ArrayList<LatLng> test = new ArrayList<LatLng>();
            test = tweetMap.get(date);
            for(LatLng l : test){
                cto.addPoint(l);
            }
        }
        final long elapsedTimeMillis = System.currentTimeMillis() - t0; //TIMER
        Log.v("loadInterval Timer JSON", "" + elapsedTimeMillis);
        //final long t1 = System.currentTimeMillis(); //TIMER
        tile = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(cto));
        //final long t1_elapsed = System.currentTimeMillis() - t1; //TIMER
        //Log.v("loadInterval Tile", t1_elapsed+"");
    }

    private class DrawerItemClickListener implements ListView.OnItemClickListener {
        Context context = getApplicationContext();
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id){
            Toast.makeText(context, "Working!", Toast.LENGTH_LONG).show();

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


    private String convertDate(String tweetDate, boolean withHour) {
        //Log.v("TimeFormat", tweetDate);
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
        //Month, day, year
        //String date = month + "-" + elements[2] + "-" + elements[5];
        //Year, Month, Day
        String time = elements[3];
        //Log.v("Time", time);
        String[] timeElements = time.split(":");
        //HH:MM:SS
        //Log.v("TimeElements", timeElements[0]+":"+timeElements[1]+":"+timeElements[2]);
        String date;
        //SimpleDateFormat format;
        //Calendar cal = Calendar.getInstance();
        if(withHour) {
            //date = elements[5] + "-" + month + "-" + elements[2] + "-" +
            //        timeElements[0] + ":" + timeElements[1] + ":" + timeElements[2];
            date = elements[5] + "-" + month + "-" + elements[2] + "-" +
                    timeElements[0];

        } else {
            date = elements[5] + "-" + month + "-" + elements[2];
        }

        //uniqueDates.add(date);
        return date;
    }

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
        //Log.v("nextdateMethod", format.format(cal.getTime()));

        return format.format(cal.getTime());

    }

    public List<LatLng> loadJSON(int mode, String text) throws JSONException {
        //String json = null;
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

                final long torontoTimer = System.nanoTime(); //System.currentTimeMillis(); //TIMER
                //Toronto
                if (!torontoCheck) {
                    if (obj.get("text").toString().toLowerCase().contains(text.toLowerCase())) {
                        if (!obj.isNull("coordinates")) {


                            JSONObject coords = new JSONObject(obj.get("coordinates").toString());
                            JSONArray latlng = coords.getJSONArray("coordinates");

                            //Log.v("Tweet Date:", obj.get("created_at").toString());

                            //************************
                            //TWITTER USES LONGITUDE THEN LATITUDE
                            //************************

                            Double lat = Double.parseDouble(latlng.get(1).toString());
                            double lng = Double.parseDouble(latlng.get(0).toString());
                            LatLng pos = new LatLng(lat, lng);
                            list.add(pos);



                            String convertedDate = convertDate(obj.get("created_at").toString(), hour);
                            //listDates.add(convertedDate);

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
                                    //if(current.after(nextInterval) || current.equals(nextInterval)){
                                    if(current.after(nextInterval)){
                                        //torontoCurrentInterval = torontoNextDate;
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
                Log.v("loadJSON torontoTimer", torontoTimerEnd + " nanoseconds"); //TIMER

                final long oshawaTimer = System.nanoTime(); //System.currentTimeMillis(); //TIMER
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
                                    //if(current.after(nextInterval) || current.equals(nextInterval)){
                                    if(current.after(nextInterval)){
                                        //oshawaCurrentInterval = oshawaNextDate;
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
                        Log.v("loadJSON oshawaTimer", oshawaTimerEnd + " nanoseconds");
                    }
                }
            }
            final long jsonRead_t1 = System.currentTimeMillis() - jsonRead_t0; //TIMER
            Log.v("loadJSON Timer", jsonRead_t1 + ""); //TIMER
            //Log.v("Read JSON:", "Success");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        sortedUnique = new ArrayList<String>(uniqueDates);
        Collections.sort(sortedUnique);
        for (String s : sortedUnique) {
            Log.v("Unique Dates", s);
        }
        //Log.v("Num of Coords", "" + list.size());
        //Log.v("JSON", "" + tweets);

        //Get all elements under a specified date
        Log.v("HashMap", tweetMap.get(sortedUnique.get(0)).toString());
        Log.v("HashMap", "" + tweetMap.keySet());

        return list;
    }



    private class RetrieveTweets extends AsyncTask<Integer, Integer, List<LatLng>> {
        Context context = getApplicationContext();

        protected List<LatLng> doInBackground(Integer... test) {
            TwitterMapsActivity2.this.runOnUiThread(new Runnable() {
                public void run() {
                    //Toast.makeText(context, "Retrieving up to " + limit + " tweets", Toast.LENGTH_LONG).show();
                    Toast.makeText(context, "Retrieving tweets.", Toast.LENGTH_LONG).show();
                }
            });
            try {
                Log.v("TestMode", ""+test[0]);
                cachePos = loadJSON(test[0], "");
            } catch (JSONException ex) {
                ex.printStackTrace();
            }

            return cachePos;
        }

        protected void onPostExecute(List<LatLng> test) {
            Log.v("Retrieval:", "Got " + test.size() + " tweets");
            //circleToMap(cachePos);
            //new TweetOverlay().draw(test);
            Toast.makeText(context, "Retrieval complete, displaying " + test.size() + " Tweets.", Toast.LENGTH_SHORT).show();
            initializeSeekBar();


            CustomTileOverlay cto = new CustomTileOverlay();
            for (LatLng l : test) {
                cto.addPoint(l);
            }
            tile = mMap.addTileOverlay(new TileOverlayOptions().tileProvider(cto));
        }
    }

    private class CustomTileOverlay implements TileProvider {
        //Mercator Projection Equation
        //X = longitude / 360; (?)
        //Y = 1/2 * log((1+sin(lat))/(1-sin(lat)) /  -(2*pi)  )

        //Tile Overlay scale equation:
        // 2^(Zoom level)
        private List<newPoint> points = new ArrayList<newPoint>();
        public final int TILE_SIZE_DP = 256;
        public final int mScaleFactor = 2;
        private MercatorProjection mercatorprojection = new MercatorProjection(TILE_SIZE_DP);
        private int dimension = TILE_SIZE_DP * mScaleFactor;
        //public final Bitmap bitmap;
        //public final Canvas canvas = new Canvas(bitmap);


        //May not need?
        //Stuff into getTile if not needed
        //public CustomTileOverlay(Context context){

        //Canvas canvas = new Canvas(bitmap);
        //Paint paint = new Paint();
        //paint.setColor(0x7F96B0FF);
        //}

        @Override
        public Tile getTile(int x, int y, int zoom) {

            float scale = (float) Math.pow(2, zoom) * mScaleFactor;

            Matrix matrix = new Matrix();
            matrix.postScale(scale, scale);
            //matrix.postTranslate(-x * TILE_SIZE_DP * mScaleFactor, -y * TILE_SIZE_DP * mScaleFactor);
            matrix.postTranslate(-x * dimension, -y * dimension);

            /*Bitmap bitmap;
            bitmap = Bitmap.createBitmap((int) (TILE_SIZE_DP * mScaleFactor),
                    (int) (TILE_SIZE_DP * mScaleFactor), Bitmap.Config.ARGB_8888);*/
            Bitmap bitmap = Bitmap.createBitmap(dimension, dimension, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.setMatrix(matrix);

            Paint exteriorPaint = new Paint();
            Paint interiorPaint = new Paint();
            //TODO: Different Colour circles as they expand outwards
            //exteriorPaint.setColor(0x7F96B0FF);
            exteriorPaint.setColor(0x3F96B0FF);
            interiorPaint.setColor(0x7F45E3C1);

            for (newPoint p : points) {
                canvas.drawCircle((float) p.x, (float) p.y, 0.0005f, interiorPaint);
                canvas.drawCircle((float) p.x, (float) p.y, 0.001f, exteriorPaint);
            }
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            //byte[] bitmapData = stream.toByteArray();
            //return new Tile((int) (TILE_SIZE_DP * mScaleFactor),
            //       (int) (TILE_SIZE_DP * mScaleFactor), stream.toByteArray());
            return new Tile(dimension, dimension, stream.toByteArray());
        }

        public void addPoint(LatLng pos) {
            //newPoint p = MercatorProjection(pos);
            points.add(mercatorprojection.toPoint(pos));
        }

        public class MercatorProjection {
            final double worldWidth;

            public MercatorProjection(final double worldwidth) {
                worldWidth = worldwidth;
            }

            public newPoint toPoint(final LatLng latlng) {
                double x = latlng.longitude / 360 + 0.5;
                double y = 0.5 * Math.log((1 + Math.sin(Math.toRadians(latlng.latitude)))
                        / (1 - Math.sin(Math.toRadians(latlng.latitude)))) / (-2 * Math.PI) + 0.5;
                return new newPoint(x * worldWidth, y * worldWidth);
            }
        }

        /*
        public newPoint MercatorProjection(LatLng pos){
            double x = pos.longitude / 360 + 0.5;
            double y = 0.5 * Math.log((1+Math.sin(Math.toRadians(pos.latitude)))
                    / (1-Math.sin(Math.toRadians(pos.latitude)))) / (-2 * Math.PI) + 0.5;

            //Point only accepts int and not double?
            //return new Point(x * TILE_SIZE_DP, y * TILE_SIZE_DP);
            return new newPoint(x* TILE_SIZE_DP, y * TILE_SIZE_DP);*/
    }


    public class newPoint {
        public double x;
        public double y;

        public newPoint(double x, double y) {
            this.x = x;
            this.y = y;
        }
    }

        private Point toPixels(LatLng latlng) {
            return mMap.getProjection().toScreenLocation(latlng);
        }

    public class Tweets{
        private Double Lat;
        private Double Lng;
        private Collection<LatLng> coordinates = Collections.emptyList();

        public Tweets(Double Lat, Double Lng){
            this.Lat = Lat;
            this.Lng = Lng;
        }

        public Double getLat(){
            return Lat;
        }

        public Double getLng(){
            return Lng;
        }

        public String toString(){
            return Lat + ", " + Lng;
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
            LatLng currentPos = new LatLng(43.945791, -78.894689);
            //Add a marker on the map with the current position
            mMap.addMarker(new MarkerOptions().position(currentPos).title("Here"));

            //Controls the camera so it would zoom into current position
            CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(currentPos, 13);
            //mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));
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
