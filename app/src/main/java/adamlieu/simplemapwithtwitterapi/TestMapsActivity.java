package adamlieu.simplemapwithtwitterapi;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Display;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.GroundOverlay;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

public class TestMapsActivity extends FragmentActivity implements OnMapReadyCallback {
    private GoogleMap mMap;
    private LatLng currentPos;
    private LatLngBounds bounds;

    private List<LatLng> points = new ArrayList<>();
    private float radius = 100;
    private LatLngBounds.Builder build = new LatLngBounds.Builder();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_test_maps);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        /*
        points.add(new LatLng(44.968046, -94.420307));
        points.add(new LatLng(44.33328,-89.132008));
        points.add(new LatLng(33.755787,-116.359998));
        points.add(new LatLng(33.844843,-116.54911));
        points.add(new LatLng(44.92057,-93.44786));
        points.add(new LatLng(44.240309,-91.493619));
        points.add(new LatLng(44.968041,-94.419696));
        points.add(new LatLng(44.333304,-89.132027));
        points.add(new LatLng(33.755783,-116.360066));
        points.add(new LatLng(33.844847,-116.549069));
        points.add(new LatLng(44.920474,-93.447851));
        points.add(new LatLng(44.240304,-91.493768));
        */
        points.add(new LatLng(43.6532, -79.3832));
        points.add(new LatLng(43.8662067,-79.4423077));

        currentPos = new LatLng(44.333304,-94.419696);

        //bounds = new LatLngBounds(new LatLng(33.0, -120.0), new LatLng(45.0, -85.0));
        for(LatLng i : points){
            build.include(new LatLng(i.latitude, i.longitude));
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        mMap.getUiSettings().setZoomControlsEnabled(true);
        mMap.getUiSettings().setCompassEnabled(false);
        mMap.getUiSettings().setMapToolbarEnabled(true);

        // Add a marker in Sydney and move the camera
        mMap.addMarker(new MarkerOptions().position(currentPos).title("You are here"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPos));

        Bitmap overlayBitmap = createOverlayBitmap(this.points);
        BitmapDescriptor desc = BitmapDescriptorFactory.fromBitmap(overlayBitmap);
        Log.v("Bounds", "" + build.build());
        GroundOverlay t = mMap.addGroundOverlay(new GroundOverlayOptions().image(desc).positionFromBounds(build.build()));
        Log.v("Overlay POS", ""+t.getPosition());
    }

    public Bitmap createOverlayBitmap(List<LatLng> points) {
        Paint opaque50 = new Paint();
        opaque50.setARGB(128, 0, 0, 255);

        Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;

        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        for (LatLng mapPos: points) {
            Point screenPos = toPixels(mapPos);
            Log.v("ScreenPos", screenPos.x + "\t" + screenPos.y);
            canvas.drawCircle((float)screenPos.x, (float)screenPos.y, radius, opaque50);
        }

        return bitmap;
    }

    private Point toPixels(LatLng latlng) {
        return mMap.getProjection().toScreenLocation(latlng);
    }
}
